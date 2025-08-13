package com.moeasy.scheduler.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.scheduler.scheduler.domain.Question;
import com.moeasy.scheduler.scheduler.domain.Survey;
import com.moeasy.scheduler.scheduler.dto.ContentDto;
import com.moeasy.scheduler.scheduler.dto.GraphItemDto;
import com.moeasy.scheduler.scheduler.dto.KeywordsItemsDto;
import com.moeasy.scheduler.scheduler.dto.KeywordsResponseDto;
import com.moeasy.scheduler.scheduler.dto.SaveDataDto;
import com.moeasy.scheduler.scheduler.dto.SentencesListDto;
import com.moeasy.scheduler.scheduler.repository.QuestionRepository;
import com.moeasy.scheduler.scheduler.repository.SurveyRepository;
import com.moeasy.scheduler.scheduler.service.NaverCloudStudioService;
import com.moeasy.scheduler.scheduler.util.CleanJson;
import com.moeasy.scheduler.scheduler.util.Pick20InShortQuestions;
import com.moeasy.scheduler.scheduler.util.SurveyUtils;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyResultScheduler {

  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
  private final SurveyRepository surveyRepository;
  private final QuestionRepository questionRepository;
  private final NaverCloudStudioService naverCloudStudioService;

  @Transactional
  @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
  public void logSurveyResultsUpdatedInLast10Min() {
    LocalDateTime now = LocalDateTime.now(ZONE_ID);
    LocalDateTime tenMinutesAgo = now.minusMinutes(10);

    List<Survey> updated = surveyRepository
        .findAllByLastUpdatedGreaterThanEqualAndLastUpdatedLessThan(tenMinutesAgo, now);

    if (updated.isEmpty()) {
      log.info("[SurveyResults] 최근 10분({} ~ {}) 동안 갱신된 레코드가 없습니다.", tenMinutesAgo, now);
      return;
    }

    log.info("[SurveyResults] 최근 10분({} ~ {}) 갱신건수: {}", tenMinutesAgo, now, updated.size());

    try {
      for (Survey survey : updated) {
        Question question = questionRepository.findBySurveyId(survey.getId());
        Integer totalCount = question.getCount();
        String json = survey.getResultsJson();

        ObjectMapper objectMapper = new ObjectMapper();
        String summarizedJson = SurveyUtils.summarizeMaxChoices(json, objectMapper);
        String summarizedJsonWithoutDemo = SurveyUtils.excludeAgeAndGender(summarizedJson,
            objectMapper);
        ContentDto contentDto = getContentData(summarizedJsonWithoutDemo, objectMapper);

        List<GraphItemDto> graphData = SurveyUtils.extractGraphsData(json, objectMapper);
        List<String> keywordsData = getKeywordsData(summarizedJson, objectMapper);
        SentencesListDto sentencesData = getSentencesData(summarizedJson, totalCount, objectMapper);

        SaveDataDto saveDataDto = SaveDataDto.builder()
            .subject(question.getTitle())
            .graphs(graphData.isEmpty() ? null : graphData)
            .keywords(keywordsData)
            .content(contentDto.getContent())
            .sentences(sentencesData.getSentences())
            .totalCount(totalCount)
            .build();

        String saveJson = objectMapper.writeValueAsString(saveDataDto); // 단일 라인 JSON
        survey.updateSummarizeJson(saveJson);
      }
    } catch (Exception e) {
      log.info(e.getMessage());
    }
  }

  private ContentDto getContentData(String summarizedJson, ObjectMapper objectMapper)
      throws IOException {
    return getContentResponse(
        naverCloudStudioService.getPromptWithFilePath("prompts/makeContent.txt"),
        Pick20InShortQuestions.extract(summarizedJson, objectMapper)
    );
  }

  private SentencesListDto getSentencesData(String summarizedJson, Integer totalCount,
      ObjectMapper objectMapper) throws IOException {
    return getSentencesListResponse(
        naverCloudStudioService.getPromptWithFilePath("prompts/makeSentences.txt"),
        CleanJson.toObjectiveWithPercent(summarizedJson, totalCount, objectMapper)
    );
  }

  private List<String> getKeywordsData(String summarizedJson, ObjectMapper objectMapper)
      throws IOException {
    KeywordsResponseDto keywordsResponse = getKeywordsResponse(
        naverCloudStudioService.getPromptWithFilePath("prompts/makeKeywordsUsingSurvey.txt"),
        Pick20InShortQuestions.extract(summarizedJson, objectMapper)
    );

    List<String> keywordsData = new ArrayList<>();
    if (keywordsResponse.getPositives() != null) {
      for (KeywordsItemsDto keywordsItemsDto : keywordsResponse.getPositives()) {
        keywordsData.add(keywordsItemsDto.getHashtag());
      }
    }

    if (keywordsResponse.getNegatives() != null) {
      for (KeywordsItemsDto keywordsItemsDto : keywordsResponse.getNegatives()) {
        keywordsData.add(keywordsItemsDto.getHashtag());
      }
    }
    return keywordsData;
  }

  private KeywordsResponseDto getKeywordsResponse(String systemPrompt, String inputPrompt) {
    return naverCloudStudioService.chatHcx007(
        systemPrompt, inputPrompt, new TypeReference<KeywordsResponseDto>() {
        }
    );
  }

  private SentencesListDto getSentencesListResponse(String systemPrompt, String inputPrompt) {
    return naverCloudStudioService.chatHcx007(
        systemPrompt, inputPrompt, new TypeReference<SentencesListDto>() {
        }
    );
  }

  private ContentDto getContentResponse(String systemPrompt, String inputPrompt) {
    return naverCloudStudioService.chatHcx007(
        systemPrompt, inputPrompt, new TypeReference<ContentDto>() {
        }
    );
  }
}
