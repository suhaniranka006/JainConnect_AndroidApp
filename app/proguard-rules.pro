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
# FIXED: Pointed to the correct package 'data.models' instead of 'models'
-keep class com.mycompany.jainconnect.data.models.** { *; }

# General Gson rules to prevent issues with generic types and field renaming
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# If you have other classes that are serialized, keep them too
-keep class com.mycompany.jainconnect.** { *; }

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

# 5. Razorpay
-keep class com.razorpay.** { *; }
-dontwarn com.razorpay.**
-dontwarn proguard.annotation.**