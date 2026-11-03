# Zadaci

_Ploča: https://github.com/users/ninom5/projects/1. Ploča je izvor istine za status, a ovaj file je samo pregled. Svaka kartica ima popis podzadataka (checkbox) u issueu. PR s "Closes #N" zatvara karticu._

_Uloge: DATA (ingest, parseri, matching), BACK (pravila, AI, API, deploy), FRONT (web, grafovi, karta, video). Imena idu u `team/team.md`. Plan po danima: `docs/MVP_ROADMAP.md`. Feature freeze je **07.11. u 20:00**, a predaja **09.11. do 18:00**._

_Grane: radi se iz `main`. Za svaku karticu nova grana `<issue>-kratki-opis` (npr. `41-pravila-f-c`), pa PR u `main`._

| Issue | Kartica | Uloga | Prio | Veličina | Rok | Ovisi o | Prompt |
|---|---|---|---|---|---|---|---|
| #39 | Organizacija: ključevi (Sudreg, Claude API), uloge, odluka o drugom projektu, javni repo, pregled UI-ja | korisnik/tim | P0 | S | 03.11. | — | — |
| #40 | Podaci: stari EOJN format (A2), JPT, DZS (+ Hvar kao Could). **Gotovo 03.11., spojeno PR #49; Hvar (Could) prebaciti u #46** | DATA | P0 | L | 04.11. | — | `next-a2-jpt-fc.md` faze 1–2 |
| #41 | Pravila F i C + sintetički ground truth + API indikatora (`/indicators`, `/buyers/{oib}`, `ruleCode`, `band`) | BACK | P0 | L | 04.11. | #40 (spojen, PR #49) | `next-41-pravila-f-c.md` |
| #42 | UI: profil JLS, filter "Vrsta indikatora", prikaz F i C | FRONT | P0 | L | 05.11. | #41 | `next-a2-jpt-fc.md` faza 4 |
| #43 | Karta (licenca GeoJSON-a ili fallback) i usporedba općina | FRONT | P0 | M | 06.11. | DZS iz #40 | — |
| #44 | AI procjena: backend (prompt, schema, validator, cache, budžet), evaluacija 40+60, blok u UI-ju, Sonnet re-check (Should) | BACK+FRONT | P0 | XL | 06.11. | Claude ključ, #41 | piše se nakon #41 |
| #45 | Obrazac za ispravak (backend + UI) i stranica Metodologija | BACK+FRONT | P0 | M | 06.11. | — | — |
| #46 | Dodatni izvori (Should/Could): nacionalni uzorak, Sudreg/pravilo B, profil dobavljača, LLM ekstrakcija B/C, OCDS/G | DATA | P1 | L | 06.11. | ključevi | samo ako Must stoji dobro |
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
