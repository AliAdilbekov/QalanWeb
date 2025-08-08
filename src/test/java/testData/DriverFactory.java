package testData;

import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;

public class DriverFactory {

    private static String baseUrl() {
        return System.getProperty("baseUrl", "https://preprod.qalan.kz"); // NEW
    }
    private static String remote() {
        String r = System.getProperty("selenide.remote");                 // NEW
        return (r != null && !r.isBlank() && !"null".equalsIgnoreCase(r)) ? r : null;
    }

    public static SelenideDriver createStudentDriver() {
        SelenideConfig cfg = new SelenideConfig()
                .baseUrl(baseUrl())                                       // NEW
                .browser("chrome")
                .browserPosition("0x0")
                .browserSize("760x810")
                .timeout(30000)
                .pageLoadTimeout(90000);                                  // NEW

        String r = remote();                                              // NEW
        if (r != null) cfg.remote(r);                                     // NEW

        return new SelenideDriver(cfg);
    }

    public static SelenideDriver createMentorDriver() {
        SelenideConfig cfg = new SelenideConfig()
                .baseUrl(baseUrl())                                       // NEW
                .browser("chrome")
                .browserPosition("780x0")
                .browserSize("760x810")
                .timeout(30000)
                .pageLoadTimeout(90000);                                  // NEW

        String r = remote();                                              // NEW
        if (r != null) cfg.remote(r);                                     // NEW

        return new SelenideDriver(cfg);
    }
}
