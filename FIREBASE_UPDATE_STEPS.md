# 🔥 Firebase Console Update Guide - Step by Step

**Package Change:** `com.example.firstaidapp` → `com.mediguide.firstaid`  
**Firebase Project:** sample-firebase-ai-app-4a522  
**Date:** November 12, 2025

---

## ⏱️ WHEN TO DO THIS

**Do this AFTER I (AI) update all the code files!**

**Timing:**
1. ✅ AI updates all code (you'll see confirmation)
2. ✅ **THEN you do this Firebase update**
3. ✅ Then build new AAB
4. ✅ Then upload to Play Console

---

## 📋 STEP-BY-STEP INSTRUCTIONS

### **STEP 1: Open Firebase Console**

1. Open browser and go to: https://console.firebase.google.com/
2. Sign in with your Google account
3. You should see your project: **sample-firebase-ai-app-4a522**
4. Click on it to open

---

### **STEP 2: Go to Project Settings**

1. Click the **⚙️ Gear icon** (top left, next to "Project Overview")
2. Click **Project settings**
3. Scroll down to the **"Your apps"** section
4. You should see your current Android app with package: `com.example.firstaidapp`

---

### **STEP 3: Add New Android App**

1. In the "Your apps" section, click **"Add app"**
2. Select the **Android** icon
3. Fill in the form:

   **Android package name:** `com.mediguide.firstaid`  
   **App nickname (optional):** `MediGuide AI - Production`  
   **Debug signing certificate SHA-1:** (Leave blank for now - we'll add this later)

4. Click **"Register app"**

---

### **STEP 4: Download google-services.json**

1. After registering, Firebase will show you a download button
2. Click **"Download google-services.json"**
3. Save it to your computer (Desktop or Downloads folder)

**IMPORTANT:** You'll replace the existing file in your project with this new one!

---

### **STEP 5: Get SHA-1 Fingerprint**

Now you need to get the SHA-1 fingerprint from your keystore.

**Open Command Prompt** and run:

```batch
cd "C:\Users\Nuthan Reddy\FirstAidApp"
gradlew signingReport
```

**Wait for it to finish** (takes 10-30 seconds)

**Look for this section in the output:**

```
Variant: release
Config: release
Store: C:\Users\Nuthan Reddy\FirstAidApp\firstaid-release.keystore
Alias: firstaid
MD5: XX:XX:XX:...
SHA1: 4652530abeda2701085a28b7ab2bdf2fd9c80e4f
SHA-256: XX:XX:XX:...
Valid until: ...
```

**COPY the SHA1 value** (should be 40 characters with colons)

Example: `4652530abeda2701085a28b7ab2bdf2fd9c80e4f`

---

### **STEP 6: Add SHA-1 to Firebase**

1. Back in Firebase Console → Project Settings
2. Scroll to **"Your apps"** section
3. Find your NEW app: `com.mediguide.firstaid`
4. Under "SHA certificate fingerprints", click **"Add fingerprint"**
5. **Paste the SHA-1** you copied
6. Click **Save**

---

### **STEP 7: Enable Google Sign-In**

1. In Firebase Console, click **"Authentication"** in the left menu
2. Click on the **"Sign-in method"** tab
3. Make sure **"Google"** is **Enabled**
4. If not enabled:
   - Click on "Google"
   - Toggle **Enable**
   - Enter support email (your email)
   - Click **Save**

---

### **STEP 8: Verify OAuth Client Created**

1. Still in Firebase Console → Authentication → Sign-in method
2. Click on **"Google"** provider
3. You should see **"Web client ID"** listed
4. This confirms OAuth is set up correctly

---

### **STEP 9: Replace google-services.json in Project**

**CRITICAL STEP - DON'T SKIP!**

1. Locate the file you downloaded in **Step 4**
2. Go to your project folder: `C:\Users\Nuthan Reddy\FirstAidApp\app\`
3. **Find the existing file:** `google-services.json`
4. **BACKUP the old one:** Rename it to `google-services.json.old`
5. **Copy the NEW file** you downloaded into `app\` folder
6. Make sure it's named exactly: `google-services.json`

**File location should be:**
```
C:\Users\Nuthan Reddy\FirstAidApp\app\google-services.json
```

---

### **STEP 10: Verify the New File**

Open the new `google-services.json` and check:

```json
{
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.mediguide.firstaid"  ← Should be NEW package
        }
      }
    }
  ]
}
```

**Make sure it says `com.mediguide.firstaid` NOT `com.example.firstaidapp`!**

---

## ✅ VERIFICATION CHECKLIST

After completing all steps, check:

- [ ] New Android app added to Firebase: `com.mediguide.firstaid`
- [ ] SHA-1 fingerprint added to the new app
- [ ] Google Sign-In is enabled in Authentication
- [ ] New `google-services.json` downloaded
- [ ] Old `google-services.json` backed up as `.old`
- [ ] New `google-services.json` placed in `app\` folder
- [ ] New file contains package name: `com.mediguide.firstaid`

---

## 🎯 WHAT TO DO NEXT

After Firebase is updated:

1. **Open Android Studio**
2. **Sync Gradle:** File → Sync Project with Gradle Files
3. **Clean Build:** Build → Clean Project
4. **Rebuild:** Build → Rebuild Project
5. **Generate AAB:** Build → Generate Signed Bundle/APK
6. **Upload to Play Console**

---

## ⚠️ TROUBLESHOOTING

### **Problem: "Can't find google-services.json"**
- Solution: Make sure file is in `app\` folder, not project root

### **Problem: "Package name mismatch"**
- Solution: Open `google-services.json` and verify package is `com.mediguide.firstaid`

### **Problem: "Google Sign-In fails after change"**
- Solution: Double-check SHA-1 is added to Firebase for the NEW package

### **Problem: "Invalid SHA-1 format"**
- Solution: SHA-1 should be 40 hex characters with colons (e.g., `46:52:53:0a:...`)

### **Problem: "gradlew command not found"**
- Solution: Make sure you're in the correct directory: `C:\Users\Nuthan Reddy\FirstAidApp`

---

## 📸 SCREENSHOTS REFERENCE

### What you should see in Firebase Console:

**Before:**
```
Your apps:
└── Android app: com.example.firstaidapp
```

**After:**
```
Your apps:
├── Android app: com.example.firstaidapp (old - can keep or delete later)
└── Android app: com.mediguide.firstaid (new - ACTIVE)
    └── SHA-1: 4652530abeda2701085a28b7ab2bdf2fd9c80e4f ✅
```

---

## 🔄 CAN I DELETE THE OLD APP?

**Answer: Not yet!**

Keep the old `com.example.firstaidapp` app in Firebase until:
- ✅ New package is fully tested
- ✅ Google Sign-In works with new package
- ✅ App is uploaded to Play Store successfully
- ✅ You've tested the production APK

**After 1-2 weeks,** you can safely delete the old app from Firebase.

---

## 📞 HELP

**If you get stuck:**

1. **Check the package name** in the new `google-services.json`
2. **Verify SHA-1** was added correctly
3. **Re-sync Gradle** in Android Studio
4. **Rebuild** the project completely
5. **Check Firebase Console** → Authentication → Sign-in method → Google is enabled

---

## ⏱️ TIME ESTIMATE

| Step | Time |
|------|------|
| Open Firebase Console | 1 min |
| Add new Android app | 2 min |
| Download google-services.json | 1 min |
| Get SHA-1 fingerprint | 2 min |
| Add SHA-1 to Firebase | 1 min |
| Enable Google Sign-In | 1 min |
| Replace google-services.json | 2 min |
| **TOTAL** | **10 min** |

---

## ✅ COMPLETION CONFIRMATION

**When you're done, you should have:**

1. ✅ New Firebase app for `com.mediguide.firstaid`
2. ✅ SHA-1 fingerprint registered
3. ✅ Google Sign-In enabled
4. ✅ New `google-services.json` in `app\` folder
5. ✅ Old file backed up as `google-services.json.old`

**Then you're ready to build the new AAB!**

---

**Questions? Issues? Let me know and I'll help!**

**Last Updated:** November 12, 2025  
**Status:** Ready to execute  
**Difficulty:** Easy (just follow steps carefully)  
**Critical:** Yes (app won't work without this)

