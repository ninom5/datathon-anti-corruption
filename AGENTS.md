# Upute za AI agente (JavniRadar)

Vrijede za sve AI agente koji pišu kod ili dokumentaciju u ovom repou (Claude Code, Codex, Copilot …). Ovaj file sadrži tehnička pravila.

## Prije rada
1. Pročitaj `docs/PROJECT.md` i dokument u `docs/` koji se tiče zadatka: `ARCHITECTURE`, `DATA_MODEL`, `DATA_SOURCES`, `DATA_SOURCES_RESEARCH`, `ANOMALY_DETECTION`, `EVALUATION`, `DESIGN_SYSTEM`, `DEMO_SCENARIOS`.
2. Slijedi arhitekturu (`docs/ARCHITECTURE.md`) i podatkovni model (`docs/DATA_MODEL.md`). Odstupanje obrazloži u opisu PR-a.
3. **Prije pisanja koda obavezno učitaj skill:** `spring-boot-api` za `apps/api` i `react-web` za `apps/web` (oba su u `.claude/skills/`). Ako zadatak dira i backend i frontend, učitaj oba. Svaki novi zadatak "implementiraj X" počinje time.

## Podaci i integracije
- **Provjeri izvor prije integracije.** Napravi stvarni zahtjev, a datum, URL, HTTP status i uzorak zapiši u `docs/DATA_SOURCES_RESEARCH.md`. Ne izmišljaj endpointe, skupove, nazive polja ni dozvole.
- **Svaki uvezeni zapis mora imati provenanciju:** izvorna datoteka/URL, lokator (redak, stranica), vrijeme dohvata, metoda (parser/LLM/API) i pouzdanost. Nijedan broj u UI-ju bez izvora.
- Poštuj robots.txt i uvjete korištenja. Ograniči broj zahtjeva (`PoliteHttpClient`). **Nikad ne zaobilazi prijavu, CAPTCHU ni zaštitu.** eojn.hr se ne struže (robots.txt `Disallow: /`).
- Pragovi i datumi izbora su u konfiguraciji, s datumom važenja. Nikad ih ne upisuj u kod iz sjećanja.

## Indikatori i jezik
- Pravila (A–G) su **transparentna i deterministička**, verzionirana (`rule_version`) i pokrivena jediničnim testovima sa sintetičkim fixtureima.
- Za svaki indikator vode se tri odvojene dimenzije: kvaliteta podataka, veličina odstupanja i pouzdanost dokaza. **Nikad ih ne spajaj u jedan "rizik" ili "vjerojatnost korupcije".**
- **AI procjena:**
  - svaki razlog citira polja i izvore iz konteksta;
  - uvijek navodi barem jedno moguće legitimno objašnjenje;
  - uvijek je označena "AI procjena – zahtijeva ljudsku provjeru";
  - prikazuje se uz pravila, nikad s njima spojena;
  - izlaz prolazi validator; neutemeljen izlaz se odbacuje.
- **Politika neutralnog jezika:**
  - dopušteno: "neuobičajeno", "vrijedi provjeriti", "indikator rizika", "sumnjivo" (samo uz AI procjenu s dokazima);
  - **zabranjeno:** "korupcija", "kriminal", "nezakonito", "prijevara", "pronevjera", "namješteno", i imenovanje fizičkih osoba kao odgovornih.
- **Nikad ne izmišljaj nalaze o stvarnoj nabavi.** Sintetički podaci se uvijek označavaju **"SINTETIČKI PODACI ZA DEMONSTRACIJU"**, koriste fiktivne nazive ("Općina Primjerovo", "Primjer d.o.o.") i OIB-e `000000000xx`, imaju `is_synthetic = true` i ne ulaze u statistike stvarnih podataka.
- Ugovori s fizičkim osobama se ne prikazuju, a za obrte se ne radi AI procjena (GDPR).
- **Obrti:** naziv obrta često sadrži ime vlasnika. API i UI ga prikazuju kao **"Obrt (naziv skriven)"** uz OIB i poveznicu na izvor. Obrti ne ulaze u "top" liste ni sažetke; njihovi indikatori ostaju vidljivi na razini ugovora.

## Kvaliteta i rok
- Pravila rade bez `ANTHROPIC_API_KEY`. LLM pozivi idu kroz cache i budžet (`llm_usage`).
- Tajne su samo u env varijablama. Ne commitaj `.env`.
- Kad mijenjaš kod, **ažuriraj dokumentaciju** u istom commitu (API, model, pravila, izvori).
- Ne commitaj i ne pushaj bez izričitog zahtjeva korisnika.
