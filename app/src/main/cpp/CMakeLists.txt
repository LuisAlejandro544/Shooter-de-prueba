cmake_minimum_required(VERSION 3.22.1)

project("polystrike_native" LANGUAGES C CXX)

# Compilar Lua original como librería estática
file(GLOB LUA_SOURCES "lua/*.c")

add_library(lua STATIC ${LUA_SOURCES})
target_include_directories(lua PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/lua)
target_compile_definitions(lua PRIVATE LUA_COMPAT_5_3)

# C++ Native Engine y JNI Bridge
add_library(polystrike_engine SHARED
    native_engine.cpp
)

target_include_directories(polystrike_engine PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/lua
)

# Enlazar con Lua y librerías nativas de Android (GLESv2, Log, Math)
target_link_libraries(polystrike_engine
    PRIVATE
    lua
    GLESv2
    log
    m
)
