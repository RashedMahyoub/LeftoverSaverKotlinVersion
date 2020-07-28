package com.snipertech.leftoversaver.view.ui.donor

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.FragmentDonorOrdersBinding
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil.observeOnce
import com.snipertech.leftoversaver.view.adapter.VolunteerOrdersAdapter
import com.snipertech.leftoversaver.viewModel.DonorViewModel
import com.snipertech.leftoversaver.viewModel.MainStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class DonorOrdersFragment : Fragment() {

    private var binding: FragmentDonorOrdersBinding? = null
    private val donorOrdersBinding get() = binding!!
    private val viewModel: DonorViewModel by viewModels()
    private var donor: Donor? = null
    private var adapter: VolunteerOrdersAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDonorOrdersBinding.inflate(inflater, container, false)
        return donorOrdersBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        if(donor != null) viewModel.setStateEvent(MainStateEvent.GetOrders(donor!!.phoneNumber))
        //Check for new data change
        subscribeObservers()
    }

    private fun init() {
        val pref = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val obj = pref.getString(Constants.OBJECT, "")
        donor = Gson().fromJson(obj, Donor::class.java)
        initRecyclerView()
        refreshList()
    }


    private fun refreshList() {
        //Refresh to update list
        donorOrdersBinding.ordersListRefs.setOnRefreshListener {
            if(donor != null) viewModel.setStateEvent(MainStateEvent.GetOrders(donor!!.phoneNumber))
            donorOrdersBinding.ordersListRefs.isRefreshing = false
        }
    }

    private fun initRecyclerView() {
        donorOrdersBinding.ordersLists.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.stackFromEnd = true
            linearLayoutManager.reverseLayout = true
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
        }
    }

    // Search Function
    private fun searchItems() {
        donorOrdersBinding.searchOrder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                adapter?.filterList(s)
            }
        })
    }

    //Observe for data changes
    private fun subscribeObservers() {
        viewModel.dataStateOrders.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<List<Order>?> -> {
                    Log.d("WOW", "NOPE")
                    donorOrdersBinding.loadOrder.visibility = View.GONE
                    displayOrders(dataState.data)
                }
                is DataState.Error -> {
                    Log.d("WOW", "NOPE")
                    donorOrdersBinding.emptyList.visibility = View.VISIBLE
                    donorOrdersBinding.emptyText.visibility = View.VISIBLE
                    donorOrdersBinding.loadOrder.visibility = View.GONE
                    displayError()
                }
                is DataState.Loading -> {
                    Log.d("WOW", "LOADING")
                    donorOrdersBinding.loadOrder.visibility = View.VISIBLE
                }
            }
        })

        viewModel.dataStateBoolean.observeOnce(viewLifecycleOwner) { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    displaySuccess(resources.getString(R.string.order_deleted))
                    donorOrdersBinding.loadOrder.visibility = View.GONE
                }
                is DataState.Error -> {
                    donorOrdersBinding.loadOrder.visibility = View.GONE
                    displayError()
                }
                is DataState.Loading -> {
                    donorOrdersBinding.loadOrder.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun displayError() {
        Toast.makeText(
            requireContext(),
            resources.getString(R.string.error),
            Toast.LENGTH_SHORT
        ).show()
    }

    //Show success message
    private fun displaySuccess(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    //Display data
    private fun displayOrders(list: List<Order>?) {
        //If it is not null then we will display all users
        list?.let {
            //Check if list is empty
            if (it.isEmpty()) {
                donorOrdersBinding.emptyList.visibility = View.VISIBLE
                donorOrdersBinding.emptyText.visibility = View.VISIBLE
            }else {
                adapter = VolunteerOrdersAdapter(it, requireContext())
                donorOrdersBinding.ordersLists.adapter = adapter

                adapter?.setOnClickLister(object : VolunteerOrdersAdapter.OnItemClickListener {
                    override fun onItemClick(order: Order) {
                    }
                })
                searchItems()
                swipeToDelete()
            }
        }
    }

    //Swipe to delete order
    private fun swipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(adapter?.getItemAt(viewHolder.adapterPosition) != null){
                    viewModel.setStateEvent(
                        MainStateEvent.DeleteOrder(adapter?.getItemAt(viewHolder.adapterPosition)!!)
                    )
                }
            }
        }).attachToRecyclerView(donorOrdersBinding.ordersLists)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        donor = null
        adapter = null
        binding = null
    }
}