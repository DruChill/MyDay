package com.example.myapplicationmyday.data

import androidx.lifecycle.LiveData

class DiaryRepository(private val diaryDao: DiaryDao) {
    val allEntries: LiveData<List<DiaryEntry>> = diaryDao.getAllEntries()
    
    suspend fun insert(entry: DiaryEntry): Long {
        return diaryDao.insert(entry)
    }
    
    suspend fun update(entry: DiaryEntry) {
        diaryDao.update(entry)
    }
    
    suspend fun delete(entry: DiaryEntry) {
        diaryDao.delete(entry)
    }
    
    suspend fun getEntryById(id: Long): DiaryEntry? {
        return diaryDao.getEntryById(id)
    }
}
