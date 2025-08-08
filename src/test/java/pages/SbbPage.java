package pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

public class SbbPage {

    private final SelenideElement
            sbbSectionButton = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/div/div[1]"),
            surveyButton = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/div[2]/div[1]/div[1]"),
            dateInput = $("#date-picker"),
            companyDropdown = $x("//div[contains(@class,'Select-placeholder') and text()='Компания']"),
            viewButton = $("#view-btn"),
            parentCountLabel = $("#parent-count");

    @Step("Переходим в раздел функционала СББ")
    public SbbPage clickSbbFunctionality() {
        sbbSectionButton.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        return this;
    }

    @Step("Открываем раздел опроса")
    public SbbPage clickSurveyButton() {
        surveyButton.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        return this;
    }

    @Step("Выбираем дату через календарь: {date}")
    public SbbPage enterSurveyDate(String date) {
        try {
            String[] parts = date.split("\\.");
            String day = String.valueOf(Integer.parseInt(parts[0]));
            String month = parts[1];
            String year = parts[2];

            String targetMonthYear = getMonthName(month) + " " + year;

            dateInput.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();

            while (true) {
                String currentMonthYear = $(".react-datepicker__current-month").shouldBe(Condition.visible).getText();
                if (currentMonthYear.equals(targetMonthYear)) break;
                $(".react-datepicker__navigation--previous").click();
                sleep(300);
            }

            $x("//div[contains(@class, 'react-datepicker__day') and text()='" + day + "']").click();
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось выбрать дату через календарь: " + e.getMessage());
        }

        return this;
    }

    private String getMonthName(String mm) {
        switch (mm) {
            case "01":
                return "қаңтар";
            case "02":
                return "ақпан";
            case "03":
                return "наурыз";
            case "04":
                return "сәуір";
            case "05":
                return "мамыр";
            case "06":
                return "маусым";
            case "07":
                return "шілде";
            case "08":
                return "тамыз";
            case "09":
                return "қыркүйек";
            case "10":
                return "қазан";
            case "11":
                return "қараша";
            case "12":
                return "желтоқсан";
            default:
                throw new IllegalArgumentException("Месяц не распознан: " + mm);
        }
    }

    @Step("Выбираем компанию: {company}")
    public SbbPage selectCompany(String company) {
        companyDropdown.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();

        $x(String.format("//div[contains(@class,'Select-option') and text()='%s']", company))
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        return this;
    }

    @Step("Нажимаем кнопку 'Показать'")
    public SbbPage clickViewButton() {
        viewButton.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        return this;
    }

    @Step("Проверяем, что отображается {expectedCount} родителей")
    public SbbPage verifyParentCount(int expectedCount) {
        parentCountLabel.shouldBe(Condition.visible, Duration.ofSeconds(10))
                .shouldHave(Condition.text(String.valueOf(expectedCount)));
        return this;
    }
}
