# #42 UI: profil JLS, filter "Vrsta indikatora", prikaz F i C (stanje rada)

_Grana `42-ui-fc-profil` (iz `main` nakon PR #50). **Necommitano.** Zadnje ažurirano: 2026-11-03 navečer. Puni zadatak: `work/prompts/next-42-ui-fc-profil.md`._

## Gotovo

### Faza 0: backend (gotovo i provjereno)
- **`GET /api/v1/indicators/{id}`** (`IndicatorController.get`, `IndicatorService.getById`):
  - vraća sva polja `IndicatorResponse` na vrhu JSON-a (`@JsonUnwrapped`) te `contracts` i `subject`;
  - `contracts` (`IndicatorContract`): `id`, `dateSigned`, `subject`, `cpv`, `supplier` (`OrganizationRef`), `initialValue`, `totalValue`, `procedureType`, poredano po datumu. Ugovori s fizičkim osobama ne izlaze;
  - `subject` (`IndicatorSubject`, polja `null` se ne šalju):
    - F: `cpv`, `cpvLevel`, `from`, `to`, `threshold`, `supplierOib`;
    - C: `cpv2`, `years`, `topSupplierOib`;
    - D1/D2: `null`;
  - 404 "Indikator N ne postoji" (i za neaktivan), 400 za id koji nije broj.
- **`SplittingRule`:** `subject_ref` sada ima `threshold` (najniži prag u prozoru, EUR). Pravila su ponovno pokrenuta na lokalnoj bazi.
- Novi repo upiti:
  - `IndicatorRepository.findDetailById` (s `buyer` i `contract`);
  - `ContractRepository.findByIdInOrderByDateSignedAscIdAsc` (sa `supplier`).
- **Testovi:** `IndicatorControllerTest` + 2 testa (ravni JSON s `contracts` i `subject`; 404 i 400). **`./mvnw test` 48/48 zeleno**, uključujući `SyntheticGroundTruthTest`.
- Provjereno `curl`-om na stvarnoj bazi:
  - F 2354 (Sv. Filip i Jakov, "Kulturno ljeto", 5 ugovora, `threshold` 26540);
  - C (CPV 50, 6 ugovora);
  - D2 2240 (`subject: null`);
  - 404.
- Brojke nakon ponovnog pokretanja (stvarni + sintetički): F 161, C 24, D1 24, D2 96.

### Lokalno okruženje
- **Portovi 8080, 8091 i 8092 drže tri druga `ApiApplication` procesa** (PID 19120, 25792, 8828; vjerojatno stare sesije ili `Desktop/datathon`). Nisu ugašeni.
- API je pokrenut na **8093**: `ADMIN_TOKEN=local-dev SERVER_PORT=8093 ./mvnw spring-boot:run`.
- **`vite.config.ts`:** proxy čita `API_URL` (zadano je i dalje `http://localhost:8080`). Za lokalni rad: `API_URL=http://localhost:8093 yarn dev`.

### Pročitano / utvrđeno za frontend (još bez koda)
- `useContractSummary` → `toSummaryFilter` **ne prosljeđuje `ruleCode` i `band`**. Treba ih dodati, jer backend ih prima, a `contractsByRule` sam isključuje vlastiti izbor pravila.
- `evidenceStrengthHints` je pisan za D ("razlog nije objavljen"). Treba tekst po pravilu (F: ≥ 3 ugovora, svaki ≥ 70 % praga; slaba = samo CPV2; C: jaka = ≥ 15 ugovora u ćeliji).
- Profil vraća `byCpv` bez naziva CPV odjeljka. Backend nema tablicu naziva (`cpvLabel` je samo iz retka izvora).
- Stvarni JSON profila je provjeren (Ivanec `84121580205`: 799 ugovora, 62 indikatora, 161 ugovor s obrtima; Uzorkovo `00000000003`: `synthetic: true`, `municipality: null`; nepostojeći OIB vraća 404).

## Preostalo

### Faza 1: istraživač
- [ ] `utils/labels.ts`: mapiranje D1/D2/F/C ↔ `RuleCode` na jednom mjestu, nazivi opcija i nazivi pojaseva.
- [ ] `explorerState.ts`: `vrsta` (lista, `?vrsta=D1,F`) i `pojas` (`?pojas=visok`) → `ruleCode` i `band`.
- [ ] `FilterPanel`: fieldset "Vrsta indikatora" (checkboxi s brojem iz `contractsByRule`) i "Pojas" (nizak/srednji/visok, s napomenom da je to heuristika, a ne ocjena).
- [ ] `useContractSummary`: proslijediti `ruleCode` i `band`.
- [ ] `ContractTable`: naziv naručitelja vodi na profil.
- [ ] `docs/UI_UX.md` §4a: izbrisati "bez obrta".

### Faza 2: profil općine `/opcina/:oib`
- [ ] `routes.ts` (`BUYER`, `buyerPath`), `Router.tsx`, `useBuyerProfile` (+ `buyerKeys.profile`).
- [ ] Zaglavlje: naziv · županija · stanovnici (DZS 2021) · usporedna skupina; `coverage.label` + ⓘ izvori.
- [ ] Graf ugovora po godini (SVG stupci; nepotpuna godina sa šrafurom i oznakom `*`, ne samo bojom) + "Prikaži kao tablicu".
- [ ] Potrošnja po CPV odjeljku (vodoravni stupci + tablica, € po stanovniku ako je poznat broj stanovnika). Bez medijana skupine (#43).
- [ ] Najveći dobavljači + napomena "N ugovora s obrtima uključeno je u brojke, ali bez naziva".
- [ ] Indikatori: brojke po vrsti i kompaktne kartice s poveznicom na detalj (do 10 po vrsti, uz "prikaži sve").
- [ ] Sva 4 stanja; 404 → "Za ovog naručitelja nema objavljenih ugovora u podacima"; ljubičasta traka za sintetiku.
- [ ] Poveznica na profil s detalja ugovora.

### Faza 3: detalj indikatora `/indikator/:id`
- [ ] Tip `IndicatorDetail` (+ `IndicatorSubject`, `IndicatorContract`) u `types/Indicator.type.ts`, `getIndicator`, `useIndicator`, ruta `INDICATOR` + `indicatorPath`.
- [ ] `IndicatorCard` premjestiti u `components/` (s vlastitim `IndicatorCard.module.css`). Dodati poveznicu "Otvori indikator", a za F/C "Ovaj ugovor je dio skupine od N ugovora".
- [ ] `evidenceStrengthHints` napisati po pravilu.
- [ ] F: SVG vremenska os (točka po ugovoru, `initialValue`, isprekidana crta `subject.threshold`, kumulativni zbroj kao stepenasta linija), tablica ugovora. Za `cpvLevel === 2` napisati riječima da se podudara samo CPV odjeljak.
- [ ] C: vodoravni stupci udjela po dobavljaču (vrijednost = `totalValue ?? initialValue`, kao u pravilu). Obrti bez naziva. Napomena o prirodnom monopolu ili JPT-u kao kontekst, ne kao upozorenje. Tablica ugovora.
- [ ] D1/D2: ponovno iskoristiti karticu.
- [ ] Svaki graf: `aria-label` sa sažetkom, "Prikaži kao tablicu", tooltip (`<title>`), boja nikad jedini nosilac značenja, `--signal` jantarna.

### Provjera i dokumentacija
- [ ] `yarn lint`, `yarn build` (u `apps/web`) i `./mvnw test`.
- [ ] Preglednik ili headless Edge:
  - `?vrsta=F&pojas=visok`;
  - profil Ivanca i Uzorkova;
  - detalj F (2354), detalj C, informativni indikator (Varaždinske Toplice);
  - prazno stanje i greška (zaustavljen API);
  - 1280 px i mobitel.
- [ ] `docs/UI_UX.md` §4a (rute, stanje), `docs/ARCHITECTURE.md` §5 (novi endpoint), `work/tasks.md`, `log.md`.

## Otvoreno pitanje
- **Nazivi CPV odjeljaka za graf potrošnje.** Plan: mala statična tablica skraćenih hrvatskih naziva CPV 2008 odjeljaka (Uredba 213/2008) u `utils/`, s fallbackom "CPV 45". Alternativa je prikaz samo koda. Zapisati u `decisions.md` kad se uvede.
