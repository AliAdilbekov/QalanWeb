package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import testData.TestBase;

import static com.codeborne.selenide.Selenide.sleep;

@Execution(ExecutionMode.SAME_THREAD)
public class RegistrationTest extends TestBase {

    @Test
    @ResourceLock(value = "sharedResource")
    void testSuccessRegistration() {

        // 🎲 Генерация данных
        String name = faker.name().firstName();
        String surname = faker.name().lastName();
        String phone = "707" + faker.number().digits(7);
        String password = faker.number().digits(8);

        // 🚀 Регистрация
        registrationPage.openRegistrationPage()
                .enterFirstName(name)
                .enterLastName(surname)
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .enterRepeatPassword(password)
                .selectPupilRole()
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyUserFullName(name + " " + surname)
                .printGeneratedData(name, surname, phone, password, password)
                .verifyNameSurnameInConsole(name, surname);
        sleep(2000);
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testIncorrectPhone() {

        // 🎲 Генерация данных
        String name = faker.name().firstName();
        String surname = faker.name().lastName();
        String invalidPhone = "707" + faker.number().digits(6);
        String password = faker.number().digits(8);

        registrationPage.openRegistrationPage()
                .enterFirstName(name)
                .enterLastName(surname)
                .enterPhoneNumber(invalidPhone)
                .enterPassword(password)
                .enterRepeatPassword(password)
                .selectPupilRole()
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyErrorMessage("Телефон нөмірі дұрыс терілгенін тексеріңіз")
                .printGeneratedData(name, surname, invalidPhone, password, password);
        sleep(2000);
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testIncorrectPassword() {

        // 🎲 Генерация данных
        String name = faker.name().firstName();
        String surname = faker.name().lastName();
        String phone = "707" + faker.number().digits(7);
        String invalidPassword = faker.number().digits(7);

        registrationPage.openRegistrationPage()
                .enterFirstName(name)
                .enterLastName(surname)
                .enterPhoneNumber(phone)
                .enterPassword(invalidPassword)
                .enterRepeatPassword(invalidPassword)
                .selectPupilRole()
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyErrorMessage("Құпия сөздің ұзындығы 8-ден кем емес және 15-тен артық болмауы керек")
                .printGeneratedData(name, surname, phone, invalidPassword, invalidPassword);
        sleep(2000);
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testIncorrectRepeatPassword() {

        // 🎲 Генерация данных
        String name = faker.name().firstName();
        String surname = faker.name().lastName();
        String phone = "707" + faker.number().digits(7);
        String password = faker.number().digits(8);
        String invalidPassword = faker.number().digits(7);

        registrationPage.openRegistrationPage()
                .enterFirstName(name)
                .enterLastName(surname)
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .enterRepeatPassword(invalidPassword)
                .selectPupilRole()
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyErrorMessage("Терген екі құпия сөз бір-біріне сәйкес келмейді")
                .printGeneratedData(name, surname, phone, password, invalidPassword);
        sleep(2000);
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testWithOutRole() {

        // 🎲 Генерация данных
        String name = faker.name().firstName();
        String surname = faker.name().lastName();
        String phone = "707" + faker.number().digits(7);
        String password = faker.number().digits(8);

        registrationPage.openRegistrationPage()
                .enterFirstName(name)
                .enterLastName(surname)
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .enterRepeatPassword(password)
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyErrorMessage("Оқушы немесе мұғалім екеніңізді таңдаңыз")
                .printGeneratedData(name, surname, phone, password, password);
        sleep(2000);
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testIncorrectName() {

        // 🎲 Генерация данных
        String invalidName = faker.letterify("?");
        String surname = faker.name().lastName();
        String phone = "707" + faker.number().digits(7);
        String password = faker.number().digits(8);

        registrationPage.openRegistrationPage()
                .enterFirstName(invalidName)
                .enterLastName(surname)
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .enterRepeatPassword(password)
                .selectPupilRole()
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyErrorMessage("Атыңыз бен тегіңізді толық жазыңыз")
                .printGeneratedData(invalidName, surname, phone, password, password);
        sleep(2000);
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testEmptyName() {

        // 🎲 Генерация данных
        String surname = faker.name().lastName();
        String phone = "707" + faker.number().digits(7);
        String password = faker.number().digits(8);

        registrationPage.openRegistrationPage()
                .enterFirstName("")
                .enterLastName(surname)
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .enterRepeatPassword(password)
                .selectPupilRole()
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyErrorMessage("Бос өрістерді толтырыңыз")
                .printGeneratedData("", surname, phone, password, password);
        sleep(2000);
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void testIncorrectSurname() {

        // 🎲 Генерация данных
        String name = faker.name().firstName();
        String phone = "707" + faker.number().digits(7);
        String password = faker.number().digits(8);

        registrationPage.openRegistrationPage()
                .enterFirstName(name)
                .enterLastName("+++")
                .enterPhoneNumber(phone)
                .enterPassword(password)
                .enterRepeatPassword(password)
                .selectPupilRole()
                .scrollToSubmitButton()
                .clickSubmitButton()
                .verifyErrorMessage("Тегі тек әріптерден тұруы керек")
                .printGeneratedData(name, "+++", phone, password, password);
        sleep(2000);
    }
}