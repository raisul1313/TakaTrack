package com.example.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.entity.TransactionType
import com.example.domain.model.TransactionItem
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.FintechCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
  transaction: TransactionItem,
  currencySymbol: String,
  onDismiss: () -> Unit,
  onEdit: (TransactionItem) -> Unit,
  onDuplicate: (TransactionItem) -> Unit,
  onDelete: (String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var showDeleteDialog by remember { mutableStateOf(false) }

  val isExpense = transaction.type == TransactionType.EXPENSE
  val isTransfer = transaction.type == TransactionType.TRANSFER
  val typeColor = when {
    isExpense -> ExpenseRed
    isTransfer -> TransferBlue
    else -> IncomeGreen
  }

  val sdfDate = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US) }
  val sdfTime = remember { SimpleDateFormat("h:mm a", Locale.US) }
  val formattedDate = remember(transaction.date) { sdfDate.format(Date(transaction.date)) }
  val formattedTime = remember(transaction.date) { sdfTime.format(Date(transaction.date)) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .padding(bottom = 36.dp)
    ) {
      // Top bar with close & actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onDismiss) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = {
              onDuplicate(transaction)
              onDismiss()
            },
            modifier = Modifier.testTag("btn_duplicate_transaction")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Duplicate",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = {
              onEdit(transaction)
              onDismiss()
            },
            modifier = Modifier.testTag("btn_edit_transaction")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.testTag("btn_delete_transaction")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = ExpenseRed
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Center Hero Amount
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        CategoryIconBadge(
          iconName = transaction.categoryIcon,
          colorHex = transaction.categoryColorHex,
          size = 64.dp,
          iconSize = 32.dp,
          shapeCorner = 20.dp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = transaction.merchant,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        val prefix = if (isExpense) "-" else if (isTransfer) "" else "+"
        Text(
          text = "$prefix${CurrencyFormatter.format(transaction.amount, currencySymbol, showDecimals = true)}",
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.Bold,
          color = typeColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
          shape = CircleShape,
          color = typeColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = transaction.type.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = typeColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Details Card
      FintechCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          DetailRow(
            icon = Icons.Default.CalendarToday,
            label = "Date & Time",
            value = "$formattedDate at $formattedTime"
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          DetailRow(
            icon = Icons.Default.Wallet,
            label = if (isTransfer) "From Account" else "Account",
            value = transaction.accountName
          )

          if (isTransfer && transaction.toAccountName != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            DetailRow(
              icon = Icons.Default.Wallet,
              label = "To Account",
              value = transaction.toAccountName
            )
          }

          if (!isTransfer) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            DetailRow(
              icon = Icons.Default.CreditCard,
              label = "Category",
              value = transaction.categoryName
            )
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          DetailRow(
            icon = Icons.Default.CreditCard,
            label = "Payment Method",
            value = transaction.paymentMethod
          )

          if (transaction.isRecurring) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            DetailRow(
              icon = Icons.Default.Repeat,
              label = "Recurring",
              value = "Repeats Monthly"
            )
          }

          if (transaction.notes.isNotBlank()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            DetailRow(
              icon = Icons.Default.Notes,
              label = "Notes",
              value = transaction.notes
            )
          }
        }
      }

      // Receipt Attachment section
      if (transaction.attachmentUri != null) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
          text = "Receipt Attachment",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          AsyncImage(
            model = File(transaction.attachmentUri),
            contentDescription = "Receipt Attachment",
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Transaction") },
      text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            onDelete(transaction.id)
            showDeleteDialog = false
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun DetailRow(
  icon: ImageVector,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
