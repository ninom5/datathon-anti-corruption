import {routes} from "@/routes";

interface NavigationItem {
    path: string;
    label: string;
}

export const navigationItems: NavigationItem[] = [
    {path: routes.HOME, label: "Početna"},
    {path: routes.CONTRACTS, label: "Istraži ugovore"},
    {path: routes.MAP, label: "Karta"},
    {path: routes.COMPARISON, label: "Usporedba"},
    {path: routes.METHODOLOGY, label: "Metodologija"},
    {path: routes.SOURCES, label: "Izvori"},
];

export const isNavigationItemActive = (location: string, path: string) => {
    if (path === routes.HOME) {
        return location === path;
    }
    return location === path || location.startsWith(`${path}/`);
};
