package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [QuestionEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class CmaDatabase : RoomDatabase() {
  abstract fun questionDao(): QuestionDao

  companion object {
    @Volatile
    private var INSTANCE: CmaDatabase? = null

    fun getInstance(context: Context): CmaDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          CmaDatabase::class.java,
          "cma_prep.db",
        ).build().also { INSTANCE = it }
      }
    }
  }
}
