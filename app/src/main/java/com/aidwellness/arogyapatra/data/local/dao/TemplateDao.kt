package com.aidwellness.arogyapatra.data.local.dao

import androidx.room.*
import com.aidwellness.arogyapatra.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY isDefault DESC, title ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE type = :type LIMIT 1")
    fun getTemplateByType(type: String): TemplateEntity?

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    fun getTemplateById(id: Long): TemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTemplate(template: TemplateEntity): Long

    @Update
    fun updateTemplate(template: TemplateEntity): Int

    @Delete
    fun deleteTemplate(template: TemplateEntity): Int

    @Query("SELECT COUNT(*) FROM templates")
    fun getTemplateCount(): Int
}
