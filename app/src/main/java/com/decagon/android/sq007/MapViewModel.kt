package com.decagon.android.sq007

import android.location.Location
import androidx.lifecycle.MutableLiveData

class MapViewModel {

    var mapLocation = MutableLiveData<List<LocationModel>>()
}