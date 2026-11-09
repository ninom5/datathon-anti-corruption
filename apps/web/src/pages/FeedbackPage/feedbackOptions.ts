import type {FeedbackKind, FeedbackRole} from "@/types";

interface KindOption {
    value: FeedbackKind;
    label: string;
    hint: string;
}

export const kindOptions: KindOption[] = [
    {value: "greska_u_podacima", label: "Greška u podacima", hint: "Iznos, datum, dobavljač ili drugi podatak ne odgovara stvarnom ugovoru."},
    {value: "kontekst", label: "Dodatni kontekst", hint: "Objašnjenje koje u objavljenim podacima nedostaje, npr. zašto je iznos promijenjen."},
    {value: "pravo_na_odgovor", label: "Pravo na odgovor", hint: "Službeni odgovor naručitelja ili dobavljača na oznaku. Nakon provjere prikazuje se uz indikator."},
    {value: "uklanjanje_osobnih_podataka", label: "Uklanjanje osobnih podataka", hint: "Zahtjev da se ukloni podatak o fizičkoj osobi. Obrađuje se u roku od 7 dana."},
];

export const roleOptions: {value: FeedbackRole; label: string}[] = [
    {value: "narucitelj", label: "Predstavnik naručitelja (općina, grad, županija)"},
    {value: "dobavljac", label: "Predstavnik dobavljača"},
    {value: "novinar", label: "Novinar"},
    {value: "gradanin", label: "Građanin"},
    {value: "ostalo", label: "Ostalo"},
];

export const kindsNeedingContact: FeedbackKind[] = ["pravo_na_odgovor", "uklanjanje_osobnih_podataka"];

export const TEXT_MIN_LENGTH = 10;
export const TEXT_MAX_LENGTH = 4000;
