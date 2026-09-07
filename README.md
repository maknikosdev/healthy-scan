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
- Ανάλυση συστατικών, αλλεργιογόνα (με προσωπική προειδοποίηση), πρόσθετα (E-numbers)
- **AI Label Scanner**: φωτογράφιση ετικέτας + OCR εξ ολοκλήρου offline με **Tesseract** (βλ. ενότητα παρακάτω)
- Ιστορικό (με δυνατότητα διαγραφής ανά scan), Αγαπημένα, Καλάθι (Room database, offline-first)
- Onboarding με τις προτιμήσεις διατροφής
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
   χρήστης μπορεί να στείλει όνομα/μάρκα/συστατικά (π.χ. αυτά που διάβασε το
   Tesseract OCR από την ετικέτα) κατευθείαν στο Open Food Facts, μέσω ενός
   **δωρεάν για πάντα** λογαριασμού
   ([δημιουργία εδώ](https://world.openfoodfacts.org/cgi/user.pl)). Αυτό
   βελτιώνει μόνιμα την κάλυψη για όλους — όχι μόνο για τον συγκεκριμένο
   χρήστη — και είναι υλοποιημένο στο `ProductRepository.submitProductToOpenFoodFacts()`.

Ο συνδυασμός αυτός γίνεται αυτόματα στο `ProductRepository.lookupByBarcode()` —
δεν χρειάζεται καμία ενέργεια από τον χρήστη πέρα από τη σάρωση.

### Σάρωση ετικέτας (OCR) — Tesseract αντί για ML Kit

Το ML Kit Text Recognition **δεν έχει καθόλου μοντέλο ελληνικού αλφαβήτου**
(υποστηρίζει μόνο Latin, Κινέζικα, Ιαπωνικά, Κορεατικά, Devanagari) — γι' αυτό
παρερμήνευε ελληνικό κείμενο σε τυχαία λατινικά σύμβολα. Αντικαταστάθηκε με το
**Tesseract OCR** (μέσω της βιβλιοθήκης
[Tesseract4Android](https://github.com/adaptech-cz/Tesseract4Android)), που:

- Τρέχει **εξ ολοκλήρου μέσα στη συσκευή**, καμία κλήση δικτύου
- Είναι **δωρεάν για πάντα**, καμία χρέωση ανά σάρωση, κανένα Google Cloud API key
- **Έχει πραγματικό μοντέλο ελληνικών** (`ell.traineddata`) μαζί με αγγλικά (`eng.traineddata`)

Τα δύο αρχεία γλώσσας (~1.4MB + ~4MB, "fast" variant) είναι ήδη μέσα στο
project, στο `app/src/main/assets/tessdata/`. Η πρώτη φορά που ο χρήστης
χρησιμοποιεί το "Σάρωση Ετικέτας", η εφαρμογή τα αντιγράφει αυτόματα από τα
assets στον ιδιωτικό φάκελο της εφαρμογής (`ocr/TesseractOcrHelper.kt`) —
καμία ενέργεια χρειάζεται από τον χρήστη.

**Οδηγίες σωστής φωτογράφισης για καλύτερη ακρίβεια:**
1. Φωτογράφισε **μόνο την παράγραφο των συστατικών**, όχι ολόκληρη την
   ετικέτα — αν υπάρχει δίπλα πίνακας διατροφικών τιμών σε άλλη στήλη, το OCR
   μπερδεύει τις δύο στήλες.
2. Κράτησε την ετικέτα **όσο πιο ίσια/επίπεδη** γίνεται μέσα στο κάδρο —
   καμπυλωτές επιφάνειες (βάζα, μπουκάλια) μειώνουν σημαντικά την ακρίβεια.
3. **Καλός, ομοιόμορφος φωτισμός**, χωρίς αντανακλάσεις πάνω στο πλαστικό/γυαλί.
4. Γέμισε το πλαίσιο-οδηγό που εμφανίζεται στην οθόνη με το κείμενο, χωρίς
   περιττό περιθώριο γύρω του.
5. Σε δίγλωσσες ετικέτες (Ελληνικά + Αγγλικά), λειτουργεί καλύτερα αν
   φωτογραφίσεις ένα μόνο γλωσσικό μπλοκ κάθε φορά.

**Σημαντικός περιορισμός να το ξέρεις:** ακόμα και με σωστό μοντέλο γλώσσας,
το Tesseract είναι αισθητά λιγότερο ακριβές από επί πληρωμή cloud λύσεις
(π.χ. Google Cloud Vision) σε δύσκολες φωτογραφίες — καμπυλωτές ετικέτες,
μικρή γραμματοσειρά, κακός φωτισμός. Αν στο μέλλον χρειαστείς καλύτερη
ακρίβεια και είσαι ok με μηνιαίο κόστος + ανάγκη για δικό σου backend
(για να μην εκτεθεί το API key μέσα στην εφαρμογή), η εναλλακτική λύση με
Cloud Vision παραμένει διαθέσιμη ως αναβάθμιση.

### Άνοιγμα στο Android Studio

1. Άνοιξε το Android Studio → **Open** → επίλεξε τον φάκελο `HealthyScan`.
2. Το Gradle wrapper δεν είναι μέσα στο zip. Το Android Studio θα το φτιάξει
   μόνο του στο πρώτο sync (πάτα OK όταν το ζητήσει). Αν θέλεις να το κάνεις
   χειροκίνητα:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
3. Περίμενε να τελειώσει το sync (κατεβάζει AGP, Compose, CameraX, ML Kit,
   Tesseract4Android (από JitPack), Retrofit, Room από το Maven — χρειάζεται
   internet). Αν βλέπεις "Gradle JDK" λάθος έκδοση Java (π.χ. σφάλμα
   "Unsupported class file major version"), άλλαξέ το σε **File ▸ Settings ▸
   Build, Execution, Deployment ▸ Build Tools ▸ Gradle ▸ Gradle JDK** στο
   ενσωματωμένο `jbr` του Android Studio.
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
│   │   ├── assets/
│   │   │   └── tessdata/           # ell.traineddata + eng.traineddata (Tesseract OCR)
│   │   ├── java/com/healthyscan/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── HealthyScanApp.kt
│   │   │   ├── data/
│   │   │   │   ├── model/        # Product, NutritionFacts, HealthScoreResult
│   │   │   │   ├── remote/       # Open Food Facts API + mapper
│   │   │   │   ├── local/        # Room entities/DAOs/DB
│   │   │   │   └── repository/   # ProductRepository, SettingsRepository
│   │   │   ├── ocr/               # TesseractOcrHelper.kt
│   │   │   ├── scoring/          # HealthScoreEngine, ExplanationGenerator
│   │   │   │                     # (data/remote: OpenFoodFactsApi, UpcItemDbApi, mappers)
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
│   │       ├── anim/fade_in.xml, fade_out.xml   (language-switch transition)
│   │       ├── mipmap-*/                (launcher icons)
│   │       └── drawable/splash_logo.png
├── build.gradle.kts
├── settings.gradle.kts             (includes JitPack repository for Tesseract4Android)
└── gradle.properties
```

### Πηγή δεδομένων

Χρησιμοποιείται το Open Food Facts (`https://world.openfoodfacts.org`), δωρεάν,
χωρίς API key.

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
- Ingredient breakdown, allergens (with personal warning), additives (E-numbers)
- **AI Label Scanner**: photograph the label + fully offline OCR with **Tesseract** (see section below)
- History (with per-item delete), Favorites, Basket (Room database, offline-first)
- Onboarding with dietary preferences
- Premium screen
- App icon and splash screen built from the provided logo (stays on screen at least ~1.2 second; shows the **full, unmasked logo** — see note below)

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
   the user can send name/brand/ingredients (e.g. what Tesseract OCR read off
   the label) directly to Open Food Facts, through a **free forever** account
   ([create one here](https://world.openfoodfacts.org/cgi/user.pl)). This
   permanently improves coverage for everyone, not just that user, and is
   implemented in `ProductRepository.submitProductToOpenFoodFacts()`.

This combination happens automatically inside
`ProductRepository.lookupByBarcode()` — no action needed from the user beyond
scanning.

### Label scanning (OCR) — Tesseract instead of ML Kit

ML Kit's Text Recognition **has no Greek-script model at all** (it only
supports Latin, Chinese, Japanese, Korean, Devanagari) — that's why it was
misreading Greek text as random Latin-looking characters. It was replaced
with **Tesseract OCR** (via the
[Tesseract4Android](https://github.com/adaptech-cz/Tesseract4Android)
library), which:

- Runs **entirely on-device**, no network call
- Is **free forever**, no per-scan charge, no Google Cloud API key
- **Has a real Greek model** (`ell.traineddata`) alongside English (`eng.traineddata`)

Both language files (~1.4MB + ~4MB, "fast" variant) already ship inside the
project, in `app/src/main/assets/tessdata/`. The first time someone uses
"Scan Label", the app copies them from assets into its private storage
(`ocr/TesseractOcrHelper.kt`) — no action needed from the user.

**Tips for accurate photos:**
1. Photograph **only the ingredients paragraph**, not the whole label — if a
   nutrition-facts table sits in a column right next to it, the OCR mixes
   the two columns together.
2. Keep the label **as flat/straight as possible** inside the frame — curved
   surfaces (jars, bottles) noticeably reduce accuracy.
3. **Good, even lighting**, avoiding glare on plastic/glass.
4. Fill the on-screen guide frame with the text, without excess margin
   around it.
5. On bilingual labels (Greek + English), it works best if you photograph
   one language block at a time.

**Important limitation to be aware of:** even with the right language model,
Tesseract is noticeably less accurate than paid cloud solutions (e.g. Google
Cloud Vision) on difficult photos — curved labels, small print, poor
lighting. If you later need better accuracy and are fine with a monthly cost
plus needing your own backend (so the API key isn't exposed inside the app),
the Cloud Vision alternative remains available as an upgrade path.

### Opening in Android Studio

1. Open Android Studio → **Open** → select the `HealthyScan` folder.
2. The Gradle wrapper isn't included in the zip. Android Studio will offer to
   generate it on first sync (click OK). Or do it manually:
   ```bash
   gradle wrapper --gradle-version 8.9
   ```
3. Wait for sync to finish (pulls AGP, Compose, CameraX, ML Kit,
   Tesseract4Android (from JitPack), Retrofit, Room from Maven — needs
   internet). If you see a wrong Java version error ("Unsupported class file
   major version"), change it under **File ▸ Settings ▸ Build, Execution,
   Deployment ▸ Build Tools ▸ Gradle ▸ Gradle JDK** to Android Studio's
   bundled `jbr`.
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
│   │   ├── assets/
│   │   │   └── tessdata/           # ell.traineddata + eng.traineddata (Tesseract OCR)
│   │   ├── java/com/healthyscan/app/
│   │   │   ├── MainActivity.kt
│   │   │   ├── HealthyScanApp.kt
│   │   │   ├── data/
│   │   │   │   ├── model/        # Product, NutritionFacts, HealthScoreResult
│   │   │   │   ├── remote/       # Open Food Facts API + mapper
│   │   │   │   ├── local/        # Room entities/DAOs/DB
│   │   │   │   └── repository/   # ProductRepository, SettingsRepository
│   │   │   ├── ocr/               # TesseractOcrHelper.kt
│   │   │   ├── scoring/          # HealthScoreEngine, ExplanationGenerator
│   │   │   │                     # (data/remote: OpenFoodFactsApi, UpcItemDbApi, mappers)
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
│   │       ├── anim/fade_in.xml, fade_out.xml   (language-switch transition)
│   │       ├── mipmap-*/                (launcher icons)
│   │       └── drawable/splash_logo.png
├── build.gradle.kts
├── settings.gradle.kts             (includes JitPack repository for Tesseract4Android)
└── gradle.properties
```

### Data source

Uses Open Food Facts (`https://world.openfoodfacts.org`), free, no API key.
