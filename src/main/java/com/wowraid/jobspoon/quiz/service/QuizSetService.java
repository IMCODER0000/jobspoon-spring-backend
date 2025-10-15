package com.wowraid.jobspoon.quiz.service;

import com.wowraid.jobspoon.quiz.service.request.CreateQuizSessionRequest;
import com.wowraid.jobspoon.quiz.service.request.CreateQuizSetByCategoryRequest;
import com.wowraid.jobspoon.quiz.service.request.CreateQuizSetByFolderRequest;
import com.wowraid.jobspoon.quiz.service.response.BuiltQuizSetResponse;
import com.wowraid.jobspoon.quiz.service.response.CreateQuizSessionResponse;
import com.wowraid.jobspoon.quiz.service.response.CreateQuizSetByCategoryResponse;

public interface QuizSetService {
    // 카테고리 기반 퀴즈 자동 생성
    CreateQuizSetByCategoryResponse registerQuizSetByCategory(CreateQuizSetByCategoryRequest request);

    // 세트 생성 + 문항 ID까지 만들어서 반환
    BuiltQuizSetResponse registerQuizSetByCategoryReturningQuestions(CreateQuizSetByCategoryRequest request);
    BuiltQuizSetResponse registerQuizSetByFolderReturningQuestions(CreateQuizSetByFolderRequest request);

    // 즐겨찾기 용어 기반 퀴즈 자동 생성
    CreateQuizSessionResponse registerQuizSetByFavorites(CreateQuizSessionRequest request);
}
