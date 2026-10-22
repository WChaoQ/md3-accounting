# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Legacy migration serialization
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.wchaoq.md3accounting.data.ExpenseRepository** { *; }
