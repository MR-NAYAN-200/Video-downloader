# ভিডিও সেভার (Android)

Kotlin ও Jetpack Compose-এ তৈরি Android অ্যাপ। আপনার দেওয়া `nayan-video-downloader.vercel.app` API দিয়ে ভিডিওর তথ্য আনে এবং Android-এর Downloads ফোল্ডারে ফাইল সংরক্ষণ করে।

## GitHub Actions থেকে APK

1. এই project-টি নিজের GitHub repository-তে push করুন।
2. যেকোনো branch-এ push হলে **Build Android APK** workflow স্বয়ংক্রিয়ভাবে চলবে।
3. GitHub repository-এর **Actions** tab-এ সফল run খুলুন।
4. **Artifacts** অংশ থেকে `video-saver-debug-apk` ডাউনলোড করুন। ZIP-এর মধ্যে `app-debug.apk` থাকবে।

Pull request এবং Actions tab থেকে হাতে চালানো workflow-তেও APK তৈরি হয়।

## স্থানীয়ভাবে build

Android SDK, JDK 17 এবং Gradle Wrapper ব্যবহার করে:

```bash
./gradlew assembleDebug
```

APK তৈরি হবে `app/build/outputs/apk/debug/app-debug.apk`-এ।

এই debug APK পরীক্ষা বা sideload করার জন্য। Play Store release-এর জন্য আলাদা signing key ও release build configuration দরকার।

## API ও দায়িত্বশীল ব্যবহার

- API endpoint: `https://nayan-video-downloader.vercel.app/alldown`
- এটি একটি তৃতীয় পক্ষের API; এর প্রাপ্যতা বা আচরণ এই project নিয়ন্ত্রণ করে না।
- শুধু নিজের তৈরি বা ডাউনলোডের অনুমতি থাকা ভিডিও ব্যবহার করুন। কোনো platform-এর সুরক্ষা বা অ্যাক্সেস-নিয়ন্ত্রণ এড়ানোর জন্য অ্যাপটি ব্যবহার করবেন না।
