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


# --- JAIN CONNECT SPECIFIC RULES ---

# 1. Retrofit & GSON (Keep Models)
# Prevent obfuscation of your Data Transfer Objects (DTOs)/Models
# so GSON can map JSON response to them.
-keep class com.mycompany.jainconnect.models.** { *; }
-keep class com.mycompany.jainconnect.** { *; } # Keeping root package classes (User, Tithi etc if there)

# 2. Hilt / Dagger
-keep class com.mycompany.jainconnect.JainConnectApp { *; }
-keep class dagger.hilt.** { *; }
-keep interface dagger.hilt.** { *; }
-keep public class * extends dagger.hilt.android.HiltAndroidApp

# 3. Glide (Image Loading)
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# 4. ViewBinding (Safe measure)
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(...);
}