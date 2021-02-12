package org.divineuk.divineapp.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.divineuk.divineapp.data.Resource
import org.divineuk.divineapp.data.home.HomePageRepository
import org.divineuk.divineapp.network.model.HomeContent

class HomeViewModel @ViewModelInject constructor(
    repository: HomePageRepository
) : ViewModel() {
    var homePageContent: LiveData<Resource<HomeContent>> = repository.getHomePageContent()
}