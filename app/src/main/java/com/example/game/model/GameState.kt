package com.example.game.model

/**
 * Entidad de objetivo 3D (Drone flotante / Diana holográfica)
 */
data class TargetDrone(
    val id: Int,
    var x: Float,
    var y: Float,
    var z: Float,
    val baseY: Float,
    val phase: Float,
    var health: Float = 100f,
    val maxHealth: Float = 100f,
    var hitFlashTimer: Float = 0f,
    val colorR: Float,
    val colorG: Float,
    val colorB: Float,
    val radius: Float = 0.85f,
    var isDestroyed: Boolean = false,
    val moveSpeed: Float = 1.0f,
    val moveAmplitude: Float = 3.0f,
    val initialX: Float = x
)

/**
 * Proyectil láser 3D
 */
data class Bullet(
    val id: Long,
    var x: Float,
    var y: Float,
    var z: Float,
    val dirX: Float,
    val dirY: Float,
    val dirZ: Float,
    val speed: Float = 55f,
    var distanceTraveled: Float = 0f,
    val maxDistance: Float = 70f
)

/**
 * Partícula de chispa / explosión 3D
 */
data class Particle(
    var x: Float,
    var y: Float,
    var z: Float,
    val vx: Float,
    val vy: Float,
    val vz: Float,
    var life: Float = 1.0f,
    val maxLife: Float = 0.4f,
    val r: Float,
    val g: Float,
    val b: Float,
    val size: Float = 0.12f
)

/**
 * Obstáculo / Columna 3D en la arena
 */
data class PillarObstacle(
    val x: Float,
    val y: Float,
    val z: Float,
    val width: Float,
    val height: Float,
    val depth: Float,
    val r: Float,
    val g: Float,
    val b: Float
)

/**
 * Snapshot del estado de renderizado para desacoplar el bucle de juego del renderizador OpenGL
 */
data class RenderSnapshot(
    val playerX: Float,
    val playerY: Float,
    val playerZ: Float,
    val yaw: Float,
    val pitch: Float,
    val drones: List<TargetDrone>,
    val bullets: List<Bullet>,
    val particles: List<Particle>,
    val pillars: List<PillarObstacle>,
    val muzzleFlashIntensity: Float
)
