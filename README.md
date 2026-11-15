# Do you like our owl?

This is an experiment in using [Spring AI](https://spring.io/projects/spring-ai) to let a language model
talk to real data. A local model, served by LM Studio over its OpenAI-compatible API, gets a single tool
that runs read-only SQL against March 2025 Dutch railway data loaded into DuckDB — 1.9 million rows of
stops, services and delays. Nothing is pre-computed and nothing is hard-coded into the prompt: the model
sees the table schema, writes its own queries, and answers in plain language. You can talk to it from an
interactive terminal (Spring Shell) or over a small HTTP API, whichever you prefer.

## Quickstart

**Prerequisites**

- Java 25
- [LM Studio](https://lmstudio.ai/) with a chat model loaded and its local server running
  (default: `http://192.168.0.136:1234/v1`)

**1. Point the app at your model server** (skip if the default already matches):

```bash
export LIKEOUROWL_AI_BASE_URL=http://localhost:1234/v1
```

The URL must include `/v1`. The model name is set in `src/main/resources/application.properties`
(`spring.ai.openai.chat.options.model`, default `qwen/qwen3.8-27b`) — change it to whatever
`GET /v1/models` reports.

**2. Start the terminal chat:**

```bash
make start
```

On first start the railway data is downloaded into `data/likeourowl.duckdb` (about 2 seconds, 36 MB);
later starts reuse it. Then just type a question at the prompt:

```
likeourowl> how many stops in total?
owl> There were 1,902,543 stops in March 2025.

likeourowl> which station had the highest average maximum delay?
owl> ...
```

Anything that is not a command is sent to the model. `ask <question>` does the same as one explicit
turn, `chat` starts a conversation with follow-up context, `forget` clears it, and `help` lists
everything. `quit` exits.

**3. Or use the web API:**

```bash
./mvnw spring-boot:run
```

```bash
curl -s -X POST http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"how many stops did Utrecht Centraal have?"}'
```

`POST /api/chat` returns `{"reply":"..."}`; `POST /api/chat/stream` streams the answer as
server-sent events.
