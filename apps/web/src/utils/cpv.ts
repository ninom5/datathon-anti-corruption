/** Short display names for CPV 2008 divisions; not a reclassification of source contracts. */
export const CPV_SOURCE = "https://eur-lex.europa.eu/eli/reg/2008/213/oj/?locale=hr";

const divisionNames: Record<string, string | undefined> = {
    "03": "Poljoprivreda, šumarstvo i ribarstvo", "09": "Goriva i energija",
    "14": "Rudarstvo i minerali", "15": "Hrana, piće i duhan", "16": "Poljoprivredni strojevi",
    "18": "Odjeća i obuća", "19": "Koža i tekstil", "22": "Tiskani proizvodi",
    "24": "Kemijski proizvodi", "30": "Uredska i računalna oprema", "31": "Električni strojevi i oprema",
    "32": "Komunikacijska oprema", "33": "Medicinska oprema i lijekovi", "34": "Prijevozna oprema",
    "35": "Sigurnosna i obrambena oprema", "37": "Glazbena i sportska oprema", "38": "Laboratorijska i precizna oprema",
    "39": "Namještaj i oprema za kućanstvo", "41": "Prikupljena i pročišćena voda",
    "42": "Industrijski strojevi", "43": "Rudarski i građevinski strojevi", "44": "Građevinski materijal",
    "45": "Građevinski radovi", "48": "Programski paketi", "50": "Popravci i održavanje",
    "51": "Usluge instaliranja", "55": "Hotelijerstvo i ugostiteljstvo", "60": "Kopneni prijevoz",
    "63": "Prateće usluge prijevoza", "64": "Pošta i telekomunikacije", "65": "Komunalne usluge",
    "66": "Financijske usluge i osiguranje", "70": "Poslovanje nekretninama",
    "71": "Arhitektura i inženjerstvo", "72": "Informatičke usluge", "73": "Istraživanje i razvoj",
    "75": "Javna uprava i obrana", "76": "Usluge u naftnoj i plinskoj industriji", "77": "Poljoprivredne i šumarske usluge",
    "79": "Poslovne usluge", "80": "Obrazovanje", "85": "Zdravstvene i socijalne usluge",
    "90": "Otpad, čišćenje i okoliš", "92": "Kultura, rekreacija i sport", "98": "Ostale javne i osobne usluge",
};

export const cpvDivisionLabel = (code: string | null | undefined) => {
    if (!code || code === "nepoznato") return "CPV nije objavljen";
    const name = divisionNames[code];
    return name ? `${code} · ${name}` : `CPV ${code}`;
};
