**🇷🇺 Русский язык далее — Russian version below ↓**

---

# Smart LED — BLE LED Strip Controller

An Android application for controlling addressable RGB LED strips (WS2812B/NeoPixel) via Bluetooth Low Energy (BLE).

## Features

- **Individual LED color control** — tap any LED in the grid to open a color picker
- **Brightness slider** — adjust overall brightness from 0 to 255
- **Fill all LEDs** — fill the entire strip with a single color at once
- **Background BLE service** — maintains connection even when the app is in the background
- **Quick Settings tile** — toggle LEDs on (white) or off directly from the notification shade
- **Theme support** — choose between Light, Dark, or System theme
- **Persistent settings** — theme preference is saved between sessions
- **Notifications** — persistent notification with quick-action buttons (white / off)

## Requirements

- Android 14 (API 34) or higher
- A BLE LED controller device running the compatible firmware
- The following permissions are required:
  - `BLUETOOTH_CONNECT`
  - `BLUETOOTH_SCAN`
  - `FOREGROUND_SERVICE`
  - `POST_NOTIFICATIONS`

## Tech Stack

- **Kotlin** 2.0.21
- **Jetpack Compose** with Material Design 3
- **Android Navigation Compose**
- **BLE GATT** for device communication
- **AmbilWarna** — color picker library

## BLE Protocol

The app communicates with the LED controller over GATT using the following UUIDs:

| Role | UUID |
|------|------|
| Service | `99b0bf79-fe33-4fa4-9e1c-263398667c40` |
| Color stream | `2c0a8901-383b-4b83-a88b-6e024a71bc22` |
| LED count | `337597f2-02e7-4cc6-938b-e0125160161b` |
| Brightness / commands | `a7fed865-d364-424c-87d6-9e893fb661c4` |

## Project Structure

```
app/src/main/java/com/nordik/smarthub/
├── MainActivity.kt       — main UI, navigation, state management
├── BLEservice.kt         — BLE GATT service (foreground)
├── TileService.kt        — Quick Settings tile
└── ui/
    ├── screens/
    │   ├── home.kt       — LED grid, color picker, brightness
    │   ├── effects.kt    — effects tab (placeholder)
    │   └── settings.kt   — theme selection
    └── theme/
        ├── Theme.kt
        ├── Color.kt
        ├── Type.kt
        └── themePreferences.kt
```

## Building

```bash
./gradlew assembleDebug
```

## Localization

The app interface is available in English and Russian.

---

# Smart LED — Контроллер светодиодной ленты по BLE

Приложение для Android для управления адресными RGB-светодиодными лентами (WS2812B/NeoPixel) по Bluetooth Low Energy (BLE).

## Возможности

- **Индивидуальное управление цветом** — нажмите на любой диод в сетке, чтобы выбрать цвет
- **Слайдер яркости** — регулировка общей яркости от 0 до 255
- **Заливка всех диодов** — заполнить всю ленту одним цветом одним нажатием
- **Фоновый BLE-сервис** — соединение сохраняется, даже когда приложение свёрнуто
- **Плитка быстрых настроек** — включение (белый) или выключение ленты прямо из шторки уведомлений
- **Поддержка тем** — светлая, тёмная или системная тема
- **Сохранение настроек** — выбранная тема сохраняется между сессиями
- **Уведомления** — постоянное уведомление с кнопками быстрого действия (белый / выкл)

## Требования

- Android 14 (API 34) и выше
- BLE-контроллер с совместимой прошивкой
- Необходимые разрешения:
  - `BLUETOOTH_CONNECT`
  - `BLUETOOTH_SCAN`
  - `FOREGROUND_SERVICE`
  - `POST_NOTIFICATIONS`

## Технологический стек

- **Kotlin** 2.0.21
- **Jetpack Compose** с Material Design 3
- **Android Navigation Compose**
- **BLE GATT** для связи с устройством
- **AmbilWarna** — библиотека для выбора цвета

## BLE-протокол

Приложение общается с контроллером через GATT, используя следующие UUID:

| Назначение | UUID |
|------------|------|
| Сервис | `99b0bf79-fe33-4fa4-9e1c-263398667c40` |
| Цветовой поток | `2c0a8901-383b-4b83-a88b-6e024a71bc22` |
| Количество диодов | `337597f2-02e7-4cc6-938b-e0125160161b` |
| Яркость / команды | `a7fed865-d364-424c-87d6-9e893fb661c4` |

## Структура проекта

```
app/src/main/java/com/nordik/smarthub/
├── MainActivity.kt       — главный UI, навигация, состояние
├── BLEservice.kt         — BLE GATT сервис (фоновый)
├── TileService.kt        — плитка быстрых настроек
└── ui/
    ├── screens/
    │   ├── home.kt       — сетка диодов, выбор цвета, яркость
    │   ├── effects.kt    — вкладка эффектов (заглушка)
    │   └── settings.kt   — выбор темы
    └── theme/
        ├── Theme.kt
        ├── Color.kt
        ├── Type.kt
        └── themePreferences.kt
```

## Сборка

```bash
./gradlew assembleDebug
```

## Локализация

Интерфейс доступен на английском и русском языках.
