# Roadmap Técnico - PolyStrike 3D (Tactical FPS)

Este documento define la trayectoria de desarrollo para transformar el motor en un shooter táctico en primera persona (FPS) móvil estilo *Call of Duty*.

---

## Fase 1: Validación y Base de Motores (Completada ✓)
- [x] Orientación nativa horizontal (*Landscape*) bloqueada para ergonomía táctil.
- [x] Renderizador OpenGL ES con shaders GLSL (Vértice y Fragmento con iluminación Lambertiana).
- [x] Control táctil dual (Joystick izquierdo analógico + Swipe derecho de cámara, apuntado ADS y disparo).
- [x] Audio procedural táctico en tiempo real vía `AudioTrack` PCM (disparos y retrocesos sintetizados).
- [x] Detección de colisiones 3D ray-sphere / bullet-bounding box para blancos y objetivos tácticos.
- [x] Integración de C++ mediante Android NDK y CMake 3.22.
- [x] Integración del intérprete oficial puro de Lua 5.4.7 (PUC-Rio).
- [x] Integración del módulo nativo en Rust (`polystrike_core`) con targets ARM64 y x86_64.
- [x] Workflow de GitHub Actions para compilación automatizada de APK Debug con JDK 21.
- [x] **Pipeline de Generación de Assets 3D (Blender Headless + GitHub Actions)**:
  - Generación de armas realistas con huesos de cerrojo, cargador y sockets (muzzle, ADS).
  - Generación de soldados/operadores militares con rigging humanoide y animaciones tácticas.
  - Generación de entornos balísticos (muros, sacos de arena, cajas de suministros).
  - Cumplimiento estricto de licencias 100% CC0 / MIT (Cero CC-BY) para monetización comercial sin atribución obligatoria.

---

## Fase 2: Arsenal Táctico y Lógica Militar en Lua (En Progreso)
- [ ] **Lector e Importador de Modelos 3D (.glb)**: Carga en tiempo de ejecución de los assets generados por Blender en el motor OpenGL ES.
- [ ] **Mecánicas Tácticas Estilo CoD**:
  - Apuntado por la mira (ADS - Aim Down Sights) con alineación precisa de cámara al socket de la mira.
  - Retroceso físico procedural del arma y dispersión de proyectiles balísticos.
  - Animación de recarga táctica sincronizada con el hueso del cargador.
- [ ] **Arsenal Militar Balanceado en Lua**:
  - Fusil de asalto táctico (fuego automático equilibrado, alcance medio).
  - Subfusil de alta cadencia (combate cerrado con alta dispersión).
  - Escopeta de combate pesado (alto daño a quemarropa con perdigones).
  - Pistola semiautomática táctica de servicio.
  - Fusil de tirador designado (precisión milimétrica a larga distancia).
- [ ] **Despliegue de Enemigos y Oleadas en Lua**:
  - Rutinas de IA en Lua para operadores hostiles con estados: patrulla, alerta, cobertura y fuego de supresión.
- [ ] **Coberturas y Obstáculos Balísticos**:
  - Incorporación de búnkeres de sacos de arena, barricadas de hormigón y cajas de munición con colisiones físicas.

---

## Fase 3: Optimización Nativa con C++ y Rust
- [ ] **Pipeline de Balística y Física en C++**:
  - Balística de proyectiles con caída por gravedad, penetración de coberturas y partículas de impacto calculadas en C++ para minimizar el recolector de basura (GC).
- [ ] **Gestor de Perfil, Arsenal y Estado Seguro en Rust**:
  - Manejo de árboles de estado del jugador (desbloqueo de camuflajes, accesorios, estadísticas de bajas y precisión) en `polystrike_core`.
- [ ] **Gestión de Mapas y Niveles en Lua**:
  - Definición de layouts de mapas militares completos en scripts de Lua.

---

## Fase 4: Experiencia Audiovisual y Pulido Móvil
- [ ] **Efectos de Fogonazo y Casquillos**: Emisión de partículas de fuego en el socket del cañón y expulsión animada de casquillos metálicos.
- [ ] **Sacudida Dinámica de Cámara (Camera Shake)**: Respuesta física de la cámara al disparar ráfagas o recibir impactos balísticos.
- [ ] **HUD Táctico y Menús en Jetpack Compose**:
  - Selector de clase y armamento en orientación horizontal.
  - Indicador de munición, selector de modo de fuego (automático/ráfaga/semiautomático) y minimapa táctico.
- [ ] **Compatibilidad con Mandos / Gamepads**: Soporte de entrada para mandos Bluetooth estándar.

