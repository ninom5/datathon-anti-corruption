import type {ReactNode} from "react";
import {Link, useRoute} from "wouter";
import {routes} from "@/routes";
import styles from "./AppLayout.module.css";

interface NavLinkProps {
    href: string;
    children: ReactNode;
}

const NavLink = ({href, children}: NavLinkProps) => {
    const [isActive] = useRoute(href);
    return (
        <Link href={href} className={styles.navLink} aria-current={isActive ? "page" : undefined}>
            {children}
        </Link>
    );
};

interface AppLayoutProps {
    children: ReactNode;
}

export const AppLayout = ({children}: AppLayoutProps) => (
    <div className={styles.shell}>
        <a href="#sadrzaj" className={styles.skipLink}>Preskoči na sadržaj</a>
        <header className={styles.header}>
            <div className={styles.headerInner}>
                <Link href={routes.HOME} className={styles.brand}>JavniRadar</Link>
                <nav aria-label="Glavna navigacija" className={styles.nav}>
                    <NavLink href={routes.CONTRACTS}>Istraži ugovore</NavLink>
                    <NavLink href={routes.MAP}>Karta</NavLink>
                    <NavLink href={routes.COMPARISON}>Usporedba</NavLink>
                    <NavLink href={routes.SOURCES}>Izvori</NavLink>
                </nav>
            </div>
        </header>
        <main id="sadrzaj" className={styles.main}>{children}</main>
        <footer className={styles.footer}>
            <p>
                Indikatori nisu optužbe. Pokazuju što vrijedi provjeriti u objavljenim podacima, uz poveznicu na
                izvor. Podaci: registri ugovora naručitelja s data.gov.hr.
            </p>
        </footer>
    </div>
);
