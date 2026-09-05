# Contexto para Asistentes de IA (AI_CONTEXT.md)

Este documento describe las directrices, decisiones técnicas y consideraciones de diseño del proyecto **PolyStrike 3D** para que cualquier modelo de IA pueda continuar el desarrollo sin romper las convenciones establecidas.

---

## 1. Perfil del Proyecto y Usuario
- **Género del Juego**: Shooter táctico en primera persona (FPS) estilo *Call of Duty* para dispositivos móviles. Incluye mecánicas de apuntado por mira (ADS), retroceso realista, recargas animadas, operadores hostiles y coberturas balísticas.
- **Entorno del Usuario**: El usuario programa y gestiona el proyecto exclusivamente desde un teléfono móvil (sin PC de escritorio). Todas las interfaces, controles e integraciones (como disparar GitHub Actions) deben ser operables desde el móvil en orientación horizontal (*Landscape*).
- **Canal de Distribución y Monetización**: La aplicación se distribuirá en tiendas alternativas como Uptodown o descargas directas de APK (no a través de Google Play). El proyecto es de código cerrado y apto para monetización comercial.
- **Enfoque de Dependencias**: Se prioriza la funcionalidad real y robusta sobre el tamaño del binario. Se deben usar dependencias y herramientas estándar de la industria.

---

## 2. Tecnologías y Motores Híbridos

### Kotlin + Jetpack Compose
- Responsable de la interfaz superpuesta (HUD táctico), controles táctiles virtuales (joystick analógico izquierdo, swipe derecho con apuntado y disparo), paneles de diálogo y ciclo de vida.
- Todo el código UI debe usar Material 3 y Jetpack Compose (`enableEdgeToEdge`, `WindowInsets`).

### OpenGL ES 2.0 / 3.0
- Renderizado de gráficos en una superficie dedicada `GameSurfaceView`.
- Iluminación matemática directa en shaders GLSL, soporte para geometrías tácticas y carga de modelos binarios `.glb`.

### Generador de Assets 3D (GitHub Actions + Blender Headless)
- Pipeline automatizado en `.github/workflows/generate-assets-3d.yml` activable desde el móvil (`workflow_dispatch`).
- Utiliza `tools/blender/generate_realistic_assets.py` para sintetizar mallas PBR realistas de armas, soldados/operadores con rigging (esqueletos) y animaciones de combate, y coberturas balísticas en formato `.glb`.
- **Regla Estricta de Licencias**: Todos los modelos procedurales y datasets integrados deben ser **100% CC0 (Dominio Público) o MIT**. **Queda estrictamente prohibido usar datasets bajo licencia CC-BY (Creative Commons Atribución)** u otras licencias virales (GPL, CC-BY-SA, CC-BY-NC) para no obligar a dar créditos ni abrir el código comercial del juego.

### C++ & Android NDK
- Administrado por `CMake 3.22.1` con compilador Clang del NDK `25.1.8937393`.
- Responsable de hospedar el intérprete de Lua puro y operaciones de bajo nivel con JNI.
- Localización: `app/src/main/cpp/`.

### Lua 5.4 Original (PUC-Rio)
- Se utiliza el código C original de **Lua 5.4.7** sin envoltorios externos de terceros.
- Las cabeceras puras se incluyen con `extern "C" { #include "lua.h" ... }`.
- Se usa para lógica de balance balístico, armas, oleadas de soldados hostiles y eventos.

### Rust (`polystrike_core`)
- Estructurado como crate estándar con `crate-type = ["cdylib", "staticlib"]` en `app/src/main/rust/polystrike_core/`.
- Compila para `aarch64-linux-android` y `x86_64-linux-android` mediante el script `build_rust.sh`.
- Expone funciones JNI seguras para la JVM.

---

## 3. Reglas de Modificación
1. **No inventar marcas protegidas**: Evitar usar nombres comerciales o marcas registradas en el código, nombres de paquetes o archivos (usar términos militares descriptivos como `tactical_assault_rifle`, `service_pistol`, `operator_soldier`).
2. **Compatibilidad NDK/Gradle**: Siempre que se agreguen funciones en C++ o Rust, verificar que los scripts de compilación de Gradle (`build.gradle.kts`) y CMake (`CMakeLists.txt`) se mantengan sincronizados.
3. **No usar fallbacks vacíos**: Si se solicita una funcionalidad nativa en C++, Rust o Lua, debe implementarse en ese lenguaje y conectarse mediante JNI real.

