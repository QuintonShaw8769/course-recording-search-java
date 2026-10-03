package dev.learning.recordings;

import java.time.Instant;

public record CourseRecording(
        String recordingId,
        String courseId,
        String learnerId,
        String sourceType,
        String transcript,
        Instant recordedAt,
        Instant learnerDeadline,
        boolean educatorVisible
) {}
