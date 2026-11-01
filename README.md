# JavniRadar

Civic-tech alat koji lokalnu javnu nabavu u Hrvatskoj čini razumljivom, pretraživom i usporedivom. Natjecanje: Datathon Portala otvorenih podataka 2026, izazov A.

> Indikatori nisu optužbe. Pokazuju što vrijedi provjeriti.

## Dokumentacija
- [Pregled projekta](docs/PROJECT_OVERVIEW.md) · [Izvori podataka](docs/DATA_SOURCES.md) · [Funkcionalnosti](docs/FEATURES.md)
- [Detekcija obrazaca i AI procjena](docs/ANOMALY_DETECTION.md) · [Podatkovni model](docs/DATA_MODEL.md) · [Arhitektura](docs/ARCHITECTURE.md)
- [UI/UX](docs/UI_UX.md) · [Plan MVP-a](docs/MVP_ROADMAP.md) · [Demo scenariji](docs/DEMO_SCENARIOS.md)
- Pravila za AI agente: [AGENTS.md](AGENTS.md)

## Struktura
- `apps/api`: Spring Boot 4 (Java 25), PostgreSQL 17 (`docker-compose.yml`)
- `apps/web`: React 19 + TypeScript + Vite

## Pokretanje
_Dopuniti tijekom izrade (docker compose, `./mvnw spring-boot:run`, `yarn dev`)._

## Popis skupova podataka i korišteni alati
_Obvezno za predaju. Popis skupova je u [DATA_SOURCES.md §13](docs/DATA_SOURCES.md); prije predaje ga prenijeti ovdje. Korišteni AI alati: Claude API, Claude Code._
