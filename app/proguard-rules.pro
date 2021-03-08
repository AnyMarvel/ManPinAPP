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

-keep public class * extends android.support.**
-keep class android.support.v4.** {*;}
-keep class android.support.v7.** {*;}
-keep class android.support.constraint.** {*;}
-dontwarn android.support.**

-keep class java.lang.**{*;}
-dontwarn java.lang.**
-dontnote java.lang.**

-keep class javax.lang.**{*;}
-dontwarn javax.lang.**
-dontnote javax.lang.**

-keep class sun.misc.**{*;}
-dontwarn sun.misc.**
-dontnote sun.misc.**

-keep class com.google.protobuf.**{*;}
-dontwarn com.google.protobuf.**
-dontnote com.google.protobuf.**

-keep class com.google.common.**{*;}
-dontwarn com.google.common.**
-dontnote com.google.common.**

-keep class android.arch.lifecycle.**{*;}
-dontwarn android.arch.lifecycle.**
-dontnote android.arch.lifecycle.**

-keep class androidx.media.**{*;}
-dontwarn androidx.media.**
-dontnote androidx.media.**

-keep class android.support.design.widget.** {*; }
-dontwarn android.support.design.widget.**

-keep class androidx.core.graphics.drawable.IconCompatParcelizer{*;}
-dontwarn androidx.core.graphics.drawable.IconCompatParcelizer
-dontnote androidx.core.graphics.drawable.IconCompatParcelizer
-keep class com.google.android.apps.photolab.storyboard.activity.ComicTextureView{*;}
-dontwarn com.google.android.apps.photolab.storyboard.activity.ComicTextureView
-dontnote com.google.android.apps.photolab.storyboard.activity.ComicTextureView

-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.google.android.apps.photolab.storyboard.pipeline.ObjectDetection{*;}

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-keep class android.support.**{*;}

-keep class com.mp.android.apps.FeedbackActivity$*{*;}
-keep class okhttp3.**{*;}
-dontwarn okhttp3.**
#glide图片框架混淆内容
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#友盟混淆内容设置
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}


-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class UMMoreHandler{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
-keep class com.tencent.mm.sdk.** {
   *;
}
-keep class com.tencent.mm.opensdk.** {
   *;
}
-keep class com.tencent.wxop.** {
   *;
}
-keep class com.tencent.mm.sdk.** {
   *;
}
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep class com.kakao.** {*;}
-dontwarn com.kakao.**
-keep public class com.umeng.com.umeng.soexample.R$*{
    public static final int *;
}
-keep public class com.linkedin.android.mobilesdk.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}
-keep class com.umeng.socialize.impl.ImageImpl {*;}
-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class com.linkedin.** { *; }
-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
-keepattributes Signature

-keep class com.xinlan.imageeditlibrary.**{*;}




# 保留我们使用的四大组件，自定义的Application等等这些类不被混淆
# 因为这些子类都有可能被外部调用
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Appliction
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService


# 保留support下的所有类及其内部类
-keep class android.support.** {*;}

# 保留继承的
-keep public class * extends android.support.annotation.**

# 保留R下面的资源
-keep class **.R$* {*;}



# 保留在Activity中的方法参数是view的方法，
# 这样以来我们在layout中写的onClick就不会被影响
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}

# 保留枚举类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留我们自定义控件（继承自View）不被混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留Parcelable序列化类不被混淆
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 对于带有回调函数的onXXEvent、**On*Listener的，不能被混淆
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# webView处理，项目中没有使用到webView忽略即可
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
    public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.webView, jav.lang.String);
}

# 移除Log类打印各个等级日志的代码，打正式包的时候可以做为禁log使用，这里可以作为禁止log打印的功能使用
# 记得proguard-android.txt中一定不要加-dontoptimize才起作用
# 另外的一种实现方案是通过BuildConfig.DEBUG的变量来控制
#-assumenosideeffects class android.util.Log {
#    public static int v(...);
#    public static int i(...);
#    public static int w(...);
#    public static int d(...);
#    public static int e(...);
#}

### greenDAO 3
-keep class org.greenrobot.greendao.**{ *; }
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
-dontwarn org.greenrobot.greendao.database.**
-dontwarn rx.**

-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-keep class retrofit2.**{*;}
-keep class okhttp3.**{*;}
-keep class okio.**{*;}
-keep class com.hwangjr.rxbus.**{*;}
-dontwarn org.apache.log4j.lf5.viewer.**
-dontnote org.apache.log4j.lf5.viewer.**
-dontwarn freemarker.**
-dontnote org.python.core.**
-dontwarn com.hwangjr.rxbus.**

-keep class com.mp.android.apps.monke.monkeybook.widget.**{*;}
-keep class com.mp.android.apps.monke.monkeybook.bean.**{*;}
-keep class android.support.**{*;}
-keep class me.grantland.widget.**{*;}
-keep class de.hdodenhof.circleimageview.**{*;}
-keep class retrofit2.**{*;}
-keep class tyrant.explosionfield.**{*;}
-keep class tyrantgit.explosionfield.**{*;}
-keep class freemarker.**{*;}
##JSOUP
-keep class org.jsoup.**{ *; }
-keep class com.monke.mprogressbar.**{ *; }

##友盟统计
-keepclassmembers class * {
    public <init> (org.json.JSONObject);
}

-keep public class com.mp.android.apps.monke.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# keep bean类不被混淆
-keep class com.mp.android.apps.livevblank.bean.**{*;}
-keep class com.mp.android.apps.login.bean.login.**{*;}
-keep class com.mp.android.apps.explore.bean.**{*;}
-keep class com.mp.android.apps.main.home.bean.**{*;}
-keep class com.mp.android.apps.main.bookR.bean.**{*;}

-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static void dropTable(org.greenrobot.greendao.database.Database, boolean);
    public static void createTable(org.greenrobot.greendao.database.Database, boolean);
}