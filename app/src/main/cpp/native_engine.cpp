#include <jni.h>
#include <string>
#include <android/log.h>

// Inclusión del runtime oficial de Lua 5.4 en C
extern "C" {
#include "lua.h"
#include "lauxlib.h"
#include "lualib.h"
}

#define LOG_TAG "PolyStrike_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_stringFromCpp(
        JNIEnv* env,
        jobject /* this */) {
    std::string message = "C++ Core 1.0 (Direct NDK / Clang 14 / GLESv2 linked)";
    return env->NewStringUTF(message.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_nativebridge_NativeEngineBridge_evalLuaScript(
        JNIEnv* env,
        jobject /* this */,
        jstring scriptCode) {

    const char* script = env->GetStringUTFChars(scriptCode, nullptr);
    if (!script) {
        return env->NewStringUTF("Error: Null script string");
    }

    // Inicializar máquina virtual oficial de Lua 5.4
    lua_State* L = luaL_newstate();
    if (!L) {
        env->ReleaseStringUTFChars(scriptCode, script);
        return env->NewStringUTF("Error: Failed to create official Lua state");
    }

    // Cargar librerías estándar de Lua (base, math, string, table)
    luaL_openlibs(L);

    // Exponer información del entorno nativo en Lua
    lua_pushstring(L, "Android Native C++ Bridge");
    lua_setglobal(L, "PLATFORM_HOST");

    int status = luaL_dostring(L, script);
    std::string result;

    if (status != LUA_OK) {
        const char* err = lua_tostring(L, -1);
        result = std::string("Lua Error: ") + (err ? err : "Unknown error");
        LOGE("Lua execution failed: %s", result.c_str());
    } else {
        if (lua_gettop(L) > 0 && lua_isstring(L, -1)) {
            result = lua_tostring(L, -1);
        } else {
            result = "Lua execution OK (Return: nil / void)";
        }
    }

    lua_close(L);
    env->ReleaseStringUTFChars(scriptCode, script);

    return env->NewStringUTF(result.c_str());
}
