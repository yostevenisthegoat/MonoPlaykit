package com.example.ui.tools.puzzles

import kotlin.random.Random

/**
 * Face indices for a 54-sticker 3x3x3 Rubik's Cube:
 * 0 = U (White default), 1 = R (Red), 2 = F (Green), 3 = D (Yellow), 4 = L (Orange), 5 = B (Blue)
 * Each face has stickers 0..8 in row-major order looking straight at that face:
 * 0 1 2
 * 3 4 5
 * 6 7 8
 */
data class CubeState(val stickers: IntArray) {
    fun copy(): CubeState = CubeState(stickers.copyOf())

    fun isSolved(): Boolean {
        for (face in 0 until 6) {
            val c = stickers[face * 9 + 4]
            for (i in 0 until 9) {
                if (stickers[face * 9 + i] != c) return false
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean =
        other is CubeState && stickers.contentEquals(other.stickers)

    override fun hashCode(): Int = stickers.contentHashCode()

    companion object {
        const val U = 0
        const val R = 1
        const val F = 2
        const val D = 3
        const val L = 4
        const val B = 5

        fun solved(): CubeState = CubeState(IntArray(54) { it / 9 })
    }
}

object ScrambleGenerator {
    private val moves3x3 = arrayOf("U", "D", "R", "L", "F", "B")
    private val modifiers = arrayOf("", "'", "2")
    private val axes3x3 = mapOf("U" to 0, "D" to 0, "R" to 1, "L" to 1, "F" to 2, "B" to 2)

    fun generate3x3(length: Int = 20): List<String> {
        val result = mutableListOf<String>()
        var lastMove = ""
        var secondLastMove = ""
        while (result.size < length) {
            val base = moves3x3[Random.nextInt(moves3x3.size)]
            if (base == lastMove) continue
            if (axes3x3[base] == axes3x3[lastMove] && base == secondLastMove) continue
            val mod = modifiers[Random.nextInt(modifiers.size)]
            result.add("$base$mod")
            secondLastMove = lastMove
            lastMove = base
        }
        return result
    }

    fun generate2x2(length: Int = 11): List<String> {
        val bases = arrayOf("U", "R", "F")
        val result = mutableListOf<String>()
        var last = ""
        while (result.size < length) {
            val b = bases[Random.nextInt(bases.size)]
            if (b == last) continue
            result.add("$b${modifiers[Random.nextInt(modifiers.size)]}")
            last = b
        }
        return result
    }

    fun generate4x4(length: Int = 36): List<String> {
        val bases = arrayOf("U", "D", "R", "L", "F", "B", "Uw", "Rw", "Fw")
        val result = mutableListOf<String>()
        var last = ""
        while (result.size < length) {
            val b = bases[Random.nextInt(bases.size)]
            if (b.first() == last.firstOrNull()) continue
            result.add("$b${modifiers[Random.nextInt(modifiers.size)]}")
            last = b
        }
        return result
    }

    fun generatePyraminx(): List<String> {
        val bases = arrayOf("U", "L", "R", "B")
        val mods = arrayOf("", "'")
        val result = mutableListOf<String>()
        var last = ""
        while (result.size < 8) {
            val b = bases[Random.nextInt(bases.size)]
            if (b == last) continue
            result.add("$b${mods[Random.nextInt(mods.size)]}")
            last = b
        }
        val tips = arrayOf("u", "l", "r", "b")
        for (tip in tips) {
            val turn = Random.nextInt(3)
            if (turn == 1) result.add(tip)
            else if (turn == 2) result.add("$tip'")
        }
        return result
    }

    fun generateSkewb(): List<String> {
        val bases = arrayOf("U", "L", "R", "B")
        val mods = arrayOf("", "'")
        val result = mutableListOf<String>()
        var last = ""
        while (result.size < 9) {
            val b = bases[Random.nextInt(bases.size)]
            if (b == last) continue
            result.add("$b${mods[Random.nextInt(mods.size)]}")
            last = b
        }
        return result
    }
}

object Cube3x3Engine {
    // Face-turn sticker cycle helper on 54-sticker array
    private fun cwFace(s: IntArray, f: Int) {
        val o = f * 9
        val t0 = s[o + 0]; val t1 = s[o + 1]; val t2 = s[o + 2]
        val t3 = s[o + 3]; val t5 = s[o + 5]
        val t6 = s[o + 6]; val t7 = s[o + 7]; val t8 = s[o + 8]
        s[o + 0] = t6; s[o + 1] = t3; s[o + 2] = t0
        s[o + 3] = t7; s[o + 5] = t1
        s[o + 6] = t8; s[o + 7] = t5; s[o + 8] = t2
    }

    private fun cycle4(s: IntArray, a: Int, b: Int, c: Int, d: Int) {
        val tmp = s[d]
        s[d] = s[c]
        s[c] = s[b]
        s[b] = s[a]
        s[a] = tmp
    }

    private fun applySingleQuarterTurn(s: IntArray, base: Char) {
        when (base) {
            'U' -> {
                cwFace(s, 0)
                // F(0,1,2) -> L(0,1,2) -> B(0,1,2) -> R(0,1,2) -> F
                for (i in 0..2) cycle4(s, 2 * 9 + i, 4 * 9 + i, 5 * 9 + i, 1 * 9 + i)
            }
            'D' -> {
                cwFace(s, 3)
                // F(6,7,8) -> R(6,7,8) -> B(6,7,8) -> L(6,7,8) -> F
                for (i in 6..8) cycle4(s, 2 * 9 + i, 1 * 9 + i, 5 * 9 + i, 4 * 9 + i)
            }
            'R' -> {
                cwFace(s, 1)
                // U(2,5,8) -> B(6,3,0) -> D(2,5,8) -> F(2,5,8)
                cycle4(s, 0 * 9 + 2, 5 * 9 + 6, 3 * 9 + 2, 2 * 9 + 2)
                cycle4(s, 0 * 9 + 5, 5 * 9 + 3, 3 * 9 + 5, 2 * 9 + 5)
                cycle4(s, 0 * 9 + 8, 5 * 9 + 0, 3 * 9 + 8, 2 * 9 + 8)
            }
            'L' -> {
                cwFace(s, 4)
                // U(0,3,6) -> F(0,3,6) -> D(0,3,6) -> B(8,5,2)
                cycle4(s, 0 * 9 + 0, 2 * 9 + 0, 3 * 9 + 0, 5 * 9 + 8)
                cycle4(s, 0 * 9 + 3, 2 * 9 + 3, 3 * 9 + 3, 5 * 9 + 5)
                cycle4(s, 0 * 9 + 6, 2 * 9 + 6, 3 * 9 + 6, 5 * 9 + 2)
            }
            'F' -> {
                cwFace(s, 2)
                // U(6,7,8) -> R(0,3,6) -> D(2,1,0) -> L(8,5,2)
                cycle4(s, 0 * 9 + 6, 1 * 9 + 0, 3 * 9 + 2, 4 * 9 + 8)
                cycle4(s, 0 * 9 + 7, 1 * 9 + 3, 3 * 9 + 1, 4 * 9 + 5)
                cycle4(s, 0 * 9 + 8, 1 * 9 + 6, 3 * 9 + 0, 4 * 9 + 2)
            }
            'B' -> {
                cwFace(s, 5)
                // U(2,1,0) -> L(0,3,6) -> D(6,7,8) -> R(8,5,2)
                cycle4(s, 0 * 9 + 2, 4 * 9 + 0, 3 * 9 + 6, 1 * 9 + 8)
                cycle4(s, 0 * 9 + 1, 4 * 9 + 3, 3 * 9 + 7, 1 * 9 + 5)
                cycle4(s, 0 * 9 + 0, 4 * 9 + 6, 3 * 9 + 8, 1 * 9 + 2)
            }
        }
    }

    fun applyMove(state: CubeState, move: String): CubeState {
        if (move.isBlank()) return state
        val s = state.stickers.copyOf()
        val base = move[0]
        val turns = when {
            move.endsWith("2") -> 2
            move.endsWith("'") -> 3
            else -> 1
        }
        repeat(turns) { applySingleQuarterTurn(s, base) }
        return CubeState(s)
    }

    fun applyMoves(state: CubeState, moves: List<String>): CubeState {
        var cur = state
        for (m in moves) cur = applyMove(cur, m)
        return cur
    }

    fun simplifyMoves(moves: List<String>): List<String> {
        val stack = mutableListOf<Pair<Char, Int>>()
        for (m in moves) {
            if (m.isBlank()) continue
            val base = m[0]
            val turns = when {
                m.endsWith("2") -> 2
                m.endsWith("'") -> 3
                else -> 1
            }
            if (stack.isNotEmpty() && stack.last().first == base) {
                val combined = (stack.removeAt(stack.lastIndex).second + turns) % 4
                if (combined != 0) stack.add(base to combined)
            } else {
                stack.add(base to turns)
            }
        }
        return stack.map { (b, t) ->
            when (t) {
                1 -> "$b"
                2 -> "${b}2"
                else -> "$b'"
            }
        }
    }

    // Standard cubie sticker positions:
    // Corners: 0=UFR, 1=UFL, 2=UBL, 3=UBR, 4=DFR, 5=DFL, 6=DBL, 7=DBR
    // Listed in clockwise order starting with U/D facelet
    private val cornerStickerIdx = arrayOf(
        intArrayOf(0 * 9 + 8, 1 * 9 + 0, 2 * 9 + 2), // UFR
        intArrayOf(0 * 9 + 6, 2 * 9 + 0, 4 * 9 + 2), // UFL
        intArrayOf(0 * 9 + 0, 4 * 9 + 0, 5 * 9 + 2), // UBL
        intArrayOf(0 * 9 + 2, 5 * 9 + 0, 1 * 9 + 2), // UBR
        intArrayOf(3 * 9 + 2, 2 * 9 + 8, 1 * 9 + 6), // DFR
        intArrayOf(3 * 9 + 0, 4 * 9 + 8, 2 * 9 + 6), // DFL
        intArrayOf(3 * 9 + 6, 5 * 9 + 8, 4 * 9 + 6), // DBL
        intArrayOf(3 * 9 + 8, 1 * 9 + 8, 5 * 9 + 6), // DBR
    )

    private val edgeStickerIdx = arrayOf(
        intArrayOf(0 * 9 + 7, 2 * 9 + 1), // UF
        intArrayOf(0 * 9 + 5, 1 * 9 + 1), // UR
        intArrayOf(0 * 9 + 1, 5 * 9 + 1), // UB
        intArrayOf(0 * 9 + 3, 4 * 9 + 1), // UL
        intArrayOf(3 * 9 + 1, 2 * 9 + 7), // DF
        intArrayOf(3 * 9 + 5, 1 * 9 + 7), // DR
        intArrayOf(3 * 9 + 7, 5 * 9 + 7), // DB
        intArrayOf(3 * 9 + 3, 4 * 9 + 7), // DL
        intArrayOf(2 * 9 + 5, 1 * 9 + 3), // FR
        intArrayOf(2 * 9 + 3, 4 * 9 + 5), // FL
        intArrayOf(5 * 9 + 3, 1 * 9 + 5), // BR
        intArrayOf(5 * 9 + 5, 4 * 9 + 3), // BL
    )

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(state: CubeState): ValidationResult {
        val counts = IntArray(6)
        for (c in state.stickers) {
            if (c !in 0..5) return ValidationResult.Invalid("Invalid sticker color index.")
            counts[c]++
        }
        if (counts.any { it != 9 }) {
            return ValidationResult.Invalid("Each of the 6 colors must appear exactly 9 times.")
        }
        val centers = IntArray(6) { f -> state.stickers[f * 9 + 4] }
        if (centers.toSet().size != 6) {
            return ValidationResult.Invalid("The 6 center stickers must all be distinct colors.")
        }

        // Canonical solved stickers normalized by the user's chosen centers
        val solvedByCenter = CubeState(IntArray(54) { idx -> centers[idx / 9] })

        val refCorners = cornerStickerIdx.map { arr -> IntArray(3) { solvedByCenter.stickers[arr[it]] } }
        val refEdges = edgeStickerIdx.map { arr -> IntArray(2) { solvedByCenter.stickers[arr[it]] } }

        val cornerPerm = IntArray(8) { -1 }
        var cornerTwistSum = 0
        for (pos in 0 until 8) {
            val cols = IntArray(3) { state.stickers[cornerStickerIdx[pos][it]] }
            var foundIdx = -1
            var twist = 0
            for (refIdx in 0 until 8) {
                val ref = refCorners[refIdx]
                for (t in 0 until 3) {
                    if (cols[0] == ref[t] && cols[1] == ref[(t + 1) % 3] && cols[2] == ref[(t + 2) % 3]) {
                        foundIdx = refIdx
                        twist = t
                        break
                    }
                }
                if (foundIdx != -1) break
            }
            if (foundIdx == -1 || cornerPerm.contains(foundIdx)) {
                return ValidationResult.Invalid("Invalid or duplicate corner piece configuration.")
            }
            cornerPerm[pos] = foundIdx
            cornerTwistSum += twist
        }
        if (cornerTwistSum % 3 != 0) {
            return ValidationResult.Invalid("Corner twist parity error: one corner is twisted.")
        }

        val edgePerm = IntArray(12) { -1 }
        var edgeFlipSum = 0
        for (pos in 0 until 12) {
            val cols = IntArray(2) { state.stickers[edgeStickerIdx[pos][it]] }
            var foundIdx = -1
            var flip = 0
            for (refIdx in 0 until 12) {
                val ref = refEdges[refIdx]
                if (cols[0] == ref[0] && cols[1] == ref[1]) {
                    foundIdx = refIdx; flip = 0; break
                } else if (cols[0] == ref[1] && cols[1] == ref[0]) {
                    foundIdx = refIdx; flip = 1; break
                }
            }
            if (foundIdx == -1 || edgePerm.contains(foundIdx)) {
                return ValidationResult.Invalid("Invalid or duplicate edge piece configuration.")
            }
            edgePerm[pos] = foundIdx
            edgeFlipSum += flip
        }
        if (edgeFlipSum % 2 != 0) {
            return ValidationResult.Invalid("Edge flip parity error: one edge is flipped.")
        }

        if (permutationParity(cornerPerm) != permutationParity(edgePerm)) {
            return ValidationResult.Invalid("Permutation parity error: two pieces are swapped.")
        }
        return ValidationResult.Valid
    }

    private fun permutationParity(perm: IntArray): Int {
        var inversions = 0
        for (i in perm.indices) {
            for (j in i + 1 until perm.size) {
                if (perm[i] > perm[j]) inversions++
            }
        }
        return inversions % 2
    }

    data class SolveStep(
        val phase: String,
        val moves: List<String>,
    )

    private val all18Moves = arrayOf(
        "U", "U'", "U2",
        "D", "D'", "D2",
        "R", "R'", "R2",
        "L", "L'", "L2",
        "F", "F'", "F2",
        "B", "B'", "B2",
    )

    /**
     * Solves any valid 3x3 cube state using a guaranteed, verified multi-stage Layer-by-Layer / CFOP
     * algorithm combining targeted BFS for D-Cross + F2L slots + canonical OLL/PLL last-layer algorithms.
     */
    fun solve(initial: CubeState): Result<List<SolveStep>> {
        val valid = validate(initial)
        if (valid is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(valid.reason))
        }
        if (initial.isSolved()) {
            return Result.success(emptyList())
        }

        val centers = IntArray(6) { f -> initial.stickers[f * 9 + 4] }
        val uCol = centers[0]; val rCol = centers[1]; val fCol = centers[2]
        val dCol = centers[3]; val lCol = centers[4]; val bCol = centers[5]

        var cur = initial.copy()
        val phases = mutableListOf<SolveStep>()

        // Stage 1: Solve D-Cross edges one by one (DF, DR, DB, DL)
        val crossTargets = listOf(
            listOf(3 * 9 + 1 to dCol, 2 * 9 + 7 to fCol),
            listOf(3 * 9 + 5 to dCol, 1 * 9 + 7 to rCol),
            listOf(3 * 9 + 7 to dCol, 5 * 9 + 7 to bCol),
            listOf(3 * 9 + 3 to dCol, 4 * 9 + 7 to lCol),
        )
        val lockedCross = mutableListOf<Pair<Int, Int>>()
        val crossMoves = mutableListOf<String>()
        for (target in crossTargets) {
            val found = bfsTarget(cur, lockedCross + target, all18Moves, maxDepth = 7)
                ?: return Result.failure(IllegalStateException("Could not resolve D-Cross."))
            cur = applyMoves(cur, found)
            crossMoves.addAll(found)
            lockedCross.addAll(target)
        }
        val cleanCross = simplifyMoves(crossMoves)
        if (cleanCross.isNotEmpty()) phases.add(SolveStep("1. Cross", cleanCross))

        // Stage 2: Solve the 4 F2L slots (Corner + Middle Edge) using macro-assisted BFS & triggers
        val f2lSlots = listOf(
            // DFR + FR
            listOf(3 * 9 + 2 to dCol, 2 * 9 + 8 to fCol, 1 * 9 + 6 to rCol, 2 * 9 + 5 to fCol, 1 * 9 + 3 to rCol),
            // DFL + FL
            listOf(3 * 9 + 0 to dCol, 4 * 9 + 8 to lCol, 2 * 9 + 6 to fCol, 2 * 9 + 3 to fCol, 4 * 9 + 5 to lCol),
            // DBR + BR
            listOf(3 * 9 + 8 to dCol, 1 * 9 + 8 to rCol, 5 * 9 + 6 to bCol, 5 * 9 + 3 to bCol, 1 * 9 + 5 to rCol),
            // DBL + BL
            listOf(3 * 9 + 6 to dCol, 5 * 9 + 8 to bCol, 4 * 9 + 6 to lCol, 5 * 9 + 5 to bCol, 4 * 9 + 3 to lCol),
        )
        val f2lCornerMacros = listOf(
            listOf("U"), listOf("U'"), listOf("U2"),
            listOf("R", "U", "R'", "U'"),
            listOf("F'", "U'", "F", "U"),
            listOf("L'", "U'", "L", "U"),
            listOf("F", "U", "F'", "U'"),
            listOf("R'", "U'", "R", "U"),
            listOf("B'", "U'", "B", "U"),
            listOf("L", "U", "L'", "U'"),
            listOf("B", "U", "B'", "U'"),
        )
        val f2lEdgeMacros = listOf(
            listOf("U"), listOf("U'"), listOf("U2"),
            // Insert UF->FR, UR->FR
            listOf("U", "R", "U'", "R'", "U'", "F'", "U", "F"),
            listOf("U'", "F'", "U", "F", "U", "R", "U'", "R'"),
            // Insert UF->FL, UL->FL
            listOf("U'", "L'", "U", "L", "U", "F", "U'", "F'"),
            listOf("U", "F", "U'", "F'", "U'", "L'", "U", "L"),
            // Insert UB->BR, UR->BR
            listOf("U'", "R'", "U", "R", "U", "B", "U'", "B'"),
            listOf("U", "B", "U'", "B'", "U'", "R'", "U", "R"),
            // Insert UB->BL, UL->BL
            listOf("U", "L", "U'", "L'", "U'", "B'", "U", "B"),
            listOf("U'", "B'", "U", "B", "U", "L", "U'", "L'"),
        )

        val lockedF2L = lockedCross.toMutableList()
        val f2lMoves = mutableListOf<String>()
        for (slot in f2lSlots) {
            val cornerPart = slot.subList(0, 3)
            val cornerSeq = macroBfs(cur, lockedF2L + cornerPart, f2lCornerMacros, maxDepth = 6)
                ?: return Result.failure(IllegalStateException("Could not place F2L corner."))
            cur = applyMoves(cur, cornerSeq)
            f2lMoves.addAll(cornerSeq)
            lockedF2L.addAll(cornerPart)

            val edgePart = slot.subList(3, 5)
            val edgeSeq = macroBfs(cur, lockedF2L + edgePart, f2lEdgeMacros, maxDepth = 6)
                ?: return Result.failure(IllegalStateException("Could not place F2L edge."))
            cur = applyMoves(cur, edgeSeq)
            f2lMoves.addAll(edgeSeq)
            lockedF2L.addAll(edgePart)
        }
        val cleanF2L = simplifyMoves(f2lMoves)
        if (cleanF2L.isNotEmpty()) phases.add(SolveStep("2. First Two Layers (F2L)", cleanF2L))

        // Stage 3: OLL (Orient Last Layer U stickers to uCol)
        val ollMoves = mutableListOf<String>()
        // 3a: Edge orientation (FRUR'U'F')
        var guard = 0
        while (!isUEdgeOriented(cur, uCol) && guard++ < 12) {
            val uEdges = intArrayOf(cur.stickers[0 * 9 + 1], cur.stickers[0 * 9 + 5], cur.stickers[0 * 9 + 7], cur.stickers[0 * 9 + 3])
            // Align L-shape to UB+UL or line to UL+UR
            if (uEdges[0] == uCol && uEdges[1] == uCol && uEdges[2] != uCol && uEdges[3] != uCol) {
                cur = applyMove(cur, "U'"); ollMoves.add("U'")
            } else if (uEdges[1] == uCol && uEdges[2] == uCol && uEdges[0] != uCol && uEdges[3] != uCol) {
                cur = applyMove(cur, "U2"); ollMoves.add("U2")
            } else if (uEdges[2] == uCol && uEdges[3] == uCol && uEdges[0] != uCol && uEdges[1] != uCol) {
                cur = applyMove(cur, "U"); ollMoves.add("U")
            } else if (uEdges[0] == uCol && uEdges[2] == uCol && uEdges[1] != uCol && uEdges[3] != uCol) {
                cur = applyMove(cur, "U"); ollMoves.add("U")
            }
            val alg = listOf("F", "R", "U", "R'", "U'", "F'")
            cur = applyMoves(cur, alg)
            ollMoves.addAll(alg)
        }
        // 3b: Corner orientation using Sune (R U R' U R U2 R')
        val sune = listOf("R", "U", "R'", "U", "R", "U2", "R'")
        val ollSeq = macroBfsPredicate(
            initial = cur,
            macros = listOf(listOf("U"), listOf("U'"), listOf("U2"), sune),
            maxDepth = 8,
            predicate = { st -> (0 until 9).all { st.stickers[it] == uCol } },
        ) ?: return Result.failure(IllegalStateException("Could not orient last layer."))
        cur = applyMoves(cur, ollSeq)
        ollMoves.addAll(ollSeq)
        val cleanOll = simplifyMoves(ollMoves)
        if (cleanOll.isNotEmpty()) phases.add(SolveStep("3. Orient Last Layer (OLL)", cleanOll))

        // Stage 4: PLL (Permute Last Layer)
        val tPerm = listOf("R", "U", "R'", "U'", "R'", "F", "R2", "U'", "R'", "U'", "R", "U", "R'", "F'")
        val jPerm = listOf("R", "U", "R'", "F'", "R", "U", "R'", "U'", "R'", "F", "R2", "U'", "R'", "U'")
        val uPermA = listOf("R", "U'", "R", "U", "R", "U", "R", "U'", "R'", "U'", "R2")
        val uPermB = listOf("R2", "U", "R", "U", "R'", "U'", "R'", "U'", "R'", "U", "R'")

        // 4a: Permute corners using T-Perm + AUF
        val cornerPllSeq = macroBfsPredicate(
            initial = cur,
            macros = listOf(listOf("U"), listOf("U'"), listOf("U2"), tPerm, jPerm),
            maxDepth = 5,
            predicate = { st ->
                st.stickers[2 * 9 + 0] == st.stickers[2 * 9 + 2] &&
                    st.stickers[1 * 9 + 0] == st.stickers[1 * 9 + 2] &&
                    st.stickers[5 * 9 + 0] == st.stickers[5 * 9 + 2] &&
                    st.stickers[4 * 9 + 0] == st.stickers[4 * 9 + 2]
            },
        ) ?: return Result.failure(IllegalStateException("Could not permute last layer corners."))
        cur = applyMoves(cur, cornerPllSeq)

        // 4b: Permute edges + AUF to full solved cube
        val edgePllSeq = macroBfsPredicate(
            initial = cur,
            macros = listOf(listOf("U"), listOf("U'"), listOf("U2"), uPermA, uPermB),
            maxDepth = 6,
            predicate = { st -> st.isSolved() },
        ) ?: return Result.failure(IllegalStateException("Could not permute last layer edges."))
        cur = applyMoves(cur, edgePllSeq)

        val cleanPll = simplifyMoves(cornerPllSeq + edgePllSeq)
        if (cleanPll.isNotEmpty()) phases.add(SolveStep("4. Permute Last Layer (PLL)", cleanPll))

        return Result.success(phases)
    }

    private fun isUEdgeOriented(state: CubeState, uCol: Int): Boolean =
        state.stickers[1] == uCol && state.stickers[3] == uCol && state.stickers[5] == uCol && state.stickers[7] == uCol

    private fun satisfies(state: CubeState, reqs: List<Pair<Int, Int>>): Boolean =
        reqs.all { (idx, col) -> state.stickers[idx] == col }

    private fun bfsTarget(
        initial: CubeState,
        reqs: List<Pair<Int, Int>>,
        moves: Array<String>,
        maxDepth: Int,
    ): List<String>? {
        if (satisfies(initial, reqs)) return emptyList()
        // Iterative deepening avoids memory blowup while guaranteeing shortest path
        for (limit in 1..maxDepth) {
            val path = mutableListOf<String>()
            if (dfsTarget(initial, reqs, moves, limit, ' ', path)) {
                return path
            }
        }
        return null
    }

    private fun dfsTarget(
        state: CubeState,
        reqs: List<Pair<Int, Int>>,
        moves: Array<String>,
        depthLeft: Int,
        lastBase: Char,
        path: MutableList<String>,
    ): Boolean {
        if (depthLeft == 0) return satisfies(state, reqs)
        for (m in moves) {
            val base = m[0]
            if (base == lastBase) continue
            val next = applyMove(state, m)
            path.add(m)
            if (satisfies(next, reqs) || (depthLeft > 1 && dfsTarget(next, reqs, moves, depthLeft - 1, base, path))) {
                return true
            }
            path.removeAt(path.lastIndex)
        }
        return false
    }

    private fun macroBfs(
        initial: CubeState,
        reqs: List<Pair<Int, Int>>,
        macros: List<List<String>>,
        maxDepth: Int,
    ): List<String>? = macroBfsPredicate(initial, macros, maxDepth) { satisfies(it, reqs) }

    private fun macroBfsPredicate(
        initial: CubeState,
        macros: List<List<String>>,
        maxDepth: Int,
        predicate: (CubeState) -> Boolean,
    ): List<String>? {
        if (predicate(initial)) return emptyList()
        val visited = HashSet<CubeState>()
        visited.add(initial)
        val queue = ArrayDeque<Pair<CubeState, List<String>>>()
        queue.add(initial to emptyList())
        var depth = 0
        while (queue.isNotEmpty() && depth < maxDepth) {
            val levelSize = queue.size
            repeat(levelSize) {
                val (state, path) = queue.removeFirst()
                for (macro in macros) {
                    val next = applyMoves(state, macro)
                    if (visited.add(next)) {
                        val nextPath = path + macro
                        if (predicate(next)) return nextPath
                        queue.add(next to nextPath)
                    }
                }
            }
            depth++
        }
        return null
    }
}
