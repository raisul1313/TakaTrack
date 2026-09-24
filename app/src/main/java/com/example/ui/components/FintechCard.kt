package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FintechCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(20.dp),
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
  borderWidth: Dp = 1.dp,
  elevation: Dp = 1.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val cardModifier = if (onClick != null) {
    modifier
      .clip(shape)
      .clickable(onClick = onClick)
  } else {
    modifier
  }

  Card(
    modifier = cardModifier,
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = BorderStroke(borderWidth, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
  ) {
    Box(modifier = Modifier.padding(16.dp)) {
      content()
    }
  }
}
