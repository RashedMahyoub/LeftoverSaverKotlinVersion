package com.snipertech.leftoversaver.view.ui.needy

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
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
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.DonorItemsFragmentBinding
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Item
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil.observeOnce
import com.snipertech.leftoversaver.view.adapter.DonorItemsRecyclerAdapter
import com.snipertech.leftoversaver.view.adapter.DonorsRecyclerAdapter
import com.snipertech.leftoversaver.viewModel.NeedyMainStateEvent
import com.snipertech.leftoversaver.viewModel.NeedyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DonorItemsFragment : Fragment() {

    companion object {
        fun newInstance() = DonorItemsFragment()
    }

    private val viewModel: NeedyViewModel by viewModels()
    private var fragmentBinding: DonorItemsFragmentBinding? = null
    private val binding get() = fragmentBinding!!
    private var itemsAdapter: DonorItemsRecyclerAdapter? = null
    private var donor: Donor? = null
    private var needyPhone: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinding = DonorItemsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        //Setup the observers
        registerObserver()
    }

    private fun init() {
        //get the donor from previous fragment(RestaurantListFragment)
        arguments.let {
            if (it != null) {
                val args = DonorItemsFragmentArgs.fromBundle(it)
                donor = args.Donor
                viewModel.setStateEvent(
                    NeedyMainStateEvent.GetItemsByDonor(args.Donor.phoneNumber)
                )
                binding.donor.text = args.Donor.name
            }
        }
        //setup the recyclerview
        binding.donorsItems.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.stackFromEnd
            linearLayoutManager.reverseLayout
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
        }
        //Get the needy phone from the shared preferences
        val pref = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        needyPhone = pref.getString(Constants.OBJECT, "")
    }


    private fun registerObserver() {
        //observe the list of items for the donor and assign it to recyclerview
        viewModel.dataStateItems.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<List<Item>?> -> {
                    binding.loadItems.visibility = View.GONE
                    displayList(dataState.data)
                }
                is DataState.Error -> {
                    binding.loadItems.visibility = View.GONE
                    displayMessage(resources.getString(R.string.error))
                }
                is DataState.Loading -> {
                    binding.loadItems.visibility = View.VISIBLE
                }
            }
        })

        //observe the list of items for the donor and assign it to recyclerview
        viewModel.dataStateBoolean.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    binding.loadItems.visibility = View.GONE
                    displayMessage(resources.getString(R.string.order_confirmed))
                }
                is DataState.Error -> {
                    binding.loadItems.visibility = View.GONE
                    displayMessage(resources.getString(R.string.error))
                }
                is DataState.Loading -> {
                    binding.loadItems.visibility = View.VISIBLE
                }
            }
        })

        //observe the list of items for the donor and assign it to recyclerview
        viewModel.dataStateOrder.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Order?> -> {
                    binding.loadItems.visibility = View.GONE
                    val intent = Intent(requireContext(), NeedyMainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is DataState.Error -> {
                    binding.loadItems.visibility = View.GONE
                    displayMessage(resources.getString(R.string.error))
                }
                is DataState.Loading -> {
                    binding.loadItems.visibility = View.VISIBLE
                }
            }
        })
    }

    //Display list
    private fun displayList(list: List<Item>?) {
        //Display empty list image and text when list is empty
        if (list != null) {
            if (list.isEmpty()) {
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
            }
            itemsAdapter = DonorItemsRecyclerAdapter(list)
            binding.donorsItems.adapter = itemsAdapter
            val hashMap = hashMapOf<String, Double>()
            val items = arrayListOf<Item>()

            //onClick add button
            if(itemsAdapter != null) {
                itemsAdapter!!.setOnClickLister(object :
                    DonorItemsRecyclerAdapter.OnItemClickListener {
                    override fun onAddItemClick(position: Int, counter: Double, isMaximum: Boolean) {
                        val item = itemsAdapter!!.getItemAt(position)

                        if (isMaximum) {
                            Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.items_limit),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        if (counter > 0.0) {
                            if (items.contains(item)) {
                                for (i in items) {
                                    if(i.name == item.name){
                                        i.amount = counter
                                    }
                                }
                            } else {
                                items.add(item)
                                hashMap[item.name] = item.amount
                            }
                        }
                    }

                    override fun onDelItemClick(position: Int, counter: Double) {
                        val item = itemsAdapter!!.getItemAt(position)
                        if (counter == 0.0) {
                            hashMap.remove(item.name)
                            items.remove(item)
                        } else {
                            for (i in items) {
                                if (i.name == item.name) {
                                    i.amount = counter
                                }
                            }
                        }
                    }
                })
            }
            //Confirm button Delivery
            binding.confirmOrderDeliver.setOnClickListener {
                confirmOrder(hashMap, items, Constants.DELIVERY)
            }

            //Confirm button come & get
            binding.confirmOrderComeTake.setOnClickListener {
                confirmOrder(hashMap, items, Constants.COMEGET)
            }
        }
    }

    //Show Error/Success message
    private fun displayMessage(message: String){
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    //Confirm order
    private fun confirmOrder(hashMap: HashMap<String, Double>, items: List<Item>, type: String) {
        if(needyPhone != null && donor != null) {
            if (items.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.pick_one_item_message),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val order = Order(
                    null,
                    binding.orderAddress.text.toString(),
                    items,
                    type,
                    needyPhone!!,
                    "Waiting",
                    null,
                    donor!!.address
                )
                viewModel.setStateEvent(NeedyMainStateEvent.ConfirmOrder(hashMap, items, order))
            }
        }
    }
}
