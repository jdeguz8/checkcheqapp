<div align="center">
  <img src="checkcheq_banner.png" alt="CheckCheq Banner" width="100%" />

  
  
  <h1>CheckCheq — Smart Grocery Price Checker</h1>

  <p>
 An Android app built with Kotlin, Jetpack Compose, Room, and Hilt.  
    Recently enhanced with a corrected DI setup, improved Basket features,  
    and new architecture patterns in preparation for Firebase + photo uploads.
  </p>

  <p>
    <a href="#features"><strong>Features</strong></a> •
    <a href="#tech-stack"><strong>Tech Stack</strong></a> •
    <a href="#project-structure"><strong>Structure</strong></a> •
    <a href="#installation"><strong>Installation</strong></a> •
    <a href="#author"><strong>Author</strong></a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/platform-Android-green" alt="Platform: Android" />
    <img src="https://img.shields.io/badge/language-Kotlin-blueviolet" alt="Language: Kotlin" />
    <img src="https://img.shields.io/badge/ui-Jetpack%20Compose-3DDC84" alt="Jetpack Compose" />
    <img src="https://img.shields.io/badge/license-MIT-lightgrey" alt="License: MIT" />
  </p>
</div>

<hr/>

CheckCheq is a Winnipeg-focused grocery comparison and basket-tracking app.  
It began with a simple Compose UI + Room DB setup, and has now grown into a more robust architecture with:

- Fully reactive basket management  
- Clean DI through Hilt  
- Modularized repository layer  
- Fixed ApiService injection (Hilt @Provides fix)  
- Updated Basket UI with quantity controls and deletion  

The next step in the project is **Firebase Firestore sync + user photo uploads**, which will allow:
- Store data sync in the cloud  
- User-submitted price photos  
- Cross-device functionality

---

## Features

### Basket Management (Updated)

This section was recently upgraded to support:

- Add items to basket  
- Increase / decrease quantity  
- Remove items  
- Automatic persistence through Room  
- UI updates in real time using StateFlow  
- Repository-driven logic (no business logic in UI)

### Compose UI Improvements

- Cleaner Basket rows  
- Proper state hoisting  
- Separation of concerns  
- List rendering via LazyColumn  
- Text fields + buttons for rapid input
### Architecture and Dependency Injection

- MVVM pattern
- Repository layer separating data from UI
- Domain models mapped from entities
- Hilt for dependency injection of DAOs, repositories and ViewModels

---


---

Author
<div align="left">

Jonathan De Guzman
Android Developer (in training) — Winnipeg, MB, Canada

LinkedIn: https://www.linkedin.com/in/jonathan-de-guzman-56585529b/

GitHub: https://github.com/jdeguz8/

</div>

## Project Structure

```text
app/
 ├─ data/
 │   ├─ local/
 │   │   ├─ dao/
 │   │   └─ entity/
 │   ├─ remote/
 │   └─ repository/
 ├─ domain/
 │   └─ model/
 ├─ ui/
 │   ├─ screens/
 │   ├─ components/
 │   └─ theme/
 └─ di/


