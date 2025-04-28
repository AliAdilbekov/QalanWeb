package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import testData.TestBase;

@Execution(ExecutionMode.SAME_THREAD)
public class PersonalTaskTest extends TestBase {


    @Test
    @ResourceLock(value = "sharedResource")
    void happyFlow() {
        personalTaskPage.loginAsStudent("7083544313", "12345678", "Forautotest FORAUTOTEST")
                .checkPersonalStudyFlow("1001")
                .startButtonClick()
                .logTaskSummary()
                .solveTasksUntilCongrats("1001");
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void personalStudyWithOneMistake() {
        personalTaskPage.loginAsStudent("7083544313", "12345678", "Forautotest FORAUTOTEST")
                .checkPersonalStudyFlow("1001")
                .startButtonClick()
                .logTaskSummary()
                .makeErrors(1, "1001") // пишем кол-во ошибок которое должны совершить и userID
                .verifyLifeCount(1) // пишем сколько жизней должно быть потрачено
                .verifyTaskCount(4) // здесь общее число задач которое должно быть
                .logTaskSummary()
                .clearAnswerField()
                .solveTasksUntilCongrats("1001");
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void personalStudyWithThreeMistake() {
        personalTaskPage.loginAsStudent("7083544313", "12345678", "Forautotest FORAUTOTEST")
                .checkPersonalStudyFlow("1001")
                .startButtonClick()
                .logTaskSummary()
                .makeErrors(3, "1001")
                .verifyLifeCount(3)
                .verifyTaskCount(6)
                .logTaskSummary()
                .nextButtonClick()
                .clearAnswerField()
                .solveTasksUntilCongrats("1001");

    }

    @Test
    @ResourceLock(value = "sharedResource")
    void personalStudyWithShowAnswerFlow() {
        personalTaskPage.loginAsStudent("7083544313", "12345678", "Forautotest FORAUTOTEST")
                .checkPersonalStudyFlow("1001")
                .startButtonClick()
                .logTaskSummary()
                .showSolutionButtonClick()
                .nextButtonClick()
                .verifyLifeCount(1)
                .verifyTaskCount(6)
                .logTaskSummary()
                .solveTasksUntilCongrats("1001");
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void personalStudyWithVideoExplanationFlow() {
        personalTaskPage.loginAsStudent("7083544313", "12345678", "Forautotest FORAUTOTEST")
                .checkPersonalStudyFlow("1001")
                .startButtonClick()
                .logTaskSummary()
                .videoExplanationButtonClick()
                .checkVideoPlayer()
                .verifyLifeCount(1)
                .verifyTaskCount(4)
                .logTaskSummary()
                .solveTasksUntilCongrats("1001");
    }
}
