package com.example.ui.tools.puzzles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoSecondaryButton
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val pyraminxColors = listOf(
    Color(0xFF2FA84F), // 0 = Front (Green)
    Color(0xFFD9383A), // 1 = Left (Red)
    Color(0xFF2E6FD8), // 2 = Right (Blue)
    Color(0xFFF2C94C), // 3 = Down (Yellow)
)
private val pyraminxFaceNames = listOf("Front (F)", "Left (L)", "Right (R)", "Bottom (D)")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PyraminxSolverScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val scope = rememberCoroutineScope()

    var inputState by remember { mutableStateOf(PyraminxEngine.PyraminxState.solved()) }
    var selectedColor by remember { mutableIntStateOf(0) }
    var solution by remember { mutableStateOf<List<String>>(emptyList()) }
    var stepIndex by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var solving by remember { mutableStateOf(false) }

    val displayState = remember(inputState, solution, stepIndex) {
        if (solution.isEmpty() || stepIndex == 0) inputState
        else PyraminxEngine.applyMoves(inputState, solution.take(stepIndex))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Pyraminx Solver",
            subtitle = "Optimal bidirectional BFS solver",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonoCard(contentPadding = PaddingValues(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Sticker Color", style = MaterialTheme.typography.labelLarge, color = palette.textSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pyraminxColors.forEachIndexed { idx, col ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(if (selectedColor == idx) 3.dp else 1.dp, palette.textPrimary, CircleShape)
                                        .clickable { selectedColor = idx }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    (0 until 4).chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            pair.forEach { face ->
                                MonoCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(12.dp)) {
                                    Text(pyraminxFaceNames[face], style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                                    Spacer(Modifier.height(8.dp))
                                    val base = face * 9
                                    val rows = listOf(listOf(0), listOf(1, 2, 3), listOf(4, 5, 6, 7, 8))
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        rows.forEach { row ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                row.forEach { offset ->
                                                    val idx = base + offset
                                                    val colIdx = displayState.s[idx].coerceIn(0, 3)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(pyraminxColors[colIdx])
                                                            .border(1.dp, palette.bgMain, RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                if (solution.isEmpty()) {
                                                                    val copy = inputState.s.copyOf()
                                                                    copy[idx] = selectedColor
                                                                    inputState = PyraminxEngine.PyraminxState(copy)
                                                                }
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoSecondaryButton(
                        text = "Scramble",
                        icon = Icons.Outlined.Casino,
                        onClick = {
                            solution = emptyList(); stepIndex = 0; statusText = null
                            inputState = PyraminxEngine.applyMoves(
                                PyraminxEngine.PyraminxState.solved(),
                                ScrambleGenerator.generatePyraminx(),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MonoSecondaryButton(
                        text = "Reset",
                        icon = Icons.Outlined.Refresh,
                        onClick = {
                            solution = emptyList(); stepIndex = 0; statusText = null
                            inputState = PyraminxEngine.PyraminxState.solved()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                MonoPrimaryButton(
                    text = if (solving) "Solving..." else "Solve Pyraminx",
                    enabled = !solving,
                    onClick = {
                        solving = true
                        statusText = null
                        val snap = inputState.copy()
                        scope.launch {
                            val res = withContext(Dispatchers.Default) { PyraminxEngine.solve(snap) }
                            solving = false
                            res.onSuccess { steps ->
                                solution = steps
                                stepIndex = 0
                                if (steps.isEmpty()) statusText = "Pyraminx is already solved!"
                                else viewModel.feedbackAccent()
                            }.onFailure { err ->
                                solution = emptyList()
                                statusText = err.message
                                viewModel.feedbackAlert()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            statusText?.let { msg ->
                item {
                    MonoCard(contentPadding = PaddingValues(14.dp)) {
                        Text(msg, color = palette.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (solution.isNotEmpty()) {
                item {
                    MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Solution (${solution.size} moves)", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text("Step $stepIndex/${solution.size}", style = TabularNumeralStyle, color = palette.accent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            IconButton(onClick = { if (stepIndex > 0) stepIndex-- }, enabled = stepIndex > 0) {
                                Icon(Icons.AutoMirrored.Outlined.NavigateBefore, contentDescription = "Prev", tint = palette.textPrimary)
                            }
                            IconButton(onClick = { if (stepIndex < solution.size) stepIndex++ }, enabled = stepIndex < solution.size) {
                                Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = "Next", tint = palette.textPrimary)
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            solution.forEachIndexed { idx, move ->
                                val active = idx + 1 == stepIndex
                                Surface(
                                    onClick = { stepIndex = idx + 1 },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (active) palette.accent else palette.surfaceSecondary,
                                ) {
                                    Text(
                                        text = move,
                                        style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold),
                                        color = if (active) palette.onAccent else palette.textPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val skewbColors = listOf(
    Color(0xFFF5F5F5), // 0 = U
    Color(0xFFD9383A), // 1 = R
    Color(0xFF2FA84F), // 2 = F
    Color(0xFFF2C94C), // 3 = D
    Color(0xFFE67E22), // 4 = L
    Color(0xFF2E6FD8), // 5 = B
)
private val skewbFaceLabels = listOf("U", "R", "F", "D", "L", "B")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkewbSolverScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val scope = rememberCoroutineScope()

    var inputState by remember { mutableStateOf(SkewbEngine.SkewbState.solved()) }
    var selectedColor by remember { mutableIntStateOf(0) }
    var solution by remember { mutableStateOf<List<String>>(emptyList()) }
    var stepIndex by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var solving by remember { mutableStateOf(false) }

    val displayState = remember(inputState, solution, stepIndex) {
        if (solution.isEmpty() || stepIndex == 0) inputState
        else SkewbEngine.applyMoves(inputState, solution.take(stepIndex))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Skewb Solver",
            subtitle = "Optimal 11-move bidirectional BFS",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonoCard(contentPadding = PaddingValues(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Sticker Color", style = MaterialTheme.typography.labelLarge, color = palette.textSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            skewbColors.forEachIndexed { idx, col ->
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(if (selectedColor == idx) 3.dp else 1.dp, palette.textPrimary, CircleShape)
                                        .clickable { selectedColor = idx }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    (0 until 6).chunked(3).forEach { rowFaces ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowFaces.forEach { face ->
                                val base = face * 5
                                MonoCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(10.dp)) {
                                    Text("Face ${skewbFaceLabels[face]}", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                                    Spacer(Modifier.height(6.dp))
                                    // 3x3-like diamond visual using 2 corner rows + center badge
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            SkewbSticker(displayState.s[base + 1]) {
                                                if (solution.isEmpty()) {
                                                    val arr = inputState.s.copyOf(); arr[base + 1] = selectedColor
                                                    inputState = SkewbEngine.SkewbState(arr)
                                                }
                                            }
                                            SkewbSticker(displayState.s[base + 2]) {
                                                if (solution.isEmpty()) {
                                                    val arr = inputState.s.copyOf(); arr[base + 2] = selectedColor
                                                    inputState = SkewbEngine.SkewbState(arr)
                                                }
                                            }
                                        }
                                        SkewbSticker(displayState.s[base + 0], isCenter = true) {
                                            if (solution.isEmpty()) {
                                                val arr = inputState.s.copyOf(); arr[base + 0] = selectedColor
                                                inputState = SkewbEngine.SkewbState(arr)
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            SkewbSticker(displayState.s[base + 4]) {
                                                if (solution.isEmpty()) {
                                                    val arr = inputState.s.copyOf(); arr[base + 4] = selectedColor
                                                    inputState = SkewbEngine.SkewbState(arr)
                                                }
                                            }
                                            SkewbSticker(displayState.s[base + 3]) {
                                                if (solution.isEmpty()) {
                                                    val arr = inputState.s.copyOf(); arr[base + 3] = selectedColor
                                                    inputState = SkewbEngine.SkewbState(arr)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoSecondaryButton(
                        text = "Scramble",
                        icon = Icons.Outlined.Casino,
                        onClick = {
                            solution = emptyList(); stepIndex = 0; statusText = null
                            inputState = SkewbEngine.applyMoves(
                                SkewbEngine.SkewbState.solved(),
                                ScrambleGenerator.generateSkewb(),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MonoSecondaryButton(
                        text = "Reset",
                        icon = Icons.Outlined.Refresh,
                        onClick = {
                            solution = emptyList(); stepIndex = 0; statusText = null
                            inputState = SkewbEngine.SkewbState.solved()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                MonoPrimaryButton(
                    text = if (solving) "Solving..." else "Solve Skewb",
                    enabled = !solving,
                    onClick = {
                        solving = true
                        statusText = null
                        val snap = inputState.copy()
                        scope.launch {
                            val res = withContext(Dispatchers.Default) { SkewbEngine.solve(snap) }
                            solving = false
                            res.onSuccess { steps ->
                                solution = steps
                                stepIndex = 0
                                if (steps.isEmpty()) statusText = "Skewb is already solved!"
                                else viewModel.feedbackAccent()
                            }.onFailure { err ->
                                solution = emptyList()
                                statusText = err.message
                                viewModel.feedbackAlert()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            statusText?.let { msg ->
                item {
                    MonoCard(contentPadding = PaddingValues(14.dp)) {
                        Text(msg, color = palette.textSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (solution.isNotEmpty()) {
                item {
                    MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Solution (${solution.size} moves)", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text("Step $stepIndex/${solution.size}", style = TabularNumeralStyle, color = palette.accent)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            IconButton(onClick = { if (stepIndex > 0) stepIndex-- }, enabled = stepIndex > 0) {
                                Icon(Icons.AutoMirrored.Outlined.NavigateBefore, contentDescription = "Prev", tint = palette.textPrimary)
                            }
                            IconButton(onClick = { if (stepIndex < solution.size) stepIndex++ }, enabled = stepIndex < solution.size) {
                                Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = "Next", tint = palette.textPrimary)
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            solution.forEachIndexed { idx, move ->
                                val active = idx + 1 == stepIndex
                                Surface(
                                    onClick = { stepIndex = idx + 1 },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (active) palette.accent else palette.surfaceSecondary,
                                ) {
                                    Text(
                                        text = move,
                                        style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold),
                                        color = if (active) palette.onAccent else palette.textPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SkewbSticker(colorIdx: Int, isCenter: Boolean = false, onClick: () -> Unit) {
    val palette = MonoTheme.palette
    Box(
        modifier = Modifier
            .size(if (isCenter) 28.dp else 24.dp)
            .clip(RoundedCornerShape(if (isCenter) 8.dp else 5.dp))
            .background(skewbColors[colorIdx.coerceIn(0, 5)])
            .border(1.dp, palette.bgMain, RoundedCornerShape(if (isCenter) 8.dp else 5.dp))
            .clickable(onClick = onClick)
    )
}

// -----------------------------------------------------------------------------
// Twisty Puzzle Guides (Megaminx, Square-1, Mirror Cube, Gear Cube, Rubik's Clock)
// -----------------------------------------------------------------------------

private data class PuzzleGuideSection(
    val title: String,
    val subtitle: String,
    val steps: List<Pair<String, String>>,
)

private val puzzleGuides = listOf(
    PuzzleGuideSection(
        title = "Megaminx Guide",
        subtitle = "12-face dodecahedron • S2L & 4-Look Last Layer",
        steps = listOf(
            "1. Star & F2L" to "Solve the white 5-edge star, then insert 5 corner-edge pairs using R U R' and F' U' F triggers.",
            "2. S2L (Second 2 Layers)" to "Build the bottom-half side faces around each colored center before addressing the top grey/last layer.",
            "3. Last Layer Star (EO)" to "F R U R' U' F' (line) or F U R U' R' F' (hook) to orient the 5 top edges.",
            "4. Corner Orient (CO)" to "Use Sune: R U R' U R U2 R' and Anti-Sune: R U2 R' U' R U' R' (or R' D' R D commutator).",
            "5. Edge & Corner Permute" to "Cycle 3 edges: R2 U2' R2' U' R2 U2' R2'. Cycle 3 corners using commutator [R' D' R, U].",
        ),
    ),
    PuzzleGuideSection(
        title = "Square-1 Guide",
        subtitle = "Shape-shifting puzzle • Cube shape, CO, EO, CP, EP & Parity",
        steps = listOf(
            "Notation" to "(u, d) turns top layer u×30° and bottom layer d×30°. '/' executes a 180° slice along the seam.",
            "1. Back to Cube Shape" to "Group all 8 small edges together on the top layer (Scallop-Kite -> Barrel-Barrel -> Shield-Shield -> Cube).",
            "2. Corner & Edge Orientation" to "Separate white/yellow corners, then swap misoriented edges with M2 = (1,0) / (-1,-1) / (0,1).",
            "3. Corner Permutation (CP)" to "Adjacent swap (J-perm): / (3,-3) / (3,0) / (-3,0) / (0,3) / (-3,0) /.",
            "4. Edge Permutation & Parity" to "Adj-Adj swap: / (-3,0) / (0,3) / (0,-3) / (0,3) / (2,0) / (0,2) / (-2,0) / (4,0) / (0,-2) / (0,2) / (-1,4) / (0,-3) / (0,3).",
        ),
    ),
    PuzzleGuideSection(
        title = "Mirror Cube Guide",
        subtitle = "3x3 shape mod solved by block thickness instead of color",
        steps = listOf(
            "Orientation Tip" to "Start with the thinnest face on Bottom (D) or Top (U) so height differences are easiest to feel and inspect.",
            "1. Cross & F2L" to "Identical to 3x3 CFOP/LBL—match flush edges along the center piece rather than matching colors.",
            "2. Center Alignment" to "Every standard 3x3 OLL & PLL algorithm works identically: Sune, FRUR'U'F', T-Perm, U-Perm.",
            "3. Visual Check" to "Run your finger over seams—if an edge sits slightly rotated 180°, it looks tall/short relative to the center.",
        ),
    ),
    PuzzleGuideSection(
        title = "Gear Cube Guide",
        subtitle = "Geared 3x3 where half-turns are 180° and centers couple to edges",
        steps = listOf(
            "1. Solve All 8 Corners" to "Every face turn must be 180° (R2, U2, F2) to keep gears aligned. Position corners like a 2x2 using R2 U2 R2 U2 R2.",
            "2. Permute Edges" to "Swap opposite edges on U/D: (R2 U2)x3 or R2 U R2 U' (with 180° turns: R2 U2 R2 U2 R2).",
            "3. Orient Gears (Center/Edge Spin)" to "Cycle gear teeth until flat: (R U R U R U) using 90°-tooth clicks, or R4 to spin center gears 180°.",
        ),
    ),
    PuzzleGuideSection(
        title = "Rubik's Clock Guide",
        subtitle = "Simultaneous double-sided 9-dial pin puzzle",
        steps = listOf(
            "Pin Rule" to "Pins UP couple the corner dial + adjacent dials. Pins DOWN leave corner dials independent on the back.",
            "1. Front Cross (5 Dials)" to "Align the 4 edges to the center dial one pin at a time (UR -> DR -> DL -> UL -> all 4 pins UP to 12:00).",
            "2. Flip x2 (Top-to-Bottom)" to "Keep the 12:00 axis straight vertically. Repeat cross alignment on the back face.",
            "3. Align Corners" to "Pop down the pin of each non-12:00 corner, turn the remaining UP pins together to match that corner, then push all UP to 12:00.",
        ),
    ),
)

@Composable
fun TwistyPuzzleGuidesScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var selectedGuideIdx by remember { mutableIntStateOf(0) }
    val guide = puzzleGuides[selectedGuideIdx]

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Twisty Puzzle Guides",
            subtitle = "Megaminx, Square-1, Mirror, Gear & Clock",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )

        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(puzzleGuides.indices.toList()) { idx ->
                        MonoChip(
                            label = puzzleGuides[idx].title.removeSuffix(" Guide"),
                            selected = selectedGuideIdx == idx,
                            onClick = { selectedGuideIdx = idx },
                        )
                    }
                }
            }

            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                    Text(guide.title, style = MaterialTheme.typography.headlineMedium, color = palette.textPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(guide.subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                }
            }

            items(guide.steps) { (heading, body) ->
                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    Text(heading, style = MaterialTheme.typography.titleSmall, color = palette.accent, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                }
            }
        }
    }
}
