package dev.learning.recordings;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LearningPriorityTest {
    private final Clock lessonClock = Clock.fixed(Instant.parse("2026-09-17T09:00:00Z"), ZoneOffset.UTC);

    @Test
    void marks_a_recording_overdue_when_the_learner_deadline_has_passed() {
        CourseRecording note = new CourseRecording(
                "rec-algebra-17", "algebra-101", "learner-42", "VOICE_NOTE",
                "I am still unsure how to factor the final expression.",
                Instant.parse("2026-09-15T08:00:00Z"),
                Instant.parse("2026-09-16T23:59:00Z"), true);

        assertThat(LearningPriority.forRecording(note, lessonClock)).isEqualTo("OVERDUE");
    }
}
