---
name: spring-boot-api
description: MUST be loaded before writing or changing any backend code in apps/api of JavniRadar (Spring Boot 4, Java 25, JPA, PostgreSQL). Trigger on any implementation request that touches the backend, including Croatian ones such as "implementiraj", "napravi", "dodaj endpoint", "napiši ingest / parser / pravilo", "popravi bug u API-ju", or any backend task from docs/MVP_ROADMAP.md (ingest, normalizacija, pravila C/D/F/B/G, AI procjena, REST API, deploy). Covers endpoints, services, entities, repositories, SQL/migrations, external API clients (data.gov.hr CKAN, JPT, Sudski registar, OCDS, Anthropic), indicator rules, AI assessment, error handling and tests.
---

# Spring Boot API (`apps/api`)

Read first: `docs/ARCHITECTURE.md`, `docs/DATA_MODEL.md`, `docs/ANOMALY_DETECTION.md` and `AGENTS.md`. If they disagree with this skill, the docs win; fix whichever is wrong and note it in `work/decisions.md`.

Goal: code that works for the demo by 9 Nov (feature freeze 7 Nov 20:00), that the whole team can read without explanation, and that the jury can inspect (criteria "Feasibility" and "Technical architecture"). Simple and consistent beats clever.

## Stack (verified in `pom.xml`)
- Spring Boot 4.1, Java 25, `spring-boot-starter-webmvc` (blocking MVC, NOT WebFlux), Data JPA, Validation, Lombok, PostgreSQL 17 in Docker (`localhost:5434`, container `javniradar-postgres`, own compose project so it never shares a volume with `Desktop/datathon`).
- `open-in-view: false` is intentional. The schema in `docs/DATA_MODEL.md` relies on CHECK constraints, `pg_trgm` and JSONB, which `ddl-auto: update` cannot create, so `docs/MVP_ROADMAP.md` proposes **Flyway** (`V1__init.sql`); confirmed 2026-11-02 in `work/decisions.md`.
- Boot 4 uses **Jackson 3** (packages `tools.jackson.*`, not `com.fasterxml.jackson.databind`). Annotations like `@JsonProperty` are still in `com.fasterxml.jackson.annotation`. If unsure about an import, check what is on the classpath; don't guess.
- Do not add dependencies to `pom.xml` without a short justification to the user (what, why, alternative without the dependency).

## Structure: package by feature, not by layer
```
com.datathon.api
├── ApiApplication.java
├── common/              # GlobalExceptionHandler, ErrorResponse, NotFoundException, config
├── contract/            # one feature = one package
│   ├── ContractController.java
│   ├── ContractService.java
│   ├── ContractRepository.java
│   ├── Contract.java               # JPA entity
│   └── dto/
│       ├── ContractResponse.java
│       └── ContractFilter.java
├── buyer/  supplier/  indicator/  assessment/  source/  feedback/
├── ingest/              # discovery, download (PoliteHttpClient), parsers, normalization, matching
├── rules/               # deterministic indicator rules, one class per rule, versioned
└── integration/
    ├── ckan/  jpt/  sudreg/  ocds/   # one client per external API + its DTOs
    └── anthropic/       # LLM client, tool schemas, validator, usage
```
- **All code is in English**: packages, classes, methods, fields, DB tables/columns, JSON fields, URL paths. Translate domain terms using the glossary below so the whole team uses the same words.
## Domain glossary (Croatian → code)
| Croatian | Code (English) |
|---|---|
| naručitelj (javni naručitelj) | `Buyer` (an `Organization`) |
| dobavljač / ugovaratelj / izvođač | `Supplier` (an `Organization`) |
| podugovaratelj | `Subcontractor` |
| organizacija | `Organization`, type `OrganizationType`: `LOCAL_GOVERNMENT`, `COUNTY`, `PUBLIC_BODY`, `LLC` (d.o.o.), `SIMPLE_LLC` (j.d.o.o.), `JOINT_STOCK` (d.d.), `CRAFT` (obrt), `ASSOCIATION`, `NATURAL_PERSON`, `FOREIGN`, `OTHER`, `UNKNOWN` |
| JLS / grad / općina | `Municipality`, kind `MunicipalityKind`: `CITY`, `MUNICIPALITY`, `CITY_OF_ZAGREB` |
| županija | `county` |
| usporedna skupina | `peerGroup` (`P1`..`P7`) |
| broj stanovnika (Popis 2021) | `population` |
| ugovor | `Contract` |
| izmjena ugovora | `ContractAmendment` |
| isplata / isplaćeni iznos | `Payment` / `paidAmount` |
| plan nabave / stavka plana | `ProcurementPlan` / `PlanItem` |
| postupak nabave / grupa (lot) / ponuda | `Procedure` / `Lot` / `Bid` |
| vrsta postupka | `procedureType` (original text) + `ProcedureScope`: `PUBLIC`, `SIMPLE` (jednostavna), `EXEMPT` (izuzeto), `UNKNOWN` |
| predmet nabave / vrsta predmeta | `subject` / `SubjectType`: `GOODS`, `SERVICES`, `WORKS`, `OTHER` |
| CPV | `cpv` |
| okvirni sporazum | `isFrameworkBased` |
| iznos početni / izmjena / ukupni (s izmjenama) | `initialValue` / `amendmentsValue` / `totalValue` (EUR, net of VAT, `BigDecimal`); gross variants `...Gross` |
| datum sklapanja / izvršenja | `dateSigned` / `completedOn` |
| Sudreg snimak | `CompanySnapshot` (`foundedOn`, `mainActivityCode`, `legalForm`, `inBankruptcy`) |
| djelatnost (NKD) | `activityCode` (NKD) |
| OIB | `oib` (proper Croatian identifier, keep as is) |
| javnopravno tijelo (JPT): oznaka, osnivači | `jptCode`, `founders` on `Organization` |
| indikator (pravilo) | `Indicator`; `ruleCode`: `C_CONCENTRATION`, `D_VALUE_INCREASE`, `D_OVERPAYMENT`, `F_SPLITTING`, `B_YOUNG_COMPANY`, `G_DECEMBER`; `ruleVersion` |
| kvaliteta podataka / veličina odstupanja / pouzdanost dokaza | `dataQuality` / `magnitude` / `evidenceStrength`: three separate fields, **never a combined score** |
| AI procjena | `AiAssessment` (`label`: `NO_FLAG`, `WORTH_CHECKING`, `HIGH_ATTENTION`) |
| dokaz / izvor / provenancija | `Evidence` / `Source` / `Provenance` (`extractionMethod`: `PARSER`, `LLM`, `MANUAL`, `API`; `confidence`) |
| izvorna datoteka / izvorni redak | `SourceFile` / `RawRecord` (`locator`) |
| ispravak / pravo na odgovor | `Feedback` (`FeedbackKind`) |
| pokretanje dohvaćanja / problem pri uvozu | `IngestRun` / `IngestIssue` |
| potrošnja LLM-a (tokeni, cijena) | `LlmUsage` |
| sintetički podaci | `isSynthetic` |

The DB uses snake_case of the same words (`date_signed`), as in `docs/DATA_MODEL.md`. The AI tool schema keeps the Croatian keys required by the brief (`oznaka`, `razlozi`, `nedostajuci_podaci`, `moguca_legitimna_objasnjenja`, `pouzdanost`); map them to English record fields with `@JsonProperty`.
Add new terms here before using them in code.

- Don't create `service/impl/` + an interface for every service. Use an interface only when there really are two implementations (e.g. `RestSudregClient` and `FixtureSudregClient`).

## Layers and responsibilities
| Layer | May | Must not |
|---|---|---|
| `@RestController` | receive request, `@Valid`, call one service, return a DTO | business logic, call repositories, return entities |
| `@Service` | business logic, `@Transactional`, map entity → DTO, call repositories and clients | know about HTTP (`HttpServletRequest`, `ResponseEntity`) |
| `Repository` (`JpaRepository`) | derived queries, `@Query` (JPQL), `Specification` for filters | logic |
| `integration/*` client | HTTP call to an external API, map their JSON to our DTO | write to the database |

## Class rules
- **Constructor injection only**: `@RequiredArgsConstructor` + `private final` fields. Never field `@Autowired`.
- **DTOs are `record`s**, with Bean Validation annotations on input DTOs:
  ```java
  public record ContractFilter(
          @Size(max = 200) String q,
          @Pattern(regexp = "\\d{11}") String supplierOib,
          @Pattern(regexp = "\\d{2,8}") String cpv,
          @Min(0) Integer page) {}
  ```
- **Entities**: `@Getter @Setter @NoArgsConstructor` (protected constructor is fine). **Never `@Data` or `@EqualsAndHashCode` on an entity** (lazy loading, infinite `toString`). Relations `@ManyToOne(fetch = LAZY)`; `@OneToMany` only when really needed.
- ID: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id`. For data from external sources also add a natural key (`source` + `externalId`) with a unique constraint so re-imports don't create duplicates.
- Enums: `@Enumerated(EnumType.STRING)`, never ORDINAL.
- Money: `BigDecimal`; dates: `LocalDate` / `Instant`. Never `double` for amounts, never `java.util.Date`.

## Writing methods
- **One method = one thing.** If the name needs "And" (`fetchAndSave`), split it.
- Short: aim for ~25 lines max. Longer? Extract a private method whose name explains *why*, not *how*.
- **Guard clauses first**, no deep nesting:
  ```java
  public ContractResponse getById(Long id) {
      Contract contract = contractRepository.findById(id)
              .orElseThrow(() -> new NotFoundException("Ugovor " + id + " ne postoji")); // user-facing message stays Croatian
      return ContractResponse.from(contract);
  }
  ```
- Names: verb for actions (`findByBuyer`, `importContractRegister`, `evaluateSplitting`), `is/has` for booleans. No abbreviations (`nat`, `res`, `tmp`).
- Never return `null` from public methods: `Optional` for "may be absent", empty list for "no results".
- Parameters: 3-4 max. More than that → a `record` (e.g. `ContractFilter`).
- Entity → DTO mapping as a static method on the DTO (`ContractResponse.from(entity)`). No MapStruct until there's a real need.
- Use Java 25 where it makes code clearer: `record`, `switch` with pattern matching, `var` only when the type is obvious from the right-hand side. Don't overdo streams: if a stream has more than ~4 steps, a plain `for` is more readable.
- Comments explain **why** (e.g. "the same EOJN contract appears in several yearly files, hence dedup by contract URL"), not *what* the code does. A service's public API may have a short Javadoc when the name isn't enough.

## REST conventions
- Public routes under **`/api/v1`**. The frontend `axios` has `baseURL: '/api'`, so frontend paths start with `/v1/...`. Example: `@RequestMapping("/api/v1/contracts")`. Admin routes go under `/api/admin` and need `X-Admin-Token`. The endpoint list is in `docs/ARCHITECTURE.md` §5.
- Resources plural, lowercase, hyphens: `/api/v1/contracts`, `/api/v1/buyers/{oib}`, `/api/v1/buyers/{oib}/peers`.
- List `GET`s always support pagination (`page`, `size`, default `size=20`, max 100) and return a page-like DTO (`content`, `totalElements`, `page`, `size`), not a raw Spring `Page` (its JSON is not stable).
- Statuses: 200 read, 201 create (+ `Location`), 204 delete, 400 validation, 404 not found, 502 when an external source is down.
- JSON fields camelCase, dates ISO-8601 (Jackson handles `LocalDate` by itself).

## Errors: one shape for the whole API
The frontend (`apps/web/src/types/ErrorResponse.type.ts`) expects `{ statusCode, message, error }` and displays `message`. Therefore:
- `common/ErrorResponse.java`: `public record ErrorResponse(int statusCode, String message, String error) {}`
- `common/GlobalExceptionHandler.java` (`@RestControllerAdvice`) maps: `NotFoundException` → 404, `MethodArgumentNotValidException` → 400 (message = first field + reason), `ExternalSourceException` → 502, everything else → 500 with a generic message (details only in the log).
- **`message` is in Croatian** and understandable to end users, because it is shown in the UI. Stack traces never go into the response.
- Don't catch `Exception` in a service just to swallow it. Either handle it meaningfully or let the handler deal with it.

## External APIs (data.gov.hr CKAN, JPT, Sudski registar, OCDS, Anthropic)
This is the core of the "Use of open data" criterion, so it must be reliable and visible in the code.
- Client: Spring `RestClient` (or an `@HttpExchange` interface) in `integration/<source>/`, one client per source. **Timeouts are mandatory** (connect ~5 s, read ~30 s).
- Base URL and keys from `application.yaml` via `${ENV_VAR}` and a `@ConfigurationProperties` record. **Keys (Sudreg OAuth, ANTHROPIC_API_KEY) never in code or in a commit**; locally in `.env` / environment variables.
- Map their JSON to a dedicated `record` inside the client package, and let only our domain DTO into the rest of the app. Foreign field names don't leak into services.
- Wrap external source errors in `ExternalSourceException` with the source name. One failing source must not break the whole import: log, skip, continue.
- Be polite: all downloads go through `PoliteHttpClient` (robots.txt, ~1 request/s per host, retry, disk cache), with `User-Agent` `JavniRadar/0.1 (studentski projekt)`. **Never scrape eojn.hr** (robots.txt `Disallow: /`). Never bypass logins or CAPTCHAs.
- Verify a source with a real request before integrating it, and record the result in `docs/DATA_SOURCES.md`. Never invent endpoints or field names.
- For the demo: store import results in the database and read from the database. The demo must not depend on someone else's server being up while recording the video.
- Reused code is copied from repo `Desktop/datathon`: `PoliteHttpClient`, `RobotsRules`, `DiskCache`, the Sudreg clients, `AmountParser`, `CroatianDates` and AI usage tracking. Keep a one-line origin note in the class header. Field lists and per-format quirks of contract registers are in `docs/DATA_SOURCES.md` §1.

## Domain rules (JavniRadar, non-negotiable)
- **Provenance:** every imported row keeps its `RawRecord`, and every domain row has a `provenanceId`. No number without a source.
- **Amounts:** `BigDecimal` in EUR.
  - HRK before 2023-01-01: divide by `7.53450`.
  - Periodic amounts ("mjesečno") are not multiplied; store `null` and raise an `IngestIssue`.
  - `null` means "not published", never 0.
- **Thresholds and election dates** come from `config/thresholds.yaml` and `elections.yaml`, each with validity dates. They are selected by the contract's `dateSigned`. Never hard-code them.
- **Rules:**
  - deterministic, one class per rule implementing a common interface;
  - carry a `ruleVersion`;
  - produce the three dimensions separately;
  - run without `ANTHROPIC_API_KEY`.
- **Wording:** generated texts never contain the forbidden terms listed in `AGENTS.md` ("korupcija", "nezakonito", "prijevara", ...). A test checks all templates.
- **AI assessment:**
  - structured output via tool use, cached by input hash, budget-limited (`LlmUsage`);
  - passed through the validator in `docs/ANOMALY_DETECTION.md` §4.4;
  - never called for `CRAFT` or `NATURAL_PERSON` suppliers;
  - model names come from config (`claude-haiku-5-5`, `claude-sonnet-5-5`).
- **GDPR:** contracts with natural persons are not exposed by the API. Never log OIBs of natural persons.
- **Synthetic data:**
  - `isSynthetic = true` and OIBs `000000000xx`;
  - excluded from every aggregate (`WHERE NOT is_synthetic`);
  - returned with `"synthetic": true`.

## Transactions and performance
- `@Transactional(readOnly = true)` on read-only service methods, `@Transactional` on writing ones. Not on controllers.
- Watch for N+1: for lists that need a related entity use `@EntityGraph` or `join fetch`.
- Filters with many optional fields go through `Specification` or a single `@Query` with `(:param IS NULL OR ...)`, not 8 derived methods.
- Don't load a whole table into memory to filter in Java.

## Logging
- `@Slf4j` (Lombok). `info` for import start/end with numbers ("udruge.gov.hr: 97 open, 3 skipped"), `warn` for skipped records, `error` with the exception for the unexpected. Never `System.out.println`.
- Don't log personal OIBs of natural persons or API keys.

## Tests (competition level)
- We don't test every getter. We test what breaks and is hard to check by hand:
  1. parsers / mapping of external responses (unit test with a real saved JSON/HTML sample in `src/test/resources/fixtures/`),
  2. every indicator rule with positive, negative and edge fixtures in `src/test/resources/fixtures/rules/` (e.g. a window crossing 16.05.2026, framework agreements, HRK contracts, missing OIB), plus the AI validator and the forbidden-wording check,
  3. one `@WebMvcTest` per controller for the happy path + 404/400.
- Tests never call real external services.

## Before saying it's done
1. `./mvnw -q compile` passes (Windows: `mvnw.cmd -q compile`); if you touched logic that has tests, also `./mvnw test`.
2. If the API is running, check the endpoint with `curl` and show the real response.
3. New endpoint used by the frontend: tell the user the exact JSON shape so the type in `apps/web/src/types/` can be aligned (or add it yourself following the `react-web` skill).
4. A new architecture decision (new dependency, new source, model change) goes into `work/decisions.md` with the reasoning.
