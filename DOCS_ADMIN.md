# 📖 Guide for Caregivers & Family (v0.8.5)

**🌐 Language:** **English** · [Русский](DOCS_ADMIN_RU.md) · [Nederlands](DOCS_BEHEERDER_NL.md)

Welcome to the Senioren Launcher. This guide is meant for family members, caregivers and administrators. The launcher is fully prepared for **Android 16 (Baklava)** and complies with strict European privacy legislation (**GDPR**).

---

## 🛠️ 0. First installation (setup wizard)
1.  **Choose the language:** the very first screen asks for the language (Русский, Nederlands, ...). Russian is the default. You can change it later in *Settings*.
2.  **Accept the privacy statement.**
3.  **Choose a role:** *I am the caregiver* (technical setup) or *I am the user* (the senior's personal choices).
4.  **Caregiver steps:** permissions, system permissions, default apps (Home, Phone, SMS), main contacts (up to 4, they become favourites and SOS contacts), fall detection, PIN code and remote support.
5.  **Hand the phone to the senior,** who chooses the text size and the colours.

> **Tip:** every setup screen scrolls, and the *Next* button stays at the bottom of the screen, also at the largest text size.

---

## 🔐 1. Authorization: Setting yourself up as an Administrator
To manage the phone remotely, your number must be recognised as an authorised SOS contact.

### Step-by-step:
1.  **Open Contacts:** on the senior's screen, tap the **"Contacts"** button.
2.  **Add a contact:** add yourself (or edit your existing contact).
3.  **Star marking:** make sure you are marked as a **Favourite / SOS contact** (usually by tapping the star next to the name).
4.  **Number recognition:** enter your number. The app recognises all formats automatically:
    *   Local: `0487123456`
    *   International: `+32487123456` or `0032487123456`
5.  **Test:** send an SMS with the text `#PING` from your phone to the senior. If you see a confirmation on their screen, the link works.

---

## 🏠 2. Arranging the home screen
- **Layout and text size:** *Settings* (PIN protected) → choose 1x1, 2x3 or 3x4 and the text size. The number of tile rows adapts to the screen, so nothing is cut off. Extra tiles go to pages you can swipe through (page dots under the grid).
- **Changing the order of the tiles:** long-press a tile, then use **"◀ Earlier"** or **"Later ▶"** as often as you like and tap **DONE**. The dialog shows the position, for example "Calls: place 2 of 8".
- **Replacing or removing an app:** in the same dialog, tap **"Replace or remove"** (not available for Settings, Wi-Fi and Bluetooth).
- **Language:** *Settings* → Language. The app restarts and shows the new language.

---

## 📱 3. Privacy & Safety (Android 16 Ready)
The app uses **Privacy by Design**. This means:
*   **Privacy Labels:** on screens where sensitive data is used (such as Location or Contacts), the senior now sees a blue privacy icon with an explicit explanation of why the data is needed.
*   **Secure Updates:** app updates run over HTTPS and require an explicit check of installation rights, in line with the latest Android security requirements.
*   **Scam Protection:** the launcher automatically blocks known spam numbers and warns about suspicious SMS messages.

---

## 🎮 4. Remote Control via SMS (Full List)
Send an SMS to the senior with one of the following codes. The app reacts immediately. The codes are always Dutch, whatever language the app is set to (see [COMMANDS.md](COMMANDS.md) for the complete list with explanations).

| Category | Code | Action |
|:----------|:-----|:------|
| **Wellbeing** | `#WAAR` | Receive the GPS location via Google Maps. |
| | `#STATUS` | Check battery, signal strength and volume. |
| | `#PING` | Show "ALLES GOED?" (are you okay?) in large letters on the screen. |
| | `#BEL_TERUG` | The phone calls you back immediately. |
| **Help** | `#LAMP AAN` / `#LAMP UIT` | Switch the flashlight remotely. |
| | `#KNIPPER` | Make the flashlight blink 10 times (to find the phone). |
| | `#ALARM [text]` | Trigger a loud alarm with a message. |
| **Settings** | `#VOLUME [1-15]` | Adjust the ringtone volume (e.g. `#VOLUME 15`). |
| | `#HELDER [1-100]` | Adjust the screen brightness. |
| | `#WIFI AAN` / `#WIFI UIT` | Manage the internet connection. |
| | `#STIL AAN` / `#STIL UIT` | Switch silent mode (Do Not Disturb). |
| **Management** | `#PRIVACY` | Receive an overview of the privacy settings. |
| | `#UPDATE_CHECK` | Force a check for available app updates. |
| | `#RESTART` | Restart the launcher app. |
| | `#THEMA [1, 2 or 3]` | Change the theme (1=Classic, 2=High Contrast, 3=Light). |

---

## ❓ Frequently Asked Questions (FAQ)

**Q: Why does the phone not respond to my SMS?**
*   Check that your number is in the contact list with a star (SOS).
*   Make sure the senior has credit to send the reply SMS.

**Q: Can I set up multiple administrators?**
*Yes, every contact marked as SOS/Favourite can use the commands.*

**Q: Is the location accurate?**
*The app uses GPS. If the senior is indoors, the accuracy may vary. The SMS always contains the estimated deviation in metres.*

**Q: The text is too large and I cannot find a button.**
*Screens scroll, and the home screen shows fewer rows when the text is larger; swipe sideways for more tiles. If the text is still too large, lower the text size in Settings or send `#LETTER 3`.*

---

## 📬 Contact for Administrators
For technical support when setting up multiple devices or specific Android 16 questions: [amine.chtaiti@gmail.com](mailto:amine.chtaiti@gmail.com).
