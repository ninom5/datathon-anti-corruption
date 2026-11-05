# Zadaci

_Ploča: https://github.com/users/ninom5/projects/1. Ploča je izvor istine za status, a ovaj file je samo pregled. Svaka kartica ima popis podzadataka (checkbox) u issueu. PR s "Closes #N" zatvara karticu._

_Uloge: DATA (ingest, parseri, matching), BACK (pravila, AI, API, deploy), FRONT (web, grafovi, karta, video). Imena idu u `team/team.md`. Plan po danima: `docs/MVP_ROADMAP.md`. Feature freeze je **07.11. u 20:00**, a predaja **09.11. do 18:00**._

_Grane: radi se iz `main`. Za svaku karticu nova grana `<issue>-kratki-opis` (npr. `41-pravila-f-c`), pa PR u `main`._

| Issue | Kartica | Uloga | Prio | Veličina | Rok | Ovisi o | Prompt |
|---|---|---|---|---|---|---|---|
| #39 | Organizacija: ključevi (Sudreg, Claude API), uloge, odluka o drugom projektu, javni repo, pregled UI-ja | korisnik/tim | P0 | S | 03.11. | — | — |
| #40 | Podaci: stari EOJN format (A2), JPT, DZS (+ Hvar kao Could). **Gotovo 03.11., spojeno PR #49; Hvar (Could) prebaciti u #46** | DATA | P0 | L | 04.11. | — | `next-a2-jpt-fc.md` faze 1–2 |
| #41 | Pravila F i C + sintetički ground truth + API indikatora (`/indicators`, `/buyers/{oib}`, `ruleCode`, `band`). **Gotovo 03.11., spojeno PR #50** | BACK | P0 | L | 04.11. | #40 (spojen, PR #49) | `next-41-pravila-f-c.md` |
| #42 | UI: profil JLS, filter "Vrsta indikatora", prikaz F i C. **Spojeno u `main` (PR #52)** | FRONT | P0 | L | 05.11. | #41 | `next-42-ui-fc-profil.md` |
| #43 | Karta (licenca GeoJSON-a ili fallback) i usporedba općina. **IvanMosa, PR #51: `/karta`, `/usporedba`, `/municipalities` API, GeoJSON 542/556 JLS** | FRONT | P0 | M | 06.11. | DZS iz #40 | — |
| #44 | AI procjena: backend (prompt, schema, validator, cache, budžet), evaluacija 40+60, blok u UI-ju, Sonnet re-check (Should). **Grana `44-ai-procjena`, necommitano: faze 1–4 gotove (04.11.): prompt `ai-flag-v3`, sintetička evaluacija izmjerena, stvarni uzorak od 60 izvezen. **Čeka tim: ručne oznake u `work/eval/real-sample-seed44.csv` (dvije osobe, upute u `work/eval/README.md`).** Pun run 2.212 ugovora odgođen (nakon oznaka i Sudrega). Faza 5 (Sonnet) nije počela. Preostalo za #45: gumbi "Korisno / Pogrešno" na AI bloku (`feedback`, `target_type = ai_assessment`)** | BACK+FRONT | P0 | XL | 06.11. | Claude ključ (faze 1–3 rade bez njega), #41 | `next-44-ai-procjena.md` |
| #45 | Obrazac za ispravak (backend + UI) i stranica Metodologija | BACK+FRONT | P0 | M | 06.11. | — | — |
| #46 | Dodatni izvori (Should/Could): nacionalni uzorak, Sudreg/pravilo B, profil dobavljača, LLM ekstrakcija B/C, OCDS/G. **Sudreg spojen 05.11. (`V5`, `/api/admin/sudreg/*`, dohvat ~1.577 dobavljača), NKD i datum osnivanja u AI kontekstu, pravilo B (`B_YOUNG_COMPANY`) s testovima i UI filterom; GDPR prikaz naziva za `ostalo` popravljen. Preostalo iz #46: nacionalni uzorak, profil dobavljača, LLM ekstrakcija B/C, OCDS/G** | DATA | P1 | L | 06.11. | ključevi | samo ako Must stoji dobro |
| #47 | Deploy, seed snapshot, provjera provenancije, poliranje (mobitel, pristupačnost, fontovi) | svi | P0 | L | **07.11. 20:00** | sve Must | — |
| #48 | Predaja: README, video v1/v2, mail na open.data@mpudt.hr | svi | P0 | L | **09.11. 18:00** | #47 | — |

**Prvo se može raditi paralelno:** #40 (DATA), #43 karta (FRONT), #45 (BACK) i #39 (korisnik).

## Gotovo
| Što | Kad | Napomena |
|---|---|---|
| #1 POC: ingest klase A, pravilo D, sintetički ground truth, API, istraživač, izvori | 02.11. | 2.332 ugovora, 17 JLS; pravilo D: 28 indikatora; sintetika 0 FP / 0 FN; 29 testova; spojeno u `main` |
| Stack potvrđen: Java/Spring Boot + React | 02.11. | `work/decisions.md` |

## Arhiva
Faza prijave (do 23.10.) je zatvorena; prijava je predana. Detalji su u git povijesti ovog filea. Prva verzija backloga (38 sitnih zadataka) zamijenjena je ovim karticama 02.11.
