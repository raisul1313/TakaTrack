package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

@Composable
fun AppBottomNavBar(
  currentRoute: String,
  onNavigate: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier.testTag("bottom_nav_bar"),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = MaterialTheme.colorScheme.surfaceVariant.let { androidx.compose.ui.unit.Dp(3f) }
  ) {
    BottomNavScreens.forEach { screen ->
      val selected = currentRoute == screen.route
      val icon: ImageVector = when (screen) {
        Screen.Home -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        Screen.Transactions -> if (selected) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong
        Screen.Budgets -> if (selected) Icons.Filled.PieChart else Icons.Outlined.PieChart
        Screen.Insights -> if (selected) Icons.Filled.BarChart else Icons.Outlined.BarChart
        else -> Icons.Filled.Home
      }

      NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(screen.route) },
        icon = { Icon(imageVector = icon, contentDescription = screen.title) },
        label = { Text(text = screen.title, style = MaterialTheme.typography.labelSmall) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag("nav_${screen.route}")
      )
    }
  }
}
