-dontwarn rx.internal.util.unsafe.**
-dontwarn nl.komponents.kovenant.unsafe.**

-keepattributes Signature

-keepclassmembers class **$WhenMappings {
    <fields>;
}

#-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
#    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
#}

