package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessunit.contentcreator.model.ContentWork

@Composable
fun ContentCreatorProductionResultDialog(
    work: ContentWork,
    onDismiss: () -> Unit,
    onPitchPH: () -> Unit
) {
    val score = work.engagementScore ?: 50

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ContentCreatorTheme.BgDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ContentCreatorTheme.Gold.copy(alpha = 0.2f))
                        .border(1.dp, ContentCreatorTheme.Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = ContentCreatorTheme.Gold, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Produksi Selesai Dirilis!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ContentCreatorTheme.TextWhite
                )
                Text(
                    text = work.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ContentCreatorTheme.AccentMagenta
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Score Rating Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                    border = BorderStroke(1.dp, ContentCreatorTheme.Gold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ENGAGEMENT SCORE", fontSize = 10.sp, color = ContentCreatorTheme.TextGray, fontWeight = FontWeight.Bold)
                        Text(
                            text = "★ $score / 100",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ContentCreatorTheme.Gold
                        )
                    }
                }

                // Reception Verdict
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardLight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = work.receptionVerdict ?: "Penerimaan Audiens Positif",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ContentCreatorTheme.NeonGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = work.receptionNote ?: "Karya berhasil menarik perhatian audiens digital dan siap dikomersialisasikan ke katalog lisensi.",
                            fontSize = 11.sp,
                            color = ContentCreatorTheme.TextWhite,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPitchPH,
                colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.AccentCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Tawarkan Lisensi ke PH ➔", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Simpan di Katalog", color = ContentCreatorTheme.TextGray)
            }
        }
    )
}
