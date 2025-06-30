package tests;

import org.junit.jupiter.api.Test;
import pages.KpiTablePage;
import testData.TestBase;

public class SbbTest extends TestBase {

    @Test
    void testSbbSurveyView() {
        new KpiTablePage()
                .loginAsService("7000000000", "12345678", "test sbb");


        sbbPage.clickSbbFunctionality()
                .clickSurveyButton()
                .enterSurveyDate("01.04.2024")
                .selectCompany("qalan.kz")
                .clickViewButton()
                .verifyParentCount(4);
    }
}
