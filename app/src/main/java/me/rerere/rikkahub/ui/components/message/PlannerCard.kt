package me.rerere.rikkahub.ui.components.message

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.ArrowDown01
import me.rerere.hugeicons.stroke.ArrowUp01
import me.rerere.hugeicons.stroke.Tick01
import me.rerere.hugeicons.stroke.Clock02
import me.rerere.hugeicons.stroke.Settings03

/**
 * Status of a single plan step.
 */
enum class PlanStepStatus {
    COMPLETED,
    IN_PROGRESS,
    PENDING
}

/**
 * A single step in the plan.
 */
data class PlanStep(
    val title: String,
    val status: PlanStepStatus,
)

/**
 * Expandable planner capsule card — inspired by Manus AI.
 *
 * Collapsed: shows icon + "Planner" + progress counter (e.g. 3/5) + chevron.
 * Expanded:  reveals the full step list with status icons.
 */
@Composable
fun PlannerCard(
    steps: List<PlanStep>,
    modifier: Modifier = Modifier,
    title: String = "Planner",
    initiallyExpanded: Boolean = false,
) {
    if (steps.isEmpty()) return

    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val completedCount = steps.count { it.status == PlanStepStatus.COMPLETED }
    val totalCount = steps.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        ),
    ) {
        Column {
            // ── Header (always visible) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Planner icon
                Icon(
                    imageVector = HugeIcons.Settings03,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )

                // Progress counter
                Text(
                    text = "$completedCount / $totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Expand/collapse chevron
                Icon(
                    imageVector = if (expanded) HugeIcons.ArrowUp01 else HugeIcons.ArrowDown01,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Steps list (expandable) ──
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    steps.forEach { step ->
                        PlanStepRow(step = step)
                    }
                }
            }
        }
    }
}

/**
 * A single row inside the planner showing status icon + step title.
 */
@Composable
private fun PlanStepRow(step: PlanStep) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        // Status icon
        when (step.status) {
            PlanStepStatus.COMPLETED -> {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = HugeIcons.Tick01,
                        contentDescription = "Completed",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White,
                    )
                }
            }

            PlanStepStatus.IN_PROGRESS -> {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = HugeIcons.Clock02,
                        contentDescription = "In Progress",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White,
                    )
                }
            }

            PlanStepStatus.PENDING -> {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }

        // Step title
        Text(
            text = step.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = when (step.status) {
                PlanStepStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface
                PlanStepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
                PlanStepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
        )
    }
}
