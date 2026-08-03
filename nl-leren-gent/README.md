# Nederlands leren in Gent 🇧🇪 — voor Darija-sprekers

Een interactieve webapp waarmee een Marokkaans-Darija sprekende gebruiker (uit
Oujda) stap voor stap **Vlaams-Nederlands** leert om vlot in **Gent** te
integreren. De cursus houdt rekening met de klank- en taalverschillen tussen
het Darija en het Nederlands.

> **Status: Stap 2b afgerond.** Volledige leerlijn (~220 items), dashboard met
> voortgang, interactieve lessen (kaartjes + audio + auto-quiz, werkt offline)
> én de spreekoefening: opnemen met **Groq Whisper** + AI-feedback met **Gemini**.

## Tech stack

- **React 18 + Vite** — snelle, moderne frontend
- **Tailwind CSS** — strakke, mobiel-vriendelijke UI
- **Web Speech API** (`window.speechSynthesis`, `nl-BE`) — voorlezen, native en gratis
- **Google Gemini API** (gratis tier) — grammatica-/antwoordcontrole _(Stap 2)_
- **Groq Whisper** (`whisper-large-v3`) — spraak-naar-tekst _(Stap 2)_

## Snel starten

```bash
cd nl-leren-gent
npm install
npm run dev
```

De app draait dan op http://localhost:5173.

### Sleutels voor de spreekoefening (gratis)

Alle lessen en quizzen werken **zonder** sleutels. Enkel de spreekoefening
(microfoon + AI-feedback) heeft twee gratis sleutels nodig. Twee manieren:

1. **In de app (makkelijkst):** open het tandwiel (⚙️) rechtsboven en plak de
   sleutels. Ze worden lokaal op het toestel bewaard.
2. **Via .env:** `cp .env.example .env` en vul `VITE_GEMINI_API_KEY` en
   `VITE_GROQ_API_KEY` in.

- Gemini-sleutel: https://aistudio.google.com/app/apikey
- Groq-sleutel: https://console.groq.com/keys

## Projectstructuur

```
nl-leren-gent/
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
├── .env.example
└── src/
    ├── main.jsx                  # entry point
    ├── App.jsx                   # navigatie (dashboard ⇄ niveau ⇄ les)
    ├── index.css                 # Tailwind + basiscomponenten
    ├── data/
    │   ├── curriculum.js         # ⭐ de volledige leerlijn (Niveau 0 & 1)
    │   └── resources.js          # externe oefenbronnen (links)
    ├── context/
    │   └── ProgressContext.jsx   # voortgang (localStorage)
    ├── hooks/
    │   └── useRecorder.js        # microfoon-opname (MediaRecorder)
    ├── lib/
    │   ├── speech.js             # Text-to-Speech helper (nl-BE)
    │   ├── quiz.js               # bouwt quizvragen uit een les
    │   ├── config.js             # API-sleutels & modelnamen
    │   ├── gemini.js             # evaluateAnswer() — AI-feedback
    │   └── groq.js               # transcribeAudio() — Whisper
    └── components/
        ├── Header.jsx
        ├── Dashboard.jsx         # niveaukeuze + totale voortgang
        ├── LevelCard.jsx
        ├── LevelView.jsx         # modules + lessen van één niveau
        ├── LessonRow.jsx
        ├── LessonPlayer.jsx      # leren → quiz → klaar (of spreekoefening)
        ├── SpeakingExercise.jsx  # opnemen → Whisper → Gemini-feedback
        ├── Settings.jsx          # API-sleutels invullen
        └── ProgressBar.jsx
```

## De leerlijn (`src/data/curriculum.js`)

Alles staat in één helder gestructureerd data-object met het schema
`Level → Module → Lesson → Item`. Zie de uitgebreide toelichting bovenaan het
bestand. Elk `Item` kan Nederlandse tekst, een Darija-vertaling (Arabisch én
Latijns schrift), IPA-uitspraak, een uitspraaktip voor Darija-sprekers en een
voorbeeldzin bevatten.

### Niveau 0 — Absolute basis & fonetiek
- **0.1** Klankleer & uitspraak (korte/lange klinkers, tweeklanken, P/B · F/V · G/CH)
- **0.2** Begroetingen & beleefdheid
- **0.3** Getallen 0–20
- **0.4** Vraagwoorden & omgeving

### Niveau 1 — Eerste communicatie (A1.1)
- **1.1** Jezelf voorstellen
- **1.2** Familie & gezin
- **1.3** Basisgrammatica (de/het, tegenwoordige tijd, zijn & hebben)
- **1.4** Vragen stellen & boodschappen (inversie, getallen 20–100, winkel/bakker)

## Zo breid je uit

- **Nieuwe les/module:** voeg een object toe aan `curriculum.js`. De UI en de
  voortgangsberekening passen zich automatisch aan.
- **Nieuw lestype:** geef de les een `type` en plug in Stap 2 een component in
  op basis van `lesson.type` (`phonetics`, `vocab`, `grammar`, `speaking`, …).
```
