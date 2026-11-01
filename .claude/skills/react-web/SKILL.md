---
name: react-web
description: MUST be loaded before writing or changing any frontend code in apps/web of JavniRadar (React 19, TypeScript, Vite, wouter, TanStack Query, axios). Trigger on any implementation request that touches the UI, including Croatian ones such as "implementiraj", "napravi stranicu / ekran", "dodaj komponentu / graf / kartu", "popravi UI", or any frontend task from docs/MVP_ROADMAP.md or docs/UI_UX.md (istraživač ugovora, profil općine, detalj indikatora, AI procjena, karta, izvori). Covers pages, components, hooks, API calls, types, routes, charts, the map, styles and UI/UX polish.
---

# React web (`apps/web`)

Read first: `docs/UI_UX.md` (design direction, screens, tokens), `docs/FEATURES.md` and `AGENTS.md`.

UI/UX is worth **10 of 60 points**, and the video (10 pts) is recorded from this very interface. So every screen is: fast, clear at first glance, with loading, error and empty states handled. Code must stay consistent with the existing skeleton.

## Stack (verified in `package.json`)
- React 19, TypeScript (strict), Vite, **wouter** (NOT React Router), **TanStack Query v5**, axios, `react-error-boundary`.
- No UI library or CSS framework. **Don't add dependencies (Tailwind, MUI, shadcn, zustand, date-fns...) without asking the user.** It's a team decision and goes into `work/decisions.md`. `docs/UI_UX.md` §6 proposes Observable Plot (charts) and `d3-geo` or MapLibre (map); these are proposals, so ask before adding them.
- Package manager is **yarn** (`yarn.lock`). Don't use npm/pnpm.

## Structure (existing, respect it)
```
src/
├── api/          # base.ts (axios instance) + <domain>.api.ts
├── components/   # shared components
├── constants/
├── hooks/        # useContracts.ts, useBuyer.ts ... (TanStack Query hooks)
├── layouts/
├── pages/        # one folder per page: HomePage/HomePage.tsx + index.ts
├── routes/       # routes.ts (path constants)
├── types/        # <Name>.type.ts
├── App.tsx       # ONLY providers (ErrorBoundary, QueryClientProvider)
├── Router.tsx    # ONLY <Switch>/<Route>
└── main.tsx
```
- **Every folder has a barrel `index.ts`.** When you add a file, add `export * from "./File.tsx";` to the barrel. New folder = created with `index.ts` right away.
- **Always import through the `@/` alias and the barrel**: `import {HomePageTitle} from "@/components";`, not `../../components/HomePageTitle`.
- A component used by only one page lives in that page's folder (`pages/IndicatorPage/DimensionCard.tsx`). It moves to `components/` only once at least two pages use it.
- **All code is in English** (components, hooks, types, files, variables), using the domain glossary in the `spring-boot-api` skill (ugovor → `Contract`, naručitelj → `Buyer`, dobavljač → `Supplier`, indikator → `Indicator`...). Only user-visible UI text is Croatian.

## Components
- **Named export, arrow function, PascalCase**, file name = component name:
  ```tsx
  interface ContractRowProps {
      contract: Contract;
      onSelect: (id: number) => void;
  }

  export const ContractRow = ({contract, onSelect}: ContractRowProps) => {
      ...
  };
  ```
  Default export only for `App` (already exists).
- Props type as `interface <Name>Props` right above the component. No `React.FC`.
- **The page (`pages/`) wires up data, components display it.** The page calls a hook (`useContracts`) and passes data down; presentational components don't call the API.
- Component over ~120 lines or with more than one responsibility → split it.
- No `useEffect` for data fetching (TanStack Query does that) and no `useEffect` for derived state (compute it during render). `useEffect` is for syncing with the outside world.
- Lists always with a stable `key` (database id), never the index.

## Writing functions
- One function = one thing; short; **early `return`** instead of nested ternaries.
- Names: handlers `handleX` inside the component, the props for them `onX` (`onSelect` → `handleSelect`). Booleans `isX/hasX`. No abbreviations.
- Pure helpers (formatting, filtering) outside the component or in `utils/` (create the folder with a barrel when needed), so they aren't recreated every render and can be tested.
- **No `any`.** If the type is unknown: `unknown` + narrowing. No `as` casts except at boundaries (e.g. the existing `document.getElementById('root')!`).
- Don't return different types from the same function. For "may be absent" use `undefined`, don't mix with `null`.

## Types
- In `types/<Name>.type.ts`, one domain type per file, exported through `types/index.ts`.
- The type mirrors the backend JSON **exactly** (camelCase, dates as ISO `string`, amounts as `number | null`). If the backend changes the DTO, change the type too.
- For state unions use string literal types (`type DataQuality = "niska" | "srednja" | "visoka";`, values identical to the backend JSON), not `enum`.

## API layer
- The only axios instance is `api` from `@/api`. **The interceptor already returns `response.data`** and turns errors into an `Error` with the backend message (`{statusCode, message, error}`). So functions return data directly:
  ```ts
  // api/contracts.api.ts
  import {api} from "./base.ts";
  import type {Contract, ContractFilter, Page} from "@/types";

  export const getContracts = (params: ContractFilter) =>
      api.get<Page<Contract>, Page<Contract>>("/v1/contracts", {params});

  export const getContract = (id: number) =>
      api.get<Contract, Contract>(`/v1/contracts/${id}`);
  ```
  (The second generic parameter is needed because the interceptor changes the return type.)
- Paths without the `/api` prefix (already in `baseURL`), so public endpoints start with `/v1/`. Parameters via `params`, not by manually concatenating query strings.
- Dev proxy: if `vite.config.ts` has no `server.proxy` for `/api` → `http://localhost:8080` yet, add it before the first real call (otherwise calls hit Vite and return 404).

## Data fetching (TanStack Query)
- Every fetch goes through a hook in `hooks/`: `useContracts(filter)`, `useContract(id)`, `useBuyer(oib)`. Components don't call `useQuery` with an inline `queryFn`.
- Query keys as arrays from general to specific, defined in one place per domain:
  ```ts
  export const contractKeys = {
      all: ["contracts"] as const,
      list: (filter: ContractFilter) => [...contractKeys.all, "list", filter] as const,
      detail: (id: number) => [...contractKeys.all, "detail", id] as const,
  };
  ```
- Mutations (`useMutation`) call `queryClient.invalidateQueries({queryKey: contractKeys.all})` on success.
- For filters/search use `placeholderData: keepPreviousData` so the list doesn't flicker.

## Routes (wouter)
- Paths as constants in `routes/routes.ts`, never a string literal in `<Link>`/`navigate`.
- New page = folder in `pages/` + barrel + constant in `routes` + `<Route>` in `Router.tsx` (before the catch-all `NotFoundPage`).
- Params: `useParams()` / `useRoute()` from wouter. Filter state the user may want to share or come back to lives in the URL (`useSearchParams` from wouter), not only in `useState`.

## UI/UX rules (this is scored)
- **Every data screen has 4 states**: loading (skeleton or clear message), error (message from `error.message` + "Pokušaj ponovno" button → `refetch`), empty ("Nema ugovora za odabrane filtere" + how to change that; for a municipality without data: "Ova općina ne objavljuje strojno čitljiv registar ugovora"), data.
- **All UI text is in Croatian**, short and concrete. No "Lorem ipsum" and no English leftovers in the UI.
- Formatting via `Intl` with `"hr-HR"`: dates (`23. 10. 2026.`), amounts (`new Intl.NumberFormat("hr-HR", {style: "currency", currency: "EUR"})`). Missing values are shown as "nije objavljeno", never as 0. Amounts converted from HRK carry a "preračunato iz kn" note.
- Every number has a **visible source** (ⓘ with dataset, publisher, file/row link, retrieval date, method). Fields extracted by an LLM show their confidence. This builds trust and earns points for "Use of open data".

## JavniRadar UI rules (non-negotiable)
- **Indicators:**
  - show the three dimensions (kvaliteta podataka, veličina odstupanja, pouzdanost dokaza) as three equal, separate boxes;
  - never show a combined score or a "risk %";
  - always show the "Što ovo NE znači" section.
- **AI assessment:**
  - a separate block under the rule indicators, always titled **"AI procjena – zahtijeva ljudsku provjeru"**;
  - shows the reasons with links, the legitimate explanations (never collapsed away), the model and the date, and "Korisno / Pogrešno" buttons.
- **Wording:** UI text never uses the forbidden terms in `AGENTS.md` ("korupcija", "nezakonito", "prijevara"...). Use "neuobičajeno" and "vrijedi provjeriti".
- **Colour:** the signal colour is amber (`--signal`), not red. Colour is never the only carrier of meaning (the badge also shows the rule letter). "No data" on the map is its own patterned category, not zero.
- **Synthetic data:** a full-width violet banner **"SINTETIČKI PODACI ZA DEMONSTRACIJU"** whenever the API returns `synthetic: true`.
- **Charts:** every chart has an "Prikaži kao tablicu" alternative and an `aria-label` summary. Load the `dataviz` skill before building charts.
- Accessibility: semantic HTML (`button` for actions, `a`/`Link` for navigation, `label` for every form field), visible focus, contrast, `alt` on images. Accessibility is explicitly part of challenge A and the jury values it everywhere.
- Responsive: must look good at 1280 px (video) and be usable on mobile.
- Styles: until the team decides otherwise, use CSS Modules (`Component.module.css` next to the component) and the CSS variables from `docs/UI_UX.md` §1 (colours, typography) in one global file. No inline `style={{...}}` except for dynamic values.

## Before saying it's done
1. In `apps/web`: `yarn lint` and `yarn build` pass (rule from CLAUDE.md, mandatory before submission).
2. Check any UI-visible change in the browser (`yarn dev`): happy path + empty state + error (e.g. API stopped).
3. If you added a type for a new endpoint, verify it matches the real JSON from the backend, not an assumed one.
4. Check the screen against `docs/UI_UX.md` and the acceptance criteria in `docs/MVP_ROADMAP.md` §5.
