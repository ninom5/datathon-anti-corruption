# JavniRadar – plan MVP-a (02.–09.11.2026.)

**Uloge** (3 osobe; ako je tim 4, četvrta osoba preuzima AI i evaluaciju):

| Uloga | Odgovornost |
|---|---|
| **DATA** | ingest, parseri, normalizacija, matching, referentni podaci |
| **BACK** | pravila, AI procjena, REST API, testovi, deploy |
| **FRONT** | web, karta, grafovi, demo podaci, video |

**Interni rokovi:** **feature freeze 07.11. u 20:00**. Dani 08.–09.11. su samo za poliranje, video i dokumentaciju.

## 0. Odmah (01.11., ručno, korisnik)
1. **Zatražiti Sudreg OAuth ključ** (registracija na sudreg-data.gov.hr). Bez njega pravilo B ide samo u sintetički demo.
2. **Claude API ključ** s limitom potrošnje (postavljen u konzoli) → `ANTHROPIC_API_KEY`.
3. Potvrditi stack (Java, vidi `ARCHITECTURE.md` §1) i podjelu uloga.
4. Odluka o javnom repou (sigurnosna politika, `work/decisions.md`).

## 1. Raspored po danima

| Dan | DATA | BACK | FRONT | Gotovo kad |
|---|---|---|---|---|
| **N 02.11.** | CKAN discovery + download svih "registar ugovora" resursa (kopirati `PoliteHttpClient`); klasifikator A/B/C/D; JPT ingest | shema baze (Flyway `V1__init.sql` iz `DATA_MODEL.md`); kopirati Sudreg/AI pakete; `thresholds.yaml` | dizajn tokeni, layout, rute; odabir GeoJSON izvora za JLS (**provjeriti licencu**) | baza se puni JPT-om; popis klase A s brojem redaka |
| **P 03.11.** | parser klase A (traženje reda zaglavlja), normalizacija (iznosi, datumi, HRK, OIB checksum), dedup po EOJN URL-u; DZS popis + spoj s JPT-om | API: `/contracts`, `/buyers/{oib}`, `/sources`; pravilo **D** + testovi | istraživač ugovora na stvarnom API-ju | ≥ 2.000 ugovora iz ≥ 20 JLS u bazi; D radi |
| **U 04.11.** | nacionalni uzorak (benchmark); entity matching dobavljača; usporedne skupine; **odabir pilot županije/skupa** za karticu | pravila **F** i **C** + testovi (pozitivni/negativni/rubni fixtures); `/indicators` | profil JLS + graf kategorija; karta (ili fallback) | 3 pravila na stvarnim podacima; ručni pregled 20 indikatora (lažno pozitivni?) |
| **S 05.11.** | sintetički demo skup (`/demo`, `is_synthetic`); Sudreg: pravi klijent ako je ključ stigao, inače fixture | **AI procjena**: kontekst, prompt `ai-flag-v1`, tool schema, validator, cache, budžet; Haiku run na ≤ 300 ugovora | detalj indikatora (tri dimenzije, dokazi, "što NE znači") + blok AI procjene | AI procjene vidljive u UI-ju; validator odbija test-odgovore sa zabranjenim riječima |
| **Č 06.11.** | (S) LLM ekstrakcija za 2–3 JLS klase B/C; OCDS 2018–2024 za G (S) | evaluacijski skup (40 sint. + 60 stvarnih), metrike; `/feedback`; Sonnet re-check | usporedba općina, profil dobavljača, stranica Izvori/Metodologija | evaluacija izmjerena i upisana; svi Must ekrani postoje |
| **P 07.11.** | snapshot baze za seed; provjera provenancije (nijedan broj bez izvora) | deploy (VM/Render), health, rate-limit; `./mvnw test` zelen | poliranje, mobitel, pristupačnost (kontrast, tablice uz grafove) | **FEATURE FREEZE 20:00**; javni URL radi |
| **S 08.11.** | README: popis skupova s izdavateljima i poveznicama; licenca | bugfix samo kritičnih; screenshotovi | **snimanje videa** (scenarij u `DEMO_SCENARIOS.md` §5), montaža | video v1 ≤ 3:00 |
| **N 09.11.** | završna provjera dokumentacije | rezerva | video v2, prijevod naslova, upload | **mail na open.data@mpudt.hr do 18:00** (ne u 23:59) |

## 2. Must / Should / Future
| Must | Should | Future |
|---|---|---|
| Ingest klase A (~30 JLS), JPT, DZS | LLM ekstrakcija B/C/D | izravni EOJN API, svih 556 JLS |
| Pravila D, F, C s testovima | pravilo B (Sudreg), G (OCDS povijest) | E (broj ponuda iz TED-a) |
| AI procjena (Haiku) + validator + cache + budžet | Sonnet re-check, F11 NL pretraga | aktivno učenje iz povratnih informacija |
| Istraživač, profil JLS, detalj indikatora, karta, izvori | profil dobavljača sa Sudregom, vremenska crta | obavijesti, mreža odnosa |
| Obrazac za ispravak | RSS po JLS | moderacijsko sučelje |
| Deploy + video + dokumentacija | evaluacijska stranica u UI-ju | objava normaliziranog skupa na data.gov.hr (DCAT-AP) |

## 3. Ovisnosti
```
JPT ingest ─► matching naručitelja ─► profil JLS / usporedne skupine ─► pravilo C kontekst (osnivači)
parser A ───► contract ─► pravila D,F,C ─► AI kontekst ─► AI procjena
DZS ────────► peer group ─► F4 usporedba
Sudreg ključ ─► pravilo B (inače fixture u demu)
GeoJSON ────► karta (inače fallback)
```

## 4. Rizici i rezervni planovi
| # | Rizik | Vjerojatnost | Utjecaj | Rezerva |
|---|---|---|---|---|
| R1 | Podaci klase A pretanki za uvjerljive indikatore (malo izmjena, mali n) | srednja | visok | uključiti nacionalni uzorak kao **benchmark i evaluacijski skup**; demo na sintetičkim slučajevima + 1 neutralan stvarni |
| R2 | Sudreg ključ ne stigne do 04.11. | visoka | srednji | `FixtureSudregClient` samo za sintetički demo, jasno označen; pravilo B na stvarnim podacima isključeno; u videu "spremno za uključivanje" |
| R3 | data.gov.hr pada (502) ili spor tijekom demoa | srednja | visok | demo radi iz seed snapshota; ingest s retryjem |
| R4 | AI daje tendenciozne ili neutemeljene oznake | srednja | visok (pravni, ugled) | validator (polja/URL-ovi iz konteksta, zabranjene riječi), obvezna legitimna objašnjenja, nema AI-ja za obrte i fizičke osobe, ručni pregled svega što ulazi u video |
| R5 | Trošak LLM-a | niska | srednji | odabir zapisa, cache, `max_usd_per_run`, limit u konzoli |
| R6 | Nema GeoJSON-a JLS s dopuštenom licencom | srednja | srednji (video) | karta županija ili tile grid; JLS lista |
| R7 | Pravna pogreška u tumačenju ZJN-a | srednja | srednji | citirati samo provjerene članke; formulacija "okvir", ne "kršenje" |
| R8 | Tim radi i na drugom projektu (izazov C, agregator natječaja) | **visoka** | **visok** | odluka do 02.11.: JavniRadar je glavni ili rezervni; ne razvijati oba punim kapacitetom |
| R9 | Javni repo nije dopušten | srednja | nizak–srednji | predaja zip-om + link; licenca svejedno u repou |

## 5. Prihvatni kriteriji MVP-a
- [ ] ≥ 20 JLS sa stvarnim ugovorima u bazi, svaki zapis ima `provenance_id` i poveznicu na izvor.
- [ ] Pravila D, F, C imaju po ≥ 3 jedinična testa (pozitivan, negativan, rubni) i rade na stvarnim podacima.
- [ ] Nijedan tekst u UI-ju, API-ju ili promptu ne sadrži zabranjene riječi (automatski test).
- [ ] AI procjena: shema se provodi, validator odbija odgovor bez dokaza, natpis "AI procjena – zahtijeva ljudsku provjeru" je uvijek vidljiv.
- [ ] Rule-based dio radi bez `ANTHROPIC_API_KEY`.
- [ ] Sintetički zapisi imaju traku i nisu u statistikama stvarnih JLS (test).
- [ ] Karta, istraživač, profil JLS, detalj indikatora i izvori rade na javnom URL-u i na mobitelu.
- [ ] README sadrži popis svih skupova s izdavateljima i poveznicama, popis AI alata i upute za pokretanje.
- [ ] Video ≤ 3:00 pokriva problem, korisnike, izvore, funkcionalnosti, arhitekturu, vrijednost i daljnji razvoj.
