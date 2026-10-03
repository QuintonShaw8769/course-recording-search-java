#!/usr/bin/env bash
set -euo pipefail

curl --fail-with-body --request POST http://localhost:8080/api/recordings/collection
curl --fail-with-body --request POST http://localhost:8080/api/recordings \
  --header 'Content-Type: application/json' \
  --data '{"recordingId":"rec-algebra-17","courseId":"algebra-101","learnerId":"learner-42","sourceType":"VOICE_NOTE","transcript":"I am still unsure how to factor the final expression.","recordedAt":"2026-09-15T08:00:00Z","learnerDeadline":"2026-09-16T23:59:00Z","educatorVisible":true}'
curl --fail-with-body --request POST http://localhost:8080/api/recordings/search \
  --header 'Content-Type: application/json' \
  --data '{"courseId":"algebra-101","question":"Who needs help with factoring?","topK":3}'
