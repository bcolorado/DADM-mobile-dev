package com.example.reto3tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reto3tictactoe.ui.theme.Reto3TicTacToeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Reto3TicTacToeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TicTacToeGame()
                }
            }
        }
    }
}

@Composable
fun TicTacToeGame() {
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var message by remember { mutableStateOf("Tú turno!") }
    var playerWins by remember { mutableStateOf(0) }
    var aiWins by remember { mutableStateOf(0) }
    var draws by remember { mutableStateOf(0) } // Contador de empates
    var aiDifficulty by remember { mutableStateOf("Difícil") }

    // Estados para los diálogos
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    fun checkWinner(): String? {
        // Filas y columnas
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0].isNotEmpty())
                return board[i][0]
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i].isNotEmpty())
                return board[0][i]
        }
        // Diagonales
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0].isNotEmpty())
            return board[0][0]
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2].isNotEmpty())
            return board[0][2]
        // Empate
        if (board.flatten().all { it.isNotEmpty() })
            return "Empate"
        return null
    }

    // Dificultad Fácil: Juega al azar
    fun aiMoveEasy() {
        val emptyCells = board.flatMapIndexed { rowIndex, row ->
            row.mapIndexedNotNull { colIndex, value ->
                if (value.isEmpty()) Pair(rowIndex, colIndex) else null
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (row, col) = emptyCells.random()
            board[row][col] = "O"
        }
    }

    // Dificultad Normal: Prioriza bloqueo/ganar pero sin estrategias avanzadas
    fun aiMoveNormal() {
        // Intentar ganar
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = "O"
                    if (checkWinner() == "O") return
                    board[i][j] = ""
                }
            }
        }
        // Bloquear al jugador
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = "X"
                    if (checkWinner() == "X") {
                        board[i][j] = "O"
                        return
                    }
                    board[i][j] = ""
                }
            }
        }
        // Si no hay nada crítico, jugar al azar
        aiMoveEasy()
    }

    // Dificultad Difícil: Implementa estrategias avanzadas
    fun aiMoveHard() {
        // Verificar si la IA puede ganar
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = "O" // Marcar temporalmente
                    if (checkWinner() == "O") return // Realizar movimiento ganador
                    board[i][j] = "" // Deshacer movimiento
                }
            }
        }

        // Verificar si el jugador puede ganar y bloquear
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j].isEmpty()) {
                    board[i][j] = "X" // Simular movimiento del jugador
                    if (checkWinner() == "X") {
                        board[i][j] = "O" // Bloquear al jugador
                        return
                    }
                    board[i][j] = "" // Deshacer movimiento
                }
            }
        }

        // Priorizar el centro
        if (board[1][1].isEmpty()) {
            board[1][1] = "O"
            return
        }

        // Priorizar esquinas vacías
        val corners = listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
        for ((x, y) in corners) {
            if (board[x][y].isEmpty()) {
                board[x][y] = "O"
                return
            }
        }

        // Jugar en los bordes si están vacíos
        val edges = listOf(Pair(0, 1), Pair(1, 0), Pair(1, 2), Pair(2, 1))
        for ((x, y) in edges) {
            if (board[x][y].isEmpty()) {
                board[x][y] = "O"
                return
            }
        }
    }

    fun aiMove() {
        when (aiDifficulty) {
            "Fácil" -> aiMoveEasy()
            "Normal" -> aiMoveNormal()
            else -> aiMoveHard()
        }
    }

    fun resetBoard() {
        board = List(3) { MutableList(3) { "" } }
        currentPlayer = "X"
        message = "Tú turno!"
    }


        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tic Tac Toe",
                    fontSize = 32.sp,
                    color = Color(0xFFFFF7C2),
                    modifier = Modifier.padding(16.dp).padding(top = 64.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Jugador 😎: $playerWins", fontSize = 18.sp)
                            Text(text = "IA 🤖: $aiWins", fontSize = 18.sp)
                            Text(text = "Empates 😮: $draws", fontSize = 18.sp) // Mostrar empates
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Tablero
                        for (i in 0..2) {
                            Row {
                                for (j in 0..2) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(4.dp)
                                            .background(Color.LightGray, CircleShape)
                                            .clickable {
                                                if (board[i][j].isEmpty() && currentPlayer == "X") {
                                                    board = board.mapIndexed { row, cols ->
                                                        cols.mapIndexed { col, value ->
                                                            if (row == i && col == j) "X" else value
                                                        }.toMutableList()
                                                    }
                                                    val winner = checkWinner()
                                                    if (winner != null) {
                                                        when (winner) {
                                                            "X" -> {
                                                                playerWins++
                                                                message = "Tu ganas!"
                                                            }
                                                            "O" -> {
                                                                aiWins++
                                                                message = "La IA gana!"
                                                            }
                                                            "Empate" -> {
                                                                draws++ // Incrementar empates
                                                                message = "Es un empate!"
                                                            }
                                                        }
                                                    } else {
                                                        currentPlayer = "O"
                                                        aiMove()
                                                        val aiWinner = checkWinner()
                                                        if (aiWinner != null) {
                                                            when (aiWinner) {
                                                                "X" -> {
                                                                    playerWins++
                                                                    message = "Tu ganas!"
                                                                }
                                                                "O" -> {
                                                                    aiWins++
                                                                    message = "La IA gana!"
                                                                }
                                                                "Empate" -> {
                                                                    draws++ // Incrementar empates
                                                                    message = "Es un empate!"
                                                                }
                                                            }
                                                        } else {
                                                            currentPlayer = "X"
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = board[i][j],
                                            fontSize = 32.sp,
                                            textAlign = TextAlign.Center,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = message, fontSize = 20.sp)
                    }
                }
            }

            // NavigationBar en la parte inferior
            NavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Nuevo juego") },
                    label = { Text("Nuevo") },
                    selected = false,
                    onClick = { resetBoard() }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Dificultad") },
                    label = { Text("Dificultad: $aiDifficulty") },
                    selected = false,
                    onClick = { showDifficultyDialog = true }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Salir") },
                    label = { Text("Salir") },
                    selected = false,
                    onClick = { showExitDialog = true }
                )
            }
        }

        // Diálogo de selección de dificultad
        if (showDifficultyDialog) {
            AlertDialog(
                onDismissRequest = { showDifficultyDialog = false },
                title = { Text("Seleccionar dificultad") },
                text = {
                    Column {
                        listOf("Fácil", "Normal", "Difícil").forEach { difficulty ->
                            Text(
                                text = difficulty,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        aiDifficulty = difficulty
                                        showDifficultyDialog = false
                                    }
                                    .padding(vertical = 18.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Diálogo de confirmación de salida
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Salir del juego") },
                text = { Text("¿Estás seguro de que quieres salir?") },
                confirmButton = {
                    TextButton(
                        onClick = { System.exit(0) }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitDialog = false }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
