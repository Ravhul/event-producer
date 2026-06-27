# CLAUDE.md — event-producer

Guidance for Claude Code when working in this repository.

## What this project is
`event-producer` is **one of 6 standalone Spring Boot microservices** that make up
the *Insurance Claims Pipeline*, an event-driven architecture learning project.
This repo is fully self-contained: its own `build.gradle`, `main()`, `application.yml`,
port, Kafka consumer group, and git history. It is **not** part of a multi-module repo.

This service sits at the **head of the pipeline** — it is the *producer*. Its job
(by the end of Phase 2) is a Spring Batch job that reads source claims and publishes
them to the `raw-claims` Kafka topic. Downstream services (data-cleansing,
client-identification, validation-rule, financial, db-load) live in their own repos.

Pipeline: `event-producer → raw-claims → data-cleansing → cleansed-claims → ... → db-load → MySQL`


## Tech stack
- Java 21 (Gradle toolchain), Spring Boot 3.5.x, Gradle (Groovy DSL)
- Spring for Apache Kafka (`KafkaTemplate` + `@KafkaListener`) — fundamentals first;
  a switch to Spring Cloud Stream is planned for LATER (keep seams clean for it).
- Spring Batch (metadata persisted to MySQL)
- MySQL (schema `claims_pipeline`) via `mysql-connector-j`
- Apache Kafka 3.9.0, local KRaft binary (no Docker) at `~/Desktop/kafka`,
  broker on `localhost:9092`, data dir `~/Desktop/kafka-logs`

## Layout & conventions
- Base package: `com.insurance.eventproducer`
- `publisher/ClaimPublisher` — the single seam for producing to Kafka (wraps
  `KafkaTemplate`, async `.whenComplete` callback logging partition/offset). Keep all
  publishing here so the later Spring Cloud Stream switch touches one place.
- `controller/ClaimController` — REST entry (`@RequestMapping("/claims")`).
- `consumer/ClaimConsumer` — `@KafkaListener` (hello-world verification).
- **Topic names** live in a Java constants class — `constants/ClaimsConstants`
  (`public static final` fields, e.g. `RAW_CLAIM_TOPIC = "raw-claims"`), referenced
  via `import static`. This OVERRIDES the earlier `app.topic.*` + `@Value` decision
  (user chose constants in Phase 2; trade-off accepted = no per-env override without
  recompile). Do NOT reopen. The old `app.topic` YAML block + `@Value` reads were removed.
- Config style is LOCKED to **properties-based auto-config** (`spring.kafka.*` in
  `application.yml`). No hand-written `@Configuration` Kafka factory beans. Do not reopen.
- Payloads are **Java records** (immutable event contracts). Kafka value serializer
  is **JSON** (`JsonSerializer`); key serializer is `String`.
- **`model/Claim`** is the raw pharmacy-claim contract (PBM domain). Fields:
  `claimSk` (String, unique surrogate id), `rxNbr`, `fillNbr`, `locNbr`, `pdRvInd`
  (String; `"1"`=sold, `"-1"`=reversal), `binNbr`, `pcn`, `plnId`, `hrchySk`, `ndc`,
  `ndcSk`, `amount` (**BigDecimal**), `claimCreatedTs` (**Instant**, UTC).
- **Kafka message key = natural key** `rxNbr + "-" + fillNbr + "-" + locNbr` (NOT
  `claimSk`). Stable across a sold/reversal pair so both land on the same partition
  and stay ordered. `claimSk` is the per-record unique id (dedup is downstream).
- **Contract design = per-topic payload types** (architecture chosen): the raw `Claim`
  carries only SOURCE fields the store knows; derived fields (resolved client name,
  approvedAmount, status, reversalIndicator) are produced by DOWNSTREAM services, each
  emitting its own richer type (e.g. `CleansedClaim` in Phase 3). Do NOT put derived
  fields on the raw `Claim`. The old `ClaimType`/`ClaimStatus` enums are now orphaned.

## Build & run
```bash
./gradlew build           # compile + test
./gradlew bootRun         # run the app (port 8081)
```
Prereqs to run: Kafka broker up on :9092, MySQL up on :3306 with schema `claims_pipeline`.

Start Kafka (separate terminal, from ~/Desktop/kafka):
```bash
bin/kafka-server-start.sh config/kraft/server.properties
```

## Database notes
- Schema: `claims_pipeline` on `localhost:3306`. Holds Spring Batch `BATCH_*` tables now;
  will hold `claims` (Phase 7) and `failed_claims` (Phase 8) later.
- `spring.batch.jdbc.initialize-schema` is set to `never` because the `BATCH_*` tables
  are already provisioned (persistent DB → provision once, don't recreate each boot).
  Framework-owned tables (`BATCH_*`) are created by Spring Batch's bundled DDL — never
  hand-write them. Application-owned tables are designed/created per phase.

## Current status
- **Phase 1 (Foundations): DONE** — hello-world producer → topic → consumer verified.
- **Phase 2 (EventProducer Batch): IN PROGRESS** — MySQL + Spring Batch wired up
  (`BATCH_*` tables created); `Claim` contract designed; JSON serializer + `raw-claims`
  topic done; `ClaimPublisher` publishes keyed by natural key. `config/BatchConfig`
  has `claimReader()` (`ListItemReader` of 4 sample claims). Next: `claimWriter()`
  (`ItemWriter` looping the chunk → `publisher.publish`), then the Step + Job, then
  run and verify claims land on `raw-claims`.
