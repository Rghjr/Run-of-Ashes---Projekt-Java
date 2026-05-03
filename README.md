# Run of Ashes

Gra 2D event-driven survival w Javie — wcielasz się w posłańca biegnącego z Azji do Europy, żeby ostrzec o nadchodzącej Czarnej Śmierci. Każdy dzień to nowy event, każda decyzja zmienia twoje szanse na przeżycie.

---

## Wymagania

- Java 17+
- IntelliJ IDEA

---

## Struktura projektu

```
RunOfAshes/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/runofashes/
        │       └── Main.java
        └── resources/
            └── images/
```

---

## Jak odpalić

1. Otwórz projekt w **IntelliJ IDEA**
2. Po prawej stronie kliknij panel **Maven**
3. Rozwiń: `RunOfAshes → Plugins → javafx`
4. Kliknij dwuklik na **`javafx:run`**

---

## Technologie

- Java 17
- JavaFX 21
- Maven

---

## Cel gry

Grasz jako posłaniec, który musi przebiec z Azji do Europy i ostrzec o nadchodzącej Czarnej Śmierci. Każdy dzień biegu to nowy event i nowa decyzja. Musisz zarządzać zasobami, zdrowiem i psychiką — i dotrzeć do celu zanim padniesz.

---

## Mechaniki gry

### Staty gracza

- **Health** — życie; spada od ran, chorób, skrajnego wyczerpania
- **Hunger** — głód; spada pasywnie co turę, przy 0 zaczynasz tracić health
- **Hydration** — nawodnienie; spada szybciej niż hunger, szczególnie w gorących biomach
- **Energy** — energia; spada od akcji i pasywnie, przy 0 padasz z wyczerpania
- **Morale** — psychika; przy 0 poddajesz się i nie idziesz dalej

Każdy stat może spaść do 0 i zakończyć grę z innym powodem śmierci.

---

### Poziomy trudności i cechy (Traits)

Na początku wybierasz poziom trudności, który określa ile i jakich cech otrzymujesz:

- **Easy** → wybierasz 2 dobre cechy
- **Medium** → wybierasz 1 dobrą i 1 złą cechę
- **Hard** → losowo otrzymujesz 2 złe cechy

Łącznie jest 10 cech — 5 dobrych i 5 złych. Każda wpływa globalnie na cały run (modyfikatory do statów, szans, efektów eventów).

---

### System eventów

Każdego dnia losowany jest event — ale nie czysto losowo. Pula eventów jest ważona statami:

- mało Hunger → prawie pewny event głodowy
- mało Hydration → eventy pragnienia
- mało Energy → eventy zmęczenia
- niskie Morale → halucynacje i eventy psychiczne

Efekty akcji są skalowane statami zamiast binarnego sukces/porażka. Przykład: szukasz jedzenia z niską energią → znajdziesz mało albo nic; z wysoką energią → znajdziesz dużo.

---

### Eventy łańcuchowe

Niektóre eventy mają ciąg dalszy w kolejnych dniach. Przykład: pomagasz żołnierzowi → następnego dnia wraca z zapasami. Twoje decyzje mają konsekwencje rozłożone w czasie.

---

### Rare eventy

Bardzo mała szansa wystąpienia. Duży efekt — pozytywny lub negatywny. Mogą całkowicie zmienić stan gry.

---

### Biomy

Trasa podzielona jest na biomy, które zmieniają się w trakcie biegu:

- **Azja Mniejsza** — gorąco, szybszy spadek Hydration, mniej zasobów
- **Góry** — wysoki koszt Energy, trudniejsze eventy
- **Europa** — więcej NPC, inne eventy, łatwiejszy dostęp do zasobów

Każdy biom podmienia pulę dostępnych eventów i modyfikuje efekty akcji.

---

### System czasu

Każda akcja kosztuje czas. Czas wpływa na pasywny spadek statów — im więcej czasu zajmuje akcja, tym więcej tracisz pasywnie. Niektóre itemy zwiększają koszt czasowy kolejnych akcji.

---

### Inventory i itemy

Ekwipunek dostępny jako osobny panel podczas każdego eventu. Możesz w każdej chwili:

- zjeść coś
- wypić coś
- użyć przedmiotu leczącego
- wykonać akcję terenową (szukaj jedzenia / szukaj wody) — skuteczność zależy od biomu i aktualnych statów

**Dostępne itemy:**

- **Woda** — natychmiastowe +Hydration, max 40 jednostek
- **Wino** — +Morale, +Hydration, ale następna akcja kosztuje 1.25x więcej czasu
- **Oliwki** — +Hunger dużo, ale +koszt Energy w następnym evencie
- **Winogrona** — +Hunger, neutralne, bez efektów ubocznych
- **Zioła** — leczą Health powoli przez kilka tur
- **Bandaże** — natychmiastowe +Health
- **Suszone mięso** — +Hunger dużo, ale -Hydration

---

### Losowe statusy

Z małą szansą mogą się aktywować tymczasowe statusy:

- **Odwodnienie** — szybszy spadek wszystkich statów
- **Adrenalina** — lepsza skuteczność akcji przez 2 tury
- **Halucynacje** — losowe efekty akcji
- **Gorączka** — ciągły spadek Health
- **Skurcze** — akcje kosztują więcej Energy
- **Drugi oddech** — chwilowy boost Energy

---

### Pasywny decay

Co turę niezależnie od akcji:

- Hunger spada
- Hydration spada (szybciej w gorących biomach)
- Energy spada (wolniej w spokojnych biomach)
- Aktywne statusy działają
- Efekty itemów over-time działają

---

### NPC i spotkania

Możesz napotkać innych ludzi i wybrać:

- **Pomóż** — koszt zasobów, zysk Morale
- **Okradnij** — zysk zasobów, koszt Morale
- **Ignoruj** — brak efektów

Niektórzy NPC wracają w eventach łańcuchowych.

---

### System pogody

Losowa pogoda wpływa na eventy i staty:

- **Upał** — przyspiesza spadek Hydration
- **Deszcz** — może uzupełnić wodę, ale obniża Morale
- **Burza** — blokuje część akcji

---

### Progresja trudności

Im dalej w trasie:

- eventy trudniejsze
- pasywny decay szybszy
- rare eventy częściej negatywne

---

### Warunki końca

- **Health ≤ 0** → zginąłeś
- **Hunger ≤ 0** → zagłodziłeś się
- **Hydration ≤ 0** → umarłeś z pragnienia
- **Energy ≤ 0** → padłeś z wyczerpania
- **Morale ≤ 0** → poddałeś się psychicznie
- **Dotarłeś do celu** → wygrałeś

---

## Co widzi gracz

### Ekran setup
Wybór trudności, potem ekran wyboru cech z opisem każdej i jej globalnym efektem.

### Główny ekran gry
Dzień, dystans do celu (pasek postępu z zaznaczonymi biomami), pięć pasków statów, aktywne statusy z ikonami, aktualny biom i pogoda.

### Ekran eventu
Tytuł i opis eventu. Tekst dynamicznie zmienia ton zależnie od statów (przy niskim Morale narrator jest bardziej desperacki). 2–3 przyciski głównych akcji z widocznym kosztem czasu i orientacyjnym ryzykiem.

### Panel ekwipunku
Dostępny w każdej chwili podczas eventu jako osobna zakładka. Lista itemów z opisem efektów i przyciskami użycia. Sekcja akcji terenowych (szukaj jedzenia / szukaj wody) z widoczną skutecznością zależną od biomu i statów.

### Ekran efektów po akcji
Animowane zmiany pasków statów. Info o nowym statusie jeśli się aktywował. Zapowiedź chain eventu jeśli się wyzwolił.

### Ekran zmiany biomu / pogody
Krótka notka przy zmianie biomu lub pogody z informacją co się zmienia.

### Ekran game over / win
Powód śmierci z klimatycznym tekstem, albo zakończenie zwycięskie z opisem ile kosztował bieg. Podsumowanie: dni przeżyte, staty końcowe, itemy użyte, cechy i ich wpływ na run.

---

## Status

> 🚧 W trakcie rozwoju
