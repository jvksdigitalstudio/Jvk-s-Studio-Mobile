package com.jvk.studio.ui.screens
import androidx.compose.ui.Alignment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jvk.studio.MainViewModel
import com.jvk.studio.ui.components.AddChannelButton
import com.jvk.studio.ui.components.AddChannelSheet
import com.jvk.studio.ui.components.AppHeader
import com.jvk.studio.ui.components.KeyboardExpandHandle
import com.jvk.studio.ui.components.PianoKeyboard
import com.jvk.studio.ui.theme.FlDark

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val bpm             by vm.bpm.collectAsStateWithLifecycle()
    val isPlaying       by vm.isPlaying.collectAsStateWithLifecycle()
    val isRecording     by vm.isRecording.collectAsStateWithLifecycle()
    val keyboardVisible by vm.keyboardVisible.collectAsStateWithLifecycle()

    // Multi-touch active notes set
    val activeNotes = remember { mutableStateOf(setOf<Int>()) }

    // "Add channel" panel visibility
    var showAddChannel by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FlDark)
        ) {
            // ── Header (fixed top) ──
            AppHeader(
                bpm              = bpm,
                isPlaying        = isPlaying,
                isRecording      = isRecording,
                keyboardVisible  = keyboardVisible,
                onPlayToggle     = { vm.togglePlay() },
                onRecToggle      = { vm.toggleRecord() },
                onRewind         = { vm.rewind() },
                onBpmChange      = { vm.setBpm(it) },
                onKeyboardToggle = { vm.toggleKeyboard() }
            )

            // ── Work area (playlist/sequencer placeholder) — fills remaining space above keyboard ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(FlDark),
                contentAlignment = Alignment.Center
            ) {
                // FL Mobile-style "+" entry point to add channels/instruments
                AddChannelButton(onClick = { showAddChannel = true })
            }

            // ── Piano keyboard — anchored to bottom, animated show/hide ──
            AnimatedVisibility(
                visible = keyboardVisible,
                enter   = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit    = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                PianoKeyboard(
                    modifier    = Modifier
                        .fillMaxWidth(),
                    activeNotes = activeNotes.value,
                    onNoteOn    = { note ->
                        activeNotes.value = activeNotes.value + note
                        vm.noteOn(note)
                    },
                    onNoteOff   = { note ->
                        activeNotes.value = activeNotes.value - note
                        vm.noteOff(note)
                    },
                    onClose     = { vm.toggleKeyboard() }
                )
            }
        }

        // ── Blank white panel — opens on top of everything when "+" is tapped ──
        AddChannelSheet(
            visible   = showAddChannel,
            onDismiss = { showAddChannel = false }
        )

        // ── Collapsed handle — bottom-right tab to re-expand the keyboard, FL Mobile style ──
        AnimatedVisibility(
            visible  = !keyboardVisible,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter    = fadeIn(),
            exit     = fadeOut()
        ) {
            KeyboardExpandHandle(onClick = { vm.toggleKeyboard() })
        }
    }
}
