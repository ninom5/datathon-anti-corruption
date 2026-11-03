import type {ReactNode} from "react";
import {classNames} from "@/utils";
import styles from "./Card.module.css";

interface CardProps {
    children: ReactNode;
    as?: "article" | "section" | "div";
    className?: string;
    "aria-labelledby"?: string;
}

export const Card = ({children, as: Element = "div", className, "aria-labelledby": labelledBy}: CardProps) => {
    return (
        <Element className={classNames(styles.card, className)} aria-labelledby={labelledBy}>
            {children}
        </Element>
    );
};
