package com.example.projecthub.uiscreens

import androidx.compose.runtime.Composable

@Composable
fun rememberSoundClick(onClick: () -> Unit): () -> Unit = onClick
