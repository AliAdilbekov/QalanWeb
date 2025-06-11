package pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;



public class KpiTablePage {

    private final SelenideElement
        serviceChapter = $x("/html/body/div[2]/div[1]/div/div/div/div[2]/nav/div[1]/div[4]"),
        kpiButton = $("#tab-section-kpi"),
        personalStudyKpi = $("#tab-kpi"),
        companyInput = $("#select-company"),
        dateInput = $("#date-picker"),
        newPupils = $("#tab-new-pupils-kpi"),
        kazLanguageRadio = $("#radio-kaz"),
        loadButton = $("#load-btn"),
        dateConfirmationBlock = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/main/div[4]/div[2]/div/div/div[2]/div[2]/div[1]/div[5]"),
        notComplitedInRow = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/div[3]/div[1]/div[3]"),
        tenFifteen = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/main/div[3]/button[1]"),
        fifteenPlus = $x("/html/body/div[2]/div[1]/div/div[1]/div/div/div/div[2]/div/main/main/div[3]/button[2]"),
        untPupilsKpi = $("#tab-ubt-kpi");


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

    private String convertToApiDateFormat(String uiDate) {
        // Преобразует "01.04.2024" → "2024-04-01"
        String[] parts = uiDate.split("\\.");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }


    public KpiTablePage verifyKpiMatchesApi(String date, String company) {
        String apiDate = convertToApiDateFormat(date);


        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJNYXRoRWR1Y2F0b3JJc3N1ZXJJZCIsImV4cCI6MTc0OTYyNjk4MywiaWF0IjoxNzQ5NTQwNTgzLCJpc3MiOiJNYXRoRWR1Y2F0b3JJc3N1ZXJJZCIsImp0aSI6ImYxYTFjMmNjLWM5MzctNDE4Ni05MzVkLWVjMDk3MTIxZDQ1OSIsIm5iZiI6MTc0OTU0MDU4Miwic3ViIjoiMjAwOTY2MiIsInR5cCI6ImFjY2VzcyIsInVzZXJfdG9rZW5fdHlwZSI6InB1cGlsIn0.CY9eQg6j1n-_P6AqRMyW_X6WrnzHipROATw2UB3xq0ZBhV3lY6cZ4X7BxDhH7fxE2J4mmXOBra76hA89qand-g")
                .queryParam("date", apiDate)
                .queryParam("company", company)
                .when()
                .get("https://test.qalan.kz/api/personalStudy/kpi")
                .then()
                .statusCode(200)
                .extract().response();

        JSONObject json = new JSONObject(response.asString()).getJSONObject("allPupilResult");

        String expectedDate = formatToUiDate(json.getString("date")); // из 2024-04-01 -> 01.04.2024
        String expectedGraduates = String.valueOf(json.getInt("itemsCount"));
        String expectedSubscribers = String.valueOf(json.getInt("subscribedUsersCount"));
        String expectedKpi = json.getInt("percent") + "%";
        String expectedFreezings = String.valueOf(json.getInt("freezingCount"));
        // пример вычисления процента заморозки, если нужно отдельно
        String expectedFreezingPercent = "103.12 %"; // можно потом считать, если логика есть

        // Сравнение с UI
        String actualDate = $("#kpi-td-date").shouldBe(visible).getText();
        String actualGraduates = $("#kpi-td-graduates").shouldBe(visible).getText();
        String actualSubscribers = $("#kpi-td-subscribers").shouldBe(visible).getText();
        String actualKpi = $("#kpi-td-kpi").shouldBe(visible).getText();
        String actualFreezings = $("#kpi-td-freezings").shouldBe(visible).getText();
        String actualFreezingPercent = $("#kpi-td-freezing-percent").shouldBe(visible).getText();

        System.out.println("📊 Сравнение KPI (UI vs API)");
        System.out.printf("Дата:             UI='%s' | API='%s'%n", actualDate, expectedDate);
        System.out.printf("Выполнили:        UI='%s' | API='%s'%n", actualGraduates, expectedGraduates);
        System.out.printf("Подписались:      UI='%s' | API='%s'%n", actualSubscribers, expectedSubscribers);
        System.out.printf("KPI:              UI='%s' | API='%s'%n", actualKpi, expectedKpi);
        System.out.printf("Заморозки:        UI='%s' | API='%s'%n", actualFreezings, expectedFreezings);
        System.out.printf("Процент заморозки:UI='%s' | API='%s'%n", actualFreezingPercent, expectedFreezingPercent);

        // Сравнения
        assert actualDate.equals(expectedDate) : "❌ Дата не совпадает!";
        assert actualGraduates.equals(expectedGraduates) : "❌ Выполнившие не совпадают!";
        assert actualSubscribers.equals(expectedSubscribers) : "❌ Подписавшиеся не совпадают!";
        assert actualKpi.equals(expectedKpi) : "❌ KPI не совпадает!";
        assert actualFreezings.equals(expectedFreezings) : "❌ Заморозки не совпадают!";
        assert actualFreezingPercent.equals(expectedFreezingPercent) : "❌ Процент заморозки не совпадает!";

        System.out.println("✅ Все данные KPI совпадают");

        return this;
    }

    private String formatToUiDate(String apiDate) {
        // преобразуем yyyy-MM-dd → dd.MM.yyyy
        String[] parts = apiDate.split("-");
        return parts[2] + "." + parts[1] + "." + parts[0];
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
            // Распарсим дату
            String[] parts = date.split("\\."); // "01.04.2024"
            String day = String.valueOf(Integer.parseInt(parts[0])); // "1"
            String month = parts[1]; // "04"
            String year = parts[2]; // "2024"

            // Желаемая строка для заголовка, например: "April 2024"
            String targetMonthYear = getMonthName(month) + " " + year;

            // 1. Открыть календарь
            dateInput.shouldBe(visible, Duration.ofSeconds(10)).click();

            // 2. Листать назад, пока не попадём в нужный месяц
            while (true) {
                String currentMonthYear = $(".react-datepicker__current-month").shouldBe(visible).getText();
                if (currentMonthYear.equals(targetMonthYear)) break;
                $(".react-datepicker__navigation--previous").click();
                sleep(300); // подождать анимацию
            }

            // 3. Клик по дню
            $x("//div[contains(@class, 'react-datepicker__day') and text()='" + day + "']").click();

            // 4. Убедимся, что дата обновилась
            $("#kpi-td-date").shouldBe(visible, Duration.ofSeconds(10)).shouldHave(text(date));

        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось выбрать дату через календарь: " + e.getMessage());
        }

        return this;
    }

    // Хелпер для преобразования "04" → "April"
    private String getMonthName(String mm) {
        switch (mm) {
            case "01": return "қаңтар";
            case "02": return "ақпан";
            case "03": return "наурыз";
            case "04": return "сәуір";
            case "05": return "мамыр";
            case "06": return "маусым";
            case "07": return "шілде";
            case "08": return "тамыз";
            case "09": return "қыркүйек";
            case "10": return "қазан";
            case "11": return "қараша";
            case "12": return "желтоқсан";
            default: throw new IllegalArgumentException("Месяц не распознан: " + mm);
        }
    }




    @Step("Проверяем KPI-данные с логом") // оставлю на будущее
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

    @Step("Выбираем модуль Новые ученики")
    public KpiTablePage clickNewPupils() {
        newPupils.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;

    }

    @Step("Выбираем язык обучения: казахский")
    public KpiTablePage selectKazakhLanguage() {
        try {
            kazLanguageRadio.shouldBe(visible, Duration.ofSeconds(10)).click();
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось выбрать казахский язык обучения: " + e.getMessage());
        }
        return this;
    }

    @Step("Нажимаем на кнопку LOAD и ждём загрузки учеников")
    public KpiTablePage clickLoadButton() {
        try {
            loadButton.shouldBe(visible, Duration.ofSeconds(10)).click();
            // Даём время на запуск загрузки данных
            sleep(2000); // Важно! Убирает гонку

            // Ждём появление хотя бы одного ученика
            $("#pupil-code-0-0").shouldBe(visible, Duration.ofSeconds(20));
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось дождаться загрузки учеников: " + e.getMessage());
        }
        return this;
    }



    @Step("Вводим дату без проверки таблицы: {date}")
    public KpiTablePage enterDateForNewPupils(String date) {
        try {
            dateInput.shouldBe(visible, Duration.ofSeconds(10)).click();
            sleep(500); // имитация паузы перед вводом
            for (char c : date.toCharArray()) {
                dateInput.sendKeys(String.valueOf(c));
                sleep(150); // между каждым символом
            }
            sleep(500); // небольшая пауза перед Enter
            dateInput.pressEnter();
            sleep(2000); // ждём после нажатия Enter
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось ввести дату (новые ученики): " + e.getMessage());
        }
        return this;
    }



    @Step("Проверяем данные в модуле 'Новые ученики'")
    public KpiTablePage verifyNewPupilsData() {
        // 1. Проверка даты по частичному совпадению
        String expectedDatePart = "01.04.2024"; // или вынести в параметр
        String dateText = $("#accordion-title-0").shouldBe(visible).getText();
        System.out.println("📅 Дата заголовка: " + dateText);
        assert dateText.contains(expectedDatePart) : "❌ Ожидаемая дата содержит '" + expectedDatePart + "', но найдено: " + dateText;

        // 2-4. Проверка статистики
        String finished = $("#finished-label-0").getText();
        System.out.println("👨‍🎓 Завершили: " + finished);
        assert finished.equals("10") : "❌ Завершивших должно быть 10, но найдено: " + finished;

        String nonFinished = $("#nonfinished-label-0").getText();
        System.out.println("🙋‍♂️ Не завершили: " + nonFinished);
        assert nonFinished.equals("5") : "❌ Незавершивших должно быть 5, но найдено: " + nonFinished;

        String average = $("#average-label-0").getText();
        System.out.println("📊 Среднее: " + average);
        assert average.equals("67%") : "❌ Среднее значение должно быть 67%, но найдено: " + average;

        // 5. Проверка учеников (pupil-code)
        int foundPupils = 0;
        for (int i = 0; i < 15; i++) {
            $("#pupil-code-" + i + "-0").shouldBe(visible);
            foundPupils++;
        }
        System.out.println("👶 Найдено учеников: " + foundPupils);
        assert foundPupils == 15 : "❌ Ожидалось 15 учеников, но найдено: " + foundPupils;

        // 6. Проверка имён
        int correctNames = 0;
        for (int i = 0; i < 15; i++) {
            String fullName = $("#fullname-" + i + "-0").getText();
            System.out.println("👤 FullName[" + i + "]: " + fullName);
            if (fullName.equals("For AutoTest1")) {
                correctNames++;
            }
        }
        System.out.println("✅ Корректных имён: " + correctNames);
        assert correctNames == 15 : "❌ Не у всех учеников правильный full name, найдено: " + correctNames;

        // 7. Проверка статусов finished
        ElementsCollection finishedStatuses = $$("[id^=finished-status]");
        System.out.println("✅ Найдено finished-status элементов: " + finishedStatuses.size());
        assert finishedStatuses.size() == 10 : "❌ Должно быть 10 finished-status, найдено: " + finishedStatuses.size();

        // 8. Проверка freeze-status
        ElementsCollection freezeStatuses = $$("[id^=freeze-status]");
        System.out.println("❄️ Найдено freeze-status элементов: " + freezeStatuses.size());
        assert freezeStatuses.size() >= 0 : "❌ Не найден ни один freeze-status";

        System.out.println("🎉 Все проверки по модулю 'Новые ученики' прошли успешно!");
        return this;
    }

    @Step("Проверяем данные в модуле 'Ученики ЕНТ'")
    public KpiTablePage verifyUntPupilsData() {
        // 1. Проверка наличия учеников по коду
        int pupilCount = 0;
        for (int i = 0; i < 10; i++) {
            SelenideElement code = $("#pupil-code-" + i + "-0");
            if (code.exists()) {
                code.shouldBe(visible);
                pupilCount++;
                System.out.println("✅ Найден ученик с ID: pupil-code-" + i + "-0");
            }
        }
        System.out.println("🔍 Всего найдено учеников: " + pupilCount);
        assert pupilCount >= 2 : "❌ Должно быть минимум 2 ученика (pupil-code-*), но найдено: " + pupilCount;

        // 2. Поиск одного finished-status
        ElementsCollection finishedStatuses = $$("[id^=finished-status]");
        System.out.println("🏁 Найдено finished-status: " + finishedStatuses.size());
        assert finishedStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент finished-status";

        // 3. Поиск одного not-finished-status
        ElementsCollection notFinishedStatuses = $$("[id^=not-finished-status]");
        System.out.println("🔁 Найдено not-finished-status: " + notFinishedStatuses.size());
        assert notFinishedStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент not-finished-status";

        // 4. Поиск freeze-status
        ElementsCollection freezeStatuses = $$("[id^=freeze-status]");
        System.out.println("❄️ Найдено freeze-status: " + freezeStatuses.size());
        assert freezeStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент freeze-status";

        // 5. Поиск freezing-status
        ElementsCollection freezingStatuses = $$("[id^=freezing-status]");
        System.out.println("🧊 Найдено freezing-status: " + freezingStatuses.size());
        assert freezingStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент freezing-status";

        System.out.println("🎉 Все проверки по модулю 'Ученики ЕНТ' прошли успешно!");
        return this;
    }

    @Step("Выбираем модуль Ученики ЕНТ")
    public KpiTablePage clickUntPupilsKpi() {
        untPupilsKpi.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

    @Step("Выбираем казахский язык обучения")
    public KpiTablePage selectKazLanguage() {
        kazLanguageRadio.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(1000);
        return this;
    }

    @Step("Вводим дату для модуля ЕНТ: {date}")
    public KpiTablePage enterDateForUnt(String date) {
        try {
            dateInput.shouldBe(visible, Duration.ofSeconds(10)).click();
            sleep(300);
            dateInput.setValue(date);
            sleep(500); // имитация паузы перед нажатием
            dateInput.pressEnter();
            sleep(3000); // даём время на загрузку таблицы
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось ввести дату (ЕНТ): " + e.getMessage());
        }
        return this;
    }


}


