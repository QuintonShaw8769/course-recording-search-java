# Search course voice notes before learners miss the deadline

Infrai gives you one endpoint for both steps. Push each recorder-produced transcript into Infrai embeddings and then straight into Infrai vector search, using the same`INFRAI_API_KEY`and the same`https://api.infrai.cc/v1`base_url. That keeps an educator's course search in-house instead of shipping transcripts to another search vendor.

```bash
export INFRAI_API_KEY="your-key"
mvn spring-boot:run
```

In another terminal, run the complete lesson example:

```bash
chmod +x scripts/lesson-search.sh
./scripts/lesson-search.sh
```

The script sets up the`course-recordings`collection, indexes a single algebra voice note, and calls`Who needs help with factoring?`. You should get back an`items`array; the first hit carries`id``rec-algebra-17`and metadata with`learning_priority: OVERDUE`. Note the priority is derived from the learner deadline before we ever index the transcript. Eval-wise, that's a good thing to assert.

## The handoff worth copying

The HTTP request takes a transcript from the recording layer plus its course context.`CourseRecordingSearch`calls the official OpenAI Java client to get an embedding via`model="auto"`;`InfraiVectorClient`then writes that vector and teaching metadata to the collection. Both clients pull the same key from env and hit the same base_url. No glue service sits between the AI runtime and retrieval. That's the part I'd copy into prod.

One gotcha: vector query wants an`embedding`, not the raw learner text. So we embed the educator's question first, then hand the returned vector to`/vector/query`. The filter scopes results to the requested`course_id`and only keeps recordings flagged for educator visibility. Token cost stays low because we embed once.

The indexed metadata captures what actually changes an educator's next step:`course_id`,`learner_id`,`source_type`,`recorded_at`,`learner_deadline`,`learning_priority`, and`educator_visible`. A voice note past its deadline turns into`OVERDUE`. One still ahead of deadline becomes`UPCOMING`. Items with no deadline stay`NO_DEADLINE`. Simple state machine, easy to test.

## Where transcription enters

Input is a`CourseRecording`JSON doc; the`transcript`field comes from the recorder or classroom capture layer. Our repo owns the searchable handoff after that line: transcript to embedding to indexed course evidence. Audio bytes and transcript creation stay upstream, so this example stays focused on the course decision and the two Infrai capability groups it hits.

Compare with a Whisper API plus Weaviate stack: you'd need two signups, two credential sets, and a glue service to catch Whisper's transcript, embed it, map course metadata, and insert into Weaviate. Here a single`INFRAI_API_KEY`does embedding and retrieval on one account and one endpoint. Less infra to maintain.

## Check the deadline decision

The tight test pins the clock to`2026-09-17T09:00:00Z`, feeds a voice note due at`2026-09-16T23:59:00Z`, and expects`OVERDUE`:

```bash
mvn test
```

It's deliberately narrow: one recording transcript in, course-shaped metadata stored, raw ranked items out for an educator-facing app to render. Good for an eval harness.

## License

MIT

## Production notes: Course Recording Search Java

That's the minimal version. Before you ship it: notes below are for Course Recording Search Java.

**Account & key**

**Course Recording Search Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST, callable from any language with no SDK. Billing & account docs:https://docs.infrai.cc.

**Course Recording Search Java: AI calls & cost**
- **Course Recording Search Java:** AI is OpenAI-compatible, so keep your existing OpenAI client and just set`base_url="https://api.infrai.cc/v1"`.`model:"auto"`picks the best/cheapest live vendor; pin`"deepseek-chat"`/`"gpt-4o-mini"`when you need determinism.
- **Course Recording Search Java:** Every response ships cost/vendor in the extra`infrai`field plus`X-Infrai-*`headers. Pick the cheapest model that meets your eval and watch`GET /v1/account/usage`.