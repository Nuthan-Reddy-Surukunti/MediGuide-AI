# 📊 Data Types Selection - MediGuide AI

## Step 3 of 5: Data Types - What to Select

---

## ✅ SELECT THESE DATA TYPES

### **1. Location** ✅ (2/2 selected - CORRECT!)
- ✅ **Approximate location** - GPS for state detection
- ✅ **Precise location** - May be used for emergency contacts

**Keep as is - already selected correctly!**

---

### **2. Personal Info** ✅ (5/9 selected)

**Select these:**
- ✅ **Name** (from Google Sign-In or manual entry)
- ✅ **Email address** (Firebase Auth)
- ✅ **User IDs** (Firebase generates user IDs)
- ✅ **Photos** (profile photo from Google Sign-In)
- ✅ **Other info** (user preferences, settings)

**Do NOT select:**
- ❌ Address
- ❌ Phone number (you don't collect this)
- ❌ Race and ethnicity
- ❌ Political or religious beliefs

---

### **3. Financial Info** ❌ (0/4 - CORRECT!)
**Do NOT select anything** - You don't collect financial data

---

### **4. Health and Fitness** ✅ (1/2 selected)

**Currently selected (probably):**
- ✅ **Health info** OR **Fitness info**

**Recommendation:**
- ✅ Select **"Health info"** - Since you provide first aid guidance
- ❌ Do NOT select "Fitness info" - You're not a fitness app

**Note:** You don't actually COLLECT health data from users, but since you PROVIDE health guidance, select "Health info"

**IMPORTANT:** On the next screen, clarify that you PROVIDE health info but don't COLLECT it from users.

---

### **5. Messages** ❌ (0/3 - CORRECT!)
**Do NOT select anything** - You don't collect messages/emails/SMS

---

### **6. Photos and Videos** ❌ (0/2 - CORRECT!)
**Do NOT select** - Profile photos are already covered under "Personal Info → Photos"

---

### **7. Audio Files** ✅ SELECT THIS!

**Select:**
- ✅ **Voice or sound recordings** - Your app uses microphone for voice assistant

**Do NOT select:**
- ❌ Music files
- ❌ Other audio files

---

### **8. Files and Docs** ❌ (0/1 - CORRECT!)
**Do NOT select** - You don't access user files

---

### **9. Calendar** ❌ (0/1 - CORRECT!)
**Do NOT select** - You don't access calendar

---

### **10. Contacts** ❌ (0/1 - CORRECT!)
**Do NOT select** - You don't access user's phone contacts
(You provide emergency contacts, but don't access theirs)

---

### **11. App Activity** ✅ SELECT THIS!

**Select:**
- ✅ **App interactions** - Firebase Analytics tracks app usage
- ✅ **In-app search history** - User searches for guides

**Do NOT select:**
- ❌ Installed apps
- ❌ Other user-generated content
- ❌ Other actions

---

### **12. Web Browsing** ❌ (0/1 - CORRECT!)
**Do NOT select** - You don't track web browsing

---

### **13. App Info and Performance** ✅ SELECT THIS!

**Select:**
- ✅ **Crash logs** - Firebase Crashlytics
- ✅ **Diagnostics** - Firebase Analytics
- ✅ **Other app performance data** - Firebase Performance

---

### **14. Device or Other IDs** ✅ SELECT THIS!

**Select:**
- ✅ **Device or other IDs** - Firebase generates device IDs for analytics

---

## 📋 COMPLETE SELECTION SUMMARY

| Category | What to Select | Count |
|----------|----------------|-------|
| **Location** | Approximate + Precise | 2/2 ✅ |
| **Personal Info** | Name, Email, User IDs, Photos, Other | 5/9 ✅ |
| **Financial Info** | NONE | 0/4 ✅ |
| **Health and Fitness** | Health info | 1/2 ✅ |
| **Messages** | NONE | 0/3 ✅ |
| **Photos and Videos** | NONE | 0/2 ✅ |
| **Audio Files** | Voice recordings | 1/3 ✅ |
| **Files and Docs** | NONE | 0/1 ✅ |
| **Calendar** | NONE | 0/1 ✅ |
| **Contacts** | NONE | 0/1 ✅ |
| **App Activity** | App interactions, Search history | 2/5 ✅ |
| **Web Browsing** | NONE | 0/1 ✅ |
| **App Info and Performance** | Crash logs, Diagnostics, Performance | 3/3 ✅ |
| **Device IDs** | Device or other IDs | 1/1 ✅ |

**Total Categories with Data:** 8 out of 14

---

## 🎯 QUICK CHECKLIST

Go through each category and select:

### ✅ Categories to EXPAND and SELECT:

1. **Location** ✅ Already selected (2/2)
2. **Personal Info** ✅ Already selected (5/9)
3. **Health and Fitness** ✅ Select "Health info" only (1/2)
4. **Audio Files** ⚠️ **ADD: Voice or sound recordings**
5. **App Activity** ⚠️ **ADD: App interactions + Search history**
6. **App Info and Performance** ⚠️ **ADD: All 3 options**
7. **Device IDs** ⚠️ **ADD: Device or other IDs**

### ❌ Categories to LEAVE EMPTY (0 selected):

- Financial Info
- Messages
- Photos and Videos
- Files and Docs
- Calendar
- Contacts
- Web Browsing

---

## 🔍 DETAILED EXPLANATIONS

### Why "Voice or sound recordings"?
- Your app uses microphone for AI voice assistant
- Voice commands are processed by Gemini AI
- Even though not stored, they are "collected" during processing

### Why "App interactions" and "Search history"?
- Firebase Analytics tracks which features users use
- Searches within the app are logged for analytics
- This helps improve the app

### Why "Crash logs" and "Diagnostics"?
- Firebase Crashlytics collects crash data
- Firebase Analytics collects performance data
- Helps fix bugs and improve stability

### Why "Device or other IDs"?
- Firebase generates anonymous device IDs
- Used for analytics and crash reporting
- Not personally identifiable but still tracked

### Why NOT "Photos and Videos"?
- Profile photos are already covered under "Personal Info → Photos"
- You don't separately collect photo/video files

---

## ⚠️ IMPORTANT NOTES

### About Health Info:
When you select "Health info", on the NEXT screen you'll need to clarify:
- **Do you collect health data?** → **No**
- **Do you share health data?** → **No**
- You PROVIDE health guidance but don't COLLECT user health records

### About Voice Recordings:
When you select "Voice recordings", on the NEXT screen:
- **How is it collected?** → Temporarily for processing
- **Is it shared?** → Yes, with Google Gemini AI for processing
- **Is it encrypted?** → Yes
- **Can users delete?** → Not applicable (not stored)

---

## 🚀 AFTER SELECTING - NEXT SCREENS

After you select all data types and click "Next", you'll be asked:

**For EACH data type:**
1. Is it **collected**, **shared**, or **both**?
2. Is it **ephemeral** (temporary)?
3. Is it **required** or **optional**?
4. **Why** do you collect/share it?
5. Is it **encrypted in transit**?

**I'll help you with those answers for each category!**

---

## ✅ FINAL ANSWER - WHAT TO DO NOW

1. **Location** - Already correct (keep as is)
2. **Personal Info** - Already correct (keep as is)
3. **Health and Fitness** - Verify "Health info" is selected
4. **Audio Files** - **EXPAND** and select "Voice or sound recordings"
5. **App Activity** - **EXPAND** and select "App interactions" + "In-app search history"
6. **App Info and Performance** - **EXPAND** and select ALL 3 (Crash logs, Diagnostics, Performance)
7. **Device IDs** - **EXPAND** and select "Device or other IDs"

8. **Click NEXT**

---

**Status:** ✅ READY TO SELECT  
**Time:** 5-10 minutes  
**Difficulty:** Medium (lots of options)

**Select the data types listed above and click Next!** 🚀

