import type {ReactNode} from "react";
import {classNames} from "@/utils";
import {Icon} from "@/components/Icon";
import styles from "./ChoiceChip.module.css";

interface ChoiceChipProps {
    type: "checkbox" | "radio";
    name: string;
    value: string;
    isChecked: boolean;
    onChange: () => void;
    children: ReactNode;
    count?: number;
}

export const ChoiceChip = ({type, name, value, isChecked, onChange, children, count}: ChoiceChipProps) => {
    return (
        <label className={classNames(styles.chip, isChecked && styles.chipSelected)}>
            <input
                type={type}
                name={name}
                value={value}
                checked={isChecked}
                onChange={onChange}
                className="visually-hidden"
            />
            {isChecked && <Icon name="check" size={14}/>}
            {children}
            {count !== undefined && <span className={classNames(styles.count, "tabular-nums")}>{count}</span>}
        </label>
    );
};
