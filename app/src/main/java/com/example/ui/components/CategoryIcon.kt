package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun parseColorHex(hex: String, fallback: Color = Color(0xFF0D9488)): Color {
  return runCatching {
    Color(android.graphics.Color.parseColor(hex))
  }.getOrDefault(fallback)
}

fun getIconVector(iconName: String): ImageVector {
  return when (iconName.lowercase()) {
    "restaurant", "food" -> Icons.Default.Restaurant
    "shopping_cart", "groceries" -> Icons.Default.ShoppingCart
    "directions_car", "transport", "car" -> Icons.Default.DirectionsCar
    "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
    "receipt_long", "bills", "utilities" -> Icons.Default.ReceiptLong
    "home", "rent", "housing" -> Icons.Default.Home
    "medical_services", "health" -> Icons.Default.MedicalServices
    "school", "education" -> Icons.Default.School
    "movie", "entertainment" -> Icons.Default.Movie
    "flight", "travel" -> Icons.Default.Flight
    "subscriptions" -> Icons.Default.Subscriptions
    "face", "personal" -> Icons.Default.Face
    "account_balance_wallet", "wallet" -> Icons.Default.AccountBalanceWallet
    "laptop_mac", "freelance" -> Icons.Default.LaptopMac
    "business_center", "business" -> Icons.Default.BusinessCenter
    "trending_up", "investment" -> Icons.Default.TrendingUp
    "card_giftcard", "bonus" -> Icons.Default.CardGiftcard
    "redeem", "gift" -> Icons.Default.Redeem
    "attach_money", "money", "salary" -> Icons.Default.AttachMoney
    "payments", "cash" -> Icons.Default.Payments
    "account_balance", "bank" -> Icons.Default.AccountBalance
    "smartphone", "phone_android", "bkash", "nagad" -> Icons.Default.Smartphone
    "credit_card" -> Icons.Default.CreditCard
    "savings" -> Icons.Default.Savings
    else -> Icons.Default.MoreHoriz
  }
}

@Composable
fun CategoryIconBadge(
  iconName: String,
  colorHex: String,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  iconSize: Dp = 22.dp,
  shapeCorner: Dp = 14.dp
) {
  val baseColor = parseColorHex(colorHex)
  val backgroundColor = baseColor.copy(alpha = 0.14f)

  Box(
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(shapeCorner))
      .background(backgroundColor),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = getIconVector(iconName),
      contentDescription = iconName,
      tint = baseColor,
      modifier = Modifier.size(iconSize)
    )
  }
}

@Composable
fun AccountIconBadge(
  iconName: String,
  colorHex: String,
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
  iconSize: Dp = 20.dp
) {
  val baseColor = parseColorHex(colorHex)
  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(baseColor.copy(alpha = 0.15f)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = getIconVector(iconName),
      contentDescription = iconName,
      tint = baseColor,
      modifier = Modifier.size(iconSize)
    )
  }
}
