# JavniRadar – UI/UX

## 1. Smjer
- **Osjećaj:** urednički podatkovni članak (kao datanovinarski projekti velikih redakcija) + preciznost alata (Linear). **Nije** generički SaaS dashboard s gradijentima i karticama KPI-jeva.
- **Ton:** smiren i dokumentaran. Bez crvenih alarma. Crvena se ne koristi za "sumnjivo", nego jantarna i neutralna (`--signal: #B45309`), jer boja ne smije sugerirati krivnju.
- **Dizajn sustav:** isti kao frontend OtvoreniRadara (`FESB/datathon`, grana `refactor-ui`, 03.11.). Tokeni, komponente i pravila su u `DESIGN_SYSTEM.md` (korijen repoa), vrijednosti u `apps/web/src/styles/global.css`.
- **Tipografija:** Source Serif 4 (600) za `h1`/`h2`, IBM Plex Sans za sve ostalo, tabularne brojke (`.tabular-nums`). Obje imaju hrvatske dijakritike.
- **Boje (tokeni, izbor):**

| Token | Vrijednost |
|---|---|
| `--color-text` | `#1d2420` |
| `--color-background` | `#f7f7f3` |
| `--color-text-muted` | `#59625d` |
| `--color-border` | `#dfe4dc` |
| `--color-primary` | `#244a3d` (poveznice, gumbi, odabir) |
| `--color-signal` / `-soft` | `#85570a` / `#f8efda` (indikator, jantarna) |
| `--map-no-data` | uzorak s crtama (sivo) |
| `--color-synthetic` | `#5b3f8f` (traka za sintetičke podatke) |

  Tamni način je uklonjen (OtvoreniRadar ga nema; video se snima u svijetlom).
- **Gustoća:** tablice s 32 px redovima, brojevi poravnati desno, iznosi u formatu `1.234.567 €` (hr-HR).

## 2. Informacijska arhitektura
```
/                       Naslovnica (pretraga + karta + istaknuti nalazi)
/istrazi                Istraživač ugovora (F1)
/opcina/:oib            Profil JLS (F2) + /usporedba (F4)
/dobavljac/:oib         Profil dobavljača (F3)
/ugovor/:id             Detalj ugovora (F8, F9, F6)
/indikator/:id          Detalj indikatora (F5)
/izvori                 Izvori i metodologija (F14)
/ispravak               Obrazac (F12)
```

## 3. Ekrani

### 3.1 Naslovnica
```
┌──────────────────────────────────────────────────────────────────────┐
│ JavniRadar                          Istraži  Karta  Izvori  [Pretraga]│
├──────────────────────────────────────────────────────────────────────┤
│  Kamo odlazi novac vaše općine?                                       │
│  [ Upišite općinu, tvrtku ili predmet nabave …                  🔍 ]  │
│  npr. "Općina Mikleuš"  ·  "asfaltiranje 2025"  ·  OIB dobavljača    │
├───────────────────────────────┬──────────────────────────────────────┤
│  KARTA (županija/HR)          │  Što se vidi u podacima              │
│  ░ nema podataka  ▓ 0–2  █ 3+ │  ① 2.425 ugovora iz 30 JLS           │
│  [interaktivna karta JLS]     │  ② 41 ugovor s rastom vrijednosti    │
│                               │     >10 %  → pogledaj                │
│                               │  ③ 61 od 556 JLS objavljuje          │
│                               │     strojno čitljiv registar         │
├───────────────────────────────┴──────────────────────────────────────┤
│ Indikatori nisu optužbe. Pokazuju što vrijedi provjeriti. Metodologija→│
└──────────────────────────────────────────────────────────────────────┘
```
(Brojke na skici su iz provjere 2026-11-01 ili su ilustrativne. U aplikaciji dolaze iz API-ja.)

### 3.2 Istraživač ugovora
- Lijevo je kolona filtara (sklopiva na mobitelu), desno tablica.
- Gornja traka prikazuje sažetak filtra: "312 ugovora · 4,1 M € · 18 s indikatorom".
- Stupac "Oznake" sadrži male značke: `D +75 %`, `F 3 ugovora`, `AI`.

### 3.3 Profil JLS
```
Općina Mikleuš · Virovitičko-podravska · 1.234 st.* · skupina P1 (općine < 2.000)
Podaci: registar ugovora 2024–2025 (data.gov.hr, dohvaćeno 03.11.2026.) ⓘ
┌ Potrošnja po kategoriji (€ po st.) ─────┐ ┌ Najveći dobavljači ───────┐
│ 45 Građevinski radovi ████████  210 €   │ │ … d.o.o.   38 %  ⓘ        │
│ 71 Arhitektonske usl.  ██        41 €   │ │ … d.d.     12 %           │
│    medijan skupine     ┆                │ └───────────────────────────┘
└─────────────────────────────────────────┘
┌ Indikatori (3) ──────────────────────────────────────────────────────┐
│ D  Rast vrijednosti ugovora  +62 %   kvaliteta: visoka · dokaz: srednji│
│ F  Uzastopni ugovori ispod praga …                                    │
└──────────────────────────────────────────────────────────────────────┘
* broj na skici je ilustrativan
```

### 3.4 Detalj indikatora (najvažniji ekran za povjerenje)
```
D · Povećanje vrijednosti ugovora                          rules-v1 · 05.11.2026.
─────────────────────────────────────────────────────────────────────────
Ugovor "Rekonstrukcija …" · Općina … → … d.o.o.
 200.000 € ──(+150.000 € izmjena)──► 350.000 €   (+75 %)

┌ Kvaliteta podataka ┐ ┌ Veličina odstupanja ┐ ┌ Pouzdanost dokaza ┐
│ VISOKA             │ │ +75 %               │ │ SREDNJA           │
│ strukturirani      │ │ iznad 30 % (čl.316) │ │ razlog izmjene    │
│ EOJN izvoz         │ │                     │ │ nije objavljen    │
└────────────────────┘ └─────────────────────┘ └───────────────────┘
Što je izračunato  ▸ formula, ulazna polja, prag
Dokazi             ▸ Registar ugovora Općine …, redak 41 ↗ · eojn.hr/contract-eo/… ↗
Što ovo NE znači   ▸ Povećanje može biti predviđeno ugovorom (čl. 315.) …
Što nedostaje      ▸ datum i razlog izmjene
Kako provjeriti    ▸ [Generiraj ZPPI zahtjev za dokumentaciju izmjene]
Odgovor naručitelja▸ (nema) · [Prijavi grešku / dodaj kontekst]
─────────────────────────────────────────────────────────────────────────
AI procjena – zahtijeva ljudsku provjeru                 claude-haiku-5-5
 ◉ vrijedi provjeriti · pouzdanost: srednja
 • … (svaki razlog s poveznicama na polja)
 Moguća legitimna objašnjenja: …          [Korisno] [Pogrešno]
```
Tri dimenzije su **tri jednako velika okvira bez ukupnog zbroja**. Nema "ocjene 7/10".

### 3.5 Usporedba općina
Dot plot: svaka točka je JLS u skupini, os x prikazuje € po stanovniku za odabrani CPV, a odabrana općina je istaknuta. Ispod je tablica sa svim JLS u skupini i n. Prekidač godine.

### 3.6 Dobavljač
Zaglavlje s OIB-om, pravnim oblikom i Sudreg podacima (ili "nije dostupno"). Mreža "naručitelj ↔ dobavljač" je Future. U MVP-u je tablica naručitelja s udjelima.

### 3.7 Izvori i metodologija
Tablica izvora (izdavatelj, licenca, metoda dohvata, zadnji dohvat, broj zapisa, status), pokrivenost (x/556 JLS), pravila s formulama, evaluacija AI-ja, politika jezika, kontakt za ispravke.

## 4. Sintetički podaci u UI-ju
Ljubičasta traka preko cijele širine: **"SINTETIČKI PODACI ZA DEMONSTRACIJU – izmišljene organizacije"**. Takvi zapisi nisu u pretrazi ni u statistikama stvarnih podataka. U istraživaču ugovora prikazuju se samo kad korisnik uključi filter **"Prikaži sintetičke"** (zadano isključen); tada svaki sintetički redak ima oznaku, a detalj ugovora traku. (Promjena 2026-11-02 umjesto zasebne rute `/demo`, vidi `work/decisions.md`.)

## 4a. Stanje implementacije (POC, 2026-11-02)
- **Rute:** `/` (naslovnica s tri brojke), `/istrazi` (istraživač), `/ugovor/:id` (detalj), `/izvori` (izvori i metodologija). `/karta` i `/usporedba` dodani su u #43. `/dobavljac` i `/indikator` još nisu napravljeni.
- **Istraživač:** filtri su u URL-u na hrvatskom (`narucitelj`, `godina`, `dobavljac`, `oznake`, `sinteticki`, `poredak`, `stranica`). Oznake: svi, s bilo kojom oznakom, vrijedi provjeriti, **moguća greška u unosu**, bez oznake.
- **Sažetak:** "N ugovora · X € · N vrijedi provjeriti · N mogućih grešaka u unosu". Samo stvarni podaci i bez obrta, uz napomenu koliko je ugovora s obrtima izostavljeno.
- **Detalj indikatora:** blok **"Objašnjenje naručitelja iz izvora"** stoji prije postotka (D2). Moguća greška u unosu nema velik postotak u naslovu, samo sivu iscrtkanu karticu "Informativno – vjerojatna greška u izvoru".
- **Obrti:** prikazuju se kao "Obrt (naziv skriven) · OIB …", uz napomenu da se AI procjena za njih ne radi.
- **Karta (`/karta`, #43):**
  - SVG iz `public/geo/jls.geojson`, bez biblioteke: vlastita ekvirektangularna projekcija u `utils/geo.ts`;
  - boja je broj indikatora "vrijedi provjeriti" na 100 ugovora, u fiksnim razredima 0 / do 2 / 2–5 / 5–10 / > 10, na jednoj jantarnoj ljestvici;
  - JLS bez registra su šrafirane, kao zasebna kategorija;
  - klik na JLS ili brzi odabir otvara bočnu ploču (upozorenje ispod 20 ugovora), a odabir je u URL-u (`?jls=`);
  - tablica s pretragom je alternativa za tipkovnicu (putanje nisu fokusabilne, jer bi 556 tab-stopova bilo zamka).
- **Usporedba (`/usporedba?jls=&godina=&cpv=`, #43):**
  - zadana je zadnja potpuna godina;
  - prikazuju se dot plot s medijanom i istaknutom JLS, tablica, "Što ovo NE znači" i trajna napomena "Usporedba kategorija, ne jediničnih cijena";
  - ispod n = 8 prikazuje se "Nedovoljno podataka za pouzdanu usporedbu", ali brojke ostaju vidljive.
- **Fontovi:** sistemski (Georgia/Segoe UI) kao zamjena za Source Serif 4/Inter. Vanjski fontovi se ne učitavaju, da nema poziva prema trećim stranama. Fontove treba dodati lokalno ako tim želi točan izgled.

## 5. Pristupačnost (WCAG 2.1 AA)
- Kontrast ≥ 4,5:1 za tekst. Boja nikad nije jedini nositelj informacije (značka ima i slovo pravila).
- Svaki graf ima tablicu podataka ("Prikaži kao tablicu") i `aria-label` sa sažetkom.
- Tipkovnica: fokus vidljiv, karta ima alternativni popis JLS.
- `lang="hr"`, hrvatski formati datuma (`dd. MM. yyyy.`) i brojeva.
- Mobitel: jedan stupac, filtri u ladici, tablica postaje kartice ispod 640 px.

## 6. Biblioteke [PRIJEDLOG]
- Grafovi: **Observable Plot** (malen, urednički izgled) ili Recharts.
- Karta: **MapLibre GL** ili čisti SVG iz GeoJSON-a (`d3-geo`). Za ~556 poligona SVG je dovoljan i lakši.
- Stil: CSS moduli + tokeni (bez teškog UI kita), uz shadcn-like primitive po potrebi.
- Prije pisanja koda učitati skill `react-web` (kopirati iz `Desktop/datathon/.claude/skills/`) i `dataviz`.
