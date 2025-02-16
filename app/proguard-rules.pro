# Keep all classes and fields related to Firestore data models
-keepclassmembers class com.splicr.app.data.** {   # Adjust the package as per your project
    *;
}

# Keep annotations (Firestore uses annotations to mark fields in the data classes)
-keepattributes *Annotation*

# Keep all Firebase Firestore related classes
-keep class com.google.firebase.firestore.** { *; }

# Keep Firebase related classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Gson (if you use Gson for JSON serialization)
-keep class com.google.gson.** { *; }
-keepattributes Signature

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Ensure CredentialManager classes are preserved
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

# Suppress warnings for Ktor mock engine classes
-dontwarn io.ktor.client.engine.mock.MockEngine$Companion
-dontwarn io.ktor.client.engine.mock.MockEngine
-dontwarn io.ktor.client.engine.mock.MockRequestHandleScope
-dontwarn io.ktor.client.engine.mock.MockUtilsKt