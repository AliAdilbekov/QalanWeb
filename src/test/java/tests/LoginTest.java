package tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import testData.TestBase;

@Tag("auth")
@Execution(ExecutionMode.SAME_THREAD)
public class LoginTest extends TestBase {

    @Test
    @ResourceLock(value = "sharedResource")
    void testSuccessLogin() {
        loginPage.successLogin("7074979695", "12345678", "Test TESTOVNA");
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testFailPhoneLogin() {
        loginPage.failPhoneLogin("7074979698", "12345678", "Телефон нөмірі бойынша пайдаланушы табылмады");

    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testFailPasswordLogin() {
        loginPage.failPasswordLogin("7074979695", "1234567", "Құпия сөз дұрыс емес");
    }
}