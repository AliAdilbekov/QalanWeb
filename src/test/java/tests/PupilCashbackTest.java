package tests;

import org.junit.jupiter.api.Test;
import testData.TestBase;

public class PupilCashbackTest extends TestBase {

    @Test
    void testCashbackFlow() {
        pupilCashbackPage.loginAsStudent("7058281556", "12345678", "Testcashback TESTCASHBACK")
                .clickCashbackChapterButton()
                .clickGetCashbackButton()
                .clickKassa24Button() 
                .enterConfirmationCodeFromApi("77022659587")
                .verifyToastError()
                .enterCardAndPay() 
                .verifySuccessScreen();
    }
    @Test
    void testShortCodeInput() {
        pupilCashbackPage.loginAsStudent("7058281556", "12345678", "Testcashback TESTCASHBACK")
                .clickCashbackChapterButton()
                .clickGetCashbackButton()
                .clickKassa24Button()
                .enterCodeManually("123")                 // ← 3 цифры
                .expectToastErrorMustAppear();            // PASSED если тост есть
    }

    @Test
    void testInvalidFullCodeInput() {
        pupilCashbackPage.loginAsStudent("7058281556", "12345678", "Testcashback TESTCASHBACK")
                .clickCashbackChapterButton()
                .clickGetCashbackButton()
                .clickKassa24Button()
                .enterCodeManually("9999")                // ← 4 неправильные цифры
                .expectToastErrorMustAppear();            // PASSED если тост есть
    }


}
