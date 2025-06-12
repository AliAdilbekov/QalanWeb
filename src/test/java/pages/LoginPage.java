package pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class LoginPage {

    private final String baseUrl = "https://test.qalan.kz";

    private final SelenideElement
            modal = $(By.id("add-survey-modal-body")),
            signButton = $(By.id("sign-button")),
            phoneNumber = modal.$(By.id("phone-input-id")),
            password = modal.$(By.id("password-input")),
            submitButton = $(By.id("login-button")),
            errorMessage = $(By.id("error-message")),
            titleText = $(By.id("user-fullname-container"));

    @Step("Открываем главную страницу")
    public LoginPage openLoginPage() {
        open(baseUrl);
        return this;
    }

    @Step("Нажать на кнопку Войти")
    public LoginPage clickSignButton() {
        signButton.shouldBe(visible).click();
        return this;
    }

    @Step("Вводим телефон {phone}")
    public LoginPage enterPhoneNumber(String phone) {
        phoneNumber.shouldBe(visible).setValue(phone);
        return this;
    }

    @Step("Вводим пароль {pass}")
    public LoginPage enterPassword(String pass) {
        password.shouldBe(visible).setValue(pass);
        return this;
    }

    @Step("Нажимаем кнопку Вход")
    public LoginPage clickSubmitButton() {
        submitButton.shouldBe(visible).click();
        return this;
    }

    @Step("Проверяем успешный логин, ожидаем имя пользователя: {expectedName}")
    public void verifySuccessfulLogin(String expectedName) {
        titleText.shouldBe(visible).shouldHave(text(expectedName));
        String actualText = titleText.getText();
        System.out.println(actualText);
    }

    @Step("Проверяем ошибку: {expectedError}")
    public void verifyErrorMessage(String expectedError) {
        errorMessage.shouldBe(visible).shouldHave(text(expectedError));
        String actualText = errorMessage.getText();
        System.out.println(actualText);
    }

    @Step("Выполняем успешную авторизацию")
    public void successLogin(String phone, String pass, String expectedName) {
        openLoginPage()
                .clickSignButton()
                .enterPhoneNumber(phone)
                .enterPassword(pass)
                .clickSubmitButton()
                .verifySuccessfulLogin(expectedName);
    }

    @Step("Авторизация с некорректным телефоном")
    public void failPhoneLogin(String phone, String pass, String expectedError) {
        openLoginPage()
                .clickSignButton()
                .enterPhoneNumber(phone)
                .enterPassword(pass)
                .clickSubmitButton()
                .verifyErrorMessage(expectedError);
    }

    @Step("Авторизация с некоррекным паролем")
    public void failPasswordLogin(String phone, String pass, String expectedError) {
        openLoginPage()
                .clickSignButton()
                .enterPhoneNumber(phone)
                .enterPassword(pass)
                .clickSubmitButton()
                .verifyErrorMessage(expectedError);
    }
}


//fdgdf
//lm