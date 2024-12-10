package com.example.reto3tictactoe
import android.media.MediaPlayer
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.reto3tictactoe.ui.theme.Reto3TicTacToeTheme
import androidx.compose.ui.platform.LocalConfiguration
import android.content.Context
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null


    // Funciones para guardar y cargar el marcador
    fun saveScores(context: Context, playerWins: Int, aiWins: Int, draws: Int) {
        val sharedPreferences = context.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("playerWins", playerWins)
            putInt("aiWins", aiWins)
            putInt("draws", draws)
            apply()
        }
    }

    fun loadScores(context: Context): Triple<Int, Int, Int> {
        val sharedPreferences = context.getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        val playerWins = sharedPreferences.getInt("playerWins", 0)
        val aiWins = sharedPreferences.getInt("aiWins", 0)
        val draws = sharedPreferences.getInt("draws", 0)
        return Triple(playerWins, aiWins, draws)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (initialPlayerWins, initialAiWins, initialDraws) = loadScores(this)

        setContent {
            Reto3TicTacToeTheme {
                var playerWins by rememberSaveable { mutableStateOf(initialPlayerWins) }
                var aiWins by rememberSaveable { mutableStateOf(initialAiWins) }
                var draws by rememberSaveable { mutableStateOf(initialDraws) }

                // Guardar los puntajes al salir
                DisposableEffect(Unit) {
                    onDispose {
                        saveScores(this@MainActivity, playerWins, aiWins, draws)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TicTacToeGame(
                        playerWins = playerWins,
                        aiWins = aiWins,
                        draws = draws,
                        onPlayerWin = { playerWins++ },
                        onAiWin = { aiWins++ },
                        onDraw = { draws++ },
                        onResetScores = {
                            playerWins = 0
                            aiWins = 0
                            draws = 0
                        }
                    )
                }
            }
        }
        startMusic()
    }

    // Función para iniciar la música
    private fun startMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music) // Reemplaza con el nombre de tu archivo
        mediaPlayer?.isLooping = true // Hace que la música se repita en bucle
        mediaPlayer?.start() // Comienza a reproducir la música
    }

    // Asegúrate de detener la música cuando la actividad se destruya
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Libera los recursos del MediaPlayer
        mediaPlayer = null
    }
}



@Composable
fun TicTacToeGame(
    playerWins: Int,
    aiWins: Int,
    draws: Int,
    onPlayerWin: () -> Unit,
    onAiWin: () -> Unit,
    onDraw: () -> Unit,
    onResetScores: () -> Unit
) {
    var board by remember { mutableStateOf(List(3) { MutableList(3) { "" } }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var message by remember { mutableStateOf("Tú turno!") }
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

    @Composable
    fun scores(isPortrait:Boolean){
        if (isPortrait){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Jugador 😎: $playerWins", fontSize = 18.sp)
                Text(text = "IA 🤖: $aiWins", fontSize = 18.sp)
                Text(text = "Empates 😮: $draws", fontSize = 18.sp) // Mostrar empates
            }
        } else{
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Jugador 😎: $playerWins", fontSize = 18.sp)
                Text(text = "IA 🤖: $aiWins", fontSize = 18.sp)
                Text(text = "Empates 😮: $draws", fontSize = 18.sp) // Mostrar empates
            }
        }

    }

    @Composable
    fun Content(isPortrait:Boolean) {
        var cellsSize = 120.dp
        var paddingBetweenCells = 18.dp
        var horizontalCellsContainerPadding = 0.dp
        var verticalRowSpacing = 8.dp

        if(isPortrait){
            Text(
                text = "Tic Tac Toe",
                fontSize = 32.sp,
                color = Color(0xFFFFF7C2),
                modifier = Modifier.padding(16.dp).padding(top = 64.dp)
            )

        } else {
            cellsSize = 60.dp
            paddingBetweenCells = 6.dp
            horizontalCellsContainerPadding = 60.dp
            verticalRowSpacing = 1.dp
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .fillMaxHeight()
        ) {
            if (!isPortrait) {
                Column(
                    modifier = Modifier.fillMaxWidth(), // Toma el ancho completo del contenedor
                    horizontalAlignment = Alignment.CenterHorizontally // Centra los elementos horizontalmente
                ) {
                    Text(text = message, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            // Imagen de fondo
            if(isPortrait){
                Image(
                    painter = painterResource(id = R.drawable.board),
                    contentDescription = "Fondo del tablero",
                    modifier = Modifier.fillMaxSize()
                )
            } else{
                Image(
                    painter = painterResource(id = R.drawable.board),
                    contentDescription = "Fondo del tablero",
                    modifier = Modifier
                        // Espaciado alrededor de la imagen
                        .size(270.dp) // Tamaño fijo
                        .align(Alignment.Center) // Centrado en su contenedor
                        .padding(top = 60.dp)
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                scores(isPortrait)

                // Tablero
                for (i in 0..2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = verticalRowSpacing), // Espaciado entre filas
                        horizontalArrangement = Arrangement.Center // Centrado horizontal
                    ) {
                        for (j in 0..2) {
                            Box(
                                modifier = Modifier
                                    .size(cellsSize) // Tamaño ajustado de las celdas
                                    .padding(paddingBetweenCells) // Espaciado entre celdas
                                    .background(Color.Transparent, CircleShape) // Fondo transparente
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
                                                        onPlayerWin()
                                                        message = "Tu ganas!"
                                                    }
                                                    "O" -> {
                                                        onAiWin()
                                                        message = "La IA gana!"
                                                    }
                                                    "Empate" -> {
                                                        onDraw()
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
                                                            onPlayerWin()
                                                            message = "Tu ganas!"
                                                        }
                                                        "O" -> {
                                                            onAiWin()
                                                            message = "La IA gana!"
                                                        }
                                                        "Empate" -> {
                                                            onDraw()
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
                                // Cambiar la representación de "X" y "O" a imágenes
                                when (board[i][j]) {
                                    "X" -> {
                                        Image(
                                            painter = painterResource(id = R.drawable.x_image),
                                            contentDescription = "X",
                                            modifier = Modifier.size(60.dp) // Ajustar el tamaño de la imagen
                                        )
                                    }
                                    "O" -> {
                                        Image(
                                            painter = painterResource(id = R.drawable.o_image),
                                            contentDescription = "O",
                                            modifier = Modifier.size(60.dp) // Ajustar el tamaño de la imagen
                                        )
                                    }
                                    else -> {} // Si no hay nada en la celda, no mostramos nada
                                }
                            }
                        }
                    }
                }
                if(isPortrait){
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = message, fontSize = 20.sp)
                }

            }
        }

    }

    @Composable
    fun ResponsiveLayout() {
        val configuration = LocalConfiguration.current
        val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

        if (isPortrait) {
            // Modo vertical
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Content(true)
            }
        } else {
            // Modo horizontal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Content(false)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        ResponsiveLayout()

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
                label = { Text(aiDifficulty) },
                selected = false,
                onClick = { showDifficultyDialog = true }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Refresh, contentDescription = "Resetear score") },
                label = { Text("Reset") },
                selected = false,
                onClick = {
                    onResetScores()
                }
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
