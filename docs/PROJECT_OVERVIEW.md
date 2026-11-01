# JavniRadar – pregled projekta

_Datathon Portala otvorenih podataka 2026 · Izazov **A) Društvo i javne usluge** · predaja do **09.11.2026. 24:00**._

## 1. Problem
- Hrvatska ima **556 jedinica lokalne samouprave** (128 gradova, 428 općina; DZS Popis 2021, JPT) [VERIFICIRANO]. Većina su male općine bez lokalnih medija koji bi pratili trošenje.
- Registar ugovora je zakonski obvezan (ZJN čl. 28; od 16.05.2026. objavljuje se u EOJN-u) [VERIFICIRANO]. U praksi su podaci rascjepkani:
  - na data.gov.hr samo **61 od 556 JLS (~11 %)** objavljuje strojno čitljiv registar ugovora [VERIFICIRANO, CKAN API, 2026-11-01];
  - formati su različiti (XLSX, CSV, PDF, DOCX);
  - nacionalni izvoz na portalu je nepotpun uzorak od 20.000 redaka;
  - EOJN OCDS nema OIB-ove.
- Rezultat: građanin, vijećnik ili lokalni novinar ne može odgovoriti ni na osnovno pitanje: "koliko moja općina troši na ceste po stanovniku u odnosu na slične općine i kome ide taj novac?"

## 2. Ciljni korisnici
| Korisnik | Posao koji treba obaviti | Što mu JavniRadar daje |
|---|---|---|
| Građanin | razumjeti trošenje svoje općine | profil JLS, usporedba po stanovniku, jednostavni jezik |
| Lokalni novinar | naći priču koju vrijedi istražiti | indikatori s dokazima, AI procjena, ZPPI predložak |
| Udruge (watchdog) | sustavno praćenje | izvoz, RSS po općini |
| Općinski vijećnik | nadzor izvršne vlasti | usporedba sa skupinom, popis izmjena ugovora |
| Unutarnji revizori, Državni ured za reviziju | odabir uzorka za reviziju | filtar "ima indikator", tri dimenzije pouzdanosti |
| Sama općina | samoprovjera i benchmarking | usporedba sa sličnima, pravo na odgovor |

## 3. Vizija
JavniRadar čini lokalnu javnu nabavu **razumljivom, pretraživom i usporedivom**:
- spaja registre ugovora, registar javnopravnih tijela, popis stanovništva i (uz ključ) Sudski registar;
- transparentnim pravilima i AI procjenom ističe obrasce koje **vrijedi provjeriti**;
- ne tvrdi nikad da je nešto nezakonito.

## 4. MVP (8 dana) i budućnost
| MVP (do 09.11.) | Budućnost |
|---|---|
| ~30 JLS s registrom u standardnom EOJN formatu (data.gov.hr) + nacionalni uzorak kao benchmark | svih 556 JLS preko izravnog EOJN API-ja/OCDS-a s OIB-om |
| JPT API + DZS stanovništvo + usporedne skupine | proračuni i isplate (Naputak NN 59/23) za tok plan → ugovor → isplata |
| Pravila **D** (izmjene/preplate), **F** (cjepkanje), **C** (koncentracija) | B (Sudreg), E (broj ponuda iz TED-a/EOJN-a), G s 5+ godina |
| AI procjena (Haiku + validator), na ugovorima s indikatorom ili ≥ 20 k € | Sonnet provjera svih `visoka_pozornost`, aktivno učenje iz povratnih informacija |
| Karta, profil JLS, istraživač, detalj indikatora, izvori | obavijesti e-mailom, mreža naručitelj–dobavljač, API za novinare |
| Obrazac za ispravak / pravo na odgovor | moderacijsko sučelje, potvrđeni računi općina |
| Sintetički demo (A, B) jasno označen | – |

## 5. Diferencijacija
- **Od generičkog dashboarda nabave:**
  - (1) fokus na **male općine** i normalizaciju **po stanovniku** unutar usporedne skupine;
  - (2) **tri odvojene dimenzije** (kvaliteta, veličina, pouzdanost) umjesto jednog "rizika";
  - (3) uz svaki broj **dokaz** (redak izvorne datoteke, poveznica na EOJN);
  - (4) "što ovo NE znači" i pravo na odgovor.
- **Od postojećih alata** (detalji u `DATA_SOURCES.md` §11):
  - MFin "trošenje sredstava državnog proračuna" pokriva državnu riznicu, ne JLS i ne nabavu;
  - IJF mjeri objavu proračunskih dokumenata, ne ugovore;
  - Integrity Watch Red Flags gleda samo TED (iznad EU pragova), pa male općine ne vidi;
  - opentender.eu ima HR podatke do 2024. preko OCDS-a bez OIB-a i bez lokalnih registara (stranica nam nije bila dostupna, pa sadržaj nije provjeren);
  - Imamo pravo znati je komplement (ZPPI zahtjevi), na koji JavniRadar upućuje.
- **AI kao inovacija:** LLM traži ono što pravila ne vide (nesklad predmeta i djelatnosti, generički predmeti, kombinacije slabih signala, slobodni tekst obrazloženja). Svaki razlog mora citirati polje i izvor, a odgovor bez dokaza automatski se odbacuje.

## 6. Mapiranje na kriterije ocjenjivanja (60 b)
| Kriterij (bodovi) | Kojim funkcionalnostima ga pokrivamo | Dokaz za žiri |
|---|---|---|
| Upotreba otvorenih podataka (5) | ingest preko **CKAN API-ja** (otkrivanje + download), **JPT REST API**, Sudreg API (uz ključ), OCDS bulk | stranica Izvori (F14) s metodom dohvata po izvoru; video kadar "ingest run" |
| Povezivanje podataka (5) | ugovori × JPT (OIB, osnivači) × DZS (stanovništvo) × Sudreg (osnivanje, NKD) × pravni pragovi | profil JLS (€/st.), indikator C s osnivačima iz JPT-a, AI nesklad NKD |
| Relevantnost (5) | transparentnost lokalnog javnog sektora, građanski nadzor (izazov A doslovno) | naslovnica + profil JLS |
| Utjecaj (5) | 556 JLS, vijećnici, novinari, revizori; ZPPI predložak; pravo na odgovor | demo scenarij sa stvarnim podacima; pokrivenost 61/556 kao nalaz |
| Inovativnost (5) | hibrid pravila + LLM s validatorom dokaza; LLM ekstrakcija iz neurednih registara | detalj AI procjene; evaluacija (preciznost, lažno pozitivni) |
| **Izvedivost (10)** | deployan prototip na stvarnim podacima; testovi pravila; snapshot baze | link na prototip, `./mvnw test`, README |
| Arhitektura (5) | jasna arhitektura, OCDS/DCAT-AP, pravni preduvjeti | `ARCHITECTURE.md` (dijagram, §9, §10) |
| **UI/UX (10)** | urednički dizajn, karta, detalj indikatora s tri dimenzije, WCAG | screenshotovi, video |
| **Video (10)** | scenarij u `DEMO_SCENARIOS.md` §5 | video ≤ 3 min |
