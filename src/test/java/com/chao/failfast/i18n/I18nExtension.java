package com.chao.failfast.i18n;

import com.chao.failfast.util.I18n;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public class I18nExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Debug: ensure extension is running
        // throw new RuntimeException("I18nExtension is running for " + context.getDisplayName());
        
        // Use a simple MessageSource implementation to avoid any loading/locale issues
        MessageSource messageSource = new MessageSource() {
            @Override
            public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
                System.err.println("DEBUG: getMessage code=" + code);
                if ("failure.const.code.required".equals(code)) return "code 不能为空";
                if ("failure.const.message.description.required".equals(code)) return "message 或 description 至少一个不能为 null";
                if ("failure.const.too.many.errors".equals(code)) return "校验失败，错误过多";
                if ("response.code.validation.failed.dynamic".equals(code)) return "链式验证未通过，请使用 ViolationSpec 配置具体错误信息";
                if ("failure.const.cannot.reduce.empty.list".equals(code)) return "无法缩减空列表";
                if ("response.code.validation.error".equals(code)) return "参数校验失败";
                if ("failure.const.unsupported.validation.type".equals(code)) return "不支持的校验类型: ";
                if ("failure.const.validation.error.prefix".equals(code)) return "校验失败,共";
                if ("failure.const.error.item.suffix".equals(code)) return " 项问题";
                if ("failure.const.multiple.validation.errors.count".equals(code)) {
                    Object n = (args != null && args.length > 0) ? args[0] : null;
                    return "共" + (n == null ? "" : n) + "项问题";
                }
                if ("failure.const.invalid.parameter".equals(code)) return "参数无效";
                
                return defaultMessage != null ? defaultMessage : code;
            }

            @Override
            public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
                return getMessage(code, args, code, locale);
            }

            @Override
            public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
                return resolvable.getDefaultMessage();
            }
        };
        
        // Initialize I18n with the message source
        I18n i18n = new I18n(messageSource);
        i18n.init();
        System.err.println("DEBUG: I18nExtension beforeEach instance set");
        
        // Set default locale to Chinese for unit tests
        LocaleContextHolder.setLocale(Locale.CHINA);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        // Clear static instance via reflection
        // Field instanceField = I18n.class.getDeclaredField("instance");
        // instanceField.setAccessible(true);
        // instanceField.set(null, null);
        LocaleContextHolder.resetLocaleContext();
    }
}
