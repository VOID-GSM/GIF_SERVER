package com.example.gifserverv2.domain.assessment.catchase.service;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest.JudgmentItem;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseQuestionResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundResultResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.SubmitCatChaseJudgmentsResponse;
import com.example.gifserverv2.domain.assessment.catchase.entity.ConfidenceLevel;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;
import com.example.gifserverv2.domain.assessment.catchase.exception.CatChaseException;
import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(CatChaseService.class)
class CatChaseServiceTest {

    @Autowired CatChaseService service;
    @Autowired UserRepository userRepository;
    @Autowired CoinAccountRepository coinAccountRepository;

    Long userId;
    Long otherUserId;

    @BeforeEach
    void setUp() {
        userId = userRepository.save(new UserEntity("a@gsm.hs.kr", "홍길동", "1101", Role.USER, "1")).getId();
        otherUserId = userRepository.save(new UserEntity("b@gsm.hs.kr", "김철수", "2101", Role.USER, "2")).getId();
    }

    @Test
    void 문항_수가_3개_미만이거나_10개_초과면_검증에_실패한다() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(new StartAssessmentRoundRequest(2))).isNotEmpty();
        assertThat(validator.validate(new StartAssessmentRoundRequest(11))).isNotEmpty();
        assertThat(validator.validate(new StartAssessmentRoundRequest(null))).isNotEmpty();
        assertThat(validator.validate(new StartAssessmentRoundRequest(3))).isEmpty();
        assertThat(validator.validate(new StartAssessmentRoundRequest(10))).isEmpty();
    }

    @Test
    void 다섯_문항_중_세_문항을_맞히면_50코인이_지급된다() {
        CatChaseRoundResultResponse result = play(userId, 5, 3);

        assertThat(result.completed()).isTrue();
        assertThat(result.correctCount()).isEqualTo(3);
        assertThat(result.earnedCoin()).isEqualTo(50);
        assertThat(balanceOf(userId)).isEqualTo(50);
    }

    @Test
    void 모두_틀려도_끝까지_진행하면_기본_20코인이_지급된다() {
        CatChaseRoundResultResponse result = play(userId, 3, 0);

        assertThat(result.earnedCoin()).isEqualTo(20);
        assertThat(balanceOf(userId)).isEqualTo(20);
    }

    @Test
    void 중간에_종료하면_코인이_지급되지_않는다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(5));
        service.submitJudgments(userId, start.roundId(), start.question().questionId(), answer(start.question(), true));

        CatChaseRoundResultResponse result = service.finishRound(userId, start.roundId());

        assertThat(result.completed()).isFalse();
        assertThat(result.earnedCoin()).isZero();
        assertThat(coinAccountRepository.findByUserId(userId)).isEmpty();
        assertThatThrownBy(() -> service.getCurrentQuestion(userId, start.roundId()))
                .isInstanceOf(CatChaseException.class);
    }

    @Test
    void 같은_문항에_두_번_제출할_수_없다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(3));
        CatChaseQuestionResponse question = start.question();
        service.submitJudgments(userId, start.roundId(), question.questionId(), answer(question, true));

        assertThatThrownBy(() ->
                service.submitJudgments(userId, start.roundId(), question.questionId(), answer(question, true)))
                .isInstanceOf(CatChaseException.class);
    }


    /** questionCount 문항을 풀되 앞에서부터 correctCount 개만 맞힌다 */
    private CatChaseRoundResultResponse play(Long user, int questionCount, int correctCount) {
        var start = service.startRound(user, new StartAssessmentRoundRequest(questionCount));
        CatChaseQuestionResponse question = start.question();
        SubmitCatChaseJudgmentsResponse response = null;
        for (int i = 0; i < questionCount; i++) {
            response = service.submitJudgments(user, start.roundId(), question.questionId(),
                    answer(question, i < correctCount));
            assertThat(response.correct()).isEqualTo(i < correctCount);
            question = response.nextQuestion();
        }
        assertThat(question).isNull();
        return response.result();
    }

    private SubmitCatChaseJudgmentsRequest answer(CatChaseQuestionResponse question, boolean correct) {
        List<JudgmentItem> items = question.catPositions().stream()
                .map(position -> {
                    boolean hadMouse = question.mousePositions().contains(position);
                    // 오답을 만들 때는 첫 번째 판단만 뒤집는다
                    boolean flip = !correct && position.equals(question.catPositions().get(0));
                    boolean found = flip != hadMouse;
                    return new JudgmentItem(position, found ? Judgment.FOUND : Judgment.MISSED, ConfidenceLevel.CONFIDENT);
                })
                .toList();
        return new SubmitCatChaseJudgmentsRequest(items);
    }

    private int balanceOf(Long user) {
        return coinAccountRepository.findByUserId(user).map(CoinAccount::getBalance).orElse(0);
    }
}
