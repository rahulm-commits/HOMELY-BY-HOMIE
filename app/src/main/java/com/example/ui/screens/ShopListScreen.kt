package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AddBusiness
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestoreFromTrash
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DeliveryRoute
import com.example.data.model.RoutesCatalog
import com.example.data.model.Shop
import com.example.data.model.ShopStatus
import com.example.ui.components.StandardRouteDropdown
import com.example.ui.components.openDialer
import com.example.ui.components.openWhatsApp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.AmberWarningContainer
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessContainer
import java.io.File

private enum class ShopSectionTab(val label: String) {
  ALL("All Shops"),
  RECENTLY_ADDED("Recently Added"),
  RECENTLY_DELETED("Recently Deleted"),
  INACTIVE("Inactive Shops"),
}

@Composable
fun ShopListScreen(
  shops: List<Shop>,
  routes: List<DeliveryRoute> = RoutesCatalog.starterRoutes,
  pendingQueueCount: Int = 0,
  laptopServerEnabled: Boolean = false,
  onOpenSyncServers: () -> Unit = {},
  onAddShopClick: () -> Unit,
  onEditShopClick: (Shop) -> Unit,
  onChangeShopStatus: (shopId: String, newStatus: String, feedback: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedRoute by remember { mutableStateOf(RoutesCatalog.ALL) }
  var selectedTab by remember { mutableStateOf(ShopSectionTab.ALL) }

  val registeredCount = remember(shops) {
    shops.count { it.status != ShopStatus.DELETED }
  }
  val offlinePendingCount = remember(shops, pendingQueueCount) {
    maxOf(pendingQueueCount, shops.count { it.isPendingSync })
  }

  val filteredShops = remember(shops, searchQuery, selectedRoute, selectedTab) {
    val bySection = when (selectedTab) {
      ShopSectionTab.ALL -> shops.filter { it.status == ShopStatus.ACTIVE }
      ShopSectionTab.RECENTLY_ADDED -> shops.filter { it.status != ShopStatus.DELETED }.take(6)
      ShopSectionTab.RECENTLY_DELETED -> shops.filter { it.status == ShopStatus.DELETED }
      ShopSectionTab.INACTIVE -> shops.filter { it.status == ShopStatus.INACTIVE }
    }
    val byRoute = if (selectedRoute == RoutesCatalog.ALL) {
      bySection
    } else {
      bySection.filter { it.routeId == selectedRoute }
    }
    val q = searchQuery.trim().lowercase()
    if (q.isEmpty()) {
      byRoute
    } else {
      byRoute.filter { shop ->
        shop.name.lowercase().contains(q) ||
          shop.shopCode.lowercase().contains(q) ||
          shop.ownerName.lowercase().contains(q) ||
          shop.phone.lowercase().contains(q)
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top summary & filters
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Registered Shops",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(8.dp),
                ) {
                  Text(
                    text = "$registeredCount Shops",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  )
                }
                if (offlinePendingCount > 0) {
                  Surface(
                    color = AmberWarningContainer,
                    shape = RoundedCornerShape(8.dp),
                  ) {
                    Text(
                      text = "$offlinePendingCount Pending Sync",
                      style = MaterialTheme.typography.labelMedium,
                      color = AmberWarning,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                  }
                }
              }
            }

            Surface(
              color = if (offlinePendingCount > 0) AmberWarningContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenSyncServers)
                .testTag("shop_sync_servers_button"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Sync,
                  contentDescription = "Open Phone Queue and Servers",
                  tint = if (offlinePendingCount > 0) AmberWarning else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = if (offlinePendingCount > 0) {
                    "Queue ($offlinePendingCount)"
                  } else if (laptopServerEnabled) {
                    "Servers"
                  } else {
                    "Sync"
                  },
                  style = MaterialTheme.typography.labelLarge,
                  color = if (offlinePendingCount > 0) AmberWarning else MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            }
          }

          // Standard Route Selection Dropdown (matching OrderScreen)
          StandardRouteDropdown(
            selectedRoute = selectedRoute,
            routes = routes,
            onRouteSelected = { selectedRoute = it },
            fieldTestTag = "shop_route_filter",
          )

          // Search by shop name, shop ID, contact name, or phone
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search name, ID, contact or phone...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedContainerColor = MaterialTheme.colorScheme.background,
              focusedContainerColor = MaterialTheme.colorScheme.background,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("shop_search_input"),
          )

          // Section chips: All Shops, Recently Added, Recently Deleted, Inactive
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            ShopSectionTab.entries.forEach { tab ->
              FilterChip(
                selected = selectedTab == tab,
                onClick = { selectedTab = tab },
                label = { Text(tab.label) },
                modifier = Modifier.testTag("shop_tab_${tab.name.lowercase()}"),
              )
            }
          }
        }
      }

      if (filteredShops.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Storefront,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
              modifier = Modifier.size(56.dp),
            )
            Text(
              text = "No shops in ${selectedTab.label}",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Tap Add Shop below to register a shop — works offline too.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(filteredShops, key = { it.id.ifBlank { it.shopCode } }) { shop ->
            ShopCardItem(
              shop = shop,
              onCall = { openDialer(context, shop.phone) },
              onWhatsApp = {
                openWhatsApp(context, shop.phone, "Hello ${shop.ownerName}, contacting from SnackRoute regarding ${shop.name}.")
              },
              onEdit = { onEditShopClick(shop) },
              onChangeStatus = onChangeShopStatus,
            )
          }
        }
      }
    }

    // Bottom-center Add Shop button as requested
    ExtendedFloatingActionButton(
      onClick = onAddShopClick,
      icon = {
        Icon(
          imageVector = Icons.Rounded.AddBusiness,
          contentDescription = null,
        )
      },
      text = {
        Text(
          text = "Add Shop",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
      },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 14.dp)
        .testTag("add_shop_fab"),
    )
  }
}

@Composable
private fun ShopCardItem(
  shop: Shop,
  onCall: () -> Unit,
  onWhatsApp: () -> Unit,
  onEdit: () -> Unit,
  onChangeStatus: (shopId: String, newStatus: String, feedback: String) -> Unit,
) {
  var menuOpen by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
      ) {
        // Small shop photo thumbnail
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
          if (shop.photoUri.isNotBlank() && File(shop.photoUri).exists()) {
            AsyncImage(
              model = File(shop.photoUri),
              contentDescription = "${shop.name} photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          } else {
            Image(
              painter = painterResource(id = R.drawable.img_shop_placeholder),
              contentDescription = "${shop.name} storefront",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          }
        }

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
                text = shop.shopCode,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
            if (shop.isPendingSync) {
              Surface(
                color = AmberWarningContainer,
                shape = RoundedCornerShape(6.dp),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = AmberWarning,
                    modifier = Modifier.size(11.dp),
                  )
                  Text(
                    text = "Offline Pending",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberWarning,
                  )
                }
              }
            }
          }

          Text(
            text = shop.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Person,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = "${shop.ownerName} • ${shop.phone}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.LocationOn,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = "${shop.address} • PIN ${shop.pinCode}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Box {
          IconButton(onClick = { menuOpen = true }) {
            Icon(
              imageVector = Icons.Rounded.MoreHoriz,
              contentDescription = "More shop actions",
            )
          }
          DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            if (shop.status == ShopStatus.DELETED) {
              DropdownMenuItem(
                text = { Text("Restore Shop") },
                leadingIcon = { Icon(Icons.Rounded.RestoreFromTrash, contentDescription = null) },
                onClick = {
                  menuOpen = false
                  onChangeStatus(shop.id, ShopStatus.ACTIVE, "${shop.name} restored")
                },
              )
            } else {
              val toggleTarget = if (shop.status == ShopStatus.INACTIVE) ShopStatus.ACTIVE else ShopStatus.INACTIVE
              val toggleLabel = if (shop.status == ShopStatus.INACTIVE) "Mark Active" else "Mark Inactive"
              DropdownMenuItem(
                text = { Text(toggleLabel) },
                onClick = {
                  menuOpen = false
                  onChangeStatus(shop.id, toggleTarget, "${shop.name} marked $toggleTarget")
                },
              )
              DropdownMenuItem(
                text = { Text("Move to Recently Deleted", color = CoralDanger) },
                leadingIcon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = CoralDanger) },
                onClick = {
                  menuOpen = false
                  onChangeStatus(shop.id, ShopStatus.DELETED, "${shop.name} moved to Recently Deleted")
                },
              )
            }
          }
        }
      }

      // Action bar: Call, WhatsApp, Edit
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          color = EmeraldSuccessContainer,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onCall),
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.Call,
              contentDescription = "Call ${shop.name}",
              tint = EmeraldSuccess,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Call",
              style = MaterialTheme.typography.labelLarge,
              color = EmeraldSuccess,
            )
          }
        }

        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onWhatsApp),
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.Chat,
              contentDescription = "WhatsApp ${shop.name}",
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "WhatsApp",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }

        TextButton(
          onClick = onEdit,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.height(40.dp),
        ) {
          Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Edit ${shop.name}",
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Edit", style = MaterialTheme.typography.labelLarge)
        }
      }
    }
  }
}
