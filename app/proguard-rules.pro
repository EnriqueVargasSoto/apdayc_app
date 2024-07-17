# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\ROBIN\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
-dontwarn org.xmlpull.v1.**
-dontwarn com.sun.mail.**
-dontwarn okio.**
-dontwarn com.squareup.picasso.**

-keep class com.google.maps.** { *; }
-keep class org.xmlpull.v1.** { *; }
-keep class org.apache.http.** { *; }
# Para envio de correos
-keep class javamail.** {*;}
-keep class javax.mail.** {*;}
-keep class javax.activation.** {*;}

-keep class com.sun.mail.dsn.** {*;}
-keep class com.sun.mail.handlers.** {*;}
-keep class com.sun.mail.smtp.** {*;}
-keep class com.sun.mail.util.** {*;}
-keep class mailcap.** {*;}
-keep class mimetypes.** {*;}
-keep class myjava.awt.datatransfer.** {*;}
-keep class org.apache.harmony.awt.** {*;}
-keep class org.apache.harmony.misc.** {*;}
-keep class com.squareup.picasso.** {*;}

-dontnote org.xmlpull.v1.**
-dontnote org.apache.http.**
-dontnote android.net.http.**

-keepclassmembers class com.expediodigital.ventas360.util.DataBaseHelper { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep class com.RT_Printer.BluetoothPrinter.** { *; }
-dontwarn com.RT_Printer.BluetoothPrinter.**

#Mantener las clases para serializar con gson para enviar a los servicios web
-keep class com.expediodigital.ventas360.DTO.** { *; }
-keep class com.expediodigital.ventas360.model.EncuestaRespuestaModel { *; }
-keep class com.expediodigital.ventas360.model.EncuestaRespuestaDetalleModel { *; }
-keep class com.expediodigital.ventas360.model.ClienteCoordenadasModel { *; }

#Remover los mensajes log (Se cambio proguard-android.txt por proguard-android-optimize.txt en release options) para que esto se aplique
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** w(...);
    public static *** v(...);
    public static *** i(...);
    public static *** e(...);
}