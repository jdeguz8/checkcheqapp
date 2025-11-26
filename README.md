<div align="center">
  <img src="checkcheq_banner.png" alt="CheckCheq Banner" width="100%" />

  
  
  <h1>CheckCheq — Smart Grocery Price Checker</h1>

  <p>
    A modern Android application for tracking grocery prices, managing a shopping basket,
    and (soon) comparing store locations across Winnipeg.
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

## Overview

CheckCheq is an Android app built with Kotlin, Jetpack Compose, Room, and Hilt.  
It focuses on:

- A persistent shopping basket
- Reactive UI driven by local database state
- A clean, modern architecture suitable for both coursework and portfolio use

Future iterations will include store mapping and cloud sync using Firebase and Google Maps.

---

## Features

### Basket Management (Implemented)

- Add items to a basket
- Increase and decrease item quantities
- Remove items from the basket
- Persist data locally with Room Database
- Automatically update the UI via StateFlow and Jetpack Compose

### Jetpack Compose UI

- Material 3 styling
- Reactive, state-driven layout
- Simple and clean presentation of basket items and counts

### Architecture and Dependency Injection

- MVVM pattern
- Repository layer separating data from UI
- Domain models mapped from entities
- Hilt for dependency injection of DAOs, repositories and ViewModels

---

## Roadmap (Planned Features)

### Google Maps Integration

- Long-press to save store locations
- Display a map of saved stores
- Associate prices with specific stores

### Firebase Firestore Sync

- Sync basket and store data to the cloud
- Access data across multiple devices
- Store historical price records for analysis

### Price Tracking

- Integrate with a price API (future)
- Store historical price data
- Compare prices between stores over time

---

## Tech Stack

<div align="center">

| Layer         | Technologies                                                          |
|--------------|------------------------------------------------------------------------|
| Language      | Kotlin                                                                |
| UI           | Jetpack Compose, Material 3                                           |
| Architecture | MVVM, Repository pattern, StateFlow                                   |
| Local Data   | Room Database, DAOs, Entities                                         |
| Async        | Coroutines, Flow                                                      |
| DI           | Hilt                                                                   |
| Remote (future) | Firebase Firestore, Google Maps SDK, REST APIs                    |

</div>

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


