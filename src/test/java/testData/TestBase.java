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
        Configuration.browserSize = System.getProperty("browserSize", "1530x810");
        Configuration.timeout = Long.parseLong(System.getProperty("timeout", "30000"));
        Configuration.pageLoadTimeout = Long.parseLong(System.getProperty("pageLoadTimeout", "90000"));
        //Configuration.remote = System.getProperty("selenide.remote");
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