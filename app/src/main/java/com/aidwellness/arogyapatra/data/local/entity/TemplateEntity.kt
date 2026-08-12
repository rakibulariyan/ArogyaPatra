package com.aidwellness.arogyapatra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String,
    val bodyText: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
