# Zadaci

_Uloge: DATA (ingest, parseri, matching), BACK (pravila, AI, API, deploy), FRONT (web, grafovi, karta, video). Imena upisati u `team/team.md`. Plan po danima: `docs/MVP_ROADMAP.md`. Feature freeze **07.11. 20:00**, predaja **09.11. do 18:00**._

_Grana: od 02.11. radi se iz `main` (POC spojen fast-forwardom, commit 0ca7dfb). Za svaki zadatak nova grana `T<broj>-kratki-opis`, pa PR u `main`._

## Organizacijsko (korisnik / tim)
| ID | Zadatak | Vlasnik | Rok | Status |
|---|---|---|---|---|
| O1 | Zatražiti Sudreg OAuth ključ (bez njega pravilo B samo u sintetici) | korisnik | **03.11.** | OTVORENO, HITNO |
| O2 | Claude API ključ s limitom potrošnje → `ANTHROPIC_API_KEY` | korisnik | **04.11.** (treba za T13) | OTVORENO |
| O3 | Podijeliti uloge DATA/BACK/FRONT i upisati imena i broj članova u `team/team.md` | tim | 03.11. | OTVORENO |
| O4 | Odluka: JavniRadar je glavni projekt, agregator natječaja (izazov C) nije | tim | 03.11. | OTVORENO |
| O5 | Smije li repo postati javan (sigurnosna politika)? Sada je **PRIVATE** na GitHubu (provjereno 02.11.). Ako ne smije, predaja zip-om | korisnik | 07.11. | OTVORENO |
| O6 | Pregled `docs/` i UI-ja POC-a u pravom browseru (filteri u URL-u, sintetika, Lepoglava, uski ekran) | korisnik | 03.11. | OTVORENO |

## Podaci (DATA)
| ID | Zadatak | Prio | Rok | Ovisi o | Prompt / gotovo kad |
|---|---|---|---|---|---|
| T01 | Parser starog EOJN izvještaja (A2): dedup s klasom A po (naručitelj, godina), provjera točnosti 10+10, HRK→EUR | Must | 03.11. | — | `next-a2-jpt-fc.md` faza 1; izvješće o novim JLS i godinama |
| T02 | JPT ingest i matching po OIB-u; je li dobavljač u vlasništvu naručitelja | Must | 03.11. | — | `next-a2-jpt-fc.md` faza 2; zapis u `DATA_SOURCES.md` |
| T03 | DZS Popis 2021 + spoj s JLS; usporedne skupine po broju stanovnika | Must | 04.11. | T02 | iznosi po stanovniku u profilu JLS |
| T04 | Nacionalni JSON uzorak kao benchmark i evaluacijski skup | Should | 04.11. | — | |
| T05 | Sudreg: pravi klijent ako stigne ključ, inače `FixtureSudregClient` samo za sintetiku | Should | 05.11. | O1 | pravilo B samo uz ključ |
| T06 | LLM ekstrakcija klase B/C za 2–3 JLS | Should | 06.11. | O2 | |
| T07 | OCDS povijest 2018–2024 za pravilo G | Could | 06.11. | — | |
| T08 | Grad Hvar: posredni certifikat u vlastiti truststore (TLS provjeru ne isključivati) | Could | 05.11. | — | +~264 ugovora |
| T09 | Snapshot baze za seed (demo radi bez data.gov.hr); provjera provenancije | Must | 07.11. | sve DATA | nijedan broj u UI-ju bez izvora |

## Pravila, AI, API (BACK)
| ID | Zadatak | Prio | Rok | Ovisi o | Prompt / gotovo kad |
|---|---|---|---|---|---|
| T10 | Pravilo F (cjepkanje) + sintetika s očekivanim ishodima (uklj. prozor preko 16.05.2026.) | Must | 04.11. | T01, T02 | `next-a2-jpt-fc.md` faza 3; 0 FP / 0 FN na jasnim slučajevima, 10 stvarnih primjera |
| T11 | Pravilo C (koncentracija) + sintetika (komunalno poduzeće, prirodni monopol) | Must | 04.11. | T01, T02 | isto |
| T12 | API `/indicators`, `/buyers/{oib}`; filtri `ruleCode` (lista) i `band`; sažetak po vrsti | Must | 04.11. | T10, T11 | `next-a2-jpt-fc.md` faza 4 |
| T13 | AI procjena: kontekst, prompt `ai-flag-v1`, tool schema, validator, cache, budžet `llm_usage`; Haiku na ≤ 300 ugovora; ništa za obrte | Must | 05.11. | O2, T12 | prompt se piše nakon T12; validator odbija odgovor bez dokaza i sa zabranjenim riječima |
| T14 | Evaluacijski skup (40 sintetičkih + 60 stvarnih), metrike, upis u docs | Must | 06.11. | T13 | |
| T15 | `/feedback`: obrazac za ispravak (backend) | Must | 06.11. | — | |
| T16 | Sonnet re-check spornih AI procjena | Should | 06.11. | T13 | |
| T17 | Deploy (VM/Render), health, rate-limit, admin token iz env-a; javni URL | Must | 07.11. | T09 | `./mvnw test` zelen; URL radi |

## Web (FRONT)
| ID | Zadatak | Prio | Rok | Ovisi o | Prompt / gotovo kad |
|---|---|---|---|---|---|
| T20 | Filter "Vrsta indikatora" (D1/D2/F/C, višestruki odabir, brojevi, u URL-u) | Must | 04.11. | T12 | `next-a2-jpt-fc.md` faza 4 |
| T21 | Profil JLS `/jls/:oib`: graf kategorija + tablica, indikatori, pokrivenost godina | Must | 04.11. | T12 | isto |
| T22 | Detalj indikatora F (vremenska os, prag, zbroj prozora) i C (udio dobavljača po godinama) | Must | 05.11. | T12 | isto |
| T23 | Karta: provjeriti licencu GeoJSON-a za JLS; fallback karta županija ili tile grid | Must | 05.11. | — | licenca zapisana u `DATA_SOURCES.md` |
| T24 | Blok AI procjene na detalju (natpis "AI procjena – zahtijeva ljudsku provjeru", dokazi, legitimna objašnjenja) | Must | 05.11. | T13 | |
| T25 | Obrazac za ispravak (UI) | Must | 06.11. | T15 | |
| T26 | Usporedba općina | Must | 06.11. | T03 | |
| T27 | Stranica Metodologija (pravila, pragovi s izvorima, ograničenja, "što NE znači") | Must | 06.11. | T10, T11 | |
| T28 | Profil dobavljača | Should | 06.11. | T05 | |
| T29 | Poliranje: mobitel, pristupačnost (kontrast, tablice uz grafove), tamni način, fontovi preko `@fontsource` | Must | 07.11. | — | provjera u pravom browseru |

## Predaja (svi)
| ID | Zadatak | Vlasnik | Rok |
|---|---|---|---|
| P1 | README: svi skupovi s izdavateljima i poveznicama, licenca, AI alati, upute za pokretanje | DATA | 08.11. |
| P2 | Video v1 ≤ 3:00 po `docs/DEMO_SCENARIOS.md` §5. Tema: alat pomaže i naručiteljima da poprave objavljene podatke (`work/ideas.md`). Bez obrta i fizičkih osoba u kadru | FRONT | 08.11. |
| P3 | Video v2, završna provjera dokumentacije | svi | 09.11. |
| P4 | **Mail na open.data@mpudt.hr** (rješenje + video) | korisnik | **09.11. do 18:00** |

## Gotovo
| Što | Kad | Napomena |
|---|---|---|
| POC: ingest klase A, pravilo D, sintetički ground truth, API, istraživač, izvori | 02.11. | 2.332 ugovora, 17 JLS; D: 28 indikatora; sintetika 0 FP / 0 FN; 29 testova; spojeno u `main` |
| Stack potvrđen: Java/Spring Boot + React | 02.11. | `work/decisions.md` |

## Arhiva: faza prijave (do 23.10.)
Prijava je predana (izazovi A i C, prema `Desktop/datathon/CLAUDE.md`). Detalji su u git povijesti ovog filea.
