# Keep Room entities
-keep class com.obrynex.studyguard.data.db.** { *; }

# Keep WorkManager workers
-keep class com.obrynex.studyguard.notifications.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# JSON parsing
-keepclassmembers class * {
    @org.json.JSONObject *;
}
