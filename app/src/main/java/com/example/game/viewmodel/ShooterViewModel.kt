package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.engine.GameMath
import com.example.game.engine.GameRenderer
import com.example.game.engine.Ray3D
import com.example.game.engine.SoundSystem
import com.example.game.engine.Vector3
import com.example.game.model.Bullet
import com.example.game.model.Particle
import com.example.game.model.PillarObstacle
import com.example.game.model.RenderSnapshot
import com.example.game.model.TargetDrone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

data class UiHudState(
    val health: Int = 100,
    val ammo: Int = 30,
    val maxAmmo: Int = 30,
    val score: Int = 0,
    val targetsDestroyed: Int = 0,
    val accuracy: Int = 100,
    val isReloading: Boolean = false,
    val reloadProgress: Float = 0f,
    val hitMarker: Boolean = false,
    val fps: Int = 60,
    val wave: Int = 1,
    val playerX: Float = 0f,
    val playerY: Float = 1.6f,
    val playerZ: Float = 0f,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
    val activeBullets: Int = 0,
    val activeParticles: Int = 0
)

class ShooterViewModel : ViewModel() {

    val renderer = GameRenderer()
    val soundSystem = SoundSystem()

    private val _hudState = MutableStateFlow(UiHudState())
    val hudState: StateFlow<UiHudState> = _hudState.asStateFlow()

    // Estado del Jugador
    private var posX = 0f
    private var posY = 1.6f // Altura de los ojos
    private var posZ = 8f
    private var velocityY = 0f
    private var isGrounded = true
    private var yaw = 0f
    private var pitch = 0f

    // Controles
    private var moveStickX = 0f
    private var moveStickY = 0f
    private var aimSensitivity = 0.16f

    // Armas y Combate
    private var ammo = 30
    private val maxAmmo = 30
    private var isReloading = false
    private var reloadTimer = 0f
    private val reloadDuration = 1.4f
    private var lastShotTime = 0L
    private val shotCooldownMs = 150L
    private var muzzleFlash = 0f
    private var hitMarkerTimer = 0f

    // Estadísticas
    private var score = 0
    private var targetsDestroyed = 0
    private var totalShotsFired = 0
    private var totalHits = 0
    private var wave = 1

    // Entidades del mundo 3D
    private val drones = mutableListOf<TargetDrone>()
    private val bullets = mutableListOf<Bullet>()
    private val particles = mutableListOf<Particle>()
    private val pillars = mutableListOf<PillarObstacle>()

    private var nextBulletId = 1L
    private var gameTime = 0f

    init {
        setupWorld()
        startGameLoop()
    }

    private fun setupWorld() {
        // Pilares y columnas arquitectónicas para dar referencia de profundidad 3D
        pillars.clear()
        pillars.add(PillarObstacle(x = -8f, y = 3.5f, z = -12f, width = 2f, height = 7f, depth = 2f, r = 0.12f, g = 0.20f, b = 0.35f))
        pillars.add(PillarObstacle(x = 8f, y = 3.5f, z = -12f, width = 2f, height = 7f, depth = 2f, r = 0.12f, g = 0.20f, b = 0.35f))
        pillars.add(PillarObstacle(x = -14f, y = 3.0f, z = 0f, width = 2.5f, height = 6f, depth = 2.5f, r = 0.10f, g = 0.18f, b = 0.30f))
        pillars.add(PillarObstacle(x = 14f, y = 3.0f, z = 0f, width = 2.5f, height = 6f, depth = 2.5f, r = 0.10f, g = 0.18f, b = 0.30f))
        pillars.add(PillarObstacle(x = 0f, y = 2.5f, z = -22f, width = 4f, height = 5f, depth = 1.5f, r = 0.15f, g = 0.22f, b = 0.38f))

        // Spawn inicial de drones en 3D
        spawnDrones()
    }

    fun spawnDrones() {
        drones.clear()
        val initialDrones = listOf(
            TargetDrone(id = 1, x = -6f, y = 2.2f, z = -8f, baseY = 2.2f, phase = 0f, colorR = 0.0f, colorG = 0.85f, colorB = 1.0f, moveSpeed = 1.2f, moveAmplitude = 3.5f),
            TargetDrone(id = 2, x = 0f, y = 2.8f, z = -14f, baseY = 2.8f, phase = 1.5f, colorR = 1.0f, colorG = 0.45f, colorB = 0.05f, moveSpeed = 0.9f, moveAmplitude = 4.5f),
            TargetDrone(id = 3, x = 7f, y = 2.0f, z = -9f, baseY = 2.0f, phase = 3.0f, colorR = 0.2f, colorG = 1.0f, colorB = 0.4f, moveSpeed = 1.4f, moveAmplitude = 3.0f),
            TargetDrone(id = 4, x = -10f, y = 3.5f, z = -18f, baseY = 3.5f, phase = 4.2f, colorR = 0.9f, colorG = 0.2f, colorB = 0.9f, moveSpeed = 1.1f, moveAmplitude = 5.0f),
            TargetDrone(id = 5, x = 11f, y = 3.2f, z = -16f, baseY = 3.2f, phase = 2.1f, colorR = 1.0f, colorG = 0.85f, colorB = 0.1f, moveSpeed = 1.3f, moveAmplitude = 4.0f)
        )
        drones.addAll(initialDrones)
    }

    private fun startGameLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()

            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
                lastTime = now

                updateGame(dt)

                // Publicar snapshot al renderer de OpenGL
                renderer.snapshot = RenderSnapshot(
                    playerX = posX,
                    playerY = posY,
                    playerZ = posZ,
                    yaw = yaw,
                    pitch = pitch,
                    drones = drones.toList(),
                    bullets = bullets.toList(),
                    particles = particles.toList(),
                    pillars = pillars.toList(),
                    muzzleFlashIntensity = muzzleFlash
                )

                // Actualizar estado del HUD
                val acc = if (totalShotsFired > 0) ((totalHits.toFloat() / totalShotsFired) * 100).toInt() else 100
                _hudState.value = UiHudState(
                    health = 100,
                    ammo = ammo,
                    maxAmmo = maxAmmo,
                    score = score,
                    targetsDestroyed = targetsDestroyed,
                    accuracy = acc,
                    isReloading = isReloading,
                    reloadProgress = if (isReloading) (reloadTimer / reloadDuration).coerceIn(0f, 1f) else 0f,
                    hitMarker = hitMarkerTimer > 0f,
                    fps = renderer.currentFps,
                    wave = wave,
                    playerX = posX,
                    playerY = posY,
                    playerZ = posZ,
                    yaw = yaw,
                    pitch = pitch,
                    activeBullets = bullets.size,
                    activeParticles = particles.size
                )

                delay(16) // ~60 FPS update rate
            }
        }
    }

    private fun updateGame(dt: Float) {
        gameTime += dt

        // 1. Decaimientos de temporizadores
        if (muzzleFlash > 0f) muzzleFlash = (muzzleFlash - dt * 8f).coerceAtLeast(0f)
        if (hitMarkerTimer > 0f) hitMarkerTimer = (hitMarkerTimer - dt * 6f).coerceAtLeast(0f)

        // 2. Proceso de recarga
        if (isReloading) {
            reloadTimer += dt
            if (reloadTimer >= reloadDuration) {
                ammo = maxAmmo
                isReloading = false
                reloadTimer = 0f
            }
        }

        // 3. Movimiento del jugador mediante Joystick analógico
        val speed = 9.5f
        if (moveStickX != 0f || moveStickY != 0f) {
            val forward = GameMath.getForwardDirection(yaw)
            val right = GameMath.getRightDirection(yaw)

            val dx = (right.x * moveStickX + forward.x * (-moveStickY)) * speed * dt
            val dz = (right.z * moveStickX + forward.z * (-moveStickY)) * speed * dt

            var newX = posX + dx
            var newZ = posZ + dz

            // Limitar dentro de la arena (límites 3D)
            newX = newX.coerceIn(-24f, 24f)
            newZ = newZ.coerceIn(-24f, 24f)

            // Colisión básica con pilares
            var collides = false
            for (p in pillars) {
                val halfW = p.width * 0.5f + 0.6f
                val halfD = p.depth * 0.5f + 0.6f
                if (newX in (p.x - halfW)..(p.x + halfW) && newZ in (p.z - halfD)..(p.z + halfD)) {
                    collides = true
                    break
                }
            }

            if (!collides) {
                posX = newX
                posZ = newZ
            }
        }

        // 4. Física vertical de salto
        if (!isGrounded) {
            velocityY -= 22f * dt // Gravedad
            posY += velocityY * dt
            if (posY <= 1.6f) {
                posY = 1.6f
                velocityY = 0f
                isGrounded = true
            }
        }

        // 5. Actualizar Drones / Objetivos
        for (drone in drones) {
            if (drone.isDestroyed) continue

            // Oscilación vertical y movimiento horizontal
            drone.y = drone.baseY + sin(gameTime * 2.2f + drone.phase) * 0.45f
            drone.x = drone.initialX + sin(gameTime * drone.moveSpeed + drone.phase) * drone.moveAmplitude

            if (drone.hitFlashTimer > 0f) {
                drone.hitFlashTimer = (drone.hitFlashTimer - dt * 5f).coerceAtLeast(0f)
            }
        }

        // 6. Actualizar Proyectiles y Detección de Colisión 3D
        val bulletsIterator = bullets.iterator()
        while (bulletsIterator.hasNext()) {
            val b = bulletsIterator.next()
            val stepDist = b.speed * dt
            b.x += b.dirX * stepDist
            b.y += b.dirY * stepDist
            b.z += b.dirZ * stepDist
            b.distanceTraveled += stepDist

            var bulletRemoved = false

            // Comprobar colisión con cada objetivo activo
            for (drone in drones) {
                if (drone.isDestroyed) continue

                val dist = GameMath.distance(b.x, b.y, b.z, drone.x, drone.y, drone.z)
                if (dist <= drone.radius + 0.35f) {
                    // ¡Impacto detectado!
                    drone.health -= 50f
                    drone.hitFlashTimer = 0.25f
                    totalHits++
                    hitMarkerTimer = 0.3f
                    soundSystem.playHit()

                    // Generar chispas de impacto
                    spawnSparks(b.x, b.y, b.z, 8, drone.colorR, drone.colorG, drone.colorB)

                    bulletRemoved = true

                    if (drone.health <= 0f) {
                        drone.isDestroyed = true
                        targetsDestroyed++
                        score += 150
                        soundSystem.playExplosion()
                        spawnExplosion(drone.x, drone.y, drone.z, 24, drone.colorR, drone.colorG, drone.colorB)

                        // Respawn automático tras 2 segundos
                        viewModelScope.launch(Dispatchers.Default) {
                            delay(2200)
                            drone.health = drone.maxHealth
                            drone.x = (Random.nextFloat() * 18f - 9f)
                            drone.initialX
                            drone.z = -(Random.nextFloat() * 12f + 8f)
                            drone.isDestroyed = false
                        }
                    }
                    break
                }
            }

            // Comprobar colisión con el suelo o fuera de alcance
            if (!bulletRemoved) {
                if (b.y <= 0f || b.distanceTraveled >= b.maxDistance) {
                    if (b.y <= 0f) {
                        spawnSparks(b.x, 0.05f, b.z, 4, 0.2f, 0.8f, 1f)
                    }
                    bulletRemoved = true
                }
            }

            if (bulletRemoved) {
                bulletsIterator.remove()
            }
        }

        // 7. Actualizar Partículas
        val particlesIterator = particles.iterator()
        while (particlesIterator.hasNext()) {
            val p = particlesIterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.z += p.vz * dt
            p.life -= dt
            if (p.life <= 0f || p.y < 0f) {
                particlesIterator.remove()
            }
        }
    }

    private fun spawnSparks(x: Float, y: Float, z: Float, count: Int, r: Float, g: Float, b: Float) {
        for (i in 0 until count) {
            val vx = (Random.nextFloat() - 0.5f) * 7f
            val vy = (Random.nextFloat() * 4f + 1f)
            val vz = (Random.nextFloat() - 0.5f) * 7f
            particles.add(Particle(x = x, y = y, z = z, vx = vx, vy = vy, vz = vz, life = 0.35f, maxLife = 0.35f, r = r, g = g, b = b, size = 0.08f))
        }
    }

    private fun spawnExplosion(x: Float, y: Float, z: Float, count: Int, r: Float, g: Float, b: Float) {
        for (i in 0 until count) {
            val vx = (Random.nextFloat() - 0.5f) * 12f
            val vy = (Random.nextFloat() - 0.5f) * 12f
            val vz = (Random.nextFloat() - 0.5f) * 12f
            particles.add(Particle(x = x, y = y, z = z, vx = vx, vy = vy, vz = vz, life = 0.5f, maxLife = 0.5f, r = r, g = g, b = b, size = 0.15f))
        }
    }

    // Acciones de entrada del usuario
    fun onJoystickMoved(x: Float, y: Float) {
        moveStickX = x
        moveStickY = y
    }

    fun onAimDrag(deltaX: Float, deltaY: Float) {
        yaw = (yaw + deltaX * aimSensitivity) % 360f
        if (yaw < 0f) yaw += 360f

        pitch = (pitch - deltaY * aimSensitivity).coerceIn(-70f, 70f)
    }

    fun fire() {
        val now = System.currentTimeMillis()
        if (now - lastShotTime < shotCooldownMs) return
        lastShotTime = now

        if (isReloading) return

        if (ammo <= 0) {
            soundSystem.playEmpty()
            reload()
            return
        }

        ammo--
        totalShotsFired++
        muzzleFlash = 1.0f
        soundSystem.playShoot()

        // Pequeño retroceso en el pitch
        pitch = (pitch + 0.85f).coerceIn(-70f, 70f)

        // Dirección del disparo
        val lookDir = GameMath.getLookDirection(yaw, pitch)

        // Posición de salida del arma (ligeramente a la derecha y abajo de la vista)
        val right = GameMath.getRightDirection(yaw)
        val spawnX = posX + lookDir.x * 0.6f + right.x * 0.22f
        val spawnY = posY + lookDir.y * 0.6f - 0.18f
        val spawnZ = posZ + lookDir.z * 0.6f + right.z * 0.22f

        bullets.add(
            Bullet(
                id = nextBulletId++,
                x = spawnX,
                y = spawnY,
                z = spawnZ,
                dirX = lookDir.x,
                dirY = lookDir.y,
                dirZ = lookDir.z
            )
        )
    }

    fun reload() {
        if (isReloading || ammo >= maxAmmo) return
        isReloading = true
        reloadTimer = 0f
        soundSystem.playReload()
    }

    fun jump() {
        if (isGrounded) {
            velocityY = 6.2f
            isGrounded = false
        }
    }

    fun resetGame() {
        posX = 0f
        posY = 1.6f
        posZ = 8f
        yaw = 0f
        pitch = 0f
        ammo = maxAmmo
        score = 0
        targetsDestroyed = 0
        totalShotsFired = 0
        totalHits = 0
        isReloading = false
        bullets.clear()
        particles.clear()
        spawnDrones()
    }

    fun addValidationTarget() {
        val newId = drones.size + 1
        drones.add(
            TargetDrone(
                id = newId,
                x = (Random.nextFloat() * 16f - 8f),
                y = 2.5f,
                z = -(Random.nextFloat() * 10f + 6f),
                baseY = 2.5f,
                phase = Random.nextFloat() * 6.28f,
                colorR = 1.0f,
                colorG = 0.2f,
                colorB = 0.3f,
                moveSpeed = 1.5f,
                moveAmplitude = 4f
            )
        )
    }

    fun setAimSensitivity(newSensitivity: Float) {
        aimSensitivity = newSensitivity
    }
}
