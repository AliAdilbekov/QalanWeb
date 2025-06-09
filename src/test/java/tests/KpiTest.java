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
                .enterDate("01.04.2022")
                .verifyKpiRowDataWithLogs(
                        "01.04.2022",
                        "12",
                        "24",
                        "50%",
                        "3",
                        "900.00 %"
                );
    }


}

