package pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import testData.TokenProvider;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;


public class KpiTablePage {

    private final SelenideElement
            serviceChapter = $("#nav-service-dashboard"),
            kpiButton = $("#tab-section-kpi"),
            personalStudyKpi = $("#tab-kpi"),
            companyInput = $("#select-company"),
            dateInput = $("#date-picker"),
            newPupils = $("#tab-new-pupils-kpi"),
            kazLanguageRadio = $("#radio-kaz"),
            loadButton = $("#load-btn"),
            notComplitedInRow = $("#tab-consecutive-unexecuted-kpi-label"),
            tenFifteen = $("#btn-10-15-days"),
            fifteenPlus = $("#btn-15-plus-days"),
            untPupilsKpi = $("#tab-ubt-kpi"),
            oneThirtyDays = $x("//button[contains(text(), '1-30')]"),
            zhukteuUntButton = $x("//button[contains(text(), 'жүктеу')]");


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


    @Step("Сравниваем KPI с API: дата={date}, компания={company}")
    public void verifyKpiMatchesApi(String date, String company) {
        String apiDate = convertToApiDateFormat(date);

        Response response = RestAssured
                .given()
                .header("Authorization", "Bearer " + TokenProvider.getBearerToken())
                .queryParam("date", apiDate)
                .queryParam("company", company)
                .when()
                .get("/personalStudy/kpi");

        // прикладываем статус и тело (JSON) для дебага
        attachText("API status", String.valueOf(response.statusCode()));
        String rawBody = response.asString();
        String prettyBody = safePrettyJson(rawBody);
        attachJson("API /personalStudy/kpi body", prettyBody);

        if (response.statusCode() != 200) {
            throw new RuntimeException("❌ API != 200. Код: " + response.statusCode() + "\nТело:\n" + prettyBody);
        }

        JSONObject root = new JSONObject(rawBody).getJSONObject("allPupilResult");

        // Ожидания из API
        String expectedDate = formatToUiDate(root.getString("date")); // 2024-04-01 -> 01.04.2024
        String expectedGraduates = String.valueOf(root.optInt("itemsCount", 0));
        String expectedSubscribers = String.valueOf(root.optInt("subscribedUsersCount", 0));
        String expectedKpi = root.optInt("percent", 0) + "%";

        boolean hasFreezings = root.has("freezingCount");
        String expectedFreezings = hasFreezings ? String.valueOf(root.optInt("freezingCount", 0)) : null;

        boolean hasFreezingPercent = root.has("freezingPercent");
        String expectedFreezingPercent = hasFreezingPercent ? (root.optInt("freezingPercent", 0) + "%") : null;

        // UI (нормализуем)
        String actualDate = normText($("#kpi-td-date").shouldBe(visible).getText());
        String actualGraduates = normDigits($("#kpi-td-graduates").shouldBe(visible).getText());
        String actualSubscribers = normDigits($("#kpi-td-subscribers").shouldBe(visible).getText());
        String actualKpi = normPercent($("#kpi-td-kpi").shouldBe(visible).getText());
        String actualFreezings = hasFreezings ? normDigits($("#kpi-td-freezings").shouldBe(visible).getText()) : null;
        String actualFreezingPercent = hasFreezingPercent ? normPercent($("#kpi-td-freezing-percent").shouldBe(visible).getText()) : null;

        // компактная сводка (в Allure)
        String summary = ""
                + row("Дата", actualDate, expectedDate)
                + row("Выполнили", actualGraduates, expectedGraduates)
                + row("Подписались", actualSubscribers, expectedSubscribers)
                + row("KPI", actualKpi, expectedKpi)
                + (hasFreezings ? row("Заморозки", actualFreezings, expectedFreezings) : "")
                + (hasFreezingPercent ? row("Процент заморозки", actualFreezingPercent, expectedFreezingPercent) : "");
        System.out.println("📊 KPI (UI vs API)\n" + summary);
        attachText("KPI сравнение (UI vs API)", summary);

        // Сравнения
        assert actualDate.equals(expectedDate) : "❌ Дата не совпадает!\n" + summary + "\nAPI:\n" + prettyBody;
        assert actualGraduates.equals(expectedGraduates) : "❌ Выполнившие не совпадают!\n" + summary + "\nAPI:\n" + prettyBody;
        assert actualSubscribers.equals(expectedSubscribers) : "❌ Подписавшиеся не совпадают!\n" + summary + "\nAPI:\n" + prettyBody;
        assert actualKpi.equals(expectedKpi) : "❌ KPI не совпадает!\n" + summary + "\nAPI:\n" + prettyBody;
        if (hasFreezings)       assert actualFreezings.equals(expectedFreezings) : "❌ Заморозки не совпадают!\n" + summary + "\nAPI:\n" + prettyBody;
        if (hasFreezingPercent) assert actualFreezingPercent.equals(expectedFreezingPercent) : "❌ Процент заморозки не совпадает!\n" + summary + "\nAPI:\n" + prettyBody;

        System.out.println("✅ KPI: основные поля совпадают"
                + (!hasFreezings ? " (заморозки не пришли из API — скип)" : "")
                + (!hasFreezingPercent ? " (процент заморозки не пришёл — скип)" : ""));
    }

    //хелперы

    private static String row(String name, String ui, String api) {
        return String.format("%-16s UI='%s' | API='%s'%n", name + ":", ui, api);
    }

    private static String safePrettyJson(String body) {
        try {
            if (body.trim().startsWith("[")) {
                return new org.json.JSONArray(body).toString(2);
            } else {
                return new org.json.JSONObject(body).toString(2);
            }
        } catch (Exception e) {
            return body; // если это не JSON — вернём как есть
        }
    }

    @Attachment(value = "{name}", type = "text/plain")
    private static String attachText(String name, String content) { return content; }

    @Attachment(value = "{name}", type = "application/json")
    private static String attachJson(String name, String content) { return content; }

    // формат даты оставляю как есть
    private static String normText(String s) { /* ... как у тебя ... */ return s == null ? "" :
            s.replace('\u00A0',' ').replace('\u202F',' ').trim().replaceAll("\\s+", " "); }
    private static String normDigits(String s) { String d = normText(s).replaceAll("[^\\d-]", ""); return d.isEmpty() ? "0" : d; }
    private static String normPercent(String s) { String d = normText(s).replaceAll("[^\\d-]", ""); return (d.isEmpty()? "0": d) + "%"; }

    private String formatToUiDate(String apiDate) {
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


//    @Step("Проверяем данные в модуле 'Новые ученики'")
//    public void verifyNewPupilsData() {
//        // 1. Проверка даты по частичному совпадению
//        String expectedDatePart = "01.04.2024"; // или вынести в параметр
//        String dateText = $("#accordion-title-0").shouldBe(visible).getText();
//        System.out.println("📅 Дата заголовка: " + dateText);
//        assert dateText.contains(expectedDatePart) : "❌ Ожидаемая дата содержит '" + expectedDatePart + "', но найдено: " + dateText;
//
//        // 2-4. Проверка статистики
//        String finished = $("#finished-label-0").getText();
//        System.out.println("👨‍🎓 Завершили: " + finished);
//        assert finished.equals("10") : "❌ Завершивших должно быть 10, но найдено: " + finished;
//
//        String nonFinished = $("#nonfinished-label-0").getText();
//        System.out.println("🙋‍♂️ Не завершили: " + nonFinished);
//        assert nonFinished.equals("5") : "❌ Незавершивших должно быть 5, но найдено: " + nonFinished;
//
//        String average = $("#average-label-0").getText();
//        System.out.println("📊 Среднее: " + average);
//        assert average.equals("67%") : "❌ Среднее значение должно быть 67%, но найдено: " + average;
//
//        // 5. Проверка учеников (pupil-code)
//        int foundPupils = 0;
//        for (int i = 0; i < 15; i++) {
//            $("#pupil-code-" + i + "-0").shouldBe(visible);
//            foundPupils++;
//        }
//        System.out.println("👶 Найдено учеников: " + foundPupils);
//        assert foundPupils == 15 : "❌ Ожидалось 15 учеников, но найдено: " + foundPupils;
//
//        // 6. Проверка имён
//        int correctNames = 0;
//        for (int i = 0; i < 15; i++) {
//            String fullName = $("#fullname-" + i + "-0").getText();
//            System.out.println("👤 FullName[" + i + "]: " + fullName);
//            if (fullName.equals("For AutoTest1")) {
//                correctNames++;
//            }
//        }
//        System.out.println("✅ Корректных имён: " + correctNames);
//        assert correctNames == 15 : "❌ Не у всех учеников правильный full name, найдено: " + correctNames;
//
//        // 7. Проверка статусов finished
//        ElementsCollection finishedStatuses = $$("[id^=finished-status]");
//        System.out.println("✅ Найдено finished-status элементов: " + finishedStatuses.size());
//        assert finishedStatuses.size() == 10 : "❌ Должно быть 10 finished-status, найдено: " + finishedStatuses.size();
//
//        // 8. Проверка freeze-status
//        ElementsCollection freezeStatuses = $$("[id^=freeze-status]");
//        System.out.println("❄️ Найдено freeze-status элементов: " + freezeStatuses.size());
//        assert freezeStatuses.size() >= 0 : "❌ Не найден ни один freeze-status";
//
//        System.out.println("🎉 Все проверки по модулю 'Новые ученики' прошли успешно!");
//    }

//    @Step("Проверяем данные в модуле 'Ученики ЕНТ'")
//    public void verifyUntPupilsData() {
//        // 1. Проверка наличия учеников по коду
//        int pupilCount = 0;
//        for (int i = 0; i < 10; i++) {
//            SelenideElement code = $("#pupil-code-" + i + "-0");
//            if (code.exists()) {
//                code.shouldBe(visible);
//                pupilCount++;
//                System.out.println("✅ Найден ученик с ID: pupil-code-" + i + "-0");
//            }
//        }
//        System.out.println("🔍 Всего найдено учеников: " + pupilCount);
//        assert pupilCount >= 2 : "❌ Должно быть минимум 2 ученика (pupil-code-*), но найдено: " + pupilCount;
//
//        // 2. Поиск одного finished-status
//        ElementsCollection finishedStatuses = $$("[id^=finished-status]");
//        System.out.println("🏁 Найдено finished-status: " + finishedStatuses.size());
//        assert finishedStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент finished-status";
//
//        // 3. Поиск одного not-finished-status
//        ElementsCollection notFinishedStatuses = $$("[id^=not-finished-status]");
//        System.out.println("🔁 Найдено not-finished-status: " + notFinishedStatuses.size());
//        assert notFinishedStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент not-finished-status";
//
//        // 4. Поиск freeze-status
//        ElementsCollection freezeStatuses = $$("[id^=freeze-status]");
//        System.out.println("❄️ Найдено freeze-status: " + freezeStatuses.size());
//        assert freezeStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент freeze-status";
//
//        // 5. Поиск freezing-status
//        ElementsCollection freezingStatuses = $$("[id^=freezing-status]");
//        System.out.println("🧊 Найдено freezing-status: " + freezingStatuses.size());
//        assert freezingStatuses.size() >= 1 : "❌ Ожидался хотя бы 1 элемент freezing-status";
//
//        System.out.println("🎉 Все проверки по модулю 'Ученики ЕНТ' прошли успешно!");
//    }

//    @Step("Выбираем модуль Ученики ЕНТ")
//    public KpiTablePage clickUntPupilsKpi() {
//        untPupilsKpi.shouldBe(visible, Duration.ofSeconds(10)).click();
//        sleep(2000);
//        return this;
//    }

//    @Step("Выбираем казахский язык обучения")
//    public KpiTablePage selectKazLanguage() {
//        kazLanguageRadio.shouldBe(visible, Duration.ofSeconds(10)).click();
//        sleep(1000);
//        return this;
//    }

    @Step("Вводим дату для модуля {context}: {date}")
    public KpiTablePage enterDate(String date, String context) {
        try {
            dateInput.shouldBe(visible, Duration.ofSeconds(10)).click();
            sleep(300);
            dateInput.setValue(date);
            sleep(500);
            dateInput.pressEnter();
            sleep(3000);
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось ввести дату для модуля '" + context + "': " + e.getMessage());
        }
        return this;
    }


    @Step("Переходим в модуль 'Подряд не выполняли KPI'")
    public KpiTablePage clickNotCompletedInRow() {
        notComplitedInRow.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

    @Step("Нажимаем кнопку '10-15 дней подряд'")
    public KpiTablePage clickTenFifteenDays() {
        tenFifteen.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

    @Step("Нажимаем кнопку '15+ дней подряд'")
    public KpiTablePage clickTenFifteenPlusDays() {
        fifteenPlus.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }


//    @Step("Проверяем данные подряд не выполнивших: Выполнили={expectedExecuted}, Не выполнили={expectedUnexecuted}, KPI={expectedPercent}")
//    public void verifyConsecutiveUnexecutedData(int expectedExecuted, int expectedUnexecuted, String expectedPercent, int expectedTotal) {
//        try {
//            // Основные KPI значения
//            String actualExecuted = $("#executed-label").shouldBe(visible).getText();
//            String actualUnexecuted = $("#unexecuted-label").shouldBe(visible).getText();
//            String actualPercent = $("#percent-label").shouldBe(visible).getText();
//
//            System.out.printf("🔍 Выполнили: ожидалось=%s, факт=%s%n", expectedExecuted, actualExecuted);
//            System.out.printf("🔍 Не выполнили: ожидалось=%s, факт=%s%n", expectedUnexecuted, actualUnexecuted);
//            System.out.printf("🔍 KPI: ожидалось=%s, факт=%s%n", expectedPercent, actualPercent);
//
//            assert actualExecuted.equals(String.valueOf(expectedExecuted)) : "❌ Выполнивших не совпадает!";
//            assert actualUnexecuted.equals(String.valueOf(expectedUnexecuted)) : "❌ Не выполнивших не совпадает!";
//            assert actualPercent.equals(expectedPercent) : "❌ KPI не совпадает!";
//
//            // Проверка таблицы учеников
//            for (int i = 0; i < expectedTotal; i++) {
//                String userId = "td-userid-0-" + i;
//                String unexecutedId = "td-unexecuted-qty-0-" + i;
//                String freezeId = "freezing-self-no-0-" + i;
//
//                $("#" + userId).shouldBe(visible);
//                String qtyStr = $("#" + unexecutedId).shouldBe(visible).getText();
//                int qty = Integer.parseInt(qtyStr.trim());
//
//                assert qty <= 15 : "❌ Ученик #" + i + " имеет подряд не выполнено > 15: " + qty;
//                $("#" + freezeId).shouldBe(visible);
//            }
//
//            // Проверка количества finished и not-finished
//            ElementsCollection notFinished = $$("[id^='not-finished-label-0-']");
//            ElementsCollection finished = $$("[id^='finished-label-0-']");
//
//            System.out.println("📋 not-finished count: " + notFinished.size());
//            System.out.println("📋 finished count: " + finished.size());
//
//            assert notFinished.size() == (expectedTotal - expectedExecuted) : "❌ Неверное кол-во not-finished!";
//            assert finished.size() == expectedExecuted : "❌ Неверное кол-во finished!";
//
//            System.out.println("✅ Проверка подряд не выполнивших завершена успешно");
//
//        } catch (Exception e) {
//            throw new RuntimeException("❌ Ошибка при проверке подряд не выполнивших: " + e.getMessage());
//        }
//
//    }

//    @Step("Проверяем данные подряд не выполнивших 15+ дней: Выполнили={expectedExecuted}, Не выполнили={expectedUnexecuted}, KPI={expectedPercent}")
//    public void verifyConsecutiveUnexecutedData15Plus(int expectedExecuted, int expectedUnexecuted, String expectedPercent, int expectedTotal) {
//        try {
//            // Основные KPI значения
//            String actualExecuted = $("#executed-label-0").shouldBe(visible).getText();
//            String actualUnexecuted = $("#unexecuted-label-0").shouldBe(visible).getText();
//            String actualPercent = $("#percent-label-0").shouldBe(visible).getText();
//
//            System.out.printf("🔍 Выполнили: ожидалось=%s, факт=%s%n", expectedExecuted, actualExecuted);
//            System.out.printf("🔍 Не выполнили: ожидалось=%s, факт=%s%n", expectedUnexecuted, actualUnexecuted);
//            System.out.printf("🔍 KPI: ожидалось=%s, факт=%s%n", expectedPercent, actualPercent);
//
//            assert actualExecuted.equals(String.valueOf(expectedExecuted)) : "❌ Выполнивших не совпадает!";
//            assert actualUnexecuted.equals(String.valueOf(expectedUnexecuted)) : "❌ Не выполнивших не совпадает!";
//            assert actualPercent.equals(expectedPercent) : "❌ KPI не совпадает!";
//
//            // Проверка таблицы учеников
//            for (int i = 0; i < expectedTotal; i++) {
//                String userId = "td-userid-0-" + i;
//                String unexecutedId = "td-unexecuted-qty-0-" + i;
//                String freezeId = "freezing-self-no-0-" + i;
//
//                $("#" + userId).shouldBe(visible);
//                String qtyStr = $("#" + unexecutedId).shouldBe(visible).getText();
//                int qty = Integer.parseInt(qtyStr.trim());
//
//                assert qty > 15 : "❌ Ученик #" + i + " имеет подряд не выполнено ≤ 15: " + qty;
//                $("#" + freezeId).shouldBe(visible);
//            }
//
//            // Проверка количества finished и not-finished
//            ElementsCollection notFinished = $$("[id^='not-finished-label-0-']");
//            ElementsCollection finished = $$("[id^='finished-label-0-']");
//
//            System.out.println("📋 not-finished count: " + notFinished.size());
//            System.out.println("📋 finished count: " + finished.size());
//
//            assert notFinished.size() == (expectedTotal - expectedExecuted) : "❌ Неверное кол-во not-finished!";
//            assert finished.size() == expectedExecuted : "❌ Неверное кол-во finished!";
//
//            System.out.println("✅ Проверка подряд не выполнивших (15+ дней) завершена успешно");
//
//        } catch (Exception e) {
//            throw new RuntimeException("❌ Ошибка при проверке подряд не выполнивших (15+): " + e.getMessage());
//        }
//
//    }

    @Step("Нажимаем кнопку '1-30 дней подряд'")
    public KpiTablePage clickOneThirtyDays() {
        oneThirtyDays.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

//    @Step("Нажимаем кнопку 'жүктеу' и ждём загрузки учеников")
//    public KpiTablePage clickZhukteuUntButton() {
//        try {
//            zhukteuUntButton.shouldBe(visible, Duration.ofSeconds(10)).click();
//            sleep(2000);
//
//            // Ожидаем появление хотя бы одного ученика
//            $("#pupil-code-0-0").shouldBe(visible, Duration.ofSeconds(20));
//        } catch (Exception e) {
//            throw new RuntimeException("❌ Не удалось дождаться загрузки после нажатия 'жүктеу': " + e.getMessage());
//        }
//        return this;
//    }


    @Step("Сравниваем 'Новые ученики' с API: дата={uiDate}, lang={lang}, {dayFrom}-{dayTo} дней")
    public void verifyNewPupilsMatchesApi(String uiDate,
                                          String lang,
                                          int dayFrom,
                                          int dayTo,
                                          String country,
                                          String kpiType) {
        // Дата для API: локальная
        String apiDateIsoZ = toIsoZInstant(uiDate);

        // Запрос
        Response resp = RestAssured
                .given()
                .header("Authorization", "Bearer " + TokenProvider.getBearerToken())
                .queryParam("studyLanguage", lang)              // "kaz"
                .queryParam("date", apiDateIsoZ)                // "2024-03-31T18:00:00.000Z"
                .queryParam("country", country)                 // "Kazakhstan"
                .queryParam("kpiType", kpiType)                 // "only_new_pupils"
                .queryParam("dayFrom", dayFrom)                 // 0
                .queryParam("dayTo", dayTo)                     // 10
                .when()
                .get("/personalStudy/groupedByDate");

        String raw = resp.asString();
        String pretty = safePrettyJson(raw);
        attachText("NewPupils API status", String.valueOf(resp.statusCode()));
        attachJson("NewPupils API body", pretty);

        if (resp.statusCode() != 200) {
            throw new RuntimeException("❌ API != 200\n" + pretty);
        }

        // Берём первый объект, ответ может быть объектом или массивом
        org.json.JSONObject obj;
        String isoDateFromApi;
        int finishedQty, nonFinishedQty, avgPercent, pupilsCount;

        try {
            if (raw.trim().startsWith("[")) {
                org.json.JSONArray arr = new org.json.JSONArray(raw);
                if (arr.length() == 0) throw new RuntimeException("Пустой массив в ответе");
                obj = arr.getJSONObject(0);
            } else {
                obj = new org.json.JSONObject(raw);
            }
            isoDateFromApi = obj.getString("date"); // "2024-04-01T00:00:00.000"
            finishedQty = obj.optInt("finishedQuantity", 0);
            nonFinishedQty = obj.optInt("nonFinishedQuantity", 0);
            avgPercent = obj.optInt("averagePercent", 0);
            pupilsCount = obj.optJSONArray("pupils") != null ? obj.getJSONArray("pupils").length() : 0;
        } catch (Exception e) {
            throw new RuntimeException("❌ Не удалось распарсить API ответ: " + e.getMessage() + "\n" + pretty);
        }

        // Ожидания (в UI-формате)
        String expectedDate = formatToUiDate(isoDateFromApi.substring(0, 10)); // yyyy-MM-dd -> dd.MM.yyyy
        String expectedFinished = String.valueOf(finishedQty);
        String expectedNonFinished = String.valueOf(nonFinishedQty);
        String expectedAverage = avgPercent + "%";
        String expectedPupils = String.valueOf(pupilsCount);

        // Значения из UI
        String actualHeaderDate = $("#accordion-title-0").shouldBe(visible).getText();   // содержит дату
        String actualFinished = normDigits($("#finished-label-0").shouldBe(visible).getText());
        String actualNonFinished = normDigits($("#nonfinished-label-0").shouldBe(visible).getText());
        String actualAverage = normPercent($("#average-label-0").shouldBe(visible).getText());

        // Кол-во карточек учеников (индексы подряд: pupil-code-{i}-0)
        int actualPupils = 0;
        for (int i = 0; ; i++) {
            SelenideElement el = $("#pupil-code-" + i + "-0");
            if (!el.exists()) break;
            el.shouldBe(visible);
            actualPupils++;
        }
        String actualPupilsStr = String.valueOf(actualPupils);

        // Сводка + сравнение
        String summary =
                row("Дата (в заголовке содержит)", actualHeaderDate, expectedDate) +
                        row("Завершили", actualFinished, expectedFinished) +
                        row("Не завершили", actualNonFinished, expectedNonFinished) +
                        row("Средний %", actualAverage, expectedAverage) +
                        row("Кол-во учеников", actualPupilsStr, expectedPupils);

        System.out.println("📊 New Pupils (UI vs API)\n" + summary);
        attachText("New Pupils сравнение", summary);

        assert actualHeaderDate.contains(expectedDate) : "❌ Дата в заголовке не содержит ожидаемую\n" + summary + "\nAPI:\n" + pretty;
        assert actualFinished.equals(expectedFinished) : "❌ finishedQuantity\n" + summary + "\nAPI:\n" + pretty;
        assert actualNonFinished.equals(expectedNonFinished) : "❌ nonFinishedQuantity\n" + summary + "\nAPI:\n" + pretty;
        assert actualAverage.equals(expectedAverage) : "❌ averagePercent\n" + summary + "\nAPI:\n" + pretty;
        assert actualPupilsStr.equals(expectedPupils) : "❌ pupils.length\n" + summary + "\nAPI:\n" + pretty;

        System.out.println("✅ Новые ученики: UI совпадает с API");
    }


    private static String toIsoZInstant(String uiDate) {
        // uiDate: "dd.MM.yyyy"
        String[] p = uiDate.split("\\.");
        java.time.LocalDate ld = java.time.LocalDate.of(
                Integer.parseInt(p[2]),
                Integer.parseInt(p[1]),
                Integer.parseInt(p[0])
        );
        java.time.ZonedDateTime localStart = ld.atStartOfDay(java.time.ZoneId.of("Asia/Almaty"));
        java.time.Instant inst = localStart.toInstant(); // перевод в UTC
        // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(java.time.ZoneOffset.UTC)
                .format(inst);
    }


    // Универсальный enum для диапазонов подряд-невыполнений
    public enum ConsecutiveBand {
        TEN_TO_FIFTEEN("around", 10, 15, "10–15"),
        FIFTEEN_PLUS("more", 15, null, "15+");
        // при необходимости можно добавить: ONE_TO_THIRTY("range", 1, 30, "1–30")

        public final String apiType;           // значение для queryParam "type"
        public final Integer minExclusive;     // нижняя граница (исключая)
        public final Integer maxInclusive;     // верхняя граница (включая), null = без верха
        public final String label;

        ConsecutiveBand(String apiType, Integer minExclusive, Integer maxInclusive, String label) {
            this.apiType = apiType;
            this.minExclusive = minExclusive;
            this.maxInclusive = maxInclusive;
            this.label = label;
        }

        public boolean fits(int qty) {
            if (minExclusive != null && !(qty > minExclusive)) return false;
            if (maxInclusive != null && !(qty <= maxInclusive)) return false;
            return true;
        }
    }


    @Step("Сравниваем 'Подряд не выполняли KPI' (10–15 дней) с API: дата={uiDate}")
    public void verifyConsecutiveUnexecutedMatchesApi10to15(String uiDate) {
        verifyConsecutiveUnexecutedMatchesApi(uiDate, ConsecutiveBand.TEN_TO_FIFTEEN, "kaz", "math_course");
    }

    @Step("Сравниваем 'Подряд не выполняли KPI' (15+ дней) с API: дата={uiDate}")
    public void verifyConsecutiveUnexecutedMatchesApi15Plus(String uiDate) {
        verifyConsecutiveUnexecutedMatchesApi(uiDate, ConsecutiveBand.FIFTEEN_PLUS, "kaz", "math_course");
    }

    @Step("Сравниваем 'Подряд не выполняли KPI' {band.label} с API: дата={uiDate}, lang={language}, course={courseType}")
    public void verifyConsecutiveUnexecutedMatchesApi(String uiDate,
                                                      ConsecutiveBand band,
                                                      String language,
                                                      String courseType) {

        String apiDateIsoZ = toIsoZInstant(uiDate);

        Response resp = RestAssured
                .given()
                .header("Authorization", "Bearer " + TokenProvider.getBearerToken())
                .queryParam("date", apiDateIsoZ)
                .queryParam("language", language)
                .queryParam("type", band.apiType)
                .queryParam("courseType", courseType)
                .when()
                .get("/personalStudy/unexecutedKpi");

        String raw = resp.asString();
        String pretty = safePrettyJson(raw);
        attachText("Consecutive " + band.label + " API status", String.valueOf(resp.statusCode()));
        attachJson("Consecutive " + band.label + " API body", pretty);
        if (resp.statusCode() != 200) throw new RuntimeException("❌ API != 200\n" + pretty);

        org.json.JSONObject obj = new org.json.JSONObject(raw);
        int expExecuted   = obj.optInt("consecutiveExecuted", 0);
        int expUnexecuted = obj.optInt("consecutiveUnexecuted", 0);
        int expPercent    = obj.optInt("consecutiveProcent", 0);
        org.json.JSONArray items = obj.optJSONArray("consecutiveItems");
        int expTotal = (items != null) ? items.length() : 0;

        // UI: верхние лейблы (без/с -0)
        SelenideElement executedLbl   = $("#executed-label").exists()   ? $("#executed-label")   : $("#executed-label-0");
        SelenideElement unexecutedLbl = $("#unexecuted-label").exists() ? $("#unexecuted-label") : $("#unexecuted-label-0");
        SelenideElement percentLbl    = $("#percent-label").exists()    ? $("#percent-label")    : $("#percent-label-0");

        executedLbl.shouldBe(visible).shouldHave(text(String.valueOf(expExecuted)));
        unexecutedLbl.shouldBe(visible).shouldHave(text(String.valueOf(expUnexecuted)));
        percentLbl.shouldBe(visible);

        String actExecuted   = normDigits(executedLbl.getText());
        String actUnexecuted = normDigits(unexecutedLbl.getText());
        String actPercent    = normPercent(percentLbl.getText());

        // строки (учитываем возможный аккордеон и разные id)
        int uiRows = countRows();
        if (uiRows == 0 && $("#accordion-title-0").exists()) {
            $("#accordion-title-0").click();
            sleep(500);
            uiRows = countRows();
        }

        String summary =
                row("Диапазон",      band.label,    band.apiType) +
                        row("Выполнили",     actExecuted,   String.valueOf(expExecuted)) +
                        row("Не выполнили",  actUnexecuted, String.valueOf(expUnexecuted)) +
                        row("KPI %",         actPercent,    expPercent + "%") +
                        row("Всего строк",   String.valueOf(uiRows), String.valueOf(expTotal));

        System.out.println("📊 Consecutive " + band.label + " (UI vs API)\n" + summary);
        attachText("Consecutive " + band.label + " сравнение", summary);

        assert actExecuted.equals(String.valueOf(expExecuted))     : "❌ consecutiveExecuted\n" + summary + "\nAPI:\n" + pretty;
        assert actUnexecuted.equals(String.valueOf(expUnexecuted)) : "❌ consecutiveUnexecuted\n" + summary + "\nAPI:\n" + pretty;
        assert actPercent.equals(expPercent + "%")                 : "❌ consecutiveProcent\n" + summary + "\nAPI:\n" + pretty;
        assert uiRows == expTotal                                  : "❌ кол-во строк != consecutiveItems.length\n" + summary + "\nAPI:\n" + pretty;

        for (int i = 0; i < Math.min(uiRows, expTotal); i++) {
            int apiQty = items.getJSONObject(i).optInt("consecutiveUnexecutedQuantity", -1);
            String uiQtyText = textByAnyId("td-unexecuted-qty-0-" + i, "td-unexecuted-qty-" + i);
            assert uiQtyText != null : "❌ не найден qty для строки #" + i + "\nAPI:\n" + pretty;
            int uiQty = Integer.parseInt(normDigits(uiQtyText));

            // сравнение по значению
            assert uiQty == apiQty
                    : "❌ qty row#" + i + " (UI=" + uiQty + ", API=" + apiQty + ")\nAPI:\n" + pretty;

            // проверка соответствия диапазону (например, 10–15: >10 && <=15; 15+: >15)
            assert band.fits(apiQty)
                    : "❌ qty row#" + i + " не входит в диапазон " + band.label + " (qty=" + apiQty + ")\nAPI:\n" + pretty;
        }

        // finished/not-finished по флагу hasFinished
        int apiFinished = 0;
        for (int i = 0; i < expTotal; i++) if (items.getJSONObject(i).optBoolean("hasFinished", false)) apiFinished++;
        int uiFinished     = $$("[id^='finished-label-0-']").size() + $$("[id^='finished-label-']").size();
        int uiNotFinished  = $$("[id^='not-finished-label-0-']").size() + $$("[id^='not-finished-label-']").size();

        assert uiFinished == apiFinished
                : "❌ finished count (UI=" + uiFinished + ", API=" + apiFinished + ")\nAPI:\n" + pretty;
        assert uiNotFinished == (expTotal - apiFinished)
                : "❌ not-finished count (UI=" + uiNotFinished + ", API=" + (expTotal - apiFinished) + ")\nAPI:\n" + pretty;

        System.out.println("✅ Consecutive " + band.label + ": UI совпадает с API");
    }

    private int countRows() {
        // Пытаемся обе схемы id
        int n = $$("[id^='td-userid-0-']").filter(visible).size();
        if (n == 0) n = $$("[id^='td-userid-']").filter(visible).size();
        return n;
    }

    private String textByAnyId(String... ids) {
        for (String id : ids) {
            SelenideElement el = $("#" + id);
            if (el.exists()) {
                return el.shouldBe(visible).getText();
            }
        }
        return null;
    }




}
