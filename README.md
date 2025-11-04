# ScholarLens - Android Application

A modern Android application for scholarly research built with Jetpack Compose and Clean Architecture.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with three main layers:

```
app/
├── data/           # Data layer (repositories, data sources)
├── domain/         # Domain layer (use cases, business logic)
└── presentation/   # Presentation layer (UI, ViewModels)
    ├── navigation/ # Navigation setup
    └── screens/    # UI screens
        ├── home/
        ├── search/
        └── profile/
```

## 🚀 Tech Stack

- **UI Framework**: Jetpack Compose
- **Dependency Injection**: Hilt/Dagger
- **Navigation**: Navigation Compose
- **Backend**: Firebase
  - Authentication
  - Firestore Database
  - Realtime Database
  - Cloud Storage
- **Architecture**: Clean Architecture (Data, Domain, Presentation)
- **Language**: Kotlin

## 📱 Features

- ✅ Bottom Navigation with 3 tabs (Home, Search, Profile)
- ✅ Modern Material Design 3 UI
- ✅ Clean Architecture implementation
- ✅ Firebase integration ready
- ✅ Hilt dependency injection
- ✅ Navigation Component with Compose

## 🛠️ Setup Instructions

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd ScholarLensFE
```

### 2. Open in Android Studio
- Open Android Studio
- Select "Open an Existing Project"
- Navigate to the project directory
- Wait for Gradle sync to complete

### 3. Firebase Setup (Required)
**⚠️ IMPORTANT**: You must set up Firebase before running the app.

Follow the detailed guide in **[FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md)**

Quick steps:
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app with package name: `com.example.scholarlens_fe`
3. Download `google-services.json`
4. Place it in the `app/` directory
5. Uncomment the Google Services plugin in `app/build.gradle.kts`:
   ```kotlin
   // Remove the comment from this line:
   id("com.google.gms.google-services")
   ```
6. Sync Gradle

### 4. Build and Run
```bash
# Sync Gradle
./gradlew build

# Run on emulator or device
./gradlew installDebug
```

Or use Android Studio's **Run** button (Shift + F10)

## 📂 Project Structure

```
ScholarLensFE/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/scholarlens_fe/
│   │   │   ├── data/
│   │   │   │   └── repository/
│   │   │   │       └── AuthRepository.kt
│   │   │   ├── di/
│   │   │   │   └── FirebaseModule.kt
│   │   │   ├── domain/
│   │   │   ├── presentation/
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── NavDestinations.kt
│   │   │   │   │   └── NavGraph.kt
│   │   │   │   └── screens/
│   │   │   │       ├── home/
│   │   │   │       │   └── HomeScreen.kt
│   │   │   │       ├── search/
│   │   │   │       │   └── SearchScreen.kt
│   │   │   │       └── profile/
│   │   │   │           └── ProfileScreen.kt
│   │   │   ├── ui/theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   ├── MainActivity.kt
│   │   │   └── ScholarLensApplication.kt
│   │   ├── res/
│   │   │   └── values/
│   │   │       └── strings.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json (YOU NEED TO ADD THIS)
├── build.gradle.kts
├── FIREBASE_SETUP_GUIDE.md
└── README.md
```

## 🎨 Screens

### Home Screen
- Main landing page
- Welcome message and content

### Search Screen
- Search functionality for scholarly articles
- (To be implemented)

### Profile Screen
- User profile and settings
- (To be implemented)

## 🔧 Development

### Adding a New Screen

1. Create screen composable in `presentation/screens/[screen_name]/`
2. Add destination to `NavDestinations.kt`
3. Add route to `NavGraph.kt`
4. Update navigation as needed

### Using Firebase Services

Firebase instances are provided via Hilt. Inject them in your repositories:

```kotlin
class YourRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Your implementation
}
```

See `AuthRepository.kt` for a complete example.

### Dependencies

All dependencies are managed in:
- `build.gradle.kts` (project level)
- `app/build.gradle.kts` (app level)
- `gradle/libs.versions.toml` (version catalog)

## 📝 Requirements Met

✅ MainActivity with BottomNavigationView  
✅ Navigation Component integration  
✅ 3 fragments/screens: Home, Search, Profile  
✅ Proper navigation without crashes  
✅ Individual layouts for each screen  
✅ Smooth navigation without stack issues  
✅ No hardcoded strings (all in strings.xml)  
✅ Kotlin code conventions followed  
✅ Clean Architecture structure  
✅ Jetpack Compose UI  
✅ Firebase setup ready  
✅ Hilt dependency injection  

## 🐛 Troubleshooting

### Build Error: "google-services.json is missing"
- Follow the [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md) to set up Firebase
- Make sure `google-services.json` is in the `app/` directory
- Uncomment `id("com.google.gms.google-services")` in `app/build.gradle.kts`

### Navigation not working
- Make sure all destinations are defined in `NavGraph.kt`
- Check that routes in `NavDestinations.kt` match those in `NavGraph.kt`

### Hilt errors
- Ensure `@HiltAndroidApp` is on the Application class
- Ensure `@AndroidEntryPoint` is on Activities/Fragments
- Clean and rebuild: `./gradlew clean build`

## 📚 Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Firebase for Android](https://firebase.google.com/docs/android/setup)
- [Clean Architecture Guide](https://developer.android.com/topic/architecture)

## 📄 License

[Your License Here]

## 👥 Contributors

[Your Name/Team]

---

**Happy Coding! 🚀**

