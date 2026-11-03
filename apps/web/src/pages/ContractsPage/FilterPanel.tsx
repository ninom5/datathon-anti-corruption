import {type FormEvent, useState} from "react";
import {Button, Select, TextInput} from "@/components";
import type {BuyerOption, ContractSummary} from "@/types";
import {buyerTypeLabels, formatNumber, magnitudeBandOptions, readMagnitudeBand, ruleOptions} from "@/utils";
import {type ExplorerState, type IndicatorChoice, isOib, type SortChoice} from "./explorerState.ts";
import styles from "./ContractsPage.module.css";

interface FilterPanelProps {
    state: ExplorerState;
    buyers: BuyerOption[];
    ruleCounts?: ContractSummary["contractsByRule"];
    years: {year: number; incomplete: boolean}[];
    onChange: (next: Partial<ExplorerState>) => void;
    onReset: () => void;
}

const indicatorOptions: {value: IndicatorChoice; label: string}[] = [
    {value: "all", label: "Svi ugovori"},
    {value: "any", label: "S bilo kojom oznakom"},
    {value: "worth", label: "Vrijedi provjeriti"},
    {value: "error", label: "Moguća greška u unosu"},
    {value: "none", label: "Bez oznake"},
];

const buyerLabel = (buyer: BuyerOption) => {
    const type = buyer.type ? buyerTypeLabels[buyer.type] : undefined;
    return type && buyer.type !== "jls" ? `${buyer.name} (${type})` : buyer.name;
};

export const FilterPanel = ({state, buyers, years, ruleCounts, onChange, onReset}: FilterPanelProps) => {
    const [supplierInput, setSupplierInput] = useState(state.supplierOib ?? "");
    const isSupplierInvalid = supplierInput !== "" && !isOib(supplierInput);

    const handleSupplierSubmit = (event: FormEvent) => {
        event.preventDefault();
        if (isSupplierInvalid) {
            return;
        }
        onChange({supplierOib: supplierInput || undefined});
    };

    const handleReset = () => {
        setSupplierInput("");
        onReset();
    };

    return (
        <aside className={styles.filters} aria-label="Filtri">
            <h2 className={styles.filtersTitle}>Filtri</h2>

            <label className={styles.field}>
                <span>Naručitelj</span>
                <Select value={state.buyerOib ?? ""} onChange={(event) => onChange({buyerOib: event.target.value || undefined})}>
                    <option value="">Svi naručitelji</option>
                    {buyers.map((buyer) => (
                        <option key={buyer.id} value={buyer.oib ?? ""} disabled={!buyer.oib}>
                            {buyerLabel(buyer)}
                        </option>
                    ))}
                </Select>
            </label>

            <label className={styles.field}>
                <span>Godina sklapanja</span>
                <Select value={state.year ?? ""} onChange={(event) => onChange({year: event.target.value ? Number(event.target.value) : undefined})}>
                    <option value="">Sve godine</option>
                    {years.map(({year, incomplete}) => (
                        <option key={year} value={year}>{incomplete ? `${year}. (nepotpuna)` : `${year}.`}</option>
                    ))}
                </Select>
            </label>

            <form className={styles.field} onSubmit={handleSupplierSubmit}>
                <label htmlFor="supplier-oib">OIB dobavljača</label>
                <div className={styles.inline}>
                    <TextInput
                        id="supplier-oib"
                        name="supplierOib"
                        autoComplete="off"
                        spellCheck={false}
                        inputMode="numeric"
                        maxLength={11}
                        placeholder="npr. 12345678901"
                        value={supplierInput}
                        aria-invalid={isSupplierInvalid}
                        aria-describedby={isSupplierInvalid ? "supplier-oib-error" : undefined}
                        onChange={(event) => setSupplierInput(event.target.value.trim())}
                    />
                    <Button type="submit" variant="secondary">Primijeni</Button>
                </div>
                {isSupplierInvalid && <span id="supplier-oib-error" className={styles.fieldError}>OIB ima 11 znamenki.</span>}
            </form>

            <fieldset className={styles.field}>
                <legend>Oznake pravila</legend>
                {indicatorOptions.map((option) => (
                    <label key={option.value} className={styles.radio}>
                        <input
                            type="radio"
                            name="indicator"
                            value={option.value}
                            checked={state.indicator === option.value}
                            onChange={() => onChange({indicator: option.value})}
                        />
                        {option.label}
                    </label>
                ))}
            </fieldset>

            <fieldset className={styles.field}>
                <legend>Vrsta indikatora</legend>
                {ruleOptions.map((option) => (
                    <label key={option.code} className={styles.radio}>
                        <input type="checkbox" checked={state.ruleCode.includes(option.code)}
                               onChange={(event) => onChange({ruleCode: event.target.checked
                                   ? [...state.ruleCode, option.code]
                                   : state.ruleCode.filter((code) => code !== option.code)})}/>
                        <span>{option.short} · {option.label}</span>
                        <span className={styles.ruleCount}>{ruleCounts ? formatNumber(ruleCounts[option.code] ?? 0) : "—"}</span>
                    </label>
                ))}
                <p className={styles.footnote}>Broj stvarnih ugovora uz ostale filtre. Jedan ugovor može imati više vrsta.</p>
            </fieldset>

            <label className={styles.field}>
                <span>Pojas</span>
                <Select value={state.band ?? ""} aria-describedby="band-hint"
                        onChange={(event) => onChange({band: readMagnitudeBand(event.target.value)})}>
                    <option value="">Svi pojasevi</option>
                    {magnitudeBandOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                </Select>
                <span id="band-hint" className={styles.footnote}>Pojas veličine odstupanja je heuristika unutar pravila, a ne ukupna ocjena.</span>
            </label>

            <label className={styles.field}>
                <span>Poredak</span>
                <Select value={state.sort} onChange={(event) => onChange({sort: event.target.value as SortChoice})}>
                    <option value="dateSigned">Najnoviji prvo</option>
                    <option value="value">Najveći iznos prvo</option>
                </Select>
            </label>

            <label className={`${styles.radio} ${styles.syntheticToggle}`}>
                <input type="checkbox" checked={state.synthetic} onChange={(event) => onChange({synthetic: event.target.checked})}/>
                Prikaži sintetičke
            </label>

            <Button variant="ghost" size="sm" className={styles.resetButton} onClick={handleReset}>Poništi filtre</Button>
        </aside>
    );
};
