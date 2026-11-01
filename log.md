# Dnevnik

## 2026-10-22
- Dogovoren način rada: modularni md fileovi, asistent kritički savjetuje kao tech poduzetnik/softver inženjer. Postavljen kostur foldera.
- Primljen pozivni mail: Datathon Portala otvorenih podataka 2026 (MPUDT). Zapisano u `context/competition.md` i `context/timeline.md`. Uočeno: rok za prijavu je 23.10. u 24:00 (sutra).
- Folder je premješten u `Desktop/other/datathon` (klon repa github.com/SimeJadrijev/datathon). Prvi commit s md fileovima je napravljen i pushan na `origin/main` na izričit zahtjev korisnika. Vidljivost repa NIJE provjerena (`gh` nije instaliran).
- Pročitani Javni poziv i Pravila (PDF-ovi premješteni u `context/izvori/`). `competition.md`, `timeline.md`, `tasks.md` i `decisions.md` prepisani.
- **Ispravak:** prijavu ne podnosi samo mentor, nego svaki član tima ispunjava obrazac. Kriteriji: max 60 b, 9 kriterija, težište na izvedivosti, UI/UX-u i videu. Video do 3 min, obavezan, bez njega se rješenje ne ocjenjuje.
- Korisnik dodao skill `skills/product-brainstorming/SKILL.MD` (generički PM skill: modovi, HMW, JTBD, assumption testing). Nije u `.claude/skills/`, pa nije aktivan. Razgovor o tome koje još skillove dodati u fazi brainstorminga (prijedlog: custom skill za istraživanje data.gov.hr kataloga + custom "žiri" skill s rubrikom od 60 bodova).
- Skill premješten u `.claude/skills/product-brainstorming/SKILL.md` (aktivan). Napisana još 2 skilla: `jury-simulator` i `data-scout`. Provjereno curlom da data.gov.hr CKAN API radi na `/ckan/api/3/action/` (3956 skupova), upisano u `context/data.md`. Ključan nalaz: samo ~55 skupova se osvježava dnevno/tjedno/kontinuirano.
- Prijedlog (još ne odlučeno): ideja mora biti zaključana oko 27.10. da ostane ~10 dana za izradu. Brainstorm iz dva smjera (od podataka i od problema).
- Nepushani/necommitani: PDF-ovi u `context/izvori/`, `.claude/skills/` i izmjene md fileova nakon prvog commita.
- Skillovi `video-script` (3 min) i UI/UX nisu napisani, treba ih tek u fazi izrade.

## 2026-11-01
- Korisnik je zadao brief za **JavniRadar** (izazov A). Napisana je dokumentacija: `docs/` (9 fileova), `AGENTS.md`; ažurirani `CLAUDE.md` i `work/decisions.md`. Ništa nije commitano (korisnik prvo pregledava).
- Provedene stvarne HTTP provjere (CKAN, JPT, Sudreg 401 + OpenAPI, SRDI, DZS, OCP OCDS, TED; preuzeto i klasificirano 148 općinskih datoteka). Nalazi su u `docs/DATA_SOURCES.md`.
- U ovom repou nema `poc/`. Reusable kod je u Javi u `Desktop/datathon`.
- Pregled starih md fileova: ažurirani README, timeline, team, ideas, tasks (arhiva prijave), competition, jury-simulator skill (3 osobe, rok). Ništa nije obrisano.
- Kopirani skillovi `react-web` i `spring-boot-api` iz `Desktop/datathon` (izvor netaknut) i prilagođeni JavniRadaru (rječnik domene, /api/v1, domenska pravila, UI pravila za indikatore i AI). `ARCHITECTURE.md` usklađen: camelCase JSON, format greške {statusCode,message,error}.
