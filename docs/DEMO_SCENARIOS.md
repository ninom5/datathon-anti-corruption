# JavniRadar – demo scenariji

> Scenariji 1 i 2 su **SINTETIČKI PODACI ZA DEMONSTRACIJU**. Sve organizacije, OIB-i i iznosi su izmišljeni. OIB-i `000000000xx` namjerno nisu valjani. Nijedan sintetički obrazac ne smije se povezati sa stvarnom tvrtkom, institucijom ili osobom.

## 1. Računala: 10.000 € naspram 50.000 € — SINTETIČKI PODACI ZA DEMONSTRACIJU

**Ulaz:**
```json
[
  {"natural_key":"demo-a-1","is_synthetic":true,"buyer":{"oib":"00000000011","name":"Općina Primjerovo","population":3100,"peer_group":"P2"},
   "supplier":{"oib":"00000000021","name":"Primjer informatika d.o.o."},
   "subject":"Nabava 20 prijenosnih računala za osnovnu školu","cpv":"30213100","date_signed":"2025-09-15",
   "initial_value_eur":10000.00,"total_value_eur":10000.00,
   "extracted":{"quantity":20,"unit":"kom","device_type":"laptop","vat_included":false,"includes":{"monitor":"n/a","software":"nepoznato","installation":"nepoznato","warranty_months":"nepoznato"},"confidence":0.93},
   "provenance":{"source_url":"demo://registar-primjerovo.xlsx","locator":"row=12","method":"llm"}},
  {"natural_key":"demo-a-2","is_synthetic":true,"buyer":{"oib":"00000000012","name":"Općina Uzorkovac","population":3400,"peer_group":"P2"},
   "supplier":{"oib":"00000000022","name":"Ogledni sustavi d.o.o."},
   "subject":"Nabava 20 prijenosnih računala s instalacijom i trogodišnjim održavanjem","cpv":"30213100","date_signed":"2025-10-02",
   "initial_value_eur":50000.00,"total_value_eur":50000.00,
   "extracted":{"quantity":20,"unit":"kom","device_type":"laptop","vat_included":false,"includes":{"monitor":"n/a","software":"nepoznato","installation":"da","maintenance_months":36},"confidence":0.91},
   "provenance":{"source_url":"demo://registar-uzorkovac.xlsx","locator":"row=7","method":"llm"}}
]
```

**Koraci:**
1. Razina stavke je moguća (količina je izvađena, confidence ≥ 0,9).
2. Jedinične cijene: A = 10.000 / 20 = **500 €**, B = 50.000 / 20 = **2.500 €**.
3. Omjer = **5,0**, razlika = (2.500 − 500) × 20 = **40.000 €**. Pragovi (omjer ≥ 2, razlika ≥ 5.000 €) su ispunjeni.
4. Provjera usporedivosti: B uključuje instalaciju i 36 mjeseci održavanja, a A nema podatak. Softver i jamstvo su nepoznati kod obiju.
5. Zbog toga je `evidence_strength = slaba` i prikazuje se upozorenje.

**UI:**
- indikator "A · Razlika u jediničnoj cijeni (5,0×)";
- kvaliteta: srednja (LLM ekstrakcija); veličina: 5,0× / 40.000 €; pouzdanost dokaza: **slaba**;
- žuti okvir: **"Nedovoljno podataka za pouzdanu usporedbu: ugovori se razlikuju u uključenim uslugama (instalacija, održavanje)."**

**Upozorenja o podacima:** specifikacija (procesor, RAM), jamstvo i softver nisu objavljeni; je li PDV uključen pretpostavlja se iz stupca.

**Korisnik smije zaključiti:** "Cijene po komadu se razlikuju 5 puta. Drugi ugovor uključuje održavanje, pa usporedba nije izravna. Vrijedi zatražiti specifikaciju."

**Sustav NE smije zaključiti:** da je Uzorkovac preplatio, da je cijena "napuhana" ili da je netko odgovoran.

## 2. Nova tvrtka dobiva ugovor od 800.000 € — SINTETIČKI PODACI ZA DEMONSTRACIJU

**Ulaz:**
```json
{
  "contract":{"natural_key":"demo-b-1","is_synthetic":true,"buyer":{"oib":"00000000013","name":"Grad Primjerograd","population":14200,"peer_group":"P5"},
    "supplier":{"oib":"00000000001","name":"Primjer d.o.o.","type":"doo"},
    "subject":"Usluge savjetovanja i upravljanja projektom","cpv":"79411000","procedure_type":"Otvoreni postupak",
    "date_signed":"2026-04-01","initial_value_eur":800000.00,"total_value_eur":800000.00,
    "provenance":{"source_url":"demo://registar-primjerograd.xlsx","locator":"row=3","method":"parser"}},
  "company_snapshot":{"source":"fixture","founded_on":"2026-03-01","legal_form":"d.o.o.","main_nkd":"70.22","retrieved_at":"2026-11-05T10:00:00Z"},
  "rules_context":{"peer_cpv79_eur_per_capita_median":2.1,"this_buyer":56.3}
}
```

**Koraci:**
1. `age_days` = 2026-04-01 − 2026-03-01 = **31**, a `value_eur` = 800.000. Pravilo B okida (≤ 180 dana, ≥ 50.000 €), razina "vrijedi provjeriti".
2. Fallback A (kategorija 79 po stanovniku): 56,3 € naspram medijana 2,1 €, pa i taj indikator okida (slab dokaz).
3. AI procjena (sintetički očekivani izlaz):
```json
{"oznaka":"visoka_pozornost",
 "razlozi":[
  {"tvrdnja":"Dobavljač je upisan u registar 31 dan prije sklapanja ugovora vrijednog 800.000 €.","polja":["dobavljac.datum_osnivanja","ugovor.datum","ugovor.vrijednost_eur"],"izvori":["demo://registar-primjerograd.xlsx#row=3","fixture://sudreg/00000000001"]},
  {"tvrdnja":"Predmet je općenit ('usluge savjetovanja'), a potrošnja u kategoriji po stanovniku višestruko je veća od usporednih gradova.","polja":["ugovor.predmet","statistika_skupine.cpv2_79_eur_po_stanovniku_medijan"],"izvori":["demo://registar-primjerograd.xlsx#row=3"]}],
 "nedostajuci_podaci":["broj ponuda","dokazi o sposobnosti (reference)","dokumentacija o nabavi"],
 "moguca_legitimna_objasnjenja":["tvrtka je možda nastala izdvajanjem iz postojećeg poslovanja s iskusnim osobljem","projekt je možda jednokratno financiran iz EU fondova"],
 "pouzdanost":"srednja"}
```

**UI:** vremenska crta osnivanje → ugovor (31 dan); indikatori B i A (fallback); AI blok s natpisom "AI procjena – zahtijeva ljudsku provjeru".

**Upozorenja:** "Podaci o osnivanju su iz testnog skupa (Sudski registar zahtijeva registraciju)." Broj ponuda nije dostupan.

**Korisnik smije zaključiti:** "Vrijedi pogledati dokumentaciju o nabavi: kako je mlada tvrtka dokazala sposobnost."

**Sustav NE smije zaključiti:** da je postupak namješten, da postoji veza s dužnosnicima ili da je ugovor nezakonit.

## 3. Stvarni podaci: potrošnja općine po kategorijama u odnosu na skupinu (neutralno)

**Odabir** (na dan 04.11., DATA). Biramo JLS iz klase A koja ima:
- najmanje 100 ugovora u 2024–2025 (kandidati prema provjeri 2026-11-01: Općina Sveti Filip i Jakov 273, Grad Ozalj 263, Grad Ivanec 241, Općina Mikleuš 130, Općina Kloštar Ivanić 114);
- najmanje 8 članova usporedne skupine s podacima.

Ako skupina nema 8 JLS s podacima, uspoređuje se s nacionalnim uzorkom, uz jasnu oznaku "uzorak".

**Prikaz:**
1. Profil JLS: ukupna vrijednost po godini; top 5 CPV kategorija u € po stanovniku; medijan skupine.
2. Jedna kategorija u dot plotu (npr. 45 – građevinski radovi).
3. Ako pravilo D/F/C ima pogodak, prikazuje se **samo činjenica** (npr. "3 ugovora imaju isplaćen iznos veći od ugovorenog; u 2 slučaja registar navodi obrazloženje") i poveznica na izvor. Nazivi dobavljača se u videu **ne ističu** (zamućeni ili izostavljeni).

**Pravilo za video:** stvarni podaci samo za opis i usporedbu. Nikakva "sumnjivost" stvarnih subjekata u naraciji. Svaki stvarni pogodak prije snimanja ručno provjerava član tima, uz izvornu datoteku.

**Korisnik smije zaključiti:** gdje općina troši više ili manje od sličnih i gdje može tražiti dodatne informacije.
**Sustav NE smije zaključiti:** ništa o namjerama ili zakonitosti.

## 4. Testni fixturei
Svaki scenarij se sprema u `apps/api/src/test/resources/fixtures/demo/*.json` i koristi kao jedinični test. Očekivani brojevi (5,0×; 40.000 €; 31 dan) moraju točno izaći.

## 5. Scenarij videa (3:00)

| Vrijeme | Slika | Govor (nacrt) |
|---|---|---|
| 0:00–0:20 **Problem** | karta Hrvatske, 556 JLS, većina sivih | "Hrvatska ima 556 gradova i općina. Samo njih oko 60 objavljuje registar ugovora u obliku koji računalo može pročitati. O potrošnji malih općina rijetko tko piše. JavniRadar to mijenja." |
| 0:20–0:45 **Korisnik i pretraga** | upis "Općina …" u pretragu → profil JLS | "Vijećnica želi znati koliko njezina općina troši na ceste. Profil prikazuje potrošnju po stanovniku i odmah je uspoređuje sa sličnim općinama." (stvarni podaci, scenarij 3) |
| 0:45–1:15 **Indikator pravila** | detalj indikatora D, tri okvira, dokaz → izvorni redak | "Kad ugovor naraste s 200 na 350 tisuća, JavniRadar to pokaže. Ne kaže 'nezakonito'. Pokaže tri stvari: koliko su podaci kvalitetni, koliko je odstupanje i koliko je dokaz jak. Uz to piše što ovo NE znači." (sintetički, s trakom) |
| 1:15–1:50 **AI procjena** | scenarij 2: vremenska crta, AI blok | "AI pregledava kombinacije signala koje pravila ne vide: nova tvrtka, općenit predmet, neuobičajena potrošnja. Svaki razlog mora citirati izvor, a odgovor bez dokaza sustav sam odbacuje. Uvijek piše: zahtijeva ljudsku provjeru." |
| 1:50–2:15 **Usporedba cijena** | scenarij 1 | "Dvije općine, ista računala, peterostruka razlika. Ali JavniRadar vidi da jedna cijena uključuje održavanje, pa upozorava: nedovoljno podataka za pouzdanu usporedbu." |
| 2:15–2:40 **Podaci i arhitektura** | dijagram, stranica Izvori | "Podatke povlačimo preko API-ja data.gov.hr-a i registra javnopravnih tijela, a spajamo ih s popisom stanovništva i sudskim registrom. Svaki broj vodi do retka u izvornoj datoteci. Arhitektura je otvorena: OCDS ulaz, DCAT-AP izlaz." |
| 2:40–3:00 **Utjecaj i dalje** | obrazac za pravo na odgovor, ZPPI predložak | "Općine mogu odgovoriti na svaki indikator, a građani jednim klikom zatražiti dokumente. Sljedeći korak je izravni EOJN API, pa sve 556 općina. JavniRadar: javni novac, javno vidljiv." |

Prije snimanja provjeriti: brojke u govoru odgovaraju API-ju na dan snimanja; sintetički kadrovi imaju traku; nijedan stvarni subjekt nije prikazan u negativnom kontekstu.
