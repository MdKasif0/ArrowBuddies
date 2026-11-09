package com.kasif.arrowbuddies.feature.levellab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kasif.arrowbuddies.core.designsystem.component.CandyButton
import com.kasif.arrowbuddies.core.designsystem.component.CandyButtonVariant
import com.kasif.arrowbuddies.core.designsystem.theme.ArrowBuddiesThemeTokens
import com.kasif.arrowbuddies.core.designsystem.theme.Coral
import com.kasif.arrowbuddies.core.designsystem.theme.Cream
import com.kasif.arrowbuddies.core.designsystem.theme.Grape
import com.kasif.arrowbuddies.core.designsystem.theme.Ink
import com.kasif.arrowbuddies.core.designsystem.theme.Mint
import com.kasif.arrowbuddies.core.designsystem.theme.WhitePure
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LevelLabScreen(
    uiState: LevelLabUiState,
    onEvent: (LevelLabEvent) -> Unit,
    onPlayLevel: (levelId: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val typography = ArrowBuddiesThemeTokens.typography

    LaunchedEffect(Unit) {
        if (uiState.generatedLevel == null) {
            onEvent(LevelLabEvent.Generate)
        }
    }

    LaunchedEffect(uiState.copySuccess) {
        if (uiState.copySuccess) {
            snackbarHostState.showSnackbar("Level JSON copied to clipboard!")
            onEvent(LevelLabEvent.ResetCopyStatus)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Level Lab",
                        style = typography.subtitle,
                        color = Ink
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Cream
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Cream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Generator Parameters Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhitePure),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Puzzle Parameters",
                        style = typography.subtitle,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Seed control
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Seed: ${uiState.seed}",
                            style = typography.body,
                            color = Ink
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                onEvent(LevelLabEvent.UpdateSeed(Random.nextLong(1000, 999999)))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Randomize Seed",
                                    tint = Grape
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grid dimensions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Grid Size: ${uiState.cols} x ${uiState.rows}",
                            style = typography.body,
                            color = Ink
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            StepperChip(label = "Cols", value = uiState.cols, min = 4, max = 7) {
                                onEvent(LevelLabEvent.UpdateCols(it))
                            }
                            StepperChip(label = "Rows", value = uiState.rows, min = 4, max = 9) {
                                onEvent(LevelLabEvent.UpdateRows(it))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buddies & Obstacles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Buddies: ${uiState.buddyCount} | Rocks: ${uiState.obstacleBudget}",
                            style = typography.body,
                            color = Ink
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            StepperChip(label = "Buddies", value = uiState.buddyCount, min = 2, max = 20) {
                                onEvent(LevelLabEvent.UpdateBuddyCount(it))
                            }
                            StepperChip(label = "Rocks", value = uiState.obstacleBudget, min = 0, max = 5) {
                                onEvent(LevelLabEvent.UpdateObstacleBudget(it))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // World selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "World: ${uiState.world}",
                            style = typography.body,
                            color = Ink
                        )
                        StepperChip(label = "World", value = uiState.world, min = 1, max = 8) {
                            onEvent(LevelLabEvent.UpdateWorld(it))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mechanics:",
                        style = typography.body,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mechanic Chips
                    val allMechanics = listOf("rock", "mud", "turntable", "cage", "portal", "sleepy")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allMechanics.forEach { mechanic ->
                            val selected = mechanic in uiState.allowedMechanics
                            FilterChip(
                                selected = selected,
                                onClick = { onEvent(LevelLabEvent.ToggleMechanic(mechanic)) },
                                label = { Text(mechanic.replaceFirstChar { it.uppercase() }) },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Mint.copy(alpha = 0.25f),
                                    selectedLabelColor = Ink
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CandyButton(
                    text = if (uiState.isGenerating) "Generating..." else "Generate",
                    onClick = { onEvent(LevelLabEvent.Generate) },
                    variant = CandyButtonVariant.Primary,
                    enabled = !uiState.isGenerating,
                    modifier = Modifier.weight(1f)
                )

                CandyButton(
                    text = "Play",
                    onClick = {
                        val level = uiState.generatedLevel
                        if (level != null) {
                            onPlayLevel(level.id)
                        }
                    },
                    variant = CandyButtonVariant.Success,
                    enabled = uiState.generatedLevel != null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            CandyButton(
                text = "Copy JSON",
                onClick = { onEvent(LevelLabEvent.CopyJson) },
                variant = CandyButtonVariant.Secondary,
                enabled = uiState.jsonString.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Solver Metrics Card
            uiState.solveResult?.let { result ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WhitePure),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Solver Metrics",
                                style = typography.subtitle,
                                color = Ink
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (result.solvable) Mint else Coral)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (result.solvable) "SOLVABLE" else "UNSOLVABLE",
                                    color = WhitePure,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        MetricRow("Difficulty Score", "%.1f".format(result.metrics.difficultyScore))
                        MetricRow("Dependency Depth", "${result.metrics.dependencyDepth}")
                        MetricRow("Initial Free Moves", "${result.metrics.initialFreeMoves}")
                        MetricRow("Dead End Moves", "${result.metrics.deadEndMoves}")
                        MetricRow("Solution Length", "${result.shortestSolution.size} taps")
                        MetricRow("Explored Nodes", "${result.exploredNodes}")
                        MetricRow("Solve Time", "${result.timeMs} ms")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Shortest Solution:",
                            style = typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.shortestSolution.joinToString(" -> "),
                            style = typography.caption,
                            color = Grape,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: $error",
                    color = Coral,
                    style = typography.caption
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepperChip(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    val typography = ArrowBuddiesThemeTokens.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0EDF5))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(enabled = value > min) { onValueChange(value - 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("-", fontWeight = FontWeight.Bold, color = if (value > min) Color.Black else Color.Gray)
        }
        Text(
            text = "$value",
            style = typography.caption,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(enabled = value < max) { onValueChange(value + 1) },
            contentAlignment = Alignment.Center
        ) {
            Text("+", fontWeight = FontWeight.Bold, color = if (value < max) Color.Black else Color.Gray)
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    val typography = ArrowBuddiesThemeTokens.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = typography.caption, color = Color.Gray)
        Text(text = value, style = typography.caption, fontWeight = FontWeight.Bold, color = Ink)
    }
}
