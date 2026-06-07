# Run of Ashes

> *Bieg przeciwko Czarnej Śmierci*

Wcielasz się w posłańca, którego misją jest przebiegnięcie z Azji do Europy i ostrzeżenie świata przed nadchodzącą pandemią. To event-driven survival w stylu roguelike — każda tura to nowa karta do wybrania, każda decyzja odbija się echem przez kilka następnych dni.

---

## Jak uruchomić

**Wymagania:** Java 17, IntelliJ IDEA

1. Otwórz projekt w IntelliJ IDEA
2. Otwórz panel **Maven** po prawej stronie i kliknij **Sync** (pobiera zależności)
3. Otwórz `src/main/java/com/runofashes/ui/Main.java`
4. Kliknij zielony trójkąt ▶ przy klasie lub metodzie `main`

---

## Technologie

- Java 17 / JavaFX 17.0.10
- Maven
- Jackson (JSON event system)
- Ikonli (ikony FontAwesome w UI)

---

## Cel gry

Pokonaj 4000 km z Azji Mniejszej do Krakowa zarządzając pięcioma statystykami. Trasa podzielona jest na trzy etapy z osobną pulą eventów i modyfikatorami każdego biomu. Dotarcie do 0 km = wygrana. Wyzerowanie dowolnego statu = koniec biegu.

### Warunki przegranej

| Stat | Efekt przy 0 |
|---|---|
| Health | Zginąłeś od ran lub choroby |
| Hunger | Zagłodziłeś się |
| Hydration | Umarłeś z pragnienia |
| Energy | Padłeś z wyczerpania |
| Morale | Poddałeś się psychicznie |

---

## Mechaniki

### System kart

Co turę losowane są 4 karty z ważonej puli. Waga każdej kategorii zależy od aktualnych statów:

- mało Hunger → więcej kart jedzenia
- mało Hydration → więcej kart wody
- niskie Morale → więcej eventów psychicznych
- waga kart ruchu rośnie w środku trasy i opada przy starcie i mecie (krzywa paraboliczna)

System anti-repeat pamięta ostatnie kilka tur i nie powtarza tych samych eventów. Jeśli pula jest zbyt mała na pełną deduplication, automatycznie odpuszcza filtr — gracz nigdy nie zostanie z mniej niż 4 kartami.

### Wynik akcji

Każda karta ma trzy możliwe wyniki: **Sukces**, **Częściowy sukces**, **Porażka**. Szansa na sukces zależy od aktualnych statów — gracz z pełnymi statami osiąga ok. 65% sukces, 25% częściowy, 10% porażka. Przy wyczerpanych statach proporcje odwracają się.

### Sprint — Push Your Luck
3 ruchy z rzędu (eventy z distanceCost != 0) wyzwalają bonus -50 km i resetują licznik. Każdy event bez dystansu (odpoczynek, jedzenie, quest) zeruje serię.

### Progresja trudności

Po przekroczeniu połowy trasy (dystans < 2000 km) aktywuje się utrudnienie. Ujemne efekty na Hunger, Hydration i Energy są skalowane mnożnikiem rosnącym liniowo:

| Dystans | Mnożnik |
|---|---|
| 2000 km (próg) | ×1.00 |
| 1500 km | ×1.10 |
| 1000 km | ×1.20 |
| 500 km | ×1.30 |
| 0 km (meta) | ×1.40 |

### Questy łańcuchowe

66 etapów questów ogólnych (33 questów, dostępnych przez całą trasę) i 30 etapów questów etapowych (20 questów, aktywnych tylko w konkretnym biomie). Każdy quest ma 2–3 etapy rozłożone w czasie — decyzja z dnia 1 wraca jako konsekwencja kilka tur później. Część questów wzajemnie się wyklucza.
### Biomy i pogoda

Trzy etapy trasy ze swoimi modyfikatorami:

| Etap | Dystans | Charakterystyka |
|---|---|---|
| Azja Mniejsza | 4000–2600 km | Szybszy spadek Hydration, mniej zasobów |
| Góry | 2600–1400 km | Wysoki koszt Energy, trudniejsze eventy |
| Europa | 1400–0 km | Więcej NPC, łatwiejszy dostęp do zasobów |

Pogoda losuje się niezależnie i nakłada własne modyfikatory na staty i dostępne eventy.

### Rare eventy

7 unikalnych eventów z bardzo niską wagą bazową. Każdy ma wariant sukcesu (duży bonus) i porażki (znacząca kara). Mogą pojawić się w dowolnym momencie trasy.

### Statusy tymczasowe

Z losową szansą aktywują się efekty per-tura:

| Status | Efekt |
|---|---|
| Odwodnienie | Szybszy spadek wszystkich statów |
| Gorączka | Ciągły spadek Health i Energy |
| Skurcze | Akcje kosztują więcej Energy |
| Halucynacje | Losowe efekty akcji (przez `applyHallucinations`) |
| Adrenalina | Lepsza skuteczność przez 2 tury |
| Drugi oddech | Chwilowy boost Energy i Morale |

### Cechy (Traits)

Wybierane na początku, działają przez cały run jako globalne modyfikatory:

- **Easy** → wybierasz 2 pozytywne cechy
- **Medium** → 1 pozytywna + 1 negatywna
- **Hard** → losowo 2 negatywne

10 cech (5 pozytywnych, 5 negatywnych): Zahartowany, Zbieracz, Pielgrzym, Wędrowiec, Dyplomata, Pesymista, Żarłok, Nerwowy, Samotnik, Asceta.

### Ekwipunek i itemy

Panel ekwipunku dostępny w każdej chwili podczas tury. Dostępne przedmioty:

| Item | Efekt |
|---|---|
| Woda | +Hydration natychmiastowo |
| Wino | +Morale, +Hydration, ale kolejna akcja kosztuje 1.25× więcej czasu |
| Oliwki | +Hunger (dużo), ale +koszt Energy w następnym evencie |
| Winogrona | +Hunger, brak efektów ubocznych |
| Zioła | +Health przez kilka tur (over-time) |
| Bandaże | +Health natychmiastowo |
| Suszone mięso | +Hunger (dużo), -Hydration |

### System czasu

Każda akcja kosztuje czas w godzinach. Pasywny decay statów skaluje się z kosztem czasowym — dłuższa akcja = więcej strat pasywnych. Niektóre itemy (np. wino) zwiększają koszt czasowy kolejnej akcji. Pora dnia filtruje pulę dostępnych eventów.

---

## Struktura projektu

```
src/main/java/com/runofashes/
├── engine/
│   ├── GameEngine.java          # główna logika
│   ├── CardDrawer.java          # ważona pula kart, anti-repeat
│   ├── EffectApplicator.java    # aplikowanie efektów z biome/diff/late-game mult
│   ├── LateGamePressure.java    # progresja trudności po 50% trasy
│   ├── EventResolver.java       # wynik: SUCCESS / PARTIAL / FAIL
│   ├── QuestTracker.java        # śledzenie questów łańcuchowych
│   ├── StatusManager.java       # statusy tymczasowe (fever, cramps...)
│   └── ... 
├── model/
│   ├── Player.java
│   ├── GameEvent.java
│   ├── StatusEffect.java / ItemType.java
│   ├── GameState.java           # save/load
│   └── ...
├── ui/                          # JavaFX screens i panele
│   ├── Main.java 
│   └── ...
└── utils/                       # EventLoader, FileLoader
    └── ...

src/main/resources/com/runofashes/
├── events_food.json             # 27 eventów
├── events_hydration.json        # 22 eventy
├── events_energy.json           # 19 eventów
├── events_morale.json           # 23 eventy
├── events_move.json             # 22 eventy
├── events_rare.json             # 7 eventów
├── events_quests.json           # 66 etapów (33 questów łańcuchowych)
├── events_stages_quests.json    # 30 etapów (20 questów etapowych)
└── events_choice_quests.json    # 7 questów z jawnym wyborem opcji
```

---

## Testy

```
src/test/java/com/runofashes/
├── PlayerTest.java
├── StatusManagerTest.java
├── BiomeTest.java
├── CardDrawerRareWeightTest.java 
├── CardDrawerMoveWeightTest.java   
├── CardDrawerRecentIdsTest.java
└── ...  
```

**Jedna klasa — IntelliJ:**
Otwórz plik z testem → kliknij zielony trójkąt ▶ przy nazwie klasy (uruchamia wszystkie testy w klasie) lub przy konkretnej metodzie `@Test` (uruchamia tylko ten jeden test).

**Wszystkie testy naraz — terminal:**
```
mvn test
```

---

## Autorzy

Krystian Strzępek<br>
Bartłomiej Zięcina<br>
Martyna Tuszewska

---

*W trakcie rozwoju*