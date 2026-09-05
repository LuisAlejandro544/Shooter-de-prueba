package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.viewmodel.UiHudState

@Composable
fun ValidationSheet(
    hudState: UiHudState,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onSpawnTarget: () -> Unit,
    onSensitivityChange: (Float) -> Unit,
    currentSensitivity: Float
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .testTag("validation_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0D1424),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Panel de Validación Shooter 3D",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_validation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0x3300E5FF),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Contenido desplazable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Métricas en tiempo real
                    Text(
                        text = "TELEMETRÍA EN TIEMPO REAL",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryCard(
                            label = "FPS Render",
                            value = "${hudState.fps}",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCard(
                            label = "Posición (X,Y,Z)",
                            value = String.format("%.1f, %.1f, %.1f", hudState.playerX, hudState.playerY, hudState.playerZ),
                            modifier = Modifier.weight(1.5f)
                        )
                        TelemetryCard(
                            label = "Ángulo (Yaw/Pitch)",
                            value = String.format("%.0f° / %.0f°", hudState.yaw, hudState.pitch),
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryCard(
                            label = "Balas en Vuelo",
                            value = "${hudState.activeBullets}",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCard(
                            label = "Partículas",
                            value = "${hudState.activeParticles}",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCard(
                            label = "Precisión",
                            value = "${hudState.accuracy}%",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryCard(
                            label = "Dianas Bajas",
                            value = "${hudState.targetsDestroyed}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Checklist de Validación
                    Text(
                        text = "LISTA DE VALIDACIÓN DEL JUEGO",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    ValidationCheckItem("Renderizado 3D OpenGL ES (Perspectiva, iluminación difusa, buffers)")
                    ValidationCheckItem("Formato Horizontal (Landscape) nativo adaptado a pantalla de teléfono")
                    ValidationCheckItem("Control Dual Táctil: Joystick analógico (movimiento) + Swipe (mira)")
                    ValidationCheckItem("Balística 3D: Láseres, cálculo de colisión e impacto en dianas")
                    ValidationCheckItem("Objetivos Dinámicos: Drones flotantes con oscilación y barra de vida")
                    ValidationCheckItem("Efectos y Audio: Chispas 3D, hit marker y sonido sintetizado en tiempo real")
                    ValidationCheckItem("Núcleo C++ Nativo (NDK / Clang 14 / GLESv2 JNI)")
                    ValidationCheckItem("Núcleo Rust Nativo (libpolystrike_core.so / JNI / Memory Safe)")
                    ValidationCheckItem("Intérprete Oficial Lua 5.4 Original (PUC-Rio embebido en C++ nativo)")

                    // Estado de Motores Nativos
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ESTADO DE MOTORES NATIVOS (C++, RUST, LUA)",
                        color = Color(0xFF00E676),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    val nativeStatus = remember { com.example.nativebridge.NativeEngineBridge.getDiagnosticInfo() }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10192E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5500E676)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• C++: ${nativeStatus.cppMessage}", color = Color(0xFFE0E0E0), fontSize = 11.sp)
                            Text("• Rust: ${nativeStatus.rustMessage}", color = Color(0xFFE0E0E0), fontSize = 11.sp)
                            Text("• Lua 5.4: ${nativeStatus.luaMessage}", color = Color(0xFFE0E0E0), fontSize = 11.sp)
                        }
                    }

                    // Control de Sensibilidad
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SENSIBILIDAD DE APUNTADO: ${String.format("%.2f", currentSensitivity)}",
                        color = Color(0xFFFFAA00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = currentSensitivity,
                        onValueChange = onSensitivityChange,
                        valueRange = 0.05f..0.40f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        ),
                        modifier = Modifier.testTag("sensitivity_slider")
                    )
                }

                // Botones de acción inferior
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onSpawnTarget,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("spawn_target_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Text("+ Generar Objetivo Extra", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onReset,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_arena_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reiniciar Arena", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F36)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, color = Color(0xFF88A0C0), fontSize = 10.sp)
            Text(
                text = value,
                color = Color(0xFF00E5FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ValidationCheckItem(description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10192E), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF00E676),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = description,
            color = Color(0xFFE0E0E0),
            fontSize = 11.5.sp
        )
    }
}
