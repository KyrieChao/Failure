package com.chao.failfast.internal.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecursiveOptions 测试")
class RecursiveOptionTest {

    @Test
    @DisplayName("默认构建应使用默认值")
    void defaultBuilderShouldUseDefaultValues() {
        RecursiveOption options = RecursiveOption.builder().build();
        
        assertThat(options.getMaxDepth()).isEqualTo(4);
        assertThat(options.getMaxItems()).isEqualTo(1000);
        assertThat(options.getMaxErrors()).isEqualTo(100);
        assertThat(options.getInclude()).isNull();
        assertThat(options.getExclude()).isNull();
    }

    @Test
    @DisplayName("构建器应正确设置自定义值")
    void builderShouldSetCustomValues() {
        List<String> include = new ArrayList<>();
        include.add("field1");
        List<String> exclude = new ArrayList<>();
        exclude.add("field2");
        
        RecursiveOption options = RecursiveOption.builder()
                .maxDepth(10)
                .maxItems(500)
                .maxErrors(50)
                .include(include)
                .exclude(exclude)
                .build();
        
        assertThat(options.getMaxDepth()).isEqualTo(10);
        assertThat(options.getMaxItems()).isEqualTo(500);
        assertThat(options.getMaxErrors()).isEqualTo(50);
        assertThat(options.getInclude()).isSameAs(include);
        assertThat(options.getExclude()).isSameAs(exclude);
    }

    @Test
    @DisplayName("部分设置应保持其他默认值")
    void partialBuilderShouldKeepOtherDefaultValues() {
        RecursiveOption options = RecursiveOption.builder()
                .maxDepth(5)
                .build();
        
        assertThat(options.getMaxDepth()).isEqualTo(5);
        assertThat(options.getMaxItems()).isEqualTo(1000); // 保持默认值
        assertThat(options.getMaxErrors()).isEqualTo(100); // 保持默认值
    }
}
