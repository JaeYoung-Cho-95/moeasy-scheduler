package com.moeasy.scheduler.scheduler.repository;

import com.moeasy.scheduler.scheduler.domain.Survey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByQuestionId(Long questionId);

    List<Survey> findAllByLastUpdatedGreaterThanEqualAndLastUpdatedLessThan(
            LocalDateTime startInclusive, LocalDateTime endExclusive
    );
}
