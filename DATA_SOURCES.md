# JavniRadar – popis skupova podataka i izvora

_Tim Nazor · Datathon Portala otvorenih podataka 2026 · izazov A. Stanje: **2026-11-08**._

Ovaj dokument navodi **samo izvore koje kod stvarno koristi** (provjereno pretragom URL-ova u `apps/api/src/main`, `apps/web` i konfiguraciji te upitima nad lokalnom bazom). Istraživanje svih kandidata, HTTP provjere i razlozi odbacivanja su u [`docs/DATA_SOURCES.md`](docs/DATA_SOURCES.md).

- **Licence s data.gov.hr** provjerene su 2026-11-08 preko CKAN API-ja: `https://data.gov.hr/ckan/api/3/action/package_show?id=<id>` (polja `license_id`, `license_title`).
- **Datum preuzimanja** je `retrieved_at` iz tablica `source_file` i `company_snapshot` lokalne baze.
- Što nismo mogli potvrditi označeno je **PROVJERITI**.
- Pravilnik o dozvolama za ponovnu uporabu (NN 67/17) traži navođenje izvora. Aplikacija ga navodi uz svaki ugovor (skup, izdavatelj, datoteka, redak, vrijeme dohvata) i na stranici „Izvori”.

## 1. Pregled

| # | Izvor | Izdavatelj | Poveznica | Način dohvata | Licenca | Preuzeto | Uloga |
|---|---|---|---|---|---|---|---|
| 1 | **Katalog Portala otvorenih podataka (CKAN API)** | MPUDT (data.gov.hr) | https://data.gov.hr/ckan/api/3/action/ | **API** (`package_search?q=registar ugovora&rows=1000`, `package_show`), JSON, bez ključa | metapodaci kataloga | 2026-11-02 | otkrivanje skupova i resursa, izdavatelj, licenca, `metadata_modified` (`CkanClient`) |
| 2 | **Registri ugovora JLS i javnih tijela** (40 skupova, popis u §2) | 34 JLS, 2 županije, 2 javna tijela | https://data.gov.hr/dataset/… (§2) | **datoteka** (XLSX/XLS/CSV), URL resursa iz CKAN-a | Otvorena dozvola (38), Public Domain (1), **nije navedena (1) – PROVJERITI** | 2026-11-02 | glavni izvor: 5.958 ugovora |
| 3 | **Popis javnopravnih tijela (JPT)** | MPUDT | skup: https://data.gov.hr/dataset/popis-javnopravnih-tijela · API: `https://jpt.gov.hr/Backend_Prod/api/PopisOznakaNaDanasnjiDan` | **REST API**, JSON (`JptClient`) | Otvorena dozvola (OD) (CKAN 08.11.) | 2026-11-03 | 4.678 tijela: OIB, vrsta, županija, **osnivači** (`jpt_body`) |
| 4 | **Popis stanovništva 2021 – stanovništvo po gradovima/općinama** | Državni zavod za statistiku (DZS) | skup: https://data.gov.hr/dataset/statistika-u-nizu-popis-stanovnistva-kucanstava-i-stanova-2021 · datoteka: https://podaci.dzs.hr/media/td3jvrbu/popis_2021-stanovnistvo_po_gradovima_opcinama.xlsx | **datoteka** XLSX (`DzsCensusReader`, strujno čitanje lista `1.`) | Otvorena dozvola (OD) (CKAN 08.11.); DZS traži navođenje izvora | 2026-11-03 | stanovništvo 556 JLS, € po stanovniku, usporedne skupine P1–P7 (`municipality`) |
| 5 | **Sudski registar – javni API** | Ministarstvo pravosuđa, uprave i digitalne transformacije | https://sudreg-data.gov.hr (OpenAPI: `/api/javni/dokumentacija/open_api`) | **REST API**, OAuth2 client credentials (`RestSudregClient`), 1 zahtjev/s | **PROVJERITI** uvjete korištenja iz registracije ključa | 2026-11-05 | 1.569 snimki dobavljača: datum osnivanja (1.448), NKD (1.005), stečaj/likvidacija; pravilo B i AI kontekst (`company_snapshot`). Bez ključa aplikacija radi, ova polja su „nedostupno” |
| 6 | **Granice JLS (karta)** | geoBoundaries (William & Mary geoLab), izvedeno iz OpenStreetMapa | https://www.geoboundaries.org (HRV ADM2, gbOpen, `9469f09`) | **datoteka** GeoJSON, preuzeta jednom i obrađena skriptom `apps/web/scripts/build_jls_geojson.py` → `apps/web/public/geo/jls.geojson` | **CC BY-SA 2.0** (geoBoundaries); OSM podaci **ODbL**, „© OpenStreetMap contributors” (atribucija prikazana ispod karte) | 2026-11-03 | granice 542/556 JLS |
| 7 | Pravni pragovi (ZJN 2016, NN 120/16 i NN 48/26), tečaj 7,53450 HRK/EUR (NN 85/2022) | Narodne novine | https://narodne-novine.nn.hr | ručno upisano u `apps/api/src/main/resources/config/thresholds.yaml` s datumom važenja i izvorom | službeni tekst propisa | 2026-11-01 | pragovi za pravila D, F, B; pretvorba kuna |
| 8 | CPV 2008 (nazivi odjeljaka) | Europska unija | https://eur-lex.europa.eu/eli/reg/2008/213/oj/?locale=hr | ručno upisani nazivi 2-znamenkastih odjeljaka u `CpvDivisions.java` | dokumenti EU, ponovna uporaba dopuštena (Odluka Komisije 2011/833/EU) | 2026-11-03 | nazivi kategorija u usporedbi općina |

**Sintetički podaci** (nisu javni podaci, jasno označeni): `apps/api/src/main/resources/synthetic/registar-ugovora-primjerovo.csv` (fiktivna „Općina Primjerovo”, OIB-i `000000000xx`, `is_synthetic = true`, oznaka „SINTETIČKI PODACI ZA DEMONSTRACIJU”) i AI evaluacijski skup `apps/api/src/main/resources/ai/eval/synthetic-cases-v4.json`. Ne ulaze u statistike stvarnih podataka.

**Obrada, ne izvor podataka:** Claude API (Anthropic) prima kontekst ugovora i vraća AI procjenu (`AnthropicLlmClient`, `https://api.anthropic.com`). Radi samo s `ANTHROPIC_API_KEY`.

## 2. Registri ugovora koji su dali ugovore (40 skupova)

Brojevi su ugovori koji su nakon deduplikacije uvezeni iz tog skupa (upit `contract → provenance → raw_record → source_file → source_dataset` nad lokalnom bazom, 2026-11-08; zbroj = 5.958). Klasa formata: **A** = standardni EOJN izvoz (33 stupca), **A2** = stari EOJN izvještaj (18 stupaca), vidi [`docs/DATA_SOURCES.md`](docs/DATA_SOURCES.md) §1.1a i §1.1b. Način dohvata: CKAN API → izravan download resursa (`PoliteHttpClient`, robots.txt, 1 zahtjev/s po hostu, disk cache).

| Izdavatelj | Naziv skupa | Poveznica (CKAN id) | Format | Klasa | Ugovora | Licenca (CKAN, 08.11.) | Preuzeto |
|---|---|---|---|---|---|---|---|
| Bjelovarsko-bilogorska županija | Registar ugovora | [registar-ugovora-bbz-2022](https://data.gov.hr/dataset/registar-ugovora-bbz-2022) | XLSX | A2 | 31 | Other (Public Domain) | 2026-11-02 |
| Grad Bjelovar | Registar sklopljenih ugovora za 2021. godinu | [registar-sklopljenih-ugovora-za-2021-godinu](https://data.gov.hr/dataset/registar-sklopljenih-ugovora-za-2021-godinu) | XLS | A2 | 19 | ⚠️ `license_id: null` – **PROVJERITI** | 2026-11-02 |
| Grad Čakovec | Registar Ugovora Grada Čakovca | [registar-ugovora-2022](https://data.gov.hr/dataset/registar-ugovora-2022) | XLSX | A, A2 | 534 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Ivanec | Registar sklopljenih ugovora i njihovo izvršenje | [registar-sklopljenih-ugovora-i-njihovo-izvrsenje](https://data.gov.hr/dataset/registar-sklopljenih-ugovora-i-njihovo-izvrsenje) | XLSX | A, A2 | 811 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Korčula | Registri ugovora Grada Korčule | [registri-ugovora-grada-korcule](https://data.gov.hr/dataset/registri-ugovora-grada-korcule) | XLSX | A2 | 147 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Lepoglava | Grad Lepoglava - Registri Ugovora | [grad-lepoglava-registri-ugovora](https://data.gov.hr/dataset/grad-lepoglava-registri-ugovora) | XLSX | A, A2 | 366 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Makarska | Registar Ugovora | [registar-ugovora-gm](https://data.gov.hr/dataset/registar-ugovora-gm) | XLSX | A2 | 240 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Osijek | Registar Ugovora Grada Osijeka | [registar-ugovora-grada-osijeka](https://data.gov.hr/dataset/registar-ugovora-grada-osijeka) | XLSX | A | 586 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Ozalj | Registar ugovora 30.03.2026. | [https-ozalj-hr-wp-content-uploads-2026-03-registar-ugovora-30-03-2026-xlsx](https://data.gov.hr/dataset/https-ozalj-hr-wp-content-uploads-2026-03-registar-ugovora-30-03-2026-xlsx) | XLSX | A | 263 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Rovinj-Rovigno | Registar ugovora o javnoj nabavi, okvirnih sporazuma i jednostavne nabave 2023. | [registar-ugovora-o-javnoj-nabavi-okvirnih-sporazuma-i-jednostavne-nabave-2023](https://data.gov.hr/dataset/registar-ugovora-o-javnoj-nabavi-okvirnih-sporazuma-i-jednostavne-nabave-2023) | XLSX | A2 | 643 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Rovinj-Rovigno | … na dan 30. lipnja 2020 | [registar-ugovora-o-javnoj-nabavi-okvirnih-sporazuma-i-jednostavne-nabave-na-dan-30-lipnja-2020](https://data.gov.hr/dataset/registar-ugovora-o-javnoj-nabavi-okvirnih-sporazuma-i-jednostavne-nabave-na-dan-30-lipnja-2020) | XLS | A2 | 1 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Varaždinske Toplice | Registar ugovora o javnoj nabavi i okvirnih sporazuma vrijednosti 2.650,00 eura i više od 01.01.2018. | [registar-ugovora-o-javnoj-nabavi-i-okvirnih-sporazuma-vrijednosti-2-650-00-eura-i-vise-od-01-01-2018](https://data.gov.hr/dataset/registar-ugovora-o-javnoj-nabavi-i-okvirnih-sporazuma-vrijednosti-2-650-00-eura-i-vise-od-01-01-2018) | XLSX | A2 | 169 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Zaprešić | Registar ugovora javne nabave | [registar-ugovora-javne-nabave](https://data.gov.hr/dataset/registar-ugovora-javne-nabave) | XLSX | A2 | 36 | Otvorena dozvola (OD) | 2026-11-02 |
| Grad Zlatar | Registar ugovora Grada Zlatara | [registar-ugovora-grada-zlatara](https://data.gov.hr/dataset/registar-ugovora-grada-zlatara) | XLSX | A2 | 7 | Other (Open) ⚠️ prvi upit 08.11. vratio `other-open`, ponovljeni HTTP 500 – **PROVJERITI** | 2026-11-02 |
| Hrvatska energetska regulatorna agencija | HERA Registar ugovora i okvirnih sporazuma | [hera-registar-ugovora-i-okvirnih-sporazuma](https://data.gov.hr/dataset/hera-registar-ugovora-i-okvirnih-sporazuma) | XLSX | A2 | 220 | Otvorena dozvola (OD) | 2026-11-02 |
| Koprivničko-križevačka županija | Registar ugovora Koprivničko-križevačke županije | [registar-ugovora-koprivnicko-krizevacke-zupanije](https://data.gov.hr/dataset/registar-ugovora-koprivnicko-krizevacke-zupanije) | XLSX | A | 91 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Biskupija | Javna nabava | [biskupija-javna-nabava](https://data.gov.hr/dataset/biskupija-javna-nabava) | XLSX | A2 | 16 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Bistra | Registar ugovora općine Bistra | [registar-ugovora-opcine-bistra](https://data.gov.hr/dataset/registar-ugovora-opcine-bistra) | XLSX | A2 | 398 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Blato | Registri - registar ugovora javne nabave | [https-www-blato-hr-dokumenti-postupci-javne-nabave-registar-ugovora](https://data.gov.hr/dataset/https-www-blato-hr-dokumenti-postupci-javne-nabave-registar-ugovora) | XLSX | A2 | 9 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Čađavica | Registar ugovora Općine Čađavica | [registar-ugovora-opcine-cadavica](https://data.gov.hr/dataset/registar-ugovora-opcine-cadavica) | XLSX | A | 47 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Čavle | Registri ugovora | [registri-ugovora](https://data.gov.hr/dataset/registri-ugovora) | XLSX | A2 | 133 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Cernik | Registar ugovora javne nabave Općine Cernik | [registar-ugovora-javne-nabave-opcine-cernik](https://data.gov.hr/dataset/registar-ugovora-javne-nabave-opcine-cernik) | CSV, XLSX | A2 | 115 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Civljane | Registar Ugovora Općine Civljane za 2025. godinu | [registar-ugovora-opcine-civljane-za-2025-godinu](https://data.gov.hr/dataset/registar-ugovora-opcine-civljane-za-2025-godinu) | XLSX | A | 2 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Domašinec | Registar ugovora o javnoj nabavi za 2025. godinu | [registar-ugovora-o-javnoj-nabavi-za-2025-godinu](https://data.gov.hr/dataset/registar-ugovora-o-javnoj-nabavi-za-2025-godinu) | XLS | A | 5 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Dragalić | Registar ugovora | [registar-ugovora-dragalic](https://data.gov.hr/dataset/registar-ugovora-dragalic) | XLSX | A | 14 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Funtana | Registar ugovora o javnoj nabavi i okvirnih sporazuma Općine Funtana-Fontane | [registar-ugovora-o-javnoj-nabavi-i-okvirnih-sporazuma-opcine-funtana-fontane](https://data.gov.hr/dataset/registar-ugovora-o-javnoj-nabavi-i-okvirnih-sporazuma-opcine-funtana-fontane) | XLSX | A | 26 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Gornja Stubica | Registar Ugovora Općine Gornja Stubica 2022 | [registar-ugovora-opcine-gornja-stubica-2022](https://data.gov.hr/dataset/registar-ugovora-opcine-gornja-stubica-2022) | XLSX | A2 | 36 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Hlebine | Registar ugovora o javnoj nabavi | [registar-ugovora-o-javnoj-nabavi](https://data.gov.hr/dataset/registar-ugovora-o-javnoj-nabavi) | XLSX | A | 15 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Jarmina | Registar Ugovora, Općina Jarmina | [registar-ugovora-oj-2022](https://data.gov.hr/dataset/registar-ugovora-oj-2022) | XLSX | A2 | 4 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Kloštar Ivanić | Registar ugovora – nabave do 31.12.2025. | [registar-ugovora-nabave-do-31-12-2025](https://data.gov.hr/dataset/registar-ugovora-nabave-do-31-12-2025) | XLSX | A | 114 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Kravarsko | Registar ugovora javne nabave Općine Kravarsko | [registar-ugovora-javne-nabave-opcine-kravarsko](https://data.gov.hr/dataset/registar-ugovora-javne-nabave-opcine-kravarsko) | XLSX | A | 37 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Lasinja | Registar ugovora | [registar_ugovora](https://data.gov.hr/dataset/registar_ugovora) | XLSX | A2 | 46 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Mikleuš | Registar Ugovora 2021 | [registar-ugovora-om-2021](https://data.gov.hr/dataset/registar-ugovora-om-2021) | XLSX | A2 | 19 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Mikleuš | Registar Ugovora općine Mikleuš za 2025. | [registar-ugovora-opcine-mikleus-za-2025](https://data.gov.hr/dataset/registar-ugovora-opcine-mikleus-za-2025) | XLSX | A | 130 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Posedarje | Registar Ugovora Općine Posedarje za 2023. godinu | [registar-ugovora-za-2023-godinu](https://data.gov.hr/dataset/registar-ugovora-za-2023-godinu) | XLSX | A2 | 39 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Posedarje | Registar Ugovora Općine Posedarje za 2024. godinu | [registar-ugovora-opcine-posedarje-za-2024-godinu](https://data.gov.hr/dataset/registar-ugovora-opcine-posedarje-za-2024-godinu) | XLSX | A2 | 32 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Rugvica | Registar Ugovora Općine Rugvica | [registar-ugovora-opcine-rugvica](https://data.gov.hr/dataset/registar-ugovora-opcine-rugvica) | XLSX | A2 | 188 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Stankovci | Registar Ugovora Općine Stankovci | [registar-ugovora-opcine-stankovci](https://data.gov.hr/dataset/registar-ugovora-opcine-stankovci) | XLSX | A | 82 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Štrigova | Registar Ugovora Općine Štrigova | [registar-ugovora-opcine-strigova](https://data.gov.hr/dataset/registar-ugovora-opcine-strigova) | XLSX | A | 14 | Otvorena dozvola (OD) | 2026-11-02 |
| Općina Sveti Filip i Jakov | Registar Ugovora Općine Sveti Filip i Jakov za 2025. godinu | [registar-ugovora-opcine-sveti-filip-i-jakov-za-2025-godinu](https://data.gov.hr/dataset/registar-ugovora-opcine-sveti-filip-i-jakov-za-2025-godinu) | XLSX | A | 273 | Otvorena dozvola (OD) | 2026-11-02 |

**Ostali skupovi koje je ingest pregledao, a nisu dali ugovore** (prema `GET /api/v1/sources/summary`, 2026-11-08): ukupno 85 skupova i 195 datoteka; uvezeno 55 datoteka, `format_nije_podrzan` 72 (vlastite tablice klasa B i C), `nedostupno` 60 (HTTP 403/404, SSL), `necitljivo` 6, `nestrukturirano` 2. Puni popis s razlogom daje `GET /api/v1/sources` i stranica „Izvori” u aplikaciji.

## 3. Istraženo, ali kod NE koristi

Ovi izvori su provjereni i opisani u [`docs/DATA_SOURCES.md`](docs/DATA_SOURCES.md), ali **nisu u kodu** i ne smiju se u predaji navoditi kao korišteni:

| Izvor | Zašto nije korišten |
|---|---|
| Nacionalni izvoz „Registar Ugovora – EOJN sustav” (data.gov.hr, JSON) | uzorak od točno 20.000 redaka, ne cijeli registar; ID-jevi se ne poklapaju s općinskim izvozima |
| EOJN OCDS (Open Contracting Data Registry) | nema OIB-a ni broja ponuda; 2025. i 2026. gotovo prazne |
| TED API | ugovori iznad EU pragova; planirano za kasnije |
| DGU Upravne jedinice INSPIRE | WFS isključen, GML 219 MB, nejasna licenca |
| Overpass API (OSM) | `robots.txt` zabranjuje `/api/` |
| eojn.hr | `robots.txt: Disallow: /`; ne strugamo. Aplikacija samo prikazuje poveznice `https://eojn.hr/contract-eo/{id}` iz izvornih registara |

Napomena: popis u `docs/DATA_SOURCES.md` §13 navodi i nacionalni izvoz i OCDS; za predaju vrijedi ovaj dokument.
