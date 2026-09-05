# PolyStrike 3D - Shooter de Arena Poligonal

Shooter en primera persona 3D para dispositivos móviles en formato horizontal (landscape), construido con una arquitectura híbrida de alto rendimiento que combina **Jetpack Compose**, **OpenGL ES 2.0 / 3.0**, **C++ (NDK)**, **Rust nativo** y el intérprete original de **Lua 5.4**.

---

## Características Principales

- **Renderizado 3D Nativo**: Pipeline gráfico OpenGL ES con shaders personalizados, proyección en perspectiva, iluminación difusa direccional, rejilla de combate y skybox poligonal.
- **Controles Dual Táctil**: Joystick analógico virtual izquierdo (desplazamiento 3D) y superficie gestual derecha (rotación Yaw/Pitch de cámara y disparo reactivo).
- **Audio y Efectos Sintetizados**: Audio sintetizado procedural en tiempo real mediante `AudioTrack` PCM (sin archivos WAV/MP3 pesados), sistema de partículas de chispas y detección de impactos en dianas móviles.
- **Pila Nativa Multi-lenguaje**:
  - **C++ (NDK 25.1 + CMake)**: Motor de cálculo matricial y JNI.
  - **Lua 5.4.7 Oficial (PUC-Rio)**: Código fuente C puro embebido sin intermediarios para lógica de scripts, balance y eventos.
  - **Rust (Cargo + JNI)**: Biblioteca `polystrike_core` compilada para arquitecturas ARM64 y x86_64 con seguridad de memoria.

---

## Requisitos Previos

- **Android SDK**: API 34 o superior (`minSdk = 24`, `targetSdk = 36`).
- **Android NDK**: Versión `25.1.8937393`.
- **CMake**: Versión `3.22.1`.
- **Rust & Cargo**: Toolchain `stable` con targets `aarch64-linux-android` y `x86_64-linux-android`.
- **JDK**: Java 17.

---

## Instalación y Compilación Paso a Paso

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/polystrike-3d.git
   cd polystrike-3d
   ```

2. **Compilar el núcleo nativo en Rust**:
   ```bash
   chmod +x build_rust.sh
   ./build_rust.sh
   ```

3. **Compilar el APK Debug**:
   ```bash
   ./gradlew assembleDebug
   ```
   El APK generado se encontrará en:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## Estructura del Proyecto

```
├── .github/workflows/          # Acciones de integración continua (CI/CD)
│   └── build-debug.yml         # Compilación limpia y firma de APK Debug en GitHub
├── app/
│   ├── src/main/
│   │   ├── cpp/                # Código nativo C++ y fuentes oficiales de Lua 5.4
│   │   │   ├── lua/            # Código fuente original C de Lua 5.4.7 (PUC-Rio)
│   │   │   ├── CMakeLists.txt  # Configuración CMake para compilar C++ y Lua
│   │   │   └── native_engine.cpp
│   │   ├── java/com/example/   # Código fuente en Kotlin
│   │   │   ├── game/           # Motor 3D OpenGL ES, shaders, audio y controles
│   │   │   ├── nativebridge/   # Puente JNI con C++, Rust y Lua
│   │   │   └── ui/             # Interfaz HUD y panel de validación en Jetpack Compose
│   │   ├── jniLibs/            # Binarios .so precompilados de Rust
│   │   └── rust/               # Código nativo en Rust
│   │       └── polystrike_core/
├── build_rust.sh               # Script de compilación para los targets Android de Rust
├── README.md                   # Documentación general
├── ROADMAP.md                  # Plan de evolución y hitos del proyecto
├── STRUCTURE.md                # Arquitectura técnica y modelo de datos
├── AI_CONTEXT.md               # Contexto para desarrollo guiado por IA
└── AGENTS.md                   # Directivas y reglas operativas para agentes
```

---

## Pruebas Automatizadas

Para ejecutar las pruebas unitarias locales en JVM:
```bash
gradle :app:testDebugUnitTest
```
