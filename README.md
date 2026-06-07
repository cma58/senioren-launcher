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

## 🌟 Senior-First Design
Most smartphones are designed for digital natives. We turn that around. The Senioren Launcher is built following strict UX rules for the elderly:
- **No Keyboards Required:** Primary navigation uses large buttons and simple lists.
- **Gigantic UI Elements:** High-contrast text (16-36sp) and oversized touch targets (70dp+).
- **Digital Peace:** Zero unnecessary notifications or complicated swipe gestures.

## ✨ Key Features
| Feature | Description |
| :--- | :--- |
| **🏠 Dynamic Home** | Paginated app grid with customizable layouts (1x1, 2x3, 3x4). |
| **📞 Simplified Calling** | Large dialer with photo favorites and a secure emergency confirmation. |
| **💬 Message Center** | Extra-large SMS view with instant font size adjustment and unread badges. |
| **🆘 Smart SOS** | Dedicated SOS button with countdown to trigger emergency calls and GPS sharing. |
| **💊 Medication Tracker** | Reminders with pill photos and automated stock tracking. |
| **🎮 Remote Support** | Caregivers manage the phone via the [Senioren Beheerder](https://github.com/cma58/SeniorenBeheerder) dashboard. |

## 🛠️ Recent Technical Updates (v0.8.8)
- **Compose UI Stability:** Systematic resolution of `Surface` API mismatches and missing foundation imports across all major UI screens (`Weather`, `Settings`, `Calendar`, `AllApps`). Migrated `Surface` borders to `BorderStroke` and implemented `combinedClickable` for enhanced interaction handling.
- **SMS Command Optimization:** Enhanced `#STATUS` output with visual markers (`🔕`, `🔊`) for caregiver apps.
- **Bilingual Support:** Remote commands now accept both Dutch (`AAN`/`UIT`) and English (`ON`/`OFF`).
- **International Normalization:** Improved phone number parsing for **BE, NL, FR, and DE** prefixes.
- **Android 14+ FGS Compliance:** Refined Foreground Service justifications for Play Store security standards.
- **GDPR Disclosure:** Integrated a comprehensive `PrivacyScreen` with explicit mentions of the Ghent city project involvement.

## 🛡️ Security & Privacy (GDPR/AVG)
Built with **Privacy by Design** principles:
- **100% Local:** No cloud storage. Your contacts and messages never leave the device.
- **Transparent:** Open-source code allows for full security audits.
- **No Tracking:** Zero analytics, zero ads, zero data mining.

## 📸 Screenshots
<p align="center">
  <img src="screenshots/screenshot_home.jpeg" width="250" title="Home Screen">
  <img src="screenshots/screenshot_phone.jpeg" width="250" title="Phone">
  <img src="screenshots/screenshot_weather.jpeg" width="250" title="Weather">
</p>

---

## 🇳🇱 Nederlandse Versie
Zie de **[README_NL.md](README_NL.md)** voor de Nederlandstalige versie.

## ❤️ Support & Future
Senior Launcher is and will always remain free and open-source. Digital inclusion for seniors is a right, not a luxury.

[![Donate via PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://www.paypal.com/donate/?business=amine.chtaiti@gmail.com&no_recurring=0&currency_code=EUR)

---
*Created with love to connect generations and provide peace of mind.*
