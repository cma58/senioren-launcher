# 📱 Senioren Launcher 🛡️ (v0.8.8)

[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/cma58/senioren-launcher)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25-brightgreen.svg)](PRIVACY.md)
[![Android 14+](https://img.shields.io/badge/Android-14%2B-green.svg)](https://developer.android.com)

**De eerlijke, open-source Android launcher voor onze ouderen. Gemaakt om technologie weer toegankelijk, veilig en menselijk te maken.**

100% Gratis & Open Source – Geen trackers, geen cloud, gewoon digitale vrijheid. Geoptimaliseerd voor stadsbrede distributie (Project Gent).

---

## 🔗 Het Ecosysteem: Hoe het werkt
De **Senioren Launcher** is onderdeel van een ecosysteem van twee apps die samenwerken voor maximale veiligheid en rust.

1.  **Senioren Launcher** (deze app): Wordt geïnstalleerd op de **telefoon van de senior**. Het biedt een veilige interface en voert commando's op afstand uit via beveiligde SMS.
2.  **[Senioren Beheerder](https://github.com/cma58/SeniorenBeheerder)**: Wordt geïnstalleerd op de **telefoon van de mantelzorger**. Dit dient als een afstandsbediening om status, locatie en instellingen te monitoren.

---

## 🌟 Senioren-Eerst Design
De meeste smartphones zijn ontworpen voor "digital natives". Wij draaien dat om. De Senioren Launcher is gebouwd volgens strikte UX-regels voor ouderen:
- **Geen Toetsenbord Nodig:** Primaire navigatie werkt met grote knoppen en simpele lijsten.
- **Gigantische Elementen:** Teksten met hoog contrast (16-36sp) en extra grote knoppen (70dp+).
- **Digitale Rust:** Geen onnodige notificaties of ingewikkelde veeg-bewegingen.

## ✨ Belangrijkste Functies
| Functie | Beschrijving |
| :--- | :--- |
| **🏠 Dynamisch Startscherm** | Pagina-gebaseerd app-raster met aanpasbare lay-outs (1x1, 2x3, 3x4). |
| **📞 Simpel Bellen** | Grote dialer met favorieten (foto's) en een beveiligde nood-bevestiging. |
| **💬 Berichten Center** | Extra grote SMS-weergave met directe optie om tekst te vergroten en melding-badges. |
| **🆘 Slimme SOS** | Speciale SOS-knop met aftelmechanisme voor noodoproepen en GPS-locatie via SMS. |
| **💊 Medicijnbeheer** | Herinneringen met pillenfoto's en automatisch voorraadbeheer. |
| **🎮 Remote Support** | Laat mantelzorgers de telefoon beheren via de [Senioren Beheerder](https://github.com/cma58/SeniorenBeheerder) app. |

## 🛠️ Recente Technische Updates (v0.8.8)
- **Compose UI Stabiliteit:** Systematische oplossing van `Surface` API-fouten en ontbrekende foundation-imports in alle belangrijke UI-schermen (`Weer`, `Instellingen`, `Agenda`, `Alle Apps`). `Surface`-randen zijn gemigreerd naar `BorderStroke` en `combinedClickable` is geïmplementeerd voor betere interactie-afhandeling.
- **SMS Commando Optimalisatie:** Verbeterde `#STATUS` output met visuele indicatoren (`🔕`, `🔊`) voor beheer-apps.
- **Tweetalige Ondersteuning:** Afstandscommando's accepteren nu zowel Nederlands (`AAN`/`UIT`) als Engels (`ON`/`OFF`).
- **Internationale Normalisatie:** Verbeterde nummerherkenning voor **BE, NL, FR, en DE** landcodes.
- **Android 14+ FGS Compliance:** Vorderingen in Foreground Service definities voor Play Store veiligheidsnormen.
- **GDPR Transparantie:** Integratie van een uitgebreid `PrivacyScreen` met expliciete vermelding van de Gentse stadsdistributie.

## 🛡️ Veiligheid & Privacy (AVG/GDPR)
Gebouwd volgens **Privacy by Design** principes:
- **100% Lokaal:** Geen cloud-opslag. Uw contacten en berichten verlaten het toestel nooit.
- **Transparant:** Open-source code die volledige beveiligingsaudits toestaat.
- **Geen Tracking:** Geen analytics, geen advertenties, geen data-mining.

## 📸 Screenshots
<p align="center">
  <img src="screenshots/screenshot_home.jpeg" width="250" title="Startscherm">
  <img src="screenshots/screenshot_phone.jpeg" width="250" title="Telefoon">
  <img src="screenshots/screenshot_weather.jpeg" width="250" title="Weer">
</p>

---

## 🇬🇧 English Version
Zie de **[README.md](README.md)** voor de Engelstalige versie.

## ❤️ Waardering & Toekomst
Senioren Launcher is en blijft gratis en open-source. Digitale inclusie voor ouderen is een recht, geen luxe.

[![Steun via PayPal](https://img.shields.io/badge/Steun-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://www.paypal.com/donate/?business=amine.chtaiti@gmail.com&no_recurring=0&currency_code=EUR)

---
*Gemaakt met zorg om generaties te verbinden en mantelzorgers rust te geven.*
