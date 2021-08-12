package com.ui.main.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ui.main.splash.SplashState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    val splashLiveData : LiveData<SplashState> get() = splashMutableLiveData
    private val splashMutableLiveData = MutableLiveData<SplashState>()
    init {
        viewModelScope.launch {
            delay(3000)
            updateLiveData()
        }

    }

    private fun updateLiveData() {
        splashMutableLiveData.postValue(SplashState.AuthActivity())
    }

}