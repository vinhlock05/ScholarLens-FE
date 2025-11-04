# Firebase Setup Guide for ScholarLens

This guide will walk you through setting up Firebase for your ScholarLens Android application.

## Prerequisites
- Google account
- Android Studio installed
- ScholarLens project opened in Android Studio

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** or **"Create a project"**
3. Enter project name: **"ScholarLens"** (or your preferred name)
4. Click **Continue**
5. (Optional) Enable Google Analytics - recommended for tracking user engagement
6. If enabled, select or create a Google Analytics account
7. Click **Create project**
8. Wait for project creation to complete, then click **Continue**

## Step 2: Add Android App to Firebase Project

1. In Firebase Console, click the **Android icon** (</>) to add an Android app
2. Fill in the registration form:
   - **Android package name**: `com.example.scholarlens_fe`
     - This MUST match the `applicationId` in your `app/build.gradle.kts`
   - **App nickname** (optional): ScholarLens
   - **Debug signing certificate SHA-1** (optional for now, required later for Auth)
3. Click **Register app**

## Step 3: Download google-services.json

1. Click **Download google-services.json**
2. **IMPORTANT**: Move this file to your project's `app/` directory
   ```
   ScholarLensFE/
   └── app/
       └── google-services.json  ← Place it here
   ```
3. In Android Studio:
   - Switch to **Project** view (not Android view)
   - Copy `google-services.json` into the `app/` folder
   - The file should be at the same level as `build.gradle.kts`

4. Click **Next** in Firebase Console
5. Click **Next** again (we've already added the SDK dependencies)
6. Click **Continue to console**

## Step 4: Enable Firebase Services

### Authentication Setup
1. In Firebase Console, go to **Build** → **Authentication**
2. Click **Get started**
3. Enable sign-in methods you want to use:
   - **Email/Password**: Click on it → Enable toggle → Save
   - **Google**: Click on it → Enable toggle → Enter support email → Save
   - Add others as needed (Facebook, Twitter, etc.)

### Firestore Database Setup (Recommended)
1. Go to **Build** → **Firestore Database**
2. Click **Create database**
3. Select **Start in test mode** (for development)
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.time < timestamp.date(2025, 12, 31);
       }
     }
   }
   ```
4. Choose a location (select closest to your users)
5. Click **Enable**

### Realtime Database Setup (Optional)
1. Go to **Build** → **Realtime Database**
2. Click **Create Database**
3. Choose location
4. Select **Start in test mode**
5. Click **Enable**

### Cloud Storage Setup (Optional, for file uploads)
1. Go to **Build** → **Storage**
2. Click **Get started**
3. Review security rules (Start in test mode for development)
4. Choose storage location
5. Click **Done**

## Step 5: Configure Security Rules (Production)

⚠️ **IMPORTANT**: Before going to production, update your security rules!

### Firestore Rules Example:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Public read, authenticated write
    match /articles/{articleId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### Storage Rules Example:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Step 6: Get SHA-1 Certificate (For Google Sign-In)

If you plan to use Google Authentication:

### For Debug Build:
1. Open terminal in your project directory
2. Run (Windows):
   ```bash
   cd android
   gradlew signingReport
   ```
   Or (Mac/Linux):
   ```bash
   cd android
   ./gradlew signingReport
   ```
3. Look for **SHA-1** under `Variant: debug`
4. Copy the SHA-1 hash

### For Release Build:
You'll need your keystore file's SHA-1. If you don't have one yet:
```bash
keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

Then get SHA-1:
```bash
keytool -list -v -keystore my-release-key.keystore -alias my-key-alias
```

### Add SHA-1 to Firebase:
1. Go to Firebase Console → Project Settings
2. Scroll to **Your apps** section
3. Click your Android app
4. Click **Add fingerprint**
5. Paste your SHA-1
6. Click **Save**
7. **Download the new google-services.json** and replace the old one

## Step 7: Verify Installation

1. **Sync Gradle**: In Android Studio, click **"Sync Now"** or **File** → **Sync Project with Gradle Files**
2. **Rebuild Project**: **Build** → **Rebuild Project**
3. **Run the app**: Click the green play button or press **Shift + F10**

## Step 8: Using Firebase in Your Code

### Initialize Firebase (Already Done)
The app automatically initializes Firebase when you include `google-services.json`.

### Example: Firebase Authentication

Create a repository in the `data` layer:

```kotlin
// data/repository/AuthRepository.kt
package com.example.scholarlens_fe.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
```

### Provide Firebase instances with Hilt:

```kotlin
// di/FirebaseModule.kt
package com.example.scholarlens_fe.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}
```

### Example: Firestore Database Operations

```kotlin
// data/repository/ArticleRepository.kt
package com.example.scholarlens_fe.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ArticleRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val articlesCollection = firestore.collection("articles")

    suspend fun getArticles(): Result<List<Article>> {
        return try {
            val snapshot = articlesCollection.get().await()
            val articles = snapshot.documents.mapNotNull { 
                it.toObject(Article::class.java) 
            }
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addArticle(article: Article): Result<String> {
        return try {
            val docRef = articlesCollection.add(article).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Troubleshooting

### Issue: "google-services.json is missing"
- **Solution**: Make sure `google-services.json` is in the `app/` directory (same level as `app/build.gradle.kts`)

### Issue: "Default FirebaseApp is not initialized"
- **Solution**: 
  1. Ensure `google-services.json` is correctly placed
  2. Sync Gradle and rebuild
  3. Clean and rebuild: **Build** → **Clean Project** → **Rebuild Project**

### Issue: SHA-1 certificate error for Google Sign-In
- **Solution**: Add your debug/release SHA-1 to Firebase Console (see Step 6)

### Issue: "FirebaseApp initialization unsuccessful"
- **Solution**: Check that package name in `google-services.json` matches your `applicationId`

### Issue: Network permission denied
- **Solution**: Already added in `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  ```

## Build Configuration Summary

Your project is already configured with:

✅ Firebase BOM (Bill of Materials) for version management  
✅ Firebase Authentication  
✅ Firebase Storage  
✅ Firebase Realtime Database  
✅ Google Services plugin  
✅ Internet permissions  
✅ Hilt for dependency injection  

## Next Steps

1. ✅ Download and place `google-services.json` in `app/` directory
2. Enable Firebase services you need in Firebase Console
3. Sync and rebuild your project
4. Start implementing Firebase features in your app

## Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase Authentication](https://firebase.google.com/docs/auth/android/start)
- [Cloud Firestore](https://firebase.google.com/docs/firestore/quickstart)
- [Firebase Storage](https://firebase.google.com/docs/storage/android/start)

---

**Need Help?** Check the [Firebase Console](https://console.firebase.google.com/) for your project status and configuration.

