package com.snipertech.leftoversaver.viewModel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Item
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.repository.NeedyRepository
import com.snipertech.leftoversaver.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NeedyViewModel 
@ViewModelInject
constructor(
    private val needyRepository: NeedyRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _dataStateOrder: MutableLiveData<DataState<Order?>> = MutableLiveData()
    private val _dataStateBoolean: MutableLiveData<DataState<Boolean?>> = MutableLiveData()
    private val _dataStateDonors: MutableLiveData<DataState<List<Donor>?>> = MutableLiveData()
    private val _dataStateItems: MutableLiveData<DataState<List<Item>?>> = MutableLiveData()
    
    val dataStateOrder: LiveData<DataState<Order?>>
        get() = _dataStateOrder
    val dataStateBoolean: LiveData<DataState<Boolean?>>
        get() = _dataStateBoolean
    val dataStateDonors: LiveData<DataState<List<Donor>?>>
        get() = _dataStateDonors
    val dataStateItems: LiveData<DataState<List<Item>?>>
        get() = _dataStateItems

    fun setStateEvent(NeedyMainStateEvent: NeedyMainStateEvent){
        viewModelScope.launch(Dispatchers.IO) {
            when(NeedyMainStateEvent){
                is NeedyMainStateEvent.GetDonors -> {
                    needyRepository.getDonors(NeedyMainStateEvent.city)
                        .onEach { dataState ->
                            _dataStateDonors.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is NeedyMainStateEvent.GetItemsByDonor -> {
                    needyRepository.getItemsByDonors(NeedyMainStateEvent.donorPhone)
                        .onEach { dataState ->
                            _dataStateItems.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is NeedyMainStateEvent.ConfirmOrder -> {
                    needyRepository.updateItems(
                        NeedyMainStateEvent.hashMap,
                        NeedyMainStateEvent.items,
                        NeedyMainStateEvent.order
                    )
                        .onEach { dataState ->
                            _dataStateOrder.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is NeedyMainStateEvent.UpdateOrder -> {
                    needyRepository.updateOrder(NeedyMainStateEvent.phoneNumber)
                        .onEach { dataState ->
                            _dataStateBoolean.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is NeedyMainStateEvent.GetOrder -> {
                    needyRepository.getOrder(NeedyMainStateEvent.id)
                        .onEach { dataState ->
                            _dataStateOrder.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
            }
        }
    }
}

sealed class NeedyMainStateEvent{
    data class GetDonors(val city: String): NeedyMainStateEvent()
    data class GetItemsByDonor(val donorPhone: String): NeedyMainStateEvent()
    data class ConfirmOrder(val hashMap: HashMap<String, Double>, val items: List<Item>,val  order: Order): NeedyMainStateEvent()
    data class UpdateOrder(val phoneNumber :String): NeedyMainStateEvent()
    data class GetOrder(val id :String): NeedyMainStateEvent()
}

