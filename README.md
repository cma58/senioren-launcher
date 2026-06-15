# 📱 Senioren Launcher 🛡️ (v0.8.8)

[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/cma58/senioren-launcher)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25-brightgreen.svg)](PRIVACY.md)
[![Android 14+](https://img.shields.io/badge/Android-14%2B-green.svg)](https://developer.android.com)

**The honest, open-source Android launcher for our elders. Created to make technology accessible, safe, and human again.**

100% Free & Open Source – No trackers, no clouds, just digital freedom. Optimized for city-wide distribution (Project Ghent).

---

## 🔗 The Ecosystem: How it works
The **Senioren Launcher** is part of a 2-app ecosystem designed for maximum safety and peace of mind.

1.  **Senioren Launcher** (this app): Installed on the **senior's phone**. It provides a simple, safe interface and executes remote commands via secure SMS.
2.  **[Senioren Beheerder](https://github.com/cma58/SeniorenBeheerder)**: Installed on the **caregiver's phone**. It acts as a remote dashboard to monitor status, location, and settings via encrypted SMS commands.

---

## 🌟 Why the Senioren Launcher?
Most smartphones are designed for digital natives. We turn that around. The Senioren Launcher is built following strict UX rules for the elderly:
- **Zero-Learning Curve:** No complicated swipe gestures or hidden menus. Everything is immediately visible.
- **Visual Clarity:** Extra-large touch targets (90dp+), high-contrast themes, and customizable font sizes up to 36sp.
- **Audio-Visual Feedback:** Integrated Text-to-Speech (TTS) for alarms and weather, plus haptic feedback for every interaction.
- **Digital Peace:** Zero unnecessary notifications, ads, or confusing pop-ups. Only the essentials.

---

## ✨ Key Features & Innovations

### 🏠 Intelligent Home Screen
- **Flexible Layouts:** Choose between 1x1 (Massive), 2x3 (Large), or 3x4 (Compact) grids.
- **Smart Status Badges:** Visual feedback for missed calls and unread messages.
- **Night Lamp Mode:** Long-press the alarm screen to transform the display into a soft orange night light.
- **PIN-Protected Settings:** The management menu is locked to prevent accidental configuration changes.

### 🆘 Emergency & Active Safety
- **One-Tap SOS:** A large, prominent button triggers a countdown before calling emergency services and sharing GPS coordinates via SMS.
- **Fall Detection:** Automatically detects heavy impacts using device sensors and notifies family members immediately.
- **Intercom Mode:** During an SOS call, the speakerphone is automatically forced on, allowing communication without holding the phone.
- **Battery Monitoring:** Automated SMS alerts are sent to caregivers when the phone drops below 15% charge.

### 💊 Health & Activity Suite
- **Visual Medication Reminders:** Alarms show actual photos of the medication (e.g., "the blue pill") to prevent errors.
- **Caregiver Medication Alerts:** Automatically sends an SMS to SOS contacts if a dose is not confirmed within 5 minutes.
- **Integrated Step Counter:** Tracks daily movement directly on the home screen without requiring a wearable device.
- **Stock Tracking:** Automatically logs usage and counts pills to ensure refills are ordered on time.

### 🌤️ Hyper-Local Weather & Safety Advice
- **Safety Traffic Light:** Color codes (Green/Orange/Red) indicate whether it's safe to go outside based on weather extremes.
- **Personalized Tips:** Contextual advice for clothing ("wear a scarf"), garden care, window safety, and UV protection.
- **Day-Part Forecast:** A simplified view of conditions for Morning, Afternoon, Evening, and Night.

---

## 🎮 Remote Management (via SMS Commands)
Caregivers can manage the phone from anywhere, even without an internet connection:
- **Remote Message Overlay:** Send urgent text messages that take over the full screen and read themselves out loud to the senior.
- **Find My Phone:** Trigger a "Loud Alarm" to find a lost device, even if it is set to silent.
- **Real-Time Location:** Request instant GPS coordinates using the secure `#LOCATION` command.
- **System Status Reports:** Receive detailed reports on battery levels, signal strength, and silent mode status.

---

## 🛡️ Privacy & Permissions
Senior safety is our priority. The app requires access to essential system functions, but **none of this data ever leaves the device to a server; everything happens via peer-to-peer SMS.**
- **SMS:** To receive remote commands and send emergency alerts.
- **Location:** To find the senior in case of emergency or wandering.
- **Phone:** To call SOS contacts directly during an emergency.
- **Camera:** To take photos of medication for visual reminders.
- **Physical Activity:** For the built-in step counter.

---

## 🛠️ Technical Details
- **UI Framework:** Fully built with **Jetpack Compose** for a modern and fluid experience.
- **Local Storage:** Uses **Room Database** for offline storage of contacts, medication, and logs.
- **Background Reliability:** Utilizes **Foreground Services** and **WorkManager** to ensure emergency functions remain active 24/7.
- **Architecture:** Follows **MVVM (Model-View-ViewModel)** patterns for clean and maintainable code.

---

## 📸 Screenshots & Visual Guide
*The UI has been completely revamped. Below are the key screens that require updated visuals:*

<p align="center">
  <img src="screenshots/home_v8.png" width="250" title="Home Screen: The heart of the app with large buttons and step counter.">
  <img src="screenshots/weather_advice.png" width="250" title="Weather Safety: The traffic light system and clothing advice.">
  <img src="screenshots/medication_photo.png" width="250" title="Medication Alarm: Visual reminder with actual pill photo.">
</p>
<p align="center">
  <img src="screenshots/steps_counter.png" width="250" title="Step Counter: Clear overview of daily physical activity.">
  <img src="screenshots/settings_locked.png" width="250" title="Protected Settings: The PIN-entry screen for management.">
  <img src="screenshots/remote_message.png" width="250" title="Remote Message: How an urgent caregiver message appears.">
</p>

---

## 🇳🇱 Nederlandse Versie
Zie de **[README_NL.md](README_NL.md)** voor de Nederlandstalige versie.

## ❤️ Support & Future
Senior Launcher is and will always remain free and open-source. Digital inclusion for seniors is a right, not a luxury.

[![Donate via PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://www.paypal.com/donate/?business=amine.chtaiti@gmail.com&no_recurring=0&currency_code=EUR)

---
*Created with love to connect generations and provide peace of mind.*
