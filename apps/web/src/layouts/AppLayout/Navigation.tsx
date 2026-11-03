import {useState} from "react";
import {Link, useLocation} from "wouter";
import {Icon} from "@/components";
import {classNames} from "@/utils";
import {isNavigationItemActive, navigationItems} from "./navigationItems.ts";
import styles from "./Navigation.module.css";

const NAVIGATION_LIST_ID = "main-navigation";

export const Navigation = () => {
    const [location] = useLocation();
    const [isOpen, setIsOpen] = useState(false);

    const handleToggle = () => setIsOpen((wasOpen) => !wasOpen);
    const handleNavigate = () => setIsOpen(false);

    return (
        <nav className={styles.navigation} aria-label="Glavna navigacija">
            <button
                type="button"
                className={styles.toggle}
                aria-expanded={isOpen}
                aria-controls={NAVIGATION_LIST_ID}
                onClick={handleToggle}
            >
                <Icon name={isOpen ? "close" : "menu"} size={18}/>
                Izbornik
            </button>
            <ul id={NAVIGATION_LIST_ID} className={classNames(styles.list, isOpen && styles.listOpen)}>
                {navigationItems.map((item) => {
                    const isActive = isNavigationItemActive(location, item.path);

                    return (
                        <li key={item.path}>
                            <Link
                                href={item.path}
                                className={classNames(styles.link, isActive && styles.linkActive)}
                                aria-current={isActive ? "page" : undefined}
                                onClick={handleNavigate}
                            >
                                {item.label}
                            </Link>
                        </li>
                    );
                })}
            </ul>
        </nav>
    );
};
