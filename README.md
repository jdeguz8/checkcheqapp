<div align="center">

  <h1>CheckCheq — Crowd-Sourced Price Map for Winnipeg</h1>

  <p>
    An Android app built with Kotlin, Jetpack Compose, Hilt, Room, Firebase, Google Maps, and Yelp.  
    CheckCheq lets people drop pins on a map with real grocery & restaurant prices, see a feed of nearby deals,  
    and get extra context from Yelp — all tied to Firebase authentication.
  </p>

  <p>
    <a href="#features"><strong>Features</strong></a> •
    <a href="#architecture"><strong>Architecture</strong></a> •
    <a href="#data--apis"><strong>Data &amp; APIs</strong></a> •
    <a href="#project-structure"><strong>Structure</strong></a> •
    <a href="#author"><strong>Author</strong></a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/platform-Android-green" alt="Platform: Android" />
    <img src="https://img.shields.io/badge/language-Kotlin-blueviolet" alt="Language: Kotlin" />
    <img src="https://img.shields.io/badge/ui-Jetpack%20Compose-3DDC84" alt="Jetpack Compose" />
    <img src="https://img.shields.io/badge/backend-Firebase-orange" alt="Backend: Firebase" />
    <img src="https://img.shields.io/badge/maps-Google%20Maps-blue" alt="Maps: Google" />
    <img src="https://img.shields.io/badge/docs-Dokka-informational" alt="Docs: Dokka" />
    <img src="https://img.shields.io/badge/license-MIT-lightgrey" alt="License: MIT" />
  </p>
</div>

---

CheckCheq started as a simple “basket” + Room demo and has evolved into a **Winnipeg-focused price sharing app**:

- Map-based posts for grocery / restaurant / cafe / bakery prices  
- Real-time feed powered by **Firestore**  
- **Firebase Auth** (email/password + Google sign-in)  
- Optional **photos** stored in **Firebase Storage**  
- **Yelp integration** on the details screen for ratings & price tier  
- **Settings** for theme, “near me” defaults, category filter, and accessibility tweaks (font scale, etc.)  
- Documented with **KDoc** and **Dokka** to generate API docs

---

## Features

### 1. Authentication

- **Email & password auth** using `FirebaseAuth`
- **Google Sign-In** using `GoogleSignInClient` + Firebase credential sign-in
- `AuthViewModel` exposes an `AuthUiState`:
  - `isSignedIn`, `displayName`, `email`, `errorMessage`, `isLoading`
- `MainActivity` observes `AuthViewModel`:
  - If not signed in → shows `AuthScreen`
  - If signed in → shows the main app (feed / map / settings) with bottom navigation

Authentication is also used to tag each post with:
- `postedBy` (display name or email)
- `ownerUid` (Firebase UID for owner-only edit/delete)

---

### 2. Map & Posts (CRUD)

The core entity is `PricePost`:

- Store / restaurant name  
- Item or dish name  
- Price  
- Latitude / longitude  
- Optional photo (local URI / remote URL)  
- Timestamp (`createdAt`)  
- Category (Grocery, Restaurant, Cafe, Bakery, Fast food, Other)  
- `postedBy` (who added it)  
- `ownerUid` (creator’s Firebase UID)

**Create**

- Long-press on the map **or** search a place via **Google Places**
- Opens `AddPriceDialog` to enter store, item, price, category, and pick a photo
- `MyStoresViewModel.onAddPin()`:
  1. Builds a `PricePost` with `postedBy` and `ownerUid` from the current Firebase user
  2. Saves to **Room** via `PricePostRepository` (local cache)
  3. Uploads photo (if any) to **Firebase Storage**
  4. Writes a document to **Firestore** (`price_posts` collection) with the final cloud photo URL

**Read**

- `MyStoresViewModel.observeRemotePosts()` attaches a real-time Firestore listener on `price_posts`
- Maps documents → `PricePost` and exposes them through `pins: StateFlow<List<PricePost>>`
- `FeedScreen` and `MyStoresScreen` collect `pins` and display:
  - A **feed** of cards (photo, store, item, price, category, posted by, distance)
  - A **map** of pins with store + item as the marker title

**Update**

- On `PricePostDetailsScreen`, if `ownerUid` == current Firebase UID, show **Edit post**
- `EditPostDialog` lets the user update store, item, price, category
- `MyStoresViewModel.updatePost()`:
  - Looks up the Firestore document by `createdAt` (used as a stable ID)
  - Calls `.update()` with new values
  - The Firestore listener pushes updated data back into the UI

**Delete**

- Owner sees a **Delete post** button on `PricePostDetailsScreen`
- `MyStoresViewModel.deletePost()`:
  - Finds the document by `createdAt`
  - Calls `.delete()` in Firestore
  - Post disappears from feed and map once the snapshot updates

---

### 3. Feed & Filters

The **Feed** screen (`FeedScreen`) shows all posts in a vertical list with:

- Category filter chips:
  - All, Grocery, Restaurant, Cafe, Bakery, Fast food, Other
- “Near me” toggle:
  - Uses fused location provider to compute distance to each post
  - Filters posts within a radius in meters (controlled in Settings)
- Distance text:
  - Shows “83 m away” or “1.3 km away” when location is available
- “Signed in as…” label at the top
- Tap card → opens `PricePostDetailsScreen`
- Tap image → opens a full-screen photo dialog

---

### 4. Map Screen

The **Map** screen (`MyStoresScreen`) uses Google Maps Compose:

- Shows pins for each `PricePost` (optionally filtered “near me”)
- Integrates **Google Places**:
  - Search bar with autocomplete predictions
  - Tap a prediction → camera animates to that place and opens `AddPriceDialog` prefilled with the place name
- Long-press on map → `AddPriceDialog` for an arbitrary location
- “Near me” card overlay to restrict visible pins within a radius

---

### 5. Details Screen + Yelp Integration

`PricePostDetailsScreen`:

- Hero image or “No photo” placeholder
- Store + item titles
- Price chip and category chip
- “Posted by …” with date/time
- Distance from user if location is available
- Location section with **Open in Maps** intent (geo URI)
- “Why this post matters” text based on category
- **Owner** section:
  - Edit post
  - Delete post

**Yelp Integration**

- For “restaurant-like” posts (Restaurant, Fast food, Cafe, Bakery):
  - `YelpViewModel` calls `YelpRepository.findBestMatchForPost(post)`
  - `YelpRepository` uses **Retrofit + OkHttp + Moshi** to call:
    - `GET /businesses/search` from the Yelp API
  - Matches businesses by:
    - Store name (case-insensitive)
    - Proximity to the post’s lat/lng
  - Shows a `YelpInfoCard` with:
    - Yelp name
    - Rating + review count
    - Price tier (e.g. $, $$, $$$)
  - Handles loading and error states gracefully in the UI

---

### 6. Settings & Accessibility

The **Settings** screen (`SettingsScreen` + `SettingsViewModel`) exposes:

- **Account**
  - Shows signed-in display name + email
  - Sign-out button (clears FirebaseAuth + Settings state)

- **Appearance**
  - Theme mode:
    - System / Light / Dark, stored in `ThemeMode`
    - Applied in `MainActivity` via `CheckCheqTheme`
  - Text size:
    - Internal `fontScale` options (Default, Large, Extra large)
    - Stacks on top of system font size for better accessibility

- **Distance & feed defaults**
  - “Near me” radius presets (500 m, 1 km, 3 km, 5 km)
  - “Start feed with near me enabled” toggle
  - Default category filter

All of these are persisted using **AndroidX DataStore** via `SettingsRepository`.

---

### 7. Why Hilt?

Hilt is used for **dependency injection** throughout the app:

- `@HiltAndroidApp` on `CheckCheqApp`
- `@AndroidEntryPoint` on `MainActivity` and composable hosts
- `AppModule` provides:
  - `AppDatabase` (Room) + DAOs (`PricePostDao`, etc.)
  - `PricePostRepository` (`RoomPricePostRepository`)
  - `DataStore<Preferences>` for settings
  - Firebase Firestore + Storage
  - Retrofit + OkHttp for Yelp
  - Moshi instance

Benefits:

- UI never manually constructs databases, clients, or repositories
- Clear separation between layers (UI → ViewModel → Repository → Data sources)
- Easier to test and evolve (e.g., swap in a fake repository or a different backend)

---

### 8. Why both Room and Firebase?

The app uses **both** Room and Firebase on purpose:

- **Firestore + Storage**:
  - Cloud source of truth for posts and images
  - Real-time updates across devices
  - Scales easily across users

- **Room (local)**:
  - Local persistence and caching
  - Lets the app evolve towards an offline-first model
  - Provides a clean repository abstraction that doesn’t depend on any single backend

UI screens only depend on `PricePostRepository` and ViewModels, so the storage strategy can change without breaking composables.

---

### 9. Documentation (KDoc + Dokka)

Core classes (`MainActivity`, screens, ViewModels, repositories, etc.) are documented with **KDoc**.

The project includes **Dokka** configured in Gradle:

- Generates HTML API docs from KDoc
- Gives a browsable reference for:
  - Screens and navigation
  - ViewModels and state
  - Data models and repositories

---

## Data & APIs

- **Firebase**
  - Auth (email/password, Google)
  - Firestore (`price_posts` collection)
  - Storage (`images/...`)

- **Google**
  - Maps Compose (`com.google.maps.android:maps-compose`)
  - Play Services Maps & Location
  - Places SDK for place search and autocomplete

- **Yelp**
  - REST API (`/businesses/search`)
  - API key is injected via `BuildConfig.YELP_API_KEY` (from `local.properties`)

---

## Project Structure

```text
app/
 ├─ data/
 │   ├─ local/
 │   │   ├─ dao/         # Room DAOs
 │   │   └─ entity/      # Room entities
 │   ├─ remote/
 │   │   ├─ yelp/        # Yelp API service + models
 │   │   └─ firebase/    # (if separated) Firebase helpers
 │   └─ repository/      # PricePostRepository, SettingsRepository, YelpRepository
 ├─ domain/
 │   ├─ model/           # PricePost, ThemeMode, etc.
 │   └─ ...              # Domain-level abstractions
 ├─ ui/
 │   ├─ screens/         # AuthScreen, FeedScreen, MyStoresScreen, PricePostDetailsScreen, SettingsScreen
 │   ├─ viewmodels/      # AuthViewModel, MyStoresViewModel, SettingsViewModel, YelpViewModel
 │   └─ theme/           # CheckCheqTheme, colors, typography
 ├─ di/
 │   └─ AppModule.kt     # Hilt providers for DB, repos, Retrofit, Firebase, DataStore
 ├─ CheckCheqApp.kt      # @HiltAndroidApp application
 └─ MainActivity.kt      # NavHost, bottom navigation, theme, auth gating
```

### Post freshness & expiry

- Each post stores a `createdAt` timestamp (in milliseconds since epoch).
- The app enforces a **7-day retention window**:
  - On startup, a small cleanup runs against the local Room database to delete posts older than 7 days.
  - The Firestore listener also ignores any document whose `createdAt` is older than the retention window.
- Result: grocery deals automatically disappear from the feed and map after about a week, which better matches how real-world flyer deals expire.

## Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/jdeguz8/checkcheq-app.git
cd checkcheq-app
```

Create local.properties with your keys:

```bash
MAPS_API_KEY=YOUR_GOOGLE_MAPS_KEY
YELP_API_KEY=YOUR_YELP_API_KEY
WEB_CLIENT_ID=YOUR_FIREBASE_WEB_CLIENT_ID
```

Add 

```google-services.json```

Download from your Firebase project

Place it under <br>
```app/google-services.json```

Build & run in Android Studio on a device/emulator with Google Play Services.

Author
<div align="left">

Jonathan De Guzman
Developer (in training) — Winnipeg, MB, Canada

LinkedIn: https://www.linkedin.com/in/jonathan-de-guzman-56585529b/

GitHub: https://github.com/jdeguz8/

</div> ```
