package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CatalogProduct
import com.example.data.model.Shop
import com.example.data.model.SnackCatalog
import com.example.ui.components.formatInr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceOrderScreen(
  shop: Shop,
  onConfirmOrder: (items: List<Pair<CatalogProduct, Int>>, note: String) -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var productSearchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All Snacks") }
  var orderNote by remember { mutableStateOf("") }
  val quantities = remember { mutableStateMapOf<String, Int>() }

  val filteredProducts = remember(productSearchQuery, selectedCategory) {
    val q = productSearchQuery.trim().lowercase()
    SnackCatalog.products.filter { prod ->
      val matchesCat = selectedCategory == "All Snacks" || prod.category == selectedCategory
      val matchesQuery = q.isEmpty() ||
        prod.code.lowercase().contains(q) ||
        prod.name.lowercase().contains(q) ||
        prod.packSize.lowercase().contains(q)
      matchesCat && matchesQuery
    }
  }

  val totalCount = quantities.values.sum()
  val totalAmount = SnackCatalog.products.sumOf { prod ->
    (quantities[prod.code] ?: 0) * prod.defaultPrice
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Place Order",
              style = MaterialTheme.typography.titleLarge,
            )
            Text(
              text = "${shop.shopCode} • ${shop.name}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
        },
      )
    },
    bottomBar = {
      Surface(
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "$totalCount packets selected",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = formatInr(totalAmount),
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
          }
          Button(
            onClick = {
              val chosen = SnackCatalog.products.mapNotNull { prod ->
                val qty = quantities[prod.code] ?: 0
                if (qty > 0) prod to qty else null
              }
              onConfirmOrder(chosen, orderNote)
            },
            enabled = totalCount > 0,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .height(50.dp)
              .testTag("confirm_order_button"),
          ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Confirm Order", style = MaterialTheme.typography.titleSmall)
          }
        }
      }
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(104.dp),
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_snacks_banner),
              contentDescription = "Snacks catalog",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
            Surface(
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            ) {
              Text(
                text = "Route: ${shop.routeId}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }
      }

      // Product code search
      item {
        OutlinedTextField(
          value = productSearchQuery,
          onValueChange = { productSearchQuery = it },
          placeholder = { Text("Enter product code (e.g. BC100) or snack name...") },
          leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
          trailingIcon = { Icon(Icons.Rounded.QrCode2, contentDescription = "Product code") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("product_code_search"),
        )
      }

      // Category sections: All Snacks + individual categories
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          SnackCatalog.categories.forEach { category ->
            FilterChip(
              selected = selectedCategory == category,
              onClick = { selectedCategory = category },
              label = { Text(category) },
            )
          }
        }
      }

      items(filteredProducts, key = { it.code }) { product ->
        val qty = quantities[product.code] ?: 0
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = if (qty > 0) 3.dp else 1.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(6.dp),
                ) {
                  Text(
                    text = product.code,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
                Text(
                  text = product.packSize,
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${formatInr(product.defaultPrice)} / pkt • ${product.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              IconButton(
                onClick = {
                  if (qty > 0) quantities[product.code] = qty - 1
                },
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease ${product.name}")
              }
              Text(
                text = "$qty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp),
              )
              IconButton(
                onClick = { quantities[product.code] = qty + 5 },
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .testTag("add_qty_${product.code}"),
              ) {
                Icon(
                  Icons.Rounded.Add,
                  contentDescription = "Add ${product.name}",
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
          }
        }
      }

      item {
        OutlinedTextField(
          value = orderNote,
          onValueChange = { orderNote = it },
          label = { Text("Order Note (Optional)") },
          placeholder = { Text("Dispatch instructions, urgent delivery...") },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}
