# 📱 Senior Launcher (0.8.2 Beta)

**The honest, open-source Android launcher for our elders. Created to make technology accessible, safe, and human again.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25-brightgreen.svg)](PRIVACY.md)
[![Status: Beta](https://img.shields.io/badge/Status-0.8.2%20Beta-orange.svg)](#)

> **⚠️ Beta Stage:** This project is currently in the beta phase. This means you might encounter some bugs. I am building this project in my spare time to give our (grand)parents their digital freedom back.

---

## 📢 Call for Testers & Feedback
I am constantly working to improve the launcher, but since I don't own every type of Android device, I need your help!

**Help improve the project:**
I am looking for testers who want to thoroughly try out the features (especially the SMS commands) on various devices (Samsung, Nokia, Motorola, etc.). Do you have advice, a suggestion for a new feature, or did you find a bug?
- **Contact & Bug Reports:** [amine.chtaiti@gmail.com](mailto:amine.chtaiti@gmail.com)

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

[Nederlandse versie hier](README_NL.md) | [Admin Guide (NL)](DOCS_BEHEERDER_NL.md)

---

## 🌟 Our Vision: "Senior-First"
Most smartphones are designed for young people. We turn that around. The Senior Launcher is built following strict UX rules for the elderly:
- **No Keyboards:** Everything works with large plus/min buttons and clear lists.
- **Gigantic Elements:** Texts are at least 20-30sp. Buttons are at least 70dp high.
- **Contrast & Clarity:** No vague icons, only clear text like "HANG UP" or "SAVE".
- **Digital Peace:** No unnecessary notifications or complicated swipe gestures.

---

## ✨ Key Features

### 📞 Calling & Contacts
- **Simple Dialer:** Large number keys with haptic feedback.
- **Favorites with Photos:** Call family with one click on their face.
- **My Number:** The user's own number is always pinned at the top for easy reference.
- **EMERGENCY 112:** A dedicated button with a safety confirmation to prevent false alarms.

### 💬 Messaging (SMS)
- **Readability:** Messages grouped by day with an extra-large font.
- **Instant Zoom:** Change text size directly within the chat using large controls.
- **Safety:** Unknown or spam messages are clearly marked.

### 🆘 SOS & Safety
- **SOS Button:** Hold for 3 seconds to immediately call emergency contacts and send an SMS with exact GPS location.
- **Fall Detection:** Uses phone sensors to detect falls and automatically trigger help (Experimental).
- **Emergency Info:** Medical data (blood type, allergies) directly accessible from the home screen for first responders.

### 💊 Medication & Wellness
- **Reminders:** Notifications with actual photos of the medication.
- **Stock Tracking:** The app tracks pill counts and warns the family when supplies are low.
- **Steps & Radio:** Built-in step counter and a simple radio for relaxation.

---

## 🎮 Remote Management (Remote Support)

> **⚠️ Experimental Feature:** These features have been successfully tested on our primary devices. Due to the wide variety of Android hardware, we consider this experimental. Administrators can manage the phone via simple SMS commands.

### Core Commands (Safety & Location)
| Command | Action | Example |
|---------|--------|---------|
| **`#WAAR`** | Receive current GPS location via Google Maps link. | `#WAAR` |
| **`#STATUS`** | Check Battery %, Volume, and Silent mode. | `#STATUS` |
| **`#PING`** | Show an "ARE YOU OKAY?" check on the screen. | `#PING` |
| **`#BEL_TERUG`**| The phone automatically calls the administrator back. | `#BEL_TERUG` |
| **`#BERICHT`** | Show a large popup that is read aloud. | `#BERICHT Dinner at 6PM` |
| **`#VEILIG`** | Toggle Anti-Scam filter (block unknown numbers). | `#VEILIG ON` |

### System & Connectivity
| Command | Action | Example |
|---------|--------|---------|
| **`#WIFI`** | Remotely toggle WiFi. | `#WIFI ON` |
| **`#BT`** | Remotely toggle Bluetooth (for hearing aids). | `#BT ON` |
| **`#STIL`** | Force "Do Not Disturb" off. | `#STIL OFF` |
| **`#LAMP`** | Turn the flashlight ON or OFF. | `#LAMP ON` |
| **`#KNIPPER`** | Make the flashlight blink 10x (find phone). | `#KNIPPER` |
| **`#VOLUME`** | Set ringtone volume (scale 0-10). | `#VOLUME 10` |
| **`#HELDER`** | Set screen brightness (scale 1-10). | `#HELDER 10` |

### UI & User Management
| Command | Action | Example |
|---------|--------|---------|
| **`#LETTER`** | Change text size (level 1-5). | `#LETTER 4` |
| **`#THEMA`** | Switch theme (1=Classic, 2=Contrast, 3=Light). | `#THEMA 2` |
| **`#SLOT`** | Lock the settings menu for the senior. | `#SLOT ON` |
| **`#PIN`** | Remotely change the launcher's PIN code. | `#PIN 1234` |
| **`#CONTACT`** | Remotely add a contact to the home screen. | `#CONTACT John 061234` |
| **`#DELETE_CONTACT`**| Remotely remove a contact. | `#DELETE_CONTACT John` |

### Apps & Diagnostics
| Command | Action | Example |
|---------|--------|---------|
| **`#OPEN`** | Remotely open a specific app (e.g., WhatsApp). | `#OPEN WhatsApp` |
| **`#APP_LIST`**| Receive a list of all installed apps. | `#APP_LIST` |
| **`#NOTIFICATIONS_AWAY`**| Clear all active notifications on the screen. | `#NOTIFICATIONS_AWAY` |
| **`#INFO_PLUS`**| Detailed diagnostics (storage, Android version). | `#INFO_PLUS` |
| **`#STOCK`**| Update medication stock count. | `#STOCK Aspirin 30` |
| **`#HELP`** | Receive an overview of all codes. | `#HELP` |

---

## 🏗️ Technology & Safety
- **Kotlin 2.1 & Jetpack Compose:** Modern, fast, and stable code.
- **Android 16 Ready:** Using the latest APIs for maximum reliability on new devices.
- **Privacy:** 100% local. No data collection, no cloud, no ads.
- **Battery Efficient:** Optimized to save battery on older devices.

---

## 🤝 Contributing & Support
This is an open-source project. If you found a bug or want to help testing, please let me know via GitHub Issues or email at [amine.chtaiti@gmail.com](mailto:amine.chtaiti@gmail.com).

*"Technology should connect people, not exclude them."* ❤️
