package com.moeasy.scheduler.scheduler.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Survey {
    @Id
    @GeneratedValue
    @Column(name = "survey_id")
    private Long id;

    @OneToOne(mappedBy = "survey", fetch = FetchType.LAZY)
    private Question question;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String resultsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String summarizeJson;

    private LocalDateTime lastUpdated;

    @Builder
    private Survey(String resultsJson) {
        this.resultsJson = resultsJson;
    }

    public void linkQuestion(Question question) {
        this.question = question;
    }

    public void updateResultsJson(String resultsJson) {
        this.resultsJson = resultsJson;
    }

    public void updateSummarizeJson(String summarizeJson) {
        this.summarizeJson = summarizeJson;
    }

    public void updateLastUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }
}
