# Contexto para Asistentes de IA (AI_CONTEXT.md)

Este documento describe las directrices, decisiones técnicas y consideraciones de diseño del proyecto **PolyStrike 3D** para que cualquier modelo de IA pueda continuar el desarrollo sin romper las convenciones establecidas.

---

## 1. Perfil del Usuario y Dispositivo
- **Entorno del Usuario**: El usuario utiliza un teléfono móvil (no tiene PC de escritorio). Todas las interfaces, controles e información deben estar optimizados para pantalla táctil en orientación horizontal (*Landscape*).
- **Canal de Distribución**: La aplicación se distribuirá en tiendas alternativas como Uptodown o descargas directas de APK (no a través de Google Play).
- **Enfoque de Dependencias**: Se prioriza la funcionalidad real y robusta sobre el tamaño del binario. Se deben usar dependencias y herramientas estándar de la industria cuando sea necesario.

---

## 2. Tecnologías y Motores Híbridos

### Kotlin + Jetpack Compose
- Responsable de la interfaz superpuesta (HUD), controles táctiles virtuales, paneles de diálogo y ciclo de vida de la actividad.
- Todo el código UI debe usar Material 3 y Jetpack Compose (`enableEdgeToEdge`, `WindowInsets`).

### OpenGL ES 2.0 / 3.0
- Renderizado de gráficos en una superficie dedicada `GameSurfaceView`.
- Iluminación matemática directa en shaders GLSL.
- Generación procedural de mallas para cubos, dianas y arena sin assets externos pesados.

### C++ & Android NDK
- Administrado por `CMake 3.22.1` con compilador Clang del NDK `25.1.8937393`.
- Responsable de hospedar el intérprete de Lua puro y operaciones de bajo nivel con JNI.
- Localización: `app/src/main/cpp/`.

### Lua 5.4 Original (PUC-Rio)
- Se utiliza el código C original de **Lua 5.4.7** sin envoltorios externos de terceros.
- Las cabeceras puras se incluyen con `extern "C" { #include "lua.h" ... }`.
- Se usa para lógica de juego, scripting de misiones y configuración de valores.

### Rust (`polystrike_core`)
- Estructurado como crate estándar con `crate-type = ["cdylib", "staticlib"]` en `app/src/main/rust/polystrike_core/`.
- Compila para `aarch64-linux-android` y `x86_64-linux-android` mediante el script `build_rust.sh`.
- Expone funciones JNI seguras para la JVM.

---

## 3. Reglas de Modificación
1. **No inventar marcas protegidas**: Evitar usar nombres comerciales o marcas registradas en el código, nombres de paquetes o archivos.
2. **Compatibilidad NDK/Gradle**: Siempre que se agreguen funciones en C++ o Rust, verificar que los scripts de compilación de Gradle (`build.gradle.kts`) y CMake (`CMakeLists.txt`) se mantengan sincronizados.
3. **No usar fallbacks vacíos**: Si se solicita una funcionalidad nativa en C++, Rust o Lua, debe implementarse en ese lenguaje y conectarse mediante JNI real.
