package com.example.nativebridge

import android.util.Log

/**
 * Puente nativo JNI hacia los motores en C++, Rust y Lua original
 */
object NativeEngineBridge {
    private const val TAG = "NativeEngineBridge"
    
    var isCppLoaded: Boolean = false
        private set

    var isRustLoaded: Boolean = false
        private set

    init {
        // Carga de la librería C++ con Lua integrado
        try {
            System.loadLibrary("polystrike_engine")
            isCppLoaded = true
            Log.i(TAG, "Librería nativa C++ (polystrike_engine) cargada correctamente")
        } catch (t: Throwable) {
            Log.e(TAG, "No se pudo cargar la librería C++: ${t.message}")
        }

        // Carga de la librería Rust
        try {
            System.loadLibrary("polystrike_core")
            isRustLoaded = true
            Log.i(TAG, "Librería nativa Rust (polystrike_core) cargada correctamente")
        } catch (t: Throwable) {
            Log.e(TAG, "No se pudo cargar la librería Rust: ${t.message}")
        }
    }

    // Funciones nativas de C++
    external fun stringFromCpp(): String

    // Función nativa para ejecutar código con el intérprete original de Lua 5.4 en C++
    external fun evalLuaScript(scriptCode: String): String

    // Función nativa de Rust
    external fun stringFromRust(): String

    /**
     * Devuelve el estado de integración de las librerías nativas
     */
    fun getDiagnosticInfo(): NativeStatus {
        val cppMsg = if (isCppLoaded) {
            try { stringFromCpp() } catch (e: Throwable) { "Error al invocar: ${e.message}" }
        } else {
            "No cargada"
        }

        val rustMsg = if (isRustLoaded) {
            try { stringFromRust() } catch (e: Throwable) { "Error al invocar: ${e.message}" }
        } else {
            "No cargada"
        }

        val luaMsg = if (isCppLoaded) {
            try {
                evalLuaScript("return 'Lua ' .. _VERSION .. ' Original ejecutado en C++ (Host: ' .. PLATFORM_HOST .. ')'")
            } catch (e: Throwable) {
                "Error al invocar Lua: ${e.message}"
            }
        } else {
            "No disponible (depende de C++)"
        }

        return NativeStatus(
            isCppAvailable = isCppLoaded,
            cppMessage = cppMsg,
            isRustAvailable = isRustLoaded,
            rustMessage = rustMsg,
            isLuaAvailable = isCppLoaded,
            luaMessage = luaMsg
        )
    }
}

data class NativeStatus(
    val isCppAvailable: Boolean,
    val cppMessage: String,
    val isRustAvailable: Boolean,
    val rustMessage: String,
    val isLuaAvailable: Boolean,
    val luaMessage: String
)
