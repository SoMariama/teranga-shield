# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlinx serialization models (chargement des jeux de données de patterns)
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.terangashield.app.**$$serializer { *; }
-keepclassmembers class com.terangashield.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.terangashield.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
