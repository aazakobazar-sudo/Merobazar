package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MeroBazarSeedData
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandBgLight
import com.example.ui.theme.BrandIndigo
import com.example.ui.theme.BrandSaffron
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.JadeGreen
import kotlin.math.roundToInt

data class ProvinceGeo(
    val nameKey: String,
    val nameNe: String,
    val capital: String,
    val lat: Double,
    val lng: Double,
    val color: Color
)

data class NepalSearchPlace(
    val titleNe: String,
    val titleEn: String,
    val prov: String,
    val dist: String,
    val local: String,
    val ward: Int,
    val road: String,
    val lat: Double,
    val lng: Double
)

val nepalProvincesList = listOf(
    ProvinceGeo("Sudurpashchim", "सुदूरपश्चिम प्रदेश", "धनगढी (Dhangadhi)", 28.6833, 80.6000, Color(0xFF5E35B1)),
    ProvinceGeo("Karnali", "कर्णाली प्रदेश", "वीरेन्द्रनगर (Surkhet)", 28.6019, 81.6338, Color(0xFF00897B)),
    ProvinceGeo("Lumbini", "लुम्बिनी प्रदेश", "बुटवल (Butwal)", 27.7006, 83.4484, Color(0xFFD81B60)),
    ProvinceGeo("Gandaki", "गण्डकी प्रदेश", "पोखरा (Pokhara)", 28.2096, 83.9856, BrandSaffron),
    ProvinceGeo("Bagmati", "बागमती प्रदेश", "काठमाडौँ (Kathmandu)", 27.7172, 85.3240, BrandIndigo),
    ProvinceGeo("Madhesh", "मधेश प्रदेश", "जनकपुर (Janakpur)", 26.7288, 85.9244, Color(0xFFE65100)),
    ProvinceGeo("Koshi", "कोशी प्रदेश", "विराटनगर (Biratnagar)", 26.4525, 87.2718, Color(0xFF1E88E5))
)

val popularNepalPlaces = listOf(
    NepalSearchPlace("काठमाडौँ नयाँ बानेश्वर", "Kathmandu New Baneshwor", "Bagmati", "Kathmandu", "काठमाडौँ महानगरपालिका", 10, "नयाँ बानेश्वर मार्ग", 27.6934, 85.3421),
    NepalSearchPlace("काठमाडौँ दरवारमार्ग", "Kathmandu Durbarmarg", "Bagmati", "Kathmandu", "काठमाडौँ महानगरपालिका", 1, "दरवारमार्ग सडक", 27.7118, 85.3175),
    NepalSearchPlace("काठमाडौँ ठमेल", "Kathmandu Thamel", "Bagmati", "Kathmandu", "काठमाडौँ महानगरपालिका", 26, "ठमेल मार्ग", 27.7152, 85.3109),
    NepalSearchPlace("काठमाडौँ कलङ्की", "Kathmandu Kalanki", "Bagmati", "Kathmandu", "काठमाडौँ महानगरपालिका", 14, "कलङ्की चोक, रिङरोड", 27.6938, 85.2818),
    NepalSearchPlace("काठमाडौँ कोटेश्वर", "Kathmandu Koteshwor", "Bagmati", "Kathmandu", "काठमाडौँ महानगरपालिका", 32, "कोटेश्वर चोक", 27.6773, 85.3489),
    NepalSearchPlace("ललितपुर पाटन दरवार", "Lalitpur Patan Durbar", "Bagmati", "Lalitpur", "ललितपुर महानगरपालिका", 12, "मंगलबजार मार्ग", 27.6730, 85.3255),
    NepalSearchPlace("ललितपुर पुल्चोक", "Lalitpur Pulchowk", "Bagmati", "Lalitpur", "ललितपुर महानगरपालिका", 3, "पुल्चोक सडक", 27.6788, 85.3168),
    NepalSearchPlace("ललितपुर जावलाखेल", "Lalitpur Jawalakhel", "Bagmati", "Lalitpur", "ललितपुर महानगरपालिका", 4, "जावलाखेल चोक", 27.6739, 85.3128),
    NepalSearchPlace("भक्तपुर दरवार स्क्वायर", "Bhaktapur Durbar Square", "Bagmati", "Bhaktapur", "भक्तपुर नगरपालिका", 9, "दरवार मार्ग", 27.6722, 85.4283),
    NepalSearchPlace("भक्तपुर सूर्यविनायक", "Bhaktapur Suryabinayak", "Bagmati", "Bhaktapur", "सूर्यविनायक नगरपालिका", 5, "अरनिको राजमार्ग", 27.6628, 85.4225),
    NepalSearchPlace("पोखरा लेकसाइड", "Pokhara Lakeside", "Gandaki", "Kaski", "पोखरा महानगरपालिका", 6, "लेकसाइड मार्ग", 28.2096, 83.9573),
    NepalSearchPlace("पोखरा महेन्द्रपुल", "Pokhara Mahendrapool", "Gandaki", "Kaski", "पोखरा महानगरपालिका", 9, "महेन्द्रपुल बजार", 28.2238, 83.9877),
    NepalSearchPlace("चितवन नारायणगढ", "Chitwan Narayangarh", "Bagmati", "Chitwan", "भरतपुर महानगरपालिका", 2, "सहिद चोक, मेनरोड", 27.6984, 84.4220),
    NepalSearchPlace("बुटवल ट्राफिक चोक", "Butwal Traffic Chowk", "Lumbini", "Rupandehi", "बुटवल उपमहानगरपालिका", 6, "सिद्धार्थ राजमार्ग", 27.7006, 83.4484),
    NepalSearchPlace("विराटनगर मेनरोड", "Biratnagar Main Road", "Koshi", "Morang", "विराटनगर महानगरपालिका", 7, "मेनरोड बजार", 26.4525, 87.2718),
    NepalSearchPlace("धरान भानुचोक", "Dharan Bhanu Chowk", "Koshi", "Sunsari", "धरान उपमहानगरपालिका", 2, "भानुचोक घण्टाघर मार्ग", 26.8124, 87.2834),
    NepalSearchPlace("झापा विर्तामोड", "Jhapa Birtamod", "Koshi", "Jhapa", "विर्तामोड नगरपालिका", 4, "मुक्ति चोक मार्ग", 26.6341, 87.9944),
    NepalSearchPlace("नेपालगन्ज विपी चोक", "Nepalgunj BP Chowk", "Lumbini", "Banke", "नेपालगन्ज उपमहानगरपालिका", 2, "विपी चोक", 28.0500, 81.6167),
    NepalSearchPlace("धनगढी चौराहा", "Dhangadhi Chauraha", "Sudurpashchim", "Kailali", "धनगढी उपमहानगरपालिका", 1, "मेनरोड चौराहा", 28.6833, 80.6000),
    NepalSearchPlace("जनकपुर जानकी मन्दिर", "Janakpur Janaki Mandir", "Madhesh", "Dhanusha", "जनकपुरधाम उपमहानगरपालिका", 4, "जानकी मन्दिर मार्ग", 26.7288, 85.9244),
    NepalSearchPlace("सुर्खेत वीरेन्द्रनगर", "Surkhet Birendranagar", "Karnali", "Surkhet", "वीरेन्द्रनगर नगरपालिका", 6, "वीरेन्द्र चोक", 28.6019, 81.6338)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NepalMapScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onExploreShops: (String) -> Unit = {},
    onExploreProducts: (String) -> Unit = {},
    onLocationTagged: ((prov: String, dist: String, local: String, ward: Int, road: String, lat: Double, lng: Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val isNepali by viewModel.isNepali.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val products by viewModel.products.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Location State
    var selectedProvinceKey by remember { mutableStateOf(currentUser?.prov ?: "Bagmati") }
    val currentProvince = nepalProvincesList.find { it.nameKey == selectedProvinceKey } ?: nepalProvincesList[4]
    val districts = MeroBazarSeedData.provinces[selectedProvinceKey] ?: listOf("Kathmandu")
    var selectedDistrict by remember { mutableStateOf(currentUser?.dist ?: districts.firstOrNull() ?: "Kathmandu") }
    var selectedLocal by remember { mutableStateOf(currentUser?.local?.ifBlank { "काठमाडौँ महानगरपालिका" } ?: "काठमाडौँ महानगरपालिका") }
    var selectedWard by remember { mutableStateOf((currentUser?.ward ?: 10).toString()) }
    var selectedRoad by remember { mutableStateOf(currentUser?.toleRoad?.ifBlank { "नयाँ बानेश्वर मार्ग" } ?: "नयाँ बानेश्वर मार्ग") }
    var currentLat by remember { mutableFloatStateOf((currentUser?.la ?: currentProvince.lat).toFloat()) }
    var currentLng by remember { mutableFloatStateOf((currentUser?.lo ?: currentProvince.lng).toFloat()) }

    // Map Interactive Zoom and Pan State
    var mapScale by remember { mutableFloatStateOf(1f) }
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }

    // Pin relative offset on map container (pixels)
    var pinX by remember { mutableFloatStateOf(0f) }
    var pinY by remember { mutableFloatStateOf(0f) }

    // Search query state
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }

    val filteredPlaces = remember(searchQuery) {
        if (searchQuery.trim().length < 2) emptyList()
        else {
            popularNepalPlaces.filter {
                it.titleNe.contains(searchQuery, ignoreCase = true) ||
                it.titleEn.contains(searchQuery, ignoreCase = true) ||
                it.dist.contains(searchQuery, ignoreCase = true) ||
                it.road.contains(searchQuery, ignoreCase = true) ||
                "वडा ${it.ward}".contains(searchQuery) ||
                "${it.ward}".contains(searchQuery)
            }
        }
    }

    val shopIdsInDistrict = shops.filter { it.dist.equals(selectedDistrict, ignoreCase = true) || it.prov.equals(selectedProvinceKey, ignoreCase = true) }.map { it.id }.toSet()
    val shopsInDistrict = shopIdsInDistrict.size
    val productsInDistrict = products.count { it.mid in shopIdsInDistrict }

    var isTaggedSuccess by remember { mutableStateOf(currentUser?.isMapTagged == true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isNepali) "नेपाल नक्सा (अफलाइन गुगल म्याप)" else "Nepal Map (Offline Google Map)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isNepali) "अनिवार्य लोकेसन, वडा र बाटो ट्याग" else "Mandatory Location & Road Tagging",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandIndigo, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BrandBgLight)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Offline Downloaded Map Status Badge
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isNepali) "📶 डाउनलोड गरिएको अफलाइन गुगल म्याप" else "📶 Downloaded Offline Google Map",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = if (isNepali)
                                "इन्टरनेट बिना नै नेपालका ७ प्रदेश, ७७ जिल्ला, मुख्य सडक र वडासहितको नक्सा उपलब्ध छ।"
                            else
                                "Pre-downloaded full Nepal map with expressways, highways, wards, and roads.",
                            fontSize = 11.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar for Nepal Places, Wards, and Roads
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    showSearchResults = it.trim().length >= 2
                },
                label = { Text(if (isNepali) "नेपालको स्थान, वडा वा बाटो खोज्नुहोस् (उदा. नयाँ बानेश्वर, लेकसाइड, वडा १०)" else "Search place, ward or road in Nepal") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandIndigo) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            showSearchResults = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("map_search_input")
            )

            // Search Results Popup Chips
            if (showSearchResults && filteredPlaces.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = if (isNepali) "नक्सामा भेटिएका स्थानहरू (ट्याप गरी छान्नुहोस्):" else "Matching places on Nepal map:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandIndigo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        filteredPlaces.take(5).forEach { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedProvinceKey = place.prov
                                        selectedDistrict = place.dist
                                        selectedLocal = place.local
                                        selectedWard = place.ward.toString()
                                        selectedRoad = place.road
                                        currentLat = place.lat.toFloat()
                                        currentLng = place.lng.toFloat()
                                        searchQuery = place.titleNe
                                        showSearchResults = false
                                        isTaggedSuccess = false
                                        Toast.makeText(context, "स्थान चयन: ${place.titleNe}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(place.titleNe, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${place.local}, वडा ${place.ward}, ${place.road} (${place.dist})", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // INTERACTIVE OFFLINE GOOGLE MAP VIEWER
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = BrandIndigo, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isNepali) "गुगल म्याप शैली नक्सा (Google Maps Style)" else "Nepal Road Map (Google Style)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = BrandIndigo
                            )
                        }

                        // Zoom Controls (+ / - / Reset)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { mapScale = (mapScale + 0.3f).coerceAtMost(3.5f) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { mapScale = (mapScale - 0.3f).coerceAtLeast(1.0f) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    mapScale = 1.0f
                                    mapOffsetX = 0f
                                    mapOffsetY = 0f
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Embedded Map Canvas with Gesture and Pin Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    mapScale = (mapScale * zoom).coerceIn(1.0f, 3.5f)
                                    mapOffsetX += pan.x
                                    mapOffsetY += pan.y
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // The actual embedded Google Maps style offline map of Nepal
                        Image(
                            painter = painterResource(id = R.drawable.nepal_map_google),
                            contentDescription = "Downloaded Nepal Map Google Style",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = mapScale,
                                    scaleY = mapScale,
                                    translationX = mapOffsetX,
                                    translationY = mapOffsetY
                                ),
                            contentScale = ContentScale.Crop
                        )

                        // Visual Interactive Pin Marker (Centered / Tagged)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .offset { IntOffset(pinX.roundToInt(), pinY.roundToInt()) }
                                .padding(bottom = 16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandIndigo,
                                shadowElevation = 4.dp
                            ) {
                                Text(
                                    text = "$selectedDistrict, वडा $selectedWard\n$selectedRoad",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Pin",
                                tint = CrimsonRed,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        // Map Legend overlay
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.92f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFE53935), CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("राजमार्ग / वडा सडक", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(8.dp).background(BrandIndigo, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("स्थान ट्याग 📍", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isNepali)
                            "💡 नक्सालाई जुम गर्न (+/-) वा दुई औंलाले सार्नुहोस्। तल जिल्ला, वडा र बाटो छानेर स्थान ट्याग गर्नुहोस्।"
                        else
                            "💡 Pan and zoom the offline map. Select district, ward, and road below to pin your exact location.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // PROVINCE SELECTOR
            // ==========================================
            Text(
                text = if (isNepali) "१. प्रदेश छान्नुहोस् (Province):" else "1. Select Province:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BrandIndigo
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nepalProvincesList.forEach { p ->
                    val isSelected = p.nameKey == selectedProvinceKey
                    Surface(
                        modifier = Modifier
                            .clickable {
                                selectedProvinceKey = p.nameKey
                                val newDistList = MeroBazarSeedData.provinces[p.nameKey] ?: listOf("Kathmandu")
                                selectedDistrict = newDistList.firstOrNull() ?: ""
                                currentLat = p.lat.toFloat()
                                currentLng = p.lng.toFloat()
                                isTaggedSuccess = false
                            }
                            .testTag("province_chip_${p.nameKey}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) p.color else Color(0xFFF1F5F9),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = p.nameNe,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = p.capital.substringBefore(" "),
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // DISTRICT FLOW CHIPS (७७ जिल्ला)
            // ==========================================
            Text(
                text = if (isNepali) "२. जिल्ला छान्नुहोस् (${currentProvince.nameNe} - ${districts.size} जिल्ला):" else "2. Select District:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BrandIndigo
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                districts.forEach { dist ->
                    val isSel = dist == selectedDistrict
                    Surface(
                        modifier = Modifier
                            .clickable {
                                selectedDistrict = dist
                                isTaggedSuccess = false
                            }
                            .testTag("district_chip_$dist"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSel) currentProvince.color else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSel) currentProvince.color else Color(0xFFCBD5E1)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSel) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = dist,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // DETAILED LOCAL BODY, WARD & ROAD FORM (MANDATORY)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE5F1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isNepali) "३. स्थानीय तह, वडा र बाटो विवरण (अनिवार्य / Mandatory)" else "3. Local Body, Ward & Road (Mandatory)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrandIndigo
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Local Municipality
                    OutlinedTextField(
                        value = selectedLocal,
                        onValueChange = {
                            selectedLocal = it
                            isTaggedSuccess = false
                        },
                        label = { Text(if (isNepali) "गाउँपालिका / नगरपालिका / महानगरपालिका *" else "Local Body / Municipality *") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = BrandIndigo) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("map_local_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ward Number (Mandatory)
                        OutlinedTextField(
                            value = selectedWard,
                            onValueChange = {
                                selectedWard = it
                                isTaggedSuccess = false
                            },
                            label = { Text(if (isNepali) "वडा नं. *" else "Ward No. *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandIndigo) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("map_ward_input")
                        )

                        // Road / Street / Tole (Mandatory)
                        OutlinedTextField(
                            value = selectedRoad,
                            onValueChange = {
                                selectedRoad = it
                                isTaggedSuccess = false
                            },
                            label = { Text(if (isNepali) "बाटो / सडक / मार्ग / टोल *" else "Road / Street / Tole *") },
                            leadingIcon = { Icon(Icons.Default.Navigation, contentDescription = null, tint = BrandIndigo) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(2f)
                                .testTag("map_road_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // GPS Coordinates display
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "GPS पिन: ${"%.4f".format(currentLat)}° N, ${"%.4f".format(currentLng)}° E",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "अफलाइन प्रमाणित",
                                fontSize = 11.sp,
                                color = JadeGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tagged Success Notification
                    if (isTaggedSuccess) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = JadeGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isNepali) "✅ नक्सामा लोकेसन सफलतापूर्वक ट्याग भयो!" else "Location tagged successfully!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = "$selectedDistrict, $selectedLocal, वडा $selectedWard, $selectedRoad",
                                        fontSize = 11.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    }

                    // Mandatory Confirm Tagging Button
                    Button(
                        onClick = {
                            val wardInt = selectedWard.toIntOrNull() ?: 1
                            val roadFinal = selectedRoad.ifBlank { "मुख्य सडक" }
                            val localFinal = selectedLocal.ifBlank { selectedDistrict }

                            viewModel.tagUserLocation(
                                prov = selectedProvinceKey,
                                dist = selectedDistrict,
                                lat = currentLat.toDouble(),
                                lng = currentLng.toDouble(),
                                local = localFinal,
                                ward = wardInt,
                                toleRoad = roadFinal
                            )

                            onLocationTagged?.invoke(
                                selectedProvinceKey,
                                selectedDistrict,
                                localFinal,
                                wardInt,
                                roadFinal,
                                currentLat.toDouble(),
                                currentLng.toDouble()
                            )

                            isTaggedSuccess = true
                            Toast.makeText(
                                context,
                                if (isNepali) "📍 $selectedDistrict वडा $wardInt, $roadFinal ट्याग भयो!" else "Location tagged!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JadeGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_tag_location")
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNepali) "📍 यो लोकेसन अनिवार्य ट्याग गर्नुहोस्" else "Confirm & Tag Location (Mandatory)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onExploreShops(selectedDistrict) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isNepali) "स्थानीय पसलहरू ($shopsInDistrict)" else "Shops ($shopsInDistrict)", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onExploreProducts(selectedDistrict) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isNepali) "स्थानीय सामानहरू ($productsInDistrict)" else "Items ($productsInDistrict)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
