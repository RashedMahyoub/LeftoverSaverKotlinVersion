package com.snipertech.leftoversaver.view.ui.volunteer

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.VolunteerHomeFragmentBinding
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.Constants.LAUNCH_SECOND_ACTIVITY
import com.snipertech.leftoversaver.util.Constants.REGISTERED_LOCATION
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.util.UsefulMethodsUtil.observeOnce
import com.snipertech.leftoversaver.view.adapter.VolunteerOrdersAdapter
import com.snipertech.leftoversaver.view.ui.MapsActivity
import com.snipertech.leftoversaver.viewModel.VolunteerMainStateEvent
import com.snipertech.leftoversaver.viewModel.VolunteerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VolunteerHomeFragment : Fragment() {

    companion object {
        fun newInstance() = VolunteerHomeFragment()
    }

    @Inject
    lateinit var confirmDeliveryDialog: ConfirmDeliveryDialog
    private val viewModel: VolunteerViewModel by viewModels()
    private var fragmentBinding: VolunteerHomeFragmentBinding? = null
    private val binding get() = fragmentBinding!!
    private var orderAdapter: VolunteerOrdersAdapter? = null
    private var vol: Volunteer? = null
    private var city = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = VolunteerHomeFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
        registerObservers()
    }

    private fun init() {
        initRecyclerView()
        //Get user from shared preferences
        val pref = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val obj = pref.getString(Constants.OBJECT, "")
        vol = Gson().fromJson(obj, Volunteer::class.java)
        //Add a set on click for using map activity
        binding.orders.setOnClickListener {
            if (UsefulMethodsUtil.isServicesOK(requireActivity())) {
                val mIntent = Intent(requireContext(), MapsActivity::class.java)
                startActivityForResult(mIntent, LAUNCH_SECOND_ACTIVITY)
            }
        }
        refreshList()
        updateCity(city)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    city = data?.getStringExtra(REGISTERED_LOCATION).toString()
                    viewModel.setStateEvent(VolunteerMainStateEvent.GetOrdersByCity(city))
                    updateCity(city)
                }
                Activity.RESULT_CANCELED -> {
                    Toast.makeText(
                        requireContext(),
                        "No location was picked",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun registerObservers() {
        viewModel.dataStateOrders.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<List<Order>?> -> {
                    Log.d("WOW", "SUCCESS")
                    binding.loadOrders.visibility = View.GONE
                    displayList(dataState.data)
                }
                is DataState.Error -> {
                    Log.d("WOW", "NOPE")
                    binding.loadOrders.visibility = View.GONE
                    displayMessage(resources.getString(R.string.error))
                }
                is DataState.Loading -> {
                    Log.d("WOW", "LOADING")
                    binding.loadOrders.visibility = View.VISIBLE
                    binding.emptyList.visibility = View.GONE
                    binding.emptyText.visibility = View.GONE
                }
            }

        })

        viewModel.volunteerUpdatedLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                "Loading" -> {
                    binding.loadOrders.visibility = View.VISIBLE
                }
                "Success" -> {
                    binding.loadOrders.visibility = View.GONE
                    displayMessage(resources.getString(R.string.added_to_history))
                    viewModel.setStateEvent(VolunteerMainStateEvent.GetOrdersByCity(city))
                }
                "Error" -> {
                    binding.loadOrders.visibility = View.GONE
                    displayMessage(resources.getString(R.string.error))
                }
            }
        })
    }


    //Set up recyclerView
    private fun initRecyclerView() {
        //setup the recyclerview
        binding.ordersList.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.stackFromEnd
            linearLayoutManager.reverseLayout
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
        }
    }

    private fun refreshList() {
        //Refresh to update list
        binding.ordersListRef.setOnRefreshListener {
            viewModel.setStateEvent(VolunteerMainStateEvent.GetOrdersByCity(city))
            orderAdapter?.notifyDataSetChanged()
            binding.ordersListRef.isRefreshing = false
        }
    }

    //Update city text
    private fun updateCity(c: String) {
        //Get chosen city and color it as colorPrimary
        var place = if(c.isEmpty()){
            (resources.getString(R.string.choose_city))
        }else{
            (resources.getString(R.string.orders_in) + "  " + city)
        }
        val spannable: Spannable = SpannableString(place)
        spannable.setSpan(
            ForegroundColorSpan(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)),
            resources.getString(R.string.orders_in).length,
            place.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.orders.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    //Display the list
    private fun displayList(data: List<Order>?) {
        if (data != null) {
            if (data.isEmpty()) {
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
            }
            orderAdapter = VolunteerOrdersAdapter(data, requireContext())
            binding.ordersList.adapter = orderAdapter

            orderAdapter?.setOnClickLister(object :
                VolunteerOrdersAdapter.OnItemClickListener {
                override fun onItemClick(order: Order) {
                    if (vol != null) {
                        confirmDeliveryDialog.showDialog(
                            requireActivity(),
                            vol!!,
                            order
                        )
                    }
                }
            })
        }
    }

    //Display a message
    private fun displayMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderAdapter = null
        fragmentBinding = null
        vol = null
    }
}