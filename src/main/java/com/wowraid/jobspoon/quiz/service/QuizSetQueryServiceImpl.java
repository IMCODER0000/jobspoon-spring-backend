package com.wowraid.jobspoon.quiz.service;

import com.wowraid.jobspoon.quiz.entity.QuizChoice;
import com.wowraid.jobspoon.quiz.entity.QuizQuestion;
import com.wowraid.jobspoon.quiz.entity.enums.QuestionType;
import com.wowraid.jobspoon.quiz.entity.enums.QuizPartType;
import com.wowraid.jobspoon.quiz.repository.QuizChoiceRepository;
import com.wowraid.jobspoon.quiz.repository.QuizQuestionRepository;
import com.wowraid.jobspoon.quiz.repository.QuizSetRepository;
import com.wowraid.jobspoon.quiz.service.response.ChoiceQuestionRead;
import com.wowraid.jobspoon.quiz.service.response.InitialsQA;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSetQueryServiceImpl implements QuizSetQueryService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizChoiceRepository quizChoiceRepository;
    private final QuizSetRepository quizSetRepository;
    private final EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public List<ChoiceQuestionRead> findChoiceQuestionsBySetId(Long setId) {

        // 1) 세트의 CHOICE 문항(정렬 보장)
        List<QuizQuestion> questions =
                quizQuestionRepository.findByQuizSetIdAndQuestionTypeOrderByOrderIndexAscIdAsc(
                        setId, QuestionType.CHOICE
                );
        if (questions.isEmpty()) return List.of();

        // 2) 보기를 한 번에 로드(정렬: id ASC)
        List<Long> qIds = questions.stream().map(QuizQuestion::getId).toList();
        List<QuizChoice> choices = quizChoiceRepository.findByQuizQuestionIdInOrderByIdAsc(qIds);

        Map<Long, List<QuizChoice>> byQ = choices.stream()
                .collect(Collectors.groupingBy(c -> c.getQuizQuestion().getId(),
                        LinkedHashMap::new, Collectors.toList()));

        // 3) DTO 매핑 (answerIndex 1-based → 0-based 보정)
        List<ChoiceQuestionRead> out = new ArrayList<>(questions.size());
        for (QuizQuestion q : questions) {
            List<QuizChoice> cs = byQ.getOrDefault(q.getId(), List.of());

            List<String> choiceTexts = cs.stream()
                    .map(c -> Optional.ofNullable(c.getChoiceText()).orElse(""))
                    .toList();

            int correctIdx0;
            Integer ans1 = q.getAnswerIndex(); // 1-based 가정
            if (ans1 != null && ans1 >= 1 && ans1 <= choiceTexts.size()) {
                correctIdx0 = ans1 - 1;
            } else {
                // 안전장치: answer=true 첫 번째를 정답으로
                int idx = 0;
                for (int i = 0; i < cs.size(); i++) {
                    if (Boolean.TRUE.equals(cs.get(i).isAnswer())) { idx = i; break; }
                }
                correctIdx0 = Math.min(Math.max(0, idx), Math.max(0, choiceTexts.size() - 1));
            }

            String explanation = null;
            if (correctIdx0 >= 0 && correctIdx0 < cs.size()) {
                explanation = cs.get(correctIdx0).getExplanation();
            }

            out.add(new ChoiceQuestionRead(
                    q.getId(),
                    Optional.ofNullable(q.getQuestionText()).orElse(""),
                    choiceTexts,
                    correctIdx0,
                    explanation
            ));
        }
        return out;
    }

    @Override
    public List<Long> findQuestionIdsBySetId(Long setId) {
        return quizSetRepository.findQuestionIdsBySetId(setId);
    }

    @Override
    public Optional<QuizPartType> findPartTypeBySetId(Long setId) {
        List<QuizPartType> r = em.createQuery(
                "select qs.partType from QuizSet qs where qs.id = :id",
                QuizPartType.class
        ).setParameter("id", setId).getResultList();
        return r.stream().findFirst();
    }

    @Override
    public List<QuizQuestion> findInitialsQuestionsBySetId(Long setId) {
        return quizQuestionRepository.findByQuizSet_IdOrderByOrderIndexAscIdAsc(setId);
    }

}
