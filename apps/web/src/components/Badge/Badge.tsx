import type {ReactNode} from "react";
import {classNames} from "@/utils";
import {Icon, type IconName} from "@/components/Icon";
import styles from "./Badge.module.css";

export type BadgeTone = "neutral" | "primary" | "success" | "warning" | "danger" | "info" | "ai";

interface BadgeProps {
    children: ReactNode;
    tone?: BadgeTone;
    icon?: IconName;
    title?: string;
    className?: string;
}

export const Badge = ({children, tone = "neutral", icon, title, className}: BadgeProps) => {
    return (
        <span className={classNames(styles.badge, styles[tone], className)} title={title}>
            {icon && <Icon name={icon} size={12} className={styles.icon}/>}
            {children}
        </span>
    );
};
