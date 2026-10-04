package com.mailassistant.repository;

import com.mailassistant.domain.model.*;
import java.util.List;

public interface ExerciseRepository {
    List<Chapter> chapters() throws Exception;
    List<Exercise> findByChapter(long chapterId) throws Exception;
}
