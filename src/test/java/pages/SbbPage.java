package pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Keys;
import testData.TokenProvider;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;

public class SbbPage {

    private final SelenideElement
            sbbSectionButton = $("#tab-section-functionals-label"),
            surveyButton     = $("#tab-functionals-questions-to-parent"),
            companyDropdown  = $x("//div[contains(@class,'Select-placeholder') and text()='Компания']"),
            viewButton       = $("#view-btn"),
            parentCountLabel = $("#parent-count");

    private final ElementsCollection surveyDateInputs = $$("input.date-time-picker");


    private SelenideElement fromDateInput() {
        SelenideElement byCss = $("input.date-time-picker:not(#date-picker)");
        if (byCss.exists()) return byCss;
        return surveyDateInputs.first();
    }

    private SelenideElement toDateInput() {
        SelenideElement byId = $("#date-picker");
        if (byId.exists()) return byId;
        return surveyDateInputs.last();
    }


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

    @Step("Вводим период опроса: с {from} по {to}")
    public SbbPage enterSurveyDateRange(String from, String to) {
        typeDate(fromDateInput(), from);
        typeDate(toDateInput(), to);
        return this;
    }

    @Step("Вводим дату 'От': {from}")
    public SbbPage enterSurveyDateFrom(String from) {
        typeDate(fromDateInput(), from);
        return this;
    }

    @Step("Вводим дату 'До': {to}")
    public SbbPage enterSurveyDateTo(String to) {
        typeDate(toDateInput(), to);
        return this;
    }

    @Step("Выбираем дату через календарь: {date}")
    public SbbPage enterSurveyDate(String date) {
        typeDate(fromDateInput(), date);
        return this;
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

    // Локальная проверка без API
    @Step("Проверяем, что отображается {expectedCount} родителей (без API)")
    public SbbPage verifyParentCount(int expectedCount) {
        parentCountLabel.shouldBe(Condition.visible, Duration.ofSeconds(10))
                .shouldHave(Condition.text(String.valueOf(expectedCount)));
        return this;
    }

    // сравнение ui/api
    public enum ParentCountExpectation {
        AUTO,                    // попытаться сопоставить одно из ожидаемых
        UNIQUE_PARENTS,          // все уникальные родители
        UNIQUE_PARENTS_FINISHED, // только те, у кого isFinished=true
        ROWS                     // число строк из API
    }

    @Step("Сравниваем счётчик родителей на UI с API (AUTO): company={company}, {from} → {to}")
    public SbbPage verifyParentCountMatchesApi(String from, String to, String company) {
        return verifyParentCountMatchesApi(from, to, company, 25, false, ParentCountExpectation.AUTO);
    }

    @Step("Сравниваем счётчик родителей на UI с API: company={company}, {from} → {to}, filter={filter}, employeesOnly={isEmployeeOnly}")
    public SbbPage verifyParentCountMatchesApi(String from,
                                               String to,
                                               String company,
                                               int filter,
                                               boolean isEmployeeOnly) {
        return verifyParentCountMatchesApi(from, to, company, filter, isEmployeeOnly, ParentCountExpectation.AUTO);
    }

    @Step("Сравниваем счётчик родителей на UI с API: company={company}, {from} → {to}, filter={filter}, employeesOnly={isEmployeeOnly}, mode={mode}")
    public SbbPage verifyParentCountMatchesApi(String from,
                                               String to,
                                               String company,
                                               int filter,
                                               boolean isEmployeeOnly,
                                               ParentCountExpectation mode) {

        parentCountLabel.shouldBe(Condition.visible, Duration.ofSeconds(10));

        // API сначала — считаем все варианты ожиданий
        String apiFrom = toApiDate(from);
        String apiTo   = toApiDate(to);

        Response resp = RestAssured
                .given()
                .header("Authorization", "Bearer " + TokenProvider.getBearerToken())
                .queryParam("company", company)
                .queryParam("filter", filter)
                .queryParam("filterDateFrom", apiFrom)
                .queryParam("filterDateTo", apiTo)
                .queryParam("isEmployeeOnly", isEmployeeOnly)
                .when()
                .get("/parentsByFilter");

        String raw = resp.asString();
        String pretty = safePrettyJson(raw);
        attachText("ParentsByFilter status", String.valueOf(resp.statusCode()));
        attachJson("ParentsByFilter body", pretty);
        if (resp.statusCode() != 200) {
            throw new RuntimeException(" API != 200\n" + pretty);
        }

        JSONArray data = extractArray(raw);

        Set<String> uniqAll = new HashSet<>();
        Set<String> uniqFinished = new HashSet<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject r = data.getJSONObject(i);
            String key = parentKey(r);
            uniqAll.add(key);
            if (r.optBoolean("isFinished", false)) {
                uniqFinished.add(key);
            }
        }
        int apiRows = data.length();
        int apiUniqueParents = uniqAll.size();
        int apiUniqueParentsFinished = uniqFinished.size();

        //  ждём, пока UI подставит ОДНО из ожидаемых (или конкретный режим)
        int uiCount = waitUiCountToMatch(
                mode,
                apiUniqueParentsFinished,
                apiUniqueParents,
                apiRows,
                10_000,
                200
        );

        // выбираем эталон для сравнения
        int expected;
        String expectedLabel;
        switch (mode) {
            case UNIQUE_PARENTS:
                expected = apiUniqueParents;
                expectedLabel = "unique parents (ALL)";
                break;
            case UNIQUE_PARENTS_FINISHED:
                expected = apiUniqueParentsFinished;
                expectedLabel = "unique parents (FINISHED)";
                break;
            case ROWS:
                expected = apiRows;
                expectedLabel = "rows";
                break;
            case AUTO:
            default:
                if (uiCount == apiUniqueParentsFinished) {
                    expected = apiUniqueParentsFinished;
                    expectedLabel = "unique parents (FINISHED) [AUTO]";
                } else if (uiCount == apiUniqueParents) {
                    expected = apiUniqueParents;
                    expectedLabel = "unique parents (ALL) [AUTO]";
                } else if (uiCount == apiRows) {
                    expected = apiRows;
                    expectedLabel = "rows [AUTO]";
                } else {
                    expected = apiUniqueParentsFinished;
                    expectedLabel = "unique parents (FINISHED) [AUTO—no match]";
                }
                break;
        }

        String summary = ""
                + "UI parents: " + uiCount + "\n"
                + "API rows: " + apiRows + "\n"
                + "API unique parents (ALL): " + apiUniqueParents + "\n"
                + "API unique parents (FINISHED): " + apiUniqueParentsFinished + "\n"
                + "Chosen metric: " + expectedLabel + "\n"
                + "filters: company=" + company + ", filter=" + filter
                + ", from=" + apiFrom + ", to=" + apiTo
                + ", isEmployeeOnly=" + isEmployeeOnly + "\n";
        System.out.println("📊 Parents (UI vs API)\n" + summary);
        attachText("Parents (UI vs API) summary", summary);

        assert uiCount == expected
                : " UI count != " + expectedLabel + "\n" + summary + "\nAPI:\n" + pretty;

        System.out.println("UI count matches " + expectedLabel + ".");
        return this;
    }

    // helpers

    // Ждём, пока текст #parent-count стабилизируется и совпадёт с одним из ожидаемых значений
    private int waitUiCountToMatch(ParentCountExpectation mode,
                                   int expFinishedUnique,
                                   int expAllUnique,
                                   int expRows,
                                   long timeoutMs,
                                   long stepMs) {
        long start = System.currentTimeMillis();
        int last = -1, stableTicks = 0;

        while (System.currentTimeMillis() - start < timeoutMs) {
            String txt = parentCountLabel.getText();
            int val = extractLastInt(txt);

            // считаем "стабильность"
            if (val == last) stableTicks++; else stableTicks = 0;
            last = val;

            boolean matches;
            switch (mode) {
                case UNIQUE_PARENTS:
                    matches = (val == expAllUnique);
                    break;
                case UNIQUE_PARENTS_FINISHED:
                    matches = (val == expFinishedUnique);
                    break;
                case ROWS:
                    matches = (val == expRows);
                    break;
                case AUTO:
                default:
                    matches = (val == expFinishedUnique) || (val == expAllUnique) || (val == expRows);
                    break;
            }

            if (matches && stableTicks >= 2) {
                return val;
            }

            sleep(stepMs);
        }

        // не дождались — возвращаем последнее увиденное
        return last < 0 ? 0 : last;
    }

    //Берём ПОСЛЕДНЕЕ число в строке (часто справа)
    private static int extractLastInt(String text) {
        if (text == null) return 0;
        // заменим не-цифры на пробелы и возьмём последнее "слово"
        String onlyDigitsAndSpaces = text.replaceAll("[^\\d]", " ").trim();
        if (onlyDigitsAndSpaces.isEmpty()) return 0;
        String[] chunks = onlyDigitsAndSpaces.split("\\s+");
        String last = chunks[chunks.length - 1];
        try { return Integer.parseInt(last); } catch (Exception e) { return 0; }
    }

    // ввод даты
    private void typeDate(SelenideElement input, String date) {
        input.shouldBe(Condition.visible, Duration.ofSeconds(10)).click();
        clearInputHard(input);

        input.setValue(date);
        input.pressEnter();
        if (date.equals(input.getValue())) return;

        clearInputHard(input);
        for (char c : date.toCharArray()) input.sendKeys(String.valueOf(c));
        input.pressEnter();
        if (date.equals(input.getValue())) return;

        executeJavaScript(
                "arguments[0].value = arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
                        "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                input, date
        );
        input.pressEnter();
        input.shouldHave(Condition.value(date));
    }

    private void clearInputHard(SelenideElement input) {
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.chord(Keys.COMMAND, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(Keys.HOME);
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.END));
        input.sendKeys(Keys.DELETE);
    }

    private static String toApiDate(String ddmmyyyy) {
        String[] p = ddmmyyyy.split("\\.");
        return p[2] + "-" + p[1] + "-" + p[0];
    }

    private static String parentKey(JSONObject r) {
        String phone = r.optString("parentPhoneNumber", "").replaceAll("\\s+", "");
        if (!phone.isEmpty()) return "phone:" + phone;

        int userId = r.optInt("userId", -1);
        if (userId > 0) return "user:" + userId;

        String name = r.optString("parentFullname", "").trim().toLowerCase();
        if (!name.isEmpty()) return "name:" + name;

        int id = r.optInt("id", -1);
        if (id > 0) return "id:" + id;

        return "hash:" + r.toString().hashCode();
    }

    private static JSONArray extractArray(String raw) {
        try {
            String t = raw.trim();
            if (t.startsWith("[")) return new JSONArray(t);
            JSONObject obj = new JSONObject(t);
            if (obj.has("preview")) return obj.getJSONArray("preview");
            if (obj.has("data") && obj.get("data") instanceof JSONArray) return obj.getJSONArray("data");
            return new JSONArray();
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private static String safePrettyJson(String body) {
        try {
            String t = body.trim();
            if (t.startsWith("[")) return new JSONArray(t).toString(2);
            return new JSONObject(t).toString(2);
        } catch (Exception e) {
            return body;
        }
    }

    @Attachment(value = "{name}", type = "text/plain")
    private static String attachText(String name, String content) { return content; }

    @Attachment(value = "{name}", type = "application/json")
    private static String attachJson(String name, String content) { return content; }
}
