# 📟 Volledig Overzicht SMS-Commando's (v0.8.3)

Deze pagina bevat **alle** beschikbare commando's waarmee u de Senioren Launcher op afstand kunt beheren. 

---

## 🚀 Snelstart Stappenplan (Beheerder)

Om de telefoon van de senior op afstand te kunnen beheren, moeten de volgende stappen worden doorlopen:

1. **Stel uzelf in als SOS-contact:**
   - Open de app op de telefoon van de senior.
   - Tik op **"Contacten"**.
   - Tik op **"Nieuw"** (of selecteer een bestaand contact).
   - Voer uw naam en telefoonnummer in.
   - Tik op het **Sterretje (Favoriet)** naast de naam. De achtergrond van het contact wordt goudgeel. 
   - **Gefeliciteerd!** U bent nu een SOS-contact en mag commando's sturen.

2. **Geef de benodigde rechten:**
   - Ga op de telefoon van de senior naar **"Instellingen"** (tandwiel).
   - Tik op **"Systeem Rechten"**.
   - Zorg dat alle vinkjes op groen staan (✅), vooral **SMS** en **Locatie**.

3. **Stuur uw eerste commando:**
   - Pak uw eigen telefoon.
   - Stuur een SMS naar de senior met de tekst: `#STATUS`
   - Als alles goed is ingesteld, krijgt u binnen enkele seconden een SMS terug met de batterijstatus.

---

## 🛡️ 1. Veiligheid & Locatie
Cruciaal voor de directe veiligheid en hulpverlening.

| Commando | Uitleg & Voorbeeld |
|:---|:---|
| **`#WAAR`** | Vraagt de GPS-locatie op. U krijgt een Google Maps link terug. |
| **`#SOS_NU`** | Start direct de volledige noodprocedure (alle SOS-contacten worden gebeld). |
| **`#PING`** | Toont een grote melding: "ALLES GOED?" op het scherm. De senior kan op "Begrepen" tikken. |
| **`#BEL_TERUG`** | Laat de telefoon van de senior direct naar uw nummer bellen. |
| **`#SPEAKER`** | Forceert de luidspreker aan voor het eerstvolgende telefoongesprek. |
| **`#VEILIG AAN`** | Blokkeert alle oproepen van onbekende nummers (anti-scam). |
| **`#BLOKKEER [nr]`** | Blokkeert een specifiek nummer. *Voorbeeld: `#BLOKKEER 0612345678`* |

---

## ⚙️ 2. Systeem & Instellingen
Herstel instellingen of pas het toestel aan op afstand.

| Commando | Uitleg & Voorbeeld |
|:---|:---|
| **`#WIFI AAN/UIT`** | Schakelt WiFi in of uit. |
| **`#BT AAN/UIT`** | Schakelt Bluetooth in of uit (voor gehoorapparaten). |
| **`#STIL AAN/UIT`** | Schakelt de stille modus (Niet Storen) in of uit. |
| **`#VOLUME [0-10]`** | Zet het belvolume op een schaal van 0 tot 10. *Voorbeeld: `#VOLUME 8`* |
| **`#VOLUME_MEDIA [0-10]`** | Zet het volume voor muziek/radio. *Voorbeeld: `#VOLUME_MEDIA 5`* |
| **`#HELDER [1-10]`** | Past de schermhelderheid aan. *Voorbeeld: `#HELDER 10`* |
| **`#SCHERM_TIJD [min]`** | Hoe lang het scherm aan blijft (1, 2, 5 of MAX). *Voorbeeld: `#SCHERM_TIJD 5`* |
| **`#RESTART`** | Herstart de Senioren Launcher app bij problemen. |
| **`#UPDATE_CHECK`** | Controleert op nieuwe veilige app-updates (Android 16/HTTPS). |

---

## 📱 3. Uiterlijk & Apps
Pas de interface aan de behoeften van de senior aan.

| Commando | Uitleg & Voorbeeld |
|:---|:---|
| **`#LETTER [1-5]`** | Verander de tekstgrootte. 1 is normaal, 5 is gigantisch. *Voorbeeld: `#LETTER 4`* |
| **`#THEMA [1-3]`** | 1: Klassiek, 2: Hoog Contrast (geel/zwart), 3: Licht thema. *Voorbeeld: `#THEMA 2`* |
| **`#SLOT AAN/UIT`** | Vergrendel de instellingenknop zodat de senior niets per ongeluk wijzigt. |
| **`#PIN [code]`** | Wijzig de toegangscode voor de instellingen. *Voorbeeld: `#PIN 1234`* |
| **`#OPEN [naam]`** | Opent een app op de voorgrond. *Voorbeeld: `#OPEN Foto's`* |
| **`#NOTIFICATIES_WEG`** | Veegt alle verwarrende meldingen bovenin het scherm weg. |

---

## 💊 4. Gezondheid & Agenda
Beheer medicatie en afspraken op afstand.

| Commando | Uitleg & Voorbeeld |
|:---|:---|
| **`#MEDICIJN [tijd] [naam]`** | Voegt een medicijn-herinnering toe. *Voorbeeld: `#MEDICIJN 08:30 Aspirine`* |
| **`#VOORRAAD [naam] [aantal]`**| Werkt het aantal pillen bij. *Voorbeeld: `#VOORRAAD Aspirine 20`* |
| **`#AGENDA [datum] [tijd] [tekst]`** | Voegt een afspraak toe. *Voorbeeld: `#AGENDA 24-12 14:00 Tandarts`* |
| **`#WEKKER [tijd] [naam]`** | Stelt een wekker in. *Voorbeeld: `#WEKKER 07:30 Opstaan`* |
| **`#RADIO_STOP`** | Stopt direct de spelende radio. |

---

## 🔍 5. Diagnose & Overzichten
Krijg informatie over de status van de telefoon.

| Commando | Wat krijgt u terug? |
|:---|:---|
| **`#STATUS`** | Batterijpercentage, huidig volume en of de telefoon op stil staat. |
| **`#INFO_PLUS`** | Vrije opslagruimte en de Android-versie. |
| **`#PRIVACY`** | Status van alle kritieke permissies (GPS, SMS, Locatie). |
| **`#LAATSTE_OPROEP`**| Wie heeft als laatste gebeld en was dit inkomend of gemist? |
| **`#AGENDA_VANDAAG`** | Een lijst met alle afspraken van vandaag. |
| **`#WEKKERS_LIJST`** | Overzicht van alle actieve wekkers en medicijn-tijden. |
| **`#APP_LIJST`** | Een lijst van de meest gebruikte apps op het toestel. |
| **`#NETWERK`** | Provider naam (bijv. KPN) en verbindingstype (4G/5G). |

---

## ✉️ 6. Berichten & Contacten
Beheer de contactenlijst op afstand.

| Commando | Uitleg & Voorbeeld |
|:---|:---|
| **`#BERICHT [tekst]`** | Toont een grote popup-melding op het scherm van de senior. *Voorbeeld: `#BERICHT Ik kom over 10 minuten!`* |
| **`#CONTACT [naam] [nr]`** | Voegt een nieuw contact toe aan de lijst. *Voorbeeld: `#CONTACT Jan 0611223344`* |
| **`#VERWIJDER_CONTACT [naam]`**| Verwijdert een contactpersoon. *Voorbeeld: `#VERWIJDER_CONTACT Jan`* |

---

## 🔦 7. Zoeken & Licht

| Commando | Wat het doet |
|:---|:---|
| **`LAUN_ZOEK`** | De telefoon roept "Ik ben hier!" en trilt maximaal om hem terug te vinden. |
| **`#LAMP AAN/UIT`** | Zet de zaklamp aan of uit. |
| **`#KNIPPER`** | Laat de zaklamp 10 keer knipperen (visueel signaal). |

---

## 📬 Hulp nodig?
Stuur **`#HELP`** of **`#HULP`** naar de telefoon van de senior om de belangrijkste codes direct op uw eigen telefoon te ontvangen.
