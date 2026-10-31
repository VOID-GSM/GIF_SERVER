package com.example.gifserverv2.domain.assessment.roadmaking.service;

import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.request.FencePosition;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.request.SubmitRoadMakingAnswerRequest;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingProblemResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingRoundResultResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.SubmitRoadMakingAnswerResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.exception.RoadMakingException;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Person;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Vehicle;
import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({RoadMakingService.class, ProblemGenerator.class, BoardMapper.class,
        RoadMakingServiceTest.Config.class})
class RoadMakingServiceTest {

    @TestConfiguration
    static class Config {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired RoadMakingService service;
    @Autowired UserRepository userRepository;
    @Autowired CoinAccountRepository coinAccountRepository;

    Long userId;

    @BeforeEach
    void setUp() {
        userId = userRepository.save(new UserEntity("a@gsm.hs.kr", "홍길동", "1101", Role.USER, "1")).getId();
    }

    @Test
    void 지정한_문항_수를_모두_풀면_라운드가_자동_종료되고_코인이_지급된다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(3));
        assertThat(start.questionCount()).isEqualTo(3);

        RoadMakingProblemResponse problem = start.problem();
        SubmitRoadMakingAnswerResponse response = null;
        for (int i = 0; i < 3; i++) {
            assertThat(problem.sequence()).isEqualTo(i + 1);
            response = service.submit(userId, start.roundId(), problem.problemId(), anyFences(problem));
            problem = response.nextProblem();
        }

        assertThat(problem).isNull();
        RoadMakingRoundResultResponse result = response.result();
        assertThat(result).isNotNull();
        assertThat(result.completed()).isTrue();
        assertThat(result.solvedCount() + result.wrongCount()).isEqualTo(3);
        assertThat(result.earnedCoin()).isEqualTo(20 + 10 * result.solvedCount());
        assertThat(balanceOf(userId)).isEqualTo(result.earnedCoin());
    }

    @Test
    void 이미_응답한_문항은_다시_제출할_수_없다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(3));
        RoadMakingProblemResponse problem = start.problem();
        service.submit(userId, start.roundId(), problem.problemId(), anyFences(problem));

        assertThatThrownBy(() -> service.submit(userId, start.roundId(), problem.problemId(), anyFences(problem)))
                .isInstanceOf(RoadMakingException.class);
    }

    @Test
    void 중간에_종료하면_코인이_지급되지_않는다() {
        var start = service.startRound(userId, new StartAssessmentRoundRequest(5));
        service.submit(userId, start.roundId(), start.problem().problemId(), anyFences(start.problem()));

        RoadMakingRoundResultResponse result = service.finishRound(userId, start.roundId());

        assertThat(result.completed()).isFalse();
        assertThat(result.earnedCoin()).isZero();
        assertThat(balanceOf(userId)).isZero();
    }

    /** 빈 칸에 요구 개수만큼 울타리를 놓는다 (정답 여부는 상관없음) */
    private SubmitRoadMakingAnswerRequest anyFences(RoadMakingProblemResponse problem) {
        Set<Position> occupied = new HashSet<>();
        for (Vehicle vehicle : problem.vehicles()) {
            occupied.add(vehicle.position());
        }
        for (Person person : problem.people()) {
            occupied.add(person.position());
        }
        List<FencePosition> fences = new ArrayList<>();
        for (int r = 0; r < problem.rows() && fences.size() < problem.minFenceCount(); r++) {
            for (int c = 0; c < problem.cols() && fences.size() < problem.minFenceCount(); c++) {
                if (!occupied.contains(new Position(r, c))) {
                    fences.add(new FencePosition(r, c));
                }
            }
        }
        return new SubmitRoadMakingAnswerRequest(fences);
    }

    private int balanceOf(Long user) {
        return coinAccountRepository.findByUserId(user).map(CoinAccount::getBalance).orElse(0);
    }
}
