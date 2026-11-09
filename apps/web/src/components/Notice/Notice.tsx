import type {ReactNode} from "react";
import {classNames} from "@/utils";
import {Icon, type IconName} from "@/components/Icon";
import styles from "./Notice.module.css";

export type NoticeTone = "info" | "success" | "warning" | "danger";

const TONE_ICONS: Record<NoticeTone, IconName> = {
    info: "info",
    success: "check",
    warning: "alert",
    danger: "alert",
};

interface NoticeProps {
    children: ReactNode;
    tone?: NoticeTone;
    title?: string;
    className?: string;
}

export const Notice = ({children, tone = "info", title, className}: NoticeProps) => {
    return (
        <div className={classNames(styles.notice, styles[tone], className)} role="note">
            <Icon name={TONE_ICONS[tone]} className={styles.icon}/>
            <div className={styles.body}>
                {title && <p className={styles.title}>{title}</p>}
                {children}
            </div>
        </div>
    );
};
