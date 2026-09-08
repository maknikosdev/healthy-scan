# Healthy Scan

Android εφαρμογή για σάρωση barcode προϊόντων τροφίμων και άμεση αξιολόγηση της διατροφικής τους ποιότητας.

Χτισμένο με Kotlin + Jetpack Compose + Material 3.

---

## Ελληνικά

### Χρώματα & Θέμα

Brand χρώματα: `#064E3B` (βαθύ πράσινο) και `#F8E7C9` (κρεμ).

Τα δύο χρώματα αλλάζουν ρόλο ανάλογα με το mode:
- Light mode: κρεμ φόντο, πράσινο accent
- Dark mode: πράσινο φόντο, κρεμ accent

Ορίζονται στο `ui/theme/Color.kt` και `ui/theme/Theme.kt`.

Υπάρχει κουμπί εναλλαγής γλώσσας (ΕΛ/ΕΝ) σε κάθε βασική οθόνη, μέσω του per-app
language API (`locale/LocaleManager.kt`). Λειτουργεί από API 26 και πάνω. Σε
συσκευές με API < 33 η αλλαγή γλώσσας αναγκάζει πλήρες recreate του Activity —
για να μην φαίνεται σαν απότομο μαύρισμα της οθόνης, υπάρχει ένα ήπιο fade
transition ορισμένο στο `res/anim/fade_in.xml` / `fade_out.xml` και
συνδεδεμένο μέσω `windowAnimationStyle` στο `res/values/themes.xml`.

Τα strings υπάρχουν πλήρη σε `values/strings.xml` (Αγγλικά) και
`values-el/strings.xml` (Ελληνικά).

### Βασικά χαρακτηριστικά

- Σάρωση barcode με CameraX + ML Kit (on-device, χωρίς κόστος)
- Αναζήτηση προϊόντος από το Open Food Facts, με δωρεάν fallback στο UPCitemdb + δυνατότητα συνεισφοράς προϊόντος (βλ. ενότητα παρακάτω)
- Health Scoring Engine (0-100) βασισμένο σε ζάχαρη / κορεσμένα / αλάτι / ίνες / πρωτεΐνη, με διαφορετικά thresholds ανά κατηγορία προϊόντος
- Προσωποποιημένο score βάσει των προτιμήσεων που δηλώνει ο χρήστης στο onboarding
- Ανάλυση συστατικών, αλλεργιογόνα (με προσωπική προειδοποίηση βάσει των περιορισμών που έχει ορίσει ο χρήστης), πρόσθετα (E-numbers)
- Ιστορικό (με δυνατότητα διαγραφής ανά scan), Αγαπημένα, Καλάθι (Room database, offline-first)
- **Εξαγωγή/Εισαγωγή δεδομένων σε JSON** — μεταφορά ιστορικού, αγαπημένων, καλαθιού και προτιμήσεων σε άλλη συσκευή (βλ. ενότητα παρακάτω)
- Onboarding με διατροφικές προτιμήσεις και περιορισμούς/αλλεργιογόνα προς αποφυγή — επεξεργάσιμα οποτεδήποτε
- Premium οθόνη
- App icon και splash screen από το logo που δόθηκε (κρατά τουλάχιστον ~1.2 δευτερόλεπτα στην οθόνη· δείχνει το **πλήρες logo, χωρίς μάσκα** — βλ. σημείωση παρακάτω)

### Splash screen — γιατί δεν είναι μέσα στο ενσωματωμένο "εικονίδιο" του συστήματος

Από το Android 12 (API 31) και πάνω, το πλατφορμικό SplashScreen API **πάντα**
κόβει το εικονίδιο του splash σε κύκλο/squircle — δεν υπάρχει καμία ρύθμιση
theme που να το απενεργοποιεί πλήρως. Αντί να παλεύουμε με αυτό, το OS splash
είναι σκόπιμα άδειο (μόνο το brand background, βλ.
`res/drawable/splash_placeholder.xml` + `themes.xml`), και αμέσως μετά η
`MainActivity` δείχνει το δικό της Compose splash (`SplashContent()`) με το
**ολόκληρο logo**, χωρίς καμία μάσκα, για τουλάχιστον 1.2 δευτερόλεπτα, πριν
περάσει στην κανονική εφαρμογή.

### Προτιμήσεις & περιορισμοί διατροφής (επεξεργάσιμα οποτεδήποτε)

Στο πρώτο άνοιγμα της εφαρμογής (onboarding), ο χρήστης επιλέγει:
1. **Διατροφικούς στόχους** (λιγότερη ζάχαρη, vegan, χωρίς γλουτένη κ.λπ.) —
   επηρεάζουν το προσωποποιημένο Health Score.
2. **Τι πρέπει να αποφεύγει** (γαλακτοκομικά, γλουτένη, ξηροί καρποί, σόγια
   κ.λπ.) — αν ένα σκαναρισμένο προϊόν περιέχει κάτι από αυτά, εμφανίζεται
   προειδοποίηση στην οθόνη αποτελέσματος (ενότητα "Αλλεργιογόνα").

Και τα δύο **επεξεργάζονται οποτεδήποτε** από το Προφίλ → "Επεξεργασία
προτιμήσεων" (`ui/screens/preferences/EditPreferencesScreen.kt`), χωρίς να
χρειάζεται να ξαναπεράσει κανείς από το onboarding.

### Άνοιγμα ήδη-σκαναρισμένου προϊόντος δεν δημιουργεί διπλή εγγραφή

Όταν πατάς πάνω σε ένα προϊόν μέσα από το **Ιστορικό**, τα **Αγαπημένα**, ή τη
λίστα "τελευταία scans" στην **Αρχική**, η εφαρμογή το ανοίγει από την ήδη
αποθηκευμένη (cached) έκδοση — **όχι** σαν νέο barcode scan. Μόνο μια
πραγματική σάρωση με την κάμερα (ή επιλογή από αναζήτηση) προσθέτει νέα
εγγραφή στο ιστορικό. Υλοποίηση: `ProductRepository.openCachedOrLookup()` +
το route param `record` στο `Screen.ProductResult`.

### Πηγές δεδομένων προϊόντος — συνδυασμός δωρεάν πηγών

Για την πληρέστερη δυνατή ανάλυση κάθε σαρωμένου προϊόντος, χωρίς κανένα κόστος:

1. **Open Food Facts** (κύρια πηγή) — αν το barcode υπάρχει εκεί, παίρνεις πλήρη
   ανάλυση: συστατικά, διατροφικές τιμές, αλλεργιογόνα, πρόσθετα, Health Score.
2. **UPCitemdb** (δωρεάν "trial" tier, ~100 lookups/ημέρα, χωρίς API key) —
   ενεργοποιείται αυτόματα σε δύο περιπτώσεις:
   - Όταν το Open Food Facts δεν έχει καν ακούσει για το barcode: η εφαρμογή
     δείχνει τουλάχιστον όνομα/μάρκα/φωτογραφία αντί για εντελώς άδεια οθόνη
     (οθόνη "Βρέθηκαν βασικά στοιχεία" — χωρίς Health Score, γιατί δεν
     υπάρχουν πουθενά πραγματικά διατροφικά δεδομένα να υπολογιστεί σωστά).
   - Όταν το Open Food Facts έχει τα διατροφικά στοιχεία αλλά λείπει η
     φωτογραφία προϊόντος — συμπληρώνεται αυτόματα.
3. **Συνεισφορά πίσω στο Open Food Facts** (οθόνη "Πρόσθεσε το προϊόν") — ο
   χρήστης μπορεί να στείλει όνομα/μάρκα/συστατικά κατευθείαν στο Open Food
   Facts, μέσω ενός **δωρεάν για πάντα** λογαριασμού
   ([δημιουργία εδώ](https://world.openfoodfacts.org/cgi/user.pl)). Αυτό
   βελτιώνει μόνιμα την κάλυψη για όλους — όχι μόνο για τον συγκεκριμένο
   χρήστη — και είναι υλοποιημένο στο `ProductRepository.submitProductToOpenFoodFacts()`.

Ο συνδυασμός αυτός γίνεται αυτόματα στο `ProductRepository.lookupByBarcode()` —
δεν χρειάζεται καμία ενέργεια από τον χρήστη πέρα από τη σάρωση.

### Backup / μεταφορά σε άλλη συσκευή

Στο Προφίλ υπάρχει ενότητα **"Αντίγραφο ασφαλείας & μεταφορά"** με δύο κουμπιά:

- **Εξαγωγή σε αρχείο** — δημιουργεί ένα `healthyscan_backup.json` με το
  ιστορικό, τα αγαπημένα, το καλάθι και τις προτιμήσεις (θέμα, γλωσσικές
  προτιμήσεις διατροφής, avoided allergens). Χρησιμοποιεί το native Android
  file picker (Storage Access Framework) — ο χρήστης επιλέγει ο ίδιος πού θα
  αποθηκευτεί (π.χ. Downloads, Google Drive), χωρίς να χρειάζεται κανένα
  ειδικό permission.
- **Εισαγωγή από αρχείο** — διαβάζει ένα τέτοιο αρχείο (από αυτή ή άλλη
  συσκευή) και ενσωματώνει τα δεδομένα: αγαπημένα/καλάθι γίνονται merge
  (χωρίς διπλότυπα, matched by barcode), ιστορικό προστίθεται σαν νέες
  εγγραφές.
- Ο **κωδικός** του λογαριασμού Open Food Facts **δεν συμπεριλαμβάνεται
  ποτέ** στο αρχείο (μόνο το username) — μετά την εισαγωγή σε νέα συσκευή,
  θα χρειαστεί να τον ξαναβάλεις μία φορά στο "Πρόσθεσε το προϊόν".

Υλοποίηση: `data/backup/BackupManager.kt`.

### Άνοιγμα στο Android Studio

1. Άνοιξε το Android Studio → **Open** → επίλεξε τον φάκελο `HealthyScan`.
2. Το Gradle wrapper δεν είναι μέσα στο zip. Το Android Studio θα το φτιάξει
   μόνο του στο πρώτο sync (πάτα OK όταν το ζητήσει). Αν θέλεις να το κάνεις
   χειροκίνητα:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
3. Περίμενε να τελειώσει το sync (κατεβάζει AGP, Compose, CameraX, ML Kit,
   Retrofit, Room από το Maven — χρειάζεται internet). Αν βλέπεις "Gradle
   JDK" λάθος έκδοση Java (π.χ. σφάλμα "Unsupported class file major
   version"), άλλαξέ το σε **File ▸ Settings ▸ Build, Execution, Deployment ▸
   Build Tools ▸ Gradle ▸ Gradle JDK** στο ενσωματωμένο `jbr` του Android
   Studio.
4. Run σε συσκευή ή emulator με camera.

### Build

```bash
# Debug APK
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Release AAB (αυτό ανεβαίνει στο Play Console)
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

### Target API (Play Console)

`compileSdk` και `targetSdk` είναι στο 36 (Android 16), γιατί από 31/8/2026 το
Google Play απαιτεί κάθε νέα εφαρμογή ή update να στοχεύει το API level 36 ή
νεότερο για να γίνει δεκτό στο Play Console.

Είναι ήδη ρυθμισμένο στο `app/build.gradle.kts`, δεν χρειάζεται τίποτα άλλο.

### Δομή project

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
│   │   │   │   ├── remote/       # Open Food Facts API, UPCitemdb API + mappers
│   │   │   │   ├── local/        # Room entities/DAOs/DB
│   │   │   │   ├── backup/       # BackupManager.kt (JSON export/import)
│   │   │   │   └── repository/   # ProductRepository, SettingsRepository
│   │   │   ├── scoring/          # HealthScoreEngine, ExplanationGenerator
│   │   │   ├── locale/           # LocaleManager (EL/EN toggle)
│   │   │   └── ui/
│   │   │       ├── theme/        # Color.kt, Theme.kt
│   │   │       ├── navigation/   # NavGraph.kt, Screen.kt
│   │   │       ├── components/   # BottomBar, TopBar, ScoreRing, ScorePill
│   │   │       └── screens/      # onboarding, home, scan, product, history,
│   │   │                         # favorites, profile, preferences, premium,
│   │   │                         # addproduct
│   │   └── res/
│   │       ├── values/strings.xml       (English)
│   │       ├── values-el/strings.xml    (Greek)
│   │       ├── values(-night)/themes.xml
│   │       ├── anim/fade_in.xml, fade_out.xml   (language-switch transition)
│   │       ├── mipmap-*/                (launcher icons)
│   │       └── drawable/splash_logo.png
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### Πηγή δεδομένων

Χρησιμοποιείται το Open Food Facts (`https://world.openfoodfacts.org`), δωρεάν,
χωρίς API key, με UPCitemdb ως fallback (βλ. ενότητα πιο πάνω).

---

## English

### Colors & Theme

Brand colors: `#064E3B` (deep green) and `#F8E7C9` (cream).

The two colors swap roles depending on the mode:
- Light mode: cream background, green accent
- Dark mode: green background, cream accent

Defined in `ui/theme/Color.kt` and `ui/theme/Theme.kt`.

There's a language toggle button (EL/EN) on every main screen, using the
per-app language API (`locale/LocaleManager.kt`). Works from API 26 upward.
On API < 33 devices, switching language forces a full Activity recreate — to
avoid it looking like an abrupt black flash, a gentle fade transition is
defined in `res/anim/fade_in.xml` / `fade_out.xml` and wired up via
`windowAnimationStyle` in `res/values/themes.xml`.

Full strings live in `values/strings.xml` (English) and `values-el/strings.xml`
(Greek).

### Core features

- Barcode scanning with CameraX + ML Kit (on-device, free)
- Product lookup from Open Food Facts, with a free UPCitemdb fallback + product contribution flow (see section below)
- Health Scoring Engine (0-100) based on sugar / saturated fat / salt / fiber / protein, with category-specific thresholds
- Personalized score based on preferences set during onboarding
- Ingredient breakdown, allergens (with a personal warning based on restrictions the user has set), additives (E-numbers)
- History (with per-item delete), Favorites, Basket (Room database, offline-first)
- **Export/Import data as JSON** — transfer history, favorites, basket and preferences to another device (see section below)
- Onboarding with dietary preferences and allergens/restrictions to avoid — editable anytime
- Premium screen
- App icon and splash screen built from the provided logo (stays on screen at least ~1.2 second; shows the **full, unmasked logo** — see note below)

### Splash screen — why it isn't the platform's built-in "icon" slot

On Android 12 (API 31) and up, the platform SplashScreen API **always** crops
the splash icon into a circle/squircle — there's no theme setting that
disables that. Instead of fighting it, the OS splash is intentionally left
blank (just the brand background, see `res/drawable/splash_placeholder.xml` +
`themes.xml`), and right after, `MainActivity` shows its own Compose splash
(`SplashContent()`) with the **full logo**, no masking, for at least 1.2
seconds, before switching to the real app.

### Dietary preferences & restrictions (editable anytime)

On first launch (onboarding), the person picks:
1. **Dietary goals** (less sugar, vegan, gluten-free, etc.) — feed into the
   personalized Health Score.
2. **What to avoid** (dairy, gluten, tree nuts, soy, etc.) — if a scanned
   product contains one of these, a warning shows up on the result screen
   (the "Allergens" section).

Both are **editable anytime** from Profile → "Edit preferences"
(`ui/screens/preferences/EditPreferencesScreen.kt`), no need to go back
through onboarding.

### Reopening an already-scanned product doesn't create a duplicate

Tapping a product from **History**, **Favorites**, or the "recent scans"
list on **Home** opens it from the already-saved (cached) copy — **not** as
a new barcode scan. Only an actual camera scan (or picking a result from
search) adds a new History row. Implementation:
`ProductRepository.openCachedOrLookup()` + the `record` route parameter on
`Screen.ProductResult`.

### Product data sources — combining free sources

To get the most complete possible analysis for every scanned product, at zero cost:

1. **Open Food Facts** (primary source) — if the barcode exists there, you
   get the full picture: ingredients, nutrition facts, allergens, additives,
   Health Score.
2. **UPCitemdb** (free "trial" tier, ~100 lookups/day, no API key) — kicks in
   automatically in two situations:
   - When Open Food Facts has never heard of the barcode: the app shows at
     least name/brand/photo instead of a completely empty screen (a "Basic
     info found" screen — no Health Score, because there's genuinely no real
     nutrition data anywhere to compute one from).
   - When Open Food Facts has the nutrition data but is missing a product
     photo — it gets filled in automatically.
3. **Contributing back to Open Food Facts** (the "Add this product" screen) —
   the user can send name/brand/ingredients directly to Open Food Facts,
   through a **free forever** account
   ([create one here](https://world.openfoodfacts.org/cgi/user.pl)). This
   permanently improves coverage for everyone, not just that user, and is
   implemented in `ProductRepository.submitProductToOpenFoodFacts()`.

This combination happens automatically inside
`ProductRepository.lookupByBarcode()` — no action needed from the user beyond
scanning.

### Backup / transfer to another device

Profile now has a **"Backup & transfer"** section with two buttons:

- **Export to file** — creates a `healthyscan_backup.json` with history,
  favorites, basket, and preferences (theme, dietary preferences, avoided
  allergens). Uses Android's native file picker (Storage Access Framework) —
  the person chooses where to save it (Downloads, Google Drive, etc.), no
  special storage permission needed.
- **Import from file** — reads such a file (from this device or another one)
  and merges the data in: favorites/basket are matched by barcode (no
  duplicates), history entries get appended as new rows.
- The Open Food Facts **password is never included** in the file (only the
  username) — after importing on a new device, you'll need to re-enter it
  once in "Add this product".

Implementation: `data/backup/BackupManager.kt`.

### Opening in Android Studio

1. Open Android Studio → **Open** → select the `HealthyScan` folder.
2. The Gradle wrapper isn't included in the zip. Android Studio will offer to
   generate it on first sync (click OK). Or do it manually:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
3. Wait for sync to finish (pulls AGP, Compose, CameraX, ML Kit, Retrofit,
   Room from Maven — needs internet). If you see a wrong Java version error
   ("Unsupported class file major version"), change it under **File ▸
   Settings ▸ Build, Execution, Deployment ▸ Build Tools ▸ Gradle ▸ Gradle
   JDK** to Android Studio's bundled `jbr`.
4. Run on a device or emulator with a camera.

### Build

```bash
# Debug APK
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk

# Release AAB (this is what you upload to Play Console)
./gradlew bundleRelease
# → app/build/outputs/bundle/release/app-release.aab
```

### Target API (Play Console)

`compileSdk` and `targetSdk` are set to 36 (Android 16), because as of
August 31, 2026 Google Play requires every new app or update to target API
level 36 or higher to be accepted in Play Console.

Already configured in `app/build.gradle.kts`, nothing else needed.

### Project structure

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
│   │   │   │   ├── remote/       # Open Food Facts API, UPCitemdb API + mappers
│   │   │   │   ├── local/        # Room entities/DAOs/DB
│   │   │   │   ├── backup/       # BackupManager.kt (JSON export/import)
│   │   │   │   └── repository/   # ProductRepository, SettingsRepository
│   │   │   ├── scoring/          # HealthScoreEngine, ExplanationGenerator
│   │   │   ├── locale/           # LocaleManager (EL/EN toggle)
│   │   │   └── ui/
│   │   │       ├── theme/        # Color.kt, Theme.kt
│   │   │       ├── navigation/   # NavGraph.kt, Screen.kt
│   │   │       ├── components/   # BottomBar, TopBar, ScoreRing, ScorePill
│   │   │       └── screens/      # onboarding, home, scan, product, history,
│   │   │                         # favorites, profile, preferences, premium,
│   │   │                         # addproduct
│   │   └── res/
│   │       ├── values/strings.xml       (English)
│   │       ├── values-el/strings.xml    (Greek)
│   │       ├── values(-night)/themes.xml
│   │       ├── anim/fade_in.xml, fade_out.xml   (language-switch transition)
│   │       ├── mipmap-*/                (launcher icons)
│   │       └── drawable/splash_logo.png
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### Data source

Uses Open Food Facts (`https://world.openfoodfacts.org`), free, no API key,
with UPCitemdb as a fallback (see section above).
