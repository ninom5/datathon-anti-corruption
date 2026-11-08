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

## 2026-11-03: #42, faza 0
- **Detalj indikatora je ravan JSON** (`@JsonUnwrapped IndicatorResponse` + `contracts` + `subject`). Zašto: frontend tip `IndicatorDetail` jednostavno proširuje `Indicator`, a postojeće komponente (kartica, oznake) rade bez promjene.
- **Prag za F sprema se u `subject_ref.threshold`** (najniži prag u prozoru). Zašto: UI crta crtu praga, a ne smije ga ponovno računati (pragovi po datumu su u `thresholds.yaml`, samo na backendu).
- **Vite proxy čita `API_URL`** (zadano `http://localhost:8080`). Zašto: 8080 je lokalno zauzet tuđim procesom, a trajna promjena porta u repou nije dogovorena.

## 2026-11-03: #43 – karta i usporedba općina (IvanMosa)
- **Granice: geoBoundaries HRV ADM2 (OSM), ne DGU.** DGU nudi samo GML od 219 MB uz "ograničenje javnog pristupa" u ATOM-u (WFS je isključen), a Overpass zabranjuje `/api/` u robots.txt-u. Detalji su u `DATA_SOURCES.md` §9a. Cijena: 14 JLS nema granicu (otok Krk, Cres, Stankovci…) i navedene su uz kartu. **Ako tim nađe bolji otvoreni izvor s 556 JLS, mijenja se samo ulaz skripte.**
- **Karta bez biblioteke** (čisti SVG, ~40 redaka projekcije u `utils/geo.ts`), umjesto MapLibre ili d3-geo iz `UI_UX.md` §6. Zašto: 556 poligona u jednoj boji ne treba tile server ni novu ovisnost, a GeoJSON je 580 KB (≈150 KB gzip).
- **Boja karte = indikatori "vrijedi provjeriti" na 100 ugovora; informativni se ne broje.** Informativni su većinom vjerojatne greške u unosu (D2), pa bi bojanje po njima prikazalo kvalitetu unosa kao signal. Razredi su fiksni, a ne kvantilni, da boja zadrži značenje kad se doda podataka. Ispod 20 ugovora ploča upozorava da je omjer nestabilan (npr. Lasinja: 9 od 45 = 20).
- **API:** `GET /api/v1/municipalities` i `GET /api/v1/municipalities/{oib}/peers` umjesto `/map/counties/{code}` i `/buyers/{oib}/peers` iz `ARCHITECTURE.md`. Zašto: karta treba svih 556 JLS odjednom (i one bez naručitelja u bazi), a usporedba je po JLS (OIB iz JPT-a), ne po bilo kojem naručitelju.
- **Usporedba je po jednoj godini sklapanja** (zadano: zadnja potpuna). Miješanje godina bi uspoređivalo JLS s različitim pokrivenim razdobljima. JLS bez ugovora u toj godini nije u usporedbi i ne računa se kao 0. Kategorija bez ugovora unutar JLS s podacima je stvarna 0.
- **Nalaz (rizik za video):** uz sadašnju pokrivenost (33 JLS s podacima) gotovo nijedna skupina nema n ≥ 8 u istoj godini (npr. P5 2025.: 2 od 51). Usporedba će uglavnom pokazivati "Nedovoljno podataka". To je pošteno, ali za demo je jača priča karta (33 od 556 objavljuje strojno čitljiv registar). Mogući sljedeći korak je opcija "prosjek godišnje potrošnje preko dostupnih godina"; čeka odluku tima.
- **Vite proxy:** nakon spajanja s #42 koristi se Ninova varijabla `API_URL` (moja `API_PROXY` je uklonjena, isti cilj).

## 2026-11-03: refactor-ui – isti dizajn sustav kao OtvoreniRadar (korisnik)
- **Grana `refactor-ui` (od `43-karta-usporedba`, jer ta već ima `main` + kartu).** Frontend preuzima dizajn sustav iz `FESB/datathon`: tokeni (`--color-*`, `--space-*`, `--radius-*`, `--font-size-*`), fontovi IBM Plex Sans + Source Serif 4, shell (ljepljivo zaglavlje, aktivna stavka kao zelena crta, mobilni izbornik, podnožje), komponente `Button`/`ButtonLink`, `Badge`, `Notice`, `Card`, `PageHeader`, `EmptyState`/`ErrorState`/`LoadingState`, `BackLink`, `ExternalLink`, `FactList`, `Select`, `TextInput`, `Icon` (kopirane bez izmjena).
- Zašto: dva projekta istog tima izgledaju kao jedna obitelj proizvoda, a OtvoreniRadar sustav je već dokumentiran i provjeren za pristupačnost.
- **Zadržano iz JavniRadara (pravila domene):** jantarni signal (`--color-signal`) umjesto crvene, ljubičasta traka za sintetičke podatke (svoj ton, ne AI ton), tri odvojene dimenzije indikatora, "Što ovo NE znači".
- **Uklonjeno:** tamni način i velika slova iznad naslova (eyebrow), zaobljene "pill" oznake.
- Komponente su sada u mapi po komponenti (`components/<Name>/`), kao u OtvoreniRadaru.

## 2026-11-03: #44 – AI procjena (potvrdio korisnik)
- **Strukturirani izlaz (`output_config.format`, JSON shema iz `ANOMALY_DETECTION.md` §4.3) umjesto prisilnog poziva alata `zabiljezi_procjenu`.** Zašto: `claude-sonnet-5-5` (ponovna provjera, faza 5) vraća 400 na forsirani `tool_choice` (`tool`/`any`), a strukturirani izlaz jednako ograničava odgovor (enumi, obavezna polja). Isti obrazac već radi s Haiku 5.5 u `Desktop/datathon`. §4.3 dopušta "tool use / structured output". `minItems` i `maxLength` iz §4.3 provjerava validator, ne shema.
- **Few-shot primjeri:** §4.5 ih samo opisuje, pa su napisani kao potpuni sintetički JSON primjeri točno prema tim opisima (u `resources/ai/ai-flag-v1.txt`). Svaka promjena teksta prompta = nova verzija.
- **Nova ovisnost `com.anthropic:anthropic-java` 3.3.0** (ista kao u `Desktop/datathon`): službeni SDK s retryjem i timeoutima; alternativa (ručni `RestClient`) je više koda bez koristi.
- **Bez ključa:** `LlmClient` ima `AnthropicLlmClient` (ključ iz env-a) i `FixtureLlmClient` (testovi). Bez ključa AI run vraća "AI nije konfiguriran", a pravila i UI rade dalje.
- **Gumbi "Korisno / Pogrešno"** se ne rade u #44: `/feedback` (#45) još ne postoji.
- **Validator je stroži od §4.4** (zapisano u `ANOMALY_DETECTION.md` §4.7): tvrdnja > 300 znakova se odbacuje; zabranjena riječ u objašnjenju ili popisu podataka koji nedostaju odbacuje cijeli odgovor, jer se i to prikazuje; `visoka_pozornost` s manje od dva prihvaćena razloga spušta se na `vrijedi_provjeriti` (pravilo 7 prompta). *Prvotno pravilo "null polje nije citabilno" je ukinuto 03.11.*: odbacivalo je legitimne tvrdnje "izmjene nisu objavljene", a §4.4 traži samo da polje postoji.
- **`synthetic=true` u AI runu znači samo sintetički ugovori** (evaluacija bez troška na stvarnim), `false` samo stvarni. **`dryRun` je zadano `true`**, da pravi run uvijek traži izričit `dryRun=false`.
- **Ključ konteksta je `narucitelj`** (bez dijakritike), jer model citira putanje doslovno.
- **Paralelni pozivi (4):** 300 poziva jedan po jedan traje ~20 min. Cijena: budžet se može premašiti za najviše 4 poziva (desetinke centa s Haikuom).
- **AI procjena na `IndicatorPage` ("procjena postoji za N od M ugovora skupine") nije rađena** (Should iz prompta, izvan opsega dok nema stvarnih procjena).

## 2026-11-04: #44 – evaluacija i verzije prompta (korisnik: "radi što je najbolje")
- **Prompt `ai-flag-v3` je u upotrebi.** v1 je ponavljao pravila (17 od 20 stvarnih ugovora s indikatorom "vrijedi provjeriti"); v2 traži novi signal; v3 ispravlja sustavne oznake bez osnove nađene ručnim pregledom stvarnog uzorka (izuzeta nabava ispod praga, nedostajući podaci kao razlog). Brojke su u `docs/EVALUATION.md`.
- **Kontekst je dobio prag jednostavne nabave i vrijednost s PDV-om.** Bez praga model je "napamet" procjenjivao je li jednostavna nabava prevelika; bez bruto vrijednosti svaka isplata +25 % (PDV) izgledala je kao preplata. Oboje dolazi iz baze i `thresholds.yaml`, ne od modela.
- **Indikator C ulazi u AI kontekst samo za ugovore dobavljača kojeg imenuje.** #41 veže C na sve ugovore ćelije (naručitelj + CPV2), pa bi model na ugovoru dobavljača Y čitao tvrdnju o dobavljaču X. **Isti prikaz postoji na stranici ugovora (#41/#42) i nije mijenjan; preporuka za #47: na ugovoru drugog dobavljača prikazati C kao kontekst skupine, ne kao oznaku tog ugovora.**
- **Sintetički skup je ispravljan tri puta (v1→v4), samo zbog grešaka u podacima** (roba označena kao usluge, CPV, iznos iznad praga u skupini F). Nakon v4 se ne mijenja, da se ne prilagođava modelu; svi runovi su zapisani.
- **Pun run svih 2.212 stvarnih ugovora (~2,5–3,5 USD) je odgođen** dok se ne upišu ručne oznake i ne spoji Sudreg, da se ne plati dvaput. Za demo postoji ~90 stvarnih procjena (lokalna baza).
- **UI:** `nema_oznake` se prikazuje kao "Bez dodatnih signala", uz rečenicu da AI nije našao ništa osim onoga što pokazuju pravila (semantika v2/v3).

## 2026-11-05: #46 – Sudski registar (korisnik dao ključ)
- **Dohvat po OIB-u (`/detalji_subjekta`), ne bulk `/subjekti`:** trebamo ~1.600 dobavljača, a bulk vraća cijeli registar. 1 zahtjev/s ≈ 27 min, jednom; snimke se spremaju u bazu, demo ne ovisi o Sudregu.
- **Samo `doo`, `jdoo`, `dd`, `javno_tijelo`.** `ostalo` sadrži obrte s imenima vlasnika (npr. "… , DRAGUTIN KRIZMANIĆ"), pa se ti OIB-i ne šalju ni Sudregu ni modelu (AI kandidati sada isključuju i `ostalo` i `nepoznato`). **Riješeno 05.11. (vidi niže).**
- **Spremljen je samo dio odgovora** (bez e-mail adresa i sjedišta): minimizacija podataka.
- **NKD u dva oblika** (vidi `DATA_SOURCES.md` §7): stari oblik daje samo šifru; verzija klasifikacije se ne pretpostavlja.
- **GDPR prikaz (05.11.): naziv dobavljača tipa `ostalo`/`nepoznato` skriva se, osim ako je očito institucija** (zavod, ustanova, zadruga, muzej, institut, centar…) **ili ima JPT oznaku** (`OrganizationRef.isNameHidden`, isto u tekstovima pravila). Zašto: u 107 takvih naziva bilo je najmanje 10 osoba ("TERMODOR, DRAGUTIN KRIZMANIĆ", "Dr. Neven Martinović", "Ante Vojvodić AT CORNER"…), a heuristike za imena ne hvataju sve. Cijena: desetak tvrtki (npr. "Bauhaus") prikazuje se kao "Naziv skriven (moguć obrt ili osoba)", uz OIB i poveznicu na izvor. Klasifikator dodatno prepoznaje ",vl.", "član …" i "Ured ovl." kao obrt (vrijedi za nove uvoze; prikaz radi i bez ponovnog uvoza).
- **Pravilo B (`B_YOUNG_COMPANY`) uvedeno 05.11.** na Sudreg snimkama; detalji u `ANOMALY_DETECTION.md` §B (Implementacija). Pojas je jedina skala (≤ 60 / ≤ 120 / > 120 dana), bez posebne razine "viša pozornost".

## 2026-11-05: #44 se spaja bez ručne validacije (korisnik)
- Korisnik: ručno označavanje 60 stvarnih ugovora je preveliko za sada; grana se spaja, validacija kasnije.
- Posljedica: preciznost, odziv i stopa lažno pozitivnih **na stvarnim ugovorima ostaju neizmjereni**. U videu, README-u i na stranici Metodologija smiju se navoditi samo izmjerene brojke iz `docs/EVALUATION.md` (sintetika 40 slučajeva, udio oznaka na uzorku, kappa prema pravilima), uz napomenu da ručna validacija stvarnih ugovora nije napravljena.
- Pun AI run svih 2.212 ugovora i dalje čeka (nakon validacije ili odluke tima).


## 2026-11-08: validacija podataka i popravci (grana `validacija-bugovi`)
- **Proturječni iznosi se ne ispravljaju, samo označavaju.** Novo: `vat_ratio_implausible` (iznos s PDV-om > 1,40 × bez PDV-a) uz postojeći `gross_below_net`, sada u oba formata (A i A2). Iz registra se ne može znati koji je iznos krivo upisan (Bistra 2022: neto 1.783.805 €, bruto 22.297,56 €). Takvi redci (26) ne ulaze u grupna pravila F i C, jer bi 100× pogreška sama odredila skupinu; pravila D ih i dalje vide sa sniženom kvalitetom.
- **Iznos se čita samo iz ćelije koja sadrži broj i oznaku valute.** Prije je "3 MJESECA" postalo 0,40 €, datum "28.02.2023" iznos, a "1 500,50 kn" 1 kn. Sada je takva ćelija `unreadable_amount`.
- **`columns_shifted` (A2):** redak s vrijednostima desno od zadnjeg stupca zaglavlja (Cernik 2022, 9 redaka). Iznosi nisu pouzdani, pa redak ne ulazi u F i C.
- **`possible_duplicate`:** isti naručitelj, dobavljač, datum, početni iznos, evidencijski broj i broj ugovora, a različit EOJN link ili A2 ključ. Predmet se ne uspoređuje (ponovni upis ispravlja tipfeler). Prvi redak (najniži id) ostaje, ostali se ne računaju u pravilima. Ništa se ne briše. Oznaku postavlja `RulesService` pri svakom izračunu, pa je dovoljan `POST /api/admin/rules/run`. Bez broja ugovora redci se nikad ne spajaju (mogu biti grupe iste nabave).
- **Pravilo C označava samo ugovore najvećeg dobavljača.** Ugovori ostalih dobavljača iz ćelije ostaju u dokazima i `subject_ref`, ali više nemaju značku C (prije je 127 od 182 ugovora s C bilo tuđih). Isto je već radio AI kontekst.
- **F:** prijelaz 1.1.2023. (200.000 kn = 26.544,56 € → 26.540 €) više se ne opisuje kao promjena praga; promjena je tek razlika ≥ 1 €.
- **D1** bira prag (10 % / 15 %) po CPV-u kad vrsta predmeta nije objavljena (`RuleSupport.thresholdType`), kao F.
- **Sortiranje po iznosu** koristi `coalesce(ukupni, početni)`, kao sažetak i profil; prije je 3.626 A2 ugovora bilo na kraju.
- **Lokalna baza:** ponovni ingest preskače nepromijenjene datoteke, pa su novi ishodi parsera upisani jednokratnim SQL-om (isti uvjeti kao u kodu), a pravila ponovno izračunata. Svježi ingest na praznoj bazi daje isto, osim dva retka (5036, 5048) koje bi novi parser odbacio kao `missing_amount`; u lokalnoj bazi ostali su bez iznosa. Sigurnosna kopija prije promjena: `/tmp/before-validacija.dump` u kontejneru `javniradar-postgres`.
- **"Tromjesečna evidencija ugovora"** = EOJN tromjesečni zapis ugovora na temelju okvirnog sporazuma: D ga tretira kao okvirni, C ga ne zbraja (matični ugovor je obično u istom registru). F ga je već preskakao.
- **Izvori:** neuspjelo preuzimanje broji se jednom po URL-u (zadnji pokušaj) i ne prikazuje se ako je URL drugdje uspješno preuzet. Ingest i dalje sprema redak po pokušaju (povijest), samo prikaz je ispravljen.
- **Lista ugovora** ima `amountDoubtful` (gross_below_net, vat_ratio_implausible, columns_shifted) i tekst "iznosi u registru se ne slažu" uz iznos, da sortiranje po iznosu ne prikazuje izvornu pogrešku bez upozorenja.
