package testData;

import com.codeborne.selenide.SelenideConfig;
import com.codeborne.selenide.SelenideDriver;

public class DriverFactory {

    /**
     * Создаёт SelenideDriver для «ученика» (student) с нужными размерами/координатами.
     */
    public static SelenideDriver createStudentDriver() {
        SelenideConfig studentConfig = new SelenideConfig()
                .browser("chrome")
                .browserPosition("0x0")
                .browserSize("760x810")
                .holdBrowserOpen(false)
                .timeout(30000);

        return new SelenideDriver(studentConfig);
    }

    /**
     * Создаёт SelenideDriver для «ментора» (mentor) с другими позициями окна.
     */
    public static SelenideDriver createMentorDriver() {
        SelenideConfig mentorConfig = new SelenideConfig()
                .browser("chrome")
                .browserPosition("780x0")
                .browserSize("760x810")
                .holdBrowserOpen(false)
                .timeout(30000);

        return new SelenideDriver(mentorConfig);
    }
}
