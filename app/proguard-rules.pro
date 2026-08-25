# === BASIC ===
-optimizationpasses 5
-allowaccessmodification
-overloadaggressively
-mergeinterfacesaggressively
-dontpreverify
-verbose

# === KEEP APP ENTRY ===
-keep class com.universal.performance.** { *; }
-dontwarn com.universal.performance.**
-keep interface com.universal.performance.** { *; }

# === ANDROID COMPONENTS — Prevent crash on launch ===
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

# === VIEWS & CUSTOM VIEWS ===
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# === NATIVE METHODS — Critical for chipset/performance code ===
-keepclasseswithmembernames class * {
    native <methods>;
}

# === CALLBACKS — Don't strip onClick / event handlers ===
-keepclassmembers class * {
    public void *(android.view.View);
}

# === SERIALIZATION / PARCELABLE ===
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep class * implements android.os.Parcelable { *; }

# === ANNOTATIONS — Keep for frameworks/libraries ===
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# === JAVA REFLECTION — Prevent class-not-found crashes ===
-keepclassmembers class * {
    public static <fields>;
    public <methods>;
}

# === KOTLIN SUPPORT ===
-keepnames class ** implements kotlinx.serialization.SerializationStrategy
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class kotlin.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# === RESOURCES ===
-keepclassmembers class **.R$* {
    public static <fields>;
}

# === WEBVIEW ===
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# === DONT WARN FOR MISSING CLASSES ===
-dontwarn javax.annotation.**
-dontwarn lombok.**
-dontwarn org.jetbrains.annotations.**
