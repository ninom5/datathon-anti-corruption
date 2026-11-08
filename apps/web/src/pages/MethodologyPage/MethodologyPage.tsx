import {Link} from "wouter";
import {FactList, Notice, PageHeader} from "@/components";
import {routes} from "@/routes";
import {methodologyRules} from "./methodologyRules.ts";
import {RuleSection} from "./RuleSection.tsx";
import styles from "./MethodologyPage.module.css";

const SECTIONS = [
    {id: "citanje", label: "Kako čitati oznaku"},
    {id: "pravila", label: "Pravila i pragovi"},
    {id: "ai", label: "AI procjena"},
    {id: "ogranicenja", label: "Ograničenja podataka"},
    {id: "jezik", label: "Jezik i osobni podaci"},
    {id: "ispravci", label: "Ispravci i pravo na odgovor"},
];

const DIMENSIONS = [
    {label: "Kvaliteta podataka", value: "Jesu li ulazni podaci potpuni i pročitani parserom iz strukturiranog izvoza (niska, srednja, visoka)."},
    {label: "Veličina odstupanja", value: "Broj s jedinicom pravila: postotak rasta, omjer prema pragu ili HHI. Pojas (nizak, srednji, visok) je ljestvica unutar pravila."},
    {label: "Pouzdanost dokaza", value: "Koliko izravno brojevi pokazuju obrazac (slaba, srednja, jaka)."},
];

export const MethodologyPage = () => (
    <>
        <PageHeader
            title="Metodologija"
            description="Kako JavniRadar od objavljenih registara ugovora dolazi do oznake „vrijedi provjeriti” i što ta oznaka ne znači."
            meta="Pravila rules-v1 · pragovi iz Zakona o javnoj nabavi biraju se prema datumu sklapanja ugovora"
        />
        <div className={styles.layout}>
            <div className={styles.main}>
                <section id="citanje" className={styles.section} aria-labelledby="citanje-title">
                    <h2 id="citanje-title">Kako čitati oznaku</h2>
                    <p>
                        Svaka oznaka ima tri odvojene dimenzije. One se ne zbrajaju i nema ukupne ocjene, jer bi jedan
                        broj sakrio zašto je nešto označeno.
                    </p>
                    <FactList facts={DIMENSIONS} className={styles.facts}/>
                    <p>
                        <strong>Vrijedi provjeriti</strong> znači da objavljeni brojevi prelaze prag pravila.{" "}
                        <strong>Informativno</strong> znači da postoji poznato moguće objašnjenje (npr. dobavljač u
                        vlasništvu općine) ili da brojevi izgledaju kao greška pri unosu u registar.
                    </p>
                </section>

                <section id="pravila" className={styles.section} aria-labelledby="pravila-title">
                    <h2 id="pravila-title">Pravila i pragovi</h2>
                    <p>
                        Pravila su deterministička: isti podaci uvijek daju istu oznaku, bez AI-ja. Zakonski pragovi
                        su označeni člankom ZJN-a; ostale granice su heuristike JavniRadara i tako su i označene.
                    </p>
                    {methodologyRules.map((rule) => <RuleSection key={rule.code} rule={rule}/>)}
                </section>

                <section id="ai" className={styles.section} aria-labelledby="ai-title">
                    <h2 id="ai-title">AI procjena</h2>
                    <p>
                        AI procjena je zaseban blok ispod oznaka pravila, uvijek s natpisom „AI procjena – zahtijeva
                        ljudsku provjeru”. Svaki razlog mora imati poveznicu na polje u izvoru, a uz procjenu se uvijek
                        prikazuju moguća legitimna objašnjenja. Za ugovore s obrtima i fizičkim osobama ne radi se.
                    </p>
                    <Notice tone="info" title="Evaluacija još nije objavljena">
                        <p>Točnost AI procjene mjeri se na 40 sintetičkih i 60 stvarnih ugovora. Brojke objavljujemo tek kad su izmjerene.</p>
                    </Notice>
                </section>

                <section id="ogranicenja" className={styles.section} aria-labelledby="ogranicenja-title">
                    <h2 id="ogranicenja-title">Ograničenja podataka</h2>
                    <ul className={styles.list}>
                        <li>
                            Podaci postoje samo za naručitelje koji objavljuju strojno čitljiv registar ugovora.
                            JLS bez registra na <Link href={routes.MAP}>karti</Link> su šrafirane: to nije isto što i nula oznaka.
                        </li>
                        <li>Broj ponuda nije objavljen ni u jednom izvoru, pa pravilo o niskoj konkurenciji nije moguće.</li>
                        <li>Registar često ne navodi razlog izmjene ugovora. Oznaka zato govori samo o brojevima.</li>
                        <li>Iznosi u kunama prije 2023. preračunati su fiksnim tečajem 7,53450 kn = 1 €.</li>
                        <li>Nepotpuna godina (registar dohvaćen prije kraja godine) označena je zvjezdicom.</li>
                        <li>Sintetički podaci za demonstraciju nikad ne ulaze u brojke stvarnih podataka.</li>
                    </ul>
                    <p>Popis skupova podataka, datoteka i datuma dohvata je na stranici <Link href={routes.SOURCES}>Izvori</Link>.</p>
                </section>

                <section id="jezik" className={styles.section} aria-labelledby="jezik-title">
                    <h2 id="jezik-title">Jezik i osobni podaci</h2>
                    <ul className={styles.list}>
                        <li>
                            Tekstovi oznaka opisuju brojeve, a ne namjere. Koriste se izrazi „neuobičajeno” i „vrijedi
                            provjeriti”; riječi koje sugeriraju krivnju ne koriste se, što automatski provjerava test.
                        </li>
                        <li>Ugovori s fizičkim osobama ne prikazuju se. Obrti se prikazuju bez naziva, samo s OIB-om.</li>
                        <li>Nijedna osoba se ne imenuje kao odgovorna.</li>
                    </ul>
                </section>

                <section id="ispravci" className={styles.section} aria-labelledby="ispravci-title">
                    <h2 id="ispravci-title">Ispravci i pravo na odgovor</h2>
                    <p>
                        Uz svaki indikator i ugovor je poveznica „Prijavi grešku ili dodaj kontekst”. Naručitelj ili
                        dobavljač može poslati službeni odgovor, koji se nakon ručne provjere prikazuje uz oznaku.
                        Zahtjev za uklanjanje osobnih podataka obrađuje se u roku od 7 dana.
                    </p>
                </section>
            </div>
            <nav className={styles.aside} aria-label="Na ovoj stranici">
                <h2 className={styles.asideTitle}>Na ovoj stranici</h2>
                <ul className={styles.toc}>
                    {SECTIONS.map((section) => <li key={section.id}><a href={`#${section.id}`}>{section.label}</a></li>)}
                </ul>
            </nav>
        </div>
    </>
);
