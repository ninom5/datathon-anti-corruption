export const classNames = (...names: Array<string | false | undefined>) =>
    names.filter(Boolean).join(" ");
