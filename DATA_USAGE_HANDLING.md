# 📋 Data Usage and Handling - MediGuide AI

## Step 4 of 5: Data Usage and Handling - Complete Answers

---

## 🎯 OVERVIEW

## 🚨 **CRITICAL: DO NOT SELECT "HEALTH AND FITNESS"** 🚨

**❌ REMOVE "Health and Fitness > Health info" if you selected it!**

**Why?** You PROVIDE health information but DON'T COLLECT user health data.
- Your app shows first aid guides (educational content)
- You don't collect user medical records or health metrics
- Selecting this makes users think you collect their personal health data

**If you already selected it:** Go back to Step 3 and DESELECT it now!

---

You need to complete forms for **12 data types** across **7 categories**:

1. **Personal Info**: Name, Email, User IDs, Other info (4 items)
2. **Location**: Approximate + Precise (2 items)
3. **Audio Files**: Voice recordings (1 item)
4. ~~**Health and Fitness**~~: ❌ **DO NOT SELECT THIS**
5. **App Activity**: App interactions, Search history (2 items)
6. **App Info and Performance**: Crash logs, Diagnostics, Performance (3 items - may be grouped as 1)
7. **Device IDs**: Device or other IDs (1 item)

**Total: 12 forms to fill** (NOT 13 - don't include Health Info!)

---

## 📝 COMPLETE ANSWERS FOR EACH DATA TYPE

---

## ℹ️ AVAILABLE OPTIONS REFERENCE

### **Why is this user data COLLECTED?** (7 options available)
1. ✅ **App functionality** - Features in your app, enable functionality, authenticate users
2. ✅ **Analytics** - Data about how users use app, app performance, crashes, bugs
3. ✅ **Developer communications** - Send news/notifications about app
4. ❌ **Advertising or marketing** - Display/target ads, marketing (NOT USED IN YOUR APP)
5. ✅ **Fraud prevention, security, and compliance** - Monitor failed logins, security
6. ✅ **Personalization** - Customize app, show recommendations
7. ✅ **Account management** - Setup/management of user accounts, login, verify credentials

### **Why is this user data SHARED?** (Same 7 options)
1. ✅ **App functionality** - Shared with third parties for app features
2. ✅ **Analytics** - Shared for performance monitoring
3. ✅ **Developer communications** - Shared for notifications
4. ❌ **Advertising or marketing** - NOT USED
5. ✅ **Fraud prevention, security, and compliance** - Shared for security
6. ✅ **Personalization** - Shared for customization
7. ✅ **Account management** - Shared for account features

---

## 1️⃣ PERSONAL INFO - NAME

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Optional**
  - Users can sign in with Google (provides name) or email/password (may not provide name)

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Display user name in profile
- ✅ **Personalization** - Personalized greetings and user experience
- ✅ **Account management** - Part of user profile management

### **Why is this user data shared?** (Select all that apply)
- ✅ **App functionality** - Shared with Firebase Authentication for user profile
- ✅ **Account management** - Shared with Firebase for account management

### **Is this data processed ephemerally?**
- ❌ **No** - Name is stored in Firebase and user profile

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Users can choose whether this data is collected**

### **Can users request deletion?**
- ✅ **Yes** - Via account deletion

---

## 2️⃣ PERSONAL INFO - EMAIL ADDRESS

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Required**
  - Email is required for account creation and authentication

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Password reset, account recovery features
- ✅ **Account management** - User authentication and login
- ✅ **Developer communications** - Send account-related notifications (if applicable)
- ✅ **Fraud prevention, security, and compliance** - Verify user identity, prevent multiple accounts

### **Why is this user data shared?** (Select all that apply)
- ✅ **App functionality** - Shared with Firebase Authentication
- ✅ **Account management** - Shared with Firebase for account management

### **Is this data processed ephemerally?**
- ❌ **No** - Email is stored for account management

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Data collection is required** (users can't use the app without it)

### **Can users request deletion?**
- ✅ **Yes** - Via account deletion

---

## 3️⃣ PERSONAL INFO - USER IDs

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Required**
  - Firebase automatically generates user IDs

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Link user data to specific accounts
- ✅ **Account management** - Identify and manage user accounts
- ✅ **Analytics** - Track app usage per user
- ✅ **Fraud prevention, security, and compliance** - Detect suspicious activity

### **Why is this user data shared?** (Select all that apply)
- ✅ **App functionality** - Shared with Firebase services
- ✅ **Analytics** - Shared with Firebase Analytics
- ✅ **Account management** - Shared with Firebase Authentication

### **Is this data processed ephemerally?**
- ❌ **No** - User IDs are stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Data collection is required**

### **Can users request deletion?**
- ✅ **Yes** - Via account deletion

---

## 4️⃣ PERSONAL INFO - OTHER INFO

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ❌ **Shared** (No - stored locally/Firebase but not shared with third parties)

### **Is collection of this data required or optional?**
- ⭕ **Optional**
  - User preferences, profile photo, settings

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Store user preferences and settings
- ✅ **Personalization** - Customize user experience based on preferences

### **Why is this user data shared?** (Select all that apply)
- ❌ **Not applicable** - This data is not shared

### **Is this data processed ephemerally?**
- ❌ **No** - Preferences are stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Users can choose whether this data is collected**

### **Can users request deletion?**
- ✅ **Yes** - Via account deletion

---

## 5️⃣ LOCATION - APPROXIMATE LOCATION

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Optional**
  - Users can choose to share location or select state manually

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Detect user's state for emergency numbers
- ✅ **Personalization** - Show state-specific emergency contacts

### **Why is this user data shared?** (Select all that apply)
- ✅ **App functionality** - Shared with Google Location Services

### **Is this data processed ephemerally?**
- ✅ **Yes** - Location is used once to detect state, not stored permanently
  - **Note:** If you DO store the state, select "No"

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Users can choose whether this data is collected**
  - They can use GPS or manually select state

### **Can users request deletion?**
- ✅ **Yes** - State selection can be changed/cleared

---

## 6️⃣ LOCATION - PRECISE LOCATION

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Optional**

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Detect user's state for emergency services

### **Why is this user data shared?** (Select all that apply)
- ✅ **App functionality** - Shared with Google Location Services

### **Is this data processed ephemerally?**
- ✅ **Yes** - Used only to determine state, not stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Users can choose whether this data is collected**

### **Can users request deletion?**
- ✅ **Yes** - Not stored, but state selection can be cleared

---

## 7️⃣ AUDIO FILES - VOICE OR SOUND RECORDINGS

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Optional**
  - Users choose to use voice assistant feature

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Voice commands for AI assistant

### **Why is this user data shared?** (Select all that apply)
- ✅ **App functionality** - Shared with Google Gemini AI for processing voice commands

### **Is this data processed ephemerally?**
- ✅ **Yes** - Voice recordings are processed in real-time and not stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Users can choose whether this data is collected**
  - Voice assistant is optional, users can type instead

### **Can users request deletion?**
- ⭕ **Not applicable** - Data is not stored (ephemeral)

---

## 8️⃣ HEALTH AND FITNESS - HEALTH INFO

## 🚨 **DO NOT SELECT THIS CATEGORY** 🚨

### **IMPORTANT: GO BACK AND DESELECT "Health and Fitness"**

**Why you should NOT select this:**
- You PROVIDE first aid guides (educational content)
- You DON'T COLLECT user health data (medical records, fitness tracking, symptoms, etc.)
- Selecting this makes users think you're collecting their personal health information

**What Google means by "Health Info":**
- Personal medical records
- User's symptoms or diagnoses
- Fitness tracking data (steps, heart rate, etc.)
- Health metrics entered by users
- Prescription information

**What your app actually does:**
- Shows pre-written first aid guides
- Displays educational health content
- No user health data is collected

### **ACTION REQUIRED:**
1. Go back to **Step 3 - Data Types**
2. **UNCHECK** "Health and fitness > Health info"
3. Proceed to Step 4 without this category

### **How to mention health content instead:**
- ✅ App description: "Provides first aid and medical emergency guidance"
- ✅ App category: Select "Medical" or "Health & Fitness" category
- ✅ Screenshots: Show your health content
- ❌ Data Safety: DON'T select "Health Info" data type

**If you've already filled this out:** Delete it and remove from Step 3!

---

## ❌ SECTION REMOVED - DO NOT FILL OUT ❌

**The following section (8️⃣ Health Info) has been removed.**
**Skip to section 9️⃣ (App Activity) below.**

---

## 9️⃣ APP ACTIVITY - APP INTERACTIONS

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Required**
  - Firebase Analytics automatically tracks interactions

### **Why is this user data collected?** (Select all that apply)
- ✅ **Analytics** - Understand how users use the app
- ✅ **App functionality** - Track feature usage
- ✅ **Product improvement** - Improve app based on usage patterns

### **Why is this user data shared?** (Select all that apply)
- ✅ **Analytics** - Shared with Firebase Analytics

### **Is this data processed ephemerally?**
- ❌ **No** - Analytics data is stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Data collection is required**
  - Part of core Firebase integration

### **Can users request deletion?**
- ✅ **Yes** - Via account deletion

---

## 🔟 APP ACTIVITY - IN-APP SEARCH HISTORY

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Optional**
  - Only collected if user uses search feature

### **Why is this user data collected?** (Select all that apply)
- ✅ **Analytics** - Understand what users search for
- ✅ **App functionality** - Show search history to user
- ✅ **Personalization** - Improve search results

### **Why is this user data shared?** (Select all that apply)
- ✅ **Analytics** - Shared with Firebase Analytics

### **Is this data processed ephemerally?**
- ❌ **No** - Search history may be stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Users can choose whether this data is collected**
  - They can choose not to use search

### **Can users request deletion?**
- ✅ **Yes** - Via clearing search history or account deletion

---

## 1️⃣1️⃣ APP INFO AND PERFORMANCE - CRASH LOGS

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Required**
  - Firebase Crashlytics automatically collects crash data

### **Why is this user data collected?** (Select all that apply)
- ✅ **App functionality** - Identify and fix crashes
- ✅ **Analytics** - Monitor app stability
- ✅ **Developer communications** - Inform about critical issues

### **Why is this user data shared?** (Select all that apply)
- ✅ **Analytics** - Shared with Firebase Crashlytics

### **Is this data processed ephemerally?**
- ❌ **No** - Crash logs are stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Data collection is required**

### **Can users request deletion?**
- ✅ **Yes** - Crash data can be deleted

---

## 1️⃣2️⃣ APP INFO AND PERFORMANCE - DIAGNOSTICS

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Required**

### **Why is this user data collected?** (Select all that apply)
- ✅ **Analytics** - Monitor app health
- ✅ **App functionality** - Diagnose issues
- ✅ **Product improvement** - Improve performance

### **Why is this user data shared?** (Select all that apply)
- ✅ **Analytics** - Shared with Firebase

### **Is this data processed ephemerally?**
- ❌ **No**

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Data collection is required**

### **Can users request deletion?**
- ✅ **Yes**

---

## 1️⃣3️⃣ APP INFO AND PERFORMANCE - OTHER APP PERFORMANCE DATA

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Required**

### **Why is this user data collected?** (Select all that apply)
- ✅ **Analytics** - Monitor app performance
- ✅ **App functionality** - Optimize app speed
- ✅ **Product improvement** - Enhance user experience

### **Why is this user data shared?** (Select all that apply)
- ✅ **Analytics** - Shared with Firebase Performance Monitoring

### **Is this data processed ephemerally?**
- ❌ **No**

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Data collection is required**

### **Can users request deletion?**
- ✅ **Yes**

---

## 1️⃣4️⃣ DEVICE OR OTHER IDs - DEVICE OR OTHER IDs

### **Is this data collected, shared, or both?**
- ✅ **Collected**
- ✅ **Shared**

### **Is collection of this data required or optional?**
- ⭕ **Required**
  - Firebase automatically generates device IDs

### **Why is this user data collected?** (Select all that apply)
- ✅ **Analytics** - Track unique devices
- ✅ **App functionality** - Identify devices for Firebase
- ✅ **Fraud prevention, security, and compliance** - Detect suspicious activity

### **Why is this user data shared?** (Select all that apply)
- ✅ **Analytics** - Shared with Firebase services

### **Is this data processed ephemerally?**
- ❌ **No** - Device IDs are stored

### **Is this data required for your app, or can users choose whether it's collected?**
- ⭕ **Data collection is required**

### **Can users request deletion?**
- ✅ **Yes** - Via account deletion

---

## 📊 SUMMARY TABLE

| Data Type | Collected | Shared | Required/Optional | Ephemeral | Can Delete |
|-----------|-----------|--------|-------------------|-----------|------------|
| **Name** | ✅ | ✅ | Optional | ❌ | ✅ |
| **Email** | ✅ | ✅ | Required | ❌ | ✅ |
| **User IDs** | ✅ | ✅ | Required | ❌ | ✅ |
| **Other Info** | ✅ | ❌ | Optional | ❌ | ✅ |
| **Approximate Location** | ✅ | ✅ | Optional | ✅ | ✅ |
| **Precise Location** | ✅ | ✅ | Optional | ✅ | ✅ |
| **Voice Recordings** | ✅ | ✅ | Optional | ✅ | N/A |
| **App Interactions** | ✅ | ✅ | Required | ❌ | ✅ |
| **Search History** | ✅ | ✅ | Optional | ❌ | ✅ |
| **Crash Logs** | ✅ | ✅ | Required | ❌ | ✅ |
| **Diagnostics** | ✅ | ✅ | Required | ❌ | ✅ |
| **Performance Data** | ✅ | ✅ | Required | ❌ | ✅ |
| **Device IDs** | ✅ | ✅ | Required | ❌ | ✅ |

---

## ⚠️ IMPORTANT NOTES

### 1. **All Data is Encrypted in Transit**
For the question: "Is all of the user data collected by your app encrypted in transit?"
- ✅ **YES** - All data sent to Firebase is encrypted via HTTPS/TLS

### 2. **Third Parties You Share Data With**
You share data with:
- **Google Firebase** (Authentication, Analytics, Crashlytics, Performance)
- **Google Gemini AI** (Voice processing only)
- **Google Location Services** (GPS only)

### 3. **Purpose of Data Collection**
Main purposes across all categories:
- ✅ App functionality
- ✅ Analytics
- ✅ Personalization
- ✅ Account management
- ✅ Fraud prevention, security, and compliance

---

## 🎯 QUICK WORKFLOW

For each data type, click **"Start"** and follow this pattern:

1. **Is it collected/shared?** → Both (usually) or see table above
2. **Required or optional?** → See table above
3. **Why collected?** → App functionality + Analytics (usually)
4. **Why shared?** → App functionality (shared with Firebase)
5. **Ephemeral?** → NO (except Location and Voice)
6. **Users can choose?** → Depends (see table)
7. **Can users delete?** → YES (via account deletion)

---

## ✅ ENCRYPTION CONFIRMATION

**Final question after all data types:**

### "Is all of the user data collected by your app encrypted in transit?"
- ✅ **YES**

**Explanation to provide:**
"All user data is transmitted using HTTPS/TLS encryption. Firebase services enforce encryption in transit by default. Voice data sent to Gemini AI is also encrypted."

---

## 🚀 NEXT STEPS

1. Click **"Start"** on each data type
2. Fill in the answers using this guide
3. Click **"Save"** for each data type
4. Verify all data types show "Completed" status
5. Click **"Next"** to proceed to Step 5

---

**Status:** ✅ READY TO FILL  
**Time Required:** 20-30 minutes (11-14 forms)  
**Difficulty:** Medium (repetitive but important)

**Fill out each data type form carefully using the answers above!** 🚀

---

## 📝 OPTIONAL: HEALTH INFO CLARIFICATION

If you decide to KEEP "Health Info" category, here's what to say:

**Custom explanation:**
"MediGuide AI provides first aid guidance and educational health information to users. We do not collect, store, or share any personal health data from users. The health information shown is general educational content, not user-specific health records."

**Then SELECT:**
- ❌ Collected: NO
- ❌ Shared: NO

**But this creates a paradox** - why declare it if you don't collect/share it?

**RECOMMENDED ACTION:** Go back to Step 3 and **DESELECT** "Health and Fitness" entirely.

---


