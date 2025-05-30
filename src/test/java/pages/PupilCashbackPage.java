package pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;

public class PupilCashbackPage {

    private final SelenideElement
            cashbackChapter = $("[id='hideMobileItems tab-cashback']"),
            getCashbackButton = $("#get-cashback-button"),
            kassa24Button = $("#cashback-type-button-default"),
            confirmationInput = $("#confirmation-code-input"),
            submitConfirmationButton = $("#confirm-button"),
            cardInput = $x("/html/body/div/div/div/div/div/div[2]/div/form/div[1]/div[1]/div[1]/input"),
            payButton = $x("/html/body/div/div/div/div/div/div[2]/div/form/div[4]/button");

    private static final String BEARER_TOKEN = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJNYXRoRWR1Y2F0b3JJc3N1ZXJJZCIsImV4cCI6MTc1MDQ5MDY0NiwiaWF0IjoxNzQ3ODk4NjQ2LCJpc3MiOiJNYXRoRWR1Y2F0b3JJc3N1ZXJJZCIsImp0aSI6IjgwOWZkMjRiLTg3NDEtNGIzYS1iYmYxLTkyN2FkZDc5MjIwOSIsIm5iZiI6MTc0Nzg5ODY0NSwic3ViIjoiMjAwOTA3MyIsInR5cCI6ImFjY2VzcyIsInVzZXJfdG9rZW5fdHlwZSI6InB1cGlsIn0.MgQgBKU_VgB9qlDjj2p4tH-c22ew9S6gTxbhcdxwPEUxRx9tFIi3teRx4g5ALZNfCj68deuf9yXG7yo-r8m6hQ";
    @Step("Логинимся как ученик: {phone}")
    public PupilCashbackPage loginAsStudent(String phone, String password, String expectedUsername) {
        new LoginPage()
                .openLoginPage()
                .clickSignButton()
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .clickSubmitButton()
                .verifySuccessfulLogin(expectedUsername);
        return this;
    }

    @Step("Переходим в раздел Cashback")
    public PupilCashbackPage clickCashbackChapterButton() {
        cashbackChapter.shouldBe(visible, Duration.ofSeconds(10)).click();
        return this;
    }

    @Step("Нажимаем на кнопку Получить кэшбэк")
    public PupilCashbackPage clickGetCashbackButton() {
        getCashbackButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        return this;
    }

    @Step("Выбираем способ выплаты Kassa24")
    public PupilCashbackPage clickKassa24Button() {
        kassa24Button.shouldBe(visible, Duration.ofSeconds(10)).click();
        return this;
    }

    @Step("Проверка появления toast-ошибок")
    public PupilCashbackPage verifyToastError() {
        sleep(2000); // Ждём появления тоста

        ElementsCollection toasts = $$("div.Toastify__toast-body").filter(visible);

        for (SelenideElement toast : toasts) {
            String text = toast.getText();

            if (text.contains("Растау коды табылмады")) {
                System.out.println("❌ [TOAST ERROR] Неверный код подтверждения: " + text);
                throw new AssertionError("Появился toast с ошибкой подтверждения кода: " + text);

            } else if (text.contains("Сұраным кезінде қате болды")) {
                System.out.println("❌ [TOAST ERROR] Дубликат платежа: " + text);
                throw new AssertionError("Появился toast с ошибкой дубликата платежа: " + text);
            }
        }

        System.out.println("[INFO] Ошибочных toast-сообщений не найдено");
        return this;
    }




    @Step("Получаем и вводим SMS‑код")
    public PupilCashbackPage enterConfirmationCodeFromApi(String phoneNumber) {
        sleep(15_000);

        String code =
                given()
                        .baseUri("https://test.qalan.kz")                 // ← ① сервер
                        .basePath("/api/confirmationCodeByPhoneNumber")   // ← ② путь
                        .relaxedHTTPSValidation()                         // если сертификат self‑signed
                        .queryParam("phoneNumber", phoneNumber)
                        .queryParam("type", 30)
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getString("code");   // {"code":"1876"} → 1876

        System.out.println("Получен код: " + code);

        confirmationInput.shouldBe(visible, Duration.ofSeconds(10)).click();
        for (char ch : code.toCharArray()) {
            confirmationInput.sendKeys(String.valueOf(ch));
            sleep(100); // имитируем набор вручную
        }
        submitConfirmationButton.shouldBe(visible).click();
        return this;
    }


    @Step("Вводим карту и нажимаем Выплатить")
    public PupilCashbackPage enterCardAndPay() {
        cardInput.shouldBe(visible, Duration.ofSeconds(20)).setValue("");
        payButton.shouldBe(visible, Duration.ofSeconds(10)).click();
        return this;
    }


    @Step("Проверяем успешную выплату")
    public void verifySuccessScreen() {
        $("body").shouldHave(text("Принято в обработку")).shouldBe(visible, Duration.ofSeconds(15));
        $("body").shouldHave(text("ЗАГРУЗИТЬ PDF")).shouldBe(visible);

        sleep(10000); // повисеть на экране 10 секунд

        System.out.println("✅ Успешная выплата подтверждена");
    }
    @Step("Вводим код вручную: {code}")
    public PupilCashbackPage enterCodeManually(String code) {
        confirmationInput.shouldBe(visible, Duration.ofSeconds(10)).click();
        confirmationInput.setValue(code);
        submitConfirmationButton.shouldBe(visible).click();
        sleep(2000);
        return this;
    }
    @Step("Ожидаем, что появится toast с ошибкой: 'Растау коды табылмады'")
    public PupilCashbackPage expectToastErrorMustAppear() {
        sleep(2000); // подождать появления
        SelenideElement errorToast = $x("//div[contains(@class,'Toastify__toast-body') and contains(.,'Растау коды табылмады')]");

        if (errorToast.isDisplayed()) {
            System.out.println("✅ Ожидаемый toast с ошибкой появился: " + errorToast.getText());
        } else {
            throw new AssertionError("❌ Ожидали тост с ошибкой, но он не появился");
        }

        return this;
    }
}
