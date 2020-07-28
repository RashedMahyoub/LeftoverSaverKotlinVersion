package com.snipertech.leftoversaver.viewModel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Item
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.repository.AuthRepository
import com.snipertech.leftoversaver.repository.DonorRepository
import com.snipertech.leftoversaver.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class DonorViewModel
@ViewModelInject
constructor(
    private val donorRepository: DonorRepository,
    private val authRepository: AuthRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _dataStateDonor: MutableLiveData<DataState<Donor?>> = MutableLiveData()
    private val _dataStateBoolean: MutableLiveData<DataState<Boolean?>> = MutableLiveData()
    private val _dataStateOrders: MutableLiveData<DataState<List<Order>?>> = MutableLiveData()
    private val _dataStateItems: MutableLiveData<DataState<List<Item>?>> = MutableLiveData()
    private val _isItemUpdated = donorRepository.itemUpdatedLiveData

    val itemUpdatedState: LiveData<Boolean>
        get() = _isItemUpdated
    val dataStateDonor: LiveData<DataState<Donor?>>
        get() = _dataStateDonor
    val dataStateBoolean: LiveData<DataState<Boolean?>>
        get() = _dataStateBoolean
    val dataStateOrders: LiveData<DataState<List<Order>?>>
        get() = _dataStateOrders
    val dataStateItems: LiveData<DataState<List<Item>?>>
        get() = _dataStateItems

    fun setStateEvent(mainStateEvent: MainStateEvent){
        viewModelScope.launch(Dispatchers.IO) {
            when(mainStateEvent){
                is MainStateEvent.GetOrders -> {
                    donorRepository.getOrders(mainStateEvent.phoneNumber)
                        .onEach { dataState ->
                            _dataStateOrders.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.RegisterDonor -> {
                    authRepository.insertDonor(mainStateEvent.donor)
                        .onEach { dataState ->
                            _dataStateDonor.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.LoginDonor -> {
                    authRepository.searchDonor(mainStateEvent.phone, mainStateEvent.password)
                        .onEach { dataState ->
                            _dataStateDonor.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.AddItem -> {
                    donorRepository.addItem(mainStateEvent.item)
                        .onEach { dataState ->
                            _dataStateBoolean.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.GetItems -> {
                    donorRepository.getItems(mainStateEvent.id)
                        .onEach { dataState ->
                            _dataStateItems.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.Update -> {
                    donorRepository.update(mainStateEvent.phone, mainStateEvent.status)
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.DeleteItem -> {
                    donorRepository.deleteItem(mainStateEvent.item)
                        .onEach { dataState ->
                            _dataStateBoolean.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.DeleteOrder -> {
                    donorRepository.deleteOrder(mainStateEvent.order)
                        .onEach { dataState ->
                            _dataStateBoolean.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
                is MainStateEvent.UpdateProfilePicture -> {
                    donorRepository.changeProfilePic(mainStateEvent.phone, mainStateEvent.imageUrl,
                        mainStateEvent.password)
                        .onEach { dataState ->
                            _dataStateDonor.value = dataState
                        }
                        .launchIn(viewModelScope)
                }
            }
        }
    }
}

sealed class MainStateEvent{
    data class RegisterDonor(val donor: Donor): MainStateEvent()
    data class LoginDonor(val phone: String, val password: String): MainStateEvent()
    data class Update(val phone: String, val status: Boolean): MainStateEvent()
    data class GetItems(val id: String): MainStateEvent()
    data class DeleteItem(val item: Item): MainStateEvent()
    data class DeleteOrder(val order: Order): MainStateEvent()
    data class GetOrders(val phoneNumber: String): MainStateEvent()
    data class UpdateProfilePicture(val phone: String, val imageUrl: String, val password: String): MainStateEvent()
    data class AddItem(val item: Item): MainStateEvent()
}
