package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;


public class KpiTablePage {

    private final SelenideElement
        serviceChapter = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/nav/div[1]/div[4]/span"),
        kpiButton = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/div[1]/div[1]"),
        personalStudyKpi = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/div[3]/div[1]/div[1]"),
        companyInput = $("#select-company"),
        dateInput = $("#date-picker");

    @Step("Логинимся как сервис-пользователь: {phone}")
    public KpiTablePage loginAsService(String phone, String password, String expectedUsername) {
        new LoginPage()
                .openLoginPage()
                .clickSignButton()
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .clickSubmitButton()
                .verifySuccessfulLogin(expectedUsername);
        return this;
    }

    @Step("Нажимаем на раздел сервис")
    public KpiTablePage clickServiceChapter() {
        try {
            serviceChapter.shouldBe(visible, Duration.ofSeconds(10)).click();
            sleep(2000);
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось нажать на раздел 'Сервис': " + e.getMessage());
        }
        return this;
    }

    @Step("Нажимаем на кнопку статистика")
    public KpiTablePage clickKpiButton() {
        try {
            kpiButton.shouldBe(visible, Duration.ofSeconds(10)).click();
            sleep(2000);
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось нажать на кнопку 'Статистика': " + e.getMessage());
        }
        return this;
    }

    @Step("Выбираем модуль Персоналное обучение KPI")
    public KpiTablePage clickPersonalStudyKpi() {
        try {
            personalStudyKpi.shouldBe(visible, Duration.ofSeconds(10)).click();
            sleep(2000);
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось выбрать 'Персоналное обучение KPI': " + e.getMessage());
        }
        return this;
    }

    @Step("Вводим название компании: {companyName} и жмем Enter")
    public KpiTablePage typeCompanyNameAndSelect(String companyName) {
        try {
            companyInput.shouldBe(visible, Duration.ofSeconds(10))
                    .setValue(companyName)
                    .pressEnter();
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось выбрать компанию: " + companyName + " — " + e.getMessage());
        }
        return this;
    }

    @Step("Вводим дату: {date} и ждём обновления таблицы")
    public KpiTablePage enterDate(String date) {
        try {
            dateInput.shouldBe(visible, Duration.ofSeconds(10)).setValue(date).pressEnter();
            $("#kpi-td-date").shouldBe(visible, Duration.ofSeconds(10)).shouldHave(text(date));
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось ввести дату или таблица не обновилась: " + e.getMessage());
        }
        return this;
    }


    @Step("Проверяем KPI-данные с логом")
    public KpiTablePage verifyKpiRowDataWithLogs(
            String expectedDate,
            String expectedGraduates,
            String expectedSubscribers,
            String expectedKpi,
            String expectedFreezings,
            String expectedFreezingPercent) {

        String actualDate = $("#kpi-td-date").shouldBe(visible).getText();
        String actualGraduates = $("#kpi-td-graduates").shouldBe(visible).getText();
        String actualSubscribers = $("#kpi-td-subscribers").shouldBe(visible).getText();
        String actualKpi = $("#kpi-td-kpi").shouldBe(visible).getText();
        String actualFreezings = $("#kpi-td-freezings").shouldBe(visible).getText();
        String actualFreezingPercent = $("#kpi-td-freezing-percent").shouldBe(visible).getText();

        System.out.println("🔍 [KPI DATA]");
        System.out.println("Дата:         ожидаем '" + expectedDate + "' | найдено '" + actualDate + "'");
        System.out.println("Выполнили:    ожидаем '" + expectedGraduates + "' | найдено '" + actualGraduates + "'");
        System.out.println("Подписались:  ожидаем '" + expectedSubscribers + "' | найдено '" + actualSubscribers + "'");
        System.out.println("KPI:          ожидаем '" + expectedKpi + "' | найдено '" + actualKpi + "'");
        System.out.println("Заморозки:    ожидаем '" + expectedFreezings + "' | найдено '" + actualFreezings + "'");
        System.out.println("Процент зам.: ожидаем '" + expectedFreezingPercent + "' | найдено '" + actualFreezingPercent + "'");

        // Сравнения с ошибкой при расхождении
        assert actualDate.equals(expectedDate) : "❌ Дата не совпадает!";
        assert actualGraduates.equals(expectedGraduates) : "❌ Кол-во выполнивших не совпадает!";
        assert actualSubscribers.equals(expectedSubscribers) : "❌ Кол-во подписавшихся не совпадает!";
        assert actualKpi.equals(expectedKpi) : "❌ KPI не совпадает!";
        assert actualFreezings.equals(expectedFreezings) : "❌ Заморозки не совпадают!";
        assert actualFreezingPercent.equals(expectedFreezingPercent) : "❌ Процент заморозки не совпадает!";

        System.out.println("✅ Все значения совпадают");
        return this;
    }

}
