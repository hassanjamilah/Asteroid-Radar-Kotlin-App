package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.APIService
import com.udacity.asteroidradar.api.API_KEY
import com.udacity.asteroidradar.api.AsteroidAPIFilter
import com.udacity.asteroidradar.api.NasaAPI
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : ViewModel() {

    private val _asteriods = MutableLiveData<List<Asteroid>>()
    val asteriods: LiveData<List<Asteroid>>
        get() = _asteriods

    private val _todayPicture = MutableLiveData<PictureOfDay?>()
    val todayPicture: LiveData<PictureOfDay?>
        get() = _todayPicture

    private val _navigateToAsteroid = MutableLiveData<Asteroid?>()
    val navigateToAsteroid: LiveData<Asteroid?>
        get() = _navigateToAsteroid

    private val database = getDatabase(application)
    private val asteroidRespository = AsteroidsRepository(database)

    init {
        getAsteroids()
        getPictureOfToday()
    }

    fun getAsteroids(filter: AsteroidAPIFilter = AsteroidAPIFilter.SHOW_ONE_WEEK) {
        // Sample Data
//        val asteriod1 = Asteroid(3333, "1111", "22-2-2022", 55.0, 33.0, 2332.0, 343.0, false)
//        val asteriod2 = Asteroid(4444, "2222", "22-3-2023", 55.0, 33.0, 2332.0, 343.0, false)
//        val asteriods = ArrayList<Asteroid>()
//        asteriods.add(asteriod1)
//        asteriods.add(asteriod2)
//        _asteriods.value = asteriods


        // Real Data
        viewModelScope.launch {
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val minusDays = when (filter) {
                    AsteroidAPIFilter.SHOW_ONE_DAY -> 0
                    AsteroidAPIFilter.SHOW_ONE_WEEK -> 7
                    AsteroidAPIFilter.SHOW_SAVED -> -1
                }

                if (filter == AsteroidAPIFilter.SHOW_SAVED) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val savedAsteroids = asteroidRespository.getSavedAsteroids()
                        _asteriods.postValue(savedAsteroids)
                    }
                } else {
                    calendar.add(Calendar.DAY_OF_YEAR, minusDays)


                    val nextDays = formatter.format(calendar.time)
                    val today = formatter.format(Date())
                    var values = NasaAPI.retrofitService.getAsteroids(today, nextDays, API_KEY)
                    val jsonValues = JSONObject(values)
                    val parsedValues = parseAsteroidsJsonResult(jsonValues)
                    _asteriods.value = parsedValues
                    asteroidRespository.refreshAsteroids()

                    Log.i(
                        "main_tag",
                        "The json values size of items is: ${parsedValues.size} ,  ${asteriods.value?.size}"
                    )
                }

            } catch (e: Exception) {
                Log.e("main_tag", "Unable to parse the items ${e.message}")
                viewModelScope.launch(Dispatchers.IO) {
                    val savedAsteroids = asteroidRespository.getSavedAsteroids()
                    _asteriods.postValue(savedAsteroids)
                }
            }
        }
    }

    private fun getPictureOfToday() {
        viewModelScope.launch {
            try {
                val result = NasaAPI.retrofitService.getPictureOfDay(API_KEY)
                Log.i("main_tag", "The image of today is: ${result}")
                _todayPicture.value = result
            } catch (e: Exception) {
                _todayPicture.value = null
            }
        }

    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToAsteroid.value = asteroid
    }

    fun finishNavigatingToDetails() {
        _navigateToAsteroid.value = null
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}