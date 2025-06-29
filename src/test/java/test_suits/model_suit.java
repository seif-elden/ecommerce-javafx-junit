package test_suits;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectClasses;

/**
 * Runs only the test classes in the `models` package—
 * i.e., those ending with 'Test'.
 */
@Suite

// @SelectPackages("models")
@SelectClasses({
models.OrderItemTest.class,
models.ProductTest.class,
models.OrderTest.class,
models.CartItemTest.class,
models.CategoryTest.class,
models.UserTest.class
})

public class model_suit {
    // No additional code needed.
}

