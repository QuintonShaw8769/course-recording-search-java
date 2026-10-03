package dev.learning.recordings;

import java.time.Clock;

public final class LearningPriority {
    private LearningPriority() {}

    public static String forRecording(CourseRecording recording, Clock clock) {
        if (recording.learnerDeadline() == null) return "NO_DEADLINE";
        return recording.learnerDeadline().isBefore(clock.instant()) ? "OVERDUE" : "UPCOMING";
    }
}
