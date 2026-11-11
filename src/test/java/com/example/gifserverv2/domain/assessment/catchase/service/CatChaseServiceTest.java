package com.example.gifserverv2.domain.assessment.catchase.service;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest.JudgmentItem;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseQuestionResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundDetailResponse;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
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
    @Autowired TestEntityManager em;

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
    void 같은_문항_수로_다시_시작하면_진행_중인_라운드를_이어간다() {
        var first = service.startRound(userId, new StartAssessmentRoundRequest(3));
        service.submitJudgments(userId, first.roundId(), first.question().questionId(), answer(first.question(), true));

        var again = service.startRound(userId, new StartAssessmentRoundRequest(3));

        assertThat(again.roundId()).isEqualTo(first.roundId());
        assertThat(again.questionCount()).isEqualTo(3);
        assertThat(again.question().sequence()).isEqualTo(2);
        assertThat(roundRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).hasSize(1);
    }

    @Test
    void 다른_문항_수로_시작하면_진행_중인_라운드는_이탈로_끝나고_새_라운드가_시작된다() {
        var first = service.startRound(userId, new StartAssessmentRoundRequest(5));
        service.submitJudgments(userId, first.roundId(), first.question().questionId(), answer(first.question(), true));

        var second = service.startRound(userId, new StartAssessmentRoundRequest(3));

        assertThat(second.roundId()).isNotEqualTo(first.roundId());
        assertThat(second.questionCount()).isEqualTo(3);
        assertThat(second.question().sequence()).isEqualTo(1);

        CatChaseRoundResultResponse abandoned = service.finishRound(userId, first.roundId());
        assertThat(abandoned.completed()).isFalse();
        assertThat(abandoned.earnedCoin()).isZero();
        assertThat(balanceOf(userId)).isZero();
    }

    @Test
    void 문항마다_고양이는_서로_다른_칸에_정확히_2마리다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(10));
        CatChaseQuestionResponse question = start.question();
        while (question != null) {
            assertThat(question.catPositions()).hasSize(2).doesNotHaveDuplicates()
                    .allSatisfy(position -> assertThat(position).isBetween(0, CatChaseRound.GRID_SIZE - 1));
            assertThat(question.mousePositions()).hasSizeBetween(1, 13);
            question = service.submitJudgments(userId, start.roundId(), question.questionId(), answer(question, true))
                    .nextQuestion();
        }
    }

    @Test
    void TIMEOUT_판단은_쥐_유무와_관계없이_오답이고_문항도_오답이다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(3));
        CatChaseQuestionResponse question = start.question();
        int red = question.catPositions().get(0);
        int blue = question.catPositions().get(1);
        // 파란 고양이는 맞히고 빨간 고양이만 시간 초과
        JudgmentItem blueItem = answer(question, true).judgments().stream()
                .filter(item -> item.position() == blue).findFirst().orElseThrow();
        var request = new SubmitCatChaseJudgmentsRequest(List.of(new JudgmentItem(red, Judgment.TIMEOUT, null), blueItem));

        SubmitCatChaseJudgmentsResponse response = service.submitJudgments(userId, start.roundId(), question.questionId(), request);

        assertThat(response.correct()).isFalse();
        assertThat(response.correctCount()).isZero();
        CatChaseRoundDetailResponse.QuestionResult detail = service.getRound(userId, start.roundId()).questions().get(0);
        assertThat(detail.correct()).isFalse();
        CatChaseRoundDetailResponse.JudgmentResult timeout = detail.responses().stream()
                .filter(r -> r.position() == red).findFirst().orElseThrow();
        assertThat(timeout.judgment()).isEqualTo(Judgment.TIMEOUT);
        assertThat(timeout.confidence()).isNull();
        assertThat(timeout.correct()).isFalse();
    }

    @Test
    void 제한_시간을_넘겨_제출하면_보낸_판단과_관계없이_TIMEOUT으로_기록한다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(3));
        CatChaseQuestionResponse question = start.question();
        // 고양이 2마리 x 10초 + 여유 2초를 넘긴 상태로 만든다
        em.flush();
        em.getEntityManager()
                .createNativeQuery("update cat_chase_question set created_at = ? where id = ?")
                .setParameter(1, LocalDateTime.now().minusSeconds(23))
                .setParameter(2, question.questionId())
                .executeUpdate();
        em.clear();

        SubmitCatChaseJudgmentsResponse response =
                service.submitJudgments(userId, start.roundId(), question.questionId(), answer(question, true));

        assertThat(response.correct()).isFalse();
        assertThat(service.getRound(userId, start.roundId()).questions().get(0).responses())
                .allSatisfy(r -> {
                    assertThat(r.judgment()).isEqualTo(Judgment.TIMEOUT);
                    assertThat(r.confidence()).isNull();
                });
    }

    @Test
    void TIMEOUT이면_확신도를_보내도_저장하지_않는다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(3));
        CatChaseQuestionResponse question = start.question();
        var request = new SubmitCatChaseJudgmentsRequest(question.catPositions().stream()
                .map(position -> new JudgmentItem(position, Judgment.TIMEOUT, ConfidenceLevel.CONFIDENT))
                .toList());

        service.submitJudgments(userId, start.roundId(), question.questionId(), request);

        assertThat(service.getRound(userId, start.roundId()).questions().get(0).responses())
                .allSatisfy(r -> assertThat(r.confidence()).isNull());
    }

    @Test
    void 확신도는_TIMEOUT일_때만_생략할_수_있다() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        assertThat(validator.validate(request(new JudgmentItem(7, Judgment.TIMEOUT, null)))).isEmpty();
        assertThat(validator.validate(request(new JudgmentItem(7, Judgment.FOUND, null)))).isNotEmpty();
        assertThat(validator.validate(request(new JudgmentItem(7, Judgment.MISSED, null)))).isNotEmpty();
        assertThat(validator.validate(request(new JudgmentItem(7, Judgment.FOUND, ConfidenceLevel.UNSURE)))).isEmpty();
    }

    @Test
    void 판단이_2개가_아니면_거절한다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(3));
        CatChaseQuestionResponse question = start.question();
        var onlyRed = new SubmitCatChaseJudgmentsRequest(List.of(answer(question, true).judgments().get(0)));

        assertThatThrownBy(() -> service.submitJudgments(userId, start.roundId(), question.questionId(), onlyRed))
                .isInstanceOf(CatChaseException.class)
                .hasMessageContaining("개수");
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

    private SubmitCatChaseJudgmentsRequest request(JudgmentItem... items) {
        return new SubmitCatChaseJudgmentsRequest(List.of(items));
    }

    private int balanceOf(Long user) {
        return coinAccountRepository.findByUserId(user).map(CoinAccount::getBalance).orElse(0);
    }
}
