"""
Generates the synthetic AI evaluation set (docs/ANOMALY_DETECTION.md §4.6): 40 contexts in the exact shape of
AiContextBuilder (§4.2/§4.7), each with the expected label written BEFORE any model run.

SINTETIČKI PODACI ZA DEMONSTRACIJU: fictional organizations ("Općina Primjerovo", "Primjer ... d.o.o."), OIBs
000000000xx, fictional URLs on primjer.invalid. NKD and founding dates are fixtures ("izvor": "fixture"), because
the real company register (Sudreg) is not connected yet.

Expected labels follow prompt ai-flag-v2: a flag only when there is a signal the rules do not already state;
"visoka_pozornost" needs two independent signals. The metric is binary (flag = label != nema_oznake); the exact label
is reported as a secondary number.

v2 of the set (2026-11-03) fixes data errors of v1 found in the first run: goods had "vrsta_predmeta": "usluge"
(electricity, fuel, saplings, vehicle, textbooks) and one CPV did not match its subject. The model flagged those
contradictions correctly, so v1 expected labels were wrong for 4 cases. Results of v1 are kept in work/eval/.

v3 (2026-11-03) adds "ugovor.prag_jednostavne_nabave_eur" and "prag_izvor", which AiContextBuilder now fills from
thresholds.yaml (all cases are signed in 2025: 26.540 EUR goods/services, 66.360 EUR works).

v4 (2026-11-04) fixes one more data error: the informative rule F case (recurring fuel) had a single contract of
62.000 EUR by simple procurement, above the 26.540 EUR threshold; a single contract of a split group is below it.
This is the last change to the set; later results are reported as measured, without changing labels.

Run: python work/eval/generate_synthetic_cases.py  (writes apps/api/src/main/resources/ai/eval/synthetic-cases-v4.json)
"""
import json
from pathlib import Path

OUT = Path(__file__).resolve().parents[2] / "apps/api/src/main/resources/ai/eval/synthetic-cases-v4.json"
BUYERS = [("Općina Primjerovo", "00000000001", 3120, "P2"), ("Općina Oglednik", "00000000002", 1840, "P1"),
          ("Grad Uzorkovo", "00000000003", 14200, "P4"), ("Općina Probnik", "00000000004", 6100, "P3")]


def url(case_no):
    return f"https://primjer.invalid/eval/registar-ugovora.xlsx#sheet=1;row={case_no + 2}"


def rule(code, name, magnitude, band, evidence, severity, explanation, case_no):
    return {"rule_code": code, "naziv": name, "magnitude": magnitude, "pojas": band, "evidence_strength": evidence,
            "data_quality": "visoka", "oznaka": severity, "objasnjenje": explanation, "izvori": [url(case_no)]}


def context(case_no, subject, cpv, value, procedure, supplier, buyer_index=0, rules=(), paid=None, initial=None,
            amendments=None, payment_note=None, note=None, peer=None, date="2025-06-12", subject_type="usluge"):
    name, oib, population, group = BUYERS[buyer_index]
    return {
        "dobavljac": {"datum_osnivanja": supplier.get("founded"), "izvor": supplier.get("source", "nedostupno"),
                      "jpt_oznaka": supplier.get("jpt"), "naziv": supplier["name"], "nkd": supplier.get("nkd"),
                      "oib": supplier["oib"], "pravni_oblik": supplier.get("form", "doo"),
                      "u_vlasnistvu_narucitelja": supplier.get("owned")},
        "narucitelj": {"naziv": name, "oib": oib, "stanovnika": population, "usporedna_skupina": group,
                       "vrsta": "jls"},
        "pravila": list(rules),
        "slobodni_tekst": {"napomena": note, "obrazlozenje_placanja": payment_note},
        "statistika_skupine": peer,
        "ugovor": {"cpv": cpv, "datum": date, "eojn_url": None, "id": 900000 + case_no,
                   "isplaceno_eur": paid, "izmjene_eur": amendments, "izvor_url": url(case_no),
                   "okvirni_sporazum": False, "pocetna_vrijednost_eur": initial if initial is not None else value,
                   "predmet": subject, "vrijednost_eur": value, "vrsta_postupka": procedure,
                   "vrsta_predmeta": subject_type,
                   "prag_jednostavne_nabave_eur": THRESHOLDS_2025[subject_type], "prag_izvor": THRESHOLD_SOURCE},
    }


def supplier(n, name, nkd=None, founded=None, form="doo", owned=None, jpt=None):
    return {"oib": f"000000005{n:02d}", "name": name, "nkd": nkd, "founded": founded, "form": form, "owned": owned,
            "jpt": jpt, "source": "fixture" if nkd or founded else "nedostupno"}


# Copied from apps/api/src/main/resources/config/thresholds.yaml, period 2023-01-01 .. 2026-05-15.
THRESHOLDS_2025 = {"robe": 26540, "usluge": 26540, "radovi": 66360}
THRESHOLD_SOURCE = "ZJN čl. 12 st. 1; docs/DATA_SOURCES.md §10, ANOMALY_DETECTION.md §F"

CASES = []


def add(scenario, expected, description, ctx):
    CASES.append({"id": f"S{len(CASES) + 1:02d}", "scenario": scenario, "expected": expected,
                  "description": description, "context": ctx})


def build():
    n = lambda: len(CASES) + 1  # noqa: E731
    # --- NKD mismatch (needs company register data; here fixture) ---
    for subject, cpv, nkd, firm, founded, expected, stype in [
        ("Asfaltiranje nerazvrstanih cesta", "45233140 - Radovi na cestama", "62.01", "Primjer IT d.o.o.", "2025-01-10", "visoka_pozornost", "radovi"),
        ("Izgradnja oborinske odvodnje", "45232130 - Radovi na oborinskoj kanalizaciji", "56.10", "Primjer catering d.o.o.", "2016-03-02", "vrijedi_provjeriti", "radovi"),
        ("Nabava školskih udžbenika", "22112000 - Udžbenici", "41.20", "Primjer gradnja d.o.o.", "2024-11-20", "visoka_pozornost", "robe"),
        ("Održavanje javne rasvjete", "50232100 - Održavanje javne rasvjete", "96.02", "Primjer frizerski salon d.o.o.", "2012-05-14", "vrijedi_provjeriti", "usluge"),
    ]:
        k = n()
        add("nesklad_nkd", expected, f"NKD {nkd} ne odgovara predmetu; osnovan {founded}",
            context(k, subject, cpv, 86000.00, "Otvoreni postupak",
                    supplier(k, firm, nkd=nkd, founded=founded), buyer_index=k % 4, subject_type=stype))
    # --- Simple procurement far above the threshold ---
    for subject, cpv, value, stype in [("Održavanje računalnih programa", "50324100 - Usluge održavanja sustava", 412000.00, "usluge"),
                                       ("Nabava komunalnog vozila", "34144510 - Vozila za odvoz otpada", 188000.00, "robe"),
                                       ("Uređenje dječjeg igrališta u središtu naselja", "45112723 - Krajobrazni radovi za igrališta", 152000.00, "radovi")]:
        k = n()
        add("jednostavna_iznad_praga", "vrijedi_provjeriti", f"jednostavna nabava za {value} EUR",
            context(k, subject, cpv, value, "Jednostavna nabava", supplier(k, f"Primjer {k} d.o.o."), buyer_index=k % 4,
                    subject_type=stype))
    # --- Generic subject with a large value ---
    for subject, value in [("Usluge savjetovanja", 78000.00), ("Ostale usluge", 64000.00), ("Razni radovi", 121000.00)]:
        k = n()
        add("genericki_predmet", "vrijedi_provjeriti", f"generički predmet, {value} EUR",
            context(k, subject, "79411000 - Opće usluge savjetovanja u području upravljanja" if "savjet" in subject.lower()
                    else "98000000 - Ostale komunalne, socijalne i osobne usluge", value, "Otvoreni postupak",
                    supplier(k, f"Primjer usluge {k} d.o.o."), buyer_index=k % 4))
    # --- Generic subject + rule F (two signals) ---
    for subject in ["Usluge savjetovanja", "Razne usluge održavanja"]:
        k = n()
        f = rule("F_SPLITTING", "Više manjih ugovora s istim dobavljačem (moguće cjepkanje)", "1,8 × prag", "srednji",
                 "jaka", "vrijedi_provjeriti", "3 ugovora istog dobavljača u 70 dana, zbroj 1,8 × prag jednostavne nabave.", k)
        add("genericki_i_f", "visoka_pozornost", "generički predmet uz pravilo F",
            context(k, subject, "79411000 - Opće usluge savjetovanja u području upravljanja", 24800.00,
                    "Jednostavna nabava", supplier(k, f"Primjer savjet {k} d.o.o."), buyer_index=k % 4, rules=[f]))
    # --- CPV does not match the subject ---
    for subject, cpv, stype in [("Opskrba električnom energijom", "30192000 - Uredske potrepštine", "robe"),
                                ("Rekonstrukcija mrtvačnice", "15800000 - Razni prehrambeni proizvodi", "radovi"),
                                ("Košnja javnih površina", "48000000 - Programski paketi i informacijski sustavi", "usluge")]:
        k = n()
        add("cpv_nesklad", "vrijedi_provjeriti", "CPV ne odgovara predmetu",
            context(k, subject, cpv, 58000.00, "Otvoreni postupak", supplier(k, f"Primjer {k} d.d.", form="dd"),
                    buyer_index=k % 4, subject_type=stype))
    # --- Payment note contradicts the amounts (kuna additions, amendments not published) ---
    for paid, note in [(141000.00, "Dodatak ugovoru br. 1 – dodatni radovi 210.000,00 kn bez PDV-a"),
                       (99000.00, "Isplaćeno po privremenim situacijama 1-6 i dodatku ugovoru (nepredviđeni radovi)")]:
        k = n()
        d2 = rule("D_OVERPAYMENT", "Isplaćeno više od ugovorenog", f"+{round((paid / 100000 - 1) * 100)} % iznad ugovorenog iznosa s PDV-om",
                  "srednji", "srednja", "vrijedi_provjeriti", "Isplaćeno je više od ugovorenog iznosa s PDV-om; registar ne navodi izmjenu.", k)
        add("isplata_tekst", "vrijedi_provjeriti", "obrazloženje plaćanja navodi dodatke koji nisu upisani kao izmjena",
            context(k, "Rekonstrukcija nerazvrstane ceste", "45233140 - Radovi na cestama", 80000.00, "Otvoreni postupak",
                    supplier(k, f"Primjer ceste {k} d.o.o."), buyer_index=k % 4, rules=[d2], paid=paid,
                    payment_note=note, subject_type="radovi"))
    # --- Peer comparison (scenario A, level 2) ---
    for ratio in [6.4, 4.1]:
        k = n()
        peer = {"godina": 2025, "godina_nepotpuna": False, "cpv2": "79", "kategorija": "Poslovne usluge",
                "usporedna_skupina": "P2", "n_opcina_s_podacima": 11, "medijan_eur_po_stanovniku": 2.5,
                "ova_opcina_eur_po_stanovniku": round(2.5 * ratio, 2), "rang": 1}
        add("usporedba_skupine", "vrijedi_provjeriti", f"{ratio}× medijan skupine u kategoriji",
            context(k, "Usluge izrade strateških dokumenata", "79411000 - Opće usluge savjetovanja u području upravljanja",
                    46000.00, "Otvoreni postupak", supplier(k, f"Primjer konzalting {k} d.o.o."), buyer_index=0, peer=peer))
    # --- Negative: normal contracts, specific subject, consistent fields, no rules ---
    for subject, cpv, value, procedure, stype in [
        ("Izgradnja nogostupa uz državnu cestu D8 u naselju Primjerovo, dionica km 2+100 do 2+650", "45233161 - Radovi na izgradnji pješačkih staza", 186000.00, "Otvoreni postupak", "radovi"),
        ("Nabava uredskog materijala za 2025. godinu prema troškovniku", "30192000 - Uredske potrepštine", 9800.00, "Jednostavna nabava", "robe"),
        ("Izrada projektne dokumentacije za rekonstrukciju dječjeg vrtića Sunce", "71320000 - Usluge tehničkog projektiranja", 24500.00, "Jednostavna nabava", "usluge"),
        ("Sanacija krovišta društvenog doma u naselju Oglednik", "45261900 - Popravak i održavanje krovova", 54000.00, "Otvoreni postupak", "radovi"),
        ("Usluga zimske službe na nerazvrstanim cestama 2025./2026.", "90620000 - Usluge čišćenja snijega", 38000.00, "Otvoreni postupak", "usluge"),
        ("Nabava vatrogasnog navalnog vozila za DVD Probnik", "34144210 - Vatrogasna vozila", 310000.00, "Otvoreni postupak", "robe"),
        ("Rekonstrukcija javne rasvjete – zamjena 420 svjetiljki LED svjetiljkama", "45316110 - Postavljanje opreme za javnu rasvjetu", 212000.00, "Otvoreni postupak", "radovi"),
        ("Stručni nadzor nad izgradnjom reciklažnog dvorišta", "71520000 - Usluge nadzora gradnje", 18900.00, "Jednostavna nabava", "usluge"),
        ("Opskrba prirodnim plinom za zgradu općinske uprave", "09123000 - Prirodni plin", 21000.00, "Jednostavna nabava", "robe"),
        ("Održavanje zelenih površina u naselju Uzorkovo 2025.", "77310000 - Usluge sadnje i održavanja zelenih površina", 47000.00, "Otvoreni postupak", "usluge"),
        ("Geodetski elaborat za evidentiranje nerazvrstanih cesta", "71355000 - Geodetske usluge", 14700.00, "Jednostavna nabava", "usluge"),
    ]:
        k = n()
        add("uobicajen", "nema_oznake", "uobičajen ugovor, polja usklađena",
            context(k, subject, cpv, value, procedure, supplier(k, f"Primjer {k} d.o.o."), buyer_index=k % 4,
                    subject_type=stype))
    # --- Negative: only a rule result, with context that explains it ---
    for k_desc, rules_, sup, note in [
        ("C na komunalnom društvu u vlasništvu općine (in-house)",
         [rule("C_CONCENTRATION", "Ponovljene dodjele istom dobavljaču (koncentracija)", "HHI 10.000", "visok", "srednja",
               "informativno", "Svi ugovori kategorije 90 u tri godine dodijeljeni su istom dobavljaču, komunalnom društvu naručitelja.", 0)],
         dict(name="Primjer Komunalac d.o.o.", owned=True, jpt="99999"), None),
        ("C na opskrbi električnom energijom (malo dobavljača)",
         [rule("C_CONCENTRATION", "Ponovljene dodjele istom dobavljaču (koncentracija)", "HHI 9.600; najveći dobavljač 97,9 % vrijednosti",
               "visok", "srednja", "vrijedi_provjeriti", "Opskrba energijom u kategoriji 09 većinom ide istom dobavljaču.", 0)],
         dict(name="Primjer Energija d.d.", form="dd"), None),
        ("D1 s objavljenim razlogom izmjene",
         [rule("D_VALUE_INCREASE", "Povećanje vrijednosti ugovora", "+32 % u odnosu na početni iznos", "srednji", "srednja",
               "vrijedi_provjeriti", "Ugovor je sklopljen na 150.000,00 €, a s izmjenama je 198.000,00 € (+32 %).", 0)],
         dict(name="Primjer gradnja d.o.o."), "Izmjena temeljem čl. 315. ZJN: opcija proširenja predviđena dokumentacijom o nabavi, odluka KLASA 400-01/25-01/7"),
        ("F kod ponavljajuće nabave goriva (informativno)",
         [rule("F_SPLITTING", "Više manjih ugovora s istim dobavljačem (moguće cjepkanje)", "1,6 × prag", "srednji", "srednja",
               "informativno", "Ponavljajuća nabava goriva; kategorija 09 je ponavljajuća opskrba.", 0)],
         dict(name="Primjer Nafta d.d.", form="dd"), None),
    ]:
        k = n()
        for r in rules_:
            r["izvori"] = [url(k)]
        is_d1 = rules_[0]["rule_code"] == "D_VALUE_INCREASE"
        subject = {"C_CONCENTRATION": "Odvoz komunalnog otpada" if sup.get("owned") else "Opskrba električnom energijom za javnu rasvjetu",
                   "D_VALUE_INCREASE": "Izgradnja reciklažnog dvorišta", "F_SPLITTING": "Nabava goriva za službena vozila"}[rules_[0]["rule_code"]]
        add("samo_pravilo_s_kontekstom", "nema_oznake", k_desc,
            context(k, subject, "90511000 - Usluge odvoza otpada" if sup.get("owned") else "09310000 - Električna energija"
                    if "električn" in subject else "45000000 - Građevinski radovi" if is_d1 else "09134100 - Dizelsko gorivo",
                    198000.00 if is_d1 else 14000.00 if rules_[0]["rule_code"].startswith("F") else 62000.00, "Otvoreni postupak" if not rules_[0]["rule_code"].startswith("F") else "Jednostavna nabava",
                    supplier(k, sup["name"], form=sup.get("form", "doo"), owned=sup.get("owned"), jpt=sup.get("jpt")),
                    buyer_index=k % 4, rules=rules_, initial=150000.00 if is_d1 else None,
                    amendments=48000.00 if is_d1 else None, note=note,
                    subject_type="radovi" if is_d1 else "usluge" if sup.get("owned") else "robe"))
    # --- Negative: data-quality only (missing fields, not suspicious) ---
    for subject, stype in [("Nabava sadnica za uređenje parka", "robe"), ("Tisak općinskog glasila", "usluge"),
                           ("Popravak službenog vozila", "usluge")]:
        k = n()
        add("nedostaju_podaci", "nema_oznake", "mali iznos, malo podataka, ništa neuobičajeno",
            context(k, subject, None, 3200.00, "Jednostavna nabava", supplier(k, f"Primjer {k} d.o.o."), buyer_index=k % 4,
                    subject_type=stype))
    # --- Negative: peer statistics within the normal range ---
    k = n()
    add("usporedba_normalno", "nema_oznake", "potrošnja blizu medijana skupine",
        context(k, "Izrada strategije razvoja turizma Općine Primjerovo 2026.-2030.", "79411000 - Opće usluge savjetovanja u području upravljanja",
                9600.00, "Jednostavna nabava", supplier(k, f"Primjer turizam {k} d.o.o."), buyer_index=0,
                peer={"godina": 2025, "godina_nepotpuna": False, "cpv2": "79", "kategorija": "Poslovne usluge",
                      "usporedna_skupina": "P2", "n_opcina_s_podacima": 11, "medijan_eur_po_stanovniku": 2.5,
                      "ova_opcina_eur_po_stanovniku": 3.1, "rang": 5}))
    # --- Positive: young company with large works contract, no NKD mismatch info but founded recently ---
    for founded, value in [("2025-03-01", 240000.00), ("2025-01-15", 180000.00)]:
        k = n()
        add("mlada_tvrtka", "vrijedi_provjeriti", f"osnovan {founded}, ugovor {value} EUR",
            context(k, "Izgradnja sportske dvorane – II. faza", "45212225 - Građevinski radovi na sportskoj dvorani", value,
                    "Otvoreni postupak", supplier(k, f"Primjer nova gradnja {k} d.o.o.", nkd="41.20", founded=founded),
                    buyer_index=k % 4, date="2025-08-20", subject_type="radovi"))


build()
assert len(CASES) == 40, len(CASES)
positives = sum(1 for case in CASES if case["expected"] != "nema_oznake")
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps({"version": "synthetic-cases-v4", "note": "SINTETIČKI PODACI ZA DEMONSTRACIJU",
                           "cases": CASES}, ensure_ascii=False, indent=1), encoding="utf-8")
print(f"{len(CASES)} cases, {positives} expected flags, {len(CASES) - positives} expected nema_oznake -> {OUT}")
