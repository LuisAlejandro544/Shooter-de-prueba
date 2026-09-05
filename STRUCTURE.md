# Arquitectura y Estructura del Sistema (STRUCTURE.md)

Este documento detalla la organización de directorios, capas de abstracción y flujo de datos de **PolyStrike 3D**.

---

## 1. Diagrama de Capas

```
┌────────────────────────────────────────────────────────┐
│                   Capa de Interfaz                     │
│   Jetpack Compose: MainGameScreen, TouchControls, HUD  │
└───────────────────────────┬────────────────────────────┘
                            │ Estado / Comandos
┌───────────────────────────▼────────────────────────────┐
│                  Capa de Lógica (JVM)                  │
│       GameViewModel (StateFlow, Bucle de Juego, Score) │
└─────────────┬────────────────────────────┬─────────────┘
              │ Eventos                    │ Render loop
┌─────────────▼──────────────┐ ┌───────────▼─────────────┐
│    Audio & Efectos         │ │  Renderizador 3D        │
│ SoundFxManager (AudioTrack)│ │  GameSurfaceView + GLES │
└─────────────┬──────────────┘ └───────────┬─────────────┘
              │                            │
┌─────────────▼────────────────────────────▼─────────────┐
│          Capa Nativa Multi-lenguaje (JNI)              │
│  NativeEngineBridge.kt                                 │
│  ├── C++ Core: native_engine.cpp                       │
│  ├── Lua 5.4: Intérprete Oficial PUC-Rio (lapi, lvm...) │
│  └── Rust Core: polystrike_core (lib.rs, cargo)        │
└────────────────────────────────────────────────────────┘
```

---

## 2. Estructura de Directorios

```
/
├── .github/
│   └── workflows/
│       ├── build-debug.yml          # Pipeline CI/CD de compilación APK Debug en GitHub
│       └── generate-assets-3d.yml   # Generación de modelos 3D con Blender Headless y licencias CC0
├── app/
│   ├── build.gradle.kts             # Configuración Gradle, NDK y dependencias
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # Configuración de orientación landscape y actividad
│       │   ├── assets/models/        # Modelos 3D generados (.glb) con esqueletos y animaciones
│       │   ├── cpp/                  # Módulo C++ y Lua original
│       │   │   ├── CMakeLists.txt   # Script de construcción CMake
│       │   │   ├── native_engine.cpp # Implementación JNI en C++
│       │   │   └── lua/             # Fuentes C puras de Lua 5.4.7
│       │   ├── java/com/example/
│       │   │   ├── game/
│       │   │   │   ├── audio/       # Generador procedural de audio PCM táctico
│       │   │   │   ├── gl/          # Renderizador OpenGL ES, shaders, mallas 3D
│       │   │   │   ├── model/       # Entidades: Jugador, Balas, Operadores, Coberturas
│       │   │   │   └── viewmodel/   # GameViewModel y UiHudState
│       │   │   ├── nativebridge/
│       │   │   │   └── NativeEngineBridge.kt # Enlace JNI con C++, Lua y Rust
│       │   │   ├── ui/              # Componentes Jetpack Compose (HUD táctico, Miras, Joystick)
│       │   │   └── MainActivity.kt  # Punto de entrada de la app
│       │   ├── jniLibs/
│       │   │   ├── arm64-v8a/       # Binarios .so para teléfonos reales (ARM 64-bit)
│       │   │   └── x86_64/          # Binarios .so para emuladores
│       │   └── rust/
│       │       └── polystrike_core/
│       │           ├── Cargo.toml   # Definición del crate Rust con crate-type cdylib
│       │           └── src/lib.rs   # Código fuente de Rust con funciones JNI
├── tools/
│   └── blender/
│       └── generate_realistic_assets.py # Generador procedural y ensamble de armas, soldados y coberturas
├── build_rust.sh                    # Script utilitario de compilación de Rust
├── README.md                        # Documentación técnica de inicio
├── ROADMAP.md                       # Plan de trabajo
├── STRUCTURE.md                     # Este archivo de arquitectura
├── AI_CONTEXT.md                    # Contexto técnico para modelos de lenguaje
└── AGENTS.md                        # Directivas para agentes inteligentes
```

---

## 3. Modelo de Datos y Entidades Tácticas

### `PlayerState`
- `position: Vector3(x, y, z)`
- `yaw: Float` (Ángulo horizontal de vista)
- `pitch: Float` (Ángulo vertical de vista, limitado a [-75°, 75°])
- `health: Int` (Salud del jugador 0-100)
- `isAimingDownSights (ADS): Boolean` (Modo apuntado con mira óptica)
- `activeWeapon: TacticalWeapon` (Arma equipada actualmente)

### `TacticalWeapon`
- `id: String` (ej: `tactical_assault_rifle`, `service_pistol`)
- `damage: Float`
- `fireRateRpm: Int`
- `magazineCapacity: Int`
- `currentAmmo: Int`
- `recoilPattern: Vector2(vertical, horizontal)`
- `reloadTimeSeconds: Float`

### `HostileOperator` (Soldados / Mercenarios enemigos)
- `id: Long`
- `position: Vector3(x, y, z)`
- `velocity: Vector3(vx, vy, vz)`
- `health: Float` (Vida de 0 a 100)
- `state: Enum` (`PATROL`, `ALERT`, `TAKING_COVER`, `FIRING`, `DOWNED`)
- `weaponModel: String`

### `TacticalCover` (Muros, Sacos de arena, Cajas de munición)
- `id: Long`
- `position: Vector3(x, y, z)`
- `boundingBox: BoundingBox(min, max)`
- `penetrationResistance: Float` (Capacidad de frenar proyectiles)

### `BallisticProjectile` (Balística y balas trazadoras)
- `id: Long`
- `position: Vector3(x, y, z)`
- `velocity: Vector3(vx, vy, vz)`
- `damage: Float`
- `lifetimeSeconds: Float`

### `Particle` (Chispas de impacto balístico y humo de cañón)
- `position: Vector3(x, y, z)`
- `velocity: Vector3(vx, vy, vz)`
- `color: Vector4(r, g, b, a)`
- `life: Float` (0.0 a 1.0)

