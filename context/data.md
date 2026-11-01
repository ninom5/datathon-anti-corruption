# Podaci

_Datasetovi, izvori, shema, veličina, licence i ograničenja. Obvezni dio dokumentacije predaje: popis svih skupova s izdavateljima i poveznicama._

## Pristup portalu (provjereno 2026-10-22)
- **CKAN API radi:** `https://data.gov.hr/ckan/api/3/action/<akcija>` (package_search, package_show, organization_list, datastore_search). JSON, bez autentikacije.
- `https://data.gov.hr/api/3/...` (bez `/ckan/`) i WebFetch vraćaju samo SPA HTML. Ne koristiti.
- Katalog: **3956 skupova**. Detalji i naredbe u `.claude/skills/data-scout/SKILL.md`.

## Snimka kataloga (2026-10-22)
- **Formati:** XLSX 1014, PDF 955, CSV 818, XLS 479, HTML 555, JSON 188, XML 175, GeoJSON 108, SHP 102, WMS 87, KML 79, WFS 56.
- **Licence:** otvorena dozvola RH 3084, CC-BY 312, ostalo malo. Korištenje uz navođenje izvora.
- **Svježina:** godišnje 654, mjesečno 144, dnevno 33, tjedno 18, kontinuirano 4. **Većina podataka nije "živa"**, pa ideje koje obećavaju real-time moraju koristiti ~55 svježih skupova ili vanjske API-je.
- **Najveći izdavači:** Grad Zagreb 210, Grad Rijeka 181, MUP 114, DZS 105, DGU 87, Grad Rovinj 74, Min. zaštite okoliša i energetike 61, Min. poljoprivrede 53.
- Postoji polje `hvd_category` (EU skupovi visoke vrijednosti) i oznaka `datastore_active` po resursu. Prvi uzorak: `datastore_active=false`, pa dostupnost kroz API treba provjeriti po resursu.

## Kandidat-skupovi
_Još nema. Popunjava se kroz `data-scout`, jedan blok po skupu (izdavatelj, resurs/format, licenca, svježina, ključ za spajanje, kvaliteta, način dohvata)._

## JavniRadar (2026-11-01)
Svi izvori, pokrivenost i popis skupova za predaju su u `docs/DATA_SOURCES.md`. Ovaj file se više ne vodi za JavniRadar.
