# Run of Ashes

Prosta gra 2D w Javie z grafikami, animacjami i systemem wyborów — zbudowana na JavaFX.

---

## Wymagania

- Java 17+
- Maven (wbudowany w IntelliJ)

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

### Opcja 1 — przez IntelliJ (najprościej)

1. Otwórz projekt w IntelliJ IDEA
2. Po prawej stronie kliknij panel **Maven**
3. Rozwiń: `RunOfAshes → Plugins → javafx`
4. Kliknij dwuklik na **`javafx:run`**

### Opcja 2 — przez terminal (jeśli Maven jest w PATH)

```bash
mvn javafx:run
```

### Opcja 3 — dodanie Maven do PATH (Windows, raz na zawsze)

1. W IntelliJ: **File → Settings → Build, Execution, Deployment → Build Tools → Maven**
2. Skopiuj wartość pola **Maven home path** (np. `C:\Users\kryst\.m2\wrapper\dists\apache-maven-X.X.X\bin`)
3. Windows → wyszukaj **"Zmienne środowiskowe"**
4. **Path → Edytuj → Nowy** → wklej ścieżkę
5. Kliknij OK i zrestartuj terminal

---

## Technologie

- Java 17
- JavaFX 21
- Maven

---

## Status

> 🚧 W trakcie rozwoju
