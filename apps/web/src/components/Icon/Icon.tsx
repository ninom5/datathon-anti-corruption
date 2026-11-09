import type {ReactNode} from "react";

const ICON_PATHS = {
    alert: (
        <>
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/>
            <path d="M12 9v4"/>
            <path d="M12 17h.01"/>
        </>
    ),
    arrowLeft: (
        <>
            <path d="m12 19-7-7 7-7"/>
            <path d="M19 12H5"/>
        </>
    ),
    arrowRight: (
        <>
            <path d="M5 12h14"/>
            <path d="m12 5 7 7-7 7"/>
        </>
    ),
    calendar: (
        <>
            <path d="M8 2v4"/>
            <path d="M16 2v4"/>
            <rect width="18" height="18" x="3" y="4" rx="2"/>
            <path d="M3 10h18"/>
        </>
    ),
    check: <path d="M20 6 9 17l-5-5"/>,
    chevronLeft: <path d="m15 18-6-6 6-6"/>,
    chevronRight: <path d="m9 18 6-6-6-6"/>,
    close: (
        <>
            <path d="M18 6 6 18"/>
            <path d="m6 6 12 12"/>
        </>
    ),
    database: (
        <>
            <ellipse cx="12" cy="5" rx="9" ry="3"/>
            <path d="M3 5V19A9 3 0 0 0 21 19V5"/>
            <path d="M3 12A9 3 0 0 0 21 12"/>
        </>
    ),
    document: (
        <>
            <path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/>
            <path d="M14 2v4a2 2 0 0 0 2 2h4"/>
            <path d="M16 13H8"/>
            <path d="M16 17H8"/>
        </>
    ),
    euro: (
        <>
            <path d="M4 10h12"/>
            <path d="M4 14h9"/>
            <path d="M19 6a7.7 7.7 0 0 0-5.2-2A7.9 7.9 0 0 0 6 12c0 4.4 3.5 8 7.8 8 2 0 3.8-.8 5.2-2"/>
        </>
    ),
    externalLink: (
        <>
            <path d="M15 3h6v6"/>
            <path d="M10 14 21 3"/>
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
        </>
    ),
    filter: (
        <>
            <path d="M3 6h18"/>
            <path d="M7 12h10"/>
            <path d="M10 18h4"/>
        </>
    ),
    help: (
        <>
            <circle cx="12" cy="12" r="10"/>
            <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
            <path d="M12 17h.01"/>
        </>
    ),
    info: (
        <>
            <circle cx="12" cy="12" r="10"/>
            <path d="M12 16v-4"/>
            <path d="M12 8h.01"/>
        </>
    ),
    institution: (
        <>
            <path d="M3 22h18"/>
            <path d="M6 18v-7"/>
            <path d="M10 18v-7"/>
            <path d="M14 18v-7"/>
            <path d="M18 18v-7"/>
            <path d="M12 2 20 7H4Z"/>
        </>
    ),
    mapPin: (
        <>
            <path d="M20 10c0 5-5.5 10.2-7.4 11.8a1 1 0 0 1-1.2 0C9.5 20.2 4 15 4 10a8 8 0 0 1 16 0"/>
            <circle cx="12" cy="10" r="3"/>
        </>
    ),
    menu: (
        <>
            <path d="M4 6h16"/>
            <path d="M4 12h16"/>
            <path d="M4 18h16"/>
        </>
    ),
    retry: (
        <>
            <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
        </>
    ),
    search: (
        <>
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.3-4.3"/>
        </>
    ),
} satisfies Record<string, ReactNode>;

export type IconName = keyof typeof ICON_PATHS;

interface IconProps {
    name: IconName;
    size?: number;
    className?: string;
}

export const Icon = ({name, size = 16, className}: IconProps) => {
    return (
        <svg
            className={className}
            width={size}
            height={size}
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth={1.75}
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
            focusable="false"
        >
            {ICON_PATHS[name]}
        </svg>
    );
};
