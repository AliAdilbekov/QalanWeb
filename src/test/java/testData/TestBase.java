package testData;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.github.javafaker.Faker;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import pages.*;


public class TestBase {
    protected static final Faker faker = new Faker();
    protected LoginPage loginPage = new LoginPage();
    protected RegistrationPage registrationPage = new RegistrationPage();
    protected PersonalTaskPage personalTaskPage = new PersonalTaskPage();
    protected PupilCashbackPage pupilCashbackPage = new PupilCashbackPage();
    protected KpiTablePage kpiTablePage = new KpiTablePage();
    protected SbbPage sbbPage = new SbbPage();

    @BeforeAll
    static void globalSetUp() {
        RestAssured.useRelaxedHTTPSValidation();

        Configuration.baseUrl = System.getProperty("baseUrl", "https://preprod.qalan.kz");
        RestAssured.baseURI = Configuration.baseUrl;
        RestAssured.basePath = "/api";

        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        // Фуллскрин управляем флагами/капаками, а не фикс-размером
        Configuration.browserSize = null;

        var opts = new org.openqa.selenium.chrome.ChromeOptions();

        if (Configuration.headless) {
            // maximize — задаём окно явно
            String res = System.getProperty("res", "2560x1600");
            String[] wh = res.split("x");
            opts.addArguments("--headless=new");
            opts.addArguments("--window-size=" + wh[0] + "," + wh[1]);
        } else {
            // Кроссплатформенно: Win/Linux — maximize, macOS — fullscreen (игнорится где не нужно)
            opts.addArguments("--start-maximized");
            opts.addArguments("--start-fullscreen");
        }

        // REMOTE (Selenoid / Selenium Grid)
        String remote = System.getProperty("selenide.remote");
        if (remote != null && !remote.isBlank()) {
            Configuration.remote = remote;

            // Selenoid: задаём реальное разрешение Xvfb
            String sr = System.getProperty("screenResolution", "2560x1600x24");
            java.util.Map<String, Object> selenoid = new java.util.HashMap<>();
            selenoid.put("enableVNC", true);
            selenoid.put("screenResolution", sr);
            opts.setCapability("selenoid:options", selenoid);

            // Selenium Grid (Docker images): vendor capability (игнорится там, где не поддерживается)
            opts.setCapability("se:screenResolution", sr.replace("x24", ""));
        }

        Configuration.browserCapabilities = opts;

        Configuration.timeout = Long.parseLong(System.getProperty("timeout", "30000"));
        Configuration.pageLoadTimeout = Long.parseLong(System.getProperty("pageLoadTimeout", "90000"));
    }


    @BeforeEach
    public void beforeEach() {
        long start = System.currentTimeMillis();

        SelenideLogger.addListener("allure", new AllureSelenide()
                .savePageSource(true)
                .screenshots(true)
        );

        long end = System.currentTimeMillis();
        System.out.println("⏳ Время инициализации: " + (end - start) + " мс");
    }

    @AfterEach
    public void tearDown() {
        Selenide.closeWebDriver();
    }
}