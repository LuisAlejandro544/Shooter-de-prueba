# Directivas para Agentes de Código (AGENTS.md)

Este archivo define las reglas obligatorias de comportamiento y desarrollo para cualquier agente que opere en este repositorio.

---

## 1. Principios Fundamentales

1. **Razonamiento Previo Obligatorio**:
   - Antes de realizar cualquier cambio, debes razonar detenidamente sobre qué herramientas usar, el impacto arquitectónico y las dependencias requeridas. No respondas de forma precipitada.

2. **Entorno del Usuario**:
   - El usuario programa y prueba desde un dispositivo móvil. Todo el diseño de interfaz debe ser cómodo para pantallas táctiles en orientación horizontal (*Landscape*).

3. **Distribución Externa**:
   - El proyecto está orientado a tiendas de APKs de terceros (como Uptodown). No apliques restricciones arbitrarias de Google Play a menos que sean solicitadas.

4. **Reglas de Rendimiento y Sistema**:
   - En caso de trabajar con optimizadores o utilidades de juego (Game Booster), **nunca uses o intentes modificar propiedades de sistema del tipo `persist.sys.*`**.

5. **Mensajes de Commit (`commit_message.txt`)**:
   - Si existe un archivo `commit_message.txt`, asegúrate de que su información esté siempre en español. No lo sobrescribas ni lo modifiques a menos que el usuario lo solicite expresamente.

6. **Prioridad de Dependencias Reales**:
   - No intentes reinventar soluciones complejas sin dependencias para ahorrar unos pocos kilobytes. Usa librerías oficiales y bien mantenidas.

7. **Propiedad Intelectual**:
   - Evita utilizar en nombres de archivos o assets marcas registradas o nombres de terceros que puedan poner en riesgo legal al usuario.

8. **Compilación Nativa Integral**:
   - Si el proyecto usa C++, Rust o Lua, estos lenguajes deben estar completamente integrados en el proceso de compilación de Gradle (`gradlew assembleDebug`).
   - No sustituyas funciones nativas con implementaciones de reserva (*fallback*) en Kotlin si se especificó el uso de un framework nativo.

9. **Inspección Quirúrgica de Código**:
   - No abras ni inspecciones archivos de código que no sean estrictamente necesarios para resolver la tarea en curso.
