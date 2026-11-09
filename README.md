# JavniRadar

**Tim Nazor** · Autori: Ivan Mosić, Šime Jadrijev, Luka Žuljević, Nino Milas · Mentor: Mario Čagalj

Civic-tech alat koji lokalnu javnu nabavu u Hrvatskoj čini razumljivom, pretraživom i usporedivom. Natjecanje: Datathon Portala otvorenih podataka 2026, izazov A (Društvo i javne usluge: transparentnost javnog sektora).

> Indikatori nisu optužbe. Pokazuju što vrijedi provjeriti.

## Dokumentacija
- **[Projektna dokumentacija (PROJECT.md)](docs/PROJECT.md)**: problem, korisnici, funkcije, podaci, utjecaj, ograničenja, daljnji razvoj, tim
- **[Tehnička arhitektura (ARCHITECTURE.md)](docs/ARCHITECTURE.md)**: dijagram, moduli, tok podataka, AI obrada, API rute, standardi, testovi, tehnički, organizacijski i pravni preduvjeti
- **[Popis skupova podataka (DATA_SOURCES.md)](docs/DATA_SOURCES.md)** · [Licence treće strane (THIRD_PARTY_LICENSES.md)](docs/THIRD_PARTY_LICENSES.md)
- Detaljni dokumenti:
  - [Detekcija obrazaca i AI procjena](docs/ANOMALY_DETECTION.md) · [Evaluacija AI-ja](docs/EVALUATION.md) · [Podatkovni model](docs/DATA_MODEL.md)
  - [Istraživanje izvora](docs/DATA_SOURCES_RESEARCH.md) · [Dizajn sustav](docs/DESIGN_SYSTEM.md) · [Demo scenariji](docs/DEMO_SCENARIOS.md)
- Pravila za AI agente: [AGENTS.md](AGENTS.md)

## Struktura
- `apps/api`: Spring Boot 4 (Java 25), PostgreSQL 17 (`docker-compose.yml`)
- `apps/web`: React 19 + TypeScript + Vite
- `docs/`: projektna i tehnička dokumentacija, evaluacijski skup AI-ja (`docs/eval/`)

## Pokretanje

**Preduvjeti:** Java 25, Node 22 i Yarn 1, Docker (za bazu i za testove backenda).

```bash
# 1. Baza (PostgreSQL 17 na portu 5434)
cd apps/api
docker compose up -d

# 2. API na http://localhost:8080 (Windows: mvnw.cmd)
export ADMIN_TOKEN=<proizvoljna-tajna>        # obvezno za /api/admin rute
export ANTHROPIC_API_KEY=...                  # opcionalno: AI procjena
export SUDREG_CLIENT_ID=... SUDREG_CLIENT_SECRET=...   # opcionalno: Sudski registar, pravilo B
./mvnw spring-boot:run

# 3. Prvi uvoz podataka (CKAN, registri, JPT, DZS, pa pravila; ~5 min)
curl -X POST -H "X-Admin-Token: $ADMIN_TOKEN" http://localhost:8080/api/admin/ingest/run
# opcionalno: sintetički demo skup ("Općina Primjerovo")
curl -X POST -H "X-Admin-Token: $ADMIN_TOKEN" http://localhost:8080/api/admin/synthetic/load
# opcionalno, uz ključeve: Sudreg i AI (dryRun=true je zadano i ne zove model)
curl -X POST -H "X-Admin-Token: $ADMIN_TOKEN" http://localhost:8080/api/admin/sudreg/run
curl -X POST -H "X-Admin-Token: $ADMIN_TOKEN" "http://localhost:8080/api/admin/ai/run?dryRun=false"

# 4. Web na http://localhost:5173 (proxy /api → :8080; drugi port: API_URL=http://localhost:8095 yarn dev)
cd ../web
yarn install
yarn dev
```

Spring Boot ne čita `apps/api/.env` sam; varijable treba postaviti u ljusci ili u IDE-u. Ostale postavke (port baze, model, budžet AI-ja) su u `apps/api/src/main/resources/application.yaml`, a pragovi pravila u `config/thresholds.yaml`.

**Provjere prije predaje:**

```bash
cd apps/web && yarn build && yarn lint && yarn test
cd apps/api && ./mvnw test          # treba Docker (Testcontainers, PostgreSQL 17)
```

## Izvori podataka

Svi podaci su otvoreni podaci javnog sektora. Puni popis s izdavateljima, poveznicama, licencama i datumima preuzimanja: **[DATA_SOURCES.md](docs/DATA_SOURCES.md)**.

| Izvor | Izdavatelj | Pristup |
|---|---|---|
| Registri ugovora JLS i javnih tijela (40 skupova) | pojedine JLS, županije, HERA | [data.gov.hr](https://data.gov.hr) CKAN API + XLSX/XLS/CSV |
| Popis javnopravnih tijela | MPUDT | [data.gov.hr](https://data.gov.hr/dataset/popis-javnopravnih-tijela), REST API `jpt.gov.hr` |
| Popis stanovništva 2021 po gradovima/općinama | DZS | [data.gov.hr](https://data.gov.hr/dataset/statistika-u-nizu-popis-stanovnistva-kucanstava-i-stanova-2021), XLSX |
| Sudski registar | Ministarstvo pravosuđa, uprave i digitalne transformacije | [sudreg-data.gov.hr](https://sudreg-data.gov.hr), REST API (OAuth2) |
| Granice JLS (karta) | geoBoundaries, © OpenStreetMap suradnici | [geoboundaries.org](https://www.geoboundaries.org), CC BY-SA 2.0 / ODbL |

## Korišteni alati

| Alat | Čemu je služio |
|---|---|
| **Claude Code** (Anthropic) | pomoć pri razvoju: kod backenda i frontenda, testovi, dokumentacija, istraživanje izvora podataka |
| **Claude API** (Anthropic, `claude-haiku-5-5`) | dio aplikacije: AI procjena ugovora („AI procjena – zahtijeva ljudsku provjeru”), uz validator dokaza |
| **ChatGPT / Codex** (OpenAI) | pomoć pri razvoju |
| **Gemini** (Google) | razrada ideja |
| Spring Boot, PostgreSQL, Flyway, Apache POI | backend, baza, migracije, čitanje XLSX registara |
| React, TypeScript, Vite, TanStack Query, wouter, axios | web sučelje |
| Docker, Testcontainers, JUnit, ESLint | lokalna baza, testovi, provjera koda |
| Python (`apps/web/scripts/build_jls_geojson.py`, `docs/eval/`) | jednokratna priprema granica za kartu i AI evaluacijskog skupa |

Sav kod koji su predložili AI alati tim je pregledao i preuzeo. Licence ovisnosti, fontova, ikona i kartografske podloge su u [THIRD_PARTY_LICENSES.md](docs/THIRD_PARTY_LICENSES.md).

## Licenca

[MIT](LICENSE) © 2026 Ivan Mosić, Šime Jadrijev, Luka Žuljević, Nino Milas.

Podaci ostaju pod licencama izvora (pretežno Otvorena dozvola RH, uz obvezno navođenje izvora; granice JLS pod CC BY-SA 2.0 / ODbL), vidi [DATA_SOURCES.md](docs/DATA_SOURCES.md).
