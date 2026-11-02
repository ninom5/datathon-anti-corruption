# Zadaci

## Arhiva: faza prijave (do 23.10.)
_Rok je prošao. Prema `Desktop/datathon/CLAUDE.md` prijava je predana (izazovi A i C); ovdje statusi nisu ažurirani. Otvoreno je samo pitanje GitHuba, koje je prebačeno dolje._

| Zadatak | Vlasnik | Rok | Status |
|---|---|---|---|
| Naći mentora (nastavnik na visokom učilištu na kojem studira barem jedan član tima) i dobiti njegove kontakt podatke za obrazac | | **23.10. 24:00** | OTVORENO, HITNO |
| Potvrditi da su svi članovi studenti i da nitko nije u drugom Datathon timu | | 23.10. | OTVORENO |
| Odlučiti izazov(e) za prijavu (preporuka: jedan, vidi `work/decisions.md`) | tim | 23.10. | OTVORENO |
| **Svaki član** ispuni online prijavni obrazac (naziv tima, izazov, kontakt mentora, privola za video) | svi 4 | **23.10. 24:00** | OTVORENO, HITNO |
| Pregledati prijavni obrazac prije ispunjavanja: što traži (JMBAG? OIB? potvrda?) | korisnik | 23.10. | OTVORENO |
| Pročitati Javni poziv i Pravila | asistent | 22.10. | GOTOVO (vidi `context/competition.md`) |
| Pregledati data.gov.hr datasetove i API-je, naći ideju | tim | nakon prijave | OTVORENO |
| Dogovoriti interni plan: prototip do ~6.11., video i dokumentacija do 9.11. | tim | nakon prijave | OTVORENO |

## JavniRadar (od 2026-11-01), plan u `docs/MVP_ROADMAP.md`
| Zadatak | Vlasnik | Rok | Status |
|---|---|---|---|
| Zatražiti Sudreg OAuth ključ | korisnik | 01.11. | OTVORENO, HITNO |
| Claude API ključ s limitom potrošnje | korisnik | 02.11. | OTVORENO |
| Potvrditi stack (Java) i uloge DATA/BACK/FRONT | tim | 02.11. | Java GOTOVO; uloge OTVORENO |
| Odluka: JavniRadar naspram agregatora natječaja (C) | tim | 02.11. | OTVORENO |
| Pregled dokumentacije `docs/` | korisnik | 02.11. | OTVORENO |
| Provjeriti smije li kod javno na GitHub (sigurnosna politika); inače predaja zip-om | korisnik | 07.11. | OTVORENO |
| Potvrditi broj članova tima (3 ili 4) i upisati `team/team.md` | korisnik | 02.11. | OTVORENO |

## POC (vertikalni presjek), prompt `work/prompts/poc-implementation.md`
| Faza | Status | Napomena |
|---|---|---|
| 1. Shema + ingest + parser klase A | **GOTOVO 02.11.**, potvrđeno; slijedi provjera točnosti parsera | 2.332 ugovora, 17 JLS (+1 županija, 1 vrtić), 0 odbačenih redaka, 11 testova zeleno |
| 2. Pravilo D + sintetički skup s očekivanim ishodima + testovi | **GOTOVO 02.11.**, čeka potvrdu korisnika | sintetika 28 redaka: 0 FP / 0 FN; stvarno 16 D1 + 12 D2 (4 informativna); 22 testa zeleno |
| 3. API (`/contracts`, `/contracts/{id}`, `/sources`) | **GOTOVO 02.11.** | uz `/contracts/summary`, `/buyers`, `/sources/summary`; GDPR test za obrte i fizičke osobe; 29 testova zeleno |
| 4. UI (istraživač, detalj ugovora, izvori) | **GOTOVO 02.11.**, čeka pregled korisnika | naslovnica, `/istrazi`, `/ugovor/:id`, `/izvori`; `yarn lint` i `yarn build` zeleno; provjereno headless screenshotovima (Edge) |

**Nakon POC-a (redoslijed potvrdio korisnik 02.11.):**
1. **Parser "A2" za stari EOJN izvještaj** (~20 datoteka, 14+ izdavatelja, 2018.–2023.). PRVI zadatak nakon POC-a, prije pravila F i C (trebaju više godina i JLS). Vidi `work/decisions.md`.
2. Pravila F i C.

**Odgođeno:**
- Grad Hvar (264 ugovora prema 1.11.): Java odbija TLS certifikat (`SSLHandshakeException`). Za POC preskočeno; TLS provjera se ne dira. Opcija: dodati nedostajući posredni certifikat u vlastiti truststore.
- Kontejner `datathon-postgres` (drugi projekt) je ostao upaljen jer ga je pokrenuo naš stari compose. Korisnik ga može ugasiti (`docker stop datathon-postgres`).
