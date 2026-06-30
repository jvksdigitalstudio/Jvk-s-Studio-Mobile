package com.jvk.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jvk.studio.ui.theme.*

private val BLACK_SEMITONES = setOf(1, 3, 6, 8, 10)
private val NOTE_NAMES = listOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")

data class KeyInfo(
    val midiNote: Int,
    val isBlack: Boolean,
    val octave: Int,
    val semitone: Int,
    val noteName: String,
)

private val ALL_KEYS: List<KeyInfo> = (0..119).map { note ->
    val semi = note % 12
    val oct  = (note / 12) - 1
    KeyInfo(
        midiNote = note,
        isBlack  = semi in BLACK_SEMITONES,
        octave   = oct,
        semitone = semi,
        noteName = "${NOTE_NAMES[semi]}$oct"
    )
}

@Composable
fun PianoKeyboard(
    modifier: Modifier = Modifier,
    activeNotes: Set<Int> = emptySet(),
    onNoteOn: (Int) -> Unit,
    onNoteOff: (Int) -> Unit,
    onClose: () -> Unit = {},
    whiteKeyWidth: Dp = 42.dp,
) {
    val density     = LocalDensity.current
    val whiteKeys   = remember { ALL_KEYS.filter { !it.isBlack } }
    val blackKeyW   = whiteKeyWidth * 0.6f
    val scrollState = rememberScrollState()

    val pointerMap = remember { mutableStateMapOf<Long, Int>() }

    // ── Premium frame: dark housing + header bar + keys ──
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0712), Color(0xFF050309))
                )
            )
    ) {
        // ── Header bar — accent rail + close (×) button, sits ABOVE the keys ──
        // Closing this (×) hides the ENTIRE piano roll, header included.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF140B22), Color(0xFF0D0716), Color(0xFF140B22))
                    )
                )
        ) {
            // glowing separator line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                FlPurple.copy(alpha = 0.15f),
                                FlPurpleLight.copy(alpha = 0.55f),
                                FlPurple.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Text(
                text = "⌨ TECLADO",
                style = TextStyle(
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.SemiBold,
                    fontSize      = 10.sp,
                    letterSpacing = 1.sp,
                    color         = FlPurpleLight.copy(alpha = 0.55f)
                ),
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
            )

            // Close (×) button — top-right, in the keyboard's own header.
            // Hides the whole piano roll (header + keys) when tapped.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A1F40), Color(0xFF160E26))
                        )
                    )
                    .border(1.dp, FlPurple.copy(alpha = 0.55f), CircleShape)
                    .pointerInput(Unit) { detectTapGestures(onTap = { onClose() }) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Ocultar teclado",
                    tint               = FlPurpleLight,
                    modifier           = Modifier.size(14.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(start = 2.dp, end = 2.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(Color(0xFF070410))
                .horizontalScroll(scrollState)
        ) {
            BoxWithConstraints {
                val keyHeightPx = constraints.maxHeight.toFloat()
                val keyHeight   = with(density) { keyHeightPx.toDp() }
                val blackKeyH   = keyHeight * 0.6f

                val whiteKeyWPx = with(density) { whiteKeyWidth.toPx() }
                val blackKeyWPx = with(density) { blackKeyW.toPx() }
                val blackKeyHPx = with(density) { blackKeyH.toPx() }

                val keyPositions = remember {
                    var wIdx = 0
                    ALL_KEYS.map { key ->
                        if (!key.isBlack) {
                            val x = wIdx * whiteKeyWPx
                            wIdx++
                            x
                        } else {
                            (wIdx - 1) * whiteKeyWPx + whiteKeyWPx - blackKeyWPx / 2f
                        }
                    }
                }

                fun noteAt(x: Float, y: Float): Int? {
                    ALL_KEYS.forEachIndexed { i, key ->
                        if (key.isBlack) {
                            val kx = keyPositions[i]
                            if (y <= blackKeyHPx && x >= kx && x <= kx + blackKeyWPx) {
                                return key.midiNote
                            }
                        }
                    }
                    val wIdx = (x / whiteKeyWPx).toInt().coerceIn(0, whiteKeys.size - 1)
                    return whiteKeys.getOrNull(wIdx)?.midiNote
                }

                val totalWidth = whiteKeyWidth * whiteKeys.size

                Box(
                    modifier = Modifier
                        .width(totalWidth)
                        .fillMaxHeight()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    event.changes.forEach { change ->
                                        val rawX = change.position.x
                                        val rawY = change.position.y
                                        val id   = change.id.value

                                        when {
                                            change.pressed && !change.previousPressed -> {
                                                val note = noteAt(rawX, rawY)
                                                if (note != null) {
                                                    pointerMap[id] = note
                                                    onNoteOn(note)
                                                }
                                                change.consume()
                                            }
                                            !change.pressed && change.previousPressed -> {
                                                pointerMap[id]?.let { onNoteOff(it) }
                                                pointerMap.remove(id)
                                                change.consume()
                                            }
                                            change.pressed -> {
                                                val note = noteAt(rawX, rawY)
                                                val prev = pointerMap[id]
                                                if (note != null && note != prev) {
                                                    prev?.let { onNoteOff(it) }
                                                    pointerMap[id] = note
                                                    onNoteOn(note)
                                                }
                                                change.consume()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    // White keys
                    Row(modifier = Modifier.fillMaxSize()) {
                        whiteKeys.forEach { key ->
                            WhiteKey(
                                width   = whiteKeyWidth,
                                pressed = key.midiNote in activeNotes,
                                label   = if (key.semitone == 0) key.noteName else ""
                            )
                        }
                    }

                    // Black keys — absolute positioned, drawn on top
                    ALL_KEYS.forEachIndexed { i, key ->
                        if (key.isBlack) {
                            val xDp = with(density) { keyPositions[i].toDp() }
                            BlackKey(
                                xOffset = xDp,
                                width   = blackKeyW,
                                height  = blackKeyH,
                                pressed = key.midiNote in activeNotes
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WhiteKey(width: Dp, pressed: Boolean, label: String) {
    Box(
        modifier = Modifier
            .width(width - 1.dp)
            .fillMaxHeight()
            .padding(end = 1.dp)
            .shadow(if (pressed) 0.dp else 3.dp, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
            .background(
                if (pressed) Brush.verticalGradient(
                    listOf(FlPurpleLight, FlPurple, FlPurpleDim)
                ) else Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF3EEFF),
                        Color(0xFFE2D6F7),
                        Color(0xFFD2C2EE)
                    )
                )
            )
            .border(
                width = 0.6.dp,
                color = if (pressed) FlPurpleLight.copy(alpha = 0.8f) else Color(0xFFBBA8DD).copy(alpha = 0.5f),
                shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (label.isNotEmpty()) {
            Text(
                text  = label,
                style = TextStyle(
                    fontFamily   = FontFamily.Monospace,
                    fontWeight   = FontWeight.SemiBold,
                    fontSize     = 8.sp,
                    letterSpacing = 0.5.sp,
                    color = if (pressed) Color.White else Color(0xFF6B4FA8).copy(alpha = 0.85f)
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
fun BlackKey(xOffset: Dp, width: Dp, height: Dp, pressed: Boolean) {
    Box(
        modifier = Modifier
            .absoluteOffset(x = xOffset)
            .width(width)
            .height(height)
            .shadow(if (pressed) 1.dp else 5.dp, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
            .background(
                if (pressed) Brush.verticalGradient(
                    listOf(FlPurpleDim, Color(0xFF4C1D95), Color(0xFF3B1670))
                ) else Brush.verticalGradient(
                    listOf(
                        Color(0xFF2A1840),
                        Color(0xFF160B26),
                        Color(0xFF0A0514),
                        Color(0xFF050208)
                    )
                )
            )
            .border(
                width = 0.6.dp,
                color = if (pressed) FlPurpleLight.copy(alpha = 0.9f) else Color(0xFF4A2D78).copy(alpha = 0.6f),
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
    ) {
        // subtle top highlight for glossy feel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height * 0.12f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (pressed) 0.12f else 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
