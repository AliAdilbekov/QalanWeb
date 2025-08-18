package tests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import testData.TestBase;

@Tag("kpiService")
@Execution(ExecutionMode.SAME_THREAD)
@Disabled("В связи с изменениями логики")
public class KpiTest extends TestBase {

    @Test
    void testPersonalStudyKpi() {
        kpiTablePage.loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickPersonalStudyKpi()
                .typeCompanyNameAndSelect("qalan.kz")
                .enterDate("01.04.2024")
                .verifyKpiMatchesApi("01.04.2024", "qalan.kz");
    }


    @Test
    void testNewPupilsChapter() {
        kpiTablePage.loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickNewPupils()
                .selectKazakhLanguage()
                .enterDateForNewPupils("01.04.2024")
                .clickOneThirtyDays()
                .verifyNewPupilsData();
    }

    @Test
    void testUntPupilsKpi() {
        kpiTablePage.loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickUntPupilsKpi()
                .selectKazLanguage()
                .enterDate("01.04.2024", "ЕНТ")
                .clickZhukteuUntButton()
                .verifyUntPupilsData();
    }

    @Test
    void testConsecutiveUnexecuted10to15Days() {
        kpiTablePage.loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickNotCompletedInRow()
                .selectKazakhLanguage()
                .enterDate("14.04.2024", "Подряд не выпол.10-15")
                .clickTenFifteenDays()
                .verifyConsecutiveUnexecutedData(
                        1,      // expectedExecuted
                        20,     // expectedUnexecuted
                        "5%",   // expectedPercent
                        21      // expectedTotal
                );
    }

    @Test
    void testConsecutiveUnexecuted15PlusDays() {
        kpiTablePage.loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickNotCompletedInRow()
                .selectKazakhLanguage()
                .enterDate("17.04.2024", "Подряд не выпол.15+")
                .clickTenFifteenPlusDays()
                .verifyConsecutiveUnexecutedData15Plus(
                        0,      // expectedExecuted
                        15,     // expectedUnexecuted
                        "0%",   // expectedPercent
                        15      // expectedTotal
                );


    }


}
