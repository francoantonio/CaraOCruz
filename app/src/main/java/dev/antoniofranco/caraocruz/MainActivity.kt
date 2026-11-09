package dev.antoniofranco.caraocruz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val HISTORY_SIZE = 10

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { AppTheme { App() } }
    }
}

@Composable
private fun AppTheme(content: @Composable () -> Unit) {
    val green = Color(0xFF1E5B4F)
    val scheme = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFF8FD3C1), onPrimary = Color(0xFF00382D))
    } else {
        lightColorScheme(primary = green, onPrimary = Color.White)
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
private fun App() {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 },
                    text = { Text(stringResource(R.string.tab_coin)) })
                Tab(selected = tab == 1, onClick = { tab = 1 },
                    text = { Text(stringResource(R.string.tab_dice)) })
            }
            // Conserva el estado (historial, resultado) de cada pestaña al cambiar entre ellas
            val holder = rememberSaveableStateHolder()
            holder.SaveableStateProvider(tab) {
                if (tab == 0) CoinScreen() else DiceScreen()
            }
        }
    }
}

/* ---------------- Moneda ---------------- */

@Composable
private fun CoinScreen() {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var result by rememberSaveable { mutableStateOf<Boolean?>(null) } // true = cara
    // Al restaurar, la moneda arranca mostrando la cara del último resultado
    val rotation = remember { Animatable(if (result == false) 180f else 0f) }
    var busy by remember { mutableStateOf(false) }
    val history = rememberSaveable(saver = listSaver(), init = { mutableStateListOf<Boolean>() })
    val heads = stringResource(R.string.heads)
    val tails = stringResource(R.string.tails)

    // Cara visible cuando la rotación (mod 360) está en el frente
    val angle = ((rotation.value % 360f) + 360f) % 360f
    val showingHeads = angle < 90f || angle > 270f

    ScreenLayout(
        visual = {
            Box(
                Modifier.size(220.dp).graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 12f * density
                },
                contentAlignment = Alignment.Center,
            ) {
                Coin(face = if (result == null) "?" else if (showingHeads) heads else tails,
                    mirrored = !showingHeads)
            }
        },
        resultText = when (result) {
            null -> stringResource(R.string.tap_to_start)
            else -> if (busy) "…" else if (result == true) heads else tails
        },
        buttonText = stringResource(R.string.flip),
        busy = busy,
        onAction = {
            busy = true
            val isHeads = Random.nextBoolean()
            scope.launch {
                if (result == null) result = isHeads
                // Siempre arranca desde la cara actual y da 5 vueltas, terminando en la nueva cara
                val start = if (result == true) 0f else 180f
                rotation.snapTo(start)
                result = isHeads
                val end = 360f * 5 + if (isHeads) 0f else 180f
                rotation.animateTo(end, tween(1200, easing = FastOutSlowInEasing))
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                history.add(0, isHeads)
                while (history.size > HISTORY_SIZE) history.removeAt(history.lastIndex)
                busy = false
            }
        },
        historyItems = history.map { if (it) heads else tails },
        stats = stringResource(R.string.stats_coin, history.count { it }, history.count { !it }),
        onClear = { history.clear() },
    )
}

@Composable
private fun Coin(face: String, mirrored: Boolean) {
    val gold = Color(0xFFF4C430)
    val rim = Color(0xFFB8860B)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2
            drawCircle(rim, r)
            drawCircle(gold, r * 0.86f)
            drawCircle(rim, r * 0.62f, style = Stroke(width = r * 0.04f))
        }
        Text(
            face,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A4300),
            // La cara de atrás se ve espejada por la rotación: la compensamos
            modifier = Modifier.graphicsLayer { scaleX = if (mirrored) -1f else 1f },
        )
    }
}

/* ---------------- Dado ---------------- */

@Composable
private fun DiceScreen() {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val spin = remember { Animatable(0f) }
    var value by rememberSaveable { mutableIntStateOf(0) } // 0 = sin tirar
    var busy by remember { mutableStateOf(false) }
    val history = rememberSaveable(saver = intListSaver(), init = { mutableStateListOf<Int>() })

    ScreenLayout(
        visual = {
            Die(value = if (value == 0) 6 else value,
                modifier = Modifier.size(200.dp).graphicsLayer { rotationZ = spin.value })
        },
        resultText = if (value == 0) stringResource(R.string.tap_to_start)
        else if (busy) "…" else stringResource(R.string.dice_result, value),
        buttonText = stringResource(R.string.roll),
        busy = busy,
        onAction = {
            busy = true
            val final = Random.nextInt(1, 7)
            scope.launch {
                launch {
                    spin.snapTo(0f)
                    spin.animateTo(720f, tween(900, easing = FastOutSlowInEasing))
                }
                repeat(9) {
                    value = Random.nextInt(1, 7)
                    delay(90)
                }
                value = final
                delay(100)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                history.add(0, final)
                while (history.size > HISTORY_SIZE) history.removeAt(history.lastIndex)
                busy = false
            }
        },
        historyItems = history.map { it.toString() },
        stats = if (history.isEmpty()) "" else
            "Promedio: %.1f".format(history.average()),
        onClear = { history.clear() },
    )
}

@Composable
private fun Die(value: Int, modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val body = if (dark) Color(0xFFEDEDED) else Color.White
    val border = Color(0xFF2B2B2B)
    val pip = Color(0xFF2B2B2B)
    Canvas(modifier) {
        val s = size.minDimension
        val corner = CornerRadius(s * 0.16f)
        drawRoundRect(body, size = Size(s, s), cornerRadius = corner)
        drawRoundRect(border, size = Size(s, s), cornerRadius = corner,
            style = Stroke(width = s * 0.03f))
        val r = s * 0.085f
        fun p(x: Float, y: Float) = drawCircle(pip, r, Offset(s * x, s * y))
        val lo = 0.27f; val mid = 0.5f; val hi = 0.73f
        when (value) {
            1 -> p(mid, mid)
            2 -> { p(lo, lo); p(hi, hi) }
            3 -> { p(lo, lo); p(mid, mid); p(hi, hi) }
            4 -> { p(lo, lo); p(hi, lo); p(lo, hi); p(hi, hi) }
            5 -> { p(lo, lo); p(hi, lo); p(mid, mid); p(lo, hi); p(hi, hi) }
            else -> { p(lo, lo); p(hi, lo); p(lo, mid); p(hi, mid); p(lo, hi); p(hi, hi) }
        }
    }
}

/* ---------------- Layout común ---------------- */

@Composable
private fun ScreenLayout(
    visual: @Composable () -> Unit,
    resultText: String,
    buttonText: String,
    busy: Boolean,
    onAction: () -> Unit,
    historyItems: List<String>,
    stats: String,
    onClear: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        visual()
        Spacer(Modifier.height(28.dp))
        Text(resultText, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onAction,
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(buttonText, fontSize = 18.sp) }
        Spacer(Modifier.height(20.dp))
        // Altura fija para que el botón no se mueva cuando aparece el historial
        Column(Modifier.fillMaxWidth().height(120.dp)) {
        if (historyItems.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.history),
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = onClear, enabled = !busy) {
                    Text(stringResource(R.string.clear))
                }
            }
            LazyRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(historyItems) { item ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(item, Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(stats, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        }
    }
}

/* ---------------- Guardado de estado al rotar ---------------- */

private fun listSaver() = androidx.compose.runtime.saveable.listSaver<
    androidx.compose.runtime.snapshots.SnapshotStateList<Boolean>, Boolean>(
    save = { it.toList() },
    restore = { mutableStateListOf<Boolean>().apply { addAll(it) } },
)

private fun intListSaver() = androidx.compose.runtime.saveable.listSaver<
    androidx.compose.runtime.snapshots.SnapshotStateList<Int>, Int>(
    save = { it.toList() },
    restore = { mutableStateListOf<Int>().apply { addAll(it) } },
)
