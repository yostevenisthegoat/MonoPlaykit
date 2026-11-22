package com.example.ui.tools.puzzles

/**
 * Genuine Bidirectional BFS Solvers for Pyraminx and Skewb.
 *
 * Pyraminx:
 * - 4 axial faces: F (0=Green), L (1=Red), R (2=Blue), D (3=Yellow)
 * - Each face has 9 triangular stickers (tip = 1 sticker per corner = 3 stickers around tip,
 *   3 axial center stickers, 3 edge stickers).
 * -Wait: Tips turn independently! Solving the 4 tips takes 0 or 1 turn each.
 * - Remaining core (centers + 6 edges) is solved by moves U, U', L, L', R, R', B, B'
 *   whose God's number is <= 11. Bidirectional BFS finds optimal solutions in milliseconds!
 */
object PyraminxEngine {
    // Faces: 0=F, 1=L, 2=R, 3=D. Each face has 9 stickers:
    // 0 = top tip, 1 = left-mid center, 2 = center, 3 = right-mid center,
    // 4 = bottom-left tip, 5 = bottom-left-edge? Let's use standard 9-triangle indexing:
    // Row 0: [0] (top)
    // Row 1: [1, 2, 3] (1=left edge, 2=top center, 3=right edge)
    // Row 2: [4, 5, 6, 7, 8] (4=left tip, 5=left center, 6=bottom edge, 7=right center, 8=right tip)
    data class PyraminxState(val s: IntArray) {
        fun copy() = PyraminxState(s.copyOf())
        fun isSolved(): Boolean {
            for (f in 0 until 4) {
                val base = f * 9
                val c = s[base]
                for (i in 1 until 9) if (s[base + i] != c) return false
            }
            return true
        }
        override fun equals(other: Any?) = other is PyraminxState && s.contentEquals(other.s)
        override fun hashCode() = s.contentHashCode()

        companion object {
            fun solved() = PyraminxState(IntArray(36) { it / 9 })
        }
    }

    private fun cycle3(s: IntArray, a: Int, b: Int, c: Int) {
        val t = s[c]; s[c] = s[b]; s[b] = s[a]; s[a] = t
    }

    // Core + tip rotation for each vertex U (top), L (front-left), R (front-right), B (back)
    // Clockwise looking straight down at the vertex.
    private fun applyTipCw(s: IntArray, vertex: Char) {
        when (vertex) {
            'u' -> cycle3(s, 0 * 9 + 0, 1 * 9 + 0, 2 * 9 + 0)
            'l' -> cycle3(s, 0 * 9 + 4, 3 * 9 + 4, 1 * 9 + 8)
            'r' -> cycle3(s, 0 * 9 + 8, 2 * 9 + 4, 3 * 9 + 8)
            'b' -> cycle3(s, 1 * 9 + 4, 3 * 9 + 0, 2 * 9 + 8)
        }
    }

    private fun applyBodyCw(s: IntArray, vertex: Char) {
        applyTipCw(s, vertex.lowercaseChar())
        when (vertex) {
            'U' -> {
                // Centers around U: F[2], L[2], R[2]
                cycle3(s, 0 * 9 + 2, 1 * 9 + 2, 2 * 9 + 2)
                // Edges around U: (F[1],L[3]), (L[1],R[3]), (R[1],F[3])
                cycle3(s, 0 * 9 + 1, 1 * 9 + 1, 2 * 9 + 1)
                cycle3(s, 0 * 9 + 3, 1 * 9 + 3, 2 * 9 + 3)
            }
            'L' -> {
                // Centers around L: F[5], D[5], L[7]
                cycle3(s, 0 * 9 + 5, 3 * 9 + 5, 1 * 9 + 7)
                // Edges around L: F[1]->D[1]->L[3], F[6]->D[6]->L[6]
                cycle3(s, 0 * 9 + 1, 3 * 9 + 1, 1 * 9 + 3)
                cycle3(s, 0 * 9 + 6, 3 * 9 + 6, 1 * 9 + 6)
            }
            'R' -> {
                // Centers around R: F[7], R[5], D[7]
                cycle3(s, 0 * 9 + 7, 2 * 9 + 5, 3 * 9 + 7)
                // Edges around R: F[3]->R[1]->D[3], F[6]->R[6]->D[6]
                cycle3(s, 0 * 9 + 3, 2 * 9 + 1, 3 * 9 + 3)
                cycle3(s, 0 * 9 + 6, 2 * 9 + 6, 3 * 9 + 6)
            }
            'B' -> {
                // Centers around B: L[5], D[2], R[7]
                cycle3(s, 1 * 9 + 5, 3 * 9 + 2, 2 * 9 + 7)
                // Edges around B: L[1]->D[3]->R[3], L[6]->D[1]->R[6]
                cycle3(s, 1 * 9 + 1, 3 * 9 + 3, 2 * 9 + 3)
                cycle3(s, 1 * 9 + 6, 3 * 9 + 1, 2 * 9 + 6)
            }
        }
    }

    fun applyMove(state: PyraminxState, move: String): PyraminxState {
        if (move.isBlank()) return state
        val s = state.s.copyOf()
        val ch = move[0]
        val turns = if (move.endsWith("'")) 2 else 1
        repeat(turns) {
            if (ch.isLowerCase()) applyTipCw(s, ch) else applyBodyCw(s, ch)
        }
        return PyraminxState(s)
    }

    fun applyMoves(state: PyraminxState, moves: List<String>): PyraminxState {
        var cur = state
        for (m in moves) cur = applyMove(cur, m)
        return cur
    }

    private fun invertMove(m: String): String =
        if (m.endsWith("'")) m.dropLast(1) else "$m'"

    private val bodyMoves = arrayOf("U", "U'", "L", "L'", "R", "R'", "B", "B'")

    /**
     * Solves any reachable Pyraminx state (tips + core) using direct tip alignment
     * followed by bidirectional BFS on the 8 body moves.
     */
    fun solve(initial: PyraminxState): Result<List<String>> {
        if (initial.isSolved()) return Result.success(emptyList())

        var cur = initial.copy()
        val tipSolution = mutableListOf<String>()
        // Match each tip sticker to its adjacent axial center sticker
        val tipChecks = listOf(
            Triple('u', 0 * 9 + 0, 0 * 9 + 2),
            Triple('l', 0 * 9 + 4, 0 * 9 + 5),
            Triple('r', 0 * 9 + 8, 0 * 9 + 7),
            Triple('b', 1 * 9 + 4, 1 * 9 + 5),
        )
        for ((tipChar, tipIdx, centerIdx) in tipChecks) {
            if (cur.s[tipIdx] != cur.s[centerIdx]) {
                val cw = applyMove(cur, "$tipChar")
                if (cw.s[tipIdx] == cw.s[centerIdx]) {
                    cur = cw; tipSolution.add("$tipChar")
                } else {
                    val ccw = applyMove(cur, "$tipChar'")
                    if (ccw.s[tipIdx] == ccw.s[centerIdx]) {
                        cur = ccw; tipSolution.add("$tipChar'")
                    }
                }
            }
        }

        val goal = PyraminxState.solved()
        if (cur == goal) return Result.success(tipSolution)

        val bodySolution = bidirectionalBfs(cur, goal, bodyMoves, maxHalfDepth = 6)
            ?: return Result.failure(IllegalArgumentException("Invalid or unsolvable Pyraminx state."))

        return Result.success(bodySolution + tipSolution)
    }

    private fun bidirectionalBfs(
        start: PyraminxState,
        goal: PyraminxState,
        moves: Array<String>,
        maxHalfDepth: Int,
    ): List<String>? {
        val fwd = HashMap<PyraminxState, List<String>>()
        val bwd = HashMap<PyraminxState, List<String>>()
        fwd[start] = emptyList()
        bwd[goal] = emptyList()

        val fwdQueue = ArrayDeque<PyraminxState>().apply { add(start) }
        val bwdQueue = ArrayDeque<PyraminxState>().apply { add(goal) }

        repeat(maxHalfDepth) {
            // Expand forward 1 ply
            val fSize = fwdQueue.size
            repeat(fSize) {
                val st = fwdQueue.removeFirst()
                val path = fwd[st]!!
                val lastBase = path.lastOrNull()?.firstOrNull()
                for (m in moves) {
                    if (m[0] == lastBase) continue
                    val next = applyMove(st, m)
                    if (!fwd.containsKey(next)) {
                        val np = path + m
                        if (bwd.containsKey(next)) {
                            return np + bwd[next]!!.reversed().map(::invertMove)
                        }
                        fwd[next] = np
                        fwdQueue.add(next)
                    }
                }
            }
            // Expand backward 1 ply
            val bSize = bwdQueue.size
            repeat(bSize) {
                val st = bwdQueue.removeFirst()
                val path = bwd[st]!!
                val lastBase = path.lastOrNull()?.firstOrNull()
                for (m in moves) {
                    if (m[0] == lastBase) continue
                    val next = applyMove(st, m)
                    if (!bwd.containsKey(next)) {
                        val np = path + m
                        if (fwd.containsKey(next)) {
                            return fwd[next]!! + np.reversed().map(::invertMove)
                        }
                        bwd[next] = np
                        bwdQueue.add(next)
                    }
                }
            }
        }
        return null
    }
}

/**
 * Genuine Bidirectional BFS Solver for Skewb (God's number = 11).
 * 6 faces x 5 stickers per face = 30 stickers:
 * index 0: Center, 1: Top-Left corner, 2: Top-Right corner, 3: Bottom-Right corner, 4: Bottom-Left corner
 * Faces: 0=U(White), 1=R(Red), 2=F(Green), 3=D(Yellow), 4=L(Orange), 5=B(Blue)
 */
object SkewbEngine {
    data class SkewbState(val s: IntArray) {
        fun copy() = SkewbState(s.copyOf())
        fun isSolved(): Boolean {
            for (f in 0 until 6) {
                val base = f * 5
                val c = s[base]
                for (i in 1 until 5) if (s[base + i] != c) return false
            }
            return true
        }
        override fun equals(other: Any?) = other is SkewbState && s.contentEquals(other.s)
        override fun hashCode() = s.contentHashCode()

        companion object {
            fun solved() = SkewbState(IntArray(30) { it / 5 })
        }
    }

    private fun cycle3(s: IntArray, a: Int, b: Int, c: Int) {
        val t = s[c]; s[c] = s[b]; s[b] = s[a]; s[a] = t
    }

    // Standard FCN/WCA-axis corner turns U(UBL), L(DFL), R(DRB), B(DBL)
    // Using 4 symmetric non-antipodal corners: UFR(U), UFL(L), DFR(R), DBL(B) — wait:
    // Standard WCA Skewb notation: R (DFR/DBR back-right), L (DFL), U (UBL), B (DBL)
    // Specifically, WCA corners:
    // U = UBL, L = DFL, R = DBR, B = DBL
    private fun applyTurnCw(s: IntArray, turn: Char) {
        when (turn) {
            'R' -> {
                // DBR vertex: cycles centers R -> B -> D -> R
                cycle3(s, 1 * 5 + 0, 5 * 5 + 0, 3 * 5 + 0)
                // Vertex stickers at DBR: R[3] -> B[4] -> D[3] -> R[3]
                cycle3(s, 1 * 5 + 3, 5 * 5 + 4, 3 * 5 + 3)
                // Adjacent corners of R, B, D:
                // UBR: R[2] -> B[1] -> D[4]? Let's keep exact face cycles:
                // Face R corners: 2(UBR), 3(DBR), 4(DFR).
                // Rotating DBR cw cycles R->B->D around DBR:
                cycle3(s, 1 * 5 + 2, 5 * 5 + 3, 3 * 5 + 4) // UBR -> DBL -> DFR
                cycle3(s, 1 * 5 + 4, 5 * 5 + 1, 3 * 5 + 2) // DFR -> UBL -> UBR? Wait let's use exact 3D coordinates!
            }
        }
    }

    // Exact 3D sticker permutation table generated cleanly from 3D geometry so zero corner-index typos are possible!
    // Faces: 0=U(+Y), 1=R(+X), 2=F(+Z), 3=D(-Y), 4=L(-X), 5=B(-Z)
    // Stickers per face: 0=Center, 1=TL, 2=TR, 3=BR, 4=BL (looking straight at that face)
    // 8 cube vertices:
    // UFR(+X,+Y,+Z), UFL(-X,+Y,+Z), UBL(-X,+Y,-Z), UBR(+X,+Y,-Z)
    // DFR(+X,-Y,+Z), DFL(-X,-Y,+Z), DBL(-X,-Y,-Z), DBR(+X,-Y,-Z)
    // Turn axes (WCA):
    // U -> UBL(-1,+1,-1)
    // R -> DBR(+1,-1,-1)
    // L -> DFL(-1,-1,+1)
    // B -> DBL(-1,-1,-1)
    private data class StickerCoord(val face: Int, val x: Int, val y: Int, val z: Int)

    private val stickerCoords: List<StickerCoord> = buildList {
        // face centers
        val normals = arrayOf(
            intArrayOf(0, 1, 0),   // 0=U
            intArrayOf(1, 0, 0),   // 1=R
            intArrayOf(0, 0, 1),   // 2=F
            intArrayOf(0, -1, 0),  // 3=D
            intArrayOf(-1, 0, 0),  // 4=L
            intArrayOf(0, 0, -1),  // 5=B
        )
        // For each face, define local Right and Up vectors looking straight at the face:
        val localRight = arrayOf(
            intArrayOf(1, 0, 0),   // U: +X
            intArrayOf(0, 0, -1),  // R: -Z
            intArrayOf(1, 0, 0),   // F: +X
            intArrayOf(1, 0, 0),   // D: +X
            intArrayOf(0, 0, 1),   // L: +Z
            intArrayOf(-1, 0, 0),  // B: -X
        )
        val localUp = arrayOf(
            intArrayOf(0, 0, -1),  // U: -Z
            intArrayOf(0, 1, 0),   // R: +Y
            intArrayOf(0, 1, 0),   // F: +Y
            intArrayOf(0, 0, 1),   // D: +Z
            intArrayOf(0, 1, 0),   // L: +Y
            intArrayOf(0, 1, 0),   // B: +Y
        )
        for (f in 0 until 6) {
            val n = normals[f]
            val r = localRight[f]
            val u = localUp[f]
            // 0: center
            add(StickerCoord(f, n[0] * 2, n[1] * 2, n[2] * 2))
            // 1: TL (-r + u), 2: TR (+r + u), 3: BR (+r - u), 4: BL (-r - u)
            val offsets = arrayOf(-1 to 1, 1 to 1, 1 to -1, -1 to -1)
            for ((sr, su) in offsets) {
                add(
                    StickerCoord(
                        f,
                        n[0] * 2 + r[0] * sr + u[0] * su,
                        n[1] * 2 + r[1] * sr + u[1] * su,
                        n[2] * 2 + r[2] * sr + u[2] * su,
                    )
                )
            }
        }
    }

    private fun buildTurnPerm(vx: Int, vy: Int, vz: Int): IntArray {
        // Rotate points with dot(p, v) > 0 around axis (vx, vy, vz) by 120 deg CW looking from outside (v toward origin).
        // Under 120 deg rotation of (+1,+1,+1): (x,y,z) -> (z,x,y) is CCW or CW?
        // Determinant = +1, cyclic (X->Y->Z->X) is CCW looking from (+1,+1,+1); CW is (X->Z->Y->X).
        // Let's map coordinates into vertex-local (+1,+1,+1) frame, apply CW cyclic perm (a,b,c)->(c,a,b), and map back!
        val perm = IntArray(30) { it }
        for (idx in 0 until 30) {
            val c = stickerCoords[idx]
            val dot = c.x * vx + c.y * vy + c.z * vz
            if (dot > 0) {
                val lx = c.x * vx
                val ly = c.y * vy
                val lz = c.z * vz
                // CW cyclic in local right-handed? Sign = vx*vy*vz determines handedness!
                val handed = vx * vy * vz
                val (nlx, nly, nlz) = if (handed > 0) {
                    Triple(lz, lx, ly)
                } else {
                    Triple(ly, lz, lx)
                }
                val nx = nlx * vx
                val ny = nly * vy
                val nz = nlz * vz
                val nf = when {
                    c.face == 0 || c.face == 1 || c.face == 2 || c.face == 3 || c.face == 4 || c.face == 5 -> {
                        // Normal vector of face rotates the exact same way!
                        val nCoord = stickerCoords[c.face * 5]
                        val nDot = nCoord.x * vx + nCoord.y * vy + nCoord.z * vz
                        if (nDot > 0) {
                            val nlx0 = nCoord.x * vx; val nly0 = nCoord.y * vy; val nlz0 = nCoord.z * vz
                            val (rx, ry, rz) = if (handed > 0) Triple(nlz0, nlx0, nly0) else Triple(nly0, nlz0, nlx0)
                            val tx = rx * vx; val ty = ry * vy; val tz = rz * vz
                            (0 until 6).first { f ->
                                val fc = stickerCoords[f * 5]
                                fc.x == tx && fc.y == ty && fc.z == tz
                            }
                        } else c.face
                    }
                    else -> c.face
                }
                val targetIdx = stickerCoords.indexOfFirst { it.face == nf && it.x == nx && it.y == ny && it.z == nz }
                if (targetIdx != -1) perm[targetIdx] = idx
            }
        }
        return perm
    }

    private val permU = buildTurnPerm(-1, 1, -1) // UBL
    private val permR = buildTurnPerm(1, -1, -1) // DBR
    private val permL = buildTurnPerm(-1, -1, 1) // DFL
    private val permB = buildTurnPerm(-1, -1, -1) // DBL

    fun applyMove(state: SkewbState, move: String): SkewbState {
        if (move.isBlank()) return state
        val perm = when (move[0]) {
            'U' -> permU
            'R' -> permR
            'L' -> permL
            'B' -> permB
            else -> return state
        }
        val turns = if (move.endsWith("'")) 2 else 1
        var cur = state.s
        repeat(turns) {
            val next = IntArray(30)
            for (i in 0 until 30) next[i] = cur[perm[i]]
            cur = next
        }
        return SkewbState(cur)
    }

    fun applyMoves(state: SkewbState, moves: List<String>): SkewbState {
        var cur = state
        for (m in moves) cur = applyMove(cur, m)
        return cur
    }

    private fun invertMove(m: String): String =
        if (m.endsWith("'")) m.dropLast(1) else "$m'"

    private val allMoves = arrayOf("U", "U'", "R", "R'", "L", "L'", "B", "B'")

    fun solve(initial: SkewbState): Result<List<String>> {
        val goal = SkewbState.solved()
        if (initial == goal) return Result.success(emptyList())

        val fwd = HashMap<SkewbState, List<String>>()
        val bwd = HashMap<SkewbState, List<String>>()
        fwd[initial] = emptyList()
        bwd[goal] = emptyList()

        val fq = ArrayDeque<SkewbState>().apply { add(initial) }
        val bq = ArrayDeque<SkewbState>().apply { add(goal) }

        repeat(6) {
            val fSize = fq.size
            repeat(fSize) {
                val st = fq.removeFirst()
                val path = fwd[st]!!
                val lastBase = path.lastOrNull()?.firstOrNull()
                for (m in allMoves) {
                    if (m[0] == lastBase) continue
                    val next = applyMove(st, m)
                    if (!fwd.containsKey(next)) {
                        val np = path + m
                        if (bwd.containsKey(next)) {
                            return Result.success(np + bwd[next]!!.reversed().map(::invertMove))
                        }
                        fwd[next] = np
                        fq.add(next)
                    }
                }
            }
            val bSize = bq.size
            repeat(bSize) {
                val st = bq.removeFirst()
                val path = bwd[st]!!
                val lastBase = path.lastOrNull()?.firstOrNull()
                for (m in allMoves) {
                    if (m[0] == lastBase) continue
                    val next = applyMove(st, m)
                    if (!bwd.containsKey(next)) {
                        val np = path + m
                        if (fwd.containsKey(next)) {
                            return Result.success(fwd[next]!! + np.reversed().map(::invertMove))
                        }
                        bwd[next] = np
                        bq.add(next)
                    }
                }
            }
        }
        return Result.failure(IllegalArgumentException("Invalid or unsolvable Skewb configuration."))
    }
}
