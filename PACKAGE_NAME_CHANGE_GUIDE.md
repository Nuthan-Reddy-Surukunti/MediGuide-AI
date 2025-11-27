# 📦 Package Name Change Guide

**Current Package:** `com.example.firstaidapp` ❌  
**New Package:** `com.mediguide.firstaid` ✅  
**Date:** November 12, 2025  
**Status:** Ready to Execute

---

## 🎯 WHY THIS IS NEEDED

Google Play Store **does not allow** package names starting with `com.example`. You'll get this error when uploading:
> "You need to use a different package name because 'com.example' is restricted."

---

## ✅ GOOD NEWS: KEYSTORE STAYS THE SAME!

**You DON'T need to change:**
- ✅ `firstaid-release.keystore` file
- ✅ `keyAlias=firstaid`
- ✅ `storePassword=Nuthan@2005`
- ✅ `keyPassword=Nuthan@2005`

**The keystore is independent of package name!**

---

## 🤖 PART 1: WHAT I (AI) WILL CHANGE AUTOMATICALLY

These files will be updated by me:

### 📝 **Build Configuration Files**

1. **`app/build.gradle.kts`**
   - Change: `namespace = "com.example.firstaidapp"`
   - To: `namespace = "com.mediguide.firstaid"`
   - Change: `applicationId = "com.example.firstaidapp"`
   - To: `applicationId = "com.mediguide.firstaid"`

2. **`settings.gradle.kts`** (check if needed)

### 📱 **Navigation & XML Files**

3. **`app/src/main/res/navigation/nav_graph.xml`**
   - Update all fragment references:
   - `com.example.firstaidapp.ui.home.HomeFragment` → `com.mediguide.firstaid.ui.home.HomeFragment`
   - (8 fragments total to update)

4. **`app/src/main/res/layout/fragment_voice_assistant.xml`**
   - Update custom view:
   - `<com.example.firstaidapp.ui.voice.EmergencyProgressView` 
   - → `<com.mediguide.firstaid.ui.voice.EmergencyProgressView`

### 💻 **Kotlin Source Files**

5. **All Kotlin files in `app/src/main/java/com/example/firstaidapp/`**
   - Refactor package declarations in all `.kt` files
   - Update all imports automatically
   - Move to new folder structure: `app/src/main/java/com/mediguide/firstaid/`

### 📄 **Documentation Files**

6. **`PLAY_STORE_SUBMISSION_GUIDE.md`**
   - Update package references

7. **`APP_NAME_MEDIGUIDE_AI.md`**
   - Update package information

8. **`COPY_PASTE_RELEASE.txt`**
   - Update app details

---

## 👤 PART 2: WHAT YOU NEED TO CHANGE MANUALLY

### 🔥 **CRITICAL: Firebase Console Updates**

You **MUST** update Firebase because Google Sign-In won't work otherwise!

#### **Step 1: Add New Package to Firebase**

1. Go to: https://console.firebase.google.com/
2. Select your project: **sample-firebase-ai-app-4a522**
3. Click **⚙️ Settings** → **Project settings**
4. Scroll to **Your apps** section
5. Find your Android app (currently shows `com.example.firstaidapp`)
6. Click **Add app** → **Android**
7. Enter new package name: `com.mediguide.firstaid`
8. Download the **NEW** `google-services.json`
9. **Replace** the file at: `C:\Users\Nuthan Reddy\FirstAidApp\app\google-services.json`

#### **Step 2: Add SHA-1 Fingerprint for New Package**

After I update the package, you'll run this command to get the NEW SHA-1:

```batch
cd "C:\Users\Nuthan Reddy\FirstAidApp"
gradlew signingReport
```

Copy the **SHA-1** from the output (it will be the SAME as before, but you need to register it for the new package).

Then in Firebase Console:
1. Go to your NEW app (`com.mediguide.firstaid`)
2. Click **⚙️ Settings** → **Project settings**
3. Scroll to your new app
4. Click **Add fingerprint**
5. Paste the SHA-1
6. Click **Save**

#### **Step 3: Update OAuth Client (for Google Sign-In)**

1. In Firebase Console, go to **Authentication** → **Sign-in method**
2. Make sure **Google** is enabled
3. The SHA-1 you added should automatically create OAuth credentials
4. Download the **updated** `google-services.json` again (if prompted)

### 📋 **Optional: Keep Old Package for Testing**

You can keep BOTH packages in Firebase:
- Old: `com.example.firstaidapp` (for local testing)
- New: `com.mediguide.firstaid` (for Play Store)

But the new APK/AAB must use `com.mediguide.firstaid`.

---

## 🚀 EXECUTION PLAN

### **Timeline:**

| Step | Who | Time | Description |
|------|-----|------|-------------|
| 1 | **AI** | 5 min | Update all code files, build files, XML |
| 2 | **AI** | 2 min | Generate new build script |
| 3 | **YOU** | 10 min | Update Firebase Console (add new package) |
| 4 | **YOU** | 2 min | Get SHA-1 fingerprint |
| 5 | **YOU** | 5 min | Add SHA-1 to Firebase |
| 6 | **YOU** | 2 min | Download new google-services.json |
| 7 | **YOU** | 1 min | Replace google-services.json file |
| 8 | **YOU** | 5 min | Build new AAB in Android Studio |
| 9 | **YOU** | 3 min | Upload to Play Console |
| **TOTAL** | | **35 min** | **Complete package change** |

---

## ⚠️ IMPORTANT WARNINGS

### **DON'T Do These:**

❌ **DON'T** manually edit every Kotlin file - I'll refactor automatically  
❌ **DON'T** create a new keystore - use your existing one  
❌ **DON'T** change keystore passwords  
❌ **DON'T** delete the old Firebase app yet (keep for reference)  
❌ **DON'T** forget to replace `google-services.json`  

### **DO These:**

✅ **DO** wait for me to update all files first  
✅ **DO** update Firebase Console immediately after  
✅ **DO** test Google Sign-In after rebuilding  
✅ **DO** verify GPS permissions still work  
✅ **DO** keep a backup of old `google-services.json`  

---

## 📝 VERIFICATION CHECKLIST

After the change, verify:

### **In Android Studio:**
- [ ] Project syncs without errors
- [ ] No red imports in Kotlin files
- [ ] Build completes successfully
- [ ] Package name shows as `com.mediguide.firstaid` in build output

### **In Firebase Console:**
- [ ] New package `com.mediguide.firstaid` added
- [ ] SHA-1 fingerprint registered
- [ ] Google Sign-In enabled
- [ ] `google-services.json` downloaded and replaced

### **After Building APK/AAB:**
- [ ] Install on physical device
- [ ] Google Sign-In works
- [ ] Profile photo/name loads
- [ ] GPS location works
- [ ] AI Assistant responds
- [ ] No crashes in any feature

---

## 🔄 ROLLBACK PLAN (If Something Goes Wrong)

If anything breaks, you can rollback:

1. **Restore old package name:**
   - Change `build.gradle.kts` back to `com.example.firstaidapp`
   - Sync project
   - Build debug APK (works locally, won't upload to Play Store)

2. **Use old google-services.json:**
   - Replace with backed-up version
   - Rebuild

3. **For Play Store:**
   - You'll still need to change package eventually
   - Can't use `com.example` on Play Store

---

## 📞 SUPPORT DURING MIGRATION

### **If You Get Errors:**

**Error: "Duplicate class found"**
- Solution: Clean project → Build → Rebuild

**Error: "Package does not match"**
- Solution: Check `google-services.json` has new package name

**Error: "Google Sign-In failed"**
- Solution: Verify SHA-1 is added to Firebase for new package

**Error: "Could not find com.mediguide.firstaid"**
- Solution: Sync Gradle, Invalidate Caches & Restart

---

## ✅ READY TO START?

**Confirm you want to proceed with:**

**New Package Name:** `com.mediguide.firstaid`

**Alternative Options:**
- `com.nuthanreddy.mediguide`
- `com.nrs.mediguide`
- `com.firstaid.mediguide`

**Once you confirm, I will:**
1. ✅ Update all 100+ files automatically
2. ✅ Refactor the entire codebase
3. ✅ Generate Firebase setup instructions
4. ✅ Create a new build script
5. ✅ Update all documentation

**Type "PROCEED" to start, or suggest a different package name.**

---

## 📊 IMPACT SUMMARY

| Category | Files Affected | Auto-Fixed by AI | Manual by You |
|----------|----------------|------------------|---------------|
| Build Config | 2 | ✅ | - |
| XML Layouts | 2 | ✅ | - |
| Navigation | 1 | ✅ | - |
| Kotlin Files | ~50+ | ✅ | - |
| Firebase Config | 1 | - | ✅ Manual |
| Documentation | 3 | ✅ | - |
| **TOTAL** | **~60** | **~59** | **1** |

**You only need to manually update Firebase!** Everything else is automated.

---

**Last Updated:** November 12, 2025  
**Status:** Waiting for your confirmation to proceed  
**Estimated Total Time:** 35 minutes  
**Risk Level:** Low (with proper Firebase updates)  
**Reversible:** Yes (can rollback if needed)

🚀 **Ready when you are!**

