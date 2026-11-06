# Bijdragen aan Senioren Launcher

**🌐 Taal:** [English](CONTRIBUTING_EN.md) · [Русский](CONTRIBUTING_RU.md) · **Nederlands**

Bedankt dat je wilt bijdragen!

## Hoe bij te dragen

### Bugs melden
Open een issue met het Bug Report template.

### Code bijdragen
1. Fork het project
2. Maak een feature branch: `git checkout -b feature/mijn-functie`
3. Commit: `git commit -m 'Voeg X toe'`
4. Push: `git push origin feature/mijn-functie`
5. Open een Pull Request

### Bouwen
- JDK 17 vereist (stel `JAVA_HOME` in); `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`).

### Vertalen
Zichtbare tekst staat in resourcebestanden, niet in de code.
1. Standaardtaal is Nederlands: `app/src/main/res/values/strings.xml`, `strings_ui.xml` en `strings_tiles.xml`.
2. Russisch staat in `app/src/main/res/values-ru/` (zelfde bestandsnamen) en dient als voorbeeld.
3. Maak voor een nieuwe taal een map `values-XX` (XX = taalcode), kopieer de drie bestanden en vertaal alle strings. Behoud plaatshouders (`%1$s`, `%2$d`).
4. Voeg de taalcode toe aan `LocaleHelper.SUPPORTED` en aan de taallijst in `SettingsScreen.kt`.
5. Gebruik in code `i18n(R.string.sleutel, args)` in plaats van tekstliteralen.
6. Vertaal de SMS-commando's (`#WAAR`, `#PING`, ...) **niet**; dat is een protocol tussen de twee apps.
7. Test met de grootste tekstgrootte.

## Ontwerp Principes
1. **Simpel** — als oma het niet snapt, is het te complex
2. **Groot** — alles moet leesbaar zijn zonder leesbril
3. **Veilig** — geen actie mag "iets kapotmaken"
4. **Privé** — geen data verlaat het apparaat
