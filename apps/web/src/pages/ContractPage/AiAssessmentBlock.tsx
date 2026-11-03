import {ExternalLink} from "@/components";
import type {AiAssessment, AiReason, ContractDetail} from "@/types";
import {aiConfidenceLabels, aiFieldLabel, aiLabelLabels, classNames, formatDate} from "@/utils";
import styles from "./AiAssessmentBlock.module.css";

const TITLE = "AI procjena – zahtijeva ljudsku provjeru";

/** "…xlsx#sheet=1;row=41" → "registar ugovora, redak 41"; EOJN links by name; anything else as "izvor". */
const sourceLabel = (url: string, fileUrl: string | null) => {
    if (fileUrl && url.startsWith(fileUrl)) {
        const row = /row=(\d+)/.exec(url);
        return row ? `registar ugovora, redak ${row[1]}` : "registar ugovora";
    }
    if (url.includes("eojn.hr")) {
        return "EOJN";
    }
    return "izvor";
};

const ReasonItem = ({reason, fileUrl}: {reason: AiReason; fileUrl: string | null}) => (
    <li className={styles.reason}>
        <p>{reason.claim}</p>
        <p className={styles.reasonMeta}>
            <span>Polja: {reason.fields.map(aiFieldLabel).join(", ")}</span>
            <span>
                Izvori:{" "}
                {reason.sources.map((url, index) => (
                    <span key={url}>
                        {index > 0 && ", "}
                        <ExternalLink href={url}>{sourceLabel(url, fileUrl)}</ExternalLink>
                    </span>
                ))}
            </span>
        </p>
    </li>
);

const AssessmentBody = ({assessment, fileUrl}: {assessment: AiAssessment; fileUrl: string | null}) => {
    const isFlagged = assessment.label !== "nema_oznake";
    return (
        <>
            <p className={styles.verdict}>
                <span className={classNames(styles.label, isFlagged ? styles.labelSignal : styles.labelNeutral)}>
                    {aiLabelLabels[assessment.label]}
                </span>
                <span className={styles.muted}>pouzdanost procjene: {aiConfidenceLabels[assessment.confidence]}</span>
            </p>
            {!isFlagged && (
                <p className={styles.explanation}>
                    AI nije našao ništa neuobičajeno osim onoga što već pokazuju oznake pravila iznad.
                </p>
            )}
            {assessment.reasons.length > 0 && (
                <>
                    <h3>Razlozi</h3>
                    <ul className={styles.reasons}>
                        {assessment.reasons.map((reason) => <ReasonItem key={reason.claim} reason={reason} fileUrl={fileUrl}/>)}
                    </ul>
                </>
            )}
            <h3>Moguća legitimna objašnjenja</h3>
            <ul className={styles.list}>
                {assessment.legitExplanations.map((text) => <li key={text}>{text}</li>)}
            </ul>
            {assessment.missingData.length > 0 && (
                <>
                    <h3>Što nedostaje za pouzdaniji zaključak</h3>
                    <ul className={styles.list}>
                        {assessment.missingData.map((text) => <li key={text}>{text}</li>)}
                    </ul>
                </>
            )}
            <p className={styles.note}>
                Jezični model je pročitao samo podatke s ove stranice i rezultate pravila. Procjena ne utvrđuje
                nepravilnost, nego pomaže odlučiti što vrijedi ručno provjeriti u izvornim dokumentima. Razlozi koji ne
                citiraju polja i izvore iz podataka automatski se odbacuju.
            </p>
        </>
    );
};

interface AiAssessmentBlockProps {
    contract: ContractDetail;
}

/** Separate from the rule indicators and never merged with them (AGENTS.md, docs/UI_UX.md §3.4). */
export const AiAssessmentBlock = ({contract}: AiAssessmentBlockProps) => {
    const assessment = contract.aiAssessment;
    return (
        <section className={styles.block} aria-labelledby="ai-title">
            <header className={styles.header}>
                <h2 id="ai-title">{TITLE}</h2>
                {assessment && (
                    <p className={styles.muted}>
                        {assessment.model} · {assessment.promptVersion} ·{" "}
                        <span className="tabular-nums">{formatDate(assessment.createdAt)}</span>
                    </p>
                )}
            </header>
            {assessment
                ? <AssessmentBody assessment={assessment} fileUrl={contract.provenance.fileUrl}/>
                : <p className={styles.muted}>{contract.aiAssessmentUnavailableReason ?? "AI procjena nije dostupna."}</p>}
        </section>
    );
};
