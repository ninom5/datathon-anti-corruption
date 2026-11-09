# JavniRadar design system

The look and frontend conventions of `apps/web`. It is the **same system as the OtvoreniRadar frontend** (`FESB/datathon`, `DESIGN_SYSTEM.md` there is the long version with rationale). Token values live in `apps/web/src/styles/global.css`, components in `apps/web/src/components/` (one folder per component).
If you change a token or a component, update this file in the same change. Code rules (hooks, API, types) are in the `react-web` skill.

## 1. Principles
1. **Trust before decoration.** Every number shows where it came from (file, row, dataset).
2. **Information density.** Compact rows and tables, short labels.
3. **Calm.** One brand colour (forest green), warm neutrals, 1 px borders instead of shadows, no motion beyond colour transitions.
4. **Honest states.** Every data screen has loading, error, empty and data states. Missing value = "nije objavljeno", never 0.
5. **Reuse before inventing.** A new pattern is a design-system change.

## 2. Tokens (`global.css`)
- **Neutrals and brand:** identical to OtvoreniRadar (`--color-background`, `--color-surface`, `--color-surface-subtle`, `--color-surface-hover`, `--color-border`, `--color-border-strong`, `--color-text`, `--color-text-muted`, `--color-text-subtle`, `--color-primary` + `-hover`/`-soft`/`-border`).
- **Semantic:** `--color-success`, `--color-warning`, `--color-danger`, `--color-info`, `--color-ai` with `-soft` pairs.
- **JavniRadar only:**

| Token | Hex | Use |
|---|---|---|
| `--color-signal` / `-soft` / `-border` | `#85570a` / `#f8efda` / `#dcc393` | Indicator "vrijedi provjeriti". Amber, never red: colour must not suggest guilt |
| `--color-synthetic` / `-soft` | `#5b3f8f` / `#efeaf6` | Synthetic demo data (banner, row marker, toggle). Its own violet, not the AI hue |
| `--map-0` … `--map-4` | amber ramp | Map classes, light = fewer indicators per 100 contracts |
| `--map-no-data` / `-line` | `#eef0ea` / `#a9b2a6` | Hatched "no machine-readable register", never the same as zero |

- **Type:** `--font-family-display` Source Serif 4 600 for `h1`/`h2`; `--font-family` IBM Plex Sans 400/500/600. Sizes `--font-size-xs` 13 px … `--font-size-2xl` 34 px; nothing under 13 px. Numbers use `.tabular-nums`.
- **Spacing** `--space-1`…`--space-8` (4 px grid). **Radii** `--radius-xs` 4, `--radius-sm` 6, `--radius-md` 8 (nothing larger). **Layout** `--content-width` 1200, `--aside-width` 320, `--header-height` 56, `--gutter`.
- Breakpoints: 960 px (two columns collapse), 768 px (mobile: menu, stacked tables, 42 px controls).
- No dark mode (OtvoreniRadar has none; the video is recorded in light mode).

## 3. Components (`@/components`)
Copied unchanged from OtvoreniRadar: `Button` / `ButtonLink` / `ExternalButtonLink`, `Badge`, `Notice`, `Card`, `PageHeader`, `EmptyState`, `ErrorState`, `LoadingState`, `BackLink`, `ExternalLink`, `FactList` + `UnknownValue` (default "nije objavljeno"), `Select`, `TextInput`, `ChoiceChip`, `Icon` (Lucide paths, no icon library). `TextArea` is JavniRadar's own, with the `TextInput` look (correction form).

JavniRadar components on the same tokens:

| Component | Use |
|---|---|
| `SyntheticBanner` | Full-width violet "SINTETIČKI PODACI ZA DEMONSTRACIJU" bar, flush under the header. Render it as the first child of the page |
| `IndicatorTag` | Badge-shaped rule tag with the rule letter (colour never the only carrier). Signal / informative / dashed "moguća greška u unosu" |
| `IndicatorCard` | Card with amber left rule (neutral when informative), three equal dimension boxes, "Što ovo NE znači". Never a combined score |
| `OrganizationName` | Name or "Obrt (naziv skriven)" + OIB |
| `ChartFrame`, `HorizontalBars` (`DataView/`) | Framed chart section with "Prikaži kao tablicu". `DataView.module.css` also holds shared section, table and note styles for detail pages |

Folder rule: every component has its own folder with `Name.tsx`, `Name.module.css`, `index.ts`. Components import each other through the folder barrel (`@/components/Icon`), pages through `@/components`.

## 4. Page layouts
- **Shell** (`layouts/AppLayout`): sticky white header (brand mark left, nav right, active item = 2 px green bar), mobile "Izbornik", main max 1200 px, white footer with disclaimer and "Izvori i metodologija". Skip link to `#main-content`.
- **Every page** starts with `PageHeader` (one `h1`, description, optional `meta` facts line and `actions`). No uppercase eyebrows above titles.
- **Home:** `PageHeader` → two columns: framed facts list (serif numbers, one divider per fact) + 320 px "Kako radi" side panel with serif green step numbers.
- **Explorer:** `PageHeader` → summary line of real numbers → 260 px bordered filter panel (sticky) + framed table (subtle header row, hover row, stacked rows under 768 px) → pagination with `Button`s.
- **Details (contract, buyer, indicator):** `BackLink` → `PageHeader` → main column + 320 px aside of bordered panels (amounts, source). Chart and table sections are framed `Card`-like surfaces; lists of indicator cards are not framed again.
- **Form (correction, `/ispravak`):** `BackLink` → `PageHeader` with the target in `meta` → bordered form (`ChoiceChip` radios, `TextArea`, `Select`, `TextInput`, inline errors, focus on the first invalid field) + 320 px panel "Što se događa s prijavom". Success replaces the form with a success `Notice`.
- **Reading page (`/metodologija`):** narrow column (`--content-width-narrow`) + sticky "Na ovoj stranici" list; one bordered surface per rule with formula, threshold table (value + source) and "Što ovo NE znači".
- **States:** `LoadingState` / `ErrorState` (with "Pokušaj ponovno") / `EmptyState` (title without full stop, hint, one secondary action).

## 5. Do and don't
| Do | Don't |
|---|---|
| Tokens only (`var(--space-4)`, `var(--color-signal)`) | Hex values or red for indicators |
| `Badge`-shaped tags with `--radius-xs` | Pill tags, uppercase labels, eyebrows |
| `ButtonLink` for navigation, `Button` for actions | Underlined text buttons for primary actions |
| `ExternalLink` for links leaving the app | A bare `target="_blank"` link without the icon |
| One framed list or table with dividers | A card per item with its own coloured inner box |
