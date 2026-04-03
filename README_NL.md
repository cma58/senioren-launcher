# 📱 Senioren Launcher (Beta)

**De eerlijke, open-source Android launcher voor onze ouderen. Gemaakt om technologie weer toegankelijk en veilig te maken.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25-brightgreen.svg)](PRIVACY.md)
[![Status: Beta](https://img.shields.io/badge/Status-Beta-orange.svg)](#)

---

## 📸 Screenshots

<p align="center">
  <img src="screenshots/screenshot_home.jpg" width="250" title="Startscherm">
  <img src="screenshots/screenshot_phone.jpg" width="250" title="Telefoon">
  <img src="screenshots/screenshot_weather.jpg" width="250" title="Weer & Advies">
</p>
<p align="center">
  <img src="screenshots/screenshot_medication.jpg" width="250" title="Medicijnen">
  <img src="screenshots/screenshot_emergency.jpg" width="250" title="Noodinfo">
  <img src="screenshots/screenshot_remote_support.jpg" width="250" title="Hulp op Afstand">
</p>
<p align="center">
  <img src="screenshots/screenshot_sos.jpg" width="250" title="SOS Noodhulp">
</p>

---

[English version here](README.md)

---

## Waarom Senioren Launcher?

Veel bestaande launchers voor senioren zijn onnodig duur, zitten vol advertenties of verzamelen stiekem data. Wij geloven dat onze ouders en grootouders beter verdienen. 

Dit is een **passieproject waar ik alleen in mijn vrije tijd aan werk**, met als doel een launcher te bouwen die:

- **Echt Simpel is** — Grote knoppen, duidelijke teksten en geen overbodige menu's.
- **Privacy voorop stelt** — Uw gegevens blijven op uw telefoon. Nul analytics, nul advertenties.
- **100% Gratis & Open Source** — Voor iedereen beschikbaar zonder winstoogmerk.

> **Let op:** Dit project is momenteel in **Bèta-fase**. Omdat ik dit naast mijn dagelijkse werk doe, kan de ontwikkeling soms iets langzamer gaan, maar ik werk er met veel liefde aan. **Mijn doel is om deze app uiteindelijk ook op F-Droid te publiceren.**

---

## ☕ Steun mijn werk

Senioren Launcher is een project dat ik volledig gratis aanbied. Jouw steun helpt enorm om de kosten te dekken (zoals voor hosting of testapparatuur) en motiveert mij om in mijn vrije avonden en weekenden door te blijven bouwen aan nieuwe functies.

**Vind je dit een mooi initiatief?** Elke bijdrage, hoe klein ook, wordt ontzettend gewaardeerd!

[![Doneer via PayPal](https://img.shields.io/badge/Doneer-PayPal-blue.svg?style=for-the-badge)](https://www.paypal.com/donate/?business=amine.chtaiti@gmail.com&no_recurring=0&currency_code=EUR)

---

## ✨ Functies in detail

### 📞 Bellen & Contacten
Grote nummertoetsen en een lijst van favorieten met foto's. Eén druk op de knop om direct te bellen naar familie of vrienden.

### 🆘 SOS Noodknop
Bij het 3 seconden indrukken van de SOS-knop worden noodcontacten direct ingelicht via SMS met de exacte GPS-locatie.

### 👨‍🔧 Hulp op Afstand (RustDesk)
Unieke functie waarmee een familielid op afstand kan meekijken om te helpen bij instellingen. Inclusief een duidelijk stappenplan voor de senior.

### 🔍 Vergrootglas & Zaklamp
Verander de telefoon in een digitale loep om kleine teksten op medicijnen of menukaarten makkelijk te kunnen lezen.

### 🚶 Stappenteller
Stimuleert beweging door het aantal stappen van vandaag groot en duidelijk te tonen.

### 📅 Agenda & Notities
Een versimpelde agenda en een plek voor korte notities, zodat u nooit meer een afspraak of boodschap vergeet.

---

## 🛠️ Status van Ontwikkeling

Omdat we transparant willen zijn over wat wel en niet werkt in deze bèta:

- **🛡️ Valdetectie**: Momenteel **experimenteel**. De gevoeligheid wordt nog getest om valse alarmen te voorkomen.
- **🪫 Batterijwaarschuwing**: Deze functie is momenteel **in ontwikkeling** en werkt nog niet. 
- **🌍 Talen**: De basis is Nederlands, andere talen volgen zodra ik daar tijd voor vind.

---

## 🎨 Aanpassing & Beveiliging

De launcher kan worden aangepast aan de behoeften van de gebruiker:
- **Layouts**: Kies uit 2x3, 3x4 of 1x1 grid.
- **Lettergrootte**: Alles kan extra groot worden weergegeven.
- **🔒 Instellingen vergrendelen**: Beveilig de instellingen met een PIN-code.
  - **Standaard PIN-code**: `1234`

---

## 🏗️ Technologie

Ik gebruik moderne en veilige technologieën om de beste ervaring te bieden:

| Component | Technologie |
|-----------|-------------|
| **Taal** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material 3 |
| **Architectuur** | MVVM + Clean Architecture |
| **Database** | Room (SQLite) + DataStore |
| **Achtergrond** | WorkManager |
| **Camera** | CameraX (voor het Vergrootglas) |
| **Sensoren** | Accelerometer (Valdetectie) & Step Counter |
| **Media** | Media3 ExoPlayer (voor de Radio) |
| **Min SDK** | API 26 (Android 8.0) |
| **Target SDK** | API 36 (Android 15) |

---

## 🤝 Bijdragen

Wil je helpen het project te verbeteren? Graag! Zie [CONTRIBUTING.md](CONTRIBUTING.md) voor de richtlijnen over het melden van bugs of het insturen van code.

## 🔒 Privacy

Privacy is geen bijzaak, het is de kern van dit project. Wij verzamelen **geen data**. Alles blijft op de telefoon van de senior. Lees ons volledige [PRIVACY.md](PRIVACY.md).

## 📜 Licentie

Dit project is gelicentieerd onder de **GNU General Public License v3.0**. Dit betekent dat de software altijd open en gratis zal blijven.

---

*"Technologie moet mensen verbinden, niet uitsluiten."* ❤️
