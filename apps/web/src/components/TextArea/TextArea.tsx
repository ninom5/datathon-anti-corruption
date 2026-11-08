import type {Ref, TextareaHTMLAttributes} from "react";
import {classNames} from "@/utils";
import styles from "./TextArea.module.css";

interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
    ref?: Ref<HTMLTextAreaElement>;
}

// Same look as TextInput, for longer free text (correction form).
export const TextArea = ({className, rows = 6, ...props}: TextAreaProps) => {
    return <textarea rows={rows} className={classNames(styles.input, className)} {...props}/>;
};
