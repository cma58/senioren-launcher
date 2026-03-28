# 📱 Senioren Launcher

**De gratis, open-source Android launcher voor ouderen en mensen met een visuele of motorische beperking.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![F-Droid](https://img.shields.io/badge/F--Droid-beschikbaar-green.svg)](https://f-droid.org)
[![Geen Tracking](https://img.shields.io/badge/Tracking-Geen-brightgreen.svg)](#-privacy)

---

## Waarom Senioren Launcher?

Alle senioren-launchers op de markt zijn **duur** of zitten vol met **advertenties en tracking**. Onze ouders verdienen beter.

- **100% gratis** — geen in-app aankopen, geen abonnementen
- **Open source** — iedereen kan meebouwen
- **Geen tracking** — nul analytics, nul advertenties
- **Beschikbaar op F-Droid** — geen Google Play nodig

---

## ✨ Functies

### Kern
- 🔘 Grote knoppen — 3 layouts (2×3, 3×4, 1×1)
- 📞 Bellen — Grote nummertoetsen + snelkeuze fotoknoppen
- 💬 Berichten — Extra groot lettertype
- 🆘 SOS noodknop — GPS-locatie via SMS naar noodcontacten
- 🔔 Grote meldingen
- 🌤️ Weer met kledingadvies

### Gezondheid & Veiligheid
- 💊 Medicijnherinnering met aftikken
- 🛡️ Valdetectie — Automatisch SOS bij val
- 🏥 Noodinfo — Bloedgroep, allergieën, ICE-contacten
- 📍 Locatie delen met familie
- 🪫 Batterijwaarschuwing — SMS bij < 15%
- 🚶 Stappenteller

### Dagelijks Gebruik
- 📅 Agenda met herinneringen
- 📝 Notities
- 🔦 Zaklamp
- 🔍 Vergrootglas (camera als loep)
- 📻 Radio met grote knoppen
- 🎥 Videobellen snelknop

### Aanpassing
- 🎨 3 Thema's (Klassiek, Hoog Contrast, Licht)
- 🔤 Lettergrootte slider
- 🔒 PIN-vergrendeling
- 🌍 Meertalig (NL, FR, DE, EN, TR, AR)
- 🌙 Automatische nachtmodus
- 📱 Afstandsbediening via web-app

---

## 📲 Installatie

### Zelf bouwen
```bash
git clone https://github.com/aminechtaiti-source/senioren-launcher.git
cd senioren-launcher
./gradlew assembleRelease
```

Of open in **Android Studio** → Run.

---

## 🏗️ Technologie

| Component | Technologie |
|-----------|-------------|
| Taal | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Architectuur | MVVM + Clean Architecture |
| Database | Room + DataStore |
| Achtergrond | WorkManager |
| Sensoren | Accelerometer (valdetectie) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 |

---

## 🤝 Bijdragen

Zie [CONTRIBUTING.md](CONTRIBUTING.md) voor details.

---

## 🔒 Privacy

Nul data-verzameling. Zie [PRIVACY.md](PRIVACY.md).

---

## 📜 Licentie

[GNU General Public License v3.0](LICENSE)

---

*"Technologie moet mensen verbinden, niet uitsluiten."* ❤️
