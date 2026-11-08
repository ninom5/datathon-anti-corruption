import {Link} from "wouter";
import {feedbackPath} from "@/routes";
import type {ContractDetail} from "@/types";
import {formatDateTime, formatNumber} from "@/utils";
import styles from "./ContractPage.module.css";

interface ProvenanceBlockProps {
    contract: ContractDetail;
}

/** Every number on the page can be checked against this exact file and row (AGENTS.md: provenance). */
export const ProvenanceBlock = ({contract}: ProvenanceBlockProps) => {
    const {provenance} = contract;
    return (
        <section className={styles.block} aria-labelledby="source-title">
            <h2 id="source-title">Izvor</h2>
            <dl className={styles.facts}>
                <dt>Skup podataka</dt>
                <dd>
                    {provenance.datasetUrl
                        ? <a href={provenance.datasetUrl} target="_blank" rel="noreferrer">{provenance.datasetTitle ?? "skup podataka"}</a>
                        : provenance.datasetTitle ?? "nije poznato"}
                </dd>
                <dt>Izdavatelj</dt>
                <dd>{provenance.publisher ?? "nije naveden"}</dd>
                <dt>Datoteka i redak</dt>
                <dd>
                    {provenance.fileUrl
                        ? <a href={provenance.fileUrl} target="_blank" rel="noreferrer">{provenance.fileName ?? "preuzmi datoteku"}</a>
                        : "nije poznato"}
                    {provenance.locatorLabel && <strong className={styles.locator}> · {provenance.locatorLabel}</strong>}
                </dd>
                <dt>Dohvaćeno</dt>
                <dd className="tabular-nums">{formatDateTime(provenance.retrievedAt)}</dd>
                <dt>Metoda</dt>
                <dd>
                    {provenance.method === "parser" ? "parser EOJN izvoza" : provenance.method} ({provenance.extractorVersion}),
                    pouzdanost {formatNumber(provenance.confidence)}
                </dd>
                {provenance.licenseId && (
                    <>
                        <dt>Licenca</dt>
                        <dd>{provenance.licenseId}</dd>
                    </>
                )}
                {contract.eojnContractUrl && (
                    <>
                        <dt>EOJN</dt>
                        <dd>
                            {contract.synthetic
                                ? <span className={styles.muted}>Sintetički ugovor, nema stvarnog zapisa u EOJN-u.</span>
                                : <a href={contract.eojnContractUrl} target="_blank" rel="noreferrer">Ugovor u EOJN-u</a>}
                        </dd>
                    </>
                )}
            </dl>
            <p className={styles.feedbackLink}>
                <Link href={feedbackPath("ugovor", contract.id)}>Prijavi grešku ili dodaj kontekst</Link>
            </p>
        </section>
    );
};
