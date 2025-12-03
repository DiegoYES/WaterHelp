package com.example.waterhelp.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. DATASTORE (Para el límite)
val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val LIMIT_KEY = doublePreferencesKey("daily_limit")

    val limitFlow: Flow<Double> = context.dataStore.data.map { it[LIMIT_KEY] ?: 100.0 }

    suspend fun saveLimit(limit: Double) {
        context.dataStore.edit { it[LIMIT_KEY] = limit }
    }
}

// 2. ROOM (Para los registros diarios)
@Entity
data class WaterRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // Guardamos como Epoch Day (Long)
    val liters: Double
)

@Dao
interface WaterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WaterRecord)

    // Obtener consumo total de un día específico
    @Query("SELECT SUM(liters) FROM WaterRecord WHERE date = :date")
    fun getLitersByDate(date: Long): Flow<Double?>

    // CAMBIO: Obtener TODO el historial ordenado por fecha (sin filtro de días)
    @Query("SELECT * FROM WaterRecord ORDER BY date ASC")
    fun getAllHistory(): Flow<List<WaterRecord>>
}

@Database(entities = [WaterRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): WaterDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "water.db")
                    .build().also { INSTANCE = it }
            }
        }
    }
}