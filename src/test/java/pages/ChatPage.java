package pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.UIAssertionError;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import java.time.Duration;
import java.util.Random;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.*;

public class ChatPage {

    private final SelenideDriver driver;

    // Данные сайта / логины
    private static final String STUDENT_PHONE = "7083544313";
    private static final String STUDENT_PASSWORD = "12345678";
    private static final String MENTOR_PHONE  = "7022738232";
    private static final String MENTOR_PASSWORD = "87654321";

    // Локаторы
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

    private final By messageItems = By.cssSelector("div.message-item");

    public ChatPage(SelenideDriver driver) {
        this.driver = driver;

        // ИНИЦИАЛИЗАЦИЯ ТОЛЬКО ЧЕРЕЗ driver
        modal                  = driver.$(By.id("add-survey-modal-body"));
        signButton             = driver.$(By.id("sign-button"));
        phoneInput             = modal.$(By.id("phone-input-id"));
        passwordInput          = modal.$(By.id("password-input"));
        submitButton           = driver.$(By.id("login-button"));
        titleText              = driver.$(By.id("user-fullname-container"));
        studentChatButton      = driver.$(By.id("mobile-chat-button"));
        mentorSessionsButton   = driver.$(By.id("group-button-mentor-sessions"));
        mentorChatMessageListBtn = driver.$(By.id("chat-message-list-button"));
        messageInput           = driver.$(By.id("chat-message-input"));
        // refreshButton НЕ КЭШИРУЕМ — ищем по месту в методе
    }

    // --------------------------------------
    // Авторизация
    // --------------------------------------
    @Step("Открываем сайт и нажимаем 'Войти'")
    private void openAndClickSign() {
        driver.open("/"); // только через driver
        signButton.shouldBe(visible).click();
    }

    @Step("Вводим телефон/пароль, нажимаем 'Вход'")
    private void doLogin(String phone, String password) {
        phoneInput.shouldBe(visible).setValue(phone);
        passwordInput.shouldBe(visible).setValue(password);
        submitButton.shouldBe(enabled).click();

        titleText.shouldBe(visible);
        System.out.println("Авторизовался: " + titleText.getText());
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
        driver.$(By.id("search-database-input"))
                .shouldBe(visible)
                .setValue(STUDENT_PHONE);

        driver.$(By.id("search-database-button"))
                .shouldBe(visible)
                .click();

        driver.$(By.id("chat-message-list-button"))
                .shouldBe(visible)
                .click();

        messageInput.shouldBe(visible);
        return this;
    }

    @Step("Ментор: переходим в чат через ПО3")
    public ChatPage openChatFromSessionsAsMentor() {
        mentorSessionsButton.shouldBe(visible, Duration.ofSeconds(5)).click();

        try {
            mentorChatMessageListBtn
                    .shouldBe(visible, Duration.ofSeconds(5))
                    .click();
        } catch (UIAssertionError e) {
            System.out.println("❌ Кнопка перехода в чат не найдена — нужно настроить сессию в базе для ментора");
            throw new AssertionError("❌ Кнопка перехода в чат не найдена — нужно настроить сессию в базе для ментора", e);
        }

        messageInput.shouldBe(visible, Duration.ofSeconds(5));
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

    @Step("Нажимаем 'Алдыңғы хабарламалар'")
    public ChatPage refreshChat() {
        // Ищем кнопку каждый раз — так устойчивее
        SelenideElement refreshButton = driver.$$(By.tagName("button"))
                .findBy(text("Алдыңғы хабарламалар"));
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
        found.shouldHave(CollectionCondition.sizeLessThan(2));
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
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return prefix + sb;
    }

    // --------------------------------------
    // Закрытие браузера
    // --------------------------------------
    @Step("Закрываем браузер (SelenideDriver)")
    public ChatPage closeDriver() {
        driver.close(); // закрываем именно этот driver, а не глобальный
        return this;
    }
}
