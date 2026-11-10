# Proguard rules for ArrowBuddies

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# Keep serializable classes and their properties
-keepclassmembers class * implements kotlinx.serialization.internal.GeneratedSerializer {
    *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}

# Coroutines
-dontwarn kotlinx.coroutines.**

# Google Mobile Ads & UMP SDK
-keep public class com.google.android.gms.ads.** { *; }
-keep public class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.ump.**

