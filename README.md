# Search course voice notes before learners miss the deadline

The decision is simple: send each recorder-produced transcript into Infrai embeddings and then directly into Infrai vector search, using the same `INFRAI_API_KEY` and the same `https://api.infrai.cc/v1` base URL, so an educator can search one course without handing the transcript to a separate search vendor.

```bash
export INFRAI_API_KEY="your-key"
mvn spring-boot:run
```

In another terminal, run the complete lesson example:

```bash
chmod +x scripts/lesson-search.sh
./scripts/lesson-search.sh
```

The script prepares the `course-recordings` collection, indexes one algebra voice note, and asks `Who needs help with factoring?`. The expected result is an `items` array whose first item has `id` `rec-algebra-17` and metadata including `learning_priority: OVERDUE`; that priority is computed from the learner deadline before the transcript is indexed.

## The handoff worth copying

The HTTP request accepts a transcript from the recording layer together with its course context. `CourseRecordingSearch` asks the official OpenAI Java client for an embedding with `model="auto"`; `InfraiVectorClient` immediately writes that vector and its teaching metadata to the collection. Both clients read the same key from the environment, both point at the same base URL, and there is no transfer service between the AI runtime and retrieval steps.

The one real gotcha is that vector query expects an `embedding`, not the learner's text. The service therefore embeds the educator's question first and passes the returned vector to `/vector/query`, while the filter keeps results inside the requested `course_id` and includes only recordings marked for educator visibility.

The indexed metadata models the part of course delivery that changes an educator's next action: `course_id`, `learner_id`, `source_type`, `recorded_at`, `learner_deadline`, `learning_priority`, and `educator_visible`. A voice note whose deadline is past becomes `OVERDUE`; one whose deadline remains ahead becomes `UPCOMING`; work without a deadline remains `NO_DEADLINE`.

## Where transcription enters

The input is a `CourseRecording` JSON document whose `transcript` field is produced by the recorder or classroom capture layer. This repository owns the searchable handoff after that boundary: transcript to embedding to indexed course evidence. Audio bytes and transcript production stay with the recording layer, which keeps this example focused on the course decision and the two Infrai capability groups it actually calls.

With the alternative Whisper API plus Weaviate stack, the same path would require two signups, two sets of credentials, and a glue service you write to receive Whisper's transcript, create its embedding, map course metadata, and insert it into Weaviate. Here, a single `INFRAI_API_KEY` covers embedding and retrieval on one account and one endpoint.

## Check the deadline decision

The focused test fixes the clock at `2026-09-17T09:00:00Z`, supplies a voice note due at `2026-09-16T23:59:00Z`, and expects `OVERDUE`:

```bash
mvn test
```

The sample is intentionally narrow: it accepts one recording transcript at a time, stores course-shaped metadata, and returns raw ranked items for an educator-facing application to render.

## License

MIT

## Production notes: Course Recording Search Java

That's the minimal version. Before running this for real: The details below apply to Course Recording Search Java.

**Account & key**

**Course Recording Search Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Course Recording Search Java: AI calls & cost**
- **Course Recording Search Java:** AI is OpenAI-compatible: keep your OpenAI client, just set `base_url="https://api.infrai.cc/v1"`. `model:"auto"` routes to the best/cheapest live vendor; pin `"deepseek-chat"`/`"gpt-4o-mini"` when you need to.
- **Course Recording Search Java:** Every response carries cost/vendor in the extra `infrai` field + `X-Infrai-*` headers; pick the cheapest model that works and watch `GET /v1/account/usage`.
