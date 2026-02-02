#отключение удаления кода, оптимизации
-dontshrink
-dontoptimize
-overloadaggressively
-useuniqueclassmembernames
-repackageclasses "org.ayoree.chatimprover.internal"

#сохранение аттрибутов
-keepattributes *Annotation*,Signature,*SerialEntry*

#сохранение библиотек,классов и т.д
-keep class org.ayoree.chatimprover.ChatImprover
-keep class org.ayoree.chatimprover.mixin.**
-keep class org.ayoree.chatimprover.api.** { *; }
-keep class org.ayoree.chatimprover.internal.ModMenuIntegration
-keep class com.terraformersmc.modmenu.** { *; }
-keepclassmembers class * {
    @dev.isxander.yacl3.config.v2.api.SerialEntry *;
}

#игнор предупреждений при обфускации
-ignorewarnings

#шаблоны для обфускации
-classobfuscationdictionary proguard-dict-classes.txt
-obfuscationdictionary proguard-dict.txt
