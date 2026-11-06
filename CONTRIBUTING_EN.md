# Contributing to Senioren Launcher

**🌐 Language:** **English** · [Русский](CONTRIBUTING_RU.md) · [Nederlands](CONTRIBUTING.md)

Thank you for wanting to contribute!

## How to contribute

### Reporting bugs
Open an issue using the Bug Report template.

### Contributing code
1. Fork the project
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit: `git commit -m 'Add X'`
4. Push: `git push origin feature/my-feature`
5. Open a Pull Request

### Building
- JDK 17 is required (set `JAVA_HOME`).
- `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`) → `app/build/outputs/apk/debug/app-debug.apk`.
- Some phones (e.g. Xiaomi/MIUI without a SIM card) block `adb install`; copy the APK to the phone and open it with the Files app.

### Translating
User-visible text lives in resource files, never directly in code:
1. Dutch is the default language: `app/src/main/res/values/strings.xml`, `strings_ui.xml` and `strings_tiles.xml`.
2. Russian is in `app/src/main/res/values-ru/` (same file names). Use it as an example.
3. For a new language, create a folder `values-XX` (XX = language code), copy the three files and translate every string. Keep placeholders (`%1$s`, `%2$d`) and their numbering.
4. Add the language code to `LocaleHelper.SUPPORTED` (`util/LocaleHelper.kt`) and to the language list in `SettingsScreen.kt`. The first-run language picker uses `LocaleHelper.SUPPORTED`.
5. In code use `i18n(R.string.key, args)` (works in composables, services and receivers) instead of string literals. In composables `stringResource` also works.
6. Do **not** translate the SMS remote commands (`#WAAR`, `#PING`, ...). They are a protocol between the two apps.
7. Test with the largest text size: Russian and German texts are longer than Dutch ones and must not be cut off.

## Design Principles
1. **Simple** — if grandma doesn't understand it, it is too complex
2. **Large** — everything must be readable without reading glasses, at every text size (the whole UI scales with it, so layouts must scroll or adapt instead of assuming a fixed size)
3. **Safe** — no action may "break something"; prefer big buttons over gestures such as drag-and-drop or long-press-to-delete
4. **Private** — no data leaves the device
