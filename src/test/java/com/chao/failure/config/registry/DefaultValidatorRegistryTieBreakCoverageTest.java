package com.chao.failure.config.registry;

import com.chao.failure.validator.FastValidator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultValidatorRegistryTieBreakCoverageTest {

    interface AInt {
    }

    interface ZInt {
    }

    interface TieInt {
    }

    static class TieBase {
    }

    static class TieImpl extends TieBase implements TieInt, AInt, ZInt {
    }

    @Test
    void tieBreakPrefersClassOverInterface() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<TieInt> i = (target, ctx) -> {
        };
        FastValidator<TieBase> c = (target, ctx) -> {
        };

        registry.register(TieInt.class, i);
        registry.register(TieBase.class, c);

        assertSame(c, registry.getValidator(TieImpl.class));
    }

    @Test
    void tieBreakPrefersLexicographicallySmallerTypeName() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<AInt> a = (target, ctx) -> {
        };
        FastValidator<ZInt> z = (target, ctx) -> {
        };

        registry.register(ZInt.class, z);
        registry.register(AInt.class, a);

        assertSame(a, registry.getValidator(TieImpl.class));
    }

    @Test
    void exactMatchUpdatesBestDistanceBranch() {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        FastValidator<String> v = (target, ctx) -> {
        };
        registry.register(String.class, v);
        assertSame(v, registry.getValidator(String.class));
    }

    @Test
    void computeBestHandlerUpdatesWhenDistanceImproves() throws Exception {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        resetValidatorsToLinkedHashMap(registry);

        FastValidator<ABase> base = (target, ctx) -> {
        };
        FastValidator<Mid> mid = (target, ctx) -> {
        };

        registry.register(ABase.class, base);
        registry.register(Mid.class, mid);

        assertSame(mid, registry.getValidator(Leaf.class));
    }

    @Test
    void computeBestHandlerPrefersClassOverInterfaceWhenDistanceSame() throws Exception {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        resetValidatorsToLinkedHashMap(registry);

        FastValidator<IBase> i = (target, ctx) -> {
        };
        FastValidator<CBase> c = (target, ctx) -> {
        };

        registry.register(IBase.class, i);
        registry.register(CBase.class, c);

        Field validators = DefaultValidatorRegistry.class.getDeclaredField("validators");
        validators.setAccessible(true);
        var it = ((LinkedHashMap<?, ?>) validators.get(registry)).keySet().iterator();
        assertSame(IBase.class, it.next());
        assertSame(CBase.class, it.next());

        assertSame(c, registry.getValidator(Impl.class));
    }

    @Test
    void computeBestHandlerDoesNotReplaceClassWithInterfaceWhenDistanceSame() throws Exception {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        resetValidatorsToLinkedHashMap(registry);

        FastValidator<CBase> c = (target, ctx) -> {
        };
        FastValidator<IBase> i = (target, ctx) -> {
        };

        registry.register(CBase.class, c);
        registry.register(IBase.class, i);

        assertSame(c, registry.getValidator(Impl.class));
    }

    @Test
    void computeBestHandlerPrefersLexicographicallySmallerTypeNameWhenDistanceSame() throws Exception {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        resetValidatorsToLinkedHashMap(registry);

        FastValidator<ZInt> z = (target, ctx) -> {
        };
        FastValidator<AInt> a = (target, ctx) -> {
        };

        registry.register(ZInt.class, z);
        registry.register(AInt.class, a);

        Field validators = DefaultValidatorRegistry.class.getDeclaredField("validators");
        validators.setAccessible(true);
        var it = ((LinkedHashMap<?, ?>) validators.get(registry)).keySet().iterator();
        assertSame(ZInt.class, it.next());
        assertSame(AInt.class, it.next());

        assertSame(a, registry.getValidator(TieImpl.class));
    }

    @Test
    void computeBestHandlerDoesNotUpdateWhenTypeNameNotSmaller() throws Exception {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        resetValidatorsToLinkedHashMap(registry);

        FastValidator<AInt> a = (target, ctx) -> {
        };
        FastValidator<ZInt> z = (target, ctx) -> {
        };

        registry.register(AInt.class, a);
        registry.register(ZInt.class, z);

        assertSame(a, registry.getValidator(TieImpl.class));
    }

    @Test
    void computeBestHandlerCoversEqualAndNonEqualDistanceBranches() throws Exception {
        DefaultValidatorRegistry registry = new DefaultValidatorRegistry();
        resetValidatorsToLinkedHashMap(registry);

        FastValidator<IBase> i = (target, ctx) -> {
        };
        FastValidator<CBase> c = (target, ctx) -> {
        };
        FastValidator<Object> o = (target, ctx) -> {
        };

        registry.register(IBase.class, i);
        registry.register(Object.class, o);
        registry.register(CBase.class, c);

        assertSame(c, registry.getValidator(Impl.class));
    }

    @Test
    void distanceReturnsMaxValueWhenUnreachable() throws Exception {
        Method m = DefaultValidatorRegistry.class.getDeclaredMethod("distance", Class.class, Class.class);
        m.setAccessible(true);
        int d = (int) m.invoke(null, String.class, Integer.class);
        assertThat(d).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void distanceHandlesNullAndEquals() throws Exception {
        Method m = DefaultValidatorRegistry.class.getDeclaredMethod("distance", Class.class, Class.class);
        m.setAccessible(true);
        assertThat((int) m.invoke(null, null, String.class)).isEqualTo(Integer.MAX_VALUE);
        assertThat((int) m.invoke(null, String.class, null)).isEqualTo(Integer.MAX_VALUE);
        assertThat((int) m.invoke(null, String.class, String.class)).isEqualTo(0);
    }

    private static void resetValidatorsToLinkedHashMap(DefaultValidatorRegistry registry) throws Exception {
        Field validators = DefaultValidatorRegistry.class.getDeclaredField("validators");
        validators.setAccessible(true);
        validators.set(registry, new LinkedHashMap<>());
        assertThat(validators.get(registry)).isInstanceOf(LinkedHashMap.class);

        Field resolvedHandlers = DefaultValidatorRegistry.class.getDeclaredField("resolvedHandlers");
        resolvedHandlers.setAccessible(true);
        ((java.util.Map<?, ?>) resolvedHandlers.get(registry)).clear();
    }

    static class ABase {
    }

    static class Mid extends ABase {
    }

    static class Leaf extends Mid {
    }

    interface IBase {
    }

    static class CBase {
    }

    static class Impl extends CBase implements IBase {
    }
}

