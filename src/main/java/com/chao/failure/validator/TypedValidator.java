package com.chao.failure.validator;

import com.chao.failure.constant.FailureConst;
import com.chao.failure.internal.core.ResponseCode;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * TypedValidator - Abstract generic validator class.
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
@Slf4j
public abstract class TypedValidator implements FastValidator<Object> {

    /**
     * Store validators for different types using ConcurrentHashMap.
     */
    private final Map<Class<?>, BiConsumer<Object, ValidationContext>> validators = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, Object> resolvedHandlers = new ConcurrentHashMap<>();
    private final AtomicReference<Set<Class<?>>> registeredTypesCache = new AtomicReference<>();
    private static final Object NO_HANDLER = new Object();

    /**
     * Constructor.
     */
    protected TypedValidator() {
        registerValidators();
    }

    /**
     * Subclasses implement this method to register validation logic.
     */
    protected void registerValidators() {
        // Default empty implementation
    }

    protected int getPriority(Class<?> registeredType) {
        return 0;
    }

    protected boolean failOnAmbiguousHandler() {
        return false;
    }

    /**
     * Register a validation method for a type.
     *
     * @param type      Type class
     * @param validator Validation logic
     * @param <T>       Type
     */
    protected final <T> void register(Class<T> type, BiConsumer<T, ValidationContext> validator) {
        validators.put(type, (obj, ctx) -> validator.accept(type.cast(obj), ctx));
        resolvedHandlers.clear();
        if (validators.size() > 10) {
            registeredTypesCache.set(Set.copyOf(validators.keySet()));
        } else {
            registeredTypesCache.set(null);
        }
    }


    /**
     * Get all registered types.
     *
     * @return Set of registered types
     */
    public Set<Class<?>> getRegisteredTypes() {
        Set<Class<?>> cached = registeredTypesCache.get();
        if (cached != null) return cached;
        Set<Class<?>> computed = Set.copyOf(validators.keySet());
        registeredTypesCache.set(computed);
        return computed;
    }

    public final boolean isRegisteredType(Class<?> type) {
        return validators.containsKey(type);
    }


    public final boolean validateIfRegistered(Object object, ValidationContext context) {
        if (object == null) return false;
        BiConsumer<Object, ValidationContext> handler = resolveHandler(object.getClass());
        if (handler == null) return false;
        handler.accept(object, context);
        return true;
    }

    @Override
    public Class<?> getSupportedType() {
        return validators.size() == 1 ? validators.keySet().iterator().next() : Object.class;
    }

    /**
     * Or directly use size()
     */
    public int size() {
        return validators.size();
    }

    /**
     * Execute object validation.
     *
     * @param object  Target object to be validated
     * @param context Validation context
     */
    @Override
    public final void validate(Object object, ValidationContext context) {
        if (context == null) {
            context = new FastValidator.ValidationContext(true);
        }
        if (object == null) {
            context.reportError(ResponseCode.VALIDATION_ERROR_NULL);
            return;
        }
        BiConsumer<Object, ValidationContext> handler = resolveHandler(object.getClass());
        if (handler != null) {
            handler.accept(object, context);
        } else {
            String s = FailureConst.UNSUPPORTED_VALIDATION_TYPE + object.getClass().getSimpleName();
            context.reportError(ResponseCode.of(400, s));
        }
    }

    @SuppressWarnings("unchecked")
    private BiConsumer<Object, ValidationContext> resolveHandler(Class<?> runtimeType) {
        Object cached = resolvedHandlers.get(runtimeType);
        if (cached != null) {
            return cached == NO_HANDLER ? null : (BiConsumer<Object, ValidationContext>) cached;
        }
        BiConsumer<Object, ValidationContext> resolved = computeBestHandler(runtimeType);
        resolvedHandlers.put(runtimeType, resolved == null ? NO_HANDLER : resolved);
        return resolved;
    }

    private BiConsumer<Object, ValidationContext> computeBestHandler(Class<?> runtimeType) {
        BiConsumer<Object, ValidationContext> exact = validators.get(runtimeType);
        if (exact != null) return exact;

        List<Candidate> bestCandidates = new ArrayList<>();
        int bestDistance = Integer.MAX_VALUE;

        for (Map.Entry<Class<?>, BiConsumer<Object, ValidationContext>> e : validators.entrySet()) {
            Class<?> registeredType = e.getKey();
            if (!registeredType.isAssignableFrom(runtimeType)) continue;
            int currentDistance = distance(runtimeType, registeredType);
            if (currentDistance < bestDistance) {
                bestCandidates.clear();
                bestDistance = currentDistance;
                bestCandidates.add(new Candidate(registeredType, e.getValue(), currentDistance, getPriority(registeredType)));
                continue;
            }
            if (currentDistance == bestDistance) {
                bestCandidates.add(new Candidate(registeredType, e.getValue(), currentDistance, getPriority(registeredType)));
            }
        }

        if (bestCandidates.isEmpty()) {
            return null;
        }
        if (bestCandidates.size() == 1) {
            return bestCandidates.get(0).handler;
        }

        bestCandidates.sort(CANDIDATE_ORDER);
        Candidate chosen = bestCandidates.get(0);

        if (bestCandidates.size() > 1) {
            int chosenPriority = chosen.priority;
            boolean chosenIsInterface = chosen.type.isInterface();
            List<Candidate> ambiguous = new ArrayList<>();
            for (Candidate c : bestCandidates) {
                if (c.distance != bestDistance) continue;
                if (c.priority != chosenPriority) continue;
                if (c.type.isInterface() != chosenIsInterface) continue;
                ambiguous.add(c);
            }
            if (ambiguous.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (Candidate c : ambiguous) {
                    if (!sb.isEmpty()) sb.append(", ");
                    sb.append(c.type.getName())
                            .append("{distance=").append(c.distance)
                            .append(", priority=").append(c.priority)
                            .append(", interface=").append(c.type.isInterface())
                            .append("}");
                }
                if (failOnAmbiguousHandler()) {
                    throw new IllegalStateException("Ambiguous TypedValidator handler for runtimeType=" + runtimeType.getName()
                            + ", candidates=[" + sb + "]. Fix: override getPriority(registeredType) to break ties.");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("[FailFast] Ambiguous TypedValidator handler for runtimeType={}, chosenType={} " +
                                        "candidates=[{}]. Fix: override getPriority(registeredType) to break ties.",
                                runtimeType.getName(), chosen.type.getName(), sb);
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Candidate c : bestCandidates) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(c.type.getName())
                        .append("{distance=").append(c.distance)
                        .append(", priority=").append(c.priority)
                        .append(", interface=").append(c.type.isInterface())
                        .append("}");
            }
            log.debug("[FailFast] TypedValidator resolved handler for runtimeType={} -> chosenType={} candidates=[{}]",
                    runtimeType.getName(), chosen.type.getName(), sb);
        }

        return chosen.handler;
    }

    private record Candidate(Class<?> type, BiConsumer<Object, ValidationContext> handler, int distance, int priority) {
    }

    private static final Comparator<Candidate> CANDIDATE_ORDER = Comparator
            .comparingInt(Candidate::priority).reversed()
            .thenComparingInt(c -> c.type.isInterface() ? 1 : 0)
            .thenComparing(c -> c.type.getName());

    private static int distance(Class<?> from, Class<?> to) {
        if (from == null || to == null) return Integer.MAX_VALUE;
        if (from.equals(to)) return 0;

        ArrayDeque<Class<?>> q = new ArrayDeque<>();
        HashMap<Class<?>, Integer> dist = new HashMap<>();
        q.add(from);
        dist.put(from, 0);

        while (!q.isEmpty()) {
            Class<?> cur = q.poll();
            int d = dist.get(cur);

            Class<?> sup = cur.getSuperclass();
            if (sup != null) {
                if (sup.equals(to)) return d + 1;
                dist.putIfAbsent(sup, d + 1);
                q.add(sup);
            }
            for (Class<?> i : cur.getInterfaces()) {
                if (i.equals(to)) return d + 1;
                dist.putIfAbsent(i, d + 1);
                q.add(i);
            }
        }

        return Integer.MAX_VALUE;
    }
}
