# 📱 Senioren Launcher 🛡️ (v0.8.8)

[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/cma58/senioren-launcher)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25-brightgreen.svg)](PRIVACY.md)
[![Android 14+](https://img.shields.io/badge/Android-14%2B-green.svg)](https://developer.android.com)

**De eerlijke, open-source Android launcher voor onze ouderen. Gemaakt om technologie weer toegankelijk, veilig en menselijk te maken.**

100% Gratis & Open Source – Geen trackers, geen cloud, gewoon digitale vrijheid. Geoptimaliseerd voor stadsbrede distributie (Project Gent).

---

## 🔗 Het Ecosysteem: Hoe het werkt
De **Senioren Launcher** is onderdeel van een ecosysteem van twee apps die naadloos samenwerken voor maximale veiligheid en rust.

1.  **Senioren Launcher** (deze app): Wordt geïnstalleerd op de **telefoon van de senior**. Het biedt een veilige interface en voert commando's op afstand uit via beveiligde SMS.
2.  **[Senioren Beheerder](https://github.com/cma58/SeniorenBeheerder)**: Wordt geïnstalleerd op de **telefoon van de mantelzorger**. Dit dient als een afstandsbediening om status, locatie en instellingen te monitoren via versleutelde SMS-commando's.

---

## 🌟 Waarom de Senioren Launcher?
De meeste smartphones zijn ontworpen voor "digital natives". Wij draaien dat om. De Senioren Launcher is gebouwd volgens strikte UX-regels voor ouderen:
- **Zero-Learning Curve:** Geen ingewikkelde veeg-bewegingen of verborgen menu's. Alles is direct zichtbaar.
- **Visuele Duidelijkheid:** Extra grote touch-targets (90dp+), thema's met hoog contrast en aanpasbare lettergroottes tot 36sp.
- **Audio-Visuele Feedback:** Geïntegreerde Text-to-Speech (TTS) die wekkers en weerberichten voorleest, plus haptische feedback bij elke actie.
- **Digitale Rust:** Geen onnodige meldingen, advertenties of verwarrende pop-ups. Alleen de essentie telt.

---

## ✨ Uitgebreide Functies & Innovaties

### 🏠 Intelligent Startscherm
- **Flexibele Lay-outs:** Kies het perfecte raster: 1x1 (Enorm), 2x3 (Groot) of 3x4 (Compact).
- **Slimme Status Badges:** Zie in één oogopslag hoeveel onbeantwoorde oproepen of berichten er zijn.
- **Nachtlamp Modus:** Houd het wekkerscherm lang ingedrukt om het scherm te veranderen in een zacht oranje nachtlampje.
- **PIN-Beveiliging:** Instellingen zijn vergrendeld om te voorkomen dat de configuratie per ongeluk wordt gewijzigd.

### 🆘 Noodhulp & Actieve Veiligheid
- **Eén-Klik SOS:** Een grote knop start een aftelmechanisme, waarna direct hulpdiensten worden gebeld en GPS-coördinaten worden verzonden.
- **Valdetectie:** De app detecteert zware vallen via de sensoren van de telefoon en waarschuwt automatisch de mantelzorger.
- **Intercom Modus:** Bij een SOS-oproep wordt de luidspreker automatisch geforceerd, zodat communicatie mogelijk is zonder de telefoon vast te houden.
- **Batterij Bewaking:** De mantelzorger krijgt automatisch een SMS wanneer de batterij van de senior onder de 15% zakt.

### 💊 Gezondheid & Activiteit
- **Medicijn herinneringen met Foto:** Wekkers tonen een echte foto van het medicijn (bijv. "het blauwe pilletje") om fouten te voorkomen.
- **Mantelzorger Alerts:** Als een medicijn niet binnen 5 minuten wordt bevestigd, krijgt de mantelzorger direct een waarschuwing via SMS.
- **Ingebouwde Stappenteller:** Volg dagelijkse beweging direct op het startscherm zonder dat er een extra horloge of wearable nodig is.
- **Voorraadbeheer:** De app houdt bij hoeveel pillen er nog zijn en geeft een seintje wanneer er nieuwe besteld moeten worden.

### 🌤️ Hyper-Lokaal Weer & Leefadvies
- **Veiligheid-Stoplicht:** Kleurcodes (Groen/Oranje/Rood) geven direct aan of het weer veilig is om naar buiten te gaan.
- **Persoonlijk Advies:** Specifieke tips over kleding (bijv. "neem een sjaal mee"), tuinonderhoud, ramen sluiten en UV-bescherming.
- **Dagdeel Voorspelling:** Een eenvoudige blik op wat de ochtend, middag, avond en nacht gaan brengen.

---

## 🎮 Beheer op Afstand (SMS Commando's)
Mantelzorgers kunnen de telefoon overal vandaan beheren, zelfs zonder internetverbinding:
- **Bericht op Afstand:** Stuur een dringend tekstbericht dat het volledige scherm overneemt en zichzelf hardop voorleest aan de senior.
- **Telefoon Zoeken:** Activeer een luid alarm om een kwijtgeraakte telefoon terug te vinden, zelfs als deze op stil staat.
- **Real-time Locatie:** Vraag direct de GPS-locatie op met het `#LOCATIE` commando.
- **Systeem Status:** Krijg een gedetailleerd rapport over de batterij, het bereik en of de telefoon op stil staat.

---

## 🛡️ Privacy & Rechten (Permissions)
Omdat de veiligheid van de senior centraal staat, heeft de app toegang nodig tot essentiële systeemfuncties. **Niets van deze data verlaat het toestel naar een server; alles verloopt via SMS.**
- **SMS:** Voor het ontvangen van beheercommando's en het versturen van noodmeldingen.
- **Locatie:** Om de senior terug te vinden bij een noodgeval of dwaalgedrag.
- **Telefoon:** Om direct de SOS-contacten te bellen bij nood.
- **Camera:** Voor het maken van foto's van medicijnen.
- **Fysieke Activiteit:** Voor de ingebouwde stappenteller.

---

## 🛠️ Technische Details
- **UI Framework:** Volledig gebouwd met **Jetpack Compose** voor een moderne en vloeiende ervaring.
- **Lokale Opslag:** Gebruikt **Room Database** voor offline opslag van contacten, medicatie en logs.
- **Achtergrondtaken:** Maakt gebruik van **Foreground Services** en **WorkManager** om ervoor te zorgen dat noodfuncties altijd actief blijven.
- **Architectuur:** Volgt de **MVVM (Model-View-ViewModel)** patronen voor schone en onderhoudbare code.

---

## 📸 Screenshots & Visuele Gids
*Omdat de app volledig is vernieuwd, zijn de onderstaande screenshots essentieel voor een goed beeld:*

<p align="center">
  <img src="screenshots/home_v8.png" width="250" title="Startscherm: Het hart van de app met grote knoppen en stappenteller.">
  <img src="screenshots/weather_advice.png" width="250" title="Veilig Weerbericht: Het stoplichtsysteem en kledingadvies.">
  <img src="screenshots/medication_photo.png" width="250" title="Medicijn Alarm: De visuele herinnering met foto.">
</p>
<p align="center">
  <img src="screenshots/steps_counter.png" width="250" title="Stappenteller: Duidelijk overzicht van de dagelijkse beweging.">
  <img src="screenshots/settings_locked.png" width="250" title="Beveiligde Instellingen: Het menu dat vraagt om een pincode.">
  <img src="screenshots/remote_message.png" width="250" title="Bericht op Afstand: Hoe een bericht van de mantelzorger eruit ziet.">
</p>

---

## 🇬🇧 English Version
Zie de **[README.md](README.md)** voor de Engelstalige versie.

## ❤️ Waardering & Toekomst
Senioren Launcher is en blijft gratis en open-source. Digitale inclusie voor ouderen is een recht, geen luxe.

[![Steun via PayPal](https://img.shields.io/badge/Steun-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://www.paypal.com/donate/?business=amine.chtaiti@gmail.com&no_recurring=0&currency_code=EUR)

---
*Gemaakt met zorg om generaties te verbinden en mantelzorgers rust te geven.*
