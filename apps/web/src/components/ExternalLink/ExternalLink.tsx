import type {ReactNode} from "react";
import {classNames} from "@/utils";
import {Icon} from "@/components/Icon";
import styles from "./ExternalLink.module.css";

interface ExternalLinkProps {
    href: string;
    children: ReactNode;
    className?: string;
}

export const ExternalLink = ({href, children, className}: ExternalLinkProps) => {
    return (
        <a href={href} target="_blank" rel="noopener noreferrer" className={classNames(styles.link, className)}>
            {children}
            <Icon name="externalLink" size={13} className={styles.icon}/>
            <span className="visually-hidden">(otvara se u novoj kartici)</span>
        </a>
    );
};
