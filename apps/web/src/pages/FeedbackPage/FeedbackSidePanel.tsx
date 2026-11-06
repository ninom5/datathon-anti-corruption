import {Link} from "wouter";
import {routes} from "@/routes";
import styles from "./FeedbackPage.module.css";

// What happens after sending: moderation first, then the reply next to the indicator.
export const FeedbackSidePanel = () => (
    <section className={styles.panel} aria-labelledby="feedback-steps-title">
        <h2 id="feedback-steps-title" className={styles.panelHeading}>Što se događa s prijavom</h2>
        <ol className={styles.steps}>
            <li>
                <strong>Ručna provjera</strong>
                <span>prijavu pregledava član tima, a ništa se ne objavljuje automatski.</span>
            </li>
            <li>
                <strong>Odgovor uz indikator</strong>
                <span>potvrđen odgovor naručitelja ili dobavljača prikazuje se uz oznaku, s datumom.</span>
            </li>
            <li>
                <strong>Ispravak u izvoru</strong>
                <span>greške u registru ispravlja naručitelj; mi ponovno učitamo objavljenu datoteku.</span>
            </li>
        </ol>
        <p className={styles.panelNote}>
            Zahtjev za uklanjanje osobnih podataka obrađuje se u roku od 7 dana. Kako se računaju oznake, piše u{" "}
            <Link href={routes.METHODOLOGY}>metodologiji</Link>.
        </p>
    </section>
);
