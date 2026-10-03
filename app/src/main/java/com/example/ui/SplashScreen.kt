package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.BottomNavItem
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    // 1. Loading Text Phrases
    val loadingTexts = listOf(
        "Inisialisasi Sistem Korporat...",
        "Menghubungkan ke Bursa Saham Global...",
        "Mengaudit Laporan Keuangan Holding...",
        "Mengkalkulasi Valuasi Aset & Properti...",
        "Menyiapkan Ruang Direksi..."
    )
    
    var currentTextIndex by remember { mutableStateOf(0) }
    var startAppAnimation by remember { mutableStateOf(false) }
    var progressTarget by remember { mutableStateOf(0f) }
    
    // 2. Animations
    // Smooth Fade-In block
    val splashAlpha by animateFloatAsState(
        targetValue = if (startAppAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutQuad),
        label = "fade"
    )
    
    // Heartbeat Pulse animation for corporate logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Progress gauge from 0f to 1f over 3500ms
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 3500, easing = LinearEasing),
        label = "progress"
    )
    
    // 3. Side-effects for timers and transition logic
    LaunchedEffect(Unit) {
        startAppAnimation = true
        progressTarget = 1f
        
        // Splash screen duration: 3.7 seconds total
        delay(3700)
        
        // Navigate to primary screen, routing out the splash entry representing navigation.replace()
        navController.navigate(BottomNavItem.Investing.screen_route) {
            popUpTo("splash") { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        // Dynamic loading text timer (every 750ms to cycle through nicely)
        while (true) {
            delay(750)
            currentTextIndex = (currentTextIndex + 1) % loadingTexts.size
        }
    }
    
    // 4. Elegant Minimal Background Theme with Metallic Linear Gradient Shimmer
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0A0B),
            Color(0xFF121214),
            Color(0xFF0F0F10)
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(splashAlpha)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1.2f))
            
            // Premium Large Executive Golden Centerpiece Icon
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = logoScale,
                        scaleY = logoScale
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Mega Holding Logo",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(72.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Branded typography styling
            Text(
                text = "MEGA HOLDING",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.SansSerif
            )
            
            Text(
                text = "SIMULATOR",
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 10.sp,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp) // Offset slightly for tracking
            )
            
            Spacer(modifier = Modifier.weight(1.0f))
            
            // Micro system telemetry log (Monospace Loading indicator)
            Text(
                text = loadingTexts[currentTextIndex],
                color = Color(0xFFFFD700).copy(alpha = 0.75f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Minimalist Premium Line Gauge component
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(1.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Color(0xFFFFD700), shape = RoundedCornerShape(1.dp))
                )
            }
            
            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}
