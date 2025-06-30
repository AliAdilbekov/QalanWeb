package testData;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.github.javafaker.Faker;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
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

    @BeforeEach
    public void beforeEach() {
        long start = System.currentTimeMillis();

        Configuration.browser = "chrome";
        Configuration.browserSize = "1530x810";
        Configuration.timeout = 30000; // Ждать элементы до 30 сек
        Configuration.pageLoadTimeout = 90000; // Ждать загрузку страницы
        SelenideLogger.addListener("allure", new AllureSelenide());

        long end = System.currentTimeMillis();
        System.out.println("⏳ Время инициализации: " + (end - start) + " мс");
    }

    @AfterEach
    public void tearDown() {
        Selenide.closeWebDriver();
    }
}