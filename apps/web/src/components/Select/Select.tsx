import type {SelectHTMLAttributes} from "react";
import {classNames} from "@/utils";
import styles from "./Select.module.css";

type SelectProps = SelectHTMLAttributes<HTMLSelectElement>;

export const Select = ({className, ...props}: SelectProps) => {
    return <select className={classNames(styles.select, className)} {...props}/>;
};
