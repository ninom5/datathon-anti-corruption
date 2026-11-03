import type {DataQuality, EvidenceStrength, FileStatus, OrganizationType, SubjectType} from "@/types";

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

export const evidenceStrengthHints: Record<EvidenceStrength, string> = {
    jaka: "Objavljeni podaci izravno i dosljedno podupiru obrazac.",
    srednja: "Iznosi su izravno iz izvora, ali razlog nije objavljen.",
    slaba: "Podaci obrazac podupiru samo posredno (npr. moguća greška pri unosu ili slabije podudaranje predmeta).",
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
