# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ===== MediGuide AI App ProGuard Rules =====
# Package: com.mediguide.firstaid

# Keep BuildConfig (CRITICAL - API keys are loaded via reflection)
-keep class com.mediguide.firstaid.BuildConfig { *; }
-keepclassmembers class com.mediguide.firstaid.BuildConfig {
    public static <fields>;
}

# Keep all fields in BuildConfig (especially GEMINI_API_KEY)
-keepclassmembers class * {
    public static final java.lang.String GEMINI_API_KEY;
}

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-dontwarn com.bumptech.glide.**

# Keep Gemini AI classes (CRITICAL - prevents AI from going offline)
-keep class com.google.ai.client.generativeai.** { *; }
-keep interface com.google.ai.client.generativeai.** { *; }
-keepclassmembers class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# Keep all AI-related classes in your app
-keep class com.mediguide.firstaid.voice.** { *; }
-keepclassmembers class com.mediguide.firstaid.voice.** { *; }

# Keep Kotlin metadata for reflection (critical for Gemini SDK)
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# Keep all Kotlin classes used by AI
-keep class kotlin.** { *; }
-keep class kotlin.jvm.** { *; }
-keep class kotlin.jvm.internal.** { *; }

# Kotlin Coroutines for AI (needed for async operations)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# OkHttp and Retrofit (used by Gemini AI SDK) - CRITICAL FOR AI
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-dontwarn okhttp3.**

# Okio (used by OkHttp)
-keep class okio.** { *; }
-dontwarn okio.**

# Retrofit
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Platform calls Class.forName on types which do not exist on Android
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# GRPC (may be used by Gemini)
-keep class io.grpc.** { *; }
-dontwarn io.grpc.**
-dontwarn com.google.protobuf.**
-dontwarn com.google.common.**

# Keep app data classes (CRITICAL - used with Gson)
-keep class com.mediguide.firstaid.data.models.** { *; }
-keep class com.mediguide.firstaid.data.** { *; }
-keep class com.mediguide.firstaid.managers.** { *; }
-keep class com.mediguide.firstaid.ui.guides.** { *; }
-keep class com.mediguide.firstaid.ui.** { *; }
-keep class com.mediguide.firstaid.utils.** { *; }

# Keep all managers (they use reflection and Gson)
-keep class com.mediguide.firstaid.managers.JsonGuideManager { *; }
-keep class com.mediguide.firstaid.managers.ContactManager { *; }
-keepclassmembers class com.mediguide.firstaid.managers.** {
    <init>(...);
    public <methods>;
}

# Keep Gson models and TypeToken (CRITICAL for release builds)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Gson specific classes
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Gson TypeToken - CRITICAL FIX for IllegalStateException
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <fields>;
    <methods>;
}

# Keep generic signatures for Gson
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }

# Keep all model classes used with Gson
-keep class com.mediguide.firstaid.data.models.** { *; }
-keep class com.mediguide.firstaid.data.repository.** { *; }
-keep class com.mediguide.firstaid.managers.** { *; }

# Keep fields with @SerializedName
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Application classes that will be serialized/deserialized over Gson
-keep class com.mediguide.firstaid.data.models.Guide { *; }
-keep class com.mediguide.firstaid.data.models.EmergencyContact { *; }
-keep class com.mediguide.firstaid.data.models.PhoneContact { *; }
-keep class com.mediguide.firstaid.data.voice.** { *; }

# Keep view binding
-keep class * extends androidx.viewbinding.ViewBinding { *; }
-keep class com.mediguide.firstaid.databinding.** { *; }

# Keep data binding
-keep class androidx.databinding.** { *; }

# Navigation components
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# Keep Fragment constructors
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public <init>(...);
}

# Keep Activity classes
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Lifecycle
-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
-keep class * implements androidx.lifecycle.DefaultLifecycleObserver {
    <init>(...);
}

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep class androidx.work.impl.** { *; }

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

