# 📱 Senior Launcher (0.8.2 Beta)

**The honest, open-source Android launcher for our elders. Created to make technology accessible, safe, and human again.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25-brightgreen.svg)](PRIVACY.md)
[![Status: Beta](https://img.shields.io/badge/Status-0.8.2%20Beta-orange.svg)](#)

> **⚠️ Beta Stage:** This project is built in my spare time to give our (grand)parents their digital freedom back. It is currently in beta, which means feedback and help with testing are very welcome!

---

## 📢 Call for Testers & Feedback
I am constantly working to improve the launcher, but since I don't own every type of Android device, I need your help! 

**Help improve the project:**
I am looking for testers who want to thoroughly try out the features (especially the SMS commands) on various devices (Samsung, Nokia, Motorola, Pixel, etc.). Do you have advice, a suggestion for a new feature, or did you find a bug?
- **Contact & Bug Reports:** [amine.chtaiti@gmail.com](mailto:amine.chtaiti@gmail.com)

---

## 📖 Documentation Quick Links
- 📟 **[Full SMS Command Overview](COMMANDS.md)** — A deep dive into all 45+ remote control codes.
- 📖 **[Admin & Family Guide (NL)](DOCS_BEHEERDER_NL.md)** — How to set up the app for a family member?
- 🇳🇱 **[Nederlandse versie (Dutch)](README_NL.md)**

---

## 📸 Screenshots
<p align="center">
  <img src="screenshots/screenshot_home.jpeg" width="250" title="Home Screen">
  <img src="screenshots/screenshot_phone.jpeg" width="250" title="Phone">
  <img src="screenshots/screenshot_weather.jpeg" width="250" title="Weather & Advice">
</p>
<p align="center">
  <img src="screenshots/screenshot_medication.jpeg" width="250" title="Medication">
  <img src="screenshots/screenshot_emergency.jpeg" width="250" title="Emergency Info">
  <img src="screenshots/screenshot_remote_support.jpeg" width="250" title="Remote Support">
</p>

---

## 🌟 Our Vision: "Senior-First"
Most smartphones are designed for young people. We turn that around. The Senior Launcher is built following strict UX rules for the elderly:
- **No Keyboards:** Everything works with large Plus/Minus buttons and simple lists.
- **Gigantic Elements:** Texts are at least 20-30sp. Buttons are at least 70dp high.
- **Contrast & Clarity:** No vague icons, only clear text like "HANG UP" or "SAVE".
- **Digital Peace:** No unnecessary notifications or complicated swipe gestures.

---

## ✨ Key Features

### 📞 Calling & Contacts
- **Simple Dialer:** Large number keys with haptic feedback.
- **Favorites with Photos:** Call family with one click on their face.
- **My Number:** The user's own number is always pinned at the top.
- **EMERGENCY 112:** A dedicated button with a safety confirmation to prevent accidental dials.

### 💬 Messaging (SMS)
- **Readability:** Messages grouped by day with an extra-large font.
- **Instant Zoom:** Change text size directly within the chat using large controls.
- **Safety:** Unknown or spam messages are clearly marked.

### 🆘 SOS & Safety
- **SOS Button:** Hold for 3 seconds to immediately call emergency contacts and send an SMS with exact GPS location.
- **Fall Detection:** Uses phone sensors to detect falls and automatically trigger help (Experimental).
- **Emergency Info:** Medical data (blood type, allergies) directly accessible for first responders.

### 💊 Medication & Wellness
- **Reminders:** Notifications with actual photos of the medication.
- **Stock Tracking:** The app tracks pill counts and warns the family when supplies are low.
- **Steps & Radio:** Built-in step counter and a simple radio for relaxation.

---

## 🎮 Remote Management (SMS Commands)

> **⚠️ Experimental Feature:** These features have been successfully tested on our primary devices. Administrators can manage the phone via simple SMS commands. This is the ultimate fallback when internet is unavailable.

| Category | Commands |
|-----------|------------|
| **Safety** | `#WAAR`, `#STATUS`, `#PING`, `#BEL_TERUG`, `#VEILIG ON/OFF`, `#SOS_NU` |
| **System** | `#WIFI`, `#BT`, `#STIL`, `#LAMP`, `#KNIPPER`, `#VOLUME`, `#HELDER`, `#RESTART` |
| **UI Management** | `#LETTER [1-5]`, `#THEMA [1-3]`, `#SLOT ON/OFF`, `#PIN [Code]` |
| **Data & Apps** | `#CONTACT`, `#DELETE_CONTACT`, `#OPEN [App]`, `#STOCK`, `#AGENDA_TODAY`, `#ALARM_LIST` |

---

## 🏗️ Technology & Safety
- **Kotlin 2.1 & Jetpack Compose:** Modern, fast, and stable architecture.
- **Android 16 Ready:** Using the latest APIs for maximum reliability on new devices.
- **100% Local:** Your data stays on your device. No cloud, no ads.
- **Battery Efficient:** Optimized to save battery on older devices.

---

## 🤝 Contributing & Support
This is an open-source project. If you found a bug or want to help testing, please let me know via GitHub Issues or email at [amine.chtaiti@gmail.com](mailto:amine.chtaiti@gmail.com).

*"Technology should connect people, not exclude them."* ❤️
