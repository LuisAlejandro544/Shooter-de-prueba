package com.example.game.engine

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.game.model.RenderSnapshot
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sin

class GameRenderer : GLSurfaceView.Renderer {

    @Volatile
    var snapshot: RenderSnapshot? = null

    @Volatile
    var currentFps: Int = 60
    private var frameCount = 0
    private var lastFpsTime = System.currentTimeMillis()

    // Matrices
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Shaders handles
    private var programId = 0
    private var uMVPMatrixHandle = 0
    private var uModelMatrixHandle = 0
    private var uColorHandle = 0
    private var uEmissiveHandle = 0
    private var uLightPosHandle = 0
    private var aPositionHandle = 0
    private var aNormalHandle = 0

    // Geometry
    private val cubeBuffer = GameShapes.cubeBuffer
    private val octahedronBuffer = GameShapes.octahedronBuffer
    private val bulletBuffer = GameShapes.bulletBuffer
    private lateinit var gridBuffer: FloatBuffer
    private var gridVertexCount = 0

    private var animationTick = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Fondo espacial / cyberpunk oscuro
        GLES20.glClearColor(0.04f, 0.07f, 0.12f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Habilitar mezcla para transparencias y glow
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Compilar y enlazar shaders
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        programId = GLES20.glCreateProgram().also { prog ->
            GLES20.glAttachShader(prog, vertexShader)
            GLES20.glAttachShader(prog, fragmentShader)
            GLES20.glLinkProgram(prog)
        }

        uMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        uModelMatrixHandle = GLES20.glGetUniformLocation(programId, "uModelMatrix")
        uColorHandle = GLES20.glGetUniformLocation(programId, "uColor")
        uEmissiveHandle = GLES20.glGetUniformLocation(programId, "uEmissive")
        uLightPosHandle = GLES20.glGetUniformLocation(programId, "uLightPos")
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
        aNormalHandle = GLES20.glGetAttribLocation(programId, "aNormal")

        val (gBuf, gCount) = GameShapes.createGridBuffer(size = 70f, step = 2.5f)
        gridBuffer = gBuf
        gridVertexCount = gCount
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / if (height > 0) height.toFloat() else 1f
        // Proyección de perspectiva 3D con FOV de 65 grados
        Matrix.perspectiveM(projectionMatrix, 0, 65f, aspect, 0.1f, 120f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Calcular FPS
        frameCount++
        val now = System.currentTimeMillis()
        if (now - lastFpsTime >= 1000) {
            currentFps = frameCount
            frameCount = 0
            lastFpsTime = now
        }

        animationTick += 0.025f

        val snap = snapshot
        val flash = snap?.muzzleFlashIntensity ?: 0f

        // Limpiar pantalla y buffer de profundidad (con destello sutil si hay disparo)
        val clearR = 0.04f + flash * 0.12f
        val clearG = 0.07f + flash * 0.10f
        val clearB = 0.12f + flash * 0.18f
        GLES20.glClearColor(clearR, clearG, clearB, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (snap == null) return

        GLES20.glUseProgram(programId)

        // Configurar luz dinámica
        GLES20.glUniform3f(uLightPosHandle, snap.playerX, snap.playerY + 8f, snap.playerZ)

        // Configurar cámara 3D (FPS)
        val lookDir = GameMath.getLookDirection(snap.yaw, snap.pitch)
        val camX = snap.playerX
        val camY = snap.playerY
        val camZ = snap.playerZ
        val targetX = camX + lookDir.x
        val targetY = camY + lookDir.y
        val targetZ = camZ + lookDir.z

        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, targetX, targetY, targetZ, 0f, 1f, 0f)

        // 1. Dibujar suelo de la arena (Grid cibernético)
        drawFloorGrid()

        // 2. Dibujar columnas / obstáculos perimetrales
        drawPillars(snap)

        // 3. Dibujar objetivos 3D (Drones / Dianas)
        drawDrones(snap)

        // 4. Dibujar proyectiles de disparo 3D (Láseres)
        drawBullets(snap)

        // 5. Dibujar partículas de chispas / explosiones
        drawParticles(snap)
    }

    private fun drawFloorGrid() {
        // Cuadrícula brillante
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLES20.glUniform4f(uColorHandle, 0.15f, 0.55f, 0.85f, 0.65f)
        GLES20.glUniform1f(uEmissiveHandle, 1.0f)

        gridBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 24, gridBuffer)

        gridBuffer.position(3)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 24, gridBuffer)

        GLES20.glLineWidth(2.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, gridVertexCount)

        // Piso sólido oscuro debajo de la cuadrícula
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, -0.05f, 0f)
        Matrix.scaleM(modelMatrix, 0, 70f, 0.1f, 70f)
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLES20.glUniform4f(uColorHandle, 0.06f, 0.09f, 0.15f, 1.0f)
        GLES20.glUniform1f(uEmissiveHandle, 0.0f)

        bindCubeBuffer()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    private fun drawPillars(snap: RenderSnapshot) {
        bindCubeBuffer()
        for (pillar in snap.pillars) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pillar.x, pillar.y, pillar.z)
            Matrix.scaleM(modelMatrix, 0, pillar.width, pillar.height, pillar.depth)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLES20.glUniform4f(uColorHandle, pillar.r, pillar.g, pillar.b, 1.0f)
            GLES20.glUniform1f(uEmissiveHandle, 0.0f)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)

            // Anillo luminoso superior en cada columna
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pillar.x, pillar.y + (pillar.height * 0.5f), pillar.z)
            Matrix.scaleM(modelMatrix, 0, pillar.width * 1.05f, 0.25f, pillar.depth * 1.05f)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.2f, 0.85f, 1.0f, 0.9f)
            GLES20.glUniform1f(uEmissiveHandle, 1.0f)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
        }
    }

    private fun drawDrones(snap: RenderSnapshot) {
        bindOctahedronBuffer()
        for (drone in snap.drones) {
            if (drone.isDestroyed) continue

            // El drone gira y oscila
            val rotY = (animationTick * 45f + drone.phase * 57.3f) % 360f

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, drone.x, drone.y, drone.z)
            Matrix.rotateM(modelMatrix, 0, rotY, 0f, 1f, 0f)
            Matrix.scaleM(modelMatrix, 0, drone.radius, drone.radius * 1.3f, drone.radius)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)

            if (drone.hitFlashTimer > 0f) {
                // Destello de impacto blanco/rojo
                GLES20.glUniform4f(uColorHandle, 1.0f, 0.25f, 0.25f, 1.0f)
                GLES20.glUniform1f(uEmissiveHandle, 1.0f)
            } else {
                GLES20.glUniform4f(uColorHandle, drone.colorR, drone.colorG, drone.colorB, 0.95f)
                GLES20.glUniform1f(uEmissiveHandle, 0.6f)
            }

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24)

            // Núcleo brillante interior
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, drone.x, drone.y, drone.z)
            Matrix.rotateM(modelMatrix, 0, -rotY * 1.5f, 1f, 0f, 1f)
            Matrix.scaleM(modelMatrix, 0, drone.radius * 0.45f, drone.radius * 0.45f, drone.radius * 0.45f)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 1.0f, 0.9f, 0.2f, 1.0f)
            GLES20.glUniform1f(uEmissiveHandle, 1.0f)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24)
        }
    }

    private fun drawBullets(snap: RenderSnapshot) {
        if (snap.bullets.isEmpty()) return
        bindBulletBuffer()

        for (bullet in snap.bullets) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, bullet.x, bullet.y, bullet.z)

            // Orientar hacia la dirección del disparo
            val yaw = Math.toDegrees(kotlin.math.atan2(bullet.dirX.toDouble(), -bullet.dirZ.toDouble())).toFloat()
            val pitch = Math.toDegrees(kotlin.math.asin(bullet.dirY.toDouble())).toFloat()

            Matrix.rotateM(modelMatrix, 0, yaw, 0f, 1f, 0f)
            Matrix.rotateM(modelMatrix, 0, -pitch, 1f, 0f, 0f)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.1f, 1.0f, 0.95f, 1.0f)
            GLES20.glUniform1f(uEmissiveHandle, 1.0f)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 12)
        }
    }

    private fun drawParticles(snap: RenderSnapshot) {
        if (snap.particles.isEmpty()) return
        bindCubeBuffer()

        for (p in snap.particles) {
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, p.x, p.y, p.z)
            Matrix.scaleM(modelMatrix, 0, p.size, p.size, p.size)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLES20.glUniform4f(uColorHandle, p.r, p.g, p.b, alpha)
            GLES20.glUniform1f(uEmissiveHandle, 1.0f)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
        }
    }

    private fun bindCubeBuffer() {
        cubeBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 24, cubeBuffer)

        cubeBuffer.position(3)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 24, cubeBuffer)
    }

    private fun bindOctahedronBuffer() {
        octahedronBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 24, octahedronBuffer)

        octahedronBuffer.position(3)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 24, octahedronBuffer)
    }

    private fun bindBulletBuffer() {
        bulletBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 24, bulletBuffer)

        bulletBuffer.position(3)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 24, bulletBuffer)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    companion object {
        private const val VERTEX_SHADER_CODE = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uModelMatrix;
            attribute vec4 aPosition;
            attribute vec3 aNormal;
            varying vec3 vNormal;
            varying vec3 vFragPos;

            void main() {
                vFragPos = vec3(uModelMatrix * aPosition);
                vNormal = mat3(uModelMatrix) * aNormal;
                gl_Position = uMVPMatrix * aPosition;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            uniform vec4 uColor;
            uniform float uEmissive;
            uniform vec3 uLightPos;
            varying vec3 vNormal;
            varying vec3 vFragPos;

            void main() {
                if (uEmissive > 0.5) {
                    gl_FragColor = uColor;
                } else {
                    vec3 norm = normalize(vNormal);
                    vec3 lightDir = normalize(uLightPos - vFragPos);
                    float diff = max(dot(norm, lightDir), 0.0);
                    vec3 diffuse = diff * vec3(1.0, 0.95, 0.9);
                    vec3 ambient = vec3(0.25, 0.28, 0.35);
                    vec3 result = (ambient + diffuse) * uColor.rgb;
                    gl_FragColor = vec4(result, uColor.a);
                }
            }
        """
    }
}
