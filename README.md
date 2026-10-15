# QR Vision — قارئ QR الذكي

تطبيق أندرويد عربي لقراءة QR من الكاميرا أو من صورة، مع سجل محلي ومفضلة وإعدادات وواجهة داكنة زمردية.

## الخصائص

- Kotlin + Jetpack Compose + Material 3.
- CameraX للمعاينة المباشرة وتحليل الإطارات، مع منع معالجة الرمز نفسه مراراً أثناء جلسة المسح.
- ML Kit Barcode Scanning **بموديل مضمّن** (`com.google.mlkit:barcode-scanning:17.3.0`)، حتى لا تتطلب أول قراءة تنزيل موديل وقت التشغيل.
- Android Photo Picker لاختيار صورة من دون طلب صلاحية الوصول الشامل للصور.
- تصنيف مبدئي للرابط والنص والهاتف والبريد وWi‑Fi والموقع وSMS وجهة الاتصال والمحتوى الآخر.
- لا تُفتح الروابط تلقائياً؛ إجراءات فتح المحتوى تتطلب تأكيداً. لا تتصل الشبكات ولا تُرسل الرسائل تلقائياً.
- DataStore للتخزين المحلي للسجل والمفضلة والإعدادات. لا تحفظ الصور الأصلية.
- لا توجد إعلانات أو تسجيل دخول أو واجهات تتبع. ملف Manifest يحذف صلاحية الإنترنت المضافة من الاعتماديات غير الضرورية؛ القراءة تتم محلياً.
- اختبارات Unit Tests للمنطق الخاص بتصنيف المحتوى.

## متطلبات البناء

- JDK 17.
- Android SDK Platform 36 وBuild Tools المناسبة.
- Gradle 8.13 (ملفات المشروع تضبط AGP 8.13.2).
- اتصال بالإنترنت أثناء **البناء فقط** لتنزيل اعتمادات Gradle/Android. التطبيق نفسه مصمم للعمل دون اتصال بعد تثبيت التطبيق.

### بناء APK Debug

Linux/macOS:

```bash
./gradlew testDebugUnitTest assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk QR-Vision.apk
```

Windows: استخدم Gradle 8.13 المثبت محلياً، أو شغّل `gradle testDebugUnitTest assembleDebug` في PowerShell بعد إعداد Android SDK.

> ملاحظة حول Wrapper: يتضمن المشروع `gradle-wrapper.properties` وسكربتَي `gradlew` و`gradlew.bat` يعملان كـ bootstrap مخصّص لتنزيل Gradle 8.13 والتحقق من SHA-256 قبل تشغيله. لا يتضمن `gradle-wrapper.jar` الرسمي؛ لذلك يستخدم GitHub Actions نسخة Gradle مباشرةً عبر `gradle/actions/setup-gradle`. إذا تحتاج Wrapper القياسي المتعارف عليه، ولّده من Gradle 8.13 بالأمر `gradle wrapper --gradle-version 8.13` واستبدل ملفات Wrapper الحالية.

## GitHub Actions

- `.github/workflows/android.yml`: يشغّل اختبارات JVM والبناء ويرفع `QR-Vision.apk` كـ Artifact.
- `.github/workflows/release.yml`: Workflow يدوي لنشر Release. أنشئ Tag مثل `v1.0.0` أولاً، ثم افتح **Actions → Publish QR Vision Release → Run workflow** وأدخل اسم الـTag.
- إذا أُضيفت أسرار التوقيع الأربعة أدناه، ينشر الـWorkflow APK إصدار Release موقّعاً. إذا لم تُضف، ينشر Debug APK موقّعاً بمفتاح debug القياسي لأغراض الاختبار فقط.

GitHub Secrets المطلوبة للتوقيع الاختياري:

- `ANDROID_KEYSTORE_BASE64`: ملف keystore مشفّر Base64.
- `ANDROID_KEYSTORE_PASSWORD`: كلمة مرور keystore.
- `ANDROID_KEY_ALIAS`: الاسم المستعار للمفتاح.
- `ANDROID_KEY_PASSWORD`: كلمة مرور المفتاح.

لا تضع ملفات keystore أو كلمات المرور داخل المستودع.

## الخصوصية

يتم تحليل الصور محلياً عبر ML Kit المضمّن. يخزن التطبيق النص المقروء ونوعه ووقت القراءة داخل مساحة بيانات التطبيق. لا يحفظ الصورة الأصلية. فتح رابط أو تمرير المحتوى لتطبيق خارجي يحدث فقط بعد طلب المستخدم وتأكيده.

## الاختبارات والقيود المعروفة

- توجد اختبارات وحدة لتصنيف الأنواع الشائعة.
- هذه الحزمة لم يُنفّذ عليها بناء Android فعلي داخل بيئة إنشاء الملفات الحالية، ولم يُختبر تثبيتها على جهاز حقيقي. لذلك لا يُعلن عن APK مبني أو عن نجاح اختبارات الأجهزة قبل تشغيل الـWorkflow والتحقق من نتيجته.
- قراءة رموز QR تعتمد على جودة الصورة والإضاءة والتركيز وحجم الرمز داخل الصورة. أنواع البيانات المركبة تُصنّف محلياً ولا تُنفذ تلقائياً.
