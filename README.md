# Healthy Scan

Android εφαρμογή για σάρωση barcode προϊόντων τροφίμων και άμεση αξιολόγηση της διατροφικής τους ποιότητας.
Android app for scanning food product barcodes and getting an instant nutritional quality score.

Χτισμένο με Kotlin + Jetpack Compose + Material 3.
Built with Kotlin + Jetpack Compose + Material 3.

---

## Χρώματα & Θέμα / Colors & Theme

Brand χρώματα: `#064E3B` (βαθύ πράσινο) και `#F8E7C9` (κρεμ).
Brand colors: `#064E3B` (deep green) and `#F8E7C9` (cream).

Τα δύο χρώματα αλλάζουν ρόλο ανάλογα με το mode:
- Light mode: κρεμ φόντο, πράσινο accent
- Dark mode: πράσινο φόντο, κρεμ accent

The two colors swap roles depending on the mode:
- Light mode: cream background, green accent
- Dark mode: green background, cream accent

Ορίζονται στο `ui/theme/Color.kt` και `ui/theme/Theme.kt`.
Defined in `ui/theme/Color.kt` and `ui/theme/Theme.kt`.

Υπάρχει κουμπί εναλλαγής γλώσσας (ΕΛ/ΕΝ) σε κάθε βασική οθόνη, μέσω του per-app
language API (`locale/LocaleManager.kt`). Λειτουργεί από API 26 και πάνω.

There's a language toggle button (EL/EN) on every main screen, using the
per-app language API (`locale/LocaleManager.kt`). Works from API 26 upward.

Τα strings υπάρχουν πλήρη σε `values/strings.xml` (Αγγλικά) και
`values-el/strings.xml` (Ελληνικά).

---

## Βασικά χαρακτηριστικά / Core features

- Σάρωση barcode με CameraX + ML Kit (on-device, χωρίς κόστος)
- Barcode scanning with CameraX + ML Kit (on-device, free)
- Αναζήτηση προϊόντος από το [Open Food Facts](https://world.openfoodfacts.org) (δωρεάν API, χωρίς key)
- Product lookup from Open Food Facts (free API, no key needed)
- Health Scoring Engine (0-100) βασισμένο σε ζάχαρη / κορεσμένα / αλάτι / ίνες / πρωτεΐνη, με διαφορετικά thresholds ανά κατηγορία προϊόντος
- Health Scoring Engine (0-100) based on sugar / saturated fat / salt / fiber / protein, with category-specific thresholds
- Προσωποποιημένο score βάσει των προτιμήσεων που δηλώνει ο χρήστης στο onboarding
- Personalized score based on preferences set during onboarding
- Ανάλυση συστατικών, αλλεργιογόνα (με προσωπική προειδοποίηση), πρόσθετα (E-numbers)
- Ingredient breakdown, allergens (with personal warning), additives (E-numbers)
- AI Label Scanner: φωτογράφιση ετικέτας + OCR (ML Kit Text Recognition)
- AI Label Scanner: photograph the label + OCR (ML Kit Text Recognition)
- Ιστορικό, Αγαπημένα, Καλάθι (Room database, offline-first)
- History, Favorites, Basket (Room database, offline-first)
- Onboarding με τις προτιμήσεις διατροφής
- Onboarding with dietary preferences
- Premium οθόνη (Free vs Premium comparison)
- Premium screen (Free vs Premium comparison)
- App icon και splash screen από το logo που δόθηκε
- App icon and splash screen built from the provided logo

---

## Άνοιγμα στο Android Studio / Opening in Android Studio

1. Άνοιξε το Android Studio → **Open** → επίλεξε τον φάκελο `HealthyScan`.
2. Το Gradle wrapper δεν είναι μέσα στο zip. Το Android Studio θα το φτιάξει
   μόνο του στο πρώτο sync (πάτα OK όταν το ζητήσει). Αν θέλεις να το κάνεις
   χειροκίνητα:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
3. Περίμενε να τελειώσει το sync (κατεβάζει AGP, Compose, CameraX, ML Kit,
   Retrofit, Room από το Maven — χρειάζεται internet).
4. Run σε συσκευή ή emulator με camera.

1. Open Android Studio → **Open** → select the `HealthyScan` folder.
2. The Gradle wrapper isn't included in the zip. Android Studio will offer to
   generate it on first sync (click OK). Or do it manually:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
3. Wait for sync to finish (pulls AGP, Compose, CameraX, ML Kit, Retrofit,
   Room from Maven — needs internet).
4. Run on a device or emulator with a camera.

---

## Build

```bash
# Debug APK
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Release AAB (αυτό ανεβαίνει στο Play Console / this is what you upload to Play Console)
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

### Signing

Δεν βάζω κανένα password στο repo. Το `app/build.gradle.kts` τα διαβάζει από
environment variables:

I don't put any passwords in the repo. `app/build.gradle.kts` reads them from
environment variables:

```bash
export HEALTHYSCAN_KEYSTORE_PATH=/full/path/to/upload-keystore.jks
export HEALTHYSCAN_KEYSTORE_PASSWORD=xxxxx
export HEALTHYSCAN_KEY_ALIAS=upload
export HEALTHYSCAN_KEY_PASSWORD=xxxxx

./gradlew bundleRelease
```

Δημιουργία νέου keystore, αν δεν έχεις ήδη / creating a new keystore if you don't have one:

```bash
keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 \
  -validity 9125 -alias upload
```

---

## Target API (Play Console)

`compileSdk` και `targetSdk` είναι στο 36 (Android 16), γιατί από 31/8/2026 το
Google Play απαιτεί κάθε νέα εφαρμογή ή update να στοχεύει το API level 36 ή
νεότερο για να γίνει δεκτό στο Play Console.

`compileSdk` and `targetSdk` are set to 36 (Android 16), because as of
August 31, 2026 Google Play requires every new app or update to target API
level 36 or higher to be accepted in Play Console.

Είναι ήδη ρυθμισμένο στο `app/build.gradle.kts`, δεν χρειάζεται τίποτα άλλο.

Already configured in `app/build.gradle.kts`, nothing else needed.

---

## Git / GitHub

```bash
cd HealthyScan
git init
git add .
git commit -m "Initial Healthy Scan Android app"

git branch -M main
git remote add origin https://github.com/<username>/healthy-scan.git
git push -u origin main
```

Με GitHub CLI / with GitHub CLI:

```bash
gh repo create healthy-scan --private --source=. --remote=origin --push
```

Το `.gitignore` αποκλείει `local.properties`, keystores και τους φακέλους
`build/`. Μην κάνεις commit πραγματικά passwords ή API keys.

`.gitignore` already excludes `local.properties`, keystores and `build/`
folders. Never commit real passwords or API keys.

---

## Δομή project / Project structure

```
HealthyScan/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/healthyscan/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── HealthyScanApp.kt
│   │   │   ├── data/
│   │   │   │   ├── model/        # Product, NutritionFacts, HealthScoreResult
│   │   │   │   ├── remote/       # Open Food Facts API + mapper
│   │   │   │   ├── local/        # Room entities/DAOs/DB
│   │   │   │   └── repository/   # ProductRepository, SettingsRepository
│   │   │   ├── scoring/          # HealthScoreEngine, ExplanationGenerator
│   │   │   ├── locale/           # LocaleManager (EL/EN toggle)
│   │   │   └── ui/
│   │   │       ├── theme/        # Color.kt, Theme.kt
│   │   │       ├── navigation/   # NavGraph.kt, Screen.kt
│   │   │       ├── components/   # BottomBar, TopBar, ScoreRing, ScorePill
│   │   │       └── screens/      # onboarding, home, scan, product, history,
│   │   │                         # favorites, profile, premium
│   │   └── res/
│   │       ├── values/strings.xml       (English)
│   │       ├── values-el/strings.xml    (Greek)
│   │       ├── values(-night)/themes.xml
│   │       ├── mipmap-*/                (launcher icons)
│   │       └── drawable/splash_logo.png
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Πηγή δεδομένων / Data source

Χρησιμοποιείται το Open Food Facts (`https://world.openfoodfacts.org`), δωρεάν,
χωρίς API key.

Uses Open Food Facts (`https://world.openfoodfacts.org`), free, no API key.
