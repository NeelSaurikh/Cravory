package com.example.cravory.ui.theme.activities


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.cravory.R
import com.example.cravory.database.AppDatabase
import com.example.cravory.model.Cuisine
import com.example.cravory.model.Dish
import com.example.cravory.network.ApiClient
import com.example.cravory.repository.FoodRepository
import com.example.cravory.ui.theme.CravoryTheme
import com.example.cravory.viewmodel.CuisineViewModel
import com.example.cravory.viewmodel.CuisineViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.jvm.java


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CravoryTheme {
                CuisineScreen(
                    viewModel = viewModel(factory = CuisineViewModelFactory(FoodRepository()))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuisineScreen(viewModel: CuisineViewModel = viewModel(factory = CuisineViewModelFactory(FoodRepository()))) {
    val cuisines = viewModel.cuisines
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()
    var selectedCuisine by remember { mutableStateOf<Cuisine?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val customFont = FontFamily(
        Font(R.font.greatvibes_regular) // Your font name
    )

    if (cuisines.isNotEmpty() && selectedCuisine == null) {
        selectedCuisine = cuisines[0]
    }
    // Initialize selectedCuisine when cuisines list is first loaded
    LaunchedEffect(cuisines) {
        if (cuisines.isNotEmpty() && selectedCuisine == null) {
            selectedCuisine = cuisines[0]
        }
    }

    // Update selectedCuisine based on LazyRow scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collectLatest { index ->
                if (index in cuisines.indices) {
                    selectedCuisine = cuisines[index]
                }
            }
    }

    // Load more cuisines when nearing the end of the list
    LaunchedEffect(cuisines.size) {
        snapshotFlow { listState.layoutInfo }
            .collectLatest { layoutInfo ->
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                if (lastVisibleItem != null && lastVisibleItem >= cuisines.size - 2) {
                    viewModel.loadCuisines()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cravory",
                        fontFamily = customFont,
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White
                        )
                    )
                },
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
                modifier = Modifier
                    .fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF333333),  // Dark background
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            )
        }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && cuisines.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadCuisines() }) {
                            Text("Retry")
                        }
                    }
                }

                cuisines.isEmpty() -> {
                    Text(
                        text = "No cuisines available",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyRow(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cuisines) { cuisine ->
                                val context = LocalContext.current // Move LocalContext.current here
                                CuisineCardView(cuisine = cuisine, onClick = { selectedCuisine ->
                                    val intent = Intent(context, DishesActivity::class.java)
                                    intent.putExtra("CUISINE_NAME", selectedCuisine.cuisineName)
                                    intent.putExtra("CUISINE_ID", selectedCuisine.cuisineId)
                                    context.startActivity(intent)
                                })
                            }

                            if (isLoading) {
                                item {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }

                        selectedCuisine?.let { cuisine ->
                            Text(
                                text = "Top Dishes in ${cuisine.cuisineName}",
                                fontFamily = customFont,
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
                            )
                            DishGridView(
                                dishes = cuisine.items
                                    .sortedByDescending { it.rating } // Sort by rating descending
                                    .take(4) // Then take top 4
                            )

                        }
                    }
                }
            }

            // Cart Button - Bottom Start
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
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .width(200.dp)
            )

            // Language Toggle FAB Group - Bottom End
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                if (isFabExpanded) {
                    FloatingActionButton(
                        onClick = { isFabExpanded = false /* TODO: Switch to English */ },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) { Text("EN", color = Color.White) }

                    FloatingActionButton(
                        onClick = { isFabExpanded = false /* TODO: Switch to Hindi */ },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) { Text("हिं", color = Color.White) }
                }

                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.language_24),
                        contentDescription = "Language"
                    )
                }
            }
        }
    }
}

@Composable
fun CuisineCardView(cuisine: Cuisine, onClick: (Cuisine) -> Unit) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val customFont = FontFamily(
        Font(R.font.greatvibes_regular) // Your font name
    )

    LaunchedEffect(cuisine.cuisineImageUrl) {
        coroutineScope.launch {
            bitmap = ApiClient.loadImage(cuisine.cuisineImageUrl)
        }
    }

    Card(
        modifier = Modifier
            .width(400.dp)
            .height(240.dp)
            .padding(4.dp)
            .clickable { onClick(cuisine) },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = cuisine.cuisineName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.placeholder),
                    contentDescription = cuisine.cuisineName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = cuisine.cuisineName,
                    fontFamily = customFont,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DishGridView(dishes: List<Dish>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(dishes) { dish ->
            DishTileCard(dish = dish)
        }
    }
}

@Composable
fun DishTileCard(dish: Dish) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var bitmap by remember { mutableStateOf<Bitmap?>(null) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(dish.image_url) {
                coroutineScope.launch {
                    bitmap = ApiClient.loadImage(dish.image_url)
                }
            }

            val imageModifier = Modifier
                .height(85.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))

            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = dish.name,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.placeholder),
                    contentDescription = dish.name,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dish.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "₹${dish.price}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}