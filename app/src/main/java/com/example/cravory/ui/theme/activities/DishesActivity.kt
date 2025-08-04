package com.example.cravory.ui.theme.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cravory.Cart
import com.example.cravory.R
import com.example.cravory.model.Dish
import com.example.cravory.network.ApiClient
import com.example.cravory.ui.theme.CravoryTheme
import com.example.cravory.viewmodel.DishesUiState
import com.example.cravory.viewmodel.DishesViewModel
import kotlinx.coroutines.launch

class DishesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cuisineName = intent.getStringExtra("CUISINE_NAME") ?: ""
        val cuisineId = intent.getStringExtra("CUISINE_ID") ?: ""
        Log.d("DishesActivity", "Cuisine Name: $cuisineName, Cuisine ID: $cuisineId")

        setContent {
            CravoryTheme {
                DishesScreen(cuisineName, cuisineId)
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishesScreen(
    cuisineName: String,
    cuisineId: String,
    viewModel: DishesViewModel = viewModel()
) {
    var sortBy by remember { mutableStateOf("rating") }
    val context = LocalContext.current
    var fabExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val customFontBar = FontFamily(
        Font(R.font.greatvibes_regular)
    )

    LaunchedEffect(cuisineId, sortBy) {
        Log.d("DishesScreen", "Fetching dishes for cuisineId: $cuisineId, sortBy: $sortBy")
        viewModel.fetchDishes(cuisineId, sortBy)
    }

    val uiState = viewModel.uiState.collectAsState().value
    Log.d("DishesScreen", "UI State: $uiState")

    BackHandler {
        (context as? ComponentActivity)?.finish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                        Text(
                            text = "Craving For $cuisineName",
                            fontFamily = customFontBar,
                            fontSize = 24.sp,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White
                            )
                        ) },
                navigationIcon = {
                    val activity = LocalContext.current as? Activity
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.go_back_24),
                            contentDescription = "Exit App",
                            tint = Color.White
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF333333),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            )
        },

        // ✅ ADD THIS
        bottomBar = {
            ExtendedFloatingActionButton(
                onClick = {
                    val intent = Intent(context, CartActivity::class.java)
                    context.startActivity(intent)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.shopping_cart_24),
                        contentDescription = "Cart"
                    )
                },
                text = { Text("Go to Cart") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (fabExpanded) {
                    SmallFloatingActionButton(
                        onClick = {
                            sortBy = "price"
                            fabExpanded = false
                        }
                    ) {
                        Text("₹", fontWeight = FontWeight.Bold)
                    }

                    SmallFloatingActionButton(
                        onClick = {
                            sortBy = "rating"
                            fabExpanded = false
                        }
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Sort by Rating")
                    }

                    SmallFloatingActionButton(
                        onClick = {
                            sortBy = "name"
                            fabExpanded = false
                        }
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Sort by Name")
                    }
                }

                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sort_24),
                        contentDescription = "Decrease",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is DishesUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DishesUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DishesUiState.Success -> {
                    DishGridViewDistinct(
                        dishes = uiState.dishes,
                        cuisineId = cuisineId,
                        onAddToCart = { dish, quantity ->
                            coroutineScope.launch {
                                Cart.addItem(dish, quantity)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DishGridViewDistinct(
    dishes: List<Dish>,
    cuisineId: String,
    onAddToCart: (Dish, Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dishes) { dish ->
            DishCard(
                dish = dish,
                cuisineId = cuisineId,
                onAddToCart = onAddToCart,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DishCard(
    dish: Dish,
    cuisineId: String,
    onAddToCart: (Dish, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var wasAddedToCart by remember { mutableStateOf(false) }

    LaunchedEffect(dish.image_url) {
        bitmap = ApiClient.loadImage(dish.image_url)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Dish Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = dish.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = "Placeholder",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dish Name
            Text(
                text = dish.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Rating + Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${dish.price}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "⭐ ${dish.rating}",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quantity Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                IconButton(
                    onClick = {
                        if (quantity > 0) {
                            quantity--
                            wasAddedToCart = false // reset state
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minus),
                        contentDescription = "Decrease",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                IconButton(
                    onClick = {
                        quantity++
                        wasAddedToCart = false // reset state
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Increase",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add to Cart Button
            Button(
                onClick = {
                    if (quantity > 0) {
                        val updatedDish = dish.copy(cuisineId = cuisineId)
                        onAddToCart(updatedDish, quantity)
                        wasAddedToCart = true
                    }
                },
                enabled = quantity > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (wasAddedToCart) "Added to Cart" else "Add to Cart")
            }
        }
    }
}
