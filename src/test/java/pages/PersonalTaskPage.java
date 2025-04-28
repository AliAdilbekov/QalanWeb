package pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.util.Random;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;

public class PersonalTaskPage {

    private final SelenideElement
            personalStudyStartButton = $(By.id("start-new-table-button")),
            personalStudyContinueButton = $(By.id("continue-last-table-button")),
            nextButton = $(By.id("next-task-button")),
            answerField = $x("//span[contains(@class,'mq-textarea')]/textarea"),
            answerContainer = $x("//span[contains(@class,'mq-editable-field')]"),
            checkAnswerButton = $(By.id("check-answer-button")),
            showSolutionButton = $(By.id("show-solution-button")),
            videoExplanationButton = $(By.id("video-explanation-button")),
            dotList = $(By.id("task-modal-dot-list")),
            congratulationsToast = $x("//div[contains(@class, 'Toastify__toast-body') and contains(.,'Құттықтаймыз')]");

    private final Random random = new Random(); // это для разной частоты ввода ответа
    private final By dotsContainer = By.id("task-modal-dot-list");
    private final By livesContainer = By.id("task-modal-task-attempts");

    @Step("Логинимся как ученик: {phone}")
    public PersonalTaskPage loginAsStudent(String phone, String password, String expectedUsername) {
        new LoginPage()
                .openLoginPage()
                .clickSignButton()
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .clickSubmitButton()
                .verifySuccessfulLogin(expectedUsername);
        sleep(2000);
        return this;
    }

    @Step("Начать новое персональное обучение")
    public PersonalTaskPage startButtonClick() {
        personalStudyStartButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

    @Step("Продолжить персональное обучение")
    public void continueButtonClick() {
        personalStudyContinueButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
    }

    @Step("Нажать на кнопку следующее")
    public PersonalTaskPage nextButtonClick() {
        nextButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

    @Step("Нажать на кнопку показать ответ")
    public PersonalTaskPage showSolutionButtonClick() {
        showSolutionButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

    @Step("Нажать на кнопку видео-объяснение")
    public PersonalTaskPage videoExplanationButtonClick() {
        videoExplanationButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(2000);
        return this;
    }

    @Step("Подсчет задач и вывод статистики")
    public PersonalTaskPage logTaskSummary() {
        // все точки
        ElementsCollection dots = dotList.$$(By.cssSelector(".dot"));
        int total = dots.size();
        // уже успешно решённые
        int solved = dots.filterBy(cssClass("dot-success")).size();
        // те, где были ошибки (жизнь потрачена)
        int errors = dots.filterBy(cssClass("dot-error")).size();
        // остаток — без решённых и без тех, где уже была ошибка
        int remaining = total - solved - errors;

        System.out.printf("[TASKS] Всего: %d; Решено: %d; Ошибок: %d; Осталось: %d%n",
                total, solved, errors, remaining);
        return this;
    }

    @Step("Проверяем кол-во потраченных жизней: {expected}")
    public PersonalTaskPage verifyLifeCount(int expected) {
        ElementsCollection lives = $$(livesContainer).get(0)
                .$$(By.cssSelector(".task-attempt-item"));
        int used = lives.filterBy(cssClass("error")).size();
        if (used != expected) {
            throw new AssertionError(
                    String.format("❌ Ожидалось %d потраченных жизней, но найдено %d", expected, used));
        }
        System.out.println("✅ Жизней потрачено: " + used);
        return this;
    }

    @Step("Проверяем кол-во задач: {expected}")
    public PersonalTaskPage verifyTaskCount(int expected) {
        ElementsCollection dots = $$(dotsContainer).get(0)
                .$$(By.cssSelector(".dot"));
        int total = dots.size();
        if (total != expected) {
            throw new AssertionError(
                    String.format("❌ Ожидалось %d точек, но их %d", expected, total));
        }
        System.out.println("✅ Точек всего: " + total);
        return this;
    }

    @Step("Получаем ответ с API для userId = {userId}")
    private String fetchAnswerFromApi(String userId) {
        var response = given()
                .baseUri("https://test.qalan.kz")
                .basePath("/api/mlRequest/pupilInfo")
                .queryParam("userId", userId)
                .relaxedHTTPSValidation()
                .when()
                .get();

        int statusCode = response.getStatusCode();

        if (statusCode != 200) {
            String responseBody = response.getBody().asPrettyString(); // Только если ошибка
            throw new AssertionError("❌ Ошибка при запросе ответа с API!\n" +
                    "Статус код: " + statusCode + "\n" +
                    "Тело ответа: " + responseBody);
        }
        String rawAnswer = response.jsonPath().getString("taskSolution.answer");
        return cleanAnswer(rawAnswer);
    }

    private String cleanAnswer(String rawAnswer) {
        if (rawAnswer == null) return "";
        return rawAnswer.replaceAll("[\\\\()]", "");
    }

    @Step("Вводим в поле значение «{value}»")
    private void enterValue(String value) {
        answerContainer.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(500);
        for (char ch : value.toCharArray()) {
            answerField.sendKeys(String.valueOf(ch));
            sleep(random.nextInt(100) + 80);
        }
    }

    @Step("Очищаем поле через множественный BACKSPACE")
    public PersonalTaskPage clearAnswerField() {
        answerContainer.shouldBe(visible, Duration.ofSeconds(10)).click();
        answerField.sendKeys(Keys.END); // курсор в конец
        sleep(200);
        // Жмем BACKSPACE
        for (int i = 0; i < 20; i++) {
            answerField.sendKeys(Keys.BACK_SPACE);
            sleep(80);
        }
        return this;
    }

    @Step("Решаем задачи, пока не появится попап 'Құттықтаймыз'")
    public void solveTasksUntilCongrats(String userId) {
        int taskCounter = 1;

        while (true) {
            // Проверяем тост перед каждой задачей
            if (congratulationsToast.exists()) {
                System.out.println(" Обнаружен попап 'Құттықтаймыз' перед задачей #" + taskCounter);
                break;
            }

            String answer = fetchAnswerFromApi(userId);
            System.out.println("Ответ для задачи #" + taskCounter + ": " + answer);

            enterValue(answer);

            checkAnswerButton.shouldBe(enabled, Duration.ofSeconds(10)).click();
            sleep(5000);

            // Проверка тоста после каждой отправки
            if (congratulationsToast.exists()) {
                System.out.println(" УСПЕШНО! Появился попап 'Құттықтаймыз' после задачи #" + taskCounter);
                break;
            }

            taskCounter++;
        }

    }

    @Step("Проверяем необходимость продолжения обучения и начинаем новый флоу")
    public PersonalTaskPage checkPersonalStudyFlow(String userId) {
        if (personalStudyContinueButton.isDisplayed()) {
            System.out.println("🔁 Продолжение обучения найдено. Завершаем старое обучение...");
            continueButtonClick();
            logTaskSummary();
            solveTasksUntilCongrats(userId);
            sleep(1000);
            personalStudyStartButton.shouldBe(visible, Duration.ofSeconds(15));
            System.out.println("✅ Обучение завершено. Можно начинать новое.");
        } else if (personalStudyStartButton.isDisplayed()) {
            System.out.println("🟢 Обучение ещё не начиналось. Готов к новому флоу (основной флоу).");
        } else {
            throw new AssertionError("❌ Кнопки 'Начать' или 'Продолжить' не найдены. Что-то не так с UI.");
        }

        return this;
    }

    @Step("Делаем {errors} ошибочных попыток")
    public PersonalTaskPage makeErrors(int errors, String userId) {
        String correct = fetchAnswerFromApi(userId);
        for (int i = 1; i <= errors; i++) {
            // генерим «неправильный» ответ 11–19
            String wrong = String.valueOf(11 + random.nextInt(9));
            System.out.printf("[ERROR #%d] вместо \"%s\" ввели \"%s\"%n", i, correct, wrong);

            enterValue(wrong);
            checkAnswerButton.shouldBe(enabled).click();
            sleep(1000);
        }
        return this;
    }

    @Step("Проверяем, что в iframe появился YouTube плеер")
    public PersonalTaskPage checkVideoPlayer() {
        SelenideElement iframe = $("iframe[src*='youtube.com']");
        iframe.shouldBe(visible, Duration.ofSeconds(10));
        executeJavaScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", iframe);
        switchTo().frame(iframe);

        SelenideElement playButton = $(".ytp-large-play-button");
        playButton.shouldBe(visible, Duration.ofSeconds(10));
        System.out.println("✅ YouTube плеер найден в iframe");

        switchTo().defaultContent();
        return this;
    }

}
