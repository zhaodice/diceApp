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
-keepattributes *Annotation*,Signature

-keepclasseswithmembers class * extends java.lang.Exception { *;}

#kotlin
-keep class kotlin.collections.* { *; }
-keep class kotlin.text.* { *; }
-keep class kotlin.sequences.* { *; }
-keep class kotlin.jvm.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.ranges.* { *; }
-keep class kotlin.io.* { *; }
-keep class kotlin.reflect.* { *; }
-keep class kotlin.time.* { *; }
-keep class kotlin.comparisons.* { *; }
-keep class kotlin.random.* { *; }
-keep class kotlin.internal.* { *; }
-keep class kotlin.math.* { *; }
-keep class kotlin.concurrent.* { *; }
-keep class kotlin.properties.* { *; }
-keep class kotlin.contracts.* { *; }
-keep class kotlin.contracts.* { *; }
-keep class kotlin.annotation.* { *; }
-keep class kotlin.experimental.* { *; }
-keep class kotlin.system.* { *; }
-keep class kotlin.js.* { *; }
-keep class kotlin.* { *; }


-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * { *;}
-keep class kotlinx.coroutines.** {*;}

# jvm平台的一些不存在的类

-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn sun.misc.**
-dontwarn org.jetbrains.kotlin.**

# mirai 配置
-keep class net.mamoe.mirai.qqandroid.QQAndroid.$Companion { *; }
-keepclasseswithmembers class * extends net.mamoe.mirai.BotFactory{ *;}

-keep class net.mamoe.yamlkt.* {*;}
-keep class net.mamoe.mirai.console.** {*;}

-keep class net.mamoe.mirai.contact.* { *; }
-keep class net.mamoe.mirai.event.** { *; }
-keep class net.mamoe.mirai.message.** { *; }
-keep class net.mamoe.mirai.network.* { *; }
-keep class net.mamoe.mirai.utils.* { *; }
-keep class net.mamoe.mirai.* { *; }
-keep interface net.mamoe.mirai.* { *; }

# ktor
-keep class io.ktor.client.** { *; }

-keepclassmembers class io.ktor.** {
    volatile <fields>;
}

# json

-keep class kotlinx.serialization.json.** {*;}
-keep class kotlinx.serialization.* {*;}

# yaml

#-keep class org.yaml.snakeyaml.* {*;}
-keep class org.yaml.snakeyaml.Yaml {*;}
-keep class org.yaml.snakeyaml.util.* {*;}

# okhttp
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.internal.platform.ConscryptPlatform


-keep class javax.script.** {*;}
-keep class java.lang.management.** {*;}
-keep class fightcent.permissionrequest.** { *; }
-keep class dalvik.system.** {*;}
-keep class terminal.** {*;}
#-keep class kotlinx.** {*;}
#-keep class org.bouncycastle.asn1.** {*;}
-keep class org.bouncycastle.jce.provider.**{ *;}
-keep class org.bouncycastle.jcajce.provider.** {*;}
#-keep class okhttp3.** {*;}
#-keep interface okhttp3.**{*;}
#-dontwarn okhttp3.**

# Okhttp3的混淆配置
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform


#-keep class okio.* { *; }
#-keep interface okio.* { *; }
#-dontwarn okio.**

-keep class sun.misc.Service {*;}
-keep class sun.misc.ServiceConfigurationError {*;}
-keep class sun.misc.VM {*;}
-keep class sun.reflect.Reflection {*;}
-keep class sun.security.action.GetPropertyAction {*;}
-keep class sun.security.util.SecurityConstants {*;}
-keep interface sun.security.util.PermissionFactory {*;}