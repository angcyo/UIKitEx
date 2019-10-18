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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保持所有 entity 对象
-keep class com.wayto.**.**Entity {*;}

-keep class **.**MyObjectBox {*;}

# Native methods
-keepclasseswithmembers,allowshrinking class io.objectbox.** {
    native <methods>;
}

# For __boxStore field in entities
-keep class io.objectbox.BoxStore

-keep class * extends io.objectbox.Cursor {
    <init>(...);
}

# Native code expects names to match
-keep class io.objectbox.relation.ToOne {
    void setTargetId(long);
}

-keep class io.objectbox.relation.ToMany

-keep @interface  io.objectbox.annotation.Entity

# Keep entity constructors
-keep @io.objectbox.annotation.Entity class * {
    <init>(...);
}

# For relation ID fields
-keepclassmembers @io.objectbox.annotation.Entity class * {
    <fields>;
}

-keep interface  io.objectbox.converter.PropertyConverter {
    <fields>;
    <methods>;
}

-keep class * extends io.objectbox.converter.PropertyConverter {
    <fields>;
    <methods>;
}

-keep class io.objectbox.exception.DbException {
    <fields>;
    <methods>;
}

-keep class * extends io.objectbox.exception.DbException {
    <fields>;
    <methods>;
}

-keep class io.objectbox.internal.CrashReportLogger {
    <fields>;
    <methods>;
}

-keep class * extends io.objectbox.internal.CrashReportLogger {
    <fields>;
    <methods>;
}

-keep class io.objectbox.exception.DbExceptionListener {
    <fields>;
    <methods>;
}

-keep class * extends io.objectbox.exception.DbExceptionListener {
    <fields>;
    <methods>;
}