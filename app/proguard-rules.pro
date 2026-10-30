# The Rust side calls this class and method by name.
-keep class com.cronoszip.app.engine.NativeBridge { *; }
-keep class com.cronoszip.app.engine.NativeEventCallback { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
