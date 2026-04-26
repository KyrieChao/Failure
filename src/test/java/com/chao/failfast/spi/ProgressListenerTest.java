package com.chao.failfast.spi;

import com.chao.failfast.exception.Business;
import com.chao.failfast.internal.core.ResponseCode;
import com.chao.failfast.spi.validation.ProgressListener;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class ProgressListenerTest {

    @Test
    void testDefaultMethods() {
        // 创建一个实现 ValidationProgressListener 接口的匿名类
        ProgressListener listener = new ProgressListener() {
            // 所有方法都使用默认实现
        };
        
        // 测试 onStarted 方法
        listener.onStarted(10);
        
        // 测试 onProgress 方法
        Business error = Business.of(ResponseCode.VALIDATION_ERROR_400, "Test error");
        listener.onProgress(5, 10, error);
        
        // 测试 onCompleted 方法
        List<Business> errors = Collections.singletonList(error);
        listener.onCompleted(10, errors);
        
        // 测试 onCancelled 方法
        listener.onCancelled();
        
        // 所有方法都应该正常执行，没有异常
    }
}
