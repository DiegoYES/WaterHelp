package com.example.waterhelp.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. DATASTORE (Preferencias para el límite)
val Context.dataStore by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    private val LIMIT_KEY = doublePreferencesKey("daily_limit")

    // Obtenemos el límite (si no existe, devolvemos 100.0 por defecto)
    val limitFlow: Flow<Double> = context.dataStore.data.map { it[LIMIT_KEY] ?: 100.0 }

    suspend fun saveLimit(limit: Double) {
        context.dataStore.edit { it[LIMIT_KEY] = limit }
    }
}

// 2. ROOM (Base de datos para registros)
@Entity
data class WaterRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // Guardamos la fecha como un número (Días desde 1970)
    val liters: Double
)

@Dao
interface WaterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WaterRecord)

    // Suma total de litros de un día
    @Query("SELECT SUM(liters) FROM WaterRecord WHERE date = :date")
    fun getLitersByDate(date: Long): Flow<Double?>

    // Historial para la gráfica (desde una fecha en adelante)
    @Query("SELECT * FROM WaterRecord WHERE date >= :startDate ORDER BY date ASC")
    fun getHistory(startDate: Long): Flow<List<WaterRecord>>
}

@Database(entities = [WaterRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): WaterDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "water_help.db")
                    .build().also { INSTANCE = it }
            }
        }
    }
}