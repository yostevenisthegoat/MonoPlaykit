package com.example.ui.tools.boardgames

object Chess960Engine {
    /**
     * Generates the authentic Chess960 (Fischer Random) starting back-rank for index 0..959
     * using Scharnagl's algorithm:
     * 1. Light-squared bishop on b1/d1/f1/h1
     * 2. Dark-squared bishop on a1/c1/e1/g1
     * 3. Queen on one of remaining 6 squares
     * 4. Two Knights on remaining 5 squares (10 combinations)
     * 5. Rook - King - Rook on remaining 3 squares (King always between Rooks)
     */
    fun generatePosition(id: Int): String {
        val n = id.coerceIn(0, 959)
        val board = CharArray(8) { ' ' }

        val n2 = n / 4
        val b1 = n % 4
        board[b1 * 2 + 1] = 'B'

        val n3 = n2 / 4
        val b2 = n2 % 4
        board[b2 * 2] = 'B'

        val n4 = n3 / 6
        val q = n3 % 6
        placeInNthEmpty(board, q, 'Q')

        val knightTable = arrayOf(
            0 to 1, 0 to 2, 0 to 3, 0 to 4,
            1 to 2, 1 to 3, 1 to 4,
            2 to 3, 2 to 4,
            3 to 4,
        )
        val (k1, k2) = knightTable[n4]
        placeInNthEmpty(board, k2, 'N')
        placeInNthEmpty(board, k1, 'N')

        // Remaining 3 empty squares get R, K, R in order
        val rkr = charArrayOf('R', 'K', 'R')
        var idx = 0
        for (i in 0 until 8) {
            if (board[i] == ' ') {
                board[i] = rkr[idx++]
            }
        }
        return String(board)
    }

    private fun placeInNthEmpty(board: CharArray, targetEmptyIndex: Int, piece: Char) {
        var count = 0
        for (i in board.indices) {
            if (board[i] == ' ') {
                if (count == targetEmptyIndex) {
                    board[i] = piece
                    return
                }
                count++
            }
        }
    }

    fun pieceSymbol(ch: Char): String = when (ch) {
        'K' -> "♔"
        'Q' -> "♕"
        'R' -> "♖"
        'B' -> "♗"
        'N' -> "♘"
        else -> ch.toString()
    }
}
