package com.chao.failfast.internal.chain;

import com.chao.failfast.internal.Business;
import com.chao.failfast.internal.MultiBusiness;
import com.chao.failfast.internal.core.ResponseCode;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 终结操作接口
 */
public interface ChainTerminator<S extends ChainCore<S>> {


    /**
     * 这是一个核心方法的声明
     * 该方法名为core，不带任何参数
     * 返回类型为S（可能是某种自定义类型或泛型）
     *
     * @return 返回类型为S的对象，可能是系统的核心组件或数据结构
     */
    S core();


    /**
     * 默认的失败处理方法，当验证失败时抛出相应的业务异常
     * 如果验证失败但没有具体原因，则抛出通用的验证失败异常
     * 如果验证失败且有具体原因，则抛出第一个具体原因对应的异常
     */
    default void fail() {
        if (!core().isValid()) {
            if (core().getCauses().isEmpty()) {
                throw Business.of(ResponseCode.VALIDATION_ERROR_500);
            }
            throw core().getCauses().get(0);
        }
    }

    /**
     * 默认方法，用于处理验证失败的情况
     * 如果验证无效，则根据错误原因的数量抛出不同的异常
     */
    default void failAll() {
        if (!core().isValid()) {
            if (core().getCauses().isEmpty()) {
                throw Business.of(ResponseCode.VALIDATION_ERROR_500);
            }
            if (core().getCauses().size() == 1) throw core().getCauses().get(0);
            throw new MultiBusiness(core().getCauses());
        }
    }


    /**
     * 默认方法，用于在当前核心状态不存活时抛出业务异常
     *
     * @param code 响应状态码，用于标识具体的错误类型
     * @return 返回当前核心对象，如果核心存活
     * @throws Business 当核心不存活时抛出业务异常，异常中包含指定的响应码
     */
    default S failNow(ResponseCode code) {
        if (core().isAlive()) return core();
        throw Business.of(code);
    }

    /**
     * 默认方法：如果核心组件存活则返回核心组件，否则抛出业务异常
     *
     * @param code   响应状态码，用于标识错误类型
     * @param detail 错误详细信息，用于描述具体的错误原因
     * @return 如果核心组件存活，返回核心组件实例
     * @throws Business 当核心组件不存活时，抛出包含指定错误码和详细信息的业务异常
     */
    default S failNow(ResponseCode code, String detail) {
        if (core().isAlive()) return core();
        throw Business.of(code, detail);
    }

    /**
     * 格式化消息版本
     */
    default S failNow(ResponseCode code, String msgFormat, Object... args) {
        if (core().isAlive()) return core();
        throw Business.of(code, String.format(msgFormat, args));
    }


    /**
     * 当核心对象不存活时执行指定操作
     * 这是一个默认方法，属于某个函数式接口，允许在不修改接口的情况下提供默认实现
     *
     * @param action 需要在核心对象不存活时执行的操作
     *               是一个Runnable类型的函数式接口，代表一个无参数、无返回值的操作
     * @return 返回核心对象本身，支持链式调用
     * 返回类型S是泛型，表示核心对象的类型
     */
    default S onFail(Runnable action) {
        if (!core().isAlive()) action.run();
        return core();
    }

    /**
     * 当核心组件不存活时，从提供的Supplier获取值并包装为Optional返回
     * 否则返回空的Optional
     *
     * @param <T>      返回值的类型
     * @param supplier 用于提供值的Supplier接口实现
     * @return 如果核心组件不存活，包含Supplier提供值的Optional；否则返回空的Optional
     */
    default <T> Optional<T> onFailGet(Supplier<T> supplier) {
        return !core().isAlive() ? Optional.ofNullable(supplier.get()) : Optional.empty();
    }


    /**
     * 默认的验证方法实现
     * 该方法不执行任何操作，错误会立即报告到上下文中
     * 这是一个空操作(no-op)方法，作为接口的默认实现
     */
    default void verify() {
        // No-op: Errors are reported to context immediately - 空操作注释，说明错误会立即报告到上下文
    }
}