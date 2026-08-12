package com.aidwellness.arogyapatra.data.repository

import com.aidwellness.arogyapatra.data.local.dao.TemplateDao
import com.aidwellness.arogyapatra.data.local.entity.TemplateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TemplateRepository(private val templateDao: TemplateDao) {
    fun getAllTemplates(): Flow<List<TemplateEntity>> = templateDao.getAllTemplates()
    suspend fun getTemplateByType(type: String): TemplateEntity? = withContext(Dispatchers.IO) { templateDao.getTemplateByType(type) }
    suspend fun getTemplateById(id: Long): TemplateEntity? = withContext(Dispatchers.IO) { templateDao.getTemplateById(id) }
    suspend fun insertTemplate(template: TemplateEntity): Long = withContext(Dispatchers.IO) { templateDao.insertTemplate(template) }
    suspend fun updateTemplate(template: TemplateEntity): Int = withContext(Dispatchers.IO) { templateDao.updateTemplate(template) }
    suspend fun deleteTemplate(template: TemplateEntity): Int = withContext(Dispatchers.IO) { templateDao.deleteTemplate(template) }
}
