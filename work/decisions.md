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

## 2026-11-02: Rezultat provjere parsera i zaštite u pravilu D
- **Parser je točan:** 20/20 uzoraka (10 najvećih i 10 nasumičnih) podudara se s izvornim ćelijama. Nije bilo popravka ni ponovnog ingesta.
  - "0 odbačenih redaka" znači da 33-stupčani EOJN izvoz uvijek ima poveznicu, naručitelja, datum i iznos.
  - To ne znači da je parser popustljiv.
- **Greške su u objavljenim registrima:** isplata bez decimalnog zareza, jedinična cijena kao iznos ugovora, negativan PDV. Ne ispravljamo ih jer ne smijemo izmišljati vrijednosti, a provenancija mora pokazivati izvor kakav jest.
- **Odluka:** pravilo D takve retke i dalje prikazuje, ali s kvalitetom `niska`, pouzdanošću `slaba` i oznakom `informativno`, uz rečenicu o vjerojatnoj grešci u unosu.
  - Zašto ih ne skrivamo: skrivanje bi sakrilo i stvarne slučajeve.
  - Zašto nisu "vrijedi provjeriti": +12.000 % bio bi najveći "indikator" u sustavu i uništio bi vjerodostojnost u demu.
- **Heuristika `implausible-ratio: 10` nije zakonski prag**, nego pravilo kvalitete podataka. Zato je u `thresholds.yaml` u odjeljku `rule-d`, s oznakom "heuristika JavniRadara".
- **Pragovi ZJN-a** (10 %/15 %, 30 %, 50 %, jednostavna nabava) su u `thresholds.yaml` s `valid-from`/`valid-to` prema `DATA_SOURCES.md` §10.
  - Razdoblje "do 15.05.2026." nema početni datum jer ga `docs/` ne navode (prazan `valid-from` znači otvoreno).
  - HRK pragovi prije 2023. su izostavljeni jer su u `docs/` označeni kao pretpostavka.
- **Datoteka pragova** je `apps/api/src/main/resources/config/thresholds.yaml` (učitava se preko `spring.config.import`), a ne `config/` u rootu repoa, da bi bila dio JAR-a.

## 2026-11-02: Odluke korisnika prije faze 3 (API i UI)
1. **D2 s obrazloženjem u registru** ostaje `vrijedi_provjeriti` s pouzdanošću dokaza `srednja`.
   - API vraća obrazloženje kao zasebno polje `sourceExplanation` (tekst stupca "Obrazloženje iznosa plaćanja", doslovno).
   - UI ga prikazuje istaknuto, **prije postotka**, pod naslovom **"Objašnjenje naručitelja iz izvora"**.
   - Zašto: obrazloženje je dio izvora, a ne dokaz da je razlika opravdana. Korisnik ga mora vidjeti prije broja da ne bi zaključivao samo iz postotka.
2. **Obrti (GDPR):** naziv obrta često sadrži ime i prezime vlasnika.
   - API i UI ne prikazuju naziv, nego **"Obrt (naziv skriven)"** uz OIB i poveznicu na izvor.
   - Obrti se isključuju iz svih "top" lista i sažetaka.
   - Indikatori za obrte ostaju, a AI procjene za njih nema.
   - Test provjerava da naziv obrta ne izlazi iz API-ja.
3. **"Informativno – vjerojatna greška u izvoru"** (`severity_note = informativno`) je zasebna kategorija.
   - Istraživač ima filtar za nju, a sažetak brojku "N mogućih grešaka u unosu".
   - Zašto: ti zapisi nisu signal o ugovoru, nego o kvaliteti objavljenih podataka. Miješanje s "vrijedi provjeriti" bi napuhalo brojke.
4. **Git:** lokalni commitovi na grani `poc/vertical-slice` prije faze 3, nakon faze 3 i nakon faze 4. Bez pusha.

## 2026-11-02: Posljedica pravila "obrti izvan sažetaka" (faza 3)
- Sažetak isključuje obrte, pa je "N mogućih grešaka u unosu" na stvarnim podacima **1**, a filtar istraživača pokazuje **4** ugovora. Tri od četiri informativna zapisa (Lepoglava) su ugovori s obrtima.
- Odluka za POC: pravilo korisnika se poštuje. Sažetak uz brojke uvijek navodi koliko je ugovora s obrtima izostavljeno (`excludedCraftContracts`), da se brojke mogu uskladiti s tablicom.
- **Otvoreno za korisnika:** brojanje (bez naziva) ne otkriva osobu. Možda je dovoljno isključiti obrte samo iz "top" lista s nazivima, a u brojkama ih ostaviti.

## 2026-11-03: #40 – stari EOJN format (A2), JPT, DZS
- **A2 je zasebna klasa formata** (`FormatClass.A2`, `LegacyRegisterParser`). Isti tok kao A: `raw_record`, provenancija, normalizacija i GDPR klasifikacija. Prirodni ključ je hash sadržaja, jer A2 nema EOJN URL (`DATA_MODEL.md` §3).
  - Zašto: dodaje 17 JLS i povijest 2018.–2023. Pravila C i F trebaju više godina i više naručitelja.
- **Naručitelj A2 bez OIB-a u izvoru:** naziv iz retka "Naručitelj:" ili izdavatelj CKAN skupa. OIB se traži jedinstvenim točnim podudaranjem normaliziranog naziva u JPT-u, a ako ga nema, postojećom organizacijom s istim nazivom i OIB-om. Bez nagađanja (dvije "Privlake" daju "nije pronađen").
- **Valuta u A2:** redoslijed je oznaka u ćeliji, oznaka u zaglavlju, pa datum. Izričiti "EUR" u ćeliji sada pobjeđuje datum i u `Money` (klasa A ima brojeve, pa na nju ne utječe).
- **D2 i zamjena kune i eura:** omjer isplate i ugovorenog iznosa s PDV-om unutar 1,5 % od 7,5345 daje `informativno`, kvalitetu `niska` i objašnjenje o vjerojatnoj zamjeni valute. Prag je u `thresholds.yaml` (`rule-d.currency-mix-tolerance`, heuristika, ne zakon).
  - Zašto: na A2 je takvih bilo 6 od 76 D2 indikatora, i to među najvećima (+653 %). Nijedan ne bi preživio ručnu provjeru, a srušio bi vjerodostojnost u demu.
  - Iznose ne pretvaramo, jer se iz registra ne vidi koji je od dva iznosa ispravan.
  - `rule_version` ostaje `rules-v1`, jer se indikatori te verzije i dalje računaju iznova u svakom runu. Prije prvog stvarnog korisnika prelazimo na verzioniranje s `povucen`.
- **Otvoreno za ručnu provjeru:** nekoliko D2 slučajeva s omjerom 9,0–9,99 (npr. Ivanec: 11.010,44 € ugovoreno, 110.101,44 € isplaćeno) izgleda kao pomaknut decimalni zarez. Ispod su praga `implausible-ratio: 10`, pa ostaju "vrijedi provjeriti". Ne mijenjamo prag naslijepo; odluka nakon pregleda 10 primjera (#41).
- **JPT i DZS:** `jpt_body` i `municipality` po OIB-u (odstupanje od `DATA_MODEL.md` §2 opisano je u §8a). JPT i DZS osvježavaju se na početku svakog ingest runa; pad jednog izvora bilježi se kao `reference_source_failed` i ne zaustavlja run. DZS uvoz je idempotentan (ponovno spajanje nad istom datotekom).
- **Grad Hvar (Could) nije rađen:** TLS posredni certifikat u vlastitom truststoreu ostaje otvoren zadatak. Provjera se ne isključuje.
- **Korisnik 2026-11-03: asistent nikad ne commita** (ni push ni PR). Promjene ostaju necommitane na grani `40-a2-jpt-dzs`.

## 2026-11-03: #41 – pravila F i C, pojas veličine odstupanja (potvrdio korisnik)
- **Pragovi jednostavne nabave 2017.–2022. su provjereni** (ZJN 2016 čl. 12 st. 1, NN 120/16; na snazi od 01.01.2017., čl. 452): 200.000 kn za robe i usluge, 500.000 kn za radove. U `thresholds.yaml` su preračunati po fiksnom tečaju 7,53450 (26.544,56 € / 66.361,40 €), uz izvorni iznos u kunama. Zbog toga F pokriva i A2 podatke od 2017. do 2022.
- **Skupna pravila:** dodano je sučelje `GroupRule`, nad svim ugovorima jednog naručitelja. `ContractRule` ostaje za pravila nad jednim ugovorom. Migracija `V3` dodaje `indicator.buyer_id`, `indicator.magnitude_band` i tablicu `indicator_contract`, da filtar `/contracts?ruleCode=F` i detalj ugovora vide skupne indikatore.
- **F, detalji kojih nema u `docs/`:**
  - vrsta predmeta za A2 (`subject_type` je NULL) izvodi se iz CPV-a: `45` = radovi, ostalo = robe/usluge;
  - kriterij prozora je razmak prvog i zadnjeg ugovora ≤ 90 dana (točno 90 ulazi);
  - prozor koji prelazi promjenu praga koristi **najniži** prag u prozoru;
  - prozori se ne preklapaju (nakon prijavljenog prozora traženje nastavlja iza njegovog zadnjeg ugovora), pa isti ugovori ne daju više indikatora;
  - **CPV2 varijanta:** ako ugovori istog dobavljača imaju različite CPV3, ali isti CPV2, i samo zajedno prelaze prag, indikator nastaje s dokazom `slaba`.
- **C:** razdoblje su **3 zadnje kalendarske godine u kojima naručitelj ima ugovore** (za naručitelje samo s A2 to je npr. 2021.–2023.). Vrijednost je `coalesce(ukupni, početni)`. Obrti ulaze u izračun, ali se u tekstu ne imenuju.
- **Pojas veličine odstupanja (`band`: `nizak`/`srednji`/`visok`)** je heuristika JavniRadara, a ne zakonski prag. Granice su u `thresholds.yaml` (`bands`):
  - D1: ≤ 30 % / 30–50 % / > 50 % (čl. 316, 317);
  - D2: ≤ 30 % / 30–100 % / > 100 %;
  - F (zbroj / prag): < 1,5× / 1,5–2× / ≥ 2×;
  - C (HHI): < 7.500 / 7.500–9.000 / ≥ 9.000.
  - Zašto: filtar "pojas" u UI-ju i izvješće po pojasu trebaju jednu ljestvicu preko svih pravila, a veličina odstupanja ostaje zaseban broj s jedinicom.

## 2026-11-03: #41 – lažno pozitivni nakon ručnog pregleda (korisnik: "radi što misliš da je najbolje")
- **F ne uzima ugovore iz otvorenog postupka.** Pregled 164 stvarna F indikatora našao je 16 skupina koje su grupe (lotovi) jednog otvorenog postupka (npr. Osijek: 4 projekta istog dana; Stankovci: 5 prostornih planova). Takvi ugovori su prošli javni postupak, pa ne mogu biti izbjegavanje postupka. Vrsta postupka je ulaz pravila F u `ANOMALY_DETECTION.md` §F; sada se i koristi. Izuzima se svaki postupak javne nabave po ZJN-u, tj. vrsta postupka koja sadrži riječ "postupak" (otvoreni, ograničeni, pregovarački…). Ostaju "Jednostavna nabava" i "Nabava izuzeta od primjene ZJN" (`thresholds.yaml`, `rule-f.public-procedure-markers`). Na stvarnim podacima vrste su: jednostavna nabava 3.166, izuzeta 2.010, otvoreni postupak 754, pregovarački 28.
- **C traži da najveći dobavljač ima ≥ 2 ugovora u ćeliji** (`rule-c.min-top-contracts`). Scenarij C je "ponovljene dodjele"; u oko 7 od 22 stvarnih slučajeva udio je nosio jedan veliki ugovor (npr. Osijek 1 od 29, Štrigova 1 od 8).
- **D2: `implausible-ratio-without-note: 5`.** Isplata ≥ 5× ugovorenog s PDV-om **bez obrazloženja u registru** daje kvalitetu `niska` i `informativno`. S obrazloženjem ostaje granica 10×. Razlog: Ivanec 2021.–2022. ima niz isplata 9–10× bez obrazloženja (npr. 11.010,44 → 110.101,44 €), što izgleda kao greška u unosu. Slučajevi s obrazloženjem ("dodatni radovi", "povećanje cijena") ostaju "vrijedi provjeriti".
- **Nije uvedeno:** oznaka mogućeg duplikata (dva identična retka istog dana). Rijetko je (1 slučaj), a moglo bi sakriti stvarne ugovore.
