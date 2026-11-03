import {Link} from "wouter";
import {buyerPath, comparisonPath, contractsOfBuyerPath} from "@/routes";
import type {MunicipalityOverview} from "@/types";
import {formatNumber, plural} from "@/utils";
import {FEW_CONTRACTS} from "./mapClasses.ts";
import styles from "./MapPage.module.css";

const KIND_LABELS: Record<MunicipalityOverview["kind"], string> = {
    grad: "Grad",
    opcina: "Općina",
    grad_zagreb: "Grad Zagreb",
};

const per100Format = new Intl.NumberFormat("hr-HR", {maximumFractionDigits: 1});

const yearsLabel = (municipality: MunicipalityOverview) => {
    if (!municipality.firstYear || !municipality.lastYear) {
        return "";
    }
    return municipality.firstYear === municipality.lastYear
        ? `${municipality.firstYear}.`
        : `${municipality.firstYear}.–${municipality.lastYear}.`;
};

interface JlsPanelProps {
    municipality: MunicipalityOverview | undefined;
    isWithoutBoundary: boolean;
}

export const JlsPanel = ({municipality, isWithoutBoundary}: JlsPanelProps) => {
    if (!municipality) {
        return (
            <aside className={styles.panel} aria-live="polite">
                <p className={styles.panelHint}>Odaberite JLS na karti ili u popisu da vidite sažetak.</p>
            </aside>
        );
    }
    const hasData = municipality.contracts > 0;
    return (
        <aside className={styles.panel} aria-live="polite" aria-labelledby="jls-panel-title">
            <p className={styles.panelKind}>{KIND_LABELS[municipality.kind]} · {municipality.county}</p>
            <h2 id="jls-panel-title" className={styles.panelTitle}>{municipality.name}</h2>
            <p className={styles.panelMeta}>
                {municipality.population2021 === null
                    ? "Broj stanovnika nije objavljen"
                    : `${formatNumber(municipality.population2021)} stanovnika (DZS, Popis 2021.)`}
            </p>
            {isWithoutBoundary && <p className={styles.panelNote}>Granica ove JLS nije dostupna u izvoru granica, pa nije na karti.</p>}
            {hasData ? (
                <>
                    <dl className={styles.panelStats}>
                        <div>
                            <dt>Ugovori u registru</dt>
                            <dd>{formatNumber(municipality.contracts)} <span className={styles.muted}>({yearsLabel(municipality)})</span></dd>
                        </div>
                        <div>
                            <dt>Vrijedi provjeriti</dt>
                            <dd>{formatNumber(municipality.worthChecking)}</dd>
                        </div>
                        <div>
                            <dt>Na 100 ugovora</dt>
                            <dd>{municipality.worthCheckingPer100 === null ? "–" : per100Format.format(municipality.worthCheckingPer100)}</dd>
                        </div>
                        <div>
                            <dt>Informativno (moguće greške u unosu)</dt>
                            <dd>{formatNumber(municipality.informative)}</dd>
                        </div>
                    </dl>
                    {municipality.contracts < FEW_CONTRACTS && (
                        <p className={styles.panelNote}>
                            Samo {formatNumber(municipality.contracts)} {plural(municipality.contracts, "ugovor", "ugovora", "ugovora")}:
                            omjer na 100 ugovora je nestabilan i ne treba ga uspoređivati.
                        </p>
                    )}
                    <p className={styles.panelNote}>
                        Indikator nije optužba: pokazuje obrazac koji vrijedi provjeriti u izvornom dokumentu.
                    </p>
                    <div className={styles.panelLinks}>
                        <Link href={buyerPath(municipality.oib)}>Profil JLS</Link>
                        <Link href={contractsOfBuyerPath(municipality.oib)}>Ugovori i indikatori</Link>
                        <Link href={comparisonPath(municipality.oib)}>Usporedi sa sličnim JLS</Link>
                    </div>
                </>
            ) : (
                <p className={styles.panelNote}>
                    Ova JLS ne objavljuje strojno čitljiv registar ugovora na data.gov.hr (ili ga nismo mogli pročitati).
                    To je i samo po sebi podatak o transparentnosti.
                </p>
            )}
        </aside>
    );
};
