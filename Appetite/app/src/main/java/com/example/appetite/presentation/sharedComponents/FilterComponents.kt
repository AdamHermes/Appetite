package com.example.appetite.presentation.sharedComponents

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.appetite.R


@Composable
fun <T> FlowRowChips(options: List<T>, labels: List<String>, selected: T, onSelect: (T) -> Unit) {
    var currentRow = mutableListOf<Pair<T, String>>()
    val allRows = mutableListOf<List<Pair<T, String>>>()

    // Group items into rows based on available width
    options.zip(labels).forEach { (value, label) ->
        currentRow.add(value to label)
        // If we have enough items or this is the last item, create a new row
        if (currentRow.size >= 3 || options.zip(labels).indexOf(value to label) == options.size - 1) {
            allRows.add(currentRow.toList())
            currentRow.clear()
        }
    }

    // Render each row
    allRows.forEach { row ->
        Row(
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            row.forEach { (value, label) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorResource(R.color.green_but),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun MultiSelectChips(all: List<String>, selected: MutableList<String>) {
    var currentRow = mutableListOf<String>()
    val allRows = mutableListOf<List<String>>()

    // Group items into rows based on available width
    all.forEach { item ->
        currentRow.add(item)
        // If we have enough items or this is the last item, create a new row
        if (currentRow.size >= 3 || all.indexOf(item) == all.size - 1) {
            allRows.add(currentRow.toList())
            currentRow.clear()
        }
    }

    // Render each row
    allRows.forEach { row ->
        Row(
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            row.forEach { item ->
                val isSelected = selected.contains(item)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selected.remove(item) else selected.add(item)
                    },
                    label = { Text(item) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorResource(R.color.green_but),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}