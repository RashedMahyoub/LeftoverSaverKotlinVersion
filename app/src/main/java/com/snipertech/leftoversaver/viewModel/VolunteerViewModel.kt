package com.snipertech.leftoversaver.viewModel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.repository.AuthRepository
import com.snipertech.leftoversaver.repository.VolunteerRepository
import com.snipertech.leftoversaver.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class VolunteerViewModel
@ViewModelInject
constructor(
    private val volunteerRepository: VolunteerRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _dataStateOrders: MutableLiveData<DataState<List<Order>?>> = MutableLiveData()
    private val _dataStateVol: MutableLiveData<DataState<Volunteer?>> = MutableLiveData()
    private val updateVolunteerLiveData = volunteerRepository.updateVolunteerLiveData

    val volunteerUpdatedLiveData: LiveData<String>
        get() = updateVolunteerLiveData
    val dataStateOrders: LiveData<DataState<List<Order>?>>
        get() = _dataStateOrders
    val dataStateVolunteer: LiveData<DataState<Volunteer?>>
        get() = _dataStateVol

    fun setStateEvent(VolunteerMainStateEvent: VolunteerMainStateEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (VolunteerMainStateEvent) {
                is VolunteerMainStateEvent.GetOrdersByCity -> {
                    volunteerRepository.getOrders(VolunteerMainStateEvent.city)
                        .onEach { dataState ->
                            _dataStateOrders.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is VolunteerMainStateEvent.RegisterVolunteer -> {
                    authRepository.insertVol(VolunteerMainStateEvent.vol)
                        .onEach { dataState ->
                            _dataStateVol.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is VolunteerMainStateEvent.SearchVolunteer -> {
                    authRepository.searchVol(VolunteerMainStateEvent.phoneNumber)
                        .onEach { dataState ->
                            _dataStateVol.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
            }
        }
    }

}

sealed class VolunteerMainStateEvent {
    data class RegisterVolunteer(val vol: Volunteer) : VolunteerMainStateEvent()
    data class SearchVolunteer(val phoneNumber: String) : VolunteerMainStateEvent()
    data class GetOrdersByCity(val city: String) : VolunteerMainStateEvent()
}