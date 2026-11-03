import {Link} from "wouter";
import {routes} from "@/routes";
import styles from "./HomePage.module.css";

const BROWSE_LINKS = [
    {path: routes.MAP, label: "Karta", description: "koje JLS objavljuju registar ugovora"},
    {path: routes.COMPARISON, label: "Usporedba", description: "potrošnja po stanovniku među sličnim JLS"},
    {path: routes.SOURCES, label: "Izvori i metodologija", description: "odakle dolaze podaci i kako se računaju oznake"},
];

// What the product does in three steps, and the other ways in.
export const HowItWorks = () => (
    <section className={styles.panel} aria-labelledby="how-it-works-title">
        <h2 id="how-it-works-title" className={styles.panelHeading}>Kako radi</h2>
        <ol className={styles.steps}>
            <li>
                <strong>Čitamo registre ugovora</strong>
                <span>koje općine, gradovi i županije objavljuju na data.gov.hr.</span>
            </li>
            <li>
                <strong>Pravila označe neuobičajeno</strong>
                <span>izmjene iznad praga, cjepkanje nabave, koncentraciju dobavljača.</span>
            </li>
            <li>
                <strong>Provjeri u izvoru</strong>
                <span>svaka oznaka vodi do datoteke i retka iz kojeg je pročitana.</span>
            </li>
        </ol>
        <h3 className={styles.subheading}>Pregledaj dalje</h3>
        <ul className={styles.links}>
            {BROWSE_LINKS.map((browseLink) => (
                <li key={browseLink.path}>
                    <Link href={browseLink.path} className={styles.link}>{browseLink.label}</Link>
                    <span className={styles.detail}>{browseLink.description}</span>
                </li>
            ))}
        </ul>
    </section>
);
