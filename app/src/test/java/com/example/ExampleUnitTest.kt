package com.example

import com.example.ui.tools.boardgames.Chess960Engine
import com.example.ui.tools.puzzles.Cube3x3Engine
import com.example.ui.tools.puzzles.CubeState
import com.example.ui.tools.puzzles.PyraminxEngine
import com.example.ui.tools.puzzles.ScrambleGenerator
import com.example.ui.tools.puzzles.SkewbEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun chess960_positions_respect_fischer_random_rules() {
        for (id in 0 until 960) {
            val rank = Chess960Engine.generatePosition(id)
            assertEquals(8, rank.length)
            val bIndices = rank.indices.filter { rank[it] == 'B' }
            assertEquals(2, bIndices.size)
            assertTrue("Bishops must be on opposite colors", (bIndices[0] % 2) != (bIndices[1] % 2))
            val rIndices = rank.indices.filter { rank[it] == 'R' }
            val kIndex = rank.indexOf('K')
            assertTrue("King must sit between rooks", kIndex in (rIndices[0] + 1) until rIndices[1])
        }
    }

    @Test
    fun cube3x3_solver_solves_random_scrambles() {
        repeat(10) {
            val scramble = ScrambleGenerator.generate3x3(20)
            val scrambled = Cube3x3Engine.applyMoves(CubeState.solved(), scramble)
            val result = Cube3x3Engine.solve(scrambled)
            assertTrue("3x3 solve failed for $scramble: ${result.exceptionOrNull()}", result.isSuccess)
            val moves = result.getOrThrow().flatMap { it.moves }
            val finalState = Cube3x3Engine.applyMoves(scrambled, moves)
            assertTrue("3x3 state not solved after applying solution", finalState.isSolved())
        }
    }

    @Test
    fun pyraminx_solver_solves_random_scrambles() {
        repeat(15) {
            val scramble = ScrambleGenerator.generatePyraminx()
            val scrambled = PyraminxEngine.applyMoves(PyraminxEngine.PyraminxState.solved(), scramble)
            val result = PyraminxEngine.solve(scrambled)
            assertTrue("Pyraminx solve failed: ${result.exceptionOrNull()}", result.isSuccess)
            val solved = PyraminxEngine.applyMoves(scrambled, result.getOrThrow())
            assertTrue("Pyraminx not solved", solved.isSolved())
        }
    }

    @Test
    fun skewb_solver_solves_random_scrambles() {
        repeat(15) {
            val scramble = ScrambleGenerator.generateSkewb()
            val scrambled = SkewbEngine.applyMoves(SkewbEngine.SkewbState.solved(), scramble)
            val result = SkewbEngine.solve(scrambled)
            assertTrue("Skewb solve failed: ${result.exceptionOrNull()}", result.isSuccess)
            val solved = SkewbEngine.applyMoves(scrambled, result.getOrThrow())
            assertTrue("Skewb not solved", solved.isSolved())
        }
    }
}
