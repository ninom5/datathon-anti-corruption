import {ButtonLink, EmptyState} from "@/components";
import {routes} from "@/routes";

export const NotFoundPage = () => (
    <EmptyState
        icon="document"
        title="Stranica ne postoji"
        hint="Poveznica je možda zastarjela ili pogrešno upisana."
        actions={<ButtonLink href={routes.CONTRACTS} variant="secondary">Istraži ugovore</ButtonLink>}
    />
);
