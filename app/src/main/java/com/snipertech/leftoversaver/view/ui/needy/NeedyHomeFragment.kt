package com.snipertech.leftoversaver.view.ui.needy

import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.lifecycle.Observer
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.FragmentNeedyHomeBinding
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil.observeOnce
import com.snipertech.leftoversaver.viewModel.NeedyMainStateEvent
import com.snipertech.leftoversaver.viewModel.NeedyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NeedyHomeFragment : Fragment() {
    private var fragmentBinding: FragmentNeedyHomeBinding? = null
    private val binding get() = fragmentBinding!!
    private val viewModel: NeedyViewModel by viewModels()
    private var navController: NavController? = null
    private var order: Order? = null

    companion object {
        fun newInstance() = NeedyHomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentBinding = FragmentNeedyHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    override fun onStart() {
        super.onStart()
        //Get the needy phone from the shared preferences
        val pref = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val id = pref.getString(Constants.OBJECT, "")
        id?.let { NeedyMainStateEvent.GetOrder(it) }?.let { viewModel.setStateEvent(it) }

        //button function
        binding.makeOrder.setOnClickListener {
            //if timer is on
            if (binding.makeOrder.text == resources.getString(R.string.confirm_delivery)) {
                if (order != null) {
                    viewModel.setStateEvent(
                        NeedyMainStateEvent.UpdateOrder(
                            order!!.needy
                        )
                    )
                }
            } else { //if timer is off
                navController?.navigate(R.id.action_needyHomeFragment_to_restaurantListFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerObservers()
    }

    //Observing for data changes
    private fun registerObservers() {
        viewModel.dataStateOrder.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Order?> -> {
                    Log.d("WOW", "SUCCESS")
                    binding.makeOrder.visibility = View.VISIBLE
                    binding.confirmDeliverLoading.visibility = View.GONE
                    if (dataState.data != null) {
                        order = dataState.data
                        displayOrder(dataState.data)
                        //Change color of the button to Green
                        binding.makeOrder.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.green)
                        binding.makeOrder.text = resources.getString(R.string.confirm_delivery)
                        binding.orderDetails.visibility = View.VISIBLE
                        binding.text.visibility = View.GONE
                    }
                }
                is DataState.Error -> {
                    Log.d("WOW", "NOPE")
                    binding.makeOrder.visibility = View.VISIBLE
                    binding.confirmDeliverLoading.visibility = View.GONE
                    displayMessage(resources.getString(R.string.error))
                }
                is DataState.Loading -> {
                    Log.d("WOW", "LOADING")
                    binding.makeOrder.visibility = View.INVISIBLE
                    binding.confirmDeliverLoading.visibility = View.VISIBLE
                }
            }
        })

        viewModel.dataStateBoolean.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    Log.d("WOW", "SUCCESS")
                    binding.makeOrder.visibility = View.VISIBLE
                    binding.confirmDeliverLoading.visibility = View.GONE
                    displayMessage(resources.getString(R.string.order_updated))
                    binding.makeOrder.text = resources.getString(R.string.order)
                    binding.makeOrder.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.appBar)
                    binding.orderDetails.visibility = View.GONE
                    binding.text.visibility = View.VISIBLE
                }
                is DataState.Error -> {
                    Log.d("WOW", "NOPE")
                    binding.makeOrder.visibility = View.VISIBLE
                    binding.confirmDeliverLoading.visibility = View.GONE
                    displayMessage(resources.getString(R.string.error))
                }
                is DataState.Loading -> {
                    Log.d("WOW", "LOADING")
                    binding.makeOrder.visibility = View.INVISIBLE
                    binding.confirmDeliverLoading.visibility = View.VISIBLE
                }
            }
        })
    }

    //Display Order information to screen
    private fun displayOrder(order: Order) {
        val list: StringBuilder = StringBuilder("")
        list.clear()
        order.itemList.forEach { list.append("[${it.name}: ${it.amount}]") }

        binding.needyInfo.text = order.needy
        binding.cityName.text = order.city
        binding.volName.text = order.volunteer
        binding.donorInfo.text = order.itemList[0].donor.name
        binding.orderStatus.text = order.stage
        binding.itemsInfo.text = list

        when(order.stage){
            "Waiting" -> {
                binding.orderStatus.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.yellow, null)
                )
            }
            "Reserved" -> {
                binding.orderStatus.setTextColor(Color.GREEN)
            }
        }
    }

    //Show Error/Success message
    private fun displayMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navController = null
        fragmentBinding = null
        order = null
    }
}