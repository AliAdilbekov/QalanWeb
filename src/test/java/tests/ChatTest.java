package tests;

import com.codeborne.selenide.SelenideDriver;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import pages.ChatPage;
import testData.DriverFactory;

@Tag("chat")
@Execution(ExecutionMode.SAME_THREAD)
public class ChatTest {

    // Два SelenideDriver из DriverFactory
    private static final SelenideDriver studentDriver = DriverFactory.createStudentDriver();
    private static final SelenideDriver mentorDriver = DriverFactory.createMentorDriver();

    @Test
    void ChatFlowPersonalStudy2() {
        // --- Ученик ---
        ChatPage studentChat = new ChatPage(studentDriver)
                .loginAsStudent()      // логин (ученик)
                .openChatAsStudent();  // открывает чат

        // Ученик отправляет сообщение
        String studentMsg = studentChat.generateRandomMessage("Привет ментор ");
        studentChat.sendMessage(studentMsg)
                .checkNoDuplicateMessage(studentMsg);

        // --- Ментор ---
        ChatPage mentorChat = new ChatPage(mentorDriver)
                .loginAsMentor()        // логин (ментор)
                .openChatAsMentor()     // открывает чат
                .refreshChat()          // жмёт "Обновить"
                .waitForMessage(studentMsg)
                .checkNoDuplicateMessage(studentMsg)
                .logLastMessage();

        // Ментор отвечает
        String mentorMsg = mentorChat.generateRandomMessage("Привет ученик ");
        mentorChat.sendMessage(mentorMsg)
                .checkNoDuplicateMessage(mentorMsg);

        // Ученик снова проверяет
        studentChat.refreshChat()
                .waitForMessage(mentorMsg)
                .checkNoDuplicateMessage(mentorMsg)
                .logLastMessage();

        // Закрываем оба окна (ученик, ментор)
        studentChat.closeDriver();
        mentorChat.closeDriver();

        System.out.println("ChatFlowPersonalStudy2 пройден без дублей, все сообщения пришли.");
    }

    @Test
    void ChatFlowPersonalStudy3() {
        // --- Ученик ---
        ChatPage studentChat = new ChatPage(studentDriver)
                .loginAsStudent()
                .openChatAsStudent();

        // Ученик отправляет сообщение
        String studentMsg = studentChat.generateRandomMessage("Привет ментор ");
        studentChat.sendMessage(studentMsg)
                .checkNoDuplicateMessage(studentMsg);

        // --- Ментор ---
        ChatPage mentorChat = new ChatPage(mentorDriver)
                .loginAsMentor()
                .openChatFromSessionsAsMentor()  // открывает чат через кнопку "Сессии"
                .refreshChat()
                .waitForMessage(studentMsg)
                .checkNoDuplicateMessage(studentMsg)
                .logLastMessage();

        // Ментор отвечает
        String mentorMsg = mentorChat.generateRandomMessage("Привет ученик ");
        mentorChat.sendMessage(mentorMsg)
                .checkNoDuplicateMessage(mentorMsg);

        // Ученик снова проверяет
        studentChat.refreshChat()
                .waitForMessage(mentorMsg)
                .checkNoDuplicateMessage(mentorMsg)
                .logLastMessage();

        // Закрываем окна
        studentChat.closeDriver();
        mentorChat.closeDriver();

        System.out.println("ChatFlowPersonalStudy3 пройден без дублей, все сообщения пришли.");
    }
}
