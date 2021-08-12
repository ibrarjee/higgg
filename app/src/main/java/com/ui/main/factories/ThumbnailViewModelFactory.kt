package com.ui.main.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.ui.main.models.ThumbnailModel
import com.ui.main.viewModel.ThumbnailViewModel
import java.lang.IllegalArgumentException

class ThumbnailViewModelFactory() : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThumbnailModel::class.java))
        {
            return ThumbnailViewModel() as T
        }
        throw IllegalArgumentException("UnknownViewModel")
    }
}