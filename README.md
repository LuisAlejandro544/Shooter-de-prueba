# PolyStrike 3D - Shooter Táctico en Primera Persona (FPS)

Shooter táctico en primera persona (FPS) estilo *Call of Duty* para dispositivos móviles en formato horizontal (*landscape*), construido con una arquitectura híbrida de alto rendimiento que combina **Jetpack Compose**, **OpenGL ES 2.0 / 3.0**, **C++ (NDK)**, **Rust nativo**, el intérprete original de **Lua 5.4**, y un pipeline automatizado en **GitHub Actions con Blender Headless** para generación de assets 3D realistas con rigging y animaciones.

---

## Características Principales

- **Jugabilidad FPS Táctica**: Combate en primera persona con miras ópticas (ADS), retroceso realista de armas, recargas animadas, operadores tácticos militares y coberturas balísticas.
- **Renderizado 3D Nativo**: Pipeline gráfico OpenGL ES con shaders personalizados, proyección en perspectiva, iluminación difusa direccional, rejilla táctica y skybox dinámico.
- **Controles Dual Táctil Móvil**: Joystick analógico virtual izquierdo (desplazamiento 3D) y superficie gestual derecha (rotación Yaw/Pitch de cámara, centrado de mira y disparo reactivo).
- **Generación Automatizada de Assets 3D (GitHub Actions + Blender)**: Pipeline headless para sintetizar armas realistas (fusiles de asalto, escopetas, subfusiles, pistolas), soldados militares con esqueleto humanoide y animaciones (idle, trote táctico, disparo, impacto, muerte), y entornos balísticos (muros militares, sacos de arena, cajas de munición) en formato móvil `.glb`.
  - **Cumplimiento de Licencias**: 100% de datasets y algoritmos bajo **CC0 (Dominio Público) / MIT**, sin restricciones de atribución (Cero CC-BY) y totalmente apto para monetización y distribución cerrada en tiendas como Uptodown.
- **Audio y Efectos Sintetizados**: Audio sintetizado procedural en tiempo real mediante `AudioTrack` PCM (sin archivos WAV/MP3 pesados), sistema de partículas de chispas balísticas y detección de impactos en tiempo real.
- **Pila Nativa Multi-lenguaje**:
  - **C++ (NDK 25.1 + CMake)**: Motor de cálculo matricial y JNI.
  - **Lua 5.4.7 Oficial (PUC-Rio)**: Código fuente C puro embebido sin intermediarios para lógica de scripts, balance de armas y oleadas enemigas.
  - **Rust (Cargo + JNI)**: Biblioteca `polystrike_core` compilada para arquitecturas ARM64 y x86_64 con seguridad de memoria.

---

## Requisitos Previos

- **Android SDK**: API 34 o superior (`minSdk = 24`, `targetSdk = 36`).
- **Android NDK**: Versión `25.1.8937393`.
- **CMake**: Versión `3.22.1`.
- **Rust & Cargo**: Toolchain `stable` con targets `aarch64-linux-android` y `x86_64-linux-android`.
- **JDK**: Java 21 (Temurin).
- **Blender** (Opcional, en CI o local para generar modelos 3D): 3.0+.

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

## Generación de Assets 3D desde el Móvil (GitHub Actions)

Puedes generar nuevos modelos 3D con huesos y animaciones directamente desde el navegador de tu teléfono móvil:
1. Dirígete a la pestaña **Actions** en tu repositorio de GitHub.
2. Selecciona el flujo **Generate 3D Tactical Assets**.
3. Pulsa **Run workflow** y elige la categoría (`weapon`, `character`, `environment`) y el modelo deseado (`tactical_assault_rifle`, `operator_soldier`, etc.).
4. El Action generará el archivo binario `.glb` con materiales PBR y animaciones horneadas, guardándolo automáticamente en `app/src/main/assets/models/`.

---

## Estructura del Proyecto

```
├── .github/workflows/          # Acciones de integración continua (CI/CD)
│   ├── build-debug.yml         # Compilación limpia y firma de APK Debug en GitHub
│   └── generate-assets-3d.yml  # Generación automatizada de modelos 3D con Blender
├── app/
│   ├── src/main/
│   │   ├── assets/models/      # Modelos 3D (.glb) para armas, soldados y coberturas
│   │   ├── cpp/                # Código nativo C++ y fuentes oficiales de Lua 5.4
│   │   │   ├── lua/            # Código fuente original C de Lua 5.4.7 (PUC-Rio)
│   │   │   ├── CMakeLists.txt  # Configuración CMake para compilar C++ y Lua
│   │   │   └── native_engine.cpp
│   │   ├── java/com/example/   # Código fuente en Kotlin
│   │   │   ├── game/           # Motor 3D OpenGL ES, shaders, audio y controles
│   │   │   ├── nativebridge/   # Puente JNI con C++, Rust y Lua
│   │   │   └── ui/             # Interfaz HUD táctica y controles en Jetpack Compose
│   │   ├── jniLibs/            # Binarios .so precompilados de Rust
│   │   └── rust/               # Código nativo en Rust
│   │       └── polystrike_core/
├── tools/blender/              # Scripts de automatización 3D (Blender Python API)
│   └── generate_realistic_assets.py
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
