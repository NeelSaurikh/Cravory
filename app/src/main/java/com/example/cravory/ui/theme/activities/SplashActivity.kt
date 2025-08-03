package com.example.cravory.ui.theme.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cravory.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashScreen()
            FontExample()
        }
    }
}

@Composable
fun SplashScreen() {
    val context = LocalContext.current
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(30f) }

    val customFont = FontFamily(
        Font(R.font.greatvibes_regular, FontWeight.Normal)
    )

    LaunchedEffect(true) {
        // Run all animations in parallel
        val scaleAnim = launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        }

        val alphaAnim = launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearOutSlowInEasing
                )
            )
        }

        val offsetAnim = launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearOutSlowInEasing
                )
            )
        }

        joinAll(scaleAnim, alphaAnim, offsetAnim)

        delay(1500L)

        context.startActivity(Intent(context, MainActivity::class.java))
        (context as? ComponentActivity)?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.cravory_logo_no_bg),
                contentDescription = "Splash Logo",
                modifier = Modifier
//                    .scale(scale.value)
                    .fillMaxSize(0.7f)
            )

            Text(
                text = "Where cravings meet savory!",
                style = TextStyle(
                    fontFamily = customFont,
                    fontSize = 36.sp,
                    color = Color(0xFFC2953B),
                ),
                modifier = Modifier
                    .alpha(alpha.value)
                    .offset(y = offsetY.value.dp)
            )
        }
    }
}


@Composable
fun FontExample() {
    val customFont = FontFamily(
        Font(R.font.greatvibes_regular) // Your font name
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Cravory in Custom Font",
            fontFamily = customFont,
            fontSize = 24.sp
        )

        Text(
            text = "Default system font below",
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}