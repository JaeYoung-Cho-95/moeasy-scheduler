package com.moeasy.scheduler.scheduler.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Question {

  @Id
  @GeneratedValue
  @Column(name = "question_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "survey_id")
  private Survey survey;

  private String title;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "json")
  private String content;

  private LocalDateTime createdTime;
  private LocalDateTime expiredTime;
  private Boolean expired;
  private Integer count;

  @Column(length = 2048)
  private String urlInQrCode;


  @Builder
  private Question(Member member, String title, String content) {
    this.member = member;
    this.title = title;
    this.content = content;
  }

  @PrePersist
  public void onPrePersist() {
    this.createdTime = LocalDateTime.now();
    this.expiredTime = this.createdTime.plusWeeks(1);
    this.expired = false;
    this.urlInQrCode = "";
    this.count = 0;
  }

  public boolean refreshExpired(LocalDateTime now) {
    if (Boolean.TRUE.equals(this.expired)) {
      return false;
    }
    if (this.expiredTime != null && !this.expiredTime.isAfter(now)) {
      this.expired = true;
      return true;
    }
    return false;
  }

  public boolean refreshExpired() {
    return refreshExpired(LocalDateTime.now());
  }

  public void updateUrlInQrCode(String newUrl) {
    if (Boolean.TRUE.equals(this.expired)) {
      throw new IllegalStateException("만료된 질문의 QR URL은 수정할 수 없습니다.");
    }
    this.urlInQrCode = newUrl;
  }

  public void updateTitle(String title) {
    this.title = title;
  }

  public void linkSurvey(Survey survey) {
    this.survey = survey;
    if (survey != null) {
      survey.linkQuestion(this);
    }
  }

  public void increaseCount() {
    if (this.count == null) {
      this.count = 0;
    }
    this.count += 1;
  }
}
