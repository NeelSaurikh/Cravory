package com.example.cravory.ui.theme.activities

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cravory.CravoryApp
import com.example.cravory.database.AppDatabase
import com.example.cravory.model.CartItem
import com.example.cravory.network.ApiClient
import com.example.cravory.ui.theme.CravoryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CravoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CartScreen()
                }
            }
        }
    }
}
@Composable
fun CartScreen() {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var netTotal by remember { mutableStateOf(0.0) }
    var taxes by remember { mutableStateOf(0.0) }
    var grandTotal by remember { mutableStateOf(0.0) }
    var showDialog by remember { mutableStateOf(false) }

    // Fetch cart items when screen loads
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val items = CravoryApp.database.cartDao().getAllItems()
            withContext(Dispatchers.Main) {
                cartItems = items
                val totals = calculateCartTotals(items)
                netTotal = totals.first
                taxes = totals.second
                grandTotal = totals.third
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (cartItems.isEmpty()) {
                Text(
                    text = "Your cart is empty",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineMedium
                )
            } else {
                // Cart Items List
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChanged = { itemId, quantity ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (quantity == 0) {
                                        CravoryApp.database.cartDao().removeItem(itemId)
                                    } else {
                                        CravoryApp.database.cartDao().updateQuantity(itemId, quantity)
                                    }
                                    val updatedItems = CravoryApp.database.cartDao().getAllItems()
                                    withContext(Dispatchers.Main) {
                                        cartItems = updatedItems
                                        val totals = calculateCartTotals(updatedItems)
                                        netTotal = totals.first
                                        taxes = totals.second
                                        grandTotal = totals.third
                                    }
                                }
                            }
                        )
                    }
                }

                // Totals Section
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "Net Total: ₹${"%.2f".format(netTotal)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "CGST (2.5%) + SGST (2.5%): ₹${"%.2f".format(taxes)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Grand Total: ₹${"%.2f".format(grandTotal)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Place Order")
                    }
                }
            }
        }

        // Confirmation Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Order") },
                text = { Text("Grand Total: ₹${"%.2f".format(grandTotal)}") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            if (cartItems.isNotEmpty()) {
                                val (success, message) = ApiClient.makePayment(cartItems)
                                coroutineScope.launch {
                                    if (success) {
                                        CravoryApp.database.cartDao().clearCart()
                                        cartItems = emptyList()
                                        netTotal = 0.0
                                        taxes = 0.0
                                        grandTotal = 0.0
                                    }
                                    snackbarHostState.showSnackbar(message)
                                }
                            } else {
                                snackbarHostState.showSnackbar("Cart is empty")
                            }
                            showDialog = false
                        }
                    }) { Text("Place Order") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChanged: (String, Int) -> Unit
) {
    var itemName by remember { mutableStateOf("Item ID: ${item.itemId}") }
    var itemImage: Bitmap? by remember { mutableStateOf<Bitmap?>(null) }

    // Fetch item details
    LaunchedEffect(item.itemId) {
        val dish = ApiClient.getItemById(item.itemId)
        dish?.let {
            itemName = it.name
            itemImage = ApiClient.loadImage(it.image_url)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image
            itemImage?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Item Image",
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image")
            }

            // Item Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(text = itemName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Price: ₹${"%.2f".format(item.itemPrice)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (item.itemQuantity > 1) {
                                onQuantityChanged(item.itemId, item.itemQuantity - 1)
                            } else {
                                onQuantityChanged(item.itemId, 0)
                            }
                        }
                    ) {
                        Text("-")
                    }
                    Text(
                        text = "${item.itemQuantity}",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChanged(item.itemId, item.itemQuantity + 1) }
                    ) {
                        Text("+")
                    }
                }
            }
        }
    }
}

private fun calculateCartTotals(items: List<CartItem>): Triple<Double, Double, Double> {
    val netTotal = items.sumOf { it.itemPrice * it.itemQuantity }
    val cgst = netTotal * 0.025
    val sgst = netTotal * 0.025
    val grandTotal = netTotal + cgst + sgst
    return Triple(netTotal, cgst + sgst, grandTotal)
}