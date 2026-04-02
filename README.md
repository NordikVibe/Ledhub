# Ledhub — Smart LED Controller

> 🇷🇺 Русский и 🇺🇦 Українська переклади нижче / Russian and Ukrainian translations below

---

## 🇬🇧 English

### About
**Ledhub** is an Android application for controlling LED strips and smart lighting devices via Bluetooth Low Energy (BLE).  
Built with Kotlin + Jetpack Compose.

### Requirements
- Android Studio Meerkat (2024.3.1) or newer
- Android SDK 36 (compile) / SDK 34 (minimum)
- JDK 11+
- A physical Android device or emulator with Bluetooth support (API 34+)

### Project Structure
```
Ledhub/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/nordik/smarthub/
│   │       │   ├── MainActivity.kt        # Entry point, navigation setup
│   │       │   ├── BLEservice.kt          # Bluetooth Low Energy service
│   │       │   ├── TileService.kt         # Quick-settings tile service
│   │       │   └── ui/
│   │       │       ├── screens/
│   │       │       │   ├── home.kt        # Home screen (color picker, brightness)
│   │       │       │   ├── effects.kt     # LED effects screen
│   │       │       │   └── settings.kt    # App settings screen
│   │       │       └── theme/
│   │       │           ├── Color.kt       # Color palette
│   │       │           ├── Theme.kt       # Material3 theme
│   │       │           ├── Type.kt        # Typography
│   │       │           └── themePreferences.kt  # Theme persistence
│   │       ├── res/                       # Resources (layouts, drawables, strings)
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts                   # Module-level build config
│   └── proguard-rules.pro
├── build.gradle.kts                       # Project-level build config
├── settings.gradle.kts                    # Project settings & module includes
├── gradle.properties                      # Gradle properties
└── gradlew / gradlew.bat                  # Gradle wrapper scripts
```

### Download
Clone the repository using Git:
```bash
git clone https://github.com/NordikVibe/Ledhub.git
cd Ledhub
```

Or download the ZIP archive directly from GitHub:  
**Code → Download ZIP** → extract the archive.

### Build & Run

**Option 1 — Android Studio (recommended)**
1. Open Android Studio.
2. Select **File → Open** and choose the `Ledhub` folder.
3. Wait for Gradle sync to finish.
4. Connect a device or start an emulator.
5. Click the **▶ Run** button.

**Option 2 — Command line**
```bash
# Assemble a debug APK
./gradlew assembleDebug

# Install directly on a connected device
./gradlew installDebug

# Assemble a release APK (requires signing config)
./gradlew assembleRelease
```

The built APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🇷🇺 Русский

### О проекте
**Ledhub** — Android-приложение для управления LED-лентами и умными осветительными приборами через Bluetooth Low Energy (BLE).  
Написано на Kotlin + Jetpack Compose.

### Требования
- Android Studio Meerkat (2024.3.1) или новее
- Android SDK 36 (компиляция) / SDK 34 (минимум)
- JDK 11+
- Физическое Android-устройство или эмулятор с поддержкой Bluetooth (API 34+)

### Структура проекта
```
Ledhub/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/nordik/smarthub/
│   │       │   ├── MainActivity.kt        # Точка входа, настройка навигации
│   │       │   ├── BLEservice.kt          # Сервис Bluetooth Low Energy
│   │       │   ├── TileService.kt         # Сервис плитки быстрых настроек
│   │       │   └── ui/
│   │       │       ├── screens/
│   │       │       │   ├── home.kt        # Главный экран (выбор цвета, яркость)
│   │       │       │   ├── effects.kt     # Экран эффектов LED
│   │       │       │   └── settings.kt    # Экран настроек приложения
│   │       │       └── theme/
│   │       │           ├── Color.kt       # Цветовая палитра
│   │       │           ├── Theme.kt       # Тема Material3
│   │       │           ├── Type.kt        # Типография
│   │       │           └── themePreferences.kt  # Сохранение темы
│   │       ├── res/                       # Ресурсы (макеты, изображения, строки)
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts                   # Конфиг сборки модуля
│   └── proguard-rules.pro
├── build.gradle.kts                       # Конфиг сборки проекта
├── settings.gradle.kts                    # Настройки проекта и модулей
├── gradle.properties                      # Свойства Gradle
└── gradlew / gradlew.bat                  # Скрипты обёртки Gradle
```

### Скачивание
Клонировать репозиторий через Git:
```bash
git clone https://github.com/NordikVibe/Ledhub.git
cd Ledhub
```

Или скачать ZIP-архив напрямую с GitHub:  
**Code → Download ZIP** → распаковать архив.

### Сборка и запуск

**Вариант 1 — Android Studio (рекомендуется)**
1. Открыть Android Studio.
2. Выбрать **File → Open** и указать папку `Ledhub`.
3. Дождаться окончания синхронизации Gradle.
4. Подключить устройство или запустить эмулятор.
5. Нажать кнопку **▶ Run**.

**Вариант 2 — Командная строка**
```bash
# Собрать debug APK
./gradlew assembleDebug

# Установить на подключённое устройство
./gradlew installDebug

# Собрать release APK (требует настройки подписи)
./gradlew assembleRelease
```

Собранный APK будет находиться по адресу:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🇺🇦 Українська — Ukraine dev

### Про проєкт
**Ledhub** — Android-застосунок для керування LED-стрічками та розумними освітлювальними пристроями через Bluetooth Low Energy (BLE).  
Написано на Kotlin + Jetpack Compose.

### Вимоги
- Android Studio Meerkat (2024.3.1) або новіше
- Android SDK 36 (компіляція) / SDK 34 (мінімум)
- JDK 11+
- Фізичний Android-пристрій або емулятор із підтримкою Bluetooth (API 34+)

### Структура проєкту
```
Ledhub/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/nordik/smarthub/
│   │       │   ├── MainActivity.kt        # Точка входу, налаштування навігації
│   │       │   ├── BLEservice.kt          # Сервіс Bluetooth Low Energy
│   │       │   ├── TileService.kt         # Сервіс плитки швидких налаштувань
│   │       │   └── ui/
│   │       │       ├── screens/
│   │       │       │   ├── home.kt        # Головний екран (вибір кольору, яскравість)
│   │       │       │   ├── effects.kt     # Екран ефектів LED
│   │       │       │   └── settings.kt    # Екран налаштувань застосунку
│   │       │       └── theme/
│   │       │           ├── Color.kt       # Кольорова палітра
│   │       │           ├── Theme.kt       # Тема Material3
│   │       │           ├── Type.kt        # Типографіка
│   │       │           └── themePreferences.kt  # Збереження теми
│   │       ├── res/                       # Ресурси (макети, зображення, рядки)
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts                   # Конфіг збірки модуля
│   └── proguard-rules.pro
├── build.gradle.kts                       # Конфіг збірки проєкту
├── settings.gradle.kts                    # Налаштування проєкту та модулів
├── gradle.properties                      # Властивості Gradle
└── gradlew / gradlew.bat                  # Скрипти обгортки Gradle
```

### Завантаження
Клонувати репозиторій через Git:
```bash
git clone https://github.com/NordikVibe/Ledhub.git
cd Ledhub
```

Або завантажити ZIP-архів напряму з GitHub:  
**Code → Download ZIP** → розпакувати архів.

### Збірка та запуск

**Варіант 1 — Android Studio (рекомендується)**
1. Відкрити Android Studio.
2. Обрати **File → Open** і вказати папку `Ledhub`.
3. Зачекати завершення синхронізації Gradle.
4. Підключити пристрій або запустити емулятор.
5. Натиснути кнопку **▶ Run**.

**Варіант 2 — Командний рядок**
```bash
# Зібрати debug APK
./gradlew assembleDebug

# Встановити на підключений пристрій
./gradlew installDebug

# Зібрати release APK (потребує налаштування підпису)
./gradlew assembleRelease
```

Зібраний APK знаходитиметься за адресою:
```
app/build/outputs/apk/debug/app-debug.apk
```
