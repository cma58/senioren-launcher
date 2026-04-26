# 📟 Full Overview of SMS Commands (v0.8.3)

This page contains all available commands to manage the Senior Launcher remotely. Send these codes via a standard SMS from your own phone to the senior's phone.

> **Note:** Commands only work if your number is registered as an **SOS contact** (Golden Star in contacts) on the senior's phone.

---

## 🛡️ 1. Safety & Location
Crucial for the senior's safety and immediate assistance.

| Command | Action | Situation |
|:---|:---|:---|
| **`#WAAR`** | Sends back a Google Maps link with the current GPS location. | Use this if the senior is lost or the phone is missing. |
| **`#SOS_NU`** | Directly starts the full emergency procedure (SMS + calling all SOS contacts). | Acute emergency where the senior cannot press the button. |
| **`#PING`** | Shows a large "ARE YOU OKAY?" button on the screen. | For a quick wellness check without disturbing them with a call. |
| **`#BEL_TERUG`** | Makes the senior's phone call your number immediately. | If the senior doesn't know how to call or is in panic. |
| **`#SPEAKER`** | Forces the loudspeaker ON for the next incoming call. | If the senior has trouble hearing the phone or holding it. |
| **`#VEILIG ON`** | Blocks all incoming calls from numbers not in the contacts (Anti-Scam). | To prevent harassment by telemarketers or scammers. |
| **`#BLOKKEER [nr]`**| Blocks a specific phone number. *Example: `#BLOKKEER 0612345678`* | Manually blocking a persistent harasser. |

---

## ⚙️ 2. System & Connectivity
Restore settings the senior may have changed by accident.

| Command | Action | Situation |
|:---|:---|:---|
| **`#WIFI ON/OFF`** | Enables or disables the WiFi receiver. | If WiFi was accidentally turned off. |
| **`#BT ON/OFF`** | Enables or disables Bluetooth. | Useful for hearing aid connectivity. |
| **`#STIL ON/OFF`** | Enables or disables "Do Not Disturb" (Silent Mode). | If the senior doesn't hear calls. |
| **`#VOLUME [0-10]`**| Sets ringtone volume (scale 0-10). *Example: `#VOLUME 10`* | If the senior repeatedly misses calls. |
| **`#VOLUME_MEDIA [0-10]`**| Sets music/radio volume. *Example: `#VOLUME_MEDIA 5`* | Remote volume adjustment for entertainment. |
| **`#HELDER [1-10]`** | Sets screen brightness (scale 1-10). *Example: `#HELDER 10`* | If the screen is too dark to read. |
| **`#SCHERM_TIJD [min]`**| Sets screen timeout (1, 2, 5, or MAX). *Example: `#SCHERM_TIJD 5`* | Keep screen on longer for easier reading. |
| **`#RESTART`** | Restarts the Senior Launcher app. | To resolve minor UI glitches remotely. |
| **`#UPDATE_CHECK`** | Forces the app to check for security updates. | Ensure latest Android 16 compatibility. |

---

## 📱 3. UI & App Management
Adjust the interface to the senior's needs.

| Command | Action | Situation |
|:---|:---|:---|
| **`#LETTER [1-5]`** | Sets text size (1 is normal, 5 is huge). *Example: `#LETTER 4`* | Better readability for failing eyesight. |
| **`#THEMA [1-3]`** | 1: Classic, 2: High Contrast, 3: Light. *Example: `#THEMA 2`* | Improve visibility for visual impairments. |
| **`#SLOT ON/OFF`** | Locks or unlocks the settings button. | Prevents accidental configuration changes. |
| **`#PIN [code]`** | Changes the 4-digit security PIN. *Example: `#PIN 1234`* | Update access for caregivers. |
| **`#OPEN [name]`** | Opens a specific app on the foreground. *Example: `#OPEN Photos`* | Help the senior find a specific function. |
| **`#NOTIFICATIES_WEG`**| Clears all active notifications from the status bar. | Remove confusing icons and popups. |

---

## 🗓️ 4. Planning & Medication
Manage the calendar and health remotely.

| Command | Action | Situation |
|:---|:---|:---|
| **`#MEDICIJN [time] [name]`**| Adds a daily medication reminder. *Example: `#MEDICIJN 08:30 Aspirin`* | Remote health management. |
| **`#VOORRAAD [name] [count]`**| Updates the number of remaining pills. *Example: `#VOORRAAD Aspirin 20`*| Tracking stock after refill. |
| **`#AGENDA [date] [time] [text]`**| Adds a calendar event. *Example: `#AGENDA 24-12 14:00 Dentist`* | Remote appointment scheduling. |
| **`#WEKKER [time] [label]`**| Sets a one-time or daily alarm. *Example: `#WEKKER 07:30 Wake up`*| Wake-up or task reminders. |
| **`#RADIO_STOP`** | Immediately stops any playing radio stream. | If the senior cannot find the stop button. |

---

## 🔍 5. Diagnostics & Status
Get insight into the device's state.

| Command | Response Content |
|:---|:---|
| **`#STATUS`** | Battery %, current volume, and silent mode status. |
| **`#INFO_PLUS`** | Free storage space and Android version (e.g., Android 16). |
| **`#PRIVACY`** | Status of critical permissions (GPS, SMS, Calling). |
| **`#LAATSTE_OPROEP`**| Details about the last incoming or missed call. |
| **`#AGENDA_VANDAAG`**| List of all appointments for today. |
| **`#WEKKERS_LIJST`** | Overview of all active alarms and medication times. |
| **`#APP_LIJST`** | List of the most used apps on the device. |
| **`#NETWERK`** | Provider name and connection type (4G/5G). |

---

## ✉️ 6. Messaging & Contacts
Manage the contact list remotely.

| Command | Action | Situation |
|:---|:---|:---|
| **`#BERICHT [text]`** | Shows a large popup message on the senior's screen. | *Example: `#BERICHT I will be there in 10 minutes!`* |
| **`#CONTACT [name] [nr]`**| Adds a new contact to the quick-dial list. | *Example: `#CONTACT John 0611223344`* |
| **`#VERWIJDER_CONTACT [name]`**| Removes a contact from the list. | *Example: `#VERWIJDER_CONTACT John`* |

---

## 🔦 7. Search & Tools

| Command | Action | Situation |
|:---|:---|:---|
| **`LAUN_ZOEK`** | The phone shouts "I am here!" and vibrates at maximum. | To find a lost phone nearby. |
| **`#LAMP ON/OFF`** | Turns the flashlight on or off. | Help the senior in the dark. |
| **`#LAMP_AUTO [min]`**| Turns flashlight on for X minutes. | *Example: `#LAMP_AUTO 5`* |
| **`#KNIPPER`** | Makes the flashlight blink 10 times. | A visual signal to find the phone. |

---

## 📬 Need help?
Send **`#HELP`** to the senior's phone to receive the most important codes directly on your own phone.
