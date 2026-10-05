# RRF HR Firebase Cloud Functions

এই ডিরেক্টরিতে RRF HR চুক্তিপত্র ও কর্মী ব্যবস্থাপনা অ্যাপ্লিকেশনের Firebase Cloud Functions কোড যুক্ত করা হয়েছে।

## ফাংশনসমূহ (Functions Overview):

1. **`checkHealth`**: ক্লাউড ফাংশন সার্ভার চালু ও সক্রিয় কি-না তা চেক করে।
2. **`backupAgreementForms`**: কর্মীর সকল এগ্রিমেন্ট ফরম (১০০ টাকার স্ট্যাম্প, ২৫ টাকার প্রত্যয়ন, নমিনি, ব্যক্তিগত তথ্য, তথ্য যাচাই ফরম) ক্লাউডে ব্যাকআপ করে।
3. **`syncWorkerProfile`**: কর্মীর প্রোফাইল ডাটা কেন্দ্রীয় ডেটাবেসে সিঙ্ক করে।
4. **`verifyEmployeeOnline`**: কর্মীর এনআইডি ও মোবাইল নম্বর সার্ভার-সাইডে যাচাই করে।
5. **`sendVerificationNotification`**: জামিনদার বা কর্মীকে নোটিফিকেশন/এসএমএস বার্তা প্রেরণের জন্য শিডিউল করে।

## কিভাবে ডিপ্লয় করবেন (Deployment Steps):

1. Firebase CLI ইনস্টল করুন (যদি না থাকে):
   ```bash
   npm install -g firebase-tools
   ```

2. ফায়ারবেসে লগইন করুন:
   ```bash
   firebase login
   ```

3. আপনার ফায়ারবেস প্রজেক্ট সিলেক্ট করুন:
   ```bash
   firebase use --add
   ```

4. ডিপ্লয় করুন:
   ```bash
   firebase deploy --only functions
   ```
