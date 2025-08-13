package com.moeasy.scheduler.scheduler.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Pick20InShortQuestions {
    public static String extract(String json, ObjectMapper om) throws IOException {
        return extract(json, om, 20);
    }

    public static String extract(String json, ObjectMapper om, int sampleLimit) throws IOException {
        if (json == null || json.isBlank()) return "[]";

        try {
            List<Map<String, Object>> arr = om.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            if (!arr.isEmpty() && arr.get(0) != null && arr.get(0).containsKey("question")) {
                List<Map<String, Object>> out = new ArrayList<>();
                for (Map<String, Object> row : arr) {
                    if (row == null) continue;

                    String question = Objects.toString(row.get("question"), "");
                    Map<String, Object> outRow = new LinkedHashMap<>();
                    outRow.put("question", question);

                    if (row.containsKey("count")) {
                        String topChoice = firstChoice(row.get("topChoices"));
                        if (topChoice.isEmpty()) {
                            topChoice = firstChoice(row.get("topchoices"));
                        }
                        outRow.put("topChoices", topChoice);

                        Object cnt = row.get("count");
                        int c = (cnt instanceof Number n) ? n.intValue() : 0;
                        outRow.put("count", c);
                    } else {
                        List<String> answers = toStringList(row.get("answers"));
                        List<String> sampled = sampleLimit > 0 ? sampleUpToRandom(answers, sampleLimit) : answers;
                        outRow.put("answers", sampled);
                    }
                    out.add(outRow);
                }
                return om.writeValueAsString(out);
            }
        } catch (Exception ignore) {
        }

        List<Map<String, Map<String, Object>>> aggregates =
                om.readValue(json, new TypeReference<List<Map<String, Map<String, Object>>>>() {});

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Map<String, Object>> perQuestion : aggregates) {
            if (perQuestion == null || perQuestion.isEmpty()) continue;

            String question = perQuestion.keySet().iterator().next();
            Map<String, Object> answersMap = perQuestion.get(question);
            if (answersMap == null || answersMap.isEmpty()) continue;

            // 숫자 카운트 존재 여부 및 최댓값 탐색
            Long maxVal = null;
            for (Map.Entry<String, Object> e : answersMap.entrySet()) {
                String k = e.getKey();
                if ("others".equals(k)) continue;
                Object v = e.getValue();
                if (v instanceof Number n) {
                    long lv = n.longValue();
                    if (maxVal == null || lv > maxVal) maxVal = lv;
                }
            }

            Map<String, Object> outRow = new LinkedHashMap<>();
            outRow.put("question", question);

            if (maxVal == null) {
                List<String> answers = null;

                Object others = answersMap.get("others");
                if (others instanceof List<?>) {
                    answers = toStringList(others);
                }

                if ((answers == null || answers.isEmpty()) && answersMap.get("answers") instanceof List<?>) {
                    answers = toStringList(answersMap.get("answers"));
                }

                if (answers == null) answers = List.of();
                List<String> sampled = sampleLimit > 0 ? sampleUpToRandom(answers, sampleLimit) : answers;
                outRow.put("answers", sampled);
            } else {
                List<String> topChoices = new ArrayList<>();
                for (Map.Entry<String, Object> e : answersMap.entrySet()) {
                    String k = e.getKey();
                    if ("others".equals(k)) continue;
                    Object v = e.getValue();
                    if (v instanceof Number n && n.longValue() == maxVal) {
                        topChoices.add(k);
                    }
                }
                String topChoice = topChoices.isEmpty() ? "" : topChoices.get(0);
                outRow.put("topChoices", topChoice);
                outRow.put("count", maxVal.intValue());
            }

            out.add(outRow);
        }

        return om.writeValueAsString(out);
    }

    private static String firstChoice(Object v) {
        if (v == null) return "";
        if (v instanceof List<?> list) {
            for (Object o : list) {
                return String.valueOf(o);
            }
            return "";
        }
        if (v instanceof String s) return s;
        return String.valueOf(v);
    }

    private static List<String> toStringList(Object obj) {
        if (!(obj instanceof List<?> list)) return List.of();
        List<String> out = new ArrayList<>(list.size());
        for (Object v : list) out.add(String.valueOf(v));
        return out;
    }

    private static List<String> sampleUpToRandom(List<String> src, int limit) {
        if (src == null || src.isEmpty()) return List.of();
        if (limit <= 0 || src.size() <= limit) return new ArrayList<>(src);
        List<String> copy = new ArrayList<>(src);
        Collections.shuffle(copy);
        return new ArrayList<>(copy.subList(0, limit));
    }
}