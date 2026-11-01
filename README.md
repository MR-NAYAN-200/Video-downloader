# Video Downloader (Android)

Kotlin ও Jetpack Compose-এ তৈরি Android app। এতে animated splash, Home/Download/Info navigation, app icon, device ও creator info, এবং download progress UI আছে।

## Download

- ভিডিও link paste করে বিশ্লেষণ করুন, তারপর পাওয়া quality বেছে Download চাপুন।
- Android DownloadManager notification-এ progress ও completion দেখায়।
- ভিডিও `Downloads/Nayan`-এ সংরক্ষিত হয়; Download tab-এ active, completed ও failed item দেখা যায়।
- Completed item-এ চাপলে device-এর video player খুলবে।

## স্থানীয়ভাবে build

Android SDK 35, JDK 17 এবং Gradle Wrapper ব্যবহার করে:

```bash
./gradlew assembleDebug
```

APK তৈরি হবে `app/build/outputs/apk/debug/app-debug.apk`-এ। এটি debug APK—পরীক্ষা বা sideload-এর জন্য। Play Store release-এর জন্য signing key ও release build configuration লাগবে।

## API ও দায়িত্বশীল ব্যবহার

- API endpoint: `https://nayan-video-downloader.vercel.app/alldown`
- এটি তৃতীয় পক্ষের API; এর প্রাপ্যতা বা আচরণ এই project নিয়ন্ত্রণ করে না।
- শুধু নিজের তৈরি বা ডাউনলোডের অনুমতি থাকা ভিডিও ব্যবহার করুন। কোনো platform-এর সুরক্ষা বা access control এড়ানোর জন্য app-টি ব্যবহার করবেন না।
