package com.example.ui

import com.example.viewmodel.GameViewModel

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalTycoonIndexScreen(navController: NavController, viewModel: GameViewModel) {
    val tycoons by viewModel.tycoonList.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF39FF14)
    val textGray = Color(0xFFA0A0A0)
    val playerColor = Color(0xFF00bcd4) // Cyan
    
    val format = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    Scaffold(
        containerColor = bgDark,
        topBar = {
            TopAppBar(
                title = { Text("Global Tycoon Index", color = gold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            // Header stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Top 200 Terkaya di Dunia", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = gold)
                }
            }
            
            itemsIndexed(tycoons) { index, tycoon ->
                val isPlayer = tycoon.isPlayer
                val rank = index + 1
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isPlayer) playerColor.copy(alpha = 0.2f) else cardDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank
                        Text(
                            text = "#$rank", 
                            color = if (rank <= 3) gold else Color.White, 
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            modifier = Modifier.width(48.dp)
                        )
                        
                        // Avatar placeholder
                        Surface(
                            shape = CircleShape,
                            color = if (isPlayer) playerColor else textGray.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isPlayer) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                } else {
                                    Text(tycoon.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tycoon.name, 
                                color = if (isPlayer) Color.White else textGray, 
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isPlayer) {
                                Text("This is you!", color = playerColor, fontSize = 12.sp)
                            }
                        }
                        
                        Text(
                            text = format.format(tycoon.netWorth), 
                            color = neonGreen, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
