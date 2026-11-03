package com.datathon.api.municipality;

import java.util.Map;

/**
 * Croatian names of CPV 2008 divisions (first two digits, Regulation (EC) No 213/2008), shortened where the official
 * title is very long. The registers only publish the full CPV code, so the comparison filter needs these names.
 */
public final class CpvDivisions {

    private static final Map<String, String> NAMES = Map.ofEntries(
            Map.entry("03", "Poljoprivredni, ribarski i šumarski proizvodi"),
            Map.entry("09", "Gorivo, električna energija i drugi izvori energije"),
            Map.entry("14", "Proizvodi rudarstva i osnovni metali"),
            Map.entry("15", "Hrana, piće i srodni proizvodi"),
            Map.entry("16", "Poljoprivredni strojevi"),
            Map.entry("18", "Odjeća, obuća i pribor"),
            Map.entry("19", "Koža, tekstil, plastika i guma"),
            Map.entry("22", "Tiskani materijali"),
            Map.entry("24", "Kemijski proizvodi"),
            Map.entry("30", "Uredska i računalna oprema"),
            Map.entry("31", "Električni strojevi i rasvjeta"),
            Map.entry("32", "Komunikacijska i telekomunikacijska oprema"),
            Map.entry("33", "Medicinska oprema i farmaceutski proizvodi"),
            Map.entry("34", "Prijevozna oprema"),
            Map.entry("35", "Sigurnosna, vatrogasna i zaštitna oprema"),
            Map.entry("37", "Sportska roba, igre i glazbeni instrumenti"),
            Map.entry("38", "Laboratorijska i precizna oprema"),
            Map.entry("39", "Namještaj, kućanski uređaji i sredstva za čišćenje"),
            Map.entry("41", "Voda"),
            Map.entry("42", "Industrijski strojevi"),
            Map.entry("43", "Građevinski strojevi i strojevi za rudarstvo"),
            Map.entry("44", "Građevinski materijali i konstrukcije"),
            Map.entry("45", "Građevinski radovi"),
            Map.entry("48", "Programski paketi i informacijski sustavi"),
            Map.entry("50", "Popravci i održavanje"),
            Map.entry("51", "Usluge instalacije"),
            Map.entry("55", "Ugostiteljske i hotelske usluge"),
            Map.entry("60", "Usluge prijevoza"),
            Map.entry("63", "Pomoćne usluge u prijevozu i putničke agencije"),
            Map.entry("64", "Poštanske i telekomunikacijske usluge"),
            Map.entry("65", "Javne komunalne usluge"),
            Map.entry("66", "Financijske usluge i osiguranje"),
            Map.entry("70", "Usluge u vezi s nekretninama"),
            Map.entry("71", "Arhitektonske, inženjerske i inspekcijske usluge"),
            Map.entry("72", "Usluge informacijske tehnologije"),
            Map.entry("73", "Istraživanje i razvoj"),
            Map.entry("75", "Usluge javne uprave"),
            Map.entry("76", "Usluge za naftnu i plinsku industriju"),
            Map.entry("77", "Poljoprivredne, šumarske i hortikulturne usluge"),
            Map.entry("79", "Poslovne usluge (pravo, marketing, savjetovanje, tisak, zaštita)"),
            Map.entry("80", "Obrazovanje i osposobljavanje"),
            Map.entry("85", "Zdravstvene usluge i socijalna skrb"),
            Map.entry("90", "Otpad, otpadne vode, čišćenje i okoliš"),
            Map.entry("92", "Rekreacija, kultura i sport"),
            Map.entry("98", "Ostale javne, društvene i osobne usluge"));

    private CpvDivisions() {
    }

    public static String nameOf(String cpv2) {
        return NAMES.getOrDefault(cpv2, "CPV odjeljak " + cpv2);
    }
}
