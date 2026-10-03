package com.example.filmstudio.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Common UI helper functions for film status colors and tags.
 */
@Composable
fun getFilmStatusColor(status: String): Color {
    val s = status.lowercase()
    return when {
        s.contains("tayang") -> Color(0xFF4CAF50) // Hijau terang
        s.contains("pra-produksi") || s.contains("antrean") -> Color.Gray
        s.contains("syuting") || s.contains("produksi animasi") -> Color(0xFFFFC107) // Kuning Amber
        s.contains("pasca produksi") || s.contains("qc") || s.contains("poles") -> Color(0xFF64FFDA) // Biru/hijau muda
        s.contains("produksi") -> Color(0xFFFFC107)
        s.contains("menunggu") -> Color.LightGray // Abu-abu
        else -> Color.White
    }
}
