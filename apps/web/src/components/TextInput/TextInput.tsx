import type {InputHTMLAttributes, Ref} from "react";
import {classNames} from "@/utils";
import styles from "./TextInput.module.css";

interface TextInputProps extends InputHTMLAttributes<HTMLInputElement> {
    ref?: Ref<HTMLInputElement>;
}

export const TextInput = ({className, type = "text", ...props}: TextInputProps) => {
    return <input type={type} className={classNames(styles.input, className)} {...props}/>;
};
