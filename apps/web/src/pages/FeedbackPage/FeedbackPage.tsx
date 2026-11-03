import {useSearchParams} from "wouter";
import {isClientError} from "@/api";
import {BackLink, ButtonLink, EmptyState, ErrorState, LoadingState, Notice, PageHeader} from "@/components";
import {useContract, useIndicator, useSubmitFeedback} from "@/hooks";
import {contractPath, indicatorPath, routes} from "@/routes";
import {FeedbackForm} from "./FeedbackForm.tsx";
import {FeedbackSidePanel} from "./FeedbackSidePanel.tsx";
import {FeedbackTarget} from "./FeedbackTarget.tsx";
import {readFeedbackTarget} from "./feedbackTarget.ts";
import styles from "./FeedbackPage.module.css";

const NO_TARGET = 0;

export const FeedbackPage = () => {
    const [searchParams] = useSearchParams();
    const target = readFeedbackTarget(searchParams);
    const indicator = useIndicator(target?.targetType === "indicator" ? target.targetId : NO_TARGET);
    const contract = useContract(target?.targetType === "contract" ? target.targetId : NO_TARGET);
    const submit = useSubmitFeedback();

    if (!target) {
        return (
            <EmptyState
                icon="document"
                title="Nije odabrano na što se ispravak odnosi"
                hint="Obrazac se otvara s poveznice „Prijavi grešku ili dodaj kontekst” uz indikator ili ugovor."
                actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}
            />
        );
    }

    const targetQuery = target.targetType === "indicator" ? indicator : contract;
    const backHref = target.targetType === "indicator" ? indicatorPath(target.targetId) : contractPath(target.targetId);
    const backLabel = target.targetType === "indicator" ? "Natrag na indikator" : "Natrag na ugovor";

    const renderBody = () => {
        if (isClientError(targetQuery.error)) {
            return (
                <EmptyState
                    icon="document"
                    title="Zapis ne postoji ili nije javno prikazan"
                    hint="Ispravak se može poslati samo za indikator ili ugovor koji je prikazan u JavniRadaru."
                    actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}
                />
            );
        }
        if (targetQuery.error) {
            return <ErrorState message={targetQuery.error.message} onRetry={() => targetQuery.refetch()}/>;
        }
        if (targetQuery.isPending) {
            return <LoadingState message="Učitavanje zapisa…"/>;
        }
        if (submit.data) {
            return (
                <div aria-live="polite">
                    <Notice tone="success" title={`Prijava br. ${submit.data.id} je zaprimljena`}>
                        <p>Pregledat ćemo je ručno. Ništa se ne objavljuje prije provjere.</p>
                    </Notice>
                    <div className={styles.actions}>
                        <ButtonLink href={backHref} variant="secondary">{backLabel}</ButtonLink>
                    </div>
                </div>
            );
        }
        return (
            <FeedbackForm
                targetType={target.targetType}
                targetId={target.targetId}
                isSubmitting={submit.isPending}
                submitError={submit.error?.message}
                onSubmit={(request) => submit.mutate(request)}
            />
        );
    };

    return (
        <>
            <BackLink href={backHref}>{backLabel}</BackLink>
            <PageHeader
                title="Ispravak ili odgovor"
                description="Pošaljite ispravak podatka, dodatni kontekst ili službeni odgovor na oznaku."
                meta={<FeedbackTarget targetType={target.targetType} indicator={indicator.data} contract={contract.data}/>}
            />
            <div className={styles.layout}>
                <section className={styles.main} aria-label="Obrazac za ispravak">{renderBody()}</section>
                <aside className={styles.aside}>
                    <FeedbackSidePanel/>
                </aside>
            </div>
        </>
    );
};
