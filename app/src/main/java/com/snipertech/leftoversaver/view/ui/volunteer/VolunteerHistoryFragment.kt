package com.snipertech.leftoversaver.view.ui.volunteer

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.VolunteerHistoryFragmentBinding
import com.snipertech.leftoversaver.databinding.VolunteerHomeFragmentBinding
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.view.adapter.VolunteerOrdersAdapter
import com.snipertech.leftoversaver.viewModel.VolunteerMainStateEvent
import com.snipertech.leftoversaver.viewModel.VolunteerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class VolunteerHistoryFragment : Fragment() {

    companion object {
        fun newInstance() =
            VolunteerHistoryFragment()
    }

    private val viewModel: VolunteerViewModel by viewModels()
    private var fragmentBinding: VolunteerHistoryFragmentBinding? = null
    private val binding get() = fragmentBinding!!
    private var orderAdapter: VolunteerOrdersAdapter? = null
    private var vol: Volunteer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = VolunteerHistoryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onResume() {
        super.onResume()
        registerObservers()
    }


    private fun init() {
        initRecyclerView()

        val pref =
            requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val obj = pref.getString(Constants.OBJECT, "")
        vol = Gson().fromJson(obj, Volunteer::class.java)

        //search for the volunteer to update the list
        if (vol != null) viewModel.setStateEvent(VolunteerMainStateEvent.SearchVolunteer(vol!!.phoneNumber))

        refreshList()
    }

    //Set up recyclerView
    private fun initRecyclerView() {
        //setup the recyclerview
        binding.ordersHistory.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.stackFromEnd
            linearLayoutManager.reverseLayout
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
        }
    }

    private fun registerObservers() {
        viewModel.dataStateVolunteer.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Volunteer?> -> {
                    Log.d("WOW", "SUCCESS")
                    binding.loadHistory.visibility = View.GONE
                    displayList(dataState.data)
                }
                is DataState.Error -> {
                    Log.d("WOW", "NOPE")
                    binding.loadHistory.visibility = View.GONE
                    binding.emptyList.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.VISIBLE
                    displayError()
                }
                is DataState.Loading -> {
                    Log.d("WOW", "LOADING")
                    binding.loadHistory.visibility = View.VISIBLE

                }
            }
        })
    }

    //Display an error
    private fun displayError() {
        Toast.makeText(
            requireContext(),
            resources.getString(R.string.error),
            Toast.LENGTH_SHORT
        ).show()
    }

    //Display the list
    private fun displayList(vol: Volunteer?) {
        if (vol != null) {
            if (vol.history?.list!!.isEmpty()) {
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
            }
            orderAdapter = VolunteerOrdersAdapter(vol.history?.list!!, requireContext())
            binding.ordersHistory.adapter = orderAdapter

            orderAdapter?.setOnClickLister(object : VolunteerOrdersAdapter.OnItemClickListener {
                override fun onItemClick(order: Order) {

                }
            })
        }
    }

    private fun refreshList() {
        //Refresh to update list
        binding.ordersRefresh.setOnRefreshListener {
            if (vol != null) viewModel.setStateEvent(
                VolunteerMainStateEvent.SearchVolunteer(vol!!.phoneNumber)
            )
            binding.emptyList.visibility = View.GONE
            binding.emptyText.visibility = View.GONE
            binding.loadHistory.visibility = View.VISIBLE
            orderAdapter?.notifyDataSetChanged()
            binding.ordersRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentBinding = null
        vol = null
        orderAdapter = null
    }
}
