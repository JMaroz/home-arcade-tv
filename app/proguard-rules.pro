# ==============================================================================
# HomeArcade TV - ProGuard / R8 Optimization & Shrinking Configuration
# ==============================================================================

# --- General Optimization & Bytecode Transformations ---
# Allow R8 to perform aggressive access widening and repackaging
-allowaccessmodification
-repackageclasses ''

# Preserve line numbers and file names for readable stacktraces with mapping.txt
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Strip verbose, debug, and info logging in release builds to maximize speed & minimize APK size
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# --- Application Entry Points & Core Components ---
-keep public class com.homearcade.tv.HomeArcadeTvApp {
    public <init>();
    public <methods>;
}
-keep public class com.homearcade.tv.MainActivity {
    public <init>();
}

# --- ViewModels & Architecture Components ---
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class com.homearcade.tv.ui.**ViewModel {
    *;
}

# --- Kotlinx Serialization ---
# Keep serializers and serializable companion objects
-keepattributes *Annotation*
-keepclassmembers class com.homearcade.tv.data.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.homearcade.tv.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation class com.homearcade.tv.data.model.**
-keepclassmembers,allowobfuscation class com.homearcade.tv.data.model.** {
    <fields>;
}
-dontwarn kotlinx.serialization.**

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# --- Ktor Network Stack (Core, OkHttp Engine, Serialization) ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.netty.**

# --- OkHttp 3 / 4 ---
-keepattributes Signature
-keepclassmembers class okhttp3.internal.publicsuffix.PublicSuffixDatabase {
    <fields>;
}
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Coil (Image Loading) ---
-keep class coil3.** { *; }
-dontwarn coil3.**
-keep class coil.** { *; }
-dontwarn coil.**

# --- Jetpack Compose & TV Material / Foundation ---
-dontwarn androidx.compose.**
-dontwarn androidx.tv.**
-dontwarn androidx.datastore.**
