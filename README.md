# 💡 Smart LED — BLE RGB Controller

> 🌐 [English](#-smart-led--ble-rgb-controller-english) | [Українська](#-smart-led--ble-rgb-контролер-українська) | [Русский](#-smart-led--ble-rgb-контроллер-русский)

---

## 💡 Smart LED — BLE RGB Controller (English)

Android application for controlling addressable RGB strips (WS2812/Neopixel) via Bluetooth Low Energy.

---

### 📋 Project Status

#### ✅ Implemented

| Module | Feature | Status |
|--------|---------|--------|
| **BLE Service** | GATT device connection | ✅ Done |
| **BLE Service** | Read strip length from device | ✅ Done |
| **BLE Service** | Send color stream to device | ✅ Done |
| **BLE Service** | Brightness control | ✅ Done |
| **BLE Service** | Foreground service (background operation) | ✅ Done |
| **Main Screen** | Individual color selection for each LED | ✅ Done |
| **Main Screen** | Fill all LEDs with one color | ✅ Done |
| **Notifications** | Persistent notification with "On" / "Off" buttons | ✅ Done |
| **Quick Settings Tile** | Android Quick Settings tile | ✅ Done |

---

#### 🔄 In Development

| Module | Feature | Status |
|--------|---------|--------|
| **Effects** | Animated patterns and cycles | 🔄 In progress |
| **Presets** | Save and load color schemes | 🔄 Planned |
| **Presets** | Restore state after restart | 🔄 Planned |
| **BLE** | Device discovery and selection (address is currently hardcoded) | 🔄 Planned |
| **BLE** | Auto-reconnect on connection loss | 🔄 Planned |

---

### 🛠 Technologies

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Bluetooth | Android BLE (GATT API) |
| Min SDK | Android 14 (API 34) |

---

### 🚀 Quick Start

1. Clone the repository
2. Open in Android Studio
3. Make sure the Bluetooth device is powered on and nearby
4. Run the app — the service will connect automatically

---

### 🔨 Build Guide

#### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 11** or newer (bundled with Android Studio)
- **Android SDK** with API level 34+ installed

#### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/NordikVibe/Ledhub.git
   cd Ledhub
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **File → Open** and choose the cloned `Ledhub` folder
   - Wait for Gradle sync to complete

3. **Connect a device or start an emulator**
   - Connect an Android 14+ device via USB with **USB Debugging** enabled, **or**
   - Start an AVD (Android Virtual Device) with API 34+

4. **Build and run**
   - Press the **Run ▶** button, or use the menu **Run → Run 'app'**
   - To generate a standalone APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - The resulting APK will be located at `app/build/outputs/apk/debug/app-debug.apk`

5. **Command-line build (optional)**
   ```bash
   # Debug APK
   ./gradlew assembleDebug

   # Release APK
   ./gradlew assembleRelease
   ```

---

## 💡 Smart LED — BLE RGB-контролер (Українська)

Android-застосунок для керування адресними RGB-стрічками (WS2812/Neopixel) через Bluetooth Low Energy.

---

### 📋 Статус проєкту

#### ✅ Реалізовано

| Модуль | Функція | Статус |
|--------|---------|--------|
| **BLE-сервіс** | Підключення до пристрою по GATT | ✅ Готово |
| **BLE-сервіс** | Зчитування довжини з пристрою | ✅ Готово |
| **BLE-сервіс** | Надсилання потоку кольорів на пристрій | ✅ Готово |
| **BLE-сервіс** | Керування яскравістю | ✅ Готово |
| **BLE-сервіс** | Foreground-сервіс (робота у фоні) | ✅ Готово |
| **Головний екран** | Індивідуальний вибір кольору для кожного LED | ✅ Готово |
| **Головний екран** | Заливка всіх LED одним кольором | ✅ Готово |
| **Сповіщення** | Постійне сповіщення з кнопками «Увімк» / «Вимк» | ✅ Готово |
| **Quick Settings Tile** | Плитка швидких налаштувань Android | ✅ Готово |

---

#### 🔄 У розробці

| Модуль | Функція | Статус |
|--------|---------|--------|
| **Ефекти** | Анімовані патерни та цикли | 🔄 У розробці |
| **Пресети** | Збереження та завантаження колірних схем | 🔄 Планується |
| **Пресети** | Відновлення стану після перезапуску | 🔄 Планується |
| **BLE** | Пошук і вибір пристрою (зараз адреса захардкоджена) | 🔄 Планується |
| **BLE** | Автоперепідключення при розриві зв'язку | 🔄 Планується |

---

### 🛠 Технології

| Компонент | Технологія |
|-----------|-----------|
| Мова | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Bluetooth | Android BLE (GATT API) |
| Min SDK | Android 14 (API 34) |

---

### 🚀 Швидкий старт

1. Клонуй репозиторій
2. Відкрий в Android Studio
3. Переконайся, що Bluetooth-пристрій увімкнено і знаходиться поряд
4. Запусти застосунок — сервіс підключиться автоматично

---

### 🔨 Гайд із самостійної компіляції

#### Вимоги

- **Android Studio** Hedgehog (2023.1.1) або новіша
- **JDK 11** або новіший (входить до складу Android Studio)
- **Android SDK** з встановленим API рівня 34+

#### Кроки

1. **Клонуй репозиторій**
   ```bash
   git clone https://github.com/NordikVibe/Ledhub.git
   cd Ledhub
   ```

2. **Відкрий в Android Studio**
   - Запусти Android Studio
   - Вибери **File → Open** і вкажи папку `Ledhub`
   - Дочекайся завершення синхронізації Gradle

3. **Підключи пристрій або запусти емулятор**
   - Підключи Android 14+ пристрій через USB із увімкненим **USB-налагодженням**, **або**
   - Запусти AVD (Android Virtual Device) з API 34+

4. **Зберіть та запустіть**
   - Натисни кнопку **Run ▶**, або скористайся меню **Run → Run 'app'**
   - Для генерації окремого APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - Готовий APK знаходитиметься за шляхом `app/build/outputs/apk/debug/app-debug.apk`

5. **Збірка через командний рядок (опційно)**
   ```bash
   # Debug APK
   ./gradlew assembleDebug

   # Release APK
   ./gradlew assembleRelease
   ```

---

## 💡 Smart LED — BLE RGB-контроллер (Русский)

Android-приложение для управления адресными RGB-лентами (WS2812/Neopixel) по Bluetooth Low Energy.

---

### 📋 Статус проекта

#### ✅ Реализовано

| Модуль | Функция | Статус |
|--------|---------|--------|
| **BLE-сервис** | Подключение к устройству по GATT | ✅ Готово |
| **BLE-сервис** | Чтение длины с устройства | ✅ Готово |
| **BLE-сервис** | Отправка потока цветов на устройство | ✅ Готово |
| **BLE-сервис** | Управление яркостью | ✅ Готово |
| **BLE-сервис** | Foreground-сервис (работа в фоне) | ✅ Готово |
| **Главный экран** | Индивидуальный выбор цвета для каждого LED | ✅ Готово |
| **Главный экран** | Заливка всех LED одним цветом | ✅ Готово |
| **Уведомления** | Постоянное уведомление с кнопками «Вкл» / «Выкл» | ✅ Готово |
| **Quick Settings Tile** | Плитка быстрых настроек Android | ✅ Готово |

---

#### 🔄 В разработке

| Модуль | Функция | Статус |
|--------|---------|--------|
| **Эффекты** | Анимированные паттерны и циклы | 🔄 В разработке |
| **Пресеты** | Сохранение и загрузка цветовых схем | 🔄 Планируется |
| **Пресеты** | Восстановление состояния после перезапуска | 🔄 Планируется |
| **BLE** | Поиск и выбор устройства (сейчас адрес захардкожен) | 🔄 Планируется |
| **BLE** | Автопереподключение при разрыве связи | 🔄 Планируется |

---

### 🛠 Технологии

| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Bluetooth | Android BLE (GATT API) |
| Min SDK | Android 14 (API 34) |

---

### 🚀 Быстрый старт

1. Клонируй репозиторий
2. Открой в Android Studio
3. Убедись, что Bluetooth-устройство включено и находится рядом
4. Запусти приложение — сервис подключится автоматически

---

### 🔨 Гайд по самостоятельной компиляции

#### Требования

- **Android Studio** Hedgehog (2023.1.1) или новее
- **JDK 11** или новее (входит в состав Android Studio)
- **Android SDK** с установленным уровнем API 34+

#### Шаги

1. **Клонируй репозиторий**
   ```bash
   git clone https://github.com/NordikVibe/Ledhub.git
   cd Ledhub
   ```

2. **Открой в Android Studio**
   - Запусти Android Studio
   - Выбери **File → Open** и укажи папку `Ledhub`
   - Дождись завершения синхронизации Gradle

3. **Подключи устройство или запусти эмулятор**
   - Подключи Android 14+ устройство через USB с включённой **отладкой по USB**, **или**
   - Запусти AVD (Android Virtual Device) с API 34+

4. **Собери и запусти**
   - Нажми кнопку **Run ▶**, или используй меню **Run → Run 'app'**
   - Для генерации отдельного APK: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - Готовый APK будет находиться по пути `app/build/outputs/apk/debug/app-debug.apk`

5. **Сборка через командную строку (опционально)**
   ```bash
   # Debug APK
   ./gradlew assembleDebug

   # Release APK
   ./gradlew assembleRelease
   ```
