package com.example.todoapps

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

data class Task(
    val title: String = "",
    var isChecked: Boolean = false
)
