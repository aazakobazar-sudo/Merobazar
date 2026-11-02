package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.MeroBazarSeedData
import com.example.ui.theme.CrimsonRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    selectedCategory: Int,
    selectedDistrict: String,
    inStockOnly: Boolean,
    maxPrice: Long?,
    sortOption: String,
    isNepali: Boolean,
    onApply: (cat: Int, dist: String, inStock: Boolean, maxPrice: Long?, sort: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempCat by remember { mutableStateOf(selectedCategory) }
    var tempDist by remember { mutableStateOf(selectedDistrict) }
    var tempInStock by remember { mutableStateOf(inStockOnly) }
    var tempMaxPriceStr by remember { mutableStateOf(maxPrice?.toString() ?: "") }
    var tempSort by remember { mutableStateOf(sortOption) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNepali) "फिल्टर र क्रमबद्ध (Filters)" else "Filters & Sorting",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Sorting options
                Text(
                    text = if (isNepali) "क्रमबद्ध (Sort by):" else "Sort by:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val sortItems = listOf(
                        "popular" to if (isNepali) "लोकप्रिय" else "Popular",
                        "price_low" to if (isNepali) "सस्तो पहिले" else "Price: Low to High",
                        "price_high" to if (isNepali) "महँगो पहिले" else "Price: High to Low",
                        "discount" to if (isNepali) "धेरै छुट" else "Max Discount"
                    )
                    sortItems.forEach { (key, label) ->
                        FilterChip(
                            selected = tempSort == key,
                            onClick = { tempSort = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonRed.copy(alpha = 0.15f),
                                selectedLabelColor = CrimsonRed
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In stock toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isNepali) "मौज्दातमा मात्र (In-Stock Only)" else "In-Stock Only",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = tempInStock,
                        onCheckedChange = { tempInStock = it }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Max price filter
                Text(
                    text = if (isNepali) "अधिकतम मूल्य (Max Price in ₨):" else "Max Price (₨):",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tempMaxPriceStr,
                    onValueChange = { tempMaxPriceStr = it.filter { c -> c.isDigit() } },
                    placeholder = { Text(if (isNepali) "जस्तै: १०००" else "e.g. 1000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // District selection
                Text(
                    text = if (isNepali) "जिल्ला (District):" else "District:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                val sampleDistricts = listOf("Kathmandu", "Pokhara (Kaski)", "Biratnagar (Morang)", "Chitwan", "Kailali")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    sampleDistricts.forEach { distName ->
                        val clean = distName.substringBefore(" ")
                        val isSelected = tempDist.equals(clean, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                tempDist = if (isSelected) "" else clean
                            },
                            label = { Text(distName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonRed.copy(alpha = 0.15f),
                                selectedLabelColor = CrimsonRed
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = tempMaxPriceStr.toLongOrNull()
                    onApply(tempCat, tempDist, tempInStock, p, tempSort)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text(if (isNepali) "लागू गर्नुहोस्" else "Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // Reset
                    onApply(0, "", false, null, "popular")
                }
            ) {
                Text(if (isNepali) "रिसेट" else "Reset")
            }
        }
    )
}
