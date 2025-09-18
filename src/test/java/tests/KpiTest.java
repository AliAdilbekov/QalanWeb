package tests;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import pages.KpiTablePage;
import testData.TestBase;

@Tag("kpiService")
@Execution(ExecutionMode.SAME_THREAD)
//@Disabled("В связи с изменениями логики")
public class KpiTest extends TestBase {

    @Test
    void testPersonalStudyKpi() {
        kpiTablePage.loginAsService("7000000000", "12345678", "Sbb Employee")
                .clickServiceChapter()
                .clickKpiButton()
                .clickPersonalStudyKpi()
                .typeCompanyNameAndSelect("qalan.kz")
                .enterDate("01.04.2024")
                .verifyKpiMatchesApi("01.04.2024", "qalan.kz");
    }


    @Test
    void testNewPupilsChapter() {
        kpiTablePage.loginAsService("7000000000", "12345678", "Sbb Employee")
                .clickServiceChapter()
                .clickKpiButton()
                .clickNewPupils()
                .selectKazakhLanguage()
                .enterDateForNewPupils("01.04.2024")
                .clickOneThirtyDays()
                .verifyNewPupilsMatchesApi(
                "01.04.2024",
                "kaz",
                0, 10,
                "Kazakhstan",
                "only_new_pupils"
        );
    }


    // Unt Kpi - перешел в сервис ЕНТ, пока дизеиблим
//    @Test
//    void testUntPupilsKpi() {
//        kpiTablePage.loginAsService("7000000000", "12345678", "Sbb Employee")
//                .clickServiceChapter()
//                .clickKpiButton()
//                .clickUntPupilsKpi()
//                .selectKazLanguage()
//                .enterDate("01.04.2024", "ЕНТ")
//                .clickZhukteuUntButton()
//                .verifyUntPupilsData();
//    }

    @Test
    void testConsecutiveUnexecuted10to15Days() {
        kpiTablePage.loginAsService("7000000000", "12345678", "Sbb Employee")
                .clickServiceChapter()
                .clickKpiButton()
                .clickNotCompletedInRow()
                .selectKazakhLanguage()
                .enterDate("14.04.2024", "Подряд не выпол.10-15")
                .clickTenFifteenDays()
                .verifyConsecutiveUnexecutedMatchesApi("14.04.2024", KpiTablePage.ConsecutiveBand.TEN_TO_FIFTEEN, "kaz", "math_course");

    }

    @Test
    void testConsecutiveUnexecuted15PlusDays() {
        kpiTablePage.loginAsService("7000000000", "12345678", "Sbb Employee")
                .clickServiceChapter()
                .clickKpiButton()
                .clickNotCompletedInRow()
                .selectKazakhLanguage()
                .enterDate("17.04.2024", "Подряд не выпол.15+")
                .clickTenFifteenPlusDays()
                .verifyConsecutiveUnexecutedMatchesApi("17.04.2024", KpiTablePage.ConsecutiveBand.FIFTEEN_PLUS, "kaz", "math_course");



    }


}
