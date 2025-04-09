package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class RegistrationPage {

    // 🔗 URL сайта
    private static final String BASE_URL = "https://test.qalan.kz";

    private final SelenideElement modal = $(By.id("add-survey-modal-body"));

    private final SelenideElement
            signButton = $(By.id("sign-button")),
            registerSwitch = $(By.id("add-survey-modal-switch-register")),
            nameInput = modal.$(By.id("register-name-input")),
            surnameInput = modal.$(By.id("register-surname-input")),
            phoneInput = modal.$(By.id("phone-input-id")),
            passwordInput = modal.$(By.id("register-password-input")),
            repeatPasswordInput = modal.$(By.id("register-repeat-password-input")),
            pupilRadioButton = modal.$(By.id("register-radio-pupil")),
            submitButton = modal.$(By.id("register-submit-button")),
            userFullNameContainer = $(By.id("user-fullname-container"));

    @Step("Открываем сайт и переходим к форме регистрации")
    public RegistrationPage openRegistrationPage() {
        Selenide.open(BASE_URL);
        signButton.shouldBe(visible).click();
        registerSwitch.shouldBe(visible).click();
        return this;
    }

    @Step("Вводим имя: {name}")
    public RegistrationPage enterFirstName(String name) {
        nameInput.shouldBe(visible).setValue(name);
        return this;
    }

    @Step("Вводим фамилию: {surname}")
    public RegistrationPage enterLastName(String surname) {
        surnameInput.shouldBe(visible).setValue(surname);
        return this;
    }

    @Step("Вводим номер телефона: {phone}")
    public RegistrationPage enterPhoneNumber(String phone) {
        phoneInput.shouldBe(visible).setValue(phone);
        return this;
    }

    @Step("Вводим пароль: {password}")
    public RegistrationPage enterPassword(String password) {
        passwordInput.shouldBe(visible).setValue(password);
        return this;
    }

    @Step("Повторно вводим пароль: {password}")
    public RegistrationPage enterRepeatPassword(String password) {
        repeatPasswordInput.shouldBe(visible).setValue(password);
        return this;
    }

    @Step("Выбираем роль ученика")
    public RegistrationPage selectPupilRole() {
        pupilRadioButton.shouldBe(visible).click();
        return this;
    }

    @Step("Прокручиваем модальное окно до кнопки регистрации")
    public RegistrationPage scrollToSubmitButton() {
        Selenide.executeJavaScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", submitButton.getWrappedElement());
        Selenide.sleep(1500);
        submitButton.shouldBe(visible);
        return this;
    }

    @Step("Нажимаем на кнопку 'Зарегистрироваться'")
    public RegistrationPage clickSubmitButton() {
        submitButton.shouldBe(visible).click();
        return this;
    }

    @Step("Проверяем имя пользователя после регистрации: {expectedName}")
    public RegistrationPage verifyUserFullName(String expectedName) {
        userFullNameContainer.shouldBe(visible, Duration.ofSeconds(15)).shouldHave(text(expectedName));
        return this;
    }

    @Step("Выводим сгенерированные данные")
    public RegistrationPage printGeneratedData(String name, String surname, String phone, String password, String repeatPassword) {
        System.out.println("👤 Имя: " + name);
        System.out.println("👤 Фамилия: " + surname);
        System.out.println("📞 Сгенерированный номер: " + phone);
        System.out.println("🔑 Сгенерированный пароль: " + password);
        System.out.println("⚠️ Сгенерированный повторный пароль: " + repeatPassword);
        return this;
    }

    @Step("🧪 Проверяем корректность отображаемых имени и фамилии")
    public void verifyNameSurnameInConsole(String expectedName, String expectedSurname) {
        String actualNameSurname = userFullNameContainer.getText();
        String expectedFullName = expectedName + " " + expectedSurname;

        if (actualNameSurname.equals(expectedFullName)) {
            System.out.println("✅ Успешная регистрация: " + actualNameSurname);
        } else {
            System.out.println("❌ Несоответствие: ожидалось " + expectedFullName + ", а получили " + actualNameSurname);
        }
    }

    @Step("Проверяем текст ошибки: {expectedError}")
    public RegistrationPage verifyErrorMessage(String expectedError) {
        SelenideElement errorMessage = $(By.id("register-error-text"));
        errorMessage.shouldBe(visible, Duration.ofSeconds(10)).shouldHave(text(expectedError));
        // 🖨️ Выводим текст ошибки в консоль
        String actualErrorText = errorMessage.getText();
        System.out.println("❌ Текст ошибки: " + actualErrorText);
        return this;
    }

}