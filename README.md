# Jvk's Studio Mobile

App de producción musical nativa Android 100% — construida con **Kotlin + Jetpack Compose**.

## Stack técnico

| Componente | Tecnología |
|-----------|------------|
| Lenguaje | Kotlin 2.1 |
| UI | Jetpack Compose (Material 3) |
| MIDI | Android MIDI API (`android.media.midi`) |
| Audio | (próximamente: AAudio/Oboe) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |
| Build | Gradle 8.9 + AGP 8.7 |

## Estructura del proyecto

```
JvkStudioMobile/
├── app/src/main/
│   ├── java/com/jvk/studio/
│   │   ├── MainActivity.kt
│   │   ├── MainViewModel.kt
│   │   ├── midi/
│   │   │   └── MidiManager.kt
│   │   └── ui/
│   │       ├── theme/
│   │       │   ├── Theme.kt
│   │       │   └── Typography.kt
│   │       ├── components/
│   │       │   ├── Header.kt
│   │       │   └── PianoKeyboard.kt
│   │       └── screens/
│   │           └── MainScreen.kt
│   ├── res/values/
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── AndroidManifest.xml
├── .github/workflows/
│   └── build.yml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

## Compilar con GitHub Actions

1. Sube este proyecto a un repositorio GitHub
2. GitHub Actions compilará automáticamente en cada `push` a `main`
3. Descarga el APK desde la pestaña **Actions → artifacts**

## Compilar local

```bash
# Requiere JDK 17 y Android SDK instalados
chmod +x gradlew
./gradlew assembleDebug
# APK generado en: app/build/outputs/apk/debug/app-debug.apk
```

## Features actuales v1.0

- ✅ Teclado MIDI completo (C-1 a B8, 120 notas)
- ✅ Multi-touch en el teclado
- ✅ Transport: Play/Stop, REC, Rewind, BPM con clock MIDI
- ✅ MIDI clock sync enviado a DAW externa
- ✅ Diseño dark premium (estilo FL Studio Mobile)
- ✅ Toggle para ocultar/mostrar teclado
- ✅ Orientación landscape forzada

## Roadmap

- [ ] Playlist / Secuenciador de patrones
- [ ] Drum Pads
- [ ] Mixer multicanal
- [ ] Piano Roll
- [ ] Grabación MIDI
- [ ] Instrumentos virtuales (sintetizador)
- [ ] Efectos de audio (reverb, delay, EQ)
- [ ] Firma APK para publicación en Play Store
