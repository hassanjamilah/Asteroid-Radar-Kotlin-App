package com.udacity.asteroidradar.main

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Asteroid>?) {
    val adapter = recyclerView.adapter as AsteroidsAdapter
    adapter.submitList(data)
}

@BindingAdapter("statusImage")
fun getStatusImage(imageView: ImageView, asteroid: Asteroid) {
    if (asteroid.isPotentiallyHazardous) {
        imageView.setImageResource(R.drawable.ic_status_potentially_hazardous)
    } else {
        imageView.setImageResource(R.drawable.ic_status_normal)
    }
}

@BindingAdapter("imageURL")
fun bindImage(imageView: ImageView, imageURL: String?) {
    imageURL?.let {
        val imageURI = it.toUri().buildUpon().scheme("https").build()
        Glide.with(imageView)
            .load(imageURI)
            .apply(RequestOptions()
                .placeholder(R.drawable.placeholder_picture_of_day)
                .error(com.google.android.material.R.drawable.mtrl_ic_error)
            )
            .into(imageView)
    }
}