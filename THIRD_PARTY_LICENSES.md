# Licence treće strane

JavniRadar je objavljen pod licencom MIT (`LICENSE`). Ovaj popis navodi tuđe komponente koje projekt koristi, s njihovim licencama.

**Kako je popis napravljen (2026-11-08):**
- web: `npx license-checker --production --summary` i `--csv` u `apps/web`;
- API: `mvnw.cmd org.codehaus.mojo:license-maven-plugin:2.4.0:add-third-party -Dlicense.includedScopes=compile,runtime` u `apps/api`. Izlaz je u `apps/api/target/generated-sources/license/THIRD-PARTY.txt` i nije u repou (`target/` je u `.gitignore`).

## Sažetak provjere

| Provjera | Rezultat |
|---|---|
| GPL ili AGPL kao jedina licenca | **nema** |
| Nekomercijalne licence (npr. CC BY-NC) | **nema** |
| Nepoznate licence | **nema** (jedini `UNLICENSED` bio je naš paket `web` prije dodavanja `"license": "MIT"`) |
| ⚠️ Dvojne licence s GPL/LGPL opcijom | Logback (EPL-2.0 **ili** LGPL-2.1), `jakarta.annotation-api` i `jakarta.transaction-api` (EPL-2.0 **ili** GPL-2.0 with Classpath Exception). Koristimo ih pod EPL-2.0, bez izmjena, kao nepromijenjene biblioteke. Nisu prepreka za MIT |
| ⚠️ Share-alike podaci | granice JLS za kartu: CC BY-SA 2.0 / ODbL (vidi „Kartografska podloga”). Odnosi se na datoteku `apps/web/public/geo/jls.geojson`, ne na kod |
| ⚠️ Fontovi s CDN-a | Google Fonts (SIL OFL 1.1). Preglednik ih učitava s `fonts.googleapis.com`, što šalje IP adresu korisnika Googleu. Za produkciju preporučujemo lokalno posluživanje fontova |

## 1. Web (`apps/web`), produkcijske ovisnosti

39 paketa: MIT 37, Unlicense 1 (`wouter`), MIT 1 (naš `web`).

| Paket | Verzija | Licenca |
|---|---|---|
| `react`, `react-dom`, `scheduler`, `use-sync-external-store` | 19.3.1 / 0.28.0 / 1.7.0 | MIT |
| `@tanstack/react-query`, `@tanstack/query-core` | 5.104.5 | MIT |
| `axios` | 1.20.0 | MIT |
| `react-error-boundary` | 6.1.6 | MIT |
| `wouter` | 3.13.0 | Unlicense (javno dobro) |
| `regexparam` | 3.0.0 | MIT |
| tranzitivne ovisnosti axiosa (`form-data`, `follow-redirects`, `proxy-from-env`, `mime-types`, `mime-db`, `combined-stream`, `delayed-stream`, `asynckit`, `debug`, `ms`, `agent-base`, `https-proxy-agent`, `es-*`, `get-intrinsic`, `gopd`, `hasown`, `has-symbols`, `has-tostringtag`, `function-bind`, `call-bind-apply-helpers`, `dunder-proto`, `get-proto`, `math-intrinsics`) | razne | MIT |
| `@types/react`, `csstype` | 19.3.0 / 3.2.3 | MIT |

**Razvojni alati** (nisu u produkcijskom paketu, nisu provjereni alatom nego po licenci projekta): Vite (MIT), TypeScript (Apache-2.0), ESLint i `typescript-eslint` (MIT), `@vitejs/plugin-react` (MIT).

## 2. API (`apps/api`), Maven `compile` i `runtime`

123 artefakta. Grupirano po licenci:

| Licenca | Artefakti |
|---|---|
| **Apache-2.0** | Spring Boot 4.1.2 (svi `spring-boot-*` moduli), Spring Framework 7.0.10, Spring Data 4.1.2, Hibernate ORM 7.4.13, Hibernate Validator 9.1.5, Hibernate Models, JBoss Logging, Flyway 12.4.0 (`flyway-core`, `flyway-database-postgresql`), **Anthropic Java SDK 3.3.0** (`anthropic-java`, `-core`, `-client-okhttp`), OkHttp 4.12.0, Okio 3.6.0, Jackson 2.21 i 3.1.7, Kotlin stdlib/reflect, victools `jsonschema-generator` 4.38.0 (+ moduli jackson, swagger-2), swagger-annotations 2.2.50, **Apache POI 5.5.1** (`poi`, `poi-ooxml`, `poi-ooxml-lite`), XmlBeans 5.3.0, Apache Commons (codec, io, logging, collections4, compress, lang3, math3), Log4j API i `log4j-to-slf4j` 2.25.5, Tomcat embed 11.0.27, HikariCP 7.0.2, SparseBitSet, Micrometer 1.17.2, OpenTelemetry API 1.62.0, ClassMate, Byte Buddy, SnakeYAML 2.6, JSpecify, Error Prone annotations, JetBrains annotations, `jakarta.inject-api`, `jakarta.validation-api` |
| **MIT** | Lombok 1.18.48 (samo pri prevođenju), SLF4J 2.0.20 (`slf4j-api`, `jul-to-slf4j`), Checker Qual 3.55.1, standardwebhooks 1.2.0 |
| **BSD-2-Clause** | PostgreSQL JDBC 42.7.14 |
| **BSD-3-Clause / BSD** | ANTLR 4 runtime 4.13.2, curvesapi 1.08 |
| **EPL-2.0** | AspectJ Weaver 1.9.25.1 |
| **EPL-2.0 ili LGPL-2.1** ⚠️ | Logback classic i core 1.5.38 |
| **EPL-2.0 ili GPL-2.0 with CPE** ⚠️ | `jakarta.annotation-api` 3.0.0, `jakarta.transaction-api` 2.0.1 |
| **EDL-1.0 (BSD-3) / EPL-2.0** | `jakarta.persistence-api` 3.2.0, `jakarta.xml.bind-api` 4.0.5, `jakarta.activation-api` 2.1.4, JAXB runtime/core/txw2 4.0.9, istack-commons-runtime, Angus Activation |

Napomena: `spring-boot-devtools` je u popisu jer je `runtime` i `optional`. Spring Boot ga ne pakira u izvršni JAR.

**Testne ovisnosti** (nisu u popisu jer je `includedScopes=compile,runtime`): Spring Boot test starteri, Testcontainers (MIT), JUnit Jupiter (EPL-2.0).

## 3. Infrastruktura

| Komponenta | Licenca | Napomena |
|---|---|---|
| PostgreSQL 17 (Docker slika `postgres:17`) | PostgreSQL License | samo pokretanje, ne distribuiramo |
| Java 25, Node.js, Yarn 1 | GPL-2.0 with CE (OpenJDK) / MIT / BSD-2 | alati za izgradnju |

## 4. Fontovi, ikone i karta

| Resurs | Izvor | Licenca | Gdje |
|---|---|---|---|
| IBM Plex Sans (400, 500, 600) | Google Fonts | SIL Open Font License 1.1 | `apps/web/index.html` |
| Source Serif 4 (600) | Google Fonts | SIL Open Font License 1.1 | `apps/web/index.html` |
| Ikone (SVG putanje) | Lucide, https://lucide.dev | ISC | kopirane u `apps/web/src/components/Icon/Icon.tsx`, s napomenom o izvoru u zaglavlju |
| **Kartografska podloga: granice JLS** | geoBoundaries HRV ADM2 (gbOpen), izvedeno iz OpenStreetMapa | **CC BY-SA 2.0** (prema geoBoundaries API-ju); izvorni OSM podaci su pod **ODbL**, „© OpenStreetMap contributors” | `apps/web/public/geo/jls.geojson`, napravljeno skriptom `apps/web/scripts/build_jls_geojson.py`. Atribucija je u svojstvu `attribution` datoteke i **prikazuje se ispod karte** (`MapPage.tsx`): „Granice JLS: geoBoundaries HRV ADM2 (CC BY-SA 2.0), izvedeno iz OpenStreetMapa (© OpenStreetMap suradnici, ODbL); pojednostavio JavniRadar.” Izvedena datoteka je pod istim uvjetima |

Karta se crta kao SVG iz tog GeoJSON-a. Nema rasterskih pločica (tile servera) treće strane.

## 5. Kopirani i preuzeti kod

| Što | Odakle | Licenca / status | Kako je označeno |
|---|---|---|---|
| HTTP sloj (`PoliteHttpClient`, `RobotsRules`, `HostThrottle`, `DiskCache`, `TextDecoder`, iznimke), zajednički kod (`common/*`: `ErrorResponse`, `GlobalExceptionHandler`, `Oib`, `OibValidator`, `PageRequests`, `PageResponse`, `AdminKeyGuard`…), `AmountParser`, `CroatianDates`, `LlmUsage`, `AnthropicLlmClient`, `RestSudregClient`, `SudregProperties` | drugi projekt istog tima za Datathon 2026 (druga tema) | vlastiti kod autora, nije kod treće strane; preuzeta je samo infrastruktura (HTTP sloj, obrada grešaka, klijenti API-ja), ne logika rješenja | ovaj odjeljak |
| Dizajn sustav frontenda (tokeni, komponente) | OtvoreniRadar (`FESB/datathon`), isti tim | vlastiti rad tima | `DESIGN_SYSTEM.md`, `work/decisions.md` |
| SVG putanje ikona | Lucide | ISC | vidi §4 |
| Skillovi za AI agente (`.claude/skills/`) | `Desktop/datathon`, prilagođeni | vlastiti rad tima | `CLAUDE.md` |

Kod koji je generirao AI (Claude Code, Codex) pregledao je i preuzeo tim; vidi „Korišteni alati” u `README.md`.
