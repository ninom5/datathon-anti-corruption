import {Link} from "wouter";
import {routes} from "@/routes";
import {EmptyState} from "@/components";

export const NotFoundPage = () => (
    <EmptyState title="Stranica koju tražite ne postoji.">
        <Link href={routes.CONTRACTS}>Istraži ugovore</Link>
    </EmptyState>
);
