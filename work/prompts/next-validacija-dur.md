# Prompt: usporedba indikatora s nalazima Državnog ureda za reviziju (DUR)

_Napisano 2026-11-05. Kopirati u novu sesiju._

---

Nastavljamo JavniRadar. Zadatak je **vanjska validacija pravila**: pogađaju li naši indikatori (D, F, C, B) i AI procjena ugovore koje je **Državni ured za reviziju (DUR)** u svojim izvješćima naveo kao nepravilnosti u javnoj nabavi. Stanje projekta je u `CLAUDE.md`, `work/tasks.md`, `log.md` i `work/decisions.md`. Rok je feature freeze 07.11. u 20:00, pa ovaj zadatak ne smije trajati dulje od pola dana.

## Pravila rada (obvezno)
- **Nikad ne commitaj, ne pushaj i ne otvaraj PR.** Promjene ostavi necommitane i na kraju napiši što je spremno za commit.
- **Grana:** `git checkout main && git pull && git checkout -b validacija-dur`. Ako #44 (`44-ai-procjena`) još nije spojen u `main`, napravi granu od `44-ai-procjena` i to napiši korisniku.
- Komunikacija je na hrvatskom.
- **Pravni okvir i jezik:**
  - koristi **samo službene izvore**: izvješća DUR-a (revizija.hr) ili pravomoćne presude;
  - medijske članke ne koristi kao dokaz;
  - u svim tekstovima vrijedi politika iz `AGENTS.md`, dakle bez riječi "korupcija", "nezakonito", "prijevara", "pronevjera", "namješteno" i bez imenovanja fizičkih osoba;
  - piši "DUR je u nalazu naveo …", "pravilo D je označilo …".
- **Ništa ne izmišljaj.** Svaki nalaz mora imati poveznicu na izvješće, stranicu ili poglavlje i citiranu tvrdnju (do 15 riječi, ostalo prepričaj).
- **Poštuj robots.txt** svake stranice prije dohvata, s najviše 1 zahtjevom u sekundi. Ništa ne zaobilazi (403, prijava, CAPTCHA). Rezultat provjere robots.txt-a zapiši.
- **Brojke se upisuju samo ako su stvarno izbrojane.** Prijavi i pogotke i promašaje. Ne biraj samo slučajeve koji izgledaju dobro.

## Kontekst
- Podaci su u lokalnoj bazi `javniradar-postgres` (port 5434, `docker start javniradar-postgres`). Ima oko 5.849 stvarnih ugovora iz 34 JLS, klase A (EOJN izvoz, uglavnom 2024.–2026.) i A2 (stari EOJN izvještaj, otprilike 2018.–2023.).
- **Pravila:**
  - D1: povećanje vrijednosti iznad ZJN granica;
  - D2: isplata iznad ugovorenog;
  - F: cjepkanje ispod praga;
  - C: koncentracija dobavljača;
  - B: mlada tvrtka, na Sudreg snimkama; na stvarnim podacima ima 0 indikatora.
  - Detalji su u `docs/ANOMALY_DETECTION.md`.
- AI procjena (`ai-flag-v3`) postoji samo za oko 90 stvarnih ugovora (`ai_assessment`). Pun run nije napravljen. Ne pokreći ga bez pitanja korisnika (oko 3 USD).
- Velikih gradova (Zagreb, Rijeka, Split) nema u podacima (`docs/DATA_SOURCES.md`, zadnji odjeljak), pa su relevantni samo nalazi za naše JLS.

## Koraci
1. **Popis pokrivenosti.** Iz baze izvuci naše JLS, OIB-e i godine sklapanja ugovora po klasi:
   ```sql
   select b.name, b.oib, extract(year from c.date_signed)::int as godina, count(*)
   from contract c join organization b on b.id = c.buyer_id
   where not c.is_synthetic group by 1, 2, 3 order by 1, 3;
   ```
2. **Pretraga DUR-a.**
   - Za svaku JLS s popisa traži revizijska izvješća DUR-a (financijska revizija ili revizija učinkovitosti) za godine iz koraka 1. Izvor je revizija.hr; prije dohvata provjeri robots.txt.
   - Zanimaju te nalazi o **javnoj nabavi**: izmjene ugovora iznad dopuštenog, plaćanje iznad ugovorenog, dijeljenje nabave na manje dijelove, nabava bez postupka, ugovori bez objave u registru.
   - Za svaki nalaz zapiši: JLS, godinu, vrstu nalaza, predmet, iznos i dobavljača ako ih izvješće navodi, poveznicu i stranicu.
   - Ako DUR za neku JLS nema izvješće za te godine, i to zapiši.
3. **Usporedba.** Za svaki nalaz pronađi odgovarajući ugovor ili skupinu ugovora u bazi (naručitelj, godina, predmet, iznos, dobavljač) i provjeri ima li aktivan indikator (`indicator_contract` i `indicator`) i AI procjenu.
   - Svaki nalaz razvrstaj u jednu kategoriju:
     - **pogodak**: nalaz je u bazi i odgovarajuće pravilo ga je označilo;
     - **promašaj**: nalaz je u bazi, a pravilo ga nije označilo;
     - **nema podataka**: ugovor nije u registru ili nedostaju polja koja pravilo treba;
     - **izvan opsega**: vrsta nalaza koju nijedno pravilo ne mjeri.
   - Kod promašaja ukratko navedi zašto: prag, nedostajuće polje ili granica pravila.
   - **Pragove ne mijenjaj da bi pogodio nalaz.** Prijedlog promjene samo zapiši u `work/decisions.md` kao otvoreno pitanje.
4. **Rezultat:**
   - `work/eval/dur-nalazi.csv`: jedan redak po nalazu, sa stupcima JLS, godina, vrsta nalaza, poveznica, `contract_id` ili skupina, pravilo, kategorija, napomena;
   - novi odjeljak "Usporedba s nalazima Državnog ureda za reviziju" u `docs/EVALUATION.md`: datum, broj pregledanih izvješća, broj nalaza o nabavi, broj po kategorijama, 2–3 primjera s poveznicama, neutralnim jezikom;
   - ako je stvarnih pogodaka barem 2, predloži u `docs/DEMO_SCENARIOS.md` jedan kadar za video, npr. "pravilo D označilo je ugovor koji je i DUR naveo u nalazu". Formulaciju prvo pokaži korisniku.
5. **Dokumentacija:** dodaj DUR kao izvor u `docs/DATA_SOURCES.md` (URL, robots.txt, način dohvata, licenca ako je navedena), ažuriraj `work/tasks.md` i `log.md`.

## Što ne raditi
- Ne uvozi izvješća DUR-a u bazu i ne pravi novi UI. Ovo je evaluacija, ne nova značajka. Ako se pokaže vrijednim, za kasnije zapiši prijedlog: poveznicu "Nalaz DUR-a" uz indikator.
- Ne mijenjaj pravila ni prompt AI-ja.
- Ne pokreći plaćene LLM pozive bez potvrde korisnika.

## Na kraju
Kratko izvješće korisniku:
- koliko JLS i izvješća je pregledano;
- koliko je nalaza o nabavi pronađeno;
- broj po kategorijama (pogodak, promašaj, nema podataka, izvan opsega);
- najbolji primjer za video;
- iskrena ocjena isplati li se to pokazati žiriju.

Ako je pogodaka 0–1, reci to otvoreno i predloži da se usporedba spomene samo u metodologiji, a ne u videu.
