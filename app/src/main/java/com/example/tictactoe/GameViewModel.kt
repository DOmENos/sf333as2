package com.example.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

class GameViewModel : ViewModel() {
    var state by mutableStateOf(GameState())
    var winCell = 0
    val boardItems: MutableMap<Int, BoardCellValue> = mutableMapOf(
        1 to BoardCellValue.NONE,
        2 to BoardCellValue.NONE,
        3 to BoardCellValue.NONE,
        4 to BoardCellValue.NONE,
        5 to BoardCellValue.NONE,
        6 to BoardCellValue.NONE,
        7 to BoardCellValue.NONE,
        8 to BoardCellValue.NONE,
        9 to BoardCellValue.NONE,
    )

    fun onAction(action: UserAction) {
        when (action) {
            is UserAction.BoardTapped -> handleBoardTapped(action.cellNo)
            UserAction.PlayAgainButtonClicked -> resetGame()
        }
    }

    private fun handleBoardTapped(cellNo: Int) {
        if (boardItems[cellNo] != BoardCellValue.NONE) {
            return
        }

        if (state.currentTurn == BoardCellValue.CIRCLE) {
            handlePlayerCircleTurn(cellNo)
        } else if (state.currentTurn == BoardCellValue.CROSS) {
            handlePlayerCrossTurn(cellNo)
        }
    }

    private fun handlePlayerCircleTurn(cellNo: Int) {
        boardItems[cellNo] = BoardCellValue.CIRCLE
        if (checkForVictory(BoardCellValue.CIRCLE)) {
            handleCircleVictory()
        } else if (hasBoardFull()) {
            handleGameDraw()
        } else {
            state = state.copy(
                hintText = "Computer turn",
                currentTurn = BoardCellValue.CROSS
            )
            computerMove()
        }
    }

    private fun handlePlayerCrossTurn(cellNo: Int) {
        boardItems[cellNo] = BoardCellValue.CROSS
        if (checkForVictory(BoardCellValue.CROSS)) {
            handleCrossVictory()
        } else if (hasBoardFull()) {
            handleGameDraw()
        } else {
            state = state.copy(
                hintText = "Player 'O' turn",
                currentTurn = BoardCellValue.CIRCLE
            )
        }
    }

    private var beginTurn = state.beginTurn
    private fun resetGame() {
        boardItems.forEach { (i, _) ->
            boardItems[i] = BoardCellValue.NONE
        }
        beginTurn = if (beginTurn == BoardCellValue.CIRCLE){
            BoardCellValue.CROSS
        }
        else{
            BoardCellValue.CIRCLE
        }
        changeBeginTurn()

    }
    private fun changeBeginTurn(){
        if(beginTurn == BoardCellValue.CIRCLE){
            state = state.copy(
                hintText = "Player 'O' turn",
                currentTurn = beginTurn,
                victoryType = VictoryType.NONE,
                hasWon = false

            )
        } else{
            state = state.copy(
                hintText = "Computer turn",
                currentTurn = beginTurn,
                victoryType = VictoryType.NONE,
                hasWon = false
            )
            computerMove() //Perform computerMove() after change turn to CROSS
        }
    }


    private fun handleCircleVictory() {
        state = state.copy(
            hintText = "Player 'O' Won",
            playerCircleCount = state.playerCircleCount + 1,
            currentTurn = BoardCellValue.NONE,
            hasWon = true
        )
    }

    private fun handleCrossVictory() {
        state = state.copy(
            hintText = "Computer Won",
            playerCrossCount = state.playerCrossCount + 1, // นี่คือส่วนที่เพิ่มคะแนนสำหรับ X ที่ชนะ
            currentTurn = BoardCellValue.NONE,
            hasWon = true
        )
    }
    public fun hasBoardFull(): Boolean {
        return !boardItems.containsValue(BoardCellValue.NONE)
    }


    private fun addValueToBoard(cellNo: Int) {
        // ตรวจสอบว่าเซลล์ใน cellNo ว่างหรือไม่
        if (boardItems[cellNo] != BoardCellValue.NONE) {
            return
        }

        // กำหนดค่าใน boardItems ตามผู้เล่นปัจจุบัน
        boardItems[cellNo] = state.currentTurn

        // ตรวจสอบการชนะ
        if (checkForVictory(state.currentTurn)) {
            handleVictory(state.currentTurn)
        } else if (hasBoardFull()) {
            handleGameDraw()
        } else {
            // เปลี่ยนเทิร์นของผู้เล่น
            val nextPlayer = if (state.currentTurn == BoardCellValue.CIRCLE) BoardCellValue.CROSS else BoardCellValue.CIRCLE
            val nextPlayerHint = if (nextPlayer == BoardCellValue.CIRCLE) "Player 'O' turn" else "Computer turn"

            state = state.copy(
                currentTurn = nextPlayer,
                hintText = nextPlayerHint
            )

            // เรียก computerMove() หากเป็นตาของคอมพิวเตอร์ (X)
            if (nextPlayer == BoardCellValue.CROSS) {
                computerMove()
            }
        }
    }

    private fun handleVictory(player: BoardCellValue) {
        val hintText = when (player) {
            BoardCellValue.CIRCLE -> "Player 'O' Won"
            BoardCellValue.CROSS -> "Computer Won"
            else -> "Player Won"
        }

        state = state.copy(
            hintText = "Computer Won",
            playerCrossCount = state.playerCrossCount + 1,
            currentTurn = BoardCellValue.NONE,
            hasWon = true
        )
    }

    private fun handleGameDraw() {
        state = state.copy(
            hintText = "Game Draw",
            drawCount = state.drawCount + 1
        )
    }

    private fun canWin(boardValue: BoardCellValue): Boolean {
        //target winCell
        //[1] >> [2][3], [5][9], [4][7]
        //[2] >> [1][3], [5][8]
        //[3] >> [1][2],[5][7], [6][9]
        //[4] >> [1][7], [5][6]
        //[5] >> [2][8], [4][6], [1],[9], [3][7]
        //[6] >> [3][9], [4][5]
        //[7] >> [1][4], [3][5], [8][9]
        //[8] >> [7][9], [2][5]
        //[9] >> [7][8], [1][5], [3][6]
        when {
            ((boardItems[2] == boardValue && boardItems[3] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[4] == boardValue && boardItems[7] == boardValue)) && boardItems[1] == BoardCellValue.NONE -> {
                winCell = 1
                return true
            }
            ((boardItems[1] == boardValue && boardItems[3] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[8] == boardValue)) && boardItems[2] == BoardCellValue.NONE -> {
                winCell = 2
                return true
            }
            ((boardItems[1] == boardValue && boardItems[2] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[7] == boardValue)||
                    (boardItems[6] == boardValue && boardItems[9] == boardValue)) && boardItems[3] == BoardCellValue.NONE -> {
                winCell = 3
                return true
            }
            ((boardItems[1] == boardValue && boardItems[7] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[6] == boardValue)) && boardItems[4] == BoardCellValue.NONE -> {
                winCell = 4
                return true
            }
            ((boardItems[2] == boardValue && boardItems[8] == boardValue)||
                    (boardItems[4] == boardValue && boardItems[6] == boardValue)||
                    (boardItems[1] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[3] == boardValue && boardItems[7] == boardValue)) && boardItems[5] == BoardCellValue.NONE -> {
                winCell = 5
                return true
            }
            ((boardItems[3] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[4] == boardValue && boardItems[5] == boardValue)) && boardItems[6] == BoardCellValue.NONE -> {
                winCell = 6
                return true
            }
            ((boardItems[1] == boardValue && boardItems[4] == boardValue)||
                    (boardItems[3] == boardValue && boardItems[5] == boardValue)||
                    (boardItems[8] == boardValue && boardItems[9] == boardValue)) && boardItems[7] == BoardCellValue.NONE -> {
                winCell = 7
                return true
            }
            ((boardItems[7] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[2] == boardValue && boardItems[5] == boardValue)) && boardItems[8] == BoardCellValue.NONE -> {
                winCell = 8
                return true
            }
            ((boardItems[1] == boardValue && boardItems[5] == boardValue)||
                    (boardItems[3] == boardValue && boardItems[6] == boardValue)||
                    (boardItems[7] == boardValue && boardItems[8] == boardValue)) && boardItems[9] == BoardCellValue.NONE -> {
                winCell = 9
                return true
            }
            else -> return false
        }
    }

    private fun canBlock(): Boolean {
        //Computer's turn so we call canWin() with CIRCLE to check where to block them
        if(canWin(BoardCellValue.CIRCLE)){
            return true
        }
        return false
    }

    private fun middleFree(): Boolean {
        return boardItems[5] == BoardCellValue.NONE
    }

    private fun computerMove() {
        if (canWin(BoardCellValue.CROSS)) {
            addValueToBoard(winCell)
        } else if (canBlock()) {
            addValueToBoard(winCell)
        } else if (middleFree()) {
            addValueToBoard(5)
        } else {
            makeRandomMove()
        }
    }

    private fun makeRandomMove() {
        var randomCell = 0
        while (true) {
            randomCell = Random.nextInt(1, 10)
            if (boardItems[randomCell] == BoardCellValue.NONE) {
                addValueToBoard(randomCell)
                break
            }
        }
    }

    private fun checkForVictory(boardValue: BoardCellValue): Boolean {
        when {
            boardItems[1] == boardValue && boardItems[2] == boardValue && boardItems[3] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL1)
                return true
            }

            boardItems[4] == boardValue && boardItems[5] == boardValue && boardItems[6] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL2)
                return true
            }

            boardItems[7] == boardValue && boardItems[8] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[4] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL1)
                return true
            }

            boardItems[2] == boardValue && boardItems[5] == boardValue && boardItems[8] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL2)
                return true
            }

            boardItems[3] == boardValue && boardItems[6] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[5] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL1)
                return true
            }

            boardItems[3] == boardValue && boardItems[5] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL2)
                return true
            }

            else -> return false
        }
    }
}