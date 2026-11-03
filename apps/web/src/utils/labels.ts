import type {DataQuality, EvidenceStrength, FileStatus, MagnitudeBand, OrganizationType, RuleCode, SubjectType} from "@/types";

/** Keyed by the API code; counties and other public bodies are not JLS. */
export const buyerTypeLabels: Record<string, string | undefined> = {
    jls: "općina ili grad",
    zupanija: "županija",
    javno_tijelo: "ostalo javno tijelo",
};

export const organizationTypeLabel = (type: OrganizationType | null) => {
    if (!type) {
        return "vrsta nije poznata";
    }
    const labels: Record<OrganizationType, string> = {
        jls: "općina ili grad",
        zupanija: "županija",
        javno_tijelo: "javno tijelo",
        doo: "d.o.o.",
        jdoo: "j.d.o.o.",
        dd: "d.d.",
        obrt: "obrt",
        udruga: "udruga",
        fizicka_osoba: "fizička osoba",
        strana: "strana tvrtka",
        ostalo: "ostalo",
        nepoznato: "vrsta nije poznata",
    };
    return labels[type];
};

export const subjectTypeLabels: Record<SubjectType, string> = {
    robe: "robe",
    usluge: "usluge",
    radovi: "radovi",
    ostalo: "ostalo",
};

export const dataQualityLabels: Record<DataQuality, string> = {
    visoka: "Visoka",
    srednja: "Srednja",
    niska: "Niska",
};

export const dataQualityHints: Record<DataQuality, string> = {
    visoka: "Redak je pročitan iz strukturiranog EOJN izvoza bez upozorenja.",
    srednja: "Redak ima upozorenje pri uvozu ili je dio ulaza izveden.",
    niska: "Brojevi u izvoru upućuju na moguću grešku pri unosu.",
};

export const evidenceStrengthLabels: Record<EvidenceStrength, string> = {
    jaka: "Jaka",
    srednja: "Srednja",
    slaba: "Slaba",
};

const contractEvidenceHints: Record<EvidenceStrength, string> = {
    jaka: "Objavljeni iznosi izravno podupiru obrazac.",
    srednja: "Razlika je izračunata iz iznosa u registru; sama ne potvrđuje razlog ni opravdanost promjene.",
    slaba: "Moguća greška u izvoru smanjuje pouzdanost izračunate razlike.",
};

/** Descriptions of rules-v1; thresholds are evaluated only on the backend. */
export const evidenceStrengthHints: Record<RuleCode, Record<EvidenceStrength, string>> = {
    D_VALUE_INCREASE: contractEvidenceHints,
    D_OVERPAYMENT: contractEvidenceHints,
    F_SPLITTING: {
        jaka: "Najmanje 3 ugovora, svaki s iznosom od najmanje 70 % praga; podudaranje na CPV3.",
        srednja: "Isti dobavljač i CPV3 u vremenskom prozoru; uvjeti za jaku pouzdanost nisu svi ispunjeni.",
        slaba: "Podudara se samo CPV odjeljak (prve 2 znamenke), uz različite podskupine. Predmeti mogu biti različiti.",
    },
    C_CONCENTRATION: {
        jaka: "U odabranom CPV odjeljku i razdoblju ima najmanje 15 ugovora.",
        srednja: "Obrazac se temelji na manjem broju ugovora (manje od 15) u odjeljku i razdoblju.",
        slaba: "Objavljeni podaci daju ograničen dokaz koncentracije.",
    },
};

export const ruleLetter = (ruleCode: string) => ruleCode.charAt(0);

export const fileStatusLabels: Record<FileStatus, string> = {
    uvezeno: "Uvezeno",
    format_nije_podrzan: "Format još nije podržan",
    nestrukturirano: "Nestrukturirana tablica",
    necitljivo: "Datoteka nije čitljiva",
    nedostupno: "Datoteka nije dostupna",
};

export const fileStatusHints: Record<FileStatus, string> = {
    uvezeno: "EOJN izvoz (standardni, klasa A, ili stariji izvještaj, klasa A2) pročitan parserom, ili referentni izvor (JPT, DZS).",
    format_nije_podrzan: "Vlastita tablica naručitelja (klase B i C); parser dolazi nakon POC-a.",
    nestrukturirano: "Naslovi, spojene ćelije ili tekst (klasa D).",
    necitljivo: "Datoteka je preuzeta, ali je oštećena ili nije u navedenom formatu.",
    nedostupno: "Poslužitelj izdavatelja nije vratio datoteku (npr. 403, 404 ili prekinuta veza).",
};

const qualityFlagLabels: Record<string, string> = {
    supplier_oib_malformed: "OIB dobavljača nema 11 znamenki",
    supplier_oib_invalid_checksum: "OIB dobavljača ne prolazi kontrolnu znamenku",
    supplier_oib_missing: "OIB dobavljača nije objavljen",
    buyer_oib_malformed: "OIB naručitelja nema 11 znamenki",
    buyer_oib_invalid_checksum: "OIB naručitelja ne prolazi kontrolnu znamenku",
    subject_missing: "Predmet nije objavljen",
    cpv_invalid: "CPV oznaka nije ispravna",
    unreadable_amount: "Iznos nije čitljiv",
    periodic_amount: "Iznos je naveden po razdoblju (npr. mjesečno)",
    amount_out_of_range: "Iznos je izvan očekivanog raspona",
    unreadable_completed_on: "Datum izvršenja nije čitljiv",
    date_in_future: "Datum je u budućnosti",
    hrk_after_euro: "Iznos u kunama nakon uvođenja eura",
    supplier_consortium: "Zajednica ponuditelja (prikazan je prvi navedeni OIB)",
    gross_below_net: "Iznos s PDV-om manji je od iznosa bez PDV-a",
};

export const qualityFlagLabel = (code: string) => qualityFlagLabels[code] ?? code;

const issueCodeLabels: Record<string, string> = {
    superseded_by_newer_file: "Isti ugovor u novijoj datoteci (duplikat, zadržana novija verzija)",
    missing_required_columns: "Datoteka nema obavezne stupce",
    download_failed: "Preuzimanje nije uspjelo",
    unreadable_file: "Datoteka nije čitljiva",
    unsupported_format: "Format nije podržan",
    missing_contract_url: "Redak odbačen: nema poveznice na ugovor",
    missing_buyer: "Redak odbačen: nema naručitelja",
    missing_date_signed: "Redak odbačen: nema datuma sklapanja",
    unreadable_date_signed: "Redak odbačen: datum sklapanja nije čitljiv",
    missing_amount: "Redak odbačen: nema iznosa",
    overlaps_class_a: "Redak nije uvezen: isti naručitelj i godina već su u standardnom EOJN izvozu",
    buyer_not_in_jpt: "Naručitelj nije jednoznačno pronađen u JPT registru",
    reference_source_failed: "Referentni izvor (JPT ili DZS) nije dostupan",
    dzs_unmatched: "JLS iz DZS popisa nije pronađena u JPT registru",
};

export const issueCodeLabel = (code: string) => issueCodeLabels[code] ?? qualityFlagLabel(code);

/** The one mapping between shareable Croatian URL codes and API rule codes. */
export const ruleOptions: {short: string; code: RuleCode; label: string}[] = [
    {short: "D1", code: "D_VALUE_INCREASE", label: "Povećanje vrijednosti"},
    {short: "D2", code: "D_OVERPAYMENT", label: "Isplata iznad ugovorenog"},
    {short: "F", code: "F_SPLITTING", label: "Moguće cjepkanje"},
    {short: "C", code: "C_CONCENTRATION", label: "Koncentracija dobavljača"},
];

export const magnitudeBandOptions: {value: MagnitudeBand; label: string}[] = [
    {value: "nizak", label: "Nizak"}, {value: "srednji", label: "Srednji"}, {value: "visok", label: "Visok"},
];

export const readRuleCodes = (value: string): RuleCode[] =>
    ruleOptions.filter((option) => value.split(",").includes(option.short)).map((option) => option.code);

export const writeRuleCodes = (codes: RuleCode[]) =>
    ruleOptions.filter((option) => codes.includes(option.code)).map((option) => option.short).join(",");

export const readMagnitudeBand = (value: string | null): MagnitudeBand | undefined =>
    magnitudeBandOptions.find((option) => option.value === value)?.value;
