package me.rerere.rikkahub.ui.components.message

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.ArrowDown01
import me.rerere.hugeicons.stroke.ArrowUp01
import me.rerere.hugeicons.stroke.Settings03

// ── Terminal theme colors ──
private val TerminalBackground = Color(0xFF1A1A2E)
private val TerminalGreen = Color(0xFF4ADE80)
private val TerminalGray = Color(0xFF9CA3AF)
private val TerminalWhite = Color(0xFFE5E7EB)
private val TerminalRed = Color(0xFFEF4444)
private val TerminalYellow = Color(0xFFFBBF24)
private val TerminalPromptUser = Color(0xFF22D3EE)

/**
 * Determines the terminal type from a tool name.
 */
enum class TerminalType(val label: String, val icon: String) {
    TERMUX("Termux", ">_"),
    SSH("SSH Remote", "ssh"),
    UNKNOWN("Terminal", ">_");

    companion object {
        fun fromToolName(name: String): TerminalType = when {
            name.contains("termux", ignoreCase = true) -> TERMUX
            name.contains("ssh", ignoreCase = true) -> SSH
            else -> UNKNOWN
        }
    }
}

/**
 * Data for a terminal session display.
 */
data class TerminalSession(
    val type: TerminalType,
    val command: String,
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Int? = null,
    val isRunning: Boolean = false,
    val hostLabel: String? = null,
)

/**
 * Live terminal view — dark card with green-on-black monospace output.
 *
 * Collapsed: shows terminal icon + "Executing command: ..." + progress bar.
 * Expanded:  reveals the full terminal output with colored prompt and output.
 */
@Composable
fun TerminalView(
    session: TerminalSession,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TerminalBackground,
        ),
    ) {
        Column {
            // ── Header bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Terminal icon (circle with >_)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2D2D44)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ">_",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TerminalGreen,
                    )
                }

                // Title + subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = buildString {
                            append(session.type.label)
                            if (session.isRunning) append(" is running")
                            if (session.hostLabel != null) {
                                append(" • ${session.hostLabel}")
                            }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TerminalWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "$ ${session.command.take(50)}${if (session.command.length > 50) "…" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = TerminalGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Expand/collapse
                Icon(
                    imageVector = if (expanded) HugeIcons.ArrowUp01 else HugeIcons.ArrowDown01,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(18.dp),
                    tint = TerminalGray,
                )
            }

            // ── Progress bar (visible when running) ──
            if (session.isRunning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    color = TerminalGreen,
                    trackColor = Color(0xFF2D2D44),
                )
            }

            // ── Terminal output (expandable) ──
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp, top = 6.dp),
                ) {
                    // Title bar for terminal window
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(Color(0xFF2D2D44))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Traffic light dots
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(TerminalRed))
                            Box(Modifier.size(8.dp).clip(CircleShape).background(TerminalYellow))
                            Box(Modifier.size(8.dp).clip(CircleShape).background(TerminalGreen))
                        }

                        Text(
                            text = session.type.label.lowercase(),
                            modifier = Modifier.padding(start = 12.dp),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TerminalGray,
                        )
                    }

                    // Terminal content area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(Color(0xFF0D0D1A))
                            .heightIn(min = 60.dp, max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp),
                    ) {
                        Text(
                            text = buildTerminalOutput(session),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        )
                    }

                    // Exit code badge
                    if (session.exitCode != null && !session.isRunning) {
                        Row(
                            modifier = Modifier
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val isSuccess = session.exitCode == 0
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSuccess) TerminalGreen else TerminalRed),
                            )
                            Text(
                                text = if (isSuccess) "Exit: 0 (success)" else "Exit: ${session.exitCode} (error)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (isSuccess) TerminalGreen else TerminalRed,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds the colored terminal output as an AnnotatedString.
 * Shows: prompt + command in green, stdout in white, stderr in red.
 */
@Composable
private fun buildTerminalOutput(session: TerminalSession) = buildAnnotatedString {
    // Prompt line
    val promptPrefix = when (session.type) {
        TerminalType.TERMUX -> "termux"
        TerminalType.SSH -> session.hostLabel ?: "remote"
        TerminalType.UNKNOWN -> "shell"
    }

    withStyle(SpanStyle(color = TerminalPromptUser, fontWeight = FontWeight.Bold)) {
        append("$promptPrefix:~ $ ")
    }
    withStyle(SpanStyle(color = TerminalGreen)) {
        append(session.command)
    }
    append("\n")

    // Stdout
    if (session.stdout.isNotBlank()) {
        withStyle(SpanStyle(color = TerminalWhite)) {
            append(cleanAnsiCodes(session.stdout))
        }
        if (!session.stdout.endsWith("\n")) append("\n")
    }

    // Stderr
    if (session.stderr.isNotBlank()) {
        withStyle(SpanStyle(color = TerminalRed)) {
            append(cleanAnsiCodes(session.stderr))
        }
    }

    // Running indicator
    if (session.isRunning && session.stdout.isBlank() && session.stderr.isBlank()) {
        withStyle(SpanStyle(color = TerminalGray)) {
            append("Waiting for output...")
        }
    }
}

/**
 * Strip ANSI escape codes from terminal output for clean display.
 */
private fun cleanAnsiCodes(text: String): String {
    return text
        .replace(Regex("\\u001b\\[[0-9;]*[a-zA-Z]"), "")
        .replace(Regex("\\u001b\\[\\?[0-9;]*[a-zA-Z]"), "")
        .replace(Regex("\\u001b\\][^\u0007]*\u0007"), "")
        .trim()
}

/**
 * Checks if a tool name is a terminal-type tool.
 */
fun isTerminalTool(toolName: String): Boolean {
    return toolName in setOf(
        "termux_run_command",
        "ssh_exec",
        "ssh_exec_saved",
    )
}
