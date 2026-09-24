package com.example.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.AppCurrency
import com.example.data.local.PopularCurrencies
import com.example.ui.ExpenseViewModel
import com.example.ui.components.FintechCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
  viewModel: ExpenseViewModel,
  onComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCurrency by remember { mutableStateOf(AppCurrency.DEFAULT) }
  var startingBalanceText by remember { mutableStateOf("25000") }
  var monthlyBudgetText by remember { mutableStateOf("50000") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(28.dp))

    // App Badge
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(
          brush = Brush.linearGradient(
            colors = listOf(
              Color(0xFF0F766E),
              Color(0xFF0D9488)
            )
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.AccountBalanceWallet,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(42.dp)
      )
    }

    Spacer(modifier = Modifier.height(18.dp))

    Text(
      text = "Welcome to TakaTrack",
      style = MaterialTheme.typography.headlineLarge,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Effortless, privacy-first personal expense tracking designed for high speed and clarity.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Currency selection
    FintechCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Text(
          text = "Select Primary Currency",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AppCurrency.SUPPORTED.forEach { curr ->
            val isSelected = selectedCurrency.code == curr.code
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { selectedCurrency = curr }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "${curr.symbol} ${curr.code}",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isSelected) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Initial Cash & Target Budget
    FintechCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Text(
          text = "Starting Cash Balance (${selectedCurrency.symbol})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = startingBalanceText,
          onValueChange = { startingBalanceText = it },
          placeholder = { Text("0") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_onboarding_balance")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Target Monthly Budget Limit (${selectedCurrency.symbol})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = monthlyBudgetText,
          onValueChange = { monthlyBudgetText = it },
          placeholder = { Text("50000") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_onboarding_budget")
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Feature Highlights
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      FeatureHighlightChip(
        icon = Icons.Default.FlashOn,
        title = "Fast Entry",
        subtitle = "One-tap category logging",
        modifier = Modifier.weight(1f)
      )
      FeatureHighlightChip(
        icon = Icons.Default.Shield,
        title = "100% Offline",
        subtitle = "Safe on-device storage",
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(28.dp))

    Button(
      onClick = {
        val startBalance = startingBalanceText.toDoubleOrNull() ?: 0.0
        val targetBudget = monthlyBudgetText.toDoubleOrNull() ?: 50000.0
        viewModel.completeOnboarding(selectedCurrency, startBalance, targetBudget)
        onComplete()
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp)
        .testTag("btn_get_started"),
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Text(
        text = "Get Started",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.width(8.dp))
      Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
    }

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun FeatureHighlightChip(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  FintechCard(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    elevation = 0.5.dp
  ) {
    Column {
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
