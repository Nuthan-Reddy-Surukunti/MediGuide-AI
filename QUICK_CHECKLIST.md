# ✅ PACKAGE NAME CHANGE - QUICK CHECKLIST

**From:** `com.example.firstaidapp`  
**To:** `com.mediguide.firstaid`  
**Date:** November 12, 2025

---

## 🤖 AI WILL DO (Automatic)

- [ ] Update `app/build.gradle.kts` (namespace + applicationId)
- [ ] Update `app/src/main/res/navigation/nav_graph.xml` (8 fragments)
- [ ] Update `app/src/main/res/layout/fragment_voice_assistant.xml` (custom view)
- [ ] Refactor all Kotlin package declarations (~50+ files)
- [ ] Move Kotlin files to new folder structure
- [ ] Update all imports automatically
- [ ] Update `PLAY_STORE_SUBMISSION_GUIDE.md`
- [ ] Update `APP_NAME_MEDIGUIDE_AI.md`
- [ ] Update `COPY_PASTE_RELEASE.txt`

**Estimated Time:** 5 minutes (automated)

---

## 👤 YOU WILL DO (Manual)

### 🔥 Firebase Console (CRITICAL - 10 minutes)

- [ ] Go to https://console.firebase.google.com/
- [ ] Open project: sample-firebase-ai-app-4a522
- [ ] Click ⚙️ Settings → Project settings
- [ ] Click "Add app" → Android
- [ ] Enter package: `com.mediguide.firstaid`
- [ ] Click "Register app"
- [ ] Download new `google-services.json`
- [ ] Run: `gradlew signingReport` to get SHA-1
- [ ] Copy SHA-1 from output
- [ ] Add SHA-1 to Firebase (Add fingerprint button)
- [ ] Verify Google Sign-In is enabled (Authentication tab)
- [ ] Replace `app\google-services.json` with new file
- [ ] Backup old file as `google-services.json.old`

**See detailed steps in:** `FIREBASE_UPDATE_STEPS.md`

### 🏗️ Build New APK/AAB (5 minutes)

- [ ] Open Android Studio
- [ ] Sync Gradle (File → Sync Project with Gradle Files)
- [ ] Clean Project (Build → Clean Project)
- [ ] Rebuild Project (Build → Rebuild Project)
- [ ] Build → Generate Signed Bundle/APK
- [ ] Select "Android App Bundle (.aab)"
- [ ] Choose "release" build variant
- [ ] Use existing keystore (DON'T create new one!)
- [ ] Wait for build to complete
- [ ] Locate AAB in: `app\release\app-release.aab`

### 📤 Upload to Play Console (5 minutes)

- [ ] Go to Google Play Console
- [ ] Navigate to: Testing → Closed testing
- [ ] Create new release
- [ ] Upload new AAB file
- [ ] Add release name: "MediGuide AI v1.0 - Initial Release"
- [ ] Add release notes (copy from `COPY_PASTE_RELEASE.txt`)
- [ ] Review and confirm
- [ ] Start rollout to Closed testing

### 🧪 Testing After Upload (10 minutes)

- [ ] Install APK on physical device
- [ ] Test Google Sign-In (LOGIN FIRST!)
- [ ] Verify profile photo appears
- [ ] Verify name appears
- [ ] Test GPS location ("Use GPS" button)
- [ ] Test AI Assistant (online mode)
- [ ] Test AI Assistant (offline mode)
- [ ] Test voice commands
- [ ] Browse guides (check for TypeToken error)
- [ ] Add emergency contact
- [ ] Add guide to favorites
- [ ] Check all features work

---

## 📊 TIME BREAKDOWN

| Task | Who | Time |
|------|-----|------|
| Code refactoring | AI | 5 min |
| Firebase update | YOU | 10 min |
| Build new AAB | YOU | 5 min |
| Upload to Play Console | YOU | 5 min |
| Testing | YOU | 10 min |
| **TOTAL** | | **35 min** |

---

## ⚠️ CRITICAL REMINDERS

### ❌ DON'T:
- ❌ Create a new keystore
- ❌ Change keystore password
- ❌ Manually edit Kotlin files (AI does this)
- ❌ Delete old Firebase app yet
- ❌ Skip Firebase update (Google Sign-In will break!)

### ✅ DO:
- ✅ Use existing keystore (`firstaid-release.keystore`)
- ✅ Update Firebase Console
- ✅ Replace `google-services.json`
- ✅ Test Google Sign-In thoroughly
- ✅ Test on physical device, not emulator

---

## 🆘 IF SOMETHING BREAKS

### Google Sign-In fails:
1. Check SHA-1 is added to Firebase for NEW package
2. Verify `google-services.json` has `com.mediguide.firstaid`
3. Check Authentication → Google is enabled in Firebase

### TypeToken error (guide view crash):
- This should be fixed already (ProGuard rules)
- If still occurs, check `proguard-rules.pro` has Gson rules

### GPS permission denied:
- Check `AndroidManifest.xml` has location permissions
- Test on Android 10+ device (permissions changed)

### Build errors:
1. File → Invalidate Caches → Invalidate and Restart
2. Build → Clean Project
3. Build → Rebuild Project

---

## 📞 READY TO START?

**Before proceeding, confirm:**

1. ✅ You've read `PACKAGE_NAME_CHANGE_GUIDE.md`
2. ✅ You've read `FIREBASE_UPDATE_STEPS.md`
3. ✅ You have access to Firebase Console
4. ✅ You have Android Studio open
5. ✅ You're ready to spend ~35 minutes on this

**Type "PROCEED WITH PACKAGE CHANGE" to start!**

---

## 🎯 SUCCESS CRITERIA

You'll know it's successful when:

1. ✅ Play Console accepts the AAB upload (no "com.example" error)
2. ✅ Google Sign-In works in the production APK
3. ✅ Profile photo and name load correctly
4. ✅ GPS location detection works
5. ✅ All features work without crashes
6. ✅ Package name shows as `com.mediguide.firstaid` in device settings

---

**Last Updated:** November 12, 2025  
**Status:** Waiting for confirmation  
**Files to Reference:**
- `PACKAGE_NAME_CHANGE_GUIDE.md` - Full explanation
- `FIREBASE_UPDATE_STEPS.md` - Step-by-step Firebase guide
- This file - Quick checklist

**Let's do this! 🚀**

