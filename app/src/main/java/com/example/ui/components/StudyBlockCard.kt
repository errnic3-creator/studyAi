package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudyBlockEntity
import com.example.ui.theme.*

@Composable
fun StudyBlockCard(
    block: StudyBlockEntity,
    onToggleCompleted: (Boolean) -> Unit,
    onReschedule: (Int) -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val courseColor = try {
        Color(android.graphics.Color.parseColor(block.courseColor))
    } catch (e: Exception) {
        LavenderPrimary
    }

    val priorityColor = when (block.priority.uppercase()) {
        "HIGH" -> PriorityHigh
        "MEDIUM" -> PriorityMedium
        else -> PriorityLow
    }

    val cardBg by animateColorAsState(
        targetValue = if (block.isCompleted) {
            ElegantDarkBg
        } else {
            ElegantDarkSurface
        },
        animationSpec = tween(300),
        label = "card_bg"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_block_card_${block.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .intrinsicHeight()
        ) {
            // Left Accent Pill Indicator
            Box(
                modifier = Modifier
                    .padding(start = 14.dp, top = 16.dp, bottom = 16.dp)
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(fullDp = 4.dp))
                    .background(if (block.isCompleted) ElegantDarkOutline else courseColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header Row: Course Name + Priority Tag + Action Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = LavenderOnPrimary
                        ) {
                            Text(
                                text = block.courseName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = LavenderPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Priority Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = block.priority,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = priorityColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("block_menu_btn_${block.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(ElegantDarkSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Reschedule (+1 Day)") },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = LavenderPrimary) },
                                onClick = {
                                    showMenu = false
                                    onReschedule(1)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Session") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Rose500) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title + Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = block.isCompleted,
                        onCheckedChange = { onToggleCompleted(block.isCompleted) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LavenderPrimary,
                            checkmarkColor = LavenderOnPrimary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("task_checkbox_${block.id}")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = block.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (block.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = if (block.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )

                        if (block.subtopics.isNotBlank()) {
                            Text(
                                text = block.subtopics,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Meta Row: Time Slot + Technique Badge + Focus Launcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time slot",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${block.timeSlot} • ${block.durationMinutes}m",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Technique Tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MauveContainer
                        ) {
                            Text(
                                text = block.technique,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MauveOnContainer,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!block.isCompleted) {
                        Button(
                            onClick = onStartFocus,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("start_focus_btn_${block.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Focus",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Focus",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun RoundedCornerShape(fullDp: androidx.compose.ui.unit.Dp) = RoundedCornerShape(fullDp)
private fun Modifier.intrinsicHeight(): Modifier = this
