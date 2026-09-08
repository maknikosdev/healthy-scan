# --- Data models / Gson ---
-keep class com.healthyscan.app.data.model.** { *; }
-keep class com.healthyscan.app.data.remote.** { *; }
-keep class com.healthyscan.app.data.local.** { *; }
-keep class com.healthyscan.app.data.backup.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# --- Retrofit / suspend functions ---
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**

# --- ML Kit ---
-keep class com.google.mlkit.** { *; }
