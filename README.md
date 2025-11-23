# EcoFood - Sustainable & Fresh

**EcoFood** is a modern Android application designed to combat food waste through smart inventory management, AI-powered recipe generation, and a community-driven food marketplace. Built entirely with Jetpack Compose and modern Android architecture, this app helps users make the most of their food, save money, and contribute to a more sustainable lifestyle.

## Core Features

### 1. Smart Pantry Management
- **Track Your Groceries:** Easily add items to your digital pantry, including purchase and expiry dates, quantity, and units.
- **Expiry Date Notifications:** The app uses `WorkManager` to run a daily background check and sends you a notification when items in your pantry are about to expire.
- **Smart Filtering:** The inventory screen provides quick filters to view all items, items that are "Expiring Soon," or items that have already "Expired."

### 2. AI-Powered Recipe Generation
- **AI Recipe Generator (Pantry-Aware):** Have ingredients but don't know what to make? This feature takes a list of your available ingredients, cross-references them with your pantry, and generates a detailed recipe.
- **Smart Shopping List:** The generated recipe clearly indicates which ingredients you have and which you are missing. A "Shopping List" button instantly shows you a list of only the items you need to buy.
- **Leftover Magic:** A separate, creative AI tool designed to transform your *cooked leftovers* into exciting new dishes. Describe what you have (e.g., "leftover roasted chicken"), and get instant ideas.

### 3. Community Food Marketplace
- **Share, Don't Waste:** Users can post their surplus food items to a community marketplace, complete with photos (placeholder), price, location, and expiry date.
- **Browse Local Listings:** Discover what others in your community are sharing, creating a local network for reducing food waste.
- **Manage Your Listings:** A dedicated "My Listings" screen allows users to see and manage all the items they have personally posted.

### 4. User Authentication & Profiles
- **Secure Sign-Up & Login:** The app uses Firebase Authentication for a secure and reliable user authentication system.
- **Personalized Experience:** User profiles are integrated with the marketplace, so every listing is tied to a seller.
- **Profile Management:** A dedicated profile screen displays the user's information and provides a simple way to log out.

## Technical Implementation & Architecture

- **100% Kotlin & Jetpack Compose:** The entire UI is built with Jetpack Compose, using a modern, declarative approach.
- **MVVM Architecture:** The app follows the Model-View-ViewModel (MVVM) pattern, ensuring a clean separation of concerns between the UI and the business logic.
- **Android Room Database:** A local Room database is used for persisting the user's pantry items and the marketplace listings, providing full offline support.
- **Firebase Authentication:** Manages all user sign-up, login, and authentication state.
- **Jetpack Navigation for Compose:** A single-activity architecture is used, with all navigation handled by the Jetpack Navigation component.
- **Shared ViewModels:** Data is shared between related screens using shared ViewModels, which is a robust and efficient architectural pattern.
- **Kotlin Coroutines & Flow:** All asynchronous operations, such as database access and AI generation, are handled using Kotlin Coroutines and Flow for a responsive, non-blocking user experience.
- **WorkManager:** Used for scheduling reliable, daily background tasks to check for expiring food items and send notifications.

## How to Run the Project

1.  **Clone the Repository:**
    ```bash
    git clone <https://github.com/bipinsubedi4/Ecofood-Android>
    ```
2.  **Open in Android Studio:** Open the cloned directory in the latest version of Android Studio.
3.  **Add Firebase Configuration:**
    - This project uses Firebase for authentication. To run the app, you will need to create your own Firebase project and add your `google-services.json` file.
    - Go to the [Firebase Console](https://console.firebase.google.com/).
    - Create a new project.
    - Add an Android app to the project with the package name `com.bipin080.ecofood`.
    - Download the `google-services.json` file and place it in the `app/` directory of this project.
4.  **Build & Run:** Sync the Gradle files and run the app on an emulator or a physical device.
