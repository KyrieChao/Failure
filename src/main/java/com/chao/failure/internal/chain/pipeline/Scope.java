package com.chao.failure.internal.chain.pipeline;

import com.chao.failure.annotation.ToImprove;
import com.chao.failure.internal.check.BooleanChecks;
import com.chao.failure.internal.check.NumberChecks;
import com.chao.failure.internal.check.ObjectChecks;
import com.chao.failure.internal.check.StringChecks;
import com.chao.failure.internal.core.ResponseCode;

import java.util.Collection;
import java.util.Map;
import java.util.function.*;

/**
 * Scope for forEach validation - provides reference construction and assertion proxy.
 *
 * @param <T> Element type
 * @author Kyrie Chao
 * @version 1.3.1
 */
@ToImprove(value = "Scope might become bloated", version = "1.3.0", tag = "1.8.0")
public class Scope<T> {

    private final ChainCore<?> chain;
    private final T item;
    private final String path;
    private boolean ended = false;
    private final int baseErrorSize;
    private boolean stopItemOnFail = false;

    /**
     * Constructor.
     *
     * @param chain Chain core instance
     * @param item  Current element
     * @param path  Current path
     */
    public Scope(ChainCore<?> chain, T item, String path) {
        this.chain = chain;
        this.item = item;
        this.path = path;
        this.baseErrorSize = chain.errorSize();
    }

    /**
     * Get current element as PathEntry.
     *
     * @return PathEntry for current element
     */
    public PathEntry<T> it() {
        return new PathEntry<>(item, path);
    }

    /**
     * FieldRef class for field validation with optional alias.
     *
     * @param <R> Field type
     */
    public static class FieldRef<R> {
        private final R value;
        private final String path;

        public FieldRef(R value, String path) {
            this.value = value;
            this.path = path;
        }

        /**
         * Set custom field name.
         *
         * @param alias Custom field name
         * @return PathEntry with custom field name
         */
        public PathEntry<R> as(String alias) {
            int dot = path.lastIndexOf(".");
            String newPath = dot >= 0 ? path.substring(0, dot + 1) + alias : alias;
            return new PathEntry<>(value, newPath);
        }

        public PathEntry<R> ref() {
            return new PathEntry<>(value, path);
        }

        /**
         * Get value.
         *
         * @return Field value
         */
        public R value() {
            return value;
        }
    }

    /**
     * Get field as PathEntry with specified getter.
     *
     * @param getter Field getter function
     * @param <R>    Field type
     * @return FieldRef for field
     */
    public <R> FieldRef<R> field(Function<T, R> getter) {
        String fieldName = getFieldNameFromGetter();
        return field(fieldName, getter);
    }

    public <R> PathEntry<R> fieldEntry(Function<T, R> getter) {
        String fieldName = getFieldNameFromGetter();
        return field(fieldName, getter).ref();
    }

    public <R> FieldRef<R> field(String fieldName, Function<T, R> getter) {
        R value = getter.apply(item);
        String fieldPath = joinPath(path, fieldName);
        return new FieldRef<>(value, fieldPath);
    }


    // ===== Assertion proxy methods =====

    /**
     * Check if current item is not null.
     *
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> notNull(ResponseCode code) {
        if (ended) return this;
        chain.checkRef(ObjectChecks.notNull(item), code, it());
        endOnFail();
        return this;
    }

    /**
     * Check if string is not blank.
     *
     * @param ref  PathEntry for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> notBlank(PathEntry<String> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.notBlank(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if number is positive.
     *
     * @param ref  PathEntry for number
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> positive(PathEntry<? extends Number> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(NumberChecks.positive(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if email is valid.
     *
     * @param ref  PathEntry for email
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> email(PathEntry<String> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.email(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if mobile is valid.
     *
     * @param ref  PathEntry for mobile
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> mobile(PathEntry<String> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.mobile(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    public Scope<T> isTrue(PathEntry<Boolean> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(BooleanChecks.isTrue(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    public Scope<T> isFalse(PathEntry<Boolean> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(BooleanChecks.isFalse(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string is not blank (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> notBlank(FieldRef<String> ref, ResponseCode code) {
        return notBlank(ref.ref(), code);
    }

    /**
     * Check if number is positive (overload for FieldRef).
     *
     * @param ref  FieldRef for number
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> positive(FieldRef<? extends Number> ref, ResponseCode code) {
        return positive(ref.ref(), code);
    }

    /**
     * Check if email is valid (overload for FieldRef).
     *
     * @param ref  FieldRef for email
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> email(FieldRef<String> ref, ResponseCode code) {
        return email(ref.ref(), code);
    }

    /**
     * Check if mobile is valid (overload for FieldRef).
     *
     * @param ref  FieldRef for mobile
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> mobile(FieldRef<String> ref, ResponseCode code) {
        return mobile(ref.ref(), code);
    }

    // ===== Additional assertion methods =====

    /**
     * Check if collection is not empty.
     *
     * @param ref  PathEntry for collection
     * @param code Response code
     * @return Current scope
     */
    public <C extends Collection<?>> Scope<T> notEmptyCollection(PathEntry<C> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(ObjectChecks.notEmpty(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if collection is not empty (overload for FieldRef).
     *
     * @param ref  FieldRef for collection
     * @param code Response code
     * @return Current scope
     */
    public <C extends Collection<?>> Scope<T> notEmptyCollection(FieldRef<C> ref, ResponseCode code) {
        return notEmptyCollection(ref.ref(), code);
    }

    /**
     * Check if map is not empty.
     *
     * @param ref  PathEntry for map
     * @param code Response code
     * @return Current scope
     */
    public <K, V> Scope<T> notEmptyMap(PathEntry<Map<K, V>> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(ObjectChecks.notEmpty(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if map is not empty (overload for FieldRef).
     *
     * @param ref  FieldRef for map
     * @param code Response code
     * @return Current scope
     */
    public <K, V> Scope<T> notEmptyMap(FieldRef<Map<K, V>> ref, ResponseCode code) {
        return notEmptyMap(ref.ref(), code);
    }

    /**
     * Check if string length is between min and max (inclusive).
     *
     * @param ref  PathEntry for string
     * @param min  Minimum length
     * @param max  Maximum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> length(PathEntry<String> ref, int min, int max, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.lengthBetween(ref.value(), min, max), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string length is between min and max (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param min  Minimum length
     * @param max  Maximum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> length(FieldRef<String> ref, int min, int max, ResponseCode code) {
        return length(ref.ref(), min, max, code);
    }

    /**
     * Check if number is between min and max (inclusive).
     *
     * @param ref  PathEntry for number
     * @param min  Minimum value
     * @param max  Maximum value
     * @param code Response code
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> between(PathEntry<N> ref, N min, N max, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(NumberChecks.inRange(ref.value(), min, max), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if number is between min and max (overload for FieldRef).
     *
     * @param ref  FieldRef for number
     * @param min  Minimum value
     * @param max  Maximum value
     * @param code Response code
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> between(FieldRef<N> ref, N min, N max, ResponseCode code) {
        return between(ref.ref(), min, max, code);
    }

    /**
     * Check if string matches regular expression.
     *
     * @param ref   PathEntry for string
     * @param regex Regular expression
     * @param code  Response code
     * @return Current scope
     */
    public Scope<T> matches(PathEntry<String> ref, String regex, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.match(ref.value(), regex), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string matches regular expression (overload for FieldRef).
     *
     * @param ref   FieldRef for string
     * @param regex Regular expression
     * @param code  Response code
     * @return Current scope
     */
    public Scope<T> matches(FieldRef<String> ref, String regex, ResponseCode code) {
        return matches(ref.ref(), regex, code);
    }

    /**
     * Check with custom predicate.
     *
     * @param ref     PathEntry for value
     * @param predicate Predicate to check
     * @param code    Response code
     * @param detail  Detailed description
     * @param <R>     Value type
     * @return Current scope
     */
    public <R> Scope<T> check(PathEntry<R> ref, Predicate<R> predicate, ResponseCode code, String detail) {
        if (ended) return this;
        boolean condition = ref.value() != null && predicate.test(ref.value());
        chain.checkRef(condition, ResponseCode.of(code.getCode(), code.getMessage(), detail), ref);
        endOnFail();
        return this;
    }

    /**
     * Check with custom predicate (overload for FieldRef).
     *
     * @param ref     FieldRef for value
     * @param predicate Predicate to check
     * @param code    Response code
     * @param detail  Detailed description
     * @param <R>     Value type
     * @return Current scope
     */
    public <R> Scope<T> check(FieldRef<R> ref, Predicate<R> predicate, ResponseCode code, String detail) {
        return check(ref.ref(), predicate, code, detail);
    }

    /**
     * Check with custom supplier.
     *
     * @param ref     PathEntry for value
     * @param ok      Supplier for condition
     * @param code    Response code
     * @param detail  Detailed description
     * @param <R>     Value type
     * @return Current scope
     */
    public <R> Scope<T> check(PathEntry<R> ref, Supplier<Boolean> ok, ResponseCode code, String detail) {
        if (ended) return this;
        boolean condition = ok.get();
        chain.checkRef(condition, ResponseCode.of(code.getCode(), code.getMessage(), detail), ref);
        endOnFail();
        return this;
    }

    /**
     * Check with custom supplier (overload for FieldRef).
     *
     * @param ref     FieldRef for value
     * @param ok      Supplier for condition
     * @param code    Response code
     * @param detail  Detailed description
     * @param <R>     Value type
     * @return Current scope
     */
    public <R> Scope<T> check(FieldRef<R> ref, Supplier<Boolean> ok, ResponseCode code, String detail) {
        return check(ref.ref(), ok, code, detail);
    }

    // ===== Numeric comparison methods =====

    /**
     * Check if number is greater than threshold.
     *
     * @param ref       PathEntry for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> greaterThan(PathEntry<N> ref, N threshold, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(NumberChecks.greaterThan(ref.value(), threshold), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if number is greater than threshold (overload for FieldRef).
     *
     * @param ref       FieldRef for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> greaterThan(FieldRef<N> ref, N threshold, ResponseCode code) {
        return greaterThan(ref.ref(), threshold, code);
    }

    /**
     * Check if number is greater than or equal to threshold.
     *
     * @param ref       PathEntry for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> greaterOrEqual(PathEntry<N> ref, N threshold, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(NumberChecks.greaterOrEqual(ref.value(), threshold), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if number is greater than or equal to threshold (overload for FieldRef).
     *
     * @param ref       FieldRef for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> greaterOrEqual(FieldRef<N> ref, N threshold, ResponseCode code) {
        return greaterOrEqual(ref.ref(), threshold, code);
    }

    /**
     * Check if number is less than threshold.
     *
     * @param ref       PathEntry for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> lessThan(PathEntry<N> ref, N threshold, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(NumberChecks.lessThan(ref.value(), threshold), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if number is less than threshold (overload for FieldRef).
     *
     * @param ref       FieldRef for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> lessThan(FieldRef<N> ref, N threshold, ResponseCode code) {
        return lessThan(ref.ref(), threshold, code);
    }

    /**
     * Check if number is less than or equal to threshold.
     *
     * @param ref       PathEntry for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> lessOrEqual(PathEntry<N> ref, N threshold, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(NumberChecks.lessOrEqual(ref.value(), threshold), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if number is less than or equal to threshold (overload for FieldRef).
     *
     * @param ref       FieldRef for number
     * @param threshold Threshold value
     * @param code      Response code
     * @param <N>       Number type
     * @return Current scope
     */
    public <N extends Number & Comparable<N>> Scope<T> lessOrEqual(FieldRef<N> ref, N threshold, ResponseCode code) {
        return lessOrEqual(ref.ref(), threshold, code);
    }

    // ===== String comparison methods =====

    /**
     * Check if string length is between min and max (inclusive).
     *
     * @param ref  PathEntry for string
     * @param min  Minimum length
     * @param max  Maximum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> lengthBetween(PathEntry<String> ref, int min, int max, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.lengthBetween(ref.value(), min, max), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string length is between min and max (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param min  Minimum length
     * @param max  Maximum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> lengthBetween(FieldRef<String> ref, int min, int max, ResponseCode code) {
        return lengthBetween(ref.ref(), min, max, code);
    }

    /**
     * Check if string length is at least min.
     *
     * @param ref  PathEntry for string
     * @param min  Minimum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> lengthMin(PathEntry<String> ref, int min, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.lengthMin(ref.value(), min), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string length is at least min (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param min  Minimum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> lengthMin(FieldRef<String> ref, int min, ResponseCode code) {
        return lengthMin(ref.ref(), min, code);
    }

    /**
     * Check if string length is at most max.
     *
     * @param ref  PathEntry for string
     * @param max  Maximum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> lengthMax(PathEntry<String> ref, int max, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.lengthMax(ref.value(), max), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string length is at most max (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param max  Maximum length
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> lengthMax(FieldRef<String> ref, int max, ResponseCode code) {
        return lengthMax(ref.ref(), max, code);
    }

    /**
     * Check if string is a valid credit card number.
     *
     * @param ref  PathEntry for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> isCreditCard(PathEntry<String> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.isCreditCard(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string is a valid credit card number (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> isCreditCard(FieldRef<String> ref, ResponseCode code) {
        return isCreditCard(ref.ref(), code);
    }

    /**
     * Check if string is a valid URL.
     *
     * @param ref  PathEntry for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> url(PathEntry<String> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.url(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string is a valid URL (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> url(FieldRef<String> ref, ResponseCode code) {
        return url(ref.ref(), code);
    }

    /**
     * Check if string is a valid IP address.
     *
     * @param ref  PathEntry for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> ipAddress(PathEntry<String> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.ipAddress(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string is a valid IP address (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> ipAddress(FieldRef<String> ref, ResponseCode code) {
        return ipAddress(ref.ref(), code);
    }

    /**
     * Check if string is a valid UUID.
     *
     * @param ref  PathEntry for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> uuid(PathEntry<String> ref, ResponseCode code) {
        if (ended) return this;
        chain.checkRef(StringChecks.uuid(ref.value()), code, ref);
        endOnFail();
        return this;
    }

    /**
     * Check if string is a valid UUID (overload for FieldRef).
     *
     * @param ref  FieldRef for string
     * @param code Response code
     * @return Current scope
     */
    public Scope<T> uuid(FieldRef<String> ref, ResponseCode code) {
        return uuid(ref.ref(), code);
    }

    // ===== Conditional validation methods =====

    /**
     * Validate only when condition is true.
     *
     * @param condition Condition to check
     * @param action    Validation action
     * @return Current scope
     */
    public Scope<T> when(boolean condition, Runnable action) {
        if (ended) return this;
        if (condition) {
            action.run();
        }
        return this;
    }

    /**
     * Validate only when predicate is true for current item.
     *
     * @param predicate Predicate to check
     * @param action    Validation action
     * @return Current scope
     */
    public Scope<T> when(Predicate<T> predicate, Runnable action) {
        if (ended) return this;
        if (predicate.test(item)) {
            action.run();
        }
        return this;
    }

    /**
     * Validate only when condition is false.
     *
     * @param condition Condition to check
     * @param action    Validation action
     * @return Current scope
     */
    public Scope<T> unless(boolean condition, Runnable action) {
        if (ended) return this;
        if (!condition) {
            action.run();
        }
        return this;
    }

    /**
     * Validate only when predicate is false for current item.
     *
     * @param predicate Predicate to check
     * @param action    Validation action
     * @return Current scope
     */
    public Scope<T> unless(Predicate<T> predicate, Runnable action) {
        if (ended) return this;
        if (!predicate.test(item)) {
            action.run();
        }
        return this;
    }

    // ===== Nested validation methods =====

    /**
     * Validate nested object.
     *
     * @param getter Function to get nested object
     * @param action Validation action for nested object
     * @param <N>    Nested object type
     * @return Current scope
     */
    public <N> Scope<T> nested(Function<T, N> getter, Consumer<Scope<N>> action) {
        if (ended) return this;
        N nestedItem = getter.apply(item);
        if (nestedItem != null) {
            String nestedPath = joinPath(path, getFieldNameFromGetter());
            Scope<N> nestedScope = new Scope<>(chain, nestedItem, nestedPath);
            action.accept(nestedScope);
        }
        return this;
    }

    public <N> Scope<T> nested(String fieldName, Function<T, N> getter, Consumer<Scope<N>> action) {
        if (ended) return this;
        N nestedItem = getter.apply(item);
        if (nestedItem != null) {
            String nestedPath = joinPath(path, fieldName);
            Scope<N> nestedScope = new Scope<>(chain, nestedItem, nestedPath);
            action.accept(nestedScope);
        }
        return this;
    }

    /**
     * Validate collection items.
     *
     * @param getter Function to get collection
     * @param action Validation action for each item
     * @param <C>    Collection type
     * @param <E>    Element type
     * @return Current scope
     */
    public <C extends Collection<E>, E> Scope<T> forEach(Function<T, C> getter, Consumer<Scope<E>> action) {
        if (ended) return this;
        C collection = getter.apply(item);
        if (collection != null) {
            String collectionPath = joinPath(path, getFieldNameFromGetter());
            int index = 0;
            for (E element : collection) {
                String elementPath = collectionPath + "[" + index + "]";
                Scope<E> elementScope = new Scope<>(chain, element, elementPath);
                action.accept(elementScope);
                index++;
            }
        }
        return this;
    }

    public <C extends Collection<E>, E> Scope<T> forEach(String fieldName, Function<T, C> getter, Consumer<Scope<E>> action) {
        if (ended) return this;
        C collection = getter.apply(item);
        if (collection != null) {
            String collectionPath = joinPath(path, fieldName);
            int index = 0;
            for (E element : collection) {
                String elementPath = collectionPath + "[" + index + "]";
                Scope<E> elementScope = new Scope<>(chain, element, elementPath);
                action.accept(elementScope);
                index++;
            }
        }
        return this;
    }

    /**
     * Validate map entries.
     *
     * @param getter Function to get map
     * @param action Validation action for each entry
     * @param <K>    Key type
     * @param <V>    Value type
     * @return Current scope
     */
    public <K, V> Scope<T> forEachEntry(Function<T, Map<K, V>> getter, BiConsumer<K, Scope<V>> action) {
        if (ended) return this;
        Map<K, V> map = getter.apply(item);
        if (map != null) {
            String mapPath = joinPath(path, getFieldNameFromGetter());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                String entryPath = mapPath + "[" + key + "]";
                Scope<V> valueScope = new Scope<>(chain, value, entryPath);
                action.accept(key, valueScope);
            }
        }
        return this;
    }

    public <K, V> Scope<T> forEachEntry(String fieldName, Function<T, Map<K, V>> getter, BiConsumer<K, Scope<V>> action) {
        if (ended) return this;
        Map<K, V> map = getter.apply(item);
        if (map != null) {
            String mapPath = joinPath(path, fieldName);
            for (Map.Entry<K, V> entry : map.entrySet()) {
                K key = entry.getKey();
                V value = entry.getValue();
                String entryPath = mapPath + "[" + key + "]";
                Scope<V> valueScope = new Scope<>(chain, value, entryPath);
                action.accept(key, valueScope);
            }
        }
        return this;
    }

    // ===== Helper methods =====

    /**
     * Get field name from getter function.
     * <p>
     * Note: Lambda string parsing is unstable across different JDKs and compilers.
     * It is recommended to use the overloaded methods that accept an explicit fieldName.
     *
     * @param <R>    Return type
     * @return Field name (defaults to "field")
     */
    private <R> String getFieldNameFromGetter() {
        return "field";
    }

    private static String joinPath(String parent, String child) {
        if (parent == null || parent.isBlank()) {
            return child;
        }
        return parent + "." + child;
    }

    /**
     * End validation for current item.
     *
     */
    public void merge() {
    }

    /**
     * Stop validation for current item on first failure.
     *
     * @return Current scope
     */
    public Scope<T> stopItemOnFail() {
        stopItemOnFail = true;
        endOnFail();
        return this;
    }

    private void endOnFail() {
        if (stopItemOnFail && chain.errorSize() > baseErrorSize) {
            ended = true;
        }
    }

}