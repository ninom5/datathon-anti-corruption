# Odluke

_Format: datum, odluka, zašto, alternative koje smo odbacili._

## 2026-10-22: Ispravak: prijavu ispunjava svaki član, ne samo mentor
- Ranija bilješka (iz pozivnog maila) govorila je da prijavu podnosi mentor. Pravila (VI) i Javni poziv (V) kažu: **svaki član tima ispunjava prijavni obrazac** i navodi naziv tima + kontakt mentora. Mentor je voditelj tima i komunicira s organizatorom.
- Posljedica: svi 4 člana moraju stići ispuniti obrazac do 23.10. 24:00, a mentor mora biti dogovoren da bi se njegovi podaci mogli upisati.

## 2026-10-22: Preporuka: prijaviti jedan izazov, ne sva tri (prijedlog, čeka potvrdu tima)
- Pravila zahtijevaju **zasebno rješenje po izazovu**. Za 4 osobe u ~17 dana to znači 3 plitka rješenja umjesto jednog dobrog.
- Izvedivost, UI/UX i video nose 30 od 60 bodova. To se dobiva dubinom, ne širinom.
- Napomena: prijava je besplatna i ne obvezuje na predaju, pa se može prijaviti i drugi izazov "u rezervi" ako obrazac to dopušta. Ne treba ga razvijati.
- Odbačeno: prijava na sva tri izazova s ciljem tri nagrade. Realno se ne stigne.

## 2026-10-22: Bodovni profil određuje strategiju
- Max 60 b. Pragovi: nagrada se ne mora dodijeliti ispod **50 b**, a izjednačenje se rješava po "Izvedivost", pa "Upotreba otvorenih podataka".
- Težište: radni prototip (10), UI/UX (10), video (10). Ostalih šest kriterija po 5.
- Podaci izravno preko **API-ja** (kriterij "Upotreba otvorenih podataka") i **povezivanje više izvora** (kriterij "Povezivanje") jeftini su bodovi ako se planiraju od početka.
- Razrađeni koncept je dopušten, ali bez prototipa gubimo većinu od 10 b za izvedivost.

## 2026-10-22: Korištenje AI alata je dopušteno
- Pravila VIII dopuštaju AI alate ako ne krše licence i ako se **navedu u dokumentaciji**. Rješenje mora ostati "izvorno djelo tima". Zato u dokumentaciju ide odjeljak "Korišteni alati i biblioteke" uključujući AI.

## 2026-10-22: Predaja koda na javni repozitorij: OTVORENO, čeka sigurnosnu provjeru
- Pravila traže "izvorni kod ili poveznicu na javni repozitorij", a objava pod OSS licencom se boduje. Organizacijska sigurnosna politika zabranjuje objavu sadržaja na javne platforme.
- Do jasnog odgovora kod se **ne objavljuje javno**. Opcije: (a) pitati security/AppSec smije li se osobni projekt za natjecanje objaviti, (b) predati kod kao arhivu ili link za preuzimanje (Pravila to dopuštaju za prevelike privitke), ali link mora trajati do objave rezultata i mora biti na odobrenom mjestu (npr. SharePoint).
- Odluka ovisi o korisniku jer je pitanje politike organizacije, ne tehničko.

## 2026-11-01: Projekt JavniRadar (izazov A), dokumentacija u `docs/`
- Korisnik je zadao brief za **JavniRadar** (analiza lokalne javne nabave, pravila + AI procjena). Rok predaje 9.11. Detalji su u `docs/`.
- **Glavni izvor: općinski registri ugovora u standardnom EOJN XLSX formatu** (~30 JLS na data.gov.hr, 2.425 ugovora u strogo parsiranom podskupu). Zašto: jedina shema s OIB-om obiju strana, CPV-om, izmjenama, isplatama i poveznicom na eojn.hr. Nacionalni JSON izvoz (20.000 redaka) je uzorak (Osijek: 32 naspram 588), a OCDS nema OIB.
- **MVP pravila: D, F, C.** Zašto: jedina za koja postoje podaci. E nije izvedivo (nema broja ponuda nigdje). B ovisi o Sudreg ključu. A samo kao kategorija po stanovniku.
- **Stack: preporuka Java/Spring Boot + React (postojeći skelet), a ne FastAPI iz briefa.** Zašto: reusable kod (PoliteHttpClient, Sudreg, AI paket) postoji samo u Javi u `Desktop/datathon`. ČEKA POTVRDU TIMA.
- **ZJN je izmijenjen (NN 48/26, 16.05.2026.):** pragovi jednostavne nabave su sada 50.000/100.000 EUR (prije 26.540/66.360); registar ugovora se objavljuje u EOJN-u; upis od 5.000 EUR. Novi Pravilnik NN 86/2026 propisuje 31 obvezno polje. Pragovi idu u konfiguraciju s datumom važenja.
- Ne strugamo eojn.hr (robots.txt `Disallow: /`).
- Otvoreno: odnos prema projektu natječaja (izazov C, `Desktop/datathon`). Oba projekta punim kapacitetom u 8 dana nisu realna.

## 2026-11-02: Stack potvrđen: Java/Spring Boot + React
- Korisnik je potvrdio Javu. Obrazloženje je u `docs/ARCHITECTURE.md` §1: kod za ponovnu upotrebu postoji samo u Javi, a skelet je već postavljen. Python ostaje samo za istraživanje i evaluaciju.

## 2026-11-02: POC uključuje sintetički skup s poznatim ishodom
- Korisnik želi testne podatke koji moraju biti označeni, da se provjeri radi li pravilo D. Sintetika ide kroz isti parser (EOJN format) i dobiva očekivane oznake. Integracijski test ispisuje TP/FP/TN/FN i pada na promašaju jasnog slučaja.
- Zašto: stvarni podaci nemaju ground truth, pa bez ovoga ne znamo je li pravilo točno ili samo tiho. Isti skup kasnije služi kao 40 sintetičkih slučajeva u evaluaciji (`MVP_ROADMAP.md`, 06.11.) i za demo.
