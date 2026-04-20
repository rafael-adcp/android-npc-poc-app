-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.example.nfcpoc.**$$serializer { *; }
-keepclassmembers class com.example.nfcpoc.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.nfcpoc.** {
    kotlinx.serialization.KSerializer serializer(...);
}
