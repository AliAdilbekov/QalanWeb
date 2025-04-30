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
                .hideCalculatorButtonClick()
                .logTaskSummary()
                .solveTasksUntilCongrats("1001");
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void personalStudyWithOneMistake() {
        personalTaskPage.loginAsStudent("7083544313", "12345678", "Forautotest FORAUTOTEST")
                .checkPersonalStudyFlow("1001")
                .startButtonClick()
                .hideCalculatorButtonClick()
                .logTaskSummary()
                .makeErrors(1, "1001")  // пишем кол-во ошибок которое должны совершить и userID
                .verifyLifeCount(1)         // пишем сколько жизней должно быть потрачено
                .verifyTaskCount(1)   // здесь указываем число задач которое должно прибавиться, то есть проверяем дельту
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
                .hideCalculatorButtonClick()
                .logTaskSummary()
                .makeErrors(3, "1001")  // пишем кол-во ошибок которое должны совершить и userID
                .verifyLifeCount(3)         // пишем сколько жизней должно быть потрачено
                .verifyTaskCount(3)   // здесь указываем число задач которое должно прибавиться, то есть проверяем дельту
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
                .hideCalculatorButtonClick()
                .logTaskSummary()
                .showSolutionButtonClick()
                .verifyLifeCount(1)        // пишем сколько жизней должно быть потрачено
                .nextButtonClick()
                .verifyTaskCount(3)  // здесь указываем число задач которое должно прибавиться, то есть проверяем дельту
                .logTaskSummary()
                .solveTasksUntilCongrats("1001");
    }

    @Test
    @ResourceLock(value = "sharedResource")
    void personalStudyWithVideoExplanationFlow() {
        personalTaskPage.loginAsStudent("7083544313", "12345678", "Forautotest FORAUTOTEST")
                .checkPersonalStudyFlow("1001")
                .startButtonClick()
                .hideCalculatorButtonClick()
                .logTaskSummary()
                .videoExplanationButtonClick()
                .checkVideoPlayer()
                .verifyLifeCount(1)        // пишем сколько жизней должно быть потрачено
                .verifyTaskCount(1)  // здесь указываем число задач которое должно прибавиться, то есть проверяем дельту
                .logTaskSummary()
                .solveTasksUntilCongrats("1001");
    }
}
