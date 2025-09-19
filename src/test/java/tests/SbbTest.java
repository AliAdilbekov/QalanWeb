package tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import pages.KpiTablePage;
import pages.SbbPage;
import testData.TestBase;

@Tag("kpiSbb")
@Execution(ExecutionMode.SAME_THREAD)
//@Disabled("В связи с изменениями логики")
public class SbbTest extends TestBase {

    @Test
    void testSbbSurveyView() {
        new KpiTablePage()
                .loginAsService("7000000000", "12345678", "Sbb Employee");

        sbbPage.clickSbbFunctionality()
                .clickSurveyButton()
                .enterSurveyDateFrom("01.04.2025")
                .enterSurveyDateTo("30.04.2025")
                .selectCompany("qalan.kz")
                .clickViewButton()
                .verifyParentCountMatchesApi(
                        "01.04.2025",
                        "30.04.2025",
                        "qalan.kz",
                        25,                // filter
                        false,             // isEmployeeOnly
                        SbbPage.ParentCountExpectation.UNIQUE_PARENTS
                );
    }
}
