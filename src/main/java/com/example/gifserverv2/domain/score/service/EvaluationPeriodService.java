package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.entity.EvaluationPeriod;
import com.example.gifserverv2.domain.score.entity.ScoreCategory;
import com.example.gifserverv2.domain.score.repository.EvaluationPeriodRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EvaluationPeriodService {

    private final EvaluationPeriodRepository periodRepository;

    @Transactional(readOnly = true)
    public void validateEvaluationPeriod(ScoreCategory category) {
        LocalDateTime now = LocalDateTime.now();
        EvaluationPeriod period = periodRepository.findById(category)
                .orElseGet(() -> getDefaultPeriod(category));

        if (!period.isWithinPeriod(now)) {
            throw new IllegalArgumentException(category + " 영역의 평가 기간이 아닙니다.");
        }
    }

    @Transactional
    public void updatePeriod(AuthenticatedUser user, ScoreCategory category, LocalDateTime startDate, LocalDateTime endDate) {
        if (user.adminRole() != AdminRole.MASTER) {
            throw new IllegalArgumentException("평가 기간 설정 권한은 Master 교사에게만 있습니다.");
        }

        validatePeriodRange(startDate, endDate);

        EvaluationPeriod period = periodRepository.findById(category)
                .orElse(new EvaluationPeriod(category, startDate, endDate));

        period.updatePeriod(startDate, endDate);
        periodRepository.save(period);
    }

    private void validatePeriodRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("평가 시작일과 마감일은 필수 항목입니다.");
        }
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new IllegalArgumentException("평가 시작일은 마감일보다 이전이어야 합니다.");
        }
    }

    private EvaluationPeriod getDefaultPeriod(ScoreCategory category) {
        int currentYear = LocalDateTime.now().getYear();
        LocalDateTime defaultEndDate = LocalDateTime.of(currentYear, 12, 29, 23, 59, 59);

        if (category == ScoreCategory.REPORT) {
            LocalDateTime defaultStartDate = LocalDateTime.of(currentYear, 12, 28, 0, 0, 0);
            return new EvaluationPeriod(category, defaultStartDate, defaultEndDate);
        }

        LocalDateTime defaultStartDate = LocalDateTime.of(currentYear, 12, 28, 0, 0, 0);
        return new EvaluationPeriod(category, defaultStartDate, defaultEndDate);
    }
}