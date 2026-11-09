# Ručno označavanje stvarnog uzorka (AI evaluacija)

Datoteka: `real-sample-seed44.csv` (UTF-8, odvojeno točkom-zarezom, otvara se u Excelu). Sadrži 60 nasumičnih stvarnih ugovora (seed 44).

## Kako označiti
1. Za svaki redak otvori poveznicu iz stupca `izvor` (redak u izvornom registru) ili stranicu ugovora u JavniRadaru (`javniradar`, npr. `http://localhost:5173/ugovor/1987`).
2. **Prije gledanja AI bloka na dnu stranice** odluči bi li novinar ili vijećnik trebao ručno provjeriti taj ugovor:
   - `vrijedi_provjeriti`: nešto u podacima je neuobičajeno ili si proturječi i vrijedi pogledati dokumentaciju;
   - `ne`: ništa neuobičajeno.
3. Osoba 1 upisuje oznaku u `oznaka_osoba1`, osoba 2 u `oznaka_osoba2`. Označavajte **neovisno**, bez dogovaranja.
4. Prazna ćelija znači "nije označeno" i ne ulazi u metrike.

## Izračun
```
curl -s -XPOST -H "X-Admin-Token: <token>" -H "Content-Type: text/csv" \
  --data-binary @work/eval/real-sample-seed44.csv http://localhost:8080/api/admin/ai/eval/real
```
Rezultat se upisuje u `docs/EVALUATION.md` §2, s datumom i verzijom prompta.

## Ostale datoteke
- `generate_synthetic_cases.py`: generator sintetičkog skupa. Trenutni skup je u `apps/api/src/main/resources/ai/eval/`, a ovdje su starije verzije (`synthetic-cases-v1..v3.json`).
- `ai-flag-*-synthetic-*.json`: sirovi rezultati svakog runa evaluacije (slučaj, očekivano, dobiveno, razlozi, trošak).
