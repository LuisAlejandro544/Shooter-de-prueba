package com.example.game.engine

import kotlin.math.*

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)
    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.0001f) Vector3(x / len, y / len, z / len) else Vector3(0f, 0f, 0f)
    }
}

data class Ray3D(
    val origin: Vector3,
    val direction: Vector3
)

object GameMath {
    /**
     * Calcula el vector unitario de dirección a partir de los ángulos Yaw (rotación horizontal) y Pitch (vertical)
     * Yaw en grados (0 = mira a -Z), Pitch en grados (-89 a +89)
     */
    fun getLookDirection(yawDegrees: Float, pitchDegrees: Float): Vector3 {
        val yawRad = Math.toRadians(yawDegrees.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitchDegrees.toDouble()).toFloat()

        val cosPitch = cos(pitchRad)
        val x = sin(yawRad) * cosPitch
        val y = sin(pitchRad)
        val z = -cos(yawRad) * cosPitch

        return Vector3(x, y, z).normalized()
    }

    /**
     * Calcula el vector de dirección lateral (Strafe / Right) perpendicular al vector de vista
     */
    fun getRightDirection(yawDegrees: Float): Vector3 {
        val yawRad = Math.toRadians(yawDegrees.toDouble()).toFloat()
        val x = cos(yawRad)
        val z = sin(yawRad)
        return Vector3(x, 0f, z).normalized()
    }

    /**
     * Calcula el vector de avance horizontal (Forward en el plano XZ)
     */
    fun getForwardDirection(yawDegrees: Float): Vector3 {
        val yawRad = Math.toRadians(yawDegrees.toDouble()).toFloat()
        val x = sin(yawRad)
        val z = -cos(yawRad)
        return Vector3(x, 0f, z).normalized()
    }

    /**
     * Intersección Rayo - Esfera para detectar impactos directos de disparo
     * Retorna la distancia al punto de impacto si colisiona, o null si no hay impacto
     */
    fun intersectRaySphere(
        ray: Ray3D,
        sphereCenter: Vector3,
        sphereRadius: Float
    ): Float? {
        val oc = ray.origin - sphereCenter
        val a = ray.direction.x * ray.direction.x + ray.direction.y * ray.direction.y + ray.direction.z * ray.direction.z
        val b = 2.0f * (oc.x * ray.direction.x + oc.y * ray.direction.y + oc.z * ray.direction.z)
        val c = (oc.x * oc.x + oc.y * oc.y + oc.z * oc.z) - (sphereRadius * sphereRadius)

        val discriminant = b * b - 4.0f * a * c
        if (discriminant < 0) return null

        val sqrtDisc = sqrt(discriminant)
        val t1 = (-b - sqrtDisc) / (2.0f * a)
        val t2 = (-b + sqrtDisc) / (2.0f * a)

        return when {
            t1 > 0.05f -> t1
            t2 > 0.05f -> t2
            else -> null
        }
    }

    /**
     * Distancia euclidiana entre dos puntos 3D
     */
    fun distance(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}
