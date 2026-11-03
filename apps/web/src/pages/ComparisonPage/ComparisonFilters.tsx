import {Select} from "@/components";
import type {MunicipalityOverview, PeerComparison} from "@/types";
import {formatNumber} from "@/utils";
import styles from "./ComparisonPage.module.css";

interface ComparisonFiltersProps {
    municipalities: MunicipalityOverview[];
    selectedOib: string | undefined;
    comparison: PeerComparison | undefined;
    onChangeMunicipality: (oib: string) => void;
    onChangeYear: (year: number) => void;
    onChangeCategory: (cpv2: string | undefined) => void;
}

export const ComparisonFilters = ({
    municipalities, selectedOib, comparison, onChangeMunicipality, onChangeYear, onChangeCategory,
}: ComparisonFiltersProps) => (
    <div className={styles.filters}>
        <label className={styles.field}>
            JLS
            <Select value={selectedOib ?? ""} onChange={(event) => event.target.value && onChangeMunicipality(event.target.value)}>
                <option value="">Odaberite JLS…</option>
                {municipalities.map((municipality) => (
                    <option key={municipality.oib} value={municipality.oib}>{municipality.name}</option>
                ))}
            </Select>
        </label>
        {comparison && (
            <>
                <label className={styles.field}>
                    Godina sklapanja
                    <Select value={comparison.year} onChange={(event) => onChangeYear(Number(event.target.value))}>
                        {!comparison.years.some((option) => option.year === comparison.year) && (
                            <option value={comparison.year}>{comparison.year}. (bez podataka)</option>
                        )}
                        {comparison.years.map((option) => (
                            <option key={option.year} value={option.year}>
                                {option.year}. ({formatNumber(option.withData)} JLS s podacima{option.incomplete ? ", nepotpuna" : ""})
                            </option>
                        ))}
                    </Select>
                </label>
                <label className={styles.field}>
                    Kategorija (CPV odjeljak)
                    <Select value={comparison.cpv2 ?? ""} onChange={(event) => onChangeCategory(event.target.value || undefined)}>
                        <option value="">Sve kategorije</option>
                        {comparison.cpv2 && !comparison.categories.some((category) => category.cpv2 === comparison.cpv2) && (
                            <option value={comparison.cpv2}>{comparison.cpv2} {comparison.cpvName} (0 JLS)</option>
                        )}
                        {comparison.categories.map((category) => (
                            <option key={category.cpv2} value={category.cpv2}>
                                {category.cpv2} {category.name} ({formatNumber(category.municipalities)} JLS)
                            </option>
                        ))}
                    </Select>
                </label>
            </>
        )}
    </div>
);
