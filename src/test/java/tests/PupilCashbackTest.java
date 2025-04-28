package tests;

import org.junit.jupiter.api.Test;
import testData.TestBase;

public class PupilCashbackTest extends TestBase {

    @Test
    void testCashbackFlow() {
        pupilCashbackPage.loginAsStudent("7058281556", "12345678", "Testcashback TESTCASHBACK")
                .clickCashbackChapterButton()
                .clickGetCashbackButton()
                .clickKassa24Button() // подождёт появления поля
                .enterConfirmationCodeFromApi("77022659587") // код подставит и нажмёт
                .verifyToastError()
                .enterCardAndPay() // карта + выплата
                .verifySuccessScreen();
    }
}