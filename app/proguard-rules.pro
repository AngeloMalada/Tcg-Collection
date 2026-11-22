# Keep kotlinx.serialization & Room metadata
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.AnnotationsKt

# Room entities and query projection models
-keep class com.tcgvault.app.data.local.entity.** { *; }
-keep class com.tcgvault.app.data.local.model.** { *; }
-keep class com.tcgvault.app.data.model.** { *; }

# OkHttp / Okio optional platform warnings
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
