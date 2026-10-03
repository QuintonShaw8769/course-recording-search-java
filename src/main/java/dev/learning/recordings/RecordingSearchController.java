package dev.learning.recordings;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/recordings")
public class RecordingSearchController {
    private final CourseRecordingSearch search;

    public RecordingSearchController(CourseRecordingSearch search) { this.search = search; }

    @PostMapping("/collection")
    public Map<String, String> prepareCollection() {
        search.prepareCollection();
        return Map.of("collection", "ready");
    }

    @PostMapping
    public JsonNode index(@RequestBody CourseRecording recording) { return search.index(recording); }

    @PostMapping("/search")
    public JsonNode search(@RequestBody SearchRequest request) {
        return search.search(request.courseId(), request.question(), request.topK());
    }

    @ExceptionHandler(InfraiRejectedException.class)
    ResponseEntity<JsonNode> rejected(InfraiRejectedException error) {
        int status = error.status() >= 400 && error.status() < 500 ? error.status() : 502;
        return ResponseEntity.status(status).body(error.error());
    }

    public record SearchRequest(String courseId, String question, int topK) {}
}
