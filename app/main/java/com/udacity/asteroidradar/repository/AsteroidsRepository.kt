package com.udacity.asteroidradar.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.API_KEY
import com.udacity.asteroidradar.api.NasaAPI
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AsteroidsRepository(private val database: AsteroidsDatabase) {
    fun getSavedAsteroids(): List<Asteroid> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = formatter.format(Date())
        return database.asteroidDao.getAsteroids(today).asDomainModel()
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val minusDays = 7
            calendar.add(Calendar.DAY_OF_YEAR,  minusDays)


            val nextDays = formatter.format(calendar.time)
            val today = formatter.format(Date())
            var values = NasaAPI.retrofitService.getAsteroids(today, nextDays, API_KEY)
            val jsonValues = JSONObject(values)
            val parsedValues = parseAsteroidsJsonResult(jsonValues)
            val x = parsedValues.asDatabaseModel()
            database.asteroidDao.insertAll(*parsedValues.asDatabaseModel())
        }
    }
}