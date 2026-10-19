package com.jarvis.voiceassistant.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SettingsAccessibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.voiceassistant.ui.theme.JarvisCyan
import com.jarvis.voiceassistant.ui.theme.JarvisDeepBlue
import com.jarvis.voiceassistant.ui.theme.JarvisError
import com.jarvis.voiceassistant.ui.theme.JarvisSuccess

data class CommandHistoryItem(
    val command: String,
    val response: String,
    val success: Boolean
)

@Composable
fun JarvisScreen(
    isListening: Boolean,
    statusText: String,
    transcriptText: String,
    commandHistory: List<CommandHistoryItem>,
    pendingCallTarget: String?,
    onMicClick: () -> Unit,
    onConfirmCall: (String) -> Unit,
    onDismissCall: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF020611), Color(0xFF081226))))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "JARVIS",
                        color = JarvisCyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "AI VOICE CONTROL CORE",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.SettingsAccessibility,
                    contentDescription = "Accessibility",
                    tint = JarvisCyan,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onOpenAccessibilitySettings() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .scale(pulseScale)
                        .border(
                            width = if (isListening) 3.dp else 1.dp,
                            brush = Brush.radialGradient(listOf(JarvisCyan, Color.Transparent)),
                            shape = CircleShape
                        )
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isListening) listOf(JarvisCyan, JarvisDeepBlue)
                                else listOf(Color(0xFF0F2B48), Color(0xFF061528))
                            )
                        )
                        .border(2.dp, JarvisCyan, CircleShape)
                        .clickable { onMicClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (isListening) Color.Black else JarvisCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isListening) "LISTENING..." else statusText.uppercase(),
                color = if (isListening) JarvisCyan else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            if (transcriptText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3300E5FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$transcriptText\"",
                        color = Color.White,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "LOGS & COMMAND HISTORY",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (commandHistory.isEmpty()) {
                    item {
                        Text(
                            text = "Tap the reactor to give commands:\n• Open YouTube\n• Open WhatsApp\n• Volume Up / Volume Down\n• Open notification shade\n• Call Rahul\n• Search Python tips on Google",
                            color = Color.DarkGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                } else {
                    items(commandHistory) { item ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "> ${item.command}", color = JarvisCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = if (item.success) "SUCCESS" else "FAILED",
                                        color = if (item.success) JarvisSuccess else JarvisError,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = item.response, color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (pendingCallTarget != null) {
            AlertDialog(
                onDismissRequest = onDismissCall,
                containerColor = Color(0xFF0E1A2D),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = JarvisCyan)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Confirm Call", color = Color.White)
                    }
                },
                text = { Text("Do you want to dial $pendingCallTarget?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = { onConfirmCall(pendingCallTarget) },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Text("Dial", color = Color.Black)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismissCall) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}
