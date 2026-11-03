import type {ReactNode} from "react";
import {Link} from "wouter";
import {Icon} from "@/components/Icon";
import styles from "./BackLink.module.css";

interface BackLinkProps {
    href: string;
    children: ReactNode;
}

export const BackLink = ({href, children}: BackLinkProps) => {
    return (
        <Link href={href} className={styles.link}>
            <Icon name="arrowLeft" size={15}/>
            {children}
        </Link>
    );
};
