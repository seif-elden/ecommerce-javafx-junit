package test_suits;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Groups all DAO-layer tests.
 */
@Suite
@SelectClasses({
    DAO.WhiteBoxCartDAOTest.class,
    DAO.WhiteBoxProductDAOTest.class,
    DAO.WhiteBoxCategoryDAOTest.class,
    DAO.WhiteBoxOrderDAOTest.class,
    DAO.CartDAOTest.class,
    DAO.CategoryDAOTest.class,
    DAO.OrderDAOTest.class,
    DAO.ProductDAOTest.class,
    DAO.UserDAOTest.class,

})
public class dao_suit {
    // The suite will include every test class in the DAO package.
}
