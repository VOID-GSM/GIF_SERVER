package com.example.gifserverv2.domain.assessment.catchase.service;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest.JudgmentItem;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseQuestionResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundResultResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.SubmitCatChaseJudgmentsResponse;
import com.example.gifserverv2.domain.assessment.catchase.entity.ConfidenceLevel;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;
import com.example.gifserverv2.domain.assessment.catchase.exception.CatChaseException;
import com.example.gifserverv2.domain.assessment.common.AssessmentCoinRewarder;
import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse;
import com.example.gifserverv2.domain.assessment.ranking.service.AssessmentRankingCalculator;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseRoundRepository;
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
@Import({CatChaseService.class, AssessmentCoinRewarder.class, AssessmentRankingCalculator.class})
class CatChaseServiceTest {

    @Autowired CatChaseService service;
    @Autowired UserRepository userRepository;
    @Autowired CoinAccountRepository coinAccountRepository;
    @Autowired CatChaseRoundRepository roundRepository;

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
        // 프로필에 보이는 코인도 함께 갱신된다
        assertThat(userRepository.findById(userId).orElseThrow().getCoinBalance()).isEqualTo(50);
    }

    @Test
    void 문항_수_도입_이전_라운드는_종료된_라운드로_취급한다() {
        CatChaseRound legacy = roundRepository.save(CatChaseRound.start(userId, 0));

        assertThat(legacy.isInProgress()).isFalse();
        assertThat(service.getMyRounds(userId).get(0).finished()).isTrue();
        assertThatThrownBy(() -> service.getCurrentQuestion(userId, legacy.getId()))
                .isInstanceOf(CatChaseException.class);
        assertThat(service.finishRound(userId, legacy.getId()).earnedCoin()).isZero();
    }

    @Test
    void 끝까지_진행해도_모두_틀리면_코인이_지급되지_않는다() {
        CatChaseRoundResultResponse result = play(userId, 3, 0);

        assertThat(result.earnedCoin()).isZero();
        assertThat(balanceOf(userId)).isZero();
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

    @Test
    void 랭킹은_같은_문항_수끼리_맞힌_수_순으로_매기고_사용자별_최고_기록을_쓴다() {
        play(userId, 5, 3);
        play(otherUserId, 5, 4);
        play(userId, 5, 5); // 홍길동의 5문항 최고 기록은 5/5
        play(otherUserId, 10, 8);

        AssessmentRankingResponse ranking = service.getRanking(userId, 5, 7);

        assertThat(ranking.questionCount()).isEqualTo(5);
        assertThat(ranking.rankings()).hasSize(2);
        assertThat(ranking.rankings().get(0).name()).isEqualTo("홍길동");
        assertThat(ranking.rankings().get(0).correctCount()).isEqualTo(5);
        assertThat(ranking.rankings().get(0).earnedCoin()).isEqualTo(70);
        assertThat(ranking.rankings().get(1).name()).isEqualTo("김철수");
        assertThat(ranking.rankings().get(1).grade()).isEqualTo(2);
        assertThat(ranking.me().rank()).isEqualTo(1);
        assertThat(ranking.me().totalCount()).isEqualTo(5);

        // 10문항 랭킹에는 10문항 기록만 들어간다
        AssessmentRankingResponse tenQuestions = service.getRanking(userId, 10, 7);
        assertThat(tenQuestions.rankings()).extracting(r -> r.name()).containsExactly("김철수");
        assertThat(tenQuestions.me()).isNull();
    }

    @Test
    void 기록이_없으면_me는_null이고_범위_밖_문항_수는_거절한다() {
        Long newcomer = userRepository.save(new UserEntity("c@gsm.hs.kr", "이영희", "3101", Role.USER, "3")).getId();

        assertThat(service.getRanking(newcomer, 3, 7).me()).isNull();
        assertThat(service.getRanking(newcomer, 3, 7).rankings()).isEmpty();
        assertThatThrownBy(() -> service.getRanking(newcomer, 2, 7)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getRanking(newcomer, 11, 7)).isInstanceOf(IllegalArgumentException.class);
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
