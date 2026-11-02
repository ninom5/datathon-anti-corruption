# Odluke

_Format: datum, odluka, zašto, alternative koje smo odbacili._

## 2026-10-22: Ispravak: prijavu ispunjava svaki član, ne samo mentor
- Ranija bilješka (iz pozivnog maila) govorila je da prijavu podnosi mentor. Pravila (VI) i Javni poziv (V) kažu: **svaki član tima ispunjava prijavni obrazac** i navodi naziv tima + kontakt mentora. Mentor je voditelj tima i komunicira s organizatorom.
- Posljedica: svi 4 člana moraju stići ispuniti obrazac do 23.10. 24:00, a mentor mora biti dogovoren da bi se njegovi podaci mogli upisati.

## 2026-10-22: Preporuka: prijaviti jedan izazov, ne sva tri (prijedlog, čeka potvrdu tima)
- Pravila zahtijevaju **zasebno rješenje po izazovu**. Za 4 osobe u ~17 dana to znači 3 plitka rješenja umjesto jednog dobrog.
- Izvedivost, UI/UX i video nose 30 od 60 bodova. To se dobiva dubinom, ne širinom.
- Napomena: prijava je besplatna i ne obvezuje na predaju, pa se može prijaviti i drugi izazov "u rezervi" ako obrazac to dopušta. Ne treba ga razvijati.
- Odbačeno: prijava na sva tri izazova s ciljem tri nagrade. Realno se ne stigne.

## 2026-10-22: Bodovni profil određuje strategiju
- Max 60 b. Pragovi: nagrada se ne mora dodijeliti ispod **50 b**, a izjednačenje se rješava po "Izvedivost", pa "Upotreba otvorenih podataka".
- Težište: radni prototip (10), UI/UX (10), video (10). Ostalih šest kriterija po 5.
- Podaci izravno preko **API-ja** (kriterij "Upotreba otvorenih podataka") i **povezivanje više izvora** (kriterij "Povezivanje") jeftini su bodovi ako se planiraju od početka.
- Razrađeni koncept je dopušten, ali bez prototipa gubimo većinu od 10 b za izvedivost.

## 2026-10-22: Korištenje AI alata je dopušteno
- Pravila VIII dopuštaju AI alate ako ne krše licence i ako se **navedu u dokumentaciji**. Rješenje mora ostati "izvorno djelo tima". Zato u dokumentaciju ide odjeljak "Korišteni alati i biblioteke" uključujući AI.

## 2026-10-22: Predaja koda na javni repozitorij: OTVORENO, čeka sigurnosnu provjeru
- Pravila traže "izvorni kod ili poveznicu na javni repozitorij", a objava pod OSS licencom se boduje. Organizacijska sigurnosna politika zabranjuje objavu sadržaja na javne platforme.
- Do jasnog odgovora kod se **ne objavljuje javno**. Opcije: (a) pitati security/AppSec smije li se osobni projekt za natjecanje objaviti, (b) predati kod kao arhivu ili link za preuzimanje (Pravila to dopuštaju za prevelike privitke), ali link mora trajati do objave rezultata i mora biti na odobrenom mjestu (npr. SharePoint).
- Odluka ovisi o korisniku jer je pitanje politike organizacije, ne tehničko.

## 2026-11-01: Projekt JavniRadar (izazov A), dokumentacija u `docs/`
- Korisnik je zadao brief za **JavniRadar** (analiza lokalne javne nabave, pravila + AI procjena). Rok predaje 9.11. Detalji su u `docs/`.
- **Glavni izvor: općinski registri ugovora u standardnom EOJN XLSX formatu** (~30 JLS na data.gov.hr, 2.425 ugovora u strogo parsiranom podskupu). Zašto: jedina shema s OIB-om obiju strana, CPV-om, izmjenama, isplatama i poveznicom na eojn.hr. Nacionalni JSON izvoz (20.000 redaka) je uzorak (Osijek: 32 naspram 588), a OCDS nema OIB.
- **MVP pravila: D, F, C.** Zašto: jedina za koja postoje podaci. E nije izvedivo (nema broja ponuda nigdje). B ovisi o Sudreg ključu. A samo kao kategorija po stanovniku.
- **Stack: preporuka Java/Spring Boot + React (postojeći skelet), a ne FastAPI iz briefa.** Zašto: reusable kod (PoliteHttpClient, Sudreg, AI paket) postoji samo u Javi u `Desktop/datathon`. ČEKA POTVRDU TIMA.
- **ZJN je izmijenjen (NN 48/26, 16.05.2026.):** pragovi jednostavne nabave su sada 50.000/100.000 EUR (prije 26.540/66.360); registar ugovora se objavljuje u EOJN-u; upis od 5.000 EUR. Novi Pravilnik NN 86/2026 propisuje 31 obvezno polje. Pragovi idu u konfiguraciju s datumom važenja.
- Ne strugamo eojn.hr (robots.txt `Disallow: /`).
- Otvoreno: odnos prema projektu natječaja (izazov C, `Desktop/datathon`). Oba projekta punim kapacitetom u 8 dana nisu realna.

## 2026-11-02: Stack potvrđen: Java/Spring Boot + React
- Korisnik je potvrdio Javu. Obrazloženje je u `docs/ARCHITECTURE.md` §1: kod za ponovnu upotrebu postoji samo u Javi, a skelet je već postavljen. Python ostaje samo za istraživanje i evaluaciju.

## 2026-11-02: POC uključuje sintetički skup s poznatim ishodom
- Korisnik želi testne podatke koji moraju biti označeni, da se provjeri radi li pravilo D. Sintetika ide kroz isti parser (EOJN format) i dobiva očekivane oznake. Integracijski test ispisuje TP/FP/TN/FN i pada na promašaju jasnog slučaja.
- Zašto: stvarni podaci nemaju ground truth, pa bez ovoga ne znamo je li pravilo točno ili samo tiho. Isti skup kasnije služi kao 40 sintetičkih slučajeva u evaluaciji (`MVP_ROADMAP.md`, 06.11.) i za demo.

## 2026-11-02: Odluke za POC (potvrdio korisnik)
- **Nove ovisnosti:** Flyway (shema traži CHECK/JSONB/`pg_trgm`, `ddl-auto` to ne može), Apache POI `poi-ooxml` (XLSX), Testcontainers Postgres za integracijske testove (H2 ne podržava JSONB/`pg_trgm`). `ddl-auto` prelazi na `validate`.
- **Nečitljiv datum:** nečitljiv *datum sklapanja* → redak se odbacuje (`ingest_issue`, `ARCHITECTURE.md` §6) i to je ispravan ishod u sintetičkom skupu ("odbačen"). Slučaj loše kvalitete radi se s nečitljivim *datumom izvršenja*: zapis ostaje, `data_quality` pada na `srednja`. Zašto: `date_signed` je NOT NULL i o njemu ovisi izbor praga; izmišljanje datuma nije dopušteno.
- **Sintetički OIB-i:** raspon `000000000xx` u sintetičkom izvoru smatra se rezerviranom oznakom i ne snižava kvalitetu. Slučaj "neispravan OIB" koristi neispravan format (npr. 10 znamenki). Zašto: AGENTS.md traži `000000000xx` (namjerno nevaljani checksum), pa bi inače sva sintetika imala nisku kvalitetu.
- **Sintetika u UI-ju:** filter "prikaži sintetičke" u istraživaču (zadano isključen) umjesto zasebne rute `/demo`; `UI_UX.md` §4 ažuriran. Sintetički EOJN URL-ovi su `https://primjerovo.invalid/contract-eo/S…` (rezervirana domena `.invalid`) da nikad ne vode na stvarne ugovore.
- **Test zabranjenih riječi nema iznimki za negacije:** "ne znači korupciju" mora pasti. Tekstovi "Što ovo NE znači" formuliraju se bez zabranjenih riječi.
- **Pragovi** u `thresholds.yaml` samo iz `docs/` (ZJN NN 48/26, `DATA_SOURCES.md` §10, `ANOMALY_DETECTION.md` §D), svaki s izvorom i `valid_from`.
- **Rad po fazama:** nakon faze 1 stati i dati izvješće (JLS, ugovori klase A, odbačeni redci po razlogu, resursi po klasama); faza 2 tek nakon potvrde.
- **Zasebna lokalna baza (2026-11-02):** `apps/api/docker-compose.yml` je imao isti naziv kontejnera (`datathon-postgres`) i projekta kao repo `Desktop/datathon`, pa je `docker compose up` pokrenuo njihov kontejner s njihovim podacima (13 tablica natječaja). Ništa u njoj nije mijenjano. JavniRadar sada ima vlastiti compose projekt `javniradar`, kontejner `javniradar-postgres`, bazu `javniradar` na portu **5434**. Zašto: dva projekta ne smiju dijeliti shemu ni volumen.
- **Klasa A = samo 33-stupčani EOJN izvoz (2026-11-02).** Ingest je pokazao da "53 datoteke klase A" iz `DATA_SOURCES.md` uključuju i **stari EOJN izvještaj** (18 stupaca, naziv i OIB u jednoj ćeliji, bez poveznice na eojn.hr i bez iznosa izmjene; ~20 datoteka, 14+ izdavatelja, pretežno do 2023.). Za POC ga ne parsiramo (klasificira se kao B). Zašto: nema prirodnog ključa (EOJN URL) za deduplikaciju ni ulaza za D1 (izmjene); D2 (preplata) bi bio moguć. **Prijedlog za kasnije:** zaseban parser "A2" (pola dana rada) bi dodao ~14 izdavatelja i povijest 2018–2023, korisno za G i usporedbe. Odluka korisnika nakon faze 1.
- **GDPR klasifikacija dobavljača pooštrena (2026-11-02)** nakon pregleda stvarnih naziva: osobna imena VELIKIM SLOVIMA, OPG, odvjetnički uredi, "Ured ovlaštenog arhitekta/inženjera Ime Prezime" i nazivi obrta u formatu "NAZIV, IME PREZIME, MJESTO, ADRESA" vode se kao `fizicka_osoba` ili `obrt`. Zašto: radije sakriti tvrtku nego prikazati privatnu osobu; obrti se prikazuju bez AI procjene.

## 2026-11-02: Odluke nakon faze 1 (potvrdio korisnik)
- **Stari EOJN izvještaj (parser "A2") ne ulazi u POC**, ali je **prvi zadatak nakon POC-a, prije pravila F i C**. Zašto: koncentracija (C) i cjepkanje (F) traže više godina i više JLS da bi usporedbe imale smisla; trenutna klasa A ima 17 JLS i pretežno 2024.–2025. Za D (izmjene) A2 ne pomaže jer nema iznosa izmjene.
- **Grad Hvar se preskače u POC-u**, TLS provjera se ne dira. Opcija za kasnije: dodati nedostajući posredni certifikat u vlastiti truststore (samo za taj host), nikad isključiti provjeru.
- **Provjera točnosti parsera prije faze 2:** 0 odbačenih redaka može značiti previše popustljiv parser. Usporedba 10 najvećih i 10 nasumičnih ugovora s izvornim ćelijama i pregled distribucije iznosa po naručitelju (greške decimalnog separatora, HRK/EUR). Greška → popravak + test + ponovni ingest.
- **Tip naručitelja se razlikuje u izvješćima i API-ju:** općina/grad (`jls`), županija (`zupanija`), ostalo (`javno_tijelo`). Vrtić i županija nisu JLS. **2026. se označava kao nepotpuna godina** (podaci do datuma objave datoteke).
- **Nakon faze 2 stati** i dati: matricu TP/FP/TN/FN na sintetici, broj stvarnih D indikatora po pojasu i 10 primjera za ručnu provjeru.
