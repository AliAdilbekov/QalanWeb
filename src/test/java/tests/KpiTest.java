package tests;

import org.junit.jupiter.api.Test;
import pages.KpiTablePage;
import testData.TestBase;

public class KpiTest extends TestBase {

    @Test
    void testPersonalStudyKpi() {
        new KpiTablePage()
                .loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickPersonalStudyKpi()
                .typeCompanyNameAndSelect("qalan.kz")
                .enterDate("01.04.2024")
                .verifyKpiMatchesApi("01.04.2024", "qalan.kz");
    }


    @Test
    void testNewPupilsChapter() {
        new KpiTablePage()
                .loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickNewPupils()
                .selectKazakhLanguage()
                .enterDateForNewPupils("01.04.2024")
                .clickLoadButton()
                .verifyNewPupilsData();
    }

    @Test
    void testUntPupilsKpi() {
        new KpiTablePage()
                .loginAsService("7000000000", "12345678", "test sbb")
                .clickServiceChapter()
                .clickKpiButton()
                .clickUntPupilsKpi()
                .selectKazLanguage()
                .enterDateForUnt("01.04.2024")
                .clickLoadButton()
                .verifyUntPupilsData();
    }




}

