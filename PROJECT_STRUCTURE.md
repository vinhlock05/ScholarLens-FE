# ScholarLens Project Structure

## 📁 Complete Directory Tree

```
ScholarLensFE/
│
├── 📄 README.md                           # Main project documentation
├── 📄 QUICK_START.md                      # 5-minute quick start guide
├── 📄 FIREBASE_SETUP_GUIDE.md             # Detailed Firebase setup
├── 📄 PROJECT_STRUCTURE.md                # This file
│
├── app/
│   ├── 📄 build.gradle.kts                # App-level build configuration
│   ├── 📄 google-services.json            # ⚠️ YOU NEED TO ADD THIS
│   │
│   └── src/main/
│       ├── 📄 AndroidManifest.xml         # App manifest with permissions
│       │
│       ├── java/com/example/scholarlens_fe/
│       │   │
│       │   ├── 📱 MainActivity.kt         # Main entry point with bottom nav
│       │   ├── 🎯 ScholarLensApplication.kt # Hilt application class
│       │   │
│       │   ├── 📊 data/                   # DATA LAYER
│       │   │   └── repository/
│       │   │       └── AuthRepository.kt   # Firebase Auth operations
│       │   │
│       │   ├── 💼 domain/                 # DOMAIN LAYER (Business Logic)
│       │   │   ├── model/
│       │   │   │   └── User.kt            # User domain model
│       │   │   └── usecase/
│       │   │       └── GetCurrentUserUseCase.kt # Example use case
│       │   │
│       │   ├── 🎨 presentation/           # PRESENTATION LAYER (UI)
│       │   │   ├── navigation/
│       │   │   │   ├── NavDestinations.kt  # Screen destinations
│       │   │   │   └── NavGraph.kt         # Navigation graph
│       │   │   │
│       │   │   └── screens/
│       │   │       ├── home/
│       │   │       │   └── HomeScreen.kt   # Home tab content
│       │   │       ├── search/
│       │   │       │   └── SearchScreen.kt # Search tab content
│       │   │       └── profile/
│       │   │           └── ProfileScreen.kt # Profile tab content
│       │   │
│       │   ├── 💉 di/                     # DEPENDENCY INJECTION
│       │   │   └── FirebaseModule.kt       # Firebase DI module
│       │   │
│       │   └── 🎨 ui/theme/               # THEMING
│       │       ├── Color.kt                # Color definitions
│       │       ├── Theme.kt                # App theme
│       │       └── Type.kt                 # Typography
│       │
│       └── res/
│           ├── drawable/                   # Icons and drawables
│           ├── mipmap-*/                   # App launcher icons
│           └── values/
│               ├── colors.xml              # Color resources
│               ├── strings.xml             # String resources ✅
│               └── themes.xml              # XML themes
│
├── 📄 build.gradle.kts                    # Project-level build config
├── 📄 settings.gradle.kts                 # Project settings
│
└── gradle/
    ├── libs.versions.toml                 # Version catalog
    └── wrapper/
        └── gradle-wrapper.properties      # Gradle wrapper config
```

## 🏗️ Architecture Layers Explained

### 1️⃣ Presentation Layer (`presentation/`)
**What**: UI and user interaction  
**Contains**:
- Composable screens
- ViewModels (to be added)
- Navigation setup
- UI state management

**Files**:
- `MainActivity.kt` - Entry point, bottom navigation
- `screens/*/` - Individual screen composables
- `navigation/` - Navigation configuration

### 2️⃣ Domain Layer (`domain/`)
**What**: Business logic and rules  
**Contains**:
- Use cases (business operations)
- Domain models (pure business objects)
- Repository interfaces (contracts)

**Files**:
- `model/User.kt` - Domain user model
- `usecase/GetCurrentUserUseCase.kt` - Example use case

**Why**: Keep business logic independent of frameworks

### 3️⃣ Data Layer (`data/`)
**What**: Data sources and repositories  
**Contains**:
- Repository implementations
- Firebase/API calls
- Local database operations
- Data models/DTOs

**Files**:
- `repository/AuthRepository.kt` - Firebase authentication

**Why**: Separate data access from business logic

### 4️⃣ Dependency Injection (`di/`)
**What**: Hilt modules for DI  
**Contains**:
- Module definitions
- Providers for dependencies

**Files**:
- `FirebaseModule.kt` - Provides Firebase instances

**Why**: Decoupled, testable, maintainable code

## 📱 Navigation Flow

```
MainActivity (Bottom Navigation)
    │
    ├─── Home Tab
    │      └─── HomeScreen.kt
    │
    ├─── Search Tab
    │      └─── SearchScreen.kt
    │
    └─── Profile Tab
           └─── ProfileScreen.kt
```

**Navigation Behavior**:
- ✅ Single top (no duplicate screens)
- ✅ State saving/restoration
- ✅ No back stack accumulation
- ✅ Smooth transitions

## 🔧 Configuration Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` (root) | Project-level build config, plugins |
| `app/build.gradle.kts` | App dependencies, SDK versions |
| `libs.versions.toml` | Centralized version management |
| `AndroidManifest.xml` | App permissions, components |
| `strings.xml` | All UI text (no hardcoding!) |

## 🔥 Firebase Integration Points

### Current Setup:
```kotlin
di/FirebaseModule.kt
    ├── FirebaseAuth ────► AuthRepository
    ├── FirebaseFirestore ────► (Future repositories)
    ├── FirebaseDatabase ────► (Future repositories)
    └── FirebaseStorage ────► (Future repositories)
```

### Ready to Use:
1. **FirebaseAuth** - User authentication
2. **FirebaseFirestore** - NoSQL database
3. **FirebaseDatabase** - Realtime database
4. **FirebaseStorage** - File storage

See `data/repository/AuthRepository.kt` for usage example.

## 🎯 Key Files to Understand

### Must Read:
1. **MainActivity.kt** - App entry point, navigation setup
2. **NavGraph.kt** - Screen routing configuration
3. **NavDestinations.kt** - Available screens/routes
4. **FirebaseModule.kt** - DI configuration

### When Adding Features:
1. **Create screen** in `presentation/screens/[feature]/`
2. **Add use case** in `domain/usecase/`
3. **Add repository** in `data/repository/`
4. **Register in** `NavGraph.kt` and `NavDestinations.kt`

## 📦 Dependencies

### Core:
- Jetpack Compose (UI)
- Navigation Compose (Navigation)
- Hilt (Dependency Injection)
- Material 3 (Design system)

### Firebase:
- Firebase BOM (version management)
- Firebase Auth
- Firebase Firestore
- Firebase Realtime Database
- Firebase Storage

### Utilities:
- Coil (Image loading)
- Retrofit (HTTP client)
- OkHttp (Logging)

## 🚀 Getting Started Checklist

- [x] Project structure created
- [x] Clean architecture implemented
- [x] Bottom navigation working
- [x] All three screens created
- [x] Hilt setup complete
- [x] Firebase modules ready
- [ ] **Add google-services.json** ← YOUR NEXT STEP
- [ ] Enable Firebase services
- [ ] Implement features

## 📚 Documentation Files

1. **README.md** - Overview, setup, troubleshooting
2. **QUICK_START.md** - Get running in 5 minutes
3. **FIREBASE_SETUP_GUIDE.md** - Step-by-step Firebase setup
4. **PROJECT_STRUCTURE.md** - This file

---

**Start with**: QUICK_START.md → Build the app  
**Then follow**: FIREBASE_SETUP_GUIDE.md → Add Firebase  
**Reference**: README.md → When you need help  

