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
            nextButton = $(By.id("task-modal-next-btn")),
            answerField = $x("//span[contains(@class,'mq-textarea')]/textarea"),
            answerContainer = $x("//span[contains(@class,'mq-editable-field')]"),
            checkAnswerButton = $(By.id("task-modal-check-answer-btn")),
            showSolutionButton = $(By.id("task-modal-solution-btn")),
            hideCalculatorButton = $(By.id("calculator-icon")),
            videoExplanationButton = $(By.id("task-modal-video-explanation-btn")),
            dotList = $(By.id("task-modal-dot-list")),
            congratulationsToast = $x("//div[contains(@class, 'Toastify__toast-body') and contains(.,'Құттықтаймыз')]");

    private Integer lastDotCount = null;
    private final Random random = new Random(); // это для разной частоты ввода ответа
    private final By livesContainer = By.cssSelector(".task-attempt-list");


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

    @Step("Скрыть калькулятор")
    public PersonalTaskPage hideCalculatorButtonClick() {
        hideCalculatorButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        sleep(1500);
        return this;
    }

    @Step("Подсчет задач и вывод статистики")
    public PersonalTaskPage logTaskSummary() {
        int total = dotList.$$(By.cssSelector(".dot")).size();
        int solved = dotList.$$(By.cssSelector(".dot-success")).size();
        int errors = dotList.$$(By.cssSelector(".dot-error")).size();
        int shown = dotList.$$(By.cssSelector(".dot-show-solution")).size();
        int remaining = total - solved - errors - shown;
        System.out.printf("[TASKS] Всего: %d; Решено: %d; Ошибок: %d; Показов решения: %d; Осталось: %d%n",
                total, solved, errors, shown, remaining);
        lastDotCount = total;
        return this;
    }

    @Step("Проверяем кол-во потраченных жизней: {expected}")
    public PersonalTaskPage verifyLifeCount(int expected) {
        // 1) Находим контейнер жизней по классу
        SelenideElement container = $(livesContainer).shouldBe(visible, Duration.ofSeconds(5));

        // 2) Считаем все элементы .task-attempt-item внутри него
        ElementsCollection lives = container.$$(".task-attempt-item");

        // 3) Отфильтровываем те, у которых есть класс “error” (потраченные)
        int used = lives.filterBy(cssClass("error")).size();

        if (used != expected) {
            throw new AssertionError(
                    String.format("❌ Ожидалось %d потраченных жизней, но найдено %d", expected, used)
            );
        }
        System.out.println("✅ Жизней потрачено: " + used);
        return this;
    }

    @Step("Проверяем, что число точек выросло на: {expectedIncrease}")
    public PersonalTaskPage verifyTaskCount(int expectedIncrease) {
        if (lastDotCount == null) {
            throw new IllegalStateException("Нужно вызвать logTaskSummary() перед verifyTaskCount()");
        }
        int current = dotList.$$(By.cssSelector(".dot")).size();
        int delta = current - lastDotCount;
        if (delta != expectedIncrease) {
            throw new AssertionError(String.format(
                    "❌ Ожидался прирост точек на %d, но выросли на %d (было %d, стало %d)",
                    expectedIncrease, delta, lastDotCount, current));
        }
        System.out.println("✅ Точек добавилось: " + delta + " (с " + lastDotCount + " до " + current + ")");
        lastDotCount = current;
        return this;
    }

    @Step("Получаем ответ с API для userId = {userId}")
    private String fetchAnswerFromApi(String userId) {
        var response = given()
                .queryParam("userId", userId)
                .relaxedHTTPSValidation()
                .when()
                .get("/mlRequest/pupilInfo");

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
            hideCalculatorButtonClick();
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

            clearAnswerField();
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
