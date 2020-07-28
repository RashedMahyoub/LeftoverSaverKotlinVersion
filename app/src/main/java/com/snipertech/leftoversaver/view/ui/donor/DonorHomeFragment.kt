package com.snipertech.leftoversaver.view.ui.donor

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.view.adapter.ItemRecyclerAdapter
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Item
import com.snipertech.leftoversaver.databinding.DonorHomeFragmentBinding
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.UsefulMethodsUtil.observeOnce
import com.snipertech.leftoversaver.viewModel.DonorViewModel
import com.snipertech.leftoversaver.viewModel.MainStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class DonorHomeFragment : Fragment() {

    @Inject
    lateinit var editItemDialog: DialogEditItem
    private val viewModel: DonorViewModel by viewModels()
    private var itemAdapter: ItemRecyclerAdapter? = null
    private var navController: NavController? = null
    private var donor: Donor? = null
    private var _binding: DonorHomeFragmentBinding? = null
    private val donorHomeViewBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DonorHomeFragmentBinding.inflate(inflater, container, false)
        return donorHomeViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        init()
        donor?.phoneNumber?.let { MainStateEvent.GetItems(it) }?.let { viewModel.setStateEvent(it) }
    }


    //initialize everything
    private fun init() {
        val pref = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val obj = pref.getString(Constants.OBJECT, "")
        donor = Gson().fromJson(obj, Donor::class.java)

        //Send donor object to add item fragment using safe args
        donorHomeViewBinding.donorAdd.setOnClickListener {
            navController?.navigate(
                DonorHomeFragmentDirections.actionDonorHomeFragmentToDonorAddItemFragment(donor)
            )
        }
        initRecyclerView()
        refreshList()
    }

    override fun onResume() {
        //Setup the observers
        subscribeObservers()
        super.onResume()
    }

    //Initialize and subscribe observers for data changes
    private fun subscribeObservers() {
        viewModel.dataStateItems.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<List<Item>?> -> {
                    Log.d("WOW", "SUCCESS")
                    donorHomeViewBinding.loadItems.visibility = View.GONE
                    displayItems(dataState.data)
                }
                is DataState.Error -> {
                    Log.d("WOW", "NOPE")
                    donorHomeViewBinding.loadItems.visibility = View.GONE
                    donorHomeViewBinding.emptyList.visibility = View.VISIBLE
                    donorHomeViewBinding.emptyText.visibility = View.VISIBLE
                    displayError()
                }
                is DataState.Loading -> {
                    Log.d("WOW", "LOADING")
                    donorHomeViewBinding.loadItems.visibility = View.VISIBLE
                }
            }
        })

        viewModel.dataStateBoolean.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    donorHomeViewBinding.loadItems.visibility = View.GONE
                    viewModel.setStateEvent(MainStateEvent.GetItems(donor!!.phoneNumber))
                    displaySuccess(resources.getString(R.string.item_deleted))
                }
                is DataState.Error -> {
                    donorHomeViewBinding.loadItems.visibility = View.GONE
                    displayError()
                }
                is DataState.Loading -> {
                    donorHomeViewBinding.loadItems.visibility = View.VISIBLE
                }
            }
        })

        viewModel.itemUpdatedState.observeOnce(viewLifecycleOwner) {
            donorHomeViewBinding.loadItems.visibility = View.VISIBLE
            if (it) {
                donorHomeViewBinding.loadItems.visibility = View.GONE
                displaySuccess(resources.getString(R.string.item_updated))
                donor?.phoneNumber?.let { MainStateEvent.GetItems(it) }?.let { viewModel.setStateEvent(it) }

            } else {
                donorHomeViewBinding.loadItems.visibility = View.GONE
                displayError()
            }
        }
    }

    //Display items to the screen
    private fun displayItems(data: List<Item>?) {
        //If it is not null then we will display all users
        data?.let {
            //Check if list is empty
            if (it.isEmpty()) {
                donorHomeViewBinding.emptyList.visibility = View.VISIBLE
                donorHomeViewBinding.emptyText.visibility = View.VISIBLE
                if (donor != null && donor?.empty == false) {
                    viewModel.setStateEvent(
                        MainStateEvent.Update(
                            donor!!.phoneNumber, true
                        )
                    )
                }
            } else {
                if (donor != null && donor?.empty == true) {
                    viewModel.setStateEvent(
                        MainStateEvent.Update(
                            donor!!.phoneNumber, false
                        )
                    )
                }

                itemAdapter = ItemRecyclerAdapter(it)
                donorHomeViewBinding.recyclerview.adapter = itemAdapter
                searchItems()
                swipeToDelete()
                onItemClick()
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

    //onClick function
    private fun onItemClick() {
        //OnClick Function
        itemAdapter?.setOnClickLister(object : ItemRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(item: Item) {
                if (donor != null) {
                    editItemDialog.showDialog(
                        requireActivity(),
                        donor!!.phoneNumber,
                        item.name,
                        item.amount
                    )
                }
            }
        })
    }

    //Swipe to delete function
    private fun swipeToDelete() {
        //swipe to delete functionality
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
                if (itemAdapter != null) {
                    viewModel.setStateEvent(
                        MainStateEvent.DeleteItem(
                            itemAdapter!!.getItemAt(viewHolder.adapterPosition)
                        )
                    )
                }
            }
        }).attachToRecyclerView(donorHomeViewBinding.recyclerview)
    }

    // Search Function
    private fun searchItems() {
        donorHomeViewBinding.searchItem.addTextChangedListener(object : TextWatcher {
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
                itemAdapter?.filterList(s)
            }
        })
    }

    private fun refreshList() {
        //Refresh to update list
        donorHomeViewBinding.refreshItems.setOnRefreshListener {
            if (donor != null) {
                viewModel.setStateEvent(MainStateEvent.GetItems(donor!!.phoneNumber))
            }
            donorHomeViewBinding.refreshItems.isRefreshing = false
        }
    }

    private fun initRecyclerView() {
        donorHomeViewBinding.recyclerview.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            linearLayoutManager.stackFromEnd = true
            linearLayoutManager.reverseLayout = true
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        donor = null
        itemAdapter = null
        navController = null
    }
}
