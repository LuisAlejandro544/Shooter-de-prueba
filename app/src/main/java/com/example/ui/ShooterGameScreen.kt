package com.example.ui

import android.opengl.GLSurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.viewmodel.ShooterViewModel

@Composable
fun ShooterGameScreen(
    viewModel: ShooterViewModel,
    modifier: Modifier = Modifier
) {
    val hudState by viewModel.hudState.collectAsStateWithLifecycle()
    var showValidationDialog by remember { mutableStateOf(false) }
    var currentSensitivity by remember { mutableFloatStateOf(0.16f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shooter_screen")
    ) {
        // 1. Vista OpenGL ES 3D en tiempo real
        AndroidView(
            factory = { ctx ->
                GLSurfaceView(ctx).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(viewModel.renderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Capa táctil para rotar la cámara 3D (Aim look drag en la mitad derecha)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.65f)
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.onAimDrag(dragAmount.x, dragAmount.y)
                    }
                }
        )

        // 3. Muzzle Flash (Destello breve en pantalla al disparar)
        AnimatedVisibility(
            visible = hudState.ammo < hudState.maxAmmo && hudState.hitMarker,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x2200E5FF))
            )
        }

        // 4. Retícula central y Hit Marker
        CrosshairHud(
            isHit = hudState.hitMarker,
            isFiring = false
        )

        // 5. Barra superior del HUD (Inmersiva y adaptada a Landscape)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Salud y Wave
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xBB0A1428),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4400E5FF)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFF2A4B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${hudState.health} HP",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xBB0A1428),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FPS: ${hudState.fps}",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Puntuación y Dianas centrales
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD0D1B36),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "DIANAS: ${hudState.targetsDestroyed}",
                            color = Color(0xFFFFAA00),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PTS: ${hudState.score}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Munición y Botón de Validación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xBB0A1428),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (hudState.ammo <= 5) Color(0xFFFF2A4B) else Color(0x4400E5FF)
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (hudState.isReloading) "RECARGANDO..." else "${hudState.ammo} / ${hudState.maxAmmo}",
                            color = if (hudState.ammo <= 5) Color(0xFFFF5252) else Color(0xFF00E5FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Botón para abrir el panel de validación
                Button(
                    onClick = { showValidationDialog = true },
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("open_validation_button"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006699))
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = "Validar",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("VALIDAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Controles táctiles inferiores para pantalla móvil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Izquierda: Joystick virtual de desplazamiento y strafe
            VirtualJoystick(
                modifier = Modifier.padding(bottom = 6.dp),
                size = 145.dp,
                onMove = { x, y ->
                    viewModel.onJoystickMoved(x, y)
                }
            )

            // Derecha: Botones de Acción (Salto, Recarga y Disparo)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // Botón de Salto 3D
                IconButton(
                    onClick = { viewModel.jump() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0D1B36))
                        .border(1.5.dp, Color(0x6600E5FF), CircleShape)
                        .testTag("jump_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = "Salto",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Botón de Recarga rápida
                IconButton(
                    onClick = { viewModel.reload() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC0D1B36))
                        .border(1.5.dp, Color(0x6600E5FF), CircleShape)
                        .testTag("reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar",
                        tint = if (hudState.isReloading) Color(0xFFFFAA00) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Botón Principal de DISPARAR (Grande, táctil y accesible)
                Button(
                    onClick = { viewModel.fire() },
                    modifier = Modifier
                        .size(width = 110.dp, height = 75.dp)
                        .testTag("fire_button"),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFF3D00), Color(0xFFD50000))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(2.dp, Color(0xFFFFD600), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                text = "DISPARAR",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // 7. Diálogo / Hoja de Validación interactiva
        if (showValidationDialog) {
            ValidationSheet(
                hudState = hudState,
                onDismiss = { showValidationDialog = false },
                onReset = {
                    viewModel.resetGame()
                    showValidationDialog = false
                },
                onSpawnTarget = {
                    viewModel.addValidationTarget()
                },
                onSensitivityChange = { newSens ->
                    currentSensitivity = newSens
                    viewModel.setAimSensitivity(newSens)
                },
                currentSensitivity = currentSensitivity
            )
        }
    }
}
