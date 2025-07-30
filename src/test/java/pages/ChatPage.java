package pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.UIAssertionError;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.util.Random;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.*;
import static org.junit.jupiter.api.Assertions.fail;

public class ChatPage {

    private final SelenideDriver driver;

    // Данные сайта / логины
    private static final String BASE_URL = "https://test.qalan.kz";
    private static final String STUDENT_PHONE = "7083544313";
    private static final String STUDENT_PASSWORD = "12345678";
    private static final String MENTOR_PHONE = "7022738232";
    private static final String MENTOR_PASSWORD = "87654321";

    // Элементы
    private final SelenideElement modal;
    private final SelenideElement signButton;
    private final SelenideElement phoneInput;
    private final SelenideElement passwordInput;
    private final SelenideElement submitButton;
    private final SelenideElement titleText;
    private final SelenideElement studentChatButton;
    private final SelenideElement mentorSessionsButton;
    private final SelenideElement mentorChatMessageListBtn;
    private final SelenideElement messageInput;
    private final SelenideElement refreshButton;

    // Список сообщений
    private final By messageItems = By.cssSelector("div.message-item");

    public ChatPage(SelenideDriver driver) {
        this.driver = driver;

        // Инициализация элементов через driver
        modal = driver.$(By.id("add-survey-modal-body"));
        signButton = driver.$(By.id("sign-button"));
        phoneInput = modal.$(By.id("phone-input-id"));
        passwordInput = modal.$(By.id("password-input"));
        submitButton = driver.$(By.id("login-button"));
        titleText = driver.$(By.id("user-fullname-container"));
        studentChatButton = driver.$(By.id("mobile-chat-button"));
        mentorSessionsButton = driver.$(By.id("group-button-mentor-sessions"));
        mentorChatMessageListBtn = driver.$(By.id("chat-message-list-button"));
        messageInput = driver.$(By.id("message-input-box"));
        refreshButton = driver.$(By.id("load-message-history-button"));
    }

    // --------------------------------------
    // Авторизация
    // --------------------------------------
    @Step("Открываем сайт и нажимаем 'Войти'")
    private ChatPage openAndClickSign() {
        driver.open(BASE_URL);
        signButton.shouldBe(visible).click();
        return this;
    }

    @Step("Вводим телефон/пароль, нажимаем 'Вход'")
    private ChatPage doLogin(String phone, String password) {
        phoneInput.shouldBe(visible).setValue(phone);
        passwordInput.shouldBe(visible).setValue(password);
        submitButton.shouldBe(enabled).click();
        // Логирование ФИО
        titleText.shouldBe(visible);
        System.out.println("Авторизовался: " + titleText.getText());
        return this;
    }

    @Step("Логин как Ученик")
    public ChatPage loginAsStudent() {
        openAndClickSign();
        doLogin(STUDENT_PHONE, STUDENT_PASSWORD);
        return this;
    }

    @Step("Логин как Ментор")
    public ChatPage loginAsMentor() {
        openAndClickSign();
        doLogin(MENTOR_PHONE, MENTOR_PASSWORD);
        return this;
    }

    // --------------------------------------
    // Открытие чата
    // --------------------------------------
    @Step("Ученик: переходим в чат")
    public ChatPage openChatAsStudent() {
        studentChatButton.shouldBe(visible).click();
        messageInput.shouldBe(visible);
        return this;
    }

    @Step("Ментор: переходим в чат Через ПО2")
    public ChatPage openChatAsMentor() {
        // 1) Ввести номер ученика в поле "search-database-input"
        driver.$(By.id("search-database-input"))
                .shouldBe(visible)
                .setValue("7083544313");

        // 2) Нажать кнопку поиска "search-database-button"
        driver.$(By.id("search-database-button"))
                .shouldBe(visible)
                .click();

        // 3) Нажать на иконку чата "chat-message-list-button"
        driver.$(By.id("chat-message-list-button"))
                .shouldBe(visible)
                .click();

        // Проверить, что появилось поле для ввода сообщений
        messageInput.shouldBe(visible);

        // Возвращаем this для chain-стиля
        return this;
    }

    @Step("Ментор: переходим в чат через ПО3")
    public ChatPage openChatFromSessionsAsMentor() {
        // сначала открыли список сессий
        mentorSessionsButton
                .shouldBe(visible, Duration.ofSeconds(5))
                .click();

        // теперь — кнопка перехода в чат
        try {
            mentorChatMessageListBtn
                    .shouldBe(visible, Duration.ofSeconds(5))
                    .click();
        } catch (UIAssertionError e) {
            // если кнопки нет — падаем сразу с понятным текстом
            System.out.println("❌ Кнопка перехода в чат не найдена — нужно настроить сессию в базе для ментора");
            throw new AssertionError(
                    "❌ Кнопка перехода в чат не найдена — " +
                            "нужно настроить сессию в базе для ментора", e);
        }

        // проверяем, что поле ввода реально появилось
        messageInput
                .shouldBe(visible, Duration.ofSeconds(5));

        return this;
    }

    // --------------------------------------
    // Отправка / Обновление
    // --------------------------------------
    @Step("Отправляем сообщение {msg} (ENTER)")
    public ChatPage sendMessage(String msg) {
        messageInput.shouldBe(visible)
                .setValue(msg)
                .sendKeys(Keys.ENTER);
        System.out.println("[LOG] Сообщение отправлено: " + msg);
        return this;
    }

    @Step("Нажимаем 'Обновить'")
    public ChatPage refreshChat() {
        refreshButton.shouldBe(visible)
                .shouldBe(enabled, Duration.ofSeconds(120))
                .click();
        return this;
    }

    @Step("Ждем сообщение (RegExp): {expectedText}")
    public ChatPage waitForMessage(String expectedText) {
        driver.$$(messageItems)
                .findBy(matchText(".*" + Pattern.quote(expectedText) + ".*"))
                .shouldBe(visible, Duration.ofSeconds(7));
        System.out.println("[LOG] Сообщение прочитано: " + expectedText);
        return this;
    }

    // --------------------------------------
    // Проверка дублей
    // --------------------------------------
    @Step("Проверяем, что нет дублей: {expectedText}")
    public ChatPage checkNoDuplicateMessage(String expectedText) {
        ElementsCollection found = driver.$$(messageItems).filterBy(text(expectedText));
        if (found.size() > 1) {
            fail("Найден дубль: " + expectedText);
        }
        return this;
    }

    // --------------------------------------
    // Логируем последнее сообщение
    // --------------------------------------
    @Step("Логируем последнее сообщение")
    public ChatPage logLastMessage() {
        String text = driver.$$(messageItems).last().shouldBe(visible).getText();
        System.out.println("[LOG] Последнее сообщение: " + text);
        return this;
    }

    // --------------------------------------
    // Генерация уникального сообщения
    // --------------------------------------
    @Step("Генерируем уникальное сообщение")
    public String generateRandomMessage(String prefix) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return prefix + sb.toString();
    }

    // --------------------------------------
    // Закрытие браузера
    // --------------------------------------
    @Step("Закрываем браузер (SelenideDriver)")
    public ChatPage closeDriver() {
        driver.close();  // или driver.getWebDriver().quit();
        return this;
    }
}
