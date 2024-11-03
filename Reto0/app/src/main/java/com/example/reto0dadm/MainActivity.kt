package com.example.reto0dadm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.reto0dadm.ui.theme.Reto0DADMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Reto0DADMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "mundo para la clase de DADM 💪",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFF8A80), Color(0xFFD32F2F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hola $name!",
            color = Color.White,
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Reto0DADMTheme {
        Greeting("Android")
    }
}