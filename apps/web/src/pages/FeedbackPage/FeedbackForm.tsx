import {type FormEvent, useRef, useState} from "react";
import {Button, ChoiceChip, Notice, Select, TextArea, TextInput} from "@/components";
import type {FeedbackRequest, FeedbackTargetType} from "@/types";
import {formatNumber} from "@/utils";
import {emptyDraft, type FeedbackDraft, toRequest, validateDraft} from "./feedbackForm.ts";
import {kindOptions, kindsNeedingContact, roleOptions, TEXT_MAX_LENGTH} from "./feedbackOptions.ts";
import styles from "./FeedbackPage.module.css";

interface FeedbackFormProps {
    targetType: FeedbackTargetType;
    targetId: number;
    isSubmitting: boolean;
    submitError: string | undefined;
    onSubmit: (request: FeedbackRequest) => void;
}

export const FeedbackForm = ({targetType, targetId, isSubmitting, submitError, onSubmit}: FeedbackFormProps) => {
    const [draft, setDraft] = useState<FeedbackDraft>(emptyDraft);
    const [isValidationShown, setIsValidationShown] = useState(false);
    const textRef = useRef<HTMLTextAreaElement>(null);
    const emailRef = useRef<HTMLInputElement>(null);
    const errors = isValidationShown ? validateDraft(draft) : {};
    const isContactRequired = kindsNeedingContact.includes(draft.kind);
    const selectedKind = kindOptions.find((option) => option.value === draft.kind);

    const update = (patch: Partial<FeedbackDraft>) => setDraft((previous) => ({...previous, ...patch}));

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        const found = validateDraft(draft);
        setIsValidationShown(true);
        if (found.text) {
            textRef.current?.focus();
            return;
        }
        if (found.contactEmail) {
            emailRef.current?.focus();
            return;
        }
        onSubmit(toRequest(draft, targetType, targetId));
    };

    return (
        <form className={styles.form} onSubmit={handleSubmit} noValidate>
            <fieldset className={styles.fieldset}>
                <legend className={styles.legend}>Što želite poslati?</legend>
                <div className={styles.chips}>
                    {kindOptions.map((option) => (
                        <ChoiceChip key={option.value} type="radio" name="kind" value={option.value}
                                    isChecked={draft.kind === option.value} onChange={() => update({kind: option.value})}>
                            {option.label}
                        </ChoiceChip>
                    ))}
                </div>
                {selectedKind && <p className={styles.hint}>{selectedKind.hint}</p>}
            </fieldset>

            <div className={styles.field}>
                <label htmlFor="feedback-text" className={styles.label}>Opis</label>
                <TextArea id="feedback-text" name="text" ref={textRef} value={draft.text} maxLength={TEXT_MAX_LENGTH}
                          placeholder="npr. Izmjena ugovora predviđena je člankom 5. ugovora; dokument je na…"
                          aria-invalid={errors.text ? true : undefined}
                          aria-describedby={errors.text ? "feedback-text-error" : "feedback-text-hint"}
                          onChange={(event) => update({text: event.target.value})}/>
                {errors.text
                    ? <p id="feedback-text-error" className={styles.error}>{errors.text}</p>
                    : <p id="feedback-text-hint" className={styles.hint}>
                        Navedite što je netočno ili što nedostaje i, ako možete, poveznicu na dokument.
                        <span className="tabular-nums"> {formatNumber(draft.text.length)} / 4.000</span>
                    </p>}
            </div>

            <div className={styles.fieldRow}>
                <div className={styles.field}>
                    <label htmlFor="feedback-role" className={styles.label}>Tko piše <span className={styles.optional}>(neobavezno)</span></label>
                    <Select id="feedback-role" name="role" value={draft.role}
                            onChange={(event) => update({role: roleOptions.find((option) => option.value === event.target.value)?.value ?? ""})}>
                        <option value="">Ne želim navesti</option>
                        {roleOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </Select>
                </div>
                <div className={styles.field}>
                    <label htmlFor="feedback-email" className={styles.label}>
                        E-mail za odgovor {!isContactRequired && <span className={styles.optional}>(neobavezno)</span>}
                    </label>
                    <TextInput id="feedback-email" name="contactEmail" type="email" autoComplete="email" ref={emailRef}
                               spellCheck={false} placeholder="npr. ime@primjer.hr" value={draft.contactEmail}
                               aria-invalid={errors.contactEmail ? true : undefined}
                               aria-describedby={errors.contactEmail ? "feedback-email-error" : "feedback-email-hint"}
                               onChange={(event) => update({contactEmail: event.target.value})}/>
                    {errors.contactEmail
                        ? <p id="feedback-email-error" className={styles.error}>{errors.contactEmail}</p>
                        : <p id="feedback-email-hint" className={styles.hint}>Ne prikazuje se javno i briše se nakon 12 mjeseci.</p>}
                </div>
            </div>

            {submitError && <Notice tone="danger" title="Prijava nije poslana">{submitError}</Notice>}

            <div className={styles.actions}>
                <Button type="submit" disabled={isSubmitting}>{isSubmitting ? "Šaljem…" : "Pošalji"}</Button>
            </div>
        </form>
    );
};
