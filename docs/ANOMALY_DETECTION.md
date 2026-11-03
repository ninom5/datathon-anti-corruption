# JavniRadar – detekcija neuobičajenih obrazaca

_Verzija pravila: `rules-v1` · 2026-11-01. Izvori i polja: `DATA_SOURCES.md`. Tablice: `DATA_MODEL.md`._

## 0. Načela (obvezna)

1. **Indikator nije optužba.**
   - Dopušteno: "neuobičajeno", "vrijedi provjeriti", "indikator rizika", "sumnjivo" (samo uz AI procjenu i uz dokaze).
   - **Zabranjeno** u UI-ju, API-ju i promptovima: "korupcija", "kriminal", "nezakonito", "prijevara", "pronevjera", "namješteno", te imenovanje fizičkih osoba kao odgovornih.
   - Popis zabranjenih riječi je u kodu (`wording/forbidden_terms_hr.txt`), a test pada ako se neka pojavi u predlošcima.
2. **Tri odvojene dimenzije** za svaki indikator. **Nikad** se ne zbrajaju u jedan "rizik korupcije":
   - **kvaliteta podataka** (`data_quality`: niska / srednja / visoka): jesu li ulazna polja potpuna, parser ili LLM, pouzdanost;
   - **veličina odstupanja** (`magnitude`): broj s jedinicom (omjer, p. p., HHI, EUR);
   - **pouzdanost dokaza** (`evidence_strength`: slaba / srednja / jaka): koliko izravno podaci podupiru zaključak (npr. CPV razina je slaba, a stavka s jediničnom cijenom jaka).
3. **Svaki indikator ima dokaz:** poveznicu na izvorni zapis (`source_record`: URL datoteke + redak, ili `eojn.hr/contract-eo/{id}`).
4. **Deterministička pravila rade bez LLM-a.** AI procjena je zaseban sloj i prikazuje se **uz** pravila, nikad umjesto njih.
5. **Pragovi su konfiguracija s datumom važenja** (`config/thresholds.yaml`), nikad u kodu.

## 1. Izvedivost scenarija s dostupnim podacima

| Scen. | Potrebni podaci | Dostupno? | Izvor | Odluka |
|---|---|---|---|---|
| **A** Razlika u cijeni | količina, jedinična cijena, specifikacija | **Ne**, registri imaju samo predmet i ukupni iznos | – | **Nije izvedivo na razini stavke.** Fallback: CPV kategorija × po stanovniku (slab dokaz). **Should** |
| **B** Nova tvrtka | OIB dobavljača, datum ugovora, **datum osnivanja** | OIB i datum da; datum osnivanja **samo uz Sudreg ključ** | Klasa A + Sudreg | **Should**, uvjetno o ključu; bez ključa označeni mock samo u sintetičkom demu |
| **C** Koncentracija | naručitelj, dobavljač, CPV, iznos, razdoblje | **Da** | Klasa A, nacionalni uzorak | **Must** |
| **D** Izmjene i preplate | početni iznos, iznos izmjene, ukupni, isplaćeni, obrazloženje | **Da**, djelomično (izmjene 3,7 %, isplaćeno 27 % u klasi A) | Klasa A, nacionalni uzorak | **Must** |
| **E** Niska konkurencija | broj ponuda | **Ne**, nema ni u jednom izvoru (EOJN izvoz, OCDS `numberOfTenderers` = 0) | – | **Nije izvedivo s trenutnim podacima.** Proxy: udio postupaka bez javne objave (slab dokaz). **Could** |
| **F** Cjepkanje | naručitelj, dobavljač, CPV, datum, iznos, prag po datumu | **Da** | Klasa A, nacionalni uzorak | **Must** |
| **G** Vremenski obrasci | datum, iznos, više godina, datumi izbora | Da, ali **malo godina** (klasa A pretežno 2024–2025) | Klasa A + OCDS (2018–2024, bez OIB-a) | **Should** |

**Izmjereno na nacionalnom uzorku od 20.000 ugovora (2026-11-01).** Ovo služi za kalibraciju, ne za tvrdnje:
- **D:** `TotalValue > InitialValue` u 1.110 ugovora (>10 %: 894; >30 %: 475; >50 %: 204). Isplaćeno >110 % vrijednosti s PDV-om: 588, od toga 86 ima tekst obrazloženja.
- **F:** 282 grupe (naručitelj × dobavljač × CPV3) s ≥2 ugovora ispod 26.540 EUR u 90 dana, čiji zbroj prelazi prag (od 10.500 grupa).
- **C:** 4.690/5.520 ćelija (naručitelj × CPV2) ima HHI ≥ 5.000. Razlog je što su ćelije sitne (1–2 ugovora). **Bez minimalnog uzorka pravilo C je beskorisno.** Zato postoji `min_contracts`.
- **G:** prosinac 2025. čini 6,7 % ugovora iz 2025. u uzorku, dakle nema nacionalnog skoka (siječanj 13 %, ožujak 18 %).

**Preporuka za MVP: D, F, C** (vidi `MVP_ROADMAP.md`). B se uključuje ako Sudreg ključ stigne do 4.11.

---

## 2. Zajednička struktura indikatora

```json
{
  "indicator_id": "ind_8f3a…",
  "rule_code": "D_VALUE_INCREASE",
  "rule_version": "rules-v1",
  "subject": {"type": "contract", "id": "c_123"},
  "magnitude": {"value": 1.75, "unit": "omjer", "label": "+75 % u odnosu na početni iznos"},
  "data_quality": "visoka",
  "evidence_strength": "srednja",
  "status": "aktivan",
  "explanation_hr": "Ukupni iznos ugovora porastao je s 200.000 € na 350.000 € (+75 %)…",
  "what_it_does_not_mean_hr": "Povećanje može biti zakonito (npr. ugovorene opcije, dodatni radovi)…",
  "evidence": [{"label": "Registar ugovora Grada X, redak 41", "url": "https://…", "field": "Ukupni iznos (sa izmjenama) bez PDV-a"}],
  "missing_data": ["razlog izmjene", "datum izmjene"],
  "computed_at": "2026-11-05T10:00:00Z"
}
```

**Kvaliteta podataka** (`data_quality`), zajednička funkcija:
```
dq(record):
  if record.extraction_method == "llm" and record.confidence < 0.8: return "niska"
  if any required field missing or imputed:                         return "srednja"
  if record.extraction_method == "parser" and record.source_class == "A": return "visoka"
  return "srednja"
```

---

## 3. Scenariji

### Scenarij A – Razlika u cijeni između općina

**Ulaz:**

| Polje | Dostupno |
|---|---|
| predmet, CPV, ukupni iznos, naručitelj | da |
| količina, jedinična cijena, specifikacija (zaslon, softver, instalacija, jamstvo, PDV) | **ne** (rijetko u tekstu predmeta; LLM ekstrakcija iz predmeta je moguća, ali rijetko uspijeva) |
| stanovništvo JLS | da (DZS) |

**Logika, dvije razine:**

1. **Razina stavke (jaka)**, samo ako su i kod A i kod B pouzdano izvađeni `quantity` i `unit_price` (confidence ≥ 0,9) i ako je ista podvrsta (npr. `laptop`):
   ```
   unit_A = value_A / qty_A ; unit_B = value_B / qty_B
   ratio = max(unit_A, unit_B) / min(unit_A, unit_B)
   diff  = |unit_A - unit_B| * qty_B
   flag if ratio >= 2.0 and diff >= 5_000 EUR
   comparability checklist: [vrsta uređaja, PDV uključen?, monitor/softver/instalacija/održavanje, jamstvo, datum ±12 mj]
   if any checklist item == "nepoznato": evidence_strength = "slaba", and show "Nedovoljno podataka za pouzdanu usporedbu"
   ```
2. **Fallback po kategoriji (slaba):** potrošnja po stanovniku u CPV skupini (npr. `30` = uredska i računalna oprema), u odnosu na usporednu skupinu:
   ```
   spend_pc(j, cpv2, year) = Σ value_eur / population(j)
   z = (spend_pc - median_peer) / MAD_peer          # robusni z
   flag if z >= 3 and spend_pc >= 2 * median_peer and n_peer >= 8
   evidence_strength = "slaba" (uvijek)
   ```

**Usporedne skupine** (iz DZS-a + JPT-a):

| Skupina | Uvjet |
|---|---|
| P1 | općina < 2.000 st. |
| P2 | općina 2.000–4.999 |
| P3 | općina ≥ 5.000 |
| P4 | grad < 10.000 |
| P5 | grad 10.000–49.999 |
| P6 | grad ≥ 50.000 (bez Zagreba) |
| P7 | Grad Zagreb (bez usporedbe) |

**Pragovi:**

| Prag | Zadano | Obrazloženje |
|---|---|---|
| `ratio_min` | 2,0 | konzervativno, jer specifikacije variraju |
| `diff_min` | 5.000 € | ispod toga nije vrijedno pažnje |
| `z_min` | 3 | |
| `n_peer_min` | 8 | |

**Kad podaci nedostaju:** bez količine nema razine 1, prikazuje se samo kategorija. Ako skupina ima manje od 8 članova s podacima, poruka je "Nedovoljno podataka za pouzdanu usporedbu".

**Lažno pozitivni:**
- jednokratne investicije (npr. opremanje škole u jednoj godini);
- CPV pogrešno odabran;
- ugovor koji pokriva više godina.

**Tekst (primjer):** "Općina Primjerovo je u 2025. potrošila 41 € po stanovniku na računalnu opremu (CPV 30). Usporedive općine (2.000–4.999 st.) troše medijan od 9 € po stanovniku. Ovo je usporedba kategorija, a ne cijena pojedinih uređaja."

**Što ovo NE znači:** "Ne znači da je oprema preplaćena. Moguće je da je općina jednokratno opremila školu ili kupila skuplju opremu drugačije namjene."

**Vizualizacija:** dot plot općina u skupini (x = € po stanovniku), uz istaknutu promatranu općinu i medijan.

### Scenarij B – Nedavno osnovana tvrtka dobiva veći ugovor

**Ulaz:**

| Polje | Dostupno |
|---|---|
| OIB dobavljača, datum ugovora, vrijednost | da |
| `datum_osnivanja`, `glavna_djelatnost`, pravni oblik | **samo uz Sudreg OAuth** (`/detalji_subjekta` ili bulk `/subjekti`) |
| prethodnik (statusne promjene) | Sudreg `/statusni_postupci` [NEVERIFICIRANO sadržaj] |
| broj ponuda | ne |

**Logika:**
```
age_days = contract.date_signed - company.founded_on
flag if age_days <= 180 and value_eur >= 50_000
level "visa_pozornost_pravila" if age_days <= 60 and value_eur >= 200_000
skip if supplier.type in {obrt, fizicka_osoba, strana}      # nema u Sudregu, a za fizičke osobe vrijedi GDPR
skip if company has predecessor (statusni postupak: pripajanje/podjela) -> show as context
```

| Prag | Zadano | Obrazloženje |
|---|---|---|
| `max_age_days` | 180 | uobičajeni prag u literaturi o red flagovima [PRETPOSTAVKA] |
| `min_value_eur` | 50.000 | prag jednostavne nabave za robe i usluge od 16.05.2026. |

**Ponašanje bez Sudreg ključa:**
- pravilo se **ne izvršava nad stvarnim podacima**;
- UI prikazuje "Provjera datuma osnivanja nije dostupna (Sudski registar zahtijeva registraciju)";
- u sintetičkom demu se koristi `FixtureSudregClient` s OIB-om `00000000001` i oznakom **SINTETIČKI PODACI**.

**Pouzdanost:**
- `data_quality` = visoka, ako je Sudreg odgovorio;
- `evidence_strength` = srednja, jer mlada tvrtka može imati iskusne osnivače ili prethodnika.

**Lažno pozitivni:**
- SPV tvrtke osnovane za projekt;
- reorganizacije i promjene pravnog oblika (obrt → d.o.o.);
- podružnice stranih tvrtki.

**Tekst:** "Primjer d.o.o. upisan je u sudski registar 01.03.2026., a ugovor vrijedan 800.000 € sklopljen je 01.04.2026. (31 dan kasnije). Vrijedi provjeriti kako je ponuditelj dokazao sposobnost (reference, osoblje)."

**Što NE znači:** "Nova tvrtka može biti nastavak postojećeg poslovanja ili imati iskusne osnivače i stručnjake. Datum osnivanja sam po sebi ne govori ništa o kvaliteti ponude."

**Vizualizacija:** vremenska crta: osnivanje → objava → ugovor.

### Scenarij C – Ponovljene dodjele istom dobavljaču (koncentracija)

**Ulaz:** naručitelj OIB, dobavljač OIB, CPV (2 znamenke), `value_eur`, datum. Sve je dostupno u klasi A.

**Formule** za ćeliju (naručitelj *j*, CPV skupina *k*, razdoblje *T* = zadnje 3 godine ili sve dostupne):
```
s_i  = V_i / Σ V                  # udio dobavljača i u vrijednosti
HHI  = Σ (100 · s_i)^2            # 0–10.000
top1 = max s_i
n    = broj ugovora u ćeliji
```

**Pravilo:**
```
eligible if n >= 5 and Σ V >= 50_000 EUR and distinct_years >= 2
flag if top1 >= 0.70 and HHI >= 5_000
context:
  supplier in JPT and Osnivaci contains j.OIB  -> "dobavljač je u vlasništvu naručitelja" (legitimno, indikator se prikazuje kao "informativno")
  CPV in {65 (komunalne usluge), 64 (pošta/telekom), 09 (energija)}       -> napomena "mogući prirodni monopol"
```

| Prag | Zadano | Zašto |
|---|---|---|
| `min_contracts` | 5 | bez toga 85 % ćelija daje HHI ≥ 5.000 (izmjereno) |
| `min_total_eur` | 50.000 | |
| `top1_share` | 0,70 | |
| `hhi` | 5.000 | iznad 2.500 smatra se visoko koncentriranim (praksa tržišnog natjecanja) [PRETPOSTAVKA] |

**Pouzdanost:** `evidence_strength` = srednja; ako je n ≥ 15, jaka.

**Lažno pozitivni:**
- jedini lokalni dobavljač (otoci, npr. Hvar);
- okvirni sporazum s jednim dobavljačem;
- komunalno poduzeće u vlasništvu općine.

**Tekst:** "U zadnje 3 godine Općina Primjerovo sklopila je 9 ugovora za građevinske radove (CPV 45) vrijedne ukupno 412.000 €. Primjer gradnja d.o.o. dobio je 7 od 9 ugovora (86 % vrijednosti; HHI 7.500)."

**Što NE znači:** "Na malom tržištu često postoji samo jedan ili dva izvođača. Koncentracija ne govori je li postupak bio pošten."

**Vizualizacija:** složeni stupac po godinama (udio dobavljača) i Lorenzova linija; tablica ugovora s poveznicama.

**Implementacija (`rules-v1`, 2026-11-03, #41):** `rules/ConcentrationRule` (sučelje `GroupRule`, nad svim ugovorima jednog naručitelja). Pragovi su u `thresholds.yaml` (`rule-c`).
- Razdoblje *T* su **3 zadnje kalendarske godine u kojima naručitelj ima ugovore**. Vrijednost je `coalesce(ukupni, početni)` bez PDV-a (A2 nema ukupnog iznosa).
- Ćelija ulazi u izračun ako ima n ≥ 5, ΣV ≥ 50.000 € i ≥ 2 različite godine. Indikator nastaje ako je top1 ≥ 0,70, HHI ≥ 5.000 i **najveći dobavljač ima ≥ 2 ugovora u ćeliji** (jedan veliki ugovor nije "ponovljena dodjela"; ručni pregled 2026-11-03: oko 7 od 22).
- **Kvaliteta:** `visoka` samo ako su svi ugovori ćelije klase A bez upozorenja iz ingesta; inače `srednja`.
- **Pouzdanost:** `srednja`, a `jaka` ako je n ≥ 15.
- **`informativno`:** ako je prema JPT-u naručitelj osnivač najvećeg dobavljača (`organization.founders`). CPV 09/64/65 samo dodaju napomenu "mogući prirodni monopol", kao u dokumentu.
- Indikator je `subject_type = buyer_cpv`. `subject_ref` sadrži `buyerOib`, `cpv2`, `years`, `topSupplierOib` i `contractIds`, a `evidence` navodi redak izvora za svaki ugovor ćelije.
- Naziv obrta se ne navodi ("Obrt (naziv skriven)").

### Scenarij D – Povećanje vrijednosti i isplate iznad ugovorenog

**Ulaz:**

| Polje (klasa A) | Nacionalni uzorak | Dostupno |
|---|---|---|
| Iznos bez PDV-a | `InitialValue` | da |
| Iznos izmjene bez PDV-a | – | 3,7 % redaka |
| Ukupni iznos (sa izmjenama) bez PDV-a | `TotalValue` | da |
| Isplaćeni iznos | `PayedAmount` | 27 % / 38 % |
| Obrazloženje iznosa plaćanja | `ContractTerminationInfo` | rijetko |
| Na osnovu okvirnog sporazuma | `FrameworkAgreementId` | da |
| Vrsta predmeta (robe/usluge/radovi) | `ContractTypeId` | da / šifra |

**Logika:**
```
growth  = total_value / initial_value - 1
overpay = paid_amount / total_value_with_vat - 1      # isplaćeno uključuje PDV, pa se uspoređuje s iznosom s PDV-om [PRETPOSTAVKA]

D1 (izmjena):  flag if growth > limit_deminimis(type) and initial_value >= 10_000
               limit_deminimis = 0.10 (robe, usluge) | 0.15 (radovi)       # ZJN čl. 320
               magnitude band: >30 % (iznad čl. 316 st. 2), >50 % (iznad čl. 317)
D2 (preplata): flag if overpay > 0.10 and paid_amount - total_with_vat >= 5_000
exclude if framework_agreement or contract is "okvirni"   # količine su okvirne
explanation text present -> pass it to AI procjena (razlog izmjene)
```

**Pravni kontekst** (ZJN, NN 48/26) [VERIFICIRANO]:
- čl. 315: izmjene predviđene dokumentacijom su dopuštene bez obzira na vrijednost;
- čl. 316 st. 2: ≤ 30 % za dodatne radove, usluge i robe;
- čl. 317: ≤ 50 % za nepredviđene okolnosti;
- čl. 320: de minimis 10 %/15 %;
- **čl. 314 st. 2: izmjene po čl. 315, 318, 319 i 320 objavljuju se u EOJN-u u roku od 30 dana.**

UI prikazuje pravni kontekst samo kao **okvir**. Nikad ne tvrdi da je granica prekoračena nezakonito, jer ne znamo osnovu izmjene.

**Pouzdanost:** `evidence_strength` = jaka za činjenicu povećanja (izravni brojevi), slaba za "neuobičajenost" bez osnove izmjene.

**Lažno pozitivni:**
- okvirni sporazumi;
- indeksacija cijena;
- ugovori s jediničnim cijenama (komunalne usluge, gorivo);
- PDV zabune (iznos s PDV-om ili bez njega).

**Tekst:** "Ugovor je sklopljen na 200.000 € bez PDV-a, a ukupni iznos s izmjenama je 350.000 € (+75 %). Registar ne navodi razlog izmjene. Za dodatne radove ZJN predviđa ograničenje od 30 % (čl. 316.), a za nepredviđene okolnosti 50 % (čl. 317.). Izmjena predviđena u dokumentaciji (čl. 315.) nema takvo ograničenje."

**Što NE znači:** "Povećanje može biti predviđeno ugovorom ili uzrokovano okolnostima koje naručitelj nije mogao predvidjeti. Bez dokumentacije o izmjeni ne može se zaključiti je li izmjena bila opravdana."

**Vizualizacija:** "vodopad": početni iznos → izmjena → ukupni → isplaćeno, s poveznicom na redak registra.

**Implementacija (`rules-v1`, 2026-11-02):** `rules/ValueIncreaseRule` (D1) i `rules/OverpaymentRule` (D2), pragovi u `apps/api/src/main/resources/config/thresholds.yaml`.
- Granice su strogo "veće od": točno +10 % nije indikator, kao ni preplata od točno +10 % i 5.000 EUR.
- Ugovor na temelju okvirnog sporazuma (stupac ili "okvirn" u predmetu) se preskače. Ugovori s fizičkim osobama se ne obrađuju (GDPR).
- D2 bez ukupnog iznosa s PDV-om koristi početni iznos s PDV-om samo ako registar ne navodi izmjenu; tada je kvaliteta `srednja`.
- **Kvaliteta podataka:**
  - `visoka` za redak klase A bez upozorenja iz ingesta;
  - `srednja` ako redak ima ikakvo upozorenje (npr. neispravan OIB, nečitljiv datum izvršenja) ili je ulaz izveden;
  - **`niska`** ako brojevi upućuju na grešku u izvoru:
    - isplaćeni ili ukupni iznos je ≥ 10 × ugovorenog (`implausible-ratio`, heuristika, nije zakonski prag); za D2 bez obrazloženja u registru granica je 5 × (`implausible-ratio-without-note`, #41: Ivanec 2021.–2022. ima niz isplata 9–10× bez obrazloženja);
    - napomena navodi jedinične cijene ili količine ("jedinič…");
    - iznos s PDV-om je manji od iznosa bez PDV-a (samo D2).
- **Pouzdanost dokaza:** `srednja` jer su iznosi izravni, ali osnova izmjene ili isplate nije poznata; `slaba` kad je kvaliteta `niska`.
- **`severity_note`:** `vrijedi_provjeriti`; `informativno` kad je kvaliteta `niska`. Veličina odstupanja se uvijek računa iz objavljenih brojeva i ne "popravlja" se. Loša kvaliteta snižava samo dimenziju kvalitete.
- Zašto: provjera točnosti parsera (2026-11-02) našla je u stvarnim registrima tri vrste grešaka. Bez ovih zaštita to bi bili najveći "indikatori" u sustavu:
  - isplata upisana bez decimalnog zareza (516902 umjesto ~5.169,02);
  - jedinična cijena upisana kao iznos ugovora (30,09 EUR, isplaćeno 6.523,61);
  - negativan PDV (iznos s PDV-om 700 uz 7.000 bez PDV-a).
- Obrazloženje iz registra ("Obrazloženje iznosa plaćanja") prikazuje se doslovno u tekstu indikatora; AI procjena ga kasnije koristi kao razlog.

### Scenarij E – Niska konkurencija

- **Broj ponuda:** nije dostupan u EOJN izvozu, klasi A, OCDS-u (`numberOfTenderers` = 0/56.866) ni u nacionalnom uzorku. Mogući izvori su TED obavijesti o dodjeli (samo iznad EU pragova) [NEVERIFICIRANO] i EOJN obavijesti o dodjeli (robots.txt zabranjuje struganje eojn.hr).
- **Status: NIJE IZVEDIVO S TRENUTNIM PODACIMA.**
- **Proxy (Could, slab dokaz):** udio vrijednosti u postupcima bez javne objave (`Pregovarački postupak bez prethodne objave`, `Jednostavna nabava – poziv odabranim GS`) po naručitelju, u usporedbi s usporednom skupinom (robusni z ≥ 3, n ≥ 10).
- **Tekst:** "Udio ugovora sklopljenih bez javnog poziva veći je nego kod usporedivih općina. Broj ponuda nije javno dostupan u strojno čitljivom obliku."
- **Što NE znači:** "Postupci bez objave dopušteni su u zakonom propisanim slučajevima (npr. hitnost, isključiva prava)."

### Scenarij F – Cjepkanje nabave ispod praga

**Ulaz:** naručitelj, dobavljač (OIB), CPV (3 znamenke), datum, `value_eur` bez PDV-a, vrsta predmeta, vrsta postupka. Sve je dostupno.

**Prag po datumu ugovora** (`config/thresholds.yaml`):
```yaml
simple_procurement:
  - {valid_from: 2023-01-01, valid_to: 2026-05-15, goods_services: 26540, works: 66360}
  - {valid_from: 2026-05-16, valid_to: null,      goods_services: 50000, works: 100000}
near_band: 0.70      # "blizu praga" = >= 70 % praga
window_days: 90
```

**Logika:**
```
for each (j, supplier, cpv3) group:
  C = contracts with value < T(date, type), sorted by date
  for each sliding window W of 90 days:
     if len(W) >= 2 and Σ value(W) >= T and max(value(W)) >= near_band * T * 0.5:
        flag group with W  (magnitude = Σ value / T)
strong if len(W) >= 3 and all value >= near_band*T
exclude: supplier is komunalno poduzeće in JPT owned by j; CPV for recurring supplies (09 goriva, 65 komunalne) -> "informativno"
```

**Pouzdanost:**
- `evidence_strength` = srednja; jaka ako su ≥ 3 ugovora svaki ≥ 70 % praga;
- slaba ako je CPV podudaranje samo na 2 znamenke.

**Lažno pozitivni:**
- legitimne odvojene potrebe (različite lokacije ili projekti);
- ponavljajuće potrošne nabave;
- **promjena praga 16.05.2026.**: prozor koji prelazi taj datum koristi niži prag.

**Tekst:** "Između 03.02. i 28.04.2025. Grad Primjerovo sklopio je s istim dobavljačem 3 ugovora u istoj CPV skupini (45.2 – radovi na zgradama), svaki ispod praga jednostavne nabave od 66.360 €. Ukupno su vrijedni 171.000 €."

**Što NE znači:** "Ugovori mogu odgovarati odvojenim, unaprijed nepredvidivim potrebama. Ovaj obrazac sam po sebi ne dokazuje namjerno izbjegavanje postupka javne nabave."

**Vizualizacija:** vremenska os s točkama ugovora, crtom praga i zbrojnom trakom za prozor.

**Implementacija (`rules-v1`, 2026-11-03, #41):** `rules/SplittingRule` (`GroupRule`). Pragovi i heuristike su u `thresholds.yaml` (`simple-procurement`, `rule-f`). Odluke kojih nema u ovom dokumentu su u `work/decisions.md` (2026-11-03, #41).
- **Pragovi po datumu sklapanja:**
  - 2017.–2022.: 200.000 kn / 500.000 kn (ZJN 2016 čl. 12 st. 1, NN 120/16; provjereno), preračunato u 26.544,56 € / 66.361,40 €;
  - 2023. – 15.05.2026.: 26.540 € / 66.360 €;
  - od 16.05.2026.: 50.000 € / 100.000 €.
  - Ugovori prije 2017. nemaju prag u konfiguraciji, pa se ne ocjenjuju.
- **Kandidat:** ugovor s dobavljačem i CPV-om, s početnim iznosom bez PDV-a ispod praga svog datuma. Okvirni sporazumi se preskaču, kao i **ugovori iz postupka javne nabave po ZJN-u** (vrsta postupka sadrži "postupak", npr. lotovi otvorenog postupka; ručni pregled 2026-11-03 našao je 16 takvih od 164). Vrsta predmeta za A2 se izvodi iz CPV-a (`45` = radovi), pa je kvaliteta tada `srednja` i `missing_data` to navodi.
- **Prozor:** razmak prvog i zadnjeg ugovora ≤ 90 dana (točno 90 ulazi). Prozor koji prelazi promjenu praga koristi **najniži** prag u prozoru. Prozori se ne preklapaju.
- **CPV2:** ugovori koji nisu u CPV3 prozoru, a imaju različite CPV3 unutar istog CPV2, mogu dati indikator s dokazom `slaba`.
- **`informativno`:** dobavljač u vlasništvu naručitelja (JPT) ili CPV 09/65 (ponavljajuće nabave).
- **Pouzdanost:** `jaka` uz ≥ 3 ugovora, svaki ≥ 70 % praga; `slaba` uz podudaranje samo na CPV2; inače `srednja`.
- Indikator je `subject_type = group`. `subject_ref` sadrži `buyerOib`, `supplierOib`, `cpv`, `cpvLevel`, `from`, `to` i `contractIds`. Veličina odstupanja je zbroj/prag (`omjer`).

### Scenarij G – Vremenske anomalije (prosinac, predizborno razdoblje)

**Ulaz:** datum i iznos za **više godina** po JLS-u; datumi izbora (`config/elections.yaml`: `2025-05-18` [VERIFICIRANO], `2025-06-01` [NEVERIFICIRANO], `2021-05-16` [NEVERIFICIRANO, unijeti nakon provjere]).

**Logika:**
```
share_dec(j, y) = V(j, y, Dec) / V(j, y)
baseline_j      = median(share_dec(j, y') for other years y')    # treba >= 2 druge godine
peer            = median(share_dec(p, y) for p in peer group)
flag if share_dec >= 0.30 and share_dec >= 2*baseline_j and share_dec >= 2*peer
pre-election: window = [E-56d, E-1d]; same structure, vs. same window in non-election years
```

**Pouzdanost:**
- klasa A ima uglavnom 2024–2025, pa je bazna linija često nedostupna i `evidence_strength` = slaba;
- OCDS 2018–2024 popravlja povijest, ali se spaja **po nazivu**, pa je `data_quality` = srednja.

**Lažno pozitivni:**
- EU projekti s rokovima;
- zimska održavanja;
- zakonski rokovi;
- tehnička objava više ugovora odjednom.

**Tekst:** "U prosincu 2025. sklopljeno je 38 % godišnje vrijednosti ugovora Općine Primjerovo. U prethodnim godinama taj udio je bio 9–12 %, a kod usporednih općina 8 %."

**Što NE znači:** "Krajem godine se često zaključuju projekti financirani iz EU fondova ili državnog proračuna s fiksnim rokom."

**Vizualizacija:** mali višestruki stupčasti grafovi po mjesecima za svaku godinu, s istaknutim prosincem i mjesecom izbora.

---

## 4. AI procjena ("AI procjena – zahtijeva ljudsku provjeru")

### 4.1 Hibridni tok
```
pravila A–G (determ.) ─┐
zapis ugovora + izvor ─┼─► kontekst ► Haiku (prvi prolaz) ─► validator ─► spremi
Sudreg (NKD, datum) ───┤                    │ visoka_pozornost
statistika usp. skupine┘                    └► Sonnet (ponovna provjera) ─► validator ─► spremi
```
- **Modeli:** `claude-haiku-5-5` (masovno) i `claude-sonnet-5-5` (provjera svih `visoka_pozornost`). Oba se postavljaju konfiguracijom.
- **Cache:** ključ je `sha256(prompt_version + model + kanonski JSON konteksta)`. Ponovno računanje se radi samo kad se promijeni zapis, rezultat pravila ili verzija prompta.
- **Budžet:**
  - `ai.max_usd_per_run` i `ai.max_calls_per_run` u konfiguraciji;
  - kad se budžet potroši, run staje, a pravila i UI rade dalje;
  - potrošnja se bilježi u `llm_usage` (postojeći obrazac `LlmUsage` iz repoa `Desktop/datathon`).
- **Odabir zapisa za AI** (kontrola troška): ugovori s barem jednim indikatorom pravila **ili** vrijednošću ≥ 20.000 € **ili** nejasnim predmetom (heuristika: predmet kraći od 4 riječi ili iz popisa generičkih fraza).

### 4.2 Ulaz u model (primjer konteksta)
```json
{
  "ugovor": {"id": "c_123", "predmet": "Usluge savjetovanja", "cpv": "79411000 - Opće usluge savjetovanja u području upravljanja",
             "vrijednost_eur": 49800, "datum": "2025-11-28", "vrsta_postupka": "Jednostavna nabava - e-Dostava s pozivom odabranim GS",
             "izvor_url": "https://…/registar-ugovora.xlsx#r=41"},
  "dobavljac": {"oib": "00000000001", "naziv": "Primjer d.o.o.", "nkd": "62.01", "datum_osnivanja": "2025-06-02", "pravni_oblik": "d.o.o.", "izvor": "sudreg|fixture|nedostupno"},
  "naručitelj": {"naziv": "Općina Primjerovo", "stanovnika": 3120, "usporedna_skupina": "P2"},
  "pravila": [{"rule_code": "F_SPLITTING", "magnitude": "1,9 × prag", "evidence_strength": "srednja"}],
  "statistika_skupine": {"cpv2_79_eur_po_stanovniku_medijan": 1.8, "ova_opcina": 16.0},
  "slobodni_tekst": {"obrazlozenje_placanja": null, "napomena": null}
}
```

### 4.3 Izlaz (vrši se preko tool use / structured output, shema je obvezna)
```json
{
  "type": "object",
  "required": ["oznaka", "razlozi", "nedostajuci_podaci", "moguca_legitimna_objasnjenja", "pouzdanost"],
  "properties": {
    "oznaka": {"enum": ["nema_oznake", "vrijedi_provjeriti", "visoka_pozornost"]},
    "razlozi": {"type": "array", "items": {
      "type": "object", "required": ["tvrdnja", "polja", "izvori"],
      "properties": {
        "tvrdnja": {"type": "string", "maxLength": 300},
        "polja": {"type": "array", "items": {"type": "string"}, "minItems": 1},
        "izvori": {"type": "array", "items": {"type": "string"}, "minItems": 1}}}},
    "nedostajuci_podaci": {"type": "array", "items": {"type": "string"}},
    "moguca_legitimna_objasnjenja": {"type": "array", "items": {"type": "string"}, "minItems": 1},
    "pouzdanost": {"enum": ["niska", "srednja", "visoka"]}
  }
}
```
Sustav sam dodaje `model`, `prompt_version`, `created_at` i `input_hash`. Te vrijednosti **ne dolaze** od modela.

### 4.4 Validator (deterministički, nakon svakog poziva)
```
reject if oznaka != "nema_oznake" and len(razlozi) == 0
for r in razlozi:
   reject r if any(p not in context_field_paths for p in r.polja)          # citirano polje mora postojati
   reject r if any(u not in context_source_urls for u in r.izvori)          # samo URL-ovi iz konteksta
   reject r if contains_forbidden(r.tvrdnja)                                # popis zabranjenih riječi (lematizirano)
   reject r if mentions natural person name (supplier.type in {obrt, fizicka_osoba})
if all razlozi rejected: oznaka := "nema_oznake", status := "odbačeno_validacijom"
if len(moguca_legitimna_objasnjenja) == 0: reject whole response
```
Odbačeni odgovori se spremaju (`ai_assessment.status = rejected`) radi evaluacije. Nikad se ne prikazuju.

### 4.5 Sistemski prompt (verzija `ai-flag-v1`)

```text
Ti si analitičar javne nabave u alatu JavniRadar. Pomažeš novinarima, vijećnicima i revizorima odlučiti koje ugovore vrijedi ručno provjeriti.
Ne utvrđuješ krivnju i ne donosiš pravne zaključke.

PRAVILA:
1. Koristi ISKLJUČIVO podatke iz priloženog JSON konteksta. Ne izmišljaj činjenice, iznose, datume ni izvore.
2. Svaki razlog mora navesti točna polja iz konteksta (npr. "ugovor.predmet", "dobavljac.nkd") i barem jedan URL iz konteksta.
3. Obavezno navedi barem jedno moguće legitimno objašnjenje.
4. Dopuštene riječi: "neuobičajeno", "vrijedi provjeriti", "sumnjivo", "odstupa od usporednih općina".
   ZABRANJENO: "korupcija", "korupcijski", "kriminal", "nezakonito", "protuzakonito", "prijevara", "pronevjera", "namješteno", "mito", "sukob interesa" kao tvrdnja.
5. Nikad ne imenuj fizičke osobe niti im pripisuj odgovornost. Za obrte i fizičke osobe govori samo "dobavljač".
6. Ako podataka nema dovoljno, vrati "nema_oznake" i navedi što nedostaje.
7. "visoka_pozornost" koristi samo kad se barem DVA neovisna signala potkrepljuju (npr. nesklad djelatnosti + mlada tvrtka + iznos blizu praga).

ŠTO TRAŽIŠ (uz rezultate pravila):
- nesklad predmeta ugovora i registrirane djelatnosti dobavljača (NKD);
- nejasan ili generički predmet za velik iznos ("usluge savjetovanja", "ostale usluge", "razni radovi");
- kombinacije slabih signala (mlada tvrtka, postupak bez objave, iznos blizu praga, ponovljene dodjele);
- neslaganja između plana, ugovora i isplate opisana u slobodnom tekstu;
- neuobičajene formulacije u obrazloženju izmjena ili isplata.

Odgovori isključivo pozivom alata `zabiljezi_procjenu`.
```

**Few-shot primjeri** (svi su sintetički i dio prompta):
1. *Nesklad djelatnosti:* "Primjer IT d.o.o." (NKD 62.01, osnovan prije 7 mjeseci) dobio je "Asfaltiranje nerazvrstanih cesta" za 98.000 €. Očekivano: `vrijedi_provjeriti`. Razlozi citiraju `dobavljac.nkd`, `ugovor.predmet`, `dobavljac.datum_osnivanja`. Legitimno objašnjenje: "dobavljač je možda nositelj zajednice ponuditelja ili koristi podugovaratelje".
2. *Generički predmet + blizu praga + F:* očekivano `visoka_pozornost` (dva neovisna signala).
3. *Komunalno poduzeće u vlasništvu općine, odvoz otpada, 100 % udio:* očekivano `nema_oznake`, objašnjenje "in-house odnos".

### 4.6 Evaluacija
- **Skup:** 40 sintetičkih "sumnjivih" slučajeva (generirani iz predložaka A–G + nesklad NKD, ručno provjereni) i 60 stvarnih ugovora iz klase A, nasumičnih, koje ručno pregledaju dva člana tima i označe kao "ne bi trebalo označiti" ili "vrijedi provjeriti".
- **Metrike:**
  - preciznost i odziv `oznaka ≠ nema_oznake` u odnosu na ručnu oznaku;
  - stopa lažno pozitivnih na stvarnim neutralnim ugovorima;
  - stopa odbacivanja u validatoru;
  - podudaranje s pravilima (Cohenova kappa).
- **Gdje se objavljuje:** stranica "Metodologija" i `docs/EVALUATION.md` (izrađuje se u izradi). **Ne objavljujemo brojke koje nismo izmjerili.**
- **Povratna informacija:** gumb "Korisno / Pogrešno" na AI procjeni sprema u `feedback` (`target_type = ai_assessment`).

## 4a. Pojas veličine odstupanja (`magnitude_band`, #41)
`nizak` / `srednji` / `visok` po pravilu, granice u `thresholds.yaml` (`bands`). To je heuristika za filtre i izvješća, **nije** zbirna ocjena i ne spaja tri dimenzije.

| Pravilo | Veličina | srednji od | visok od |
|---|---|---|---|
| D1 | rast (udio) | > 30 % (čl. 316) | > 50 % (čl. 317) |
| D2 | preplata (udio) | > 30 % | > 100 % |
| F | zbroj / prag | ≥ 1,5× | ≥ 2× |
| C | HHI | ≥ 7.500 | ≥ 9.000 |

## 5. Testiranje pravila
- Za svako pravilo: `fixtures/rules/{rule}/{pozitivan,negativan,rubni}.json`. Primjeri rubnih slučajeva: prozor koji prelazi 16.05.2026., okvirni sporazum, HRK ugovor, nedostajući OIB.
- Jedinični test provjerava `magnitude`, `data_quality`, `evidence_strength` i da **tekst ne sadrži zabranjene riječi**.
- Regresija: broj indikatora na zamrznutom snapshotu pilot podataka, koji se ne smije neobjašnjeno promijeniti (snapshot test).
