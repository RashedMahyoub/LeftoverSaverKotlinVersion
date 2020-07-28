package com.snipertech.leftoversaver.view.ui.needy

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.DonorListFragmentBinding
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.view.adapter.DonorsRecyclerAdapter
import com.snipertech.leftoversaver.viewModel.NeedyMainStateEvent
import com.snipertech.leftoversaver.viewModel.NeedyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DonorListFragment : Fragment() {

    companion object {
        fun newInstance() = DonorListFragment()
    }

    private val viewModel: NeedyViewModel by viewModels()
    private var fragmentBinging: DonorListFragmentBinding? = null
    private val binding get() = fragmentBinging!!
    private var navController: NavController? = null
    private var donorAdapter: DonorsRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentBinging = DonorListFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        init()
        registerObservers()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.donorsList.apply {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
        }

        val pref = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val city = pref.getString(Constants.TYPE, "")
        if (city != null) {
            viewModel.setStateEvent(NeedyMainStateEvent.GetDonors(city))
        }
        binding.city.text = "${resources.getString(R.string.restaurants)} $city"
    }

    //Observe for data changes
    private fun registerObservers() {
        viewModel.dataStateDonors.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<List<Donor>?> -> {
                    binding.loadDonors.visibility = View.GONE
                    displayList(dataState.data)
                }
                is DataState.Error -> {
                    binding.loadDonors.visibility = View.GONE
                    displayError()
                }
                is DataState.Loading -> {
                    binding.loadDonors.visibility = View.VISIBLE
                }
            }
        })
    }

    //Display list
    private fun displayList(list: List<Donor>?) {
        //Display empty list image and text when list is empty
        if (list != null) {
            if (list.isEmpty()) {
                binding.emptyList.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
            }
            donorAdapter = DonorsRecyclerAdapter(list)
            binding.donorsList.adapter = donorAdapter

            //OnClick Function
            donorAdapter?.setOnClickLister(object : DonorsRecyclerAdapter.OnItemClickListener {
                override fun onItemClick(donor: Donor) {
                    navController?.navigate(
                        DonorListFragmentDirections.actionRestaurantListFragmentToRestauantItemsFragment(
                            donor
                        )
                    )
                }
            })
            //search function
            searchDonor()
        }
    }

    //Display Error
    private fun displayError() {
        Toast.makeText(
            requireContext(),
            resources.getString(R.string.error),
            Toast.LENGTH_SHORT
        ).show()
    }



    //Search donors
    private fun searchDonor() {
        binding.searchDonor.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    donorAdapter?.filterList(s)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navController = null
        donorAdapter = null
        fragmentBinging = null
    }
}
