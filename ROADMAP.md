# Roadmap Técnico - PolyStrike 3D

Este documento define la trayectoria de desarrollo para transformar la prueba de concepto validada en un shooter móvil 3D completo.

---

## Fase 1: Validación y Base de Motores (Completada ✓)
- [x] Orientación nativa horizontal (*Landscape*) bloqueada.
- [x] Renderizador OpenGL ES con shaders GLSL (Vértice y Fragmento con iluminación Lambertiana).
- [x] Control táctil dual (Joystick izquierdo analógico + Swipe derecho de cámara y disparo).
- [x] Audio procedural en tiempo real vía `AudioTrack` PCM (sin archivos multimedia pesados).
- [x] Detección de colisiones 3D ray-sphere / bullet-bounding box para dianas dinámicas.
- [x] Integración de C++ mediante Android NDK y CMake 3.22.
- [x] Integración del intérprete oficial puro de Lua 5.4.7 (PUC-Rio).
- [x] Integración del módulo nativo en Rust (`polystrike_core`) con targets ARM64 y x86_64.
- [x] Workflow de GitHub Actions para compilación automatizada de APK Debug.

---

## Fase 2: Expansión de Jugabilidad y Lógica en Lua (En Progreso)
- [ ] **Sistema de Oleadas**: Migración de la generación y patrones de movimiento de enemigos a scripts de Lua ejecutados en tiempo real.
- [ ] **Variedad de Armamento**:
  - Pistola láser de pulso (alta cadencia, daño moderado).
  - Cañón de riel de plasma (disparo perforante con tiempo de recarga).
  - Escopeta de dispersión poligonal (combate cercano).
- [ ] **Modificadores de Munición**: Definidos en tablas de Lua para ajustar dinámicamente velocidad, daño y dispersión.
- [ ] **Obstáculos y Coberturas 3D**: Incorporación de pilares y bloques destructibles en la arena.

---

## Fase 3: Optimización Nativa con C++ y Rust
- [ ] **Pipeline de Física y Detección en C++**:
  - Trasladar el cálculo espacial de proyectiles y partículas al módulo C++ para liberar el recolector de basura (GC) de Java/Kotlin.
- [ ] **Gestor de Entidades y Estado Seguro en Rust**:
  - Manejo de árboles de estado del juego (puntuaciones, árbol de progresión y serialización) en `polystrike_core`.
- [ ] **Carga Dinámica de Niveles**: Soporte para cargar escenarios completos codificados en scripts de Lua guardados en assets o almacenamiento local.

---

## Fase 4: Experiencia Audiovisual y Pulido Móvil
- [ ] **Efectos de Post-procesamiento**: Bloom ligero para láseres y niebla volumétrica poligonal en OpenGL ES.
- [ ] **Animaciones de Retroceso y Cámara**: Sacudida de cámara (camera shake) al disparar o recibir daño.
- [ ] **Menús y Tablas de Puntuación**: Integración de interfaz de récords personales y ajustes gráficos (60 FPS / 120 FPS).
- [ ] **Compatibilidad con Mandos / Gamepads**: Soporte de entrada para mandos Bluetooth estándar.
