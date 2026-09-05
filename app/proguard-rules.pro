# Add project specific ProGuard rules here.
# Keep data models used by Gson/Retrofit reflection
-keep class com.healthyscan.app.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Exceptions

# ML Kit
-keep class com.google.mlkit.** { *; }
