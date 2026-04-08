# 📱 Senioren Launcher (0.8.2 Beta)

**De eerlijke, open-source Android launcher voor onze ouderen. Gemaakt om technologie weer toegankelijk, veilig en menselijk te maken.**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](LICENSE)
[![Privacy](https://img.shields.io/badge/Privacy-100%25-brightgreen.svg)](PRIVACY.md)
[![Status: Beta](https://img.shields.io/badge/Status-0.8.2%20Beta-orange.svg)](#)

> **⚠️ Bèta Fase:** Dit project bevindt zich in de bèta-fase. Dit betekent dat u nog bugs kunt tegenkomen. Ik bouw dit project in mijn vrije tijd om onze (groot)ouders hun digitale vrijheid terug te geven.

---

## 📢 Oproep aan Testers & Feedback
Ik ben continu bezig de launcher te verbeteren, maar omdat ik niet elk type Android-toestel in huis heb, heb ik jouw hulp nodig!

**Help mee het project te verbeteren:**
Ik zoek testers die de functies (met name de SMS-commando's) grondig willen uitproberen op verschillende toestellen (Samsung, Nokia, Motorola, etc.). Heb je advies, een suggestie voor een nieuwe functie of heb je een bug gevonden?
- **Contact & Bug Reports:** [amine.chtaiti@gmail.com](mailto:amine.chtaiti@gmail.com)

---

## 🌟 Onze Visie: "Senioren-Eerst"
De meeste smartphones zijn ontworpen voor jonge mensen. Wij draaien het om. De Senioren Launcher is gebouwd volgens strikte UX-regels voor ouderen:
- **Geen Toetsenborden:** Alles werkt met grote plus/min knoppen en duidelijke lijsten.
- **Gigantische Elementen:** Teksten zijn minimaal 20-30sp. Knoppen zijn minstens 70dp hoog.
- **Contrast & Duidelijkheid:** Geen vage icoontjes, maar harde teksten zoals "OPHANGEN" of "OPSLAAN".
- **Digitale Rust:** Geen onnodige notificaties of ingewikkelde veeg-bewegingen.

---

## ✨ Belangrijkste Functies

### 📞 Bellen & Contacten
- **Simpele Dialer:** Grote nummertoetsen die trillen bij aanraking.
- **Favorieten met Foto:** Bel familie met één klik op hun gezicht.
- **Mijn Nummer:** Het eigen nummer staat altijd bovenaan voor het geval ze het vergeten.
- **NOOD 112:** Een directe knop met een extra bevestiging om vals alarm te voorkomen.

### 💬 Berichten (SMS)
- **Leesbaarheid:** Berichten worden gegroepeerd per dag met een zeer groot lettertype.
- **Direct Zoomen:** Verander de tekstgrootte direct in het gesprek met grote knoppen.
- **Veiligheid:** Onbekende of spam-berichten worden duidelijk gemarkeerd.

### 🆘 SOS & Veiligheid
- **SOS Knop:** Houd 3 seconden ingedrukt om direct alle noodcontacten te bellen en een SMS te sturen met de exacte GPS-locatie.
- **Valdetectie:** Gebruikt de sensoren van de telefoon om een val te detecteren en automatisch hulp in te schakelen (Experimenteel).
- **Noodinfo:** Medische gegevens (bloedgroep, allergieën) direct toegankelijk voor hulpverleners vanaf het hoofdscherm.

### 💊 Medicijnen & Wellness
- **Herinneringen:** Meldingen met foto's van de medicijnen.
- **Voorraad:** De app houdt bij hoeveel pillen er nog zijn en waarschuwt de familie bij een bijna lege strip.
- **Stappen & Radio:** Ingebouwde stappenteller en een simpele radio voor ontspanning.

---

## 🎮 Beheer op Afstand (Remote Support)

> **⚠️ Experimentele Functie:** Deze functies zijn succesvol getest op onze testtoestellen. Vanwege de grote variatie in Android-modellen (Samsung, Nokia, etc.) beschouwen we dit als experimenteel. Test de commando's altijd eerst zelf.

Beheerders kunnen de telefoon via een simpele SMS aansturen vanaf hun eigen toestel:

| Commando | Actie | Voorbeeld |
|----------|-------|-----------|
| **`#WAAR`** | Ontvang de actuele GPS-locatie via Google Maps link. | `#WAAR` |
| **`#STATUS`** | Check Batterij %, Volume en of de telefoon op Stil staat. | `#STATUS` |
| **`#PING`** | Toon een "ALLES GOED?" check op het scherm. | `#PING` |
| **`#BEL_TERUG`**| De telefoon belt de beheerder automatisch terug. | `#BEL_TERUG` |
| **`#LAMP`** | Zet de zaklamp aan of uit. | `#LAMP AAN` |
| **`#KNIPPER`** | Laat de zaklamp 10x knipperen (visueel signaal). | `#KNIPPER` |
| **`#OPEN`** | Open op afstand een app (bijv. WhatsApp). | `#OPEN WhatsApp` |
| **`#BERICHT`** | Toon een grote popup die hardop wordt voorgelezen. | `#BERICHT De kapper komt om 14u` |
| **`#VOLUME`** | Stel geluid in (schaal 1-10). | `#VOLUME 10` |
| **`#HELDER`** | Stel helderheid in (schaal 1-10). | `#HELDER 10` |
| **`#WEKKER`** | Stel op afstand een wekker in. | `#WEKKER 08:30 Ontbijt` |
| **`#HELP`** | Ontvang alle codes op je eigen telefoon. | `#HELP` |

---

## 🏗️ Technologie & Veiligheid
- **Kotlin 2.1 & Jetpack Compose:** Moderne, snelle en stabiele code.
- **Android 16 Ready:** Gebruikt de nieuwste API's voor maximale betrouwbaarheid op nieuwe toestellen.
- **Privacy:** 100% lokaal. Geen dataverzameling, geen cloud, geen advertenties.
- **Batterij-efficiënt:** Geoptimaliseerd om de accu van oudere toestellen te sparen.

---

## 🤝 Bijdragen & Support
Dit is een open-source project. Heb je een bug gevonden op jouw toestel? Laat het weten via de Issues op GitHub of stuur een e-mail naar [amine.chtaiti@gmail.com](mailto:amine.chtaiti@gmail.com).

*"Technologie moet mensen verbinden, niet uitsluiten."* ❤️
