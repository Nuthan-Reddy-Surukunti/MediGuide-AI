# 🔐 ALL SHA FINGERPRINTS FOR FIREBASE

**Generated:** November 12, 2025  
**Package Name:** com.mediguide.firstaid  
**Firebase Project:** sample-firebase-ai-app-4a522

---

## 📋 ALL 4 FINGERPRINTS TO ADD TO FIREBASE

You need to add **ALL 4** of these fingerprints to your Firebase app (`com.mediguide.firstaid`).

---

### 🔧 **DEBUG BUILD** (For testing during development)

**SHA-1 (Debug):**
```
46:52:53:0A:BE:DA:27:01:08:5A:28:B7:AB:2B:DF:2F:D9:C8:0E:4F
```

**SHA-256 (Debug):**
```
67:92:C0:AE:10:39:65:17:88:AE:48:8A:AD:7A:BA:FF:09:49:6D:CC:37:0E:A8:61:5D:19:90:6F:D9:2F:30:67
```

**Source:** `C:\Users\Nuthan Reddy\.android\debug.keystore`  
**Used for:** Testing in Android Studio, Debug APK  
**Valid until:** Sunday, 22 November, 2054

---

### 🚀 **RELEASE BUILD** (For Play Store signed APK/AAB)

**SHA-1 (Release):**
```
23:B2:25:F3:FE:D5:01:73:D1:A0:A0:98:3C:4A:D7:0C:52:C4:FA:8B
```

**SHA-256 (Release):**
```
F2:39:20:9E:FC:4D:E6:D4:9D:6D:F8:FB:24:72:60:85:38:5B:8E:35:3E:97:56:F1:29:66:B8:7B:7F:E8:74:2C
```

**Source:** `C:\Users\Nuthan Reddy\FirstAidApp\firstaid-release.keystore`  
**Used for:** Signed APK, Signed AAB, Play Store builds  
**Valid until:** Tuesday, 25 March, 2053

---

## 🔥 HOW TO ADD TO FIREBASE (Step-by-Step)

### Step 1: Go to Firebase Console
1. Open: https://console.firebase.google.com/
2. Select project: **sample-firebase-ai-app-4a522**
3. Click ⚙️ **Settings** → **Project settings**
4. Scroll to **"Your apps"** section
5. Find your app: **com.mediguide.firstaid**

### Step 2: Add Fingerprints One by One

Under "SHA certificate fingerprints", click **"Add fingerprint"** and add these **4 fingerprints**:

#### Add Fingerprint #1 (Debug SHA-1):
```
46:52:53:0A:BE:DA:27:01:08:5A:28:B7:AB:2B:DF:2F:D9:C8:0E:4F
```
Click **Save**

#### Add Fingerprint #2 (Debug SHA-256):
```
67:92:C0:AE:10:39:65:17:88:AE:48:8A:AD:7A:BA:FF:09:49:6D:CC:37:0E:A8:61:5D:19:90:6F:D9:2F:30:67
```
Click **Save**

#### Add Fingerprint #3 (Release SHA-1):
```
23:B2:25:F3:FE:D5:01:73:D1:A0:A0:98:3C:4A:D7:0C:52:C4:FA:8B
```
Click **Save**

#### Add Fingerprint #4 (Release SHA-256):
```
F2:39:20:9E:FC:4D:E6:D4:9D:6D:F8:FB:24:72:60:85:38:5B:8E:35:3E:97:56:F1:29:66:B8:7B:7F:E8:74:2C
```
Click **Save**

---

## ✅ VERIFICATION

After adding all 4, your Firebase app should show:

```
com.mediguide.firstaid
└── SHA certificate fingerprints (4)
    ├── SHA-1: 46:52:53:0A:BE:DA:27:01:08:5A:28:B7:AB:2B:DF:2F:D9:C8:0E:4F ✅
    ├── SHA-256: 67:92:C0:AE:10:39:65:17:88:AE:48:8A:AD:7A:BA:FF:09:49:6D:CC:37:0E:A8:61:5D:19:90:6F:D9:2F:30:67 ✅
    ├── SHA-1: 23:B2:25:F3:FE:D5:01:73:D1:A0:A0:98:3C:4A:D7:0C:52:C4:FA:8B ✅
    └── SHA-256: F2:39:20:9E:FC:4D:E6:D4:9D:6D:F8:FB:24:72:60:85:38:5B:8E:35:3E:97:56:F1:29:66:B8:7B:7F:E8:74:2C ✅
```

---

## 🎯 WHY YOU NEED ALL 4

| Fingerprint | Type | Used For |
|-------------|------|----------|
| Debug SHA-1 | SHA-1 | Testing in Android Studio, Google Sign-In in debug mode |
| Debug SHA-256 | SHA-256 | Additional security for debug builds |
| Release SHA-1 | SHA-1 | **CRITICAL for Play Store** - Google Sign-In in production |
| Release SHA-256 | SHA-256 | Additional security for production builds |

**Without the Release SHA-1:** Google Sign-In will NOT work in your signed APK/AAB! ⚠️

---

## 📝 COPY-PASTE READY FORMAT

If you want to copy all at once, here they are without colons (some systems need this):

### Debug SHA-1 (no colons):
```
4652530ABEDA2701085A28B7AB2BDF2FD9C80E4F
```

### Debug SHA-256 (no colons):
```
6792C0AE103965178AE488AAD7ABAFF009496DCC370EA8615D19906FD92F3067
```

### Release SHA-1 (no colons):
```
23B225F3FED50173D1A0A0983C4AD70C52C4FA8B
```

### Release SHA-256 (no colons):
```
F239209EFC4DE6D49D6DF8FB24726085385B8E353E9756F12966B87B7FE8742C
```

**Note:** Firebase usually accepts both formats (with or without colons).

---

## ⚠️ IMPORTANT NOTES

1. **Add ALL 4 fingerprints** - Don't skip any!
2. **Debug fingerprints** allow Google Sign-In to work when testing in Android Studio
3. **Release fingerprints** allow Google Sign-In to work in signed APK/AAB uploaded to Play Store
4. **SHA-1 is mandatory** for Google Sign-In
5. **SHA-256 is optional** but recommended for better security

---

## 🔄 AFTER ADDING FINGERPRINTS

1. Download the **updated google-services.json** from Firebase
2. Replace the file in: `C:\Users\Nuthan Reddy\FirstAidApp\app\google-services.json`
3. Sync Gradle in Android Studio
4. Rebuild project
5. Test Google Sign-In in both debug and release builds

---

## 📊 FULL DETAILS (From signingReport)

### Debug Build:
```
Variant: debug
Config: debug
Store: C:\Users\Nuthan Reddy\.android\debug.keystore
Alias: AndroidDebugKey
MD5: C1:1D:33:C9:42:11:CC:77:C5:4F:F6:52:49:43:FF:68
SHA1: 46:52:53:0A:BE:DA:27:01:08:5A:28:B7:AB:2B:DF:2F:D9:C8:0E:4F
SHA-256: 67:92:C0:AE:10:39:65:17:88:AE:48:8A:AD:7A:BA:FF:09:49:6D:CC:37:0E:A8:61:5D:19:90:6F:D9:2F:30:67
Valid until: Sunday, 22 November, 2054
```

### Release Build:
```
Variant: release
Config: release
Store: C:\Users\Nuthan Reddy\FirstAidApp\firstaid-release.keystore
Alias: firstaid
MD5: B7:D7:BF:11:40:14:0E:3E:D2:E8:F0:61:A0:4F:11:72
SHA1: 23:B2:25:F3:FE:D5:01:73:D1:A0:A0:98:3C:4A:D7:0C:52:C4:FA:8B
SHA-256: F2:39:20:9E:FC:4D:E6:D4:9D:6D:F8:FB:24:72:60:85:38:5B:8E:35:3E:97:56:F1:29:66:B8:7B:7F:E8:74:2C
Valid until: Tuesday, 25 March, 2053
```

---

## ✅ CHECKLIST

- [ ] Copied Debug SHA-1
- [ ] Copied Debug SHA-256
- [ ] Copied Release SHA-1
- [ ] Copied Release SHA-256
- [ ] Opened Firebase Console
- [ ] Found app: com.mediguide.firstaid
- [ ] Added all 4 fingerprints
- [ ] Downloaded updated google-services.json
- [ ] Replaced file in app/ folder
- [ ] Ready to build!

---

**You're all set! Add these 4 fingerprints to Firebase and you'll be ready to build your app! 🚀**

**Status:** ✅ Ready to copy-paste into Firebase  
**Time needed:** 5 minutes to add all 4

