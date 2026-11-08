/**
 * Rule descriptions for the Metodologija page. Values are copied from apps/api/src/main/resources/config/thresholds.yaml
 * and docs/ANOMALY_DETECTION.md (rules-v1, 2026-11-03; preskakanja usklađena s validacijom podataka 2026-11-08); the backend computes with the yaml, this page only explains it.
 * Change both together.
 */
export interface RuleThreshold {
    label: string;
    value: string;
    source: string;
}

export interface RuleDescription {
    letter: string;
    code: string;
    title: string;
    computes: string;
    formula: string;
    thresholds: RuleThreshold[];
    skipped: string;
    notMeaning: string;
}

const HEURISTIC = "heuristika JavniRadara, nije zakonski prag";

export const methodologyRules: RuleDescription[] = [
    {
        letter: "D1",
        code: "D_VALUE_INCREASE",
        title: "Povećanje vrijednosti ugovora",
        computes: "Ukupni iznos s izmjenama u odnosu na početni iznos, oba bez PDV-a, kako ih je naručitelj objavio.",
        formula: "rast = ukupni iznos / početni iznos − 1",
        thresholds: [
            {label: "Oznaka za robe i usluge", value: "rast > 10 %", source: "ZJN čl. 320 (de minimis)"},
            {label: "Oznaka za radove", value: "rast > 15 %", source: "ZJN čl. 320 (de minimis)"},
            {label: "Pojas „srednji”", value: "rast > 30 %", source: "ZJN čl. 316 st. 2 (dodatni radovi, usluge, robe)"},
            {label: "Pojas „visok”", value: "rast > 50 %", source: "ZJN čl. 317 (nepredviđene okolnosti)"},
            {label: "Najmanji početni iznos", value: "10.000 €", source: HEURISTIC},
        ],
        skipped: "Ugovori na temelju okvirnog sporazuma (količine su okvirne, uključujući „tromjesečnu evidenciju ugovora”), ugovori ispod 10.000 € i ponovni upis istog ugovora u registar. Kad vrsta predmeta nije objavljena, prag se bira po CPV-u (45 = radovi).",
        notMeaning: "Povećanje može biti predviđeno ugovorom (čl. 315.) ili uzrokovano okolnostima koje naručitelj nije mogao predvidjeti. Bez dokumentacije o izmjeni ne može se zaključiti je li izmjena bila opravdana.",
    },
    {
        letter: "D2",
        code: "D_OVERPAYMENT",
        title: "Isplaćeno više od ugovorenog",
        computes: "Isplaćeni iznos u odnosu na ukupni ugovoreni iznos s PDV-om, jer isplata uključuje PDV.",
        formula: "razlika = isplaćeno / ukupni iznos s PDV-om − 1",
        thresholds: [
            {label: "Oznaka", value: "razlika > 10 % i barem 5.000 €", source: HEURISTIC},
            {label: "Moguća greška u unosu", value: "isplata ≥ 5× ugovorenog bez obrazloženja, ili ≥ 10× s obrazloženjem", source: HEURISTIC},
        ],
        skipped: "Ugovori bez objavljene isplate, okvirni sporazumi i ponovni upis istog ugovora. Iznos koji izgleda kao kuna upisana kao euro ili iznos s PDV-om manji od iznosa bez PDV-a prikazuje se kao informativan.",
        notMeaning: "Razlika može nastati zbog indeksacije cijena, ugovora s jediničnim cijenama ili zabune s PDV-om u registru. Ne govori je li isplata bila opravdana.",
    },
    {
        letter: "F",
        code: "F_SPLITTING",
        title: "Više manjih ugovora s istim dobavljačem",
        computes: "Ugovori istog naručitelja s istim dobavljačem u istoj CPV skupini, svaki ispod praga jednostavne nabave, sklopljeni unutar 90 dana.",
        formula: "omjer = zbroj ugovora u prozoru / prag jednostavne nabave",
        thresholds: [
            {label: "Prag, robe i usluge", value: "26.544,56 € (2017.–2022.) · 26.540 € · 50.000 € (od 16.05.2026.)", source: "ZJN čl. 12 st. 1 (NN 120/16, NN 48/26)"},
            {label: "Prag, radovi", value: "66.361,40 € (2017.–2022.) · 66.360 € · 100.000 € (od 16.05.2026.)", source: "ZJN čl. 12 st. 1 (NN 120/16, NN 48/26)"},
            {label: "Prozor", value: "najviše 90 dana između prvog i zadnjeg ugovora", source: HEURISTIC},
            {label: "Oznaka", value: "barem 2 ugovora, zbroj ≥ prag", source: HEURISTIC},
            {label: "Pouzdanost „jaka”", value: "barem 3 ugovora, svaki ≥ 70 % praga", source: HEURISTIC},
        ],
        skipped: "Ugovori iz postupka javne nabave po ZJN-u (npr. grupe otvorenog postupka), okvirni sporazumi, ponovni upis istog ugovora u registar te redci čiji se iznosi s PDV-om i bez PDV-a međusobno ne slažu ili čiji su stupci pomaknuti. Dobavljač u vlasništvu naručitelja i ponavljajuće nabave (CPV 09, 65) prikazuju se kao informativni.",
        notMeaning: "Ugovori mogu odgovarati odvojenim, unaprijed nepredvidivim potrebama. Ovaj obrazac sam po sebi ne dokazuje namjerno izbjegavanje postupka javne nabave.",
    },
    {
        letter: "C",
        code: "C_CONCENTRATION",
        title: "Ponovljene dodjele istom dobavljaču",
        computes: "Udio dobavljača u vrijednosti ugovora jednog naručitelja u jednoj CPV skupini, u zadnje 3 godine s ugovorima.",
        formula: "HHI = Σ (100 · udio dobavljača)², od 0 do 10.000",
        thresholds: [
            {label: "Ulazi u izračun", value: "barem 5 ugovora, ukupno ≥ 50.000 €, barem 2 godine", source: HEURISTIC},
            {label: "Oznaka", value: "najveći dobavljač ≥ 70 % vrijednosti, HHI ≥ 5.000 i barem 2 njegova ugovora", source: HEURISTIC},
            {label: "Pouzdanost „jaka”", value: "barem 15 ugovora u skupini", source: HEURISTIC},
        ],
        skipped: "Ponovni upis istog ugovora, „tromjesečna evidencija ugovora” iz okvirnog sporazuma (isti novac već je u matičnom ugovoru) i redci s proturječnim iznosima ili pomaknutim stupcima. Oznaku dobivaju samo ugovori najvećeg dobavljača; ostali ugovori skupine su u dokazima. Dobavljač kojem je naručitelj osnivač (registar JPT) prikazuje se kao informativan, a CPV 09, 64 i 65 dobivaju napomenu o mogućem prirodnom monopolu.",
        notMeaning: "Na malom tržištu često postoji samo jedan ili dva izvođača. Koncentracija ne govori je li postupak bio pošten.",
    },
];
