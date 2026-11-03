import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import test from 'node:test';
import ts from 'typescript';

// Compile pure TypeScript modules with the project's existing compiler; no browser/test dependency.
const moduleUrl = (file, imports = {}) => {
    let source = readFileSync(new URL(`../src/${file}`, import.meta.url), 'utf8');
    for (const [name, url] of Object.entries(imports)) source = source.replaceAll(`"${name}"`, `"${url}"`);
    const {outputText} = ts.transpileModule(source, {compilerOptions: {module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2023}});
    return `data:text/javascript;base64,${Buffer.from(outputText).toString('base64')}`;
};
const labels = moduleUrl('utils/labels.ts');
const {readExplorerState, writeExplorerState, toContractFilter} = await import(moduleUrl('pages/ContractsPage/explorerState.ts', {'@/utils': labels}));
const {buildTimeline, buildSupplierShares, supplierChartLabel, indicatorContext} = await import(moduleUrl('pages/IndicatorPage/indicatorCharts.ts'));
const {cpvDivisionLabel} = await import(moduleUrl('utils/cpv.ts'));

test('shared F/high URL sends the same rule and band to the contract filter', () => {
    const state = readExplorerState(new URLSearchParams('vrsta=F&pojas=visok&narucitelj=84121580205&stranica=3'));
    assert.deepEqual(toContractFilter(state), {buyerOib:'84121580205',supplierOib:undefined,year:undefined,ruleCode:['F_SPLITTING'],band:'visok',synthetic:undefined,sort:'dateSigned',page:2,size:25});
    assert.deepEqual(writeExplorerState(state), {narucitelj:'84121580205',vrsta:'F',pojas:'visok',stranica:'3'});
});

test('multi-select URL deduplicates known rules and ignores unknown input', () => {
    const state = readExplorerState(new URLSearchParams('vrsta=F,D1,F,unknown&pojas=bad&godina=no&stranica=-1&narucitelj=x'));
    assert.deepEqual(state.ruleCode, ['D_VALUE_INCREASE','F_SPLITTING']);
    assert.equal(state.band, undefined);
    assert.equal(state.year, undefined);
    assert.equal(state.buyerOib, undefined);
    assert.equal(state.page, 0);
    assert.deepEqual(writeExplorerState(state), {vrsta:'D1,F'});
});

test('rule, severity, band and synthetic selections survive a URL round trip', () => {
    const initial = readExplorerState(new URLSearchParams('vrsta=D2,C&pojas=srednji&oznake=worth&sinteticki=da&poredak=iznos'));
    const roundTrip = readExplorerState(new URLSearchParams(writeExplorerState(initial)));
    assert.deepEqual(roundTrip, initial);
    assert.equal(toContractFilter(roundTrip).severity, 'vrijedi_provjeriti');
    assert.equal(toContractFilter(roundTrip).synthetic, true);
});

const supplier = (id, extra = {}) => ({id,oib:`0000000000${id}`,name:'Primjer d.o.o.',type:'doo',nameHidden:false,synthetic:true,...extra});
const contract = (id, extra = {}) => ({id,dateSigned:'2025-01-01',subject:'SINTETIČKI PODACI ZA DEMONSTRACIJU',cpv:'45200000',supplier:supplier(1),initialValue:100,totalValue:null,procedureType:'Jednostavna nabava',...extra});

test('F keeps same-day contracts, orders by date/id and sums initial values in cents', () => {
    const input = [contract(2,{initialValue:0.2,totalValue:900}), contract(1,{initialValue:0.1}),contract(3,{dateSigned:'2025-02-01',initialValue:10})];
    const points = buildTimeline(input);
    assert.deepEqual(points.map(point => point.contract.id), [1,2,3]);
    assert.deepEqual(points.map(point => point.cumulative), [0.1,0.3,10.3]);
    assert.equal(points[0].timestamp, points[1].timestamp);
    assert.equal(input[0].id, 2);
});

test('F never turns an absent date or initial amount into a plotted zero', () => {
    assert.equal(buildTimeline([contract(1,{initialValue:null})]), undefined);
    assert.equal(buildTimeline([contract(1,{dateSigned:null})]), undefined);
    assert.equal(buildTimeline([contract(1,{dateSigned:'bad'})]), undefined);
    assert.deepEqual(buildTimeline([]), []);
});

test('C uses amended value with initial fallback and preserves an explicit zero', () => {
    const shares = buildSupplierShares([contract(1,{totalValue:300}),contract(2,{initialValue:100,supplier:supplier(2)}),contract(3,{totalValue:0,initialValue:900})]);
    assert.deepEqual(shares.map(row => [row.id,row.value,row.share,row.contracts]), [[1,300,0.75,2],[2,100,0.25,1]]);
});

test('C keeps different crafts separate even though both names are masked', () => {
    const craft = id => supplier(id,{type:'obrt',nameHidden:true,name:'Obrt (naziv skriven)'});
    const shares = buildSupplierShares([contract(1,{supplier:craft(1)}),contract(2,{supplier:craft(2)})]);
    assert.equal(shares.length, 2);
    assert.deepEqual(shares.map(row => row.share), [0.5,0.5]);
    assert.equal(shares.reduce((sum,row) => sum+row.share,0), 1);
});

test('C does not fabricate shares when a value is absent or total is not positive', () => {
    assert.equal(buildSupplierShares([contract(1,{initialValue:null,totalValue:null})]), undefined);
    assert.equal(buildSupplierShares([contract(1,{totalValue:0})]), undefined);
    assert.equal(buildSupplierShares([contract(1,{totalValue:-1})]), undefined);
    assert.equal(buildSupplierShares([]), undefined);
});

test('craft labels never expose a source name, even if the masking flag is missing', () => {
    assert.equal(supplierChartLabel(supplier(1,{type:'obrt',name:'Fiktivni vlasnik'})), 'Obrt (naziv skriven) · OIB 00000000001');
    assert.equal(supplierChartLabel(null), 'Dobavljač nije objavljen');
});

test('context keeps the source prose with abbreviations and never invents a monopoly', () => {
    const context = 'Prema registru javnopravnih tijela (JPT) naručitelj je osnivač (npr. društvo).';
    assert.equal(indicatorContext(`Izračun. ${context}`), context);
    assert.equal(indicatorContext('CPV skupina 09. Koncentracija.'), undefined);
    assert.equal(indicatorContext('Izračun. CPV skupina 09 obuhvaća djelatnosti (mogući prirodni monopol).'), 'CPV skupina 09 obuhvaća djelatnosti (mogući prirodni monopol).');
});

test('unknown CPV remains a code and missing CPV is not mislabeled', () => {
    assert.equal(cpvDivisionLabel('45'), '45 · Građevinski radovi');
    assert.equal(cpvDivisionLabel('99'), 'CPV 99');
    assert.equal(cpvDivisionLabel(null), 'CPV nije objavljen');
});
