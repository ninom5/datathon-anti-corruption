import type {RuleDescription} from "./methodologyRules.ts";
import styles from "./MethodologyPage.module.css";

interface RuleSectionProps {
    rule: RuleDescription;
}

export const RuleSection = ({rule}: RuleSectionProps) => {
    const titleId = `rule-${rule.letter}`;
    return (
        <section className={styles.rule} aria-labelledby={titleId}>
            <h3 id={titleId} className={styles.ruleTitle}>
                <span className={styles.ruleLetter} aria-hidden="true">{rule.letter}</span>
                {rule.title}
            </h3>
            <p>{rule.computes}</p>
            <p className={styles.formula}><code>{rule.formula}</code></p>
            <div className={styles.tableWrap}>
                <table className={styles.table}>
                    <caption className="visually-hidden">Pragovi pravila {rule.letter}</caption>
                    <thead>
                    <tr>
                        <th scope="col">Prag</th>
                        <th scope="col">Vrijednost</th>
                        <th scope="col">Izvor</th>
                    </tr>
                    </thead>
                    <tbody>
                    {rule.thresholds.map((threshold) => (
                        <tr key={threshold.label}>
                            <th scope="row">{threshold.label}</th>
                            <td className="tabular-nums">{threshold.value}</td>
                            <td className={styles.source}>{threshold.source}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <dl className={styles.details}>
                <div>
                    <dt>Što se preskače</dt>
                    <dd>{rule.skipped}</dd>
                </div>
                <div>
                    <dt>Što ovo NE znači</dt>
                    <dd>{rule.notMeaning}</dd>
                </div>
            </dl>
        </section>
    );
};
