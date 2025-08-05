# 📖 My Journal - Personal Journaling App

A modern, feature-rich Android journaling application that allows users to create, manage, and share their personal journal entries with optional image attachments.

<img width="1080" height="1080" alt="Journal App" src="https://github.com/user-attachments/assets/cafb7b9f-cea4-4fe2-9774-26a6f0385551" />

## ✨ Features

### 🔐 Authentication & Security
- **Firebase Authentication** - Secure email/password login and registration
- **User Session Management** - Automatic login state persistence
- **Secure Data Storage** - All journal entries are user-specific and private

### 📝 Journal Management
- **Create Journal Entries** - Write personal thoughts and experiences
- **Rich Text Support** - Multi-line content with proper formatting
- **Image Attachments** - Add photos to your journal entries
- **Automatic Timestamps** - Each entry is automatically dated and timed
- **Edit & Share** - Modify entries and share them with others

### 🎨 User Interface
- **Modern Material Design** - Clean, intuitive interface following Google's design guidelines
- **Banner-Style Layout** - Images displayed as beautiful banners at the top of entries
- **Responsive Design** - Optimized for various screen sizes

## 🛠 Technology Stack

### Frontend
- **Kotlin** - Modern Android development language
- **Android SDK** - Native Android development framework

### Backend & Services
- **Firebase Authentication** - User authentication and session management
- **Cloud Firestore** - NoSQL cloud database for journal entries
- **Firebase Storage** - Cloud storage for image uploads
- **Firebase BoM** - Dependency management for Firebase services

### Libraries & Dependencies
- **Glide** - Efficient image loading and caching
- **AndroidX** - Modern Android support libraries
- **Material Components** - Material Design UI components
- **ConstraintLayout** - Advanced layout management
- **RecyclerView** - Efficient list display

### Development Tools
- **Android Studio** - Official Android development IDE
- **Gradle** - Build automation and dependency management
- **Git** - Version control system

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API level 24 (Android 7.0) or higher
- Google account for Firebase services

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/journal-application.git
   cd journal-application
   ```

2. **Set up Firebase**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password)
   - Create a Firestore database
   - Enable Firebase Storage
   - Download `google-services.json` and place it in the `app/` directory

3. **Configure Firestore Security Rules**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /Journal/{document} {
         allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
       }
     }
   }
   ```

4. **Build and Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

### Configuration

1. **Firebase Setup**
   - Replace the Firebase configuration in `google-services.json`
   - Update Firestore security rules for your project
   - Configure Firebase Storage rules for image uploads

2. **Permissions**
   - The app automatically requests necessary permissions
   - Storage permissions for image uploads
   - Internet permissions for Firebase connectivity

## 📁 Project Structure

```
JournalApplication/
├── app/
│   ├── src/main/
│   │   ├── java/com/bharatcoding/journalapplication/
│   │   │   ├── MainActivity.kt              # Login screen
│   │   │   ├── SignUpActivity.kt            # Registration screen
│   │   │   ├── JournalList.kt               # Main journal list
│   │   │   ├── AddJournalActivity.kt        # Create new entries
│   │   │   ├── Journal.kt                   # Data model
│   │   │   └── JournalRecyclerAdapter.kt    # List adapter
│   │   ├── res/
│   │   │   ├── layout/                      # UI layouts
│   │   │   ├── drawable/                    # Icons and images
│   │   │   ├── values/                      # Colors, strings, themes
│   │   │   └── menu/                        # Menu layouts
│   │   └── AndroidManifest.xml              # App configuration
│   └── build.gradle.kts                     # App dependencies
├── build.gradle.kts                         # Project configuration
└── README.md                                # This file
```

## 🔧 Key Features Implementation

### Authentication Flow
```kotlin
// Firebase Authentication
auth.signInWithEmailAndPassword(email, password)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // Navigate to main app
        }
    }
```

### Image Upload
```kotlin
// Firebase Storage upload
storageReference.child("journal_images/$filename")
    .putFile(imageUri)
    .addOnSuccessListener { taskSnapshot ->
        // Get download URL and save to Firestore
    }
```

### Data Binding
```xml
<!-- Efficient UI updates -->
<TextView
    android:text="@{journal.title}"
    android:visibility="@{journal.hasImage ? View.VISIBLE : View.GONE}" />
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.





Assets Credits:
1. https://www.freepik.com/free-vector/patent-law-concept_10366081.htm
