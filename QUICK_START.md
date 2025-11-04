# Quick Start Guide

Get ScholarLens running in 5 minutes!

## 🚀 Immediate Build (Without Firebase)

The app is currently configured to build **without** Firebase. You can run it immediately to see the UI:

### Step 1: Sync and Build
```bash
./gradlew build
```

Or in Android Studio:
- Click **"Sync Now"** when prompted
- Wait for Gradle sync to complete

### Step 2: Run the App
- Click the green **Run** button (▶️)
- Or press **Shift + F10**
- Select your emulator or connected device

### What You'll See
✅ App launches successfully  
✅ Bottom navigation with 3 tabs  
✅ Home, Search, Profile screens  
✅ Smooth navigation between tabs  
✅ Material Design 3 theme  

## 🔥 Enable Firebase (5 minutes)

When you're ready to add Firebase functionality:

### Quick Firebase Setup

1. **Go to Firebase Console**: https://console.firebase.google.com/

2. **Create Project**
   - Click "Add project"
   - Name: "ScholarLens"
   - Click through the setup

3. **Add Android App**
   - Click Android icon
   - Package name: `com.example.scholarlens_fe`
   - Download `google-services.json`

4. **Add to Project**
   - Copy `google-services.json` to `app/` folder
   - Location: `ScholarLensFE/app/google-services.json`

5. **Enable Google Services Plugin**
   Open `app/build.gradle.kts` and uncomment line 9:
   ```kotlin
   // BEFORE:
   // id("com.google.gms.google-services")
   
   // AFTER:
   id("com.google.gms.google-services")
   ```

6. **Sync Gradle**
   - Click "Sync Now"
   - Rebuild project

7. **Done!** 🎉

For detailed Firebase setup (Authentication, Firestore, etc.), see [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md)

## 🧪 Test Navigation

1. **Launch app**
2. **Tap Home tab** → See Home screen
3. **Tap Search tab** → See Search screen
4. **Tap Profile tab** → See Profile screen
5. **Tap back button** → App exits (doesn't navigate back through tabs)

This is correct behavior! The navigation is configured to not build up a back stack.

## 📱 What's Included

### ✅ Completed Features
- Bottom Navigation (3 tabs)
- Navigation Component with Compose
- Clean Architecture structure (Data, Domain, Presentation layers)
- Hilt dependency injection setup
- Firebase modules (ready to use after setup)
- Material Design 3 theming
- All strings in resources (no hardcoded text)

### 📂 Project Structure
```
presentation/
  ├── navigation/      # Navigation setup
  ├── screens/         # UI screens
  │   ├── home/
  │   ├── search/
  │   └── profile/
data/
  └── repository/      # Data repositories
domain/
  ├── model/           # Business models
  └── usecase/         # Business logic
di/                    # Dependency injection
```

## 🎯 Next Steps

### Add Your Features

1. **Implement Search Functionality**
   - Edit `SearchScreen.kt`
   - Add search bar and results list

2. **Implement Profile Screen**
   - Edit `ProfileScreen.kt`
   - Add user info display

3. **Add ViewModels**
   ```kotlin
   @HiltViewModel
   class HomeViewModel @Inject constructor(
       private val useCase: YourUseCase
   ) : ViewModel() {
       // Your logic
   }
   ```

4. **Create More Screens**
   - Add to `NavDestinations.kt`
   - Add to `NavGraph.kt`
   - Create screen composable

## 🐛 Troubleshooting

### Issue: Build fails
**Solution**: Check that you've synced Gradle after cloning

### Issue: Hilt errors
**Solution**: 
1. Clean project: **Build → Clean Project**
2. Rebuild: **Build → Rebuild Project**

### Issue: Navigation not working
**Solution**: Make sure you're running the latest code after sync

## 📚 Learn More

- [README.md](README.md) - Full project documentation
- [FIREBASE_SETUP_GUIDE.md](FIREBASE_SETUP_GUIDE.md) - Complete Firebase guide
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)

---

**You're all set! Start building! 🚀**

