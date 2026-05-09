package com.example.orthodoxapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.R
import com.example.orthodoxapp.data.model.Church
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.*
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NationalMapViewScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val churches by viewModel.churches.collectAsState()
    var selectedChurch by remember { mutableStateOf<Church?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Default focus on Addis Ababa (Simulated User Location)
    val userLocation = LatLng(9.03, 38.74)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 12f)
    }

    val filterOptions = listOf("All", "Churches", "Nearest", "My Location")
    var selectedFilter by remember { mutableStateOf("All") }

    // Helper to calculate distance (Haversine formula approximation)
    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- GOOGLE MAP ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true, // Shows the blue dot
                mapType = MapType.NORMAL,
                isTrafficEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false, // We use our own custom chip
                compassEnabled = true,
                mapToolbarEnabled = true
            )
        ) {
            // User Location Marker (Optional as blue dot is enabled, but good for focus)
            Marker(
                state = MarkerState(position = userLocation),
                title = "Current Focus",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                zIndex = 1f
            )

            churches.forEach { church ->
                if (church.latitude != null && church.longitude != null) {
                    val position = LatLng(church.latitude!!, church.longitude!!)
                    Marker(
                        state = MarkerState(position = position),
                        title = church.name,
                        snippet = church.location,
                        icon = if (selectedChurch == church) 
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) 
                        else 
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                        onClick = {
                            selectedChurch = church
                            // Smoothly move camera to selected church
                            true
                        }
                    )
                }
            }
        }

        // --- TOP OVERLAY: SEARCH & FILTERS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("Search church or diocese...", color = Color.LightGray, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        modifier = Modifier.clickable { 
                            selectedFilter = filter
                            if (filter == "Nearest" && churches.isNotEmpty()) {
                                // Find nearest church
                                val nearest = churches.filter { it.latitude != null && it.longitude != null }
                                    .minByOrNull { getDistance(userLocation.latitude, userLocation.longitude, it.latitude!!, it.longitude!!) }
                                if (nearest != null) {
                                    selectedChurch = nearest
                                    viewModel.viewModelScope.launch {
                                        cameraPositionState.animate(
                                            update = CameraUpdateFactory.newLatLngZoom(LatLng(nearest.latitude!!, nearest.longitude!!), 14f),
                                            durationMs = 1000
                                        )
                                    }
                                }
                            } else if (filter == "My Location") {
                                viewModel.viewModelScope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(userLocation, 16f),
                                        durationMs = 1000
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF064E3B) else Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when(filter) {
                                    "All" -> Icons.Default.GridView
                                    "Churches" -> Icons.Default.Church
                                    "Nearest" -> Icons.Default.NearMe
                                    else -> Icons.Default.MyLocation
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                filter,
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Financial Summary Card
            Card(
                modifier = Modifier.width(240.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("National Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF064E3B))
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Income", fontSize = 12.sp, color = Color.Gray)
                        Text("Birr 1.2M", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { 0.7f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Color(0xFF10B981))
                    
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expense", fontSize = 12.sp, color = Color.Gray)
                        Text("Birr 450K", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { 0.3f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Color(0xFFEF4444))
                }
            }
        }

        // --- BOTTOM OVERLAY: CHURCH DETAILS ---
        if (selectedChurch != null) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        // Church Image
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1548625361-995777174668?q=80&w=400", // Sample Church Image
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedChurch?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.Gray)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(selectedChurch?.location ?: "Unknown Location", color = Color.Gray, fontSize = 12.sp)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                val dist = if (selectedChurch?.latitude != null && selectedChurch?.longitude != null) {
                                    getDistance(userLocation.latitude, userLocation.longitude, selectedChurch!!.latitude!!, selectedChurch!!.longitude!!)
                                } else 0.0
                                Text("${String.format("%.1f", dist)} km away", color = Color.Gray, fontSize = 12.sp)
                            }

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(Modifier.height(8.dp))

                            // Financial Activity Per Location
                            Text("Financial Activity", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Monthly Income", fontSize = 10.sp, color = Color.Gray)
                                    Text("Birr 125,000", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Monthly Expense", fontSize = 10.sp, color = Color.Gray)
                                    Text("Birr 85,000", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            
                            Button(
                                onClick = { /* Navigate */ },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF064E3B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Directions")
                            }
                        }
                    }
                }
                
                // Close button
                IconButton(
                    onClick = { selectedChurch = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
        }
    }
}

// Minimal BasicTextField replacement for demonstration if needed
@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        decorationBox = decorationBox
    )
}
