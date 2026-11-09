# JavniRadar – projektna dokumentacija

_Tim **Nazor** · Datathon Portala otvorenih podataka 2026 · **izazov A) Društvo i javne usluge** (transparentnost javnog sektora, građansko sudjelovanje)._

**Demo:** https://javniradar.zulja.org

> Indikatori nisu optužbe. Pokazuju što vrijedi provjeriti.

**Izvor brojki u ovom dokumentu**, ako nije drukčije navedeno: lokalna baza nakon ingesta 2026-11-02 i 2026-11-03 (Sudreg 2026-11-05), očitano 2026-11-08 preko `GET /api/v1/contracts/summary`, `GET /api/v1/sources/summary` i SQL upita. Popis skupova je u [`DATA_SOURCES.md`](DATA_SOURCES.md).

## 1. Problem

- Hrvatska ima **556 jedinica lokalne samouprave** (128 gradova, 428 općina; izvor: JPT i DZS Popis 2021). Većina su male općine bez lokalnih medija koji prate trošenje.
- Registar ugovora je zakonska obveza (ZJN, čl. 28), ali je podatak rascjepkan: na data.gov.hr samo **oko 61 od 556 JLS (~11 %)** objavljuje strojno čitljiv registar ugovora (CKAN API, 2026-11-01, [`DATA_SOURCES_RESEARCH.md`](DATA_SOURCES_RESEARCH.md) §1.1), u desecima različitih tablica.
- Nacionalni izvoz na portalu je uzorak od 20.000 redaka, a EOJN OCDS nema OIB-ova.
- Zato građanin, vijećnik ili lokalni novinar ne može lako odgovoriti na pitanje: *koliko moja općina troši, kome, i odstupa li nešto od uobičajenog?*

## 2. Ciljani korisnici

| Korisnik | Što treba | Što mu JavniRadar daje |
|---|---|---|
| Građanin | razumjeti trošenje svoje općine | karta, profil općine, iznosi po godini i kategoriji, jednostavan jezik |
| Lokalni novinar, udruga | naći ugovore koje vrijedi provjeriti | filtar indikatora, dokazi, poveznica na izvorni redak i EOJN |
| Općinski vijećnik | nadzor izvršne vlasti | izmjene ugovora, preplate, glavni dobavljači, usporedba sa sličnim općinama |
| Unutarnji revizor, Državni ured za reviziju | uzorak za reviziju | indikatori s tri odvojene dimenzije (kvaliteta podataka, veličina odstupanja, pouzdanost dokaza) |
| Sama općina | samoprovjera | vlastiti profil i kvaliteta objavljenih podataka |

## 3. Rješenje i glavne funkcije

JavniRadar automatski preuzima općinske registre ugovora s data.gov.hr, spaja ih s registrom javnopravnih tijela, popisom stanovništva i Sudskim registrom, i na njima računa transparentna pravila. AI procjena dodaje drugo mišljenje, uvijek s dokazima.

| Funkcija | Stranica | Stanje |
|---|---|---|
| Karta svih 556 JLS (ima li registar, broj ugovora, indikatori na 100 ugovora) | `/karta` | ✅ |
| Profil naručitelja: potrošnja po godini i CPV kategoriji, glavni dobavljači, indikatori, izvori, JPT i DZS podaci | `/opcina/{oib}` | ✅ |
| Istraživač ugovora s filtrima (naručitelj, godina, pravilo, pojas, ozbiljnost) | `/istrazi` | ✅ |
| Detalj ugovora: iznosi, provenancija (skup, datoteka, redak, vrijeme dohvata), indikatori, AI procjena | `/ugovor/{id}` | ✅ |
| Detalj indikatora: tri dimenzije, dokazi, „što ovo NE znači” | `/indikator/{id}` | ✅ |
| Usporedba općine sa skupinom slične veličine (€ po stanovniku, medijan, rang) | `/usporedba` | ✅ (često „nedovoljno podataka”, vidi §9) |
| Izvori i kvaliteta podataka: što je uvezeno, što nije i zašto | `/izvori` | ✅ |
| Pravila: **D** izmjene i preplate, **F** moguće cjepkanje nabave, **C** koncentracija dobavljača, **B** mlada tvrtka | – | ✅ |
| AI procjena „zahtijeva ljudsku provjeru” s validatorom dokaza | detalj ugovora | ✅ |
| Zaštita privatnosti: fizičke osobe skrivene, obrti bez naziva | sve | ✅ |

(Putanje su iz `apps/web/src/routes/routes.ts`.)

## 4. Primjer korištenja

_Ilustrativan tok. Primjer koristi **sintetičke podatke za demonstraciju** (fiktivna „Općina Primjerovo”), ne stvarnu općinu._

1. Novinarka otvori **kartu** i vidi da njezina općina ima objavljen registar i nekoliko indikatora.
2. Na **profilu općine** vidi da jedan dobavljač u kategoriji „građevinski radovi” nosi većinu vrijednosti u zadnje tri godine (pravilo C), a da je nekoliko ugovora istog predmeta potpisano unutar 90 dana, zbrojeno iznad praga jednostavne nabave (pravilo F).
3. Otvori **indikator**: vidi izračun, dokaze (ugovore i retke izvorne datoteke), pojas odstupanja i odjeljak „što ovo NE znači” (npr. lotovi otvorenog postupka ili komunalno poduzeće u vlasništvu općine).
4. Na **detalju ugovora** pročita AI procjenu: dva razloga s citiranim poljima i jedno moguće legitimno objašnjenje.
5. Iz poveznice na **izvorni redak** i EOJN pripremi zahtjev za pristup informacijama općini. JavniRadar ne tvrdi da je išta nepravilno.

## 5. Podaci i nova vrijednost iz spajanja

| Izvor | Kako | Što daje |
|---|---|---|
| 40 registara ugovora (data.gov.hr) | CKAN API + datoteke | **5.958 ugovora**, 38 naručitelja (34 JLS, 2 županije, 2 javna tijela), 2.068 dobavljača, 2014.–2026. (najviše 2018.–2025.) |
| JPT – popis javnopravnih tijela (MPUDT) | REST API | 4.678 tijela s OIB-om, vrstom, županijom i **osnivačima** |
| DZS Popis 2021 | XLSX | stanovništvo svih 556 JLS, usporedne skupine po veličini |
| Sudski registar | REST API (OAuth2) | 1.569 snimki dobavljača: datum osnivanja, djelatnost (NKD), stečaj/likvidacija |
| geoBoundaries / OSM | GeoJSON | granice 542 JLS za kartu |

**Što nastaje tek spajanjem** (nije vidljivo ni u jednom izvoru zasebno):
- **€ po stanovniku** i usporedba sa sličnim općinama (ugovori × DZS);
- **„naručitelj je osnivač dobavljača”**: u stvarnim podacima ima 15 takvih parova (npr. grad i vlastito komunalno društvo). To se prikazuje kao legitiman kontekst, pa smanjuje lažne uzbune pravila C i F (ugovori × JPT; [`DATA_SOURCES_RESEARCH.md`](DATA_SOURCES_RESEARCH.md) §5);
- **mlada tvrtka i nesklad djelatnosti i predmeta ugovora** (ugovori × Sudski registar);
- **jedinstven pogled** na 40 različito formatiranih datoteka, s HRK → EUR pretvorbom i provenancijom svakog retka.

**Rezultati na stvarnim podacima** (izvor: `indicator`, `ai_assessment`, 2026-11-08, nakon validacije podataka):
- **262 indikatora** pravila na stvarnim podacima: D izmjena vrijednosti 16, D isplata iznad ugovorenog 86 (od toga 28 informativnih, npr. moguća zamjena kuna i eura u izvoru), F 146, C 14, B 0;
- **520 od 5.849 prikazanih ugovora** ima barem jedan indikator, 488 s oznakom „vrijedi provjeriti” (pravilo C označava samo ugovore najvećeg dobavljača);
- **provjera kvalitete podataka**: 19 dvostrukih upisa istog ugovora računa se jednom, a 26 redaka s proturječnim iznosima s PDV-om i bez njega te 9 redaka s pomaknutim stupcima ne ulazi u pravila F i C (oznake su vidljive na stranici ugovora);
- **AI procjena** na 280 stvarnih ugovora: 104 „vrijedi provjeriti”, 176 bez oznake, 0 prikazanih odgovora bez dokaza (validator).

## 6. Vrijednost i utjecaj

- **Za zajednicu:** jedan pregled lokalne nabave umjesto desetaka tablica; jezik koji ne optužuje, nego upućuje na provjeru i izvor.
- **Za javni sektor:** stranica „Izvori” pokazuje koje datoteke nisu strojno čitljive i zašto (od 155 pregledanih datoteka uvezeno je 55; 72 su u formatu koji nije standardni EOJN izvoz, 20 je nedostupno). To je konkretan popis za poboljšanje objave podataka.
- **Za nadzorna tijela:** filtriranje po pravilu i pojasu daje uzorak za reviziju, s dokazima.
- **Mjerljivo:** pokrivenost (34/556 JLS), broj indikatora, udio ugovora s izvorom (100 % ima provenanciju).

## 7. Inovativnost

- **Hibrid pravila i LLM-a s validatorom dokaza:** pravila su deterministička i verzionirana; AI (Claude Haiku 5.5, strukturirani izlaz po JSON shemi) traži ono što pravila ne vide (npr. djelatnost dobavljača ne odgovara predmetu, iznos „jednostavne nabave” iznad praga). Svaki razlog mora citirati polje iz konteksta, a validator odbacuje neutemeljen odgovor prije prikaza.
- **Tri odvojene dimenzije** (kvaliteta podataka, veličina odstupanja, pouzdanost dokaza) umjesto jednog „rizika”.
- **„Što ovo NE znači”** uz svaki indikator i izmjerena evaluacija AI-ja (sintetika 40 slučajeva: preciznost 1,00, odziv 0,905–0,952, 0 lažno pozitivnih; [`EVALUATION.md`](EVALUATION.md)).
- **Ponovljiv ingest** stvarnih, neurednih općinskih tablica (dva EOJN formata, HRK/EUR, pogreške u izvoru zadržane i označene).

## 8. Izvedivost i stanje razvoja

- **Radni prototip** na stvarnim podacima: backend (Spring Boot 4, Java 25, PostgreSQL 17) i web (React 19, TypeScript, Vite). Arhitektura: [`ARCHITECTURE.md`](ARCHITECTURE.md).
- **Testovi:** `./mvnw test` 88/88; `yarn build`, `yarn lint`, `yarn test` 11/11 (2026-11-08).
- **Točnost parsera** provjerena na 40 ugovora prema izvornim ćelijama: 40/40.
- **Trošak AI-ja:** ukupno 0,73 USD za sve dosadašnje pozive (609), uključujući evaluaciju.
- **Nije gotovo:** obrazac za ispravak, OpenAPI specifikacija, ručne oznake za evaluaciju na stvarnim podacima.

## 9. Iskrena ograničenja

- Pokriveno je **34 od 556 JLS**. Ostale ili ne objavljuju registar na portalu ili ga objavljuju u formatu koji još ne parsiramo (vlastite tablice, PDF).
- **Broj ponuda nije dostupan** ni u jednom provjerenom strojno čitljivom izvoru, pa „niska konkurencija” nije pokrivena.
- **Usporedba općina** često nema dovoljno usporedivih općina (n < 8) i tada to izričito kaže.
- **Pravilo B** (mlada tvrtka) na stvarnim podacima ne daje indikator (31 mlada tvrtka, svi ugovori ispod 50.000 €); prikazuje se samo na sintetičkom primjeru.
- **AI preciznost na stvarnim podacima nije izmjerena**: uzorak od 60 ugovora čeka ručne oznake tima. Sintetički skup je malen.
- Greške u izvornim registrima prenose se vjerno i označavaju kao kvaliteta podataka.
- Pravna osnova za prikaz obrta i podataka o tvrtkama nije pravno provjerena (vidi [`ARCHITECTURE.md`](ARCHITECTURE.md) §9.3).

## 10. Daljnji razvoj

| Rok | Što |
|---|---|
| **Kratkoročno** (1–3 mjeseca) | noćni ingest; ručne oznake i mjerenje AI-ja na stvarnim podacima; OpenAPI; obrazac za ispravak i pravo na odgovor; parseri za vlastite tablice s OIB-om (klasa B) |
| **Srednjoročno** (3–12 mjeseci) | LLM ekstrakcija iz PDF i nestrukturiranih registara; planovi nabave (plan → ugovor); isplate iz proračuna (Naputak NN 59/23) za tok plan → ugovor → isplata; obavijesti po općini; izvoz normaliziranog skupa natrag na data.gov.hr |
| **Dugoročno** | izravan EOJN feed s OIB-om i brojem ponuda (od 16.05.2026. registar ugovora se objavljuje u EOJN-u); svih 556 JLS; partnerstvo s udrugom, fakultetom ili MPUDT-om za održavanje |

## 11. Tim

| | |
|---|---|
| Tim | **Nazor** |
| Autori | Ivan Mosić, Šime Jadrijev, Luka Žuljević, Nino Milas |
| Mentor (voditelj tima) | Mario Čagalj |
| Licenca | MIT (`LICENSE`) |
| Repozitorij | https://github.com/ninom5/datathon-anti-corruption |
