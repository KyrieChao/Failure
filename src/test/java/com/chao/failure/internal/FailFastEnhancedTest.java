package com.chao.failure.internal;

import com.chao.failure.exception.Business;
import com.chao.failure.exception.MultiBusiness;
import com.chao.failure.Failure;
import com.chao.failure.annotation.FailFastBody;
import com.chao.failure.validator.FastValidator;
import com.chao.failure.annotation.Scene;
import com.chao.failure.annotation.Validate;
import com.chao.failure.constant.Scenario;
import com.chao.failure.internal.chain.pipeline.PathEntry;
import com.chao.failure.internal.core.ResponseCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import com.chao.failure.spi.config.FailFastConfigurer;
import com.chao.failure.spi.validation.ValidatorWhitelistRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FailFast enhanced functionality test.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
@SpringBootTest
@ActiveProfiles("test")
public class FailFastEnhancedTest {

    @Autowired
    private TestService testService;

    @TestConfiguration
    static class Config {
        @Bean
        TestService testService() {
            return new TestServiceImpl();
        }

        @Bean
        FailFastConfigurer failFastConfigurer() {
            return new FailFastConfigurer() {
                @Override
                public void addValidatorWhitelist(ValidatorWhitelistRegistry registry) {
                    registry.add(AgeValidator.class, NoopValidator.class);
                }
            };
        }
    }

    @Test
    void testFailFastBody() {
        UserDTO user = new UserDTO();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        String result = testService.createUser(user);
        assertEquals("User created: Test User", result);
    }

    @Test
    void testValidateWithBridge() {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setName(""); // Empty name
        invalidUser.setEmail("invalid-email"); // Invalid email
        invalidUser.setAge(-5); // Negative age

        assertThrows(Business.class, () -> testService.createUserWithValidation(invalidUser));
    }

    @Test
    void testValidateWithCustomValidator() {
        UserDTO user = new UserDTO();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(15); // Underage

        assertThrows(Business.class, () -> testService.createUserWithCustomValidation(user));
    }

    @Test
    void testSceneOnlyUpdateValidation() {
        UserDTO user = new UserDTO();
        user.setName2(null);
        user.setEmail2("invalid-email");
        user.setAge2(null);

        MultiBusiness exception = assertThrows(MultiBusiness.class, () -> testService.updateUserSceneOnly(user));
        assertNotNull(exception);
        assertEquals(2, exception.getErrors().size());
        assertTrue(exception.getErrors().stream().anyMatch(e -> "email2".equals(e.getPath())));
        assertTrue(exception.getErrors().stream().anyMatch(e -> "age2".equals(e.getPath())));
        assertFalse(exception.getErrors().stream().anyMatch(e -> "name2".equals(e.getPath())));
    }

    @Test
    void testSceneWithCustomValidatorStillRunsBridgeDefault() {
        UserDTO user = new UserDTO();
        user.setName2(null);
        user.setEmail2("invalid-email");
        user.setAge2(null);

        MultiBusiness exception = assertThrows(MultiBusiness.class, () -> testService.updateUserSceneWithCustomValidation(user));
        assertNotNull(exception);
        assertEquals(2, exception.getErrors().size());
        assertTrue(exception.getErrors().stream().anyMatch(e -> "email2".equals(e.getPath())));
        assertTrue(exception.getErrors().stream().anyMatch(e -> "age2".equals(e.getPath())));
        assertFalse(exception.getErrors().stream().anyMatch(e -> "name2".equals(e.getPath())));
    }

    @Test
    void testSceneAndGroupsMixed() {
        UserDTO user = new UserDTO();
        user.setName2(null);
        user.setEmail2("");
        user.setAge2(null);

        Business exception = assertThrows(Business.class, () -> testService.updateUserSceneWithGroups(user));
        assertNotNull(exception);
        assertEquals("email2", exception.getPath());
    }

    @Test
    void testForEachValidation() {
        OrderDTO order = new OrderDTO();
        order.setOrderNo("ORD-001");

        OrderItemDTO item1 = new OrderItemDTO();
        item1.setItemNo("ITEM-001");
        item1.setQuantity(-1); // Invalid quantity

        OrderItemDTO item2 = new OrderItemDTO();
        item2.setItemNo(""); // Empty item number
        item2.setQuantity(5);

        order.setItems(List.of(item1, item2));

        Business exception = assertThrows(Business.class, () -> testService.validateOrder(order));
        assertNotNull(exception.getPath());
        assertTrue(exception.getPath().startsWith("items"));
    }

    @Test
    void testFastMode() {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setName(""); // Empty name
        invalidUser.setEmail("invalid-email"); // Invalid email
        invalidUser.setAge(-5); // Negative age

        Business exception = assertThrows(Business.class, () -> testService.createUserWithFastValidation(invalidUser));
        // Should only get one error in fast mode
        assertNotNull(exception);
    }

    @Test
    void testStrictMode() {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setName(""); // Empty name
        invalidUser.setEmail("invalid-email"); // Invalid email
        invalidUser.setAge(-5); // Negative age

        MultiBusiness exception = assertThrows(MultiBusiness.class, () -> testService.createUserWithStrictValidation(invalidUser));
        // Should get multiple errors in strict mode
        assertNotNull(exception);
        assertFalse(exception.getErrors().isEmpty());
    }

    @Test
    void testConcurrentValidation() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);
        java.util.concurrent.atomic.AtomicReference<Throwable> firstError = new java.util.concurrent.atomic.AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    UserDTO user = new UserDTO();
                    user.setName("Test User");
                    user.setEmail("test@example.com");
                    user.setAge(25);

                    testService.createUserWithValidation(user);
                } catch (Throwable e) {
                    errorCount.incrementAndGet();
                    firstError.compareAndSet(null, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        if (firstError.get() != null) {
            fail(firstError.get());
        }
        assertEquals(0, errorCount.get());
    }

    // Test service interface
    public interface TestService {
        String createUser(@FailFastBody UserDTO user);

        void createUserWithValidation(@FailFastBody UserDTO user);

        void createUserWithCustomValidation(@FailFastBody UserDTO user);

        void updateUserSceneOnly(@FailFastBody UserDTO user);

        void updateUserSceneWithCustomValidation(@FailFastBody UserDTO user);

        void updateUserSceneWithGroups(@FailFastBody UserDTO user);

        void createUserWithFastValidation(@FailFastBody UserDTO user);

        void createUserWithStrictValidation(@FailFastBody UserDTO user);

        void validateOrder(@FailFastBody OrderDTO order);
    }

    // Test service implementation
    public static class TestServiceImpl implements TestService {

        @Override
        public String createUser(UserDTO user) {
            return "User created: " + user.getName();
        }

        @Override
        @Validate(groups = {UserDTO.Create.class})
        public void createUserWithValidation(UserDTO user) {
        }

        @Override
        @Validate(value = {AgeValidator.class}, groups = {UserDTO.Create.class})
        public void createUserWithCustomValidation(UserDTO user) {
        }

        @Override
        @Validate(scene = Scenario.UPDATE, fast = false)
        public void updateUserSceneOnly(UserDTO user) {
        }

        @Override
        @Validate(value = {NoopValidator.class}, scene = Scenario.UPDATE, fast = false)
        public void updateUserSceneWithCustomValidation(UserDTO user) {
        }

        @Override
        @Validate(groups = {UserDTO.Mixed.class}, scene = Scenario.UPDATE)
        public void updateUserSceneWithGroups(UserDTO user) {
        }

        @Override
        @Validate(groups = {UserDTO.Create.class})
        public void createUserWithFastValidation(UserDTO user) {
        }

        @Override
        @Validate(groups = {UserDTO.Create.class}, fast = false)
        public void createUserWithStrictValidation(UserDTO user) {
        }

        @Override
        public void validateOrder(OrderDTO order) {
            Failure.begin()
                    .notBlank(order.getOrderNo(), ResponseCode.VALIDATION_ERROR_400, "Order number is required")
                    .forEach(order.getItems(), "items", scope -> {
                        PathEntry<String> itemNo = scope.field(OrderItemDTO::getItemNo).as("itemNo");
                        PathEntry<Integer> quantity = scope.field(OrderItemDTO::getQuantity).as("quantity");

                        scope.notBlank(itemNo, ResponseCode.VALIDATION_ERROR_400)
                                .positive(quantity, ResponseCode.VALIDATION_ERROR_400)
                                .merge();
                    })
                    .fail();
        }
    }

    public static void main(String[] args) {

    }

    // Test DTO classes
    @Data
    public static class UserDTO {
        public interface Create {
        }

        public interface Mixed {
        }

        @NotBlank(groups = {Create.class})
        private String name;

        @Email(groups = {Create.class})
        @NotBlank(groups = {Create.class})
        private String email;

        @Positive(groups = {Create.class})
        @NotNull(groups = {Create.class})
        private Integer age;


        @Scene(value = {Scenario.CREATE})
        @NotNull
        private String name2;

        @Scene(value = Scenario.UPDATE)
        @Email
        @NotBlank(groups = {Mixed.class})
        private String email2;

        @Scene(value = {Scenario.UPDATE})
        @NotNull
        private Integer age2;

    }

    @Data
    public static class OrderDTO {
        private String orderNo;
        private List<OrderItemDTO> items = new ArrayList<>();
    }

    @Data
    public static class OrderItemDTO {
        private String itemNo;
        private Integer quantity;
    }

    // Custom validator
    public static class AgeValidator implements FastValidator<UserDTO> {
        @Override
        public void validate(UserDTO target, ValidationContext context) {
            if (target.getAge() < 18) {
                context.reportError(ResponseCode.VALIDATION_ERROR_400, "Age must be at least 18");
            }
        }

        @Override
        public Class<?> getSupportedType() {
            return UserDTO.class;
        }
    }

    public static class NoopValidator implements FastValidator<UserDTO> {
        @Override
        public void validate(UserDTO target, ValidationContext context) {
        }

        @Override
        public Class<?> getSupportedType() {
            return UserDTO.class;
        }
    }
}
