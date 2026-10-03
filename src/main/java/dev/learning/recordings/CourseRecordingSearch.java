package dev.learning.recordings;

import com.fasterxml.jackson.databind.JsonNode;
import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.EmbeddingCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseRecordingSearch {
    private final OpenAIClient ai;
    private final InfraiVectorClient vectors;
    private final Clock clock;

    @Autowired
    public CourseRecordingSearch(OpenAIClient ai, InfraiVectorClient vectors) {
        this(ai, vectors, Clock.systemUTC());
    }

    CourseRecordingSearch(OpenAIClient ai, InfraiVectorClient vectors, Clock clock) {
        this.ai = ai;
        this.vectors = vectors;
        this.clock = clock;
    }

    public void prepareCollection() {
        vectors.createCollection();
    }

    public JsonNode index(CourseRecording recording) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("course_id", recording.courseId());
        metadata.put("learner_id", recording.learnerId());
        metadata.put("source_type", recording.sourceType());
        metadata.put("recorded_at", recording.recordedAt().toString());
        metadata.put("learner_deadline", recording.learnerDeadline() == null ? "" : recording.learnerDeadline().toString());
        metadata.put("learning_priority", LearningPriority.forRecording(recording, clock));
        metadata.put("educator_visible", recording.educatorVisible());
        metadata.put("transcript", recording.transcript());
        return vectors.upsert(recording.recordingId(), embed(recording.transcript()), metadata);
    }

    public JsonNode search(String courseId, String question, int topK) {
        return vectors.query(embed(question), courseId, topK);
    }

    private List<Float> embed(String text) {
        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                .model("auto")
                .input(text)
                .build();
        return ai.embeddings().create(params).data().getFirst().embedding();
    }
}
