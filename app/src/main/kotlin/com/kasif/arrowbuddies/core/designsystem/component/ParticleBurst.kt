package com.kasif.arrowbuddies.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesTheme
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Grape
import com.kasif.arrowbuddies.core.designsystem.theme.Mango
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.Sky
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

private const val MAX_PARTICLES = 150

enum class ParticleKind {
    Confetti,
    Sparkle
}

/**
 * Mutable, object-pooled Particle. Zero allocations during simulation/drawing.
 */
class PooledParticle {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var vr: Float = 0f
    var rotation: Float = 0f
    var size: Float = 0f
    var color: Color = Color.Unspecified
    var shape: Int = 0 // 0 = ribbon, 1 = star/glint, 2 = circle
    var life: Float = 0f
    var maxLife: Float = 1f
    var gravity: Float = 980f
}

class ParticleSystemState {
    val pool = Array(MAX_PARTICLES) { PooledParticle() }
    private val random = Random()
    private val palette = listOf(Mango, Coral, Mint, Sky, Grape)

    var hasActiveParticles: Boolean = false
        private set

    fun spawnConfetti(originX: Float, originY: Float, count: Int = 80) {
        val toSpawn = count.coerceAtMost(MAX_PARTICLES)
        var spawned = 0
        for (p in pool) {
            if (!p.active) {
                p.active = true
                p.x = originX
                p.y = originY
                val angle = random.nextFloat() * Math.PI * 2
                val speed = 350f + random.nextFloat() * 450f
                p.vx = (cos(angle) * speed).toFloat()
                p.vy = (sin(angle) * speed).toFloat() - 250f // Initial upward pop
                p.vr = (random.nextFloat() - 0.5f) * 600f
                p.rotation = random.nextFloat() * 360f
                p.size = 14f + random.nextFloat() * 12f
                p.color = palette[random.nextInt(palette.size)]
                p.shape = if (random.nextBoolean()) 0 else 2
                p.life = 0f
                p.maxLife = 1.2f + random.nextFloat() * 0.8f
                p.gravity = 750f
                spawned++
                if (spawned >= toSpawn) break
            }
        }
        hasActiveParticles = true
    }

    fun spawnSparkle(originX: Float, originY: Float, count: Int = 40) {
        val toSpawn = count.coerceAtMost(MAX_PARTICLES)
        var spawned = 0
        for (p in pool) {
            if (!p.active) {
                p.active = true
                p.x = originX
                p.y = originY
                val angle = random.nextFloat() * Math.PI * 2
                val speed = 120f + random.nextFloat() * 260f
                p.vx = (cos(angle) * speed).toFloat()
                p.vy = (sin(angle) * speed).toFloat()
                p.vr = (random.nextFloat() - 0.5f) * 360f
                p.rotation = random.nextFloat() * 360f
                p.size = 8f + random.nextFloat() * 10f
                p.color = if (random.nextBoolean()) Color(0xFFFFDF7A) else Color.White
                p.shape = 1 // Sparkle glint
                p.life = 0f
                p.maxLife = 0.6f + random.nextFloat() * 0.5f
                p.gravity = 80f
                spawned++
                if (spawned >= toSpawn) break
            }
        }
        hasActiveParticles = true
    }

    fun update(dt: Float) {
        var anyActive = false
        for (p in pool) {
            if (p.active) {
                p.life += dt
                if (p.life >= p.maxLife) {
                    p.active = false
                } else {
                    p.vy += p.gravity * dt
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.rotation += p.vr * dt
                    anyActive = true
                }
            }
        }
        hasActiveParticles = anyActive
    }
}

/**
 * Lightweight particle overlay driven by [withFrameNanos].
 * Uses zero allocations per frame and object pooling.
 */
@Composable
fun ParticleBurst(
    state: ParticleSystemState,
    modifier: Modifier = Modifier
) {
    var frameTimeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(state.hasActiveParticles) {
        if (!state.hasActiveParticles) return@LaunchedEffect
        var lastFrame = 0L
        while (state.hasActiveParticles) {
            withFrameNanos { now ->
                if (lastFrame != 0L) {
                    val dt = ((now - lastFrame) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
                    state.update(dt)
                }
                lastFrame = now
                frameTimeNanos = now
            }
        }
    }

    // Force draw phase observation of frameTimeNanos
    Canvas(modifier = modifier.fillMaxSize()) {
        val dummy = frameTimeNanos
        for (p in state.pool) {
            if (p.active) {
                val alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
                val color = p.color.copy(alpha = alpha)

                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    when (p.shape) {
                        0 -> { // Ribbon
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(p.x - p.size * 0.5f, p.y - p.size * 0.25f),
                                size = Size(p.size, p.size * 0.5f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                            )
                        }
                        1 -> { // 4-pointed sparkle
                            drawSparkle(p.x, p.y, p.size, color)
                        }
                        else -> { // Circle
                            drawCircle(
                                color = color,
                                radius = p.size * 0.45f,
                                center = Offset(p.x, p.y)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSparkle(cx: Float, cy: Float, size: Float, color: Color) {
    val half = size * 0.5f
    val thin = size * 0.15f
    // Vertical diamond
    drawLine(color = color, start = Offset(cx, cy - half), end = Offset(cx, cy + half), strokeWidth = thin)
    // Horizontal diamond
    drawLine(color = color, start = Offset(cx - half, cy), end = Offset(cx + half, cy), strokeWidth = thin)
}

@Preview(name = "Particle Burst Preview", showBackground = true)
@Composable
private fun ParticleBurstPreview() {
    ArrowBuddiesTheme {
        val state = remember {
            ParticleSystemState().apply {
                spawnConfetti(200f, 200f, 30)
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            ParticleBurst(state = state)
        }
    }
}
