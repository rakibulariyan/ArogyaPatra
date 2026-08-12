# ProGuard / R8 Rules for ArogyaPatra

# Keep Room entities and DAOs
-keep class com.aidwellness.arogyapatra.data.local.entity.** { *; }
-keep class com.aidwellness.arogyapatra.data.local.dao.** { *; }
-keep class com.aidwellness.arogyapatra.data.local.AppDatabase { *; }

# Keep Backup DTO models for Gson serialization
-keep class com.aidwellness.arogyapatra.backup.** { *; }

# Keep ZXing QR code generator classes
-keep class com.google.zxing.** { *; }

# Keep Gson annotations
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
