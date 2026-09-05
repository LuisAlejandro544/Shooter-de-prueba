package com.example.game.engine

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

object GameShapes {

    /**
     * Vértices y normales de un cubo unitario centrado (tamaño 1x1x1)
     * 36 vértices (6 caras * 2 triángulos * 3 vértices)
     * Cada vértice: pos(x,y,z), normal(nx,ny,nz) -> 6 floats por vértice
     */
    val cubeBuffer: FloatBuffer by lazy {
        val data = floatArrayOf(
            // Cara Frontal (Z = +0.5) - normal (0, 0, 1)
            -0.5f, -0.5f,  0.5f,  0f, 0f, 1f,
             0.5f, -0.5f,  0.5f,  0f, 0f, 1f,
             0.5f,  0.5f,  0.5f,  0f, 0f, 1f,
            -0.5f, -0.5f,  0.5f,  0f, 0f, 1f,
             0.5f,  0.5f,  0.5f,  0f, 0f, 1f,
            -0.5f,  0.5f,  0.5f,  0f, 0f, 1f,

            // Cara Trasera (Z = -0.5) - normal (0, 0, -1)
            -0.5f, -0.5f, -0.5f,  0f, 0f, -1f,
             0.5f,  0.5f, -0.5f,  0f, 0f, -1f,
             0.5f, -0.5f, -0.5f,  0f, 0f, -1f,
            -0.5f, -0.5f, -0.5f,  0f, 0f, -1f,
            -0.5f,  0.5f, -0.5f,  0f, 0f, -1f,
             0.5f,  0.5f, -0.5f,  0f, 0f, -1f,

            // Cara Izquierda (X = -0.5) - normal (-1, 0, 0)
            -0.5f, -0.5f, -0.5f, -1f, 0f, 0f,
            -0.5f, -0.5f,  0.5f, -1f, 0f, 0f,
            -0.5f,  0.5f,  0.5f, -1f, 0f, 0f,
            -0.5f, -0.5f, -0.5f, -1f, 0f, 0f,
            -0.5f,  0.5f,  0.5f, -1f, 0f, 0f,
            -0.5f,  0.5f, -0.5f, -1f, 0f, 0f,

            // Cara Derecha (X = +0.5) - normal (1, 0, 0)
             0.5f, -0.5f, -0.5f,  1f, 0f, 0f,
             0.5f,  0.5f,  0.5f,  1f, 0f, 0f,
             0.5f, -0.5f,  0.5f,  1f, 0f, 0f,
             0.5f, -0.5f, -0.5f,  1f, 0f, 0f,
             0.5f,  0.5f, -0.5f,  1f, 0f, 0f,
             0.5f,  0.5f,  0.5f,  1f, 0f, 0f,

            // Cara Superior (Y = +0.5) - normal (0, 1, 0)
            -0.5f,  0.5f, -0.5f,  0f, 1f, 0f,
            -0.5f,  0.5f,  0.5f,  0f, 1f, 0f,
             0.5f,  0.5f,  0.5f,  0f, 1f, 0f,
            -0.5f,  0.5f, -0.5f,  0f, 1f, 0f,
             0.5f,  0.5f,  0.5f,  0f, 1f, 0f,
             0.5f,  0.5f, -0.5f,  0f, 1f, 0f,

            // Cara Inferior (Y = -0.5) - normal (0, -1, 0)
            -0.5f, -0.5f, -0.5f,  0f, -1f, 0f,
             0.5f, -0.5f,  0.5f,  0f, -1f, 0f,
            -0.5f, -0.5f,  0.5f,  0f, -1f, 0f,
            -0.5f, -0.5f, -0.5f,  0f, -1f, 0f,
             0.5f, -0.5f, -0.5f,  0f, -1f, 0f,
             0.5f, -0.5f,  0.5f,  0f, -1f, 0f
        )
        createFloatBuffer(data)
    }

    /**
     * Vértices y normales para un Octaedro 3D (Drone flotante sci-fi / diana)
     * 8 triángulos * 3 vértices = 24 vértices
     */
    val octahedronBuffer: FloatBuffer by lazy {
        val top = floatArrayOf(0f, 1f, 0f)
        val bottom = floatArrayOf(0f, -1f, 0f)
        val pFront = floatArrayOf(0f, 0f, 1f)
        val pRight = floatArrayOf(1f, 0f, 0f)
        val pBack = floatArrayOf(0f, 0f, -1f)
        val pLeft = floatArrayOf(-1f, 0f, 0f)

        fun calcNormal(p1: FloatArray, p2: FloatArray, p3: FloatArray): FloatArray {
            val u1 = p2[0] - p1[0]
            val u2 = p2[1] - p1[1]
            val u3 = p2[2] - p1[2]
            val v1 = p3[0] - p1[0]
            val v2 = p3[1] - p1[1]
            val v3 = p3[2] - p1[2]
            var nx = u2 * v3 - u3 * v2
            var ny = u3 * v1 - u1 * v3
            var nz = u1 * v2 - u2 * v1
            val len = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
            if (len > 0f) {
                nx /= len
                ny /= len
                nz /= len
            }
            return floatArrayOf(nx, ny, nz)
        }

        val triangles = listOf(
            // Pirámide superior
            listOf(top, pFront, pRight),
            listOf(top, pRight, pBack),
            listOf(top, pBack, pLeft),
            listOf(top, pLeft, pFront),
            // Pirámide inferior
            listOf(bottom, pRight, pFront),
            listOf(bottom, pBack, pRight),
            listOf(bottom, pLeft, pBack),
            listOf(bottom, pFront, pLeft)
        )

        val data = FloatArray(8 * 3 * 6)
        var idx = 0
        for (tri in triangles) {
            val n = calcNormal(tri[0], tri[1], tri[2])
            for (pt in tri) {
                data[idx++] = pt[0]
                data[idx++] = pt[1]
                data[idx++] = pt[2]
                data[idx++] = n[0]
                data[idx++] = n[1]
                data[idx++] = n[2]
            }
        }
        createFloatBuffer(data)
    }

    /**
     * Vértices para la cuadrícula del suelo de la arena (líneas neon)
     */
    fun createGridBuffer(size: Float = 60f, step: Float = 2.5f): Pair<FloatBuffer, Int> {
        val lines = mutableListOf<Float>()
        var coord = -size / 2f
        val half = size / 2f

        while (coord <= half) {
            // Línea paralela al eje Z
            lines.add(coord); lines.add(0f); lines.add(-half)
            lines.add(0f); lines.add(1f); lines.add(0f)

            lines.add(coord); lines.add(0f); lines.add(half)
            lines.add(0f); lines.add(1f); lines.add(0f)

            // Línea paralela al eje X
            lines.add(-half); lines.add(0f); lines.add(coord)
            lines.add(0f); lines.add(1f); lines.add(0f)

            lines.add(half); lines.add(0f); lines.add(coord)
            lines.add(0f); lines.add(1f); lines.add(0f)

            coord += step
        }

        val arr = lines.toFloatArray()
        return Pair(createFloatBuffer(arr), arr.size / 6)
    }

    /**
     * Vértice para proyectil cilíndrico / trazo láser
     */
    val bulletBuffer: FloatBuffer by lazy {
        val data = floatArrayOf(
            // Cilindro o prisma alargado de disparo láser (longitud 1.2, grosor 0.08)
            0f, 0f, 0.6f,  0f, 0f, 1f,
            0.04f, 0.04f, -0.6f, 1f, 1f, 0f,
            -0.04f, 0.04f, -0.6f, -1f, 1f, 0f,

            0f, 0f, 0.6f,  0f, 0f, 1f,
            -0.04f, 0.04f, -0.6f, -1f, 1f, 0f,
            -0.04f, -0.04f, -0.6f, -1f, -1f, 0f,

            0f, 0f, 0.6f,  0f, 0f, 1f,
            -0.04f, -0.04f, -0.6f, -1f, -1f, 0f,
            0.04f, -0.04f, -0.6f, 1f, -1f, 0f,

            0f, 0f, 0.6f,  0f, 0f, 1f,
            0.04f, -0.04f, -0.6f, 1f, -1f, 0f,
            0.04f, 0.04f, -0.6f, 1f, 1f, 0f
        )
        createFloatBuffer(data)
    }

    private fun createFloatBuffer(array: FloatArray): FloatBuffer {
        return ByteBuffer.allocateDirect(array.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(array)
                position(0)
            }
        }
    }
}
