---
name: data-scout
description: Istražuje katalog Portala otvorenih podataka (data.gov.hr) preko CKAN API-ja jer je web stranica SPA. Pretražuje skupove, procjenjuje svježinu, licencu i format, traži ključeve za spajanje skupova i testira dostupnost podataka preko API-ja. Koristi kad treba naći skupove za ideju, provjeriti postoje li podaci za tvrdnju, ili tražiti neočekivane spojeve skupova (izvor unikatnosti).
---

# Data scout: istraživanje data.gov.hr kataloga

Cilj: pronaći **spojeve skupova podataka** koji daju novu vrijednost, i provjeriti da ih je moguće dohvatiti izravno preko API-ja. To izravno hrani kriterije "Upotreba otvorenih podataka" (1–5) i "Povezivanje podataka" (1–5), a pomaže i kod izjednačenja.

## Pristup: CKAN API (provjereno 2026-10-22)

- **Koristi** `https://data.gov.hr/ckan/api/3/action/<akcija>`. Vraća JSON. Katalog ima oko 3950 skupova.
- **Ne koristi** `https://data.gov.hr/api/3/...` (bez `/ckan/`) ni WebFetch za stranice portala. Dobije se prazan SPA HTML.
- Samo GET upiti, bez autentikacije. Ne šalji ništa na portal.
- Windows/bash: `curl -s -m 30 -G "$B/package_search" --data-urlencode "q=..."`. Obradu JSON-a radi s `python -I -c "..."` iz stdin-a. Naslovi i tagovi mogu imati kvarne dijakritike u terminalu, što je prikazni problem, ne podatkovni.

```bash
B="https://data.gov.hr/ckan/api/3/action"

# pretraga (q = tekst, fq = filtar, rows/start = paginacija)
curl -s -m 30 -G "$B/package_search" --data-urlencode "q=zrak" --data-urlencode "rows=20"
curl -s -m 30 -G "$B/package_search" --data-urlencode 'fq=res_format:"CSV" AND organization:"grad-zagreb"' --data-urlencode "rows=0"

# facete: organizacije, formati, licence, tagovi, frekvencija osvježavanja
curl -s -m 30 -G "$B/package_search" --data-urlencode "rows=0" \
  --data-urlencode 'facet.field=["organization","res_format","license_id","tags","data_refresh_frequency"]' \
  --data-urlencode "facet.limit=50"

# detalji jednog skupa (resursi, URL-ovi, licenca, svježina)
curl -s -m 30 -G "$B/package_show" --data-urlencode "id=<name-ili-uuid>"

# popis organizacija
curl -s -m 30 "$B/organization_list?all_fields=true"

# DataStore (strojno pretraživ resurs, samo ako je datastore_active=true)
curl -s -m 30 -G "$B/datastore_search" --data-urlencode "resource_id=<uuid>" --data-urlencode "limit=5"
```

## Što je poznato o katalogu (snimka 2026-10-22)

- **Formati (broj skupova):** XLSX 1014, PDF 955, CSV 818, XLS 479, HTML 555, JSON 188, XML 175, GeoJSON 108, SHP 102, WMS 87, KML 79, WFS 56. Tablični CSV/JSON ima dosta, geo podaci (GeoJSON/WMS/WFS/SHP) isto.
- **Licence:** otvorena dozvola RH (`open-license`) 3084, CC-BY 312. Uz navođenje izvora korištenje je dopušteno, a Pravila to i traže.
- **Svježina je ključno ograničenje:** godišnje 654, mjesečno 144, **dnevno 33, tjedno 18, kontinuirano 4**. Većina podataka nije "živa". Ideja koja obećava real-time ili proaktivno obavještavanje mora se osloniti na tih ~55 skupova ili na vanjski API.
- **Veliki izdavači:** Grad Zagreb 210, Grad Rijeka 181, MUP 114, DZS 105, DGU 87, Grad Rovinj 74, Ministarstvo zaštite okoliša i energetike 61, Ministarstvo poljoprivrede 53.
- **Polje `hvd_category`** označava EU skupove visoke vrijednosti (high-value datasets). To su najkvalitetniji i najstabilniji kandidati.
- Resursi imaju `datastore_active` i `available_through_api`. Prvi uzorak imao je `datastore_active=false`, pa **ne pretpostavljaj** da je CSV dostupan kroz `datastore_search`. Provjeri po resursu. Ako nije, izravan HTTP dohvat datoteke (URL resursa) i redovito osvježavanje u vlastitoj pipeline također se računa kao dohvat izvora, ali je slabije od pravog API-ja. Zapiši što je od toga.

## Postupak istraživanja

Radi iz **dva smjera** i spoji ih:

1. **Od izazova prema podacima.** Uzmi nabrojane teme izazova (iz `context/competition.md`), pretraži katalog hrvatskim ključnim riječima i sinonimima (npr. otpad/odlagalište/komunalno, zrak/PM10/mjerna postaja, poplave/hidrologija/vodotoci, buka, javni prijevoz/GTFS, škole/vrtići, zdravstvo, proračun/javna nabava). Navedi broj pogodaka i najbolje kandidate.
2. **Od podataka prema ideji.** Pregledaj facete: koji izdavači imaju najčišće, najsvježije i najbolje strukturirane skupove? Koji skupovi imaju koordinate, šifre općina/županija, datume ili kategorije koji se mogu spojiti s drugim skupovima?
3. **Traži ključ za spajanje.** Spoj skupova ima smisla samo ako postoji zajednički ključ: šifra općine/naselja (JLS), koordinate (prostorni spoj), datum/vrijeme, OIB ili MB pravne osobe, šifra djelatnosti (NKD), adresa. Za svaki predloženi spoj imenuj ključ i procijeni pokrivenost (koliko redaka stvarno spaja).
4. **Provjeri stvarni sadržaj**, ne samo naslov. Povuci uzorak (prvih 5 redaka), provjeri stupce, kodiranje (UTF-8/Windows-1250), prazne vrijednosti, godine pokrivenosti, kad je zadnje ažuriranje (`metadata_modified`, `last_modified`).
5. **Dodaj izvore izvan portala** kad ih ima (Pravila dopuštaju): DZS, Eurostat, DHMZ, HAK, Copernicus, OpenStreetMap, javni API-ji tijela. Provjeri licencu. To podiže kriterij Povezivanje.

## Izlaz za svaki kandidat-skup

Zapiši u `context/data.md` (jedan blok po skupu, kratko):

- naziv + izdavatelj + poveznica na skup
- resurs koji koristimo: format, URL, `datastore_active`, veličina
- licenca (mora dopuštati korištenje)
- svježina (`data_refresh_frequency`, zadnja izmjena) i godine pokrivenosti
- ključ(evi) za spajanje i procjena pokrivenosti
- kvaliteta: kodiranje, prazna polja, anomalije, što je čudno
- način dohvata: pravi API / izravan download / ručno (to utječe na bodove)

Ovaj popis je ujedno **obvezni dio dokumentacije** predaje ("popis korištenih skupova podataka s izdavateljima i poveznicama"), pa ga vodi točno od početka.

## Zamke

- **Naslov obećava, sadržaj ne.** Mnogo "skupova" su PDF ili scan tablice. Provjeri format i uzorak prije nego ideju gradiš na njemu.
- **Zastarjeli skup.** Provjeri `metadata_modified` i najnoviju godinu u podacima. Skup zadnji put ažuriran 2019. ne podržava "živu" uslugu.
- **Prazan spoj.** Ključevi se često ne podudaraju (različiti nazivi naselja, nazivi s dijakriticima). Procijeni stvarno podudaranje na uzorku prije nego se obeća.
- **Osobni podaci.** Skupovi s imenima, OIB-ovima ili adresama imaju GDPR implikacije. Kriterij 7 traži navedene pravne preduvjete.
- **Nepouzdan API izdavatelja.** Dodaj u procjenu rizika: što ako API padne tijekom demo snimanja? Plan: lokalna keširana kopija kao rezerva, uz izravni poziv kao primarni put.
- Ne izmišljaj skupove. Sve što tvrdiš mora doći iz stvarnog API odgovora. Što nisi provjerio, označi **nepotvrđeno**.

## Veza s ostalim skillovima

- Kandidate-ideje koje proizađu iz skouta provuci kroz `jury-simulator`.
- Za faze otvorenog razmišljanja koristi `product-brainstorming`. Ovaj skill daje činjenice, a ne ideje.
