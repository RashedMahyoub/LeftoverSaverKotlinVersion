package com.snipertech.leftoversaver.view.ui.donor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.DonorAddItemFragmentBinding
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.model.Item
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.ImageManager
import com.snipertech.leftoversaver.util.UploadImageAsync
import com.snipertech.leftoversaver.viewModel.DonorViewModel
import com.snipertech.leftoversaver.viewModel.MainStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.util.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DonorAddItemFragment : Fragment(), View.OnClickListener {

    companion object {
        fun newInstance() = DonorAddItemFragment()
    }

    private val viewModel: DonorViewModel by viewModels()
    private var binding: DonorAddItemFragmentBinding? = null
    private val donorAddItemFragmentBinding get() = binding!!
    private var navController: NavController? = null
    private var donor: Donor? = null
    private var image = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DonorAddItemFragmentBinding.inflate(inflater, container, false)
        return donorAddItemFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        donorAddItemFragmentBinding.itemImage.setOnClickListener(this)
        donorAddItemFragmentBinding.addItems.setOnClickListener(this)
        registerObservers()
    }


    //create an Item
    private fun addItem() {
        when {
            donorAddItemFragmentBinding.itemName.text.toString() == "" -> {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.enter_item_name),
                    Toast.LENGTH_LONG
                ).show()
            }
            donorAddItemFragmentBinding.itemAmount.toString() == "" -> {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.enter_item_amount),
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                donorAddItemFragmentBinding.addItems.visibility = View.GONE
                donorAddItemFragmentBinding.addItemLoading.visibility = View.VISIBLE
                val name = donorAddItemFragmentBinding.itemName.text.toString()
                    .toUpperCase(Locale.ROOT)
                val amount = donorAddItemFragmentBinding.itemAmount.text
                    .toString()
                    .toInt()
                    .toDouble()
                arguments?.let {
                    val safeArgs = DonorAddItemFragmentArgs.fromBundle(it)
                    donor = safeArgs.donor!!
                }
                if(donor != null){
                    val item = Item(null, name, amount, donor!!, image)
                    viewModel.setStateEvent(MainStateEvent.AddItem(item))
                }
            }
        }
    }

    private fun displayError(message: String?) {
        if (message != null) {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    //Show success message
    private fun displaySuccess(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    //Show progress bar
    private fun displayProgressBar(isDisplayed: Boolean) {
        if (isDisplayed) donorAddItemFragmentBinding.addItemLoading.visibility = View.VISIBLE else View.GONE
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.item_image -> {
                changeProfilePicturePermission()
            }
            R.id.addItems -> {
                addItem()
            }
        }
    }

    private fun registerObservers() {
        viewModel.dataStateBoolean.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Boolean?> -> {
                    displayProgressBar(false)
                    displaySuccess(resources.getString(R.string.item_added))
                    navController?.navigate(R.id.action_donorAddItemFragment_to_donorHomeFragment)
                }
                is DataState.Error -> {
                    displayProgressBar(false)
                    displayError(dataState.exception.message)
                }
                is DataState.Loading -> {
                    displayProgressBar(true)
                }
            }
        })
    }

    //handle result of picked image
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.IMAGE_PICK_CODE) {
            //Get Image Uri
            val imageUri = data?.data

            //Display image
            donorAddItemFragmentBinding.itemImage.setImageURI(imageUri)

            //Get the file of the image
            val f = File(imageUri?.let { ImageManager.getPath(requireContext(), it) }!!)

            //Get the file name
            val imageName: String = f.name

            //Upload the file into Amazon S3 bucket
            UploadImageAsync(requireActivity()).execute(
                imageName,
                ImageManager.getPath(requireContext(), imageUri)
            )

            Log.d("RETROFIT", imageName)

            //get the url for that file to access it later
            image = "https~%%${Constants.bucketName}.s3-us-west-1.amazonaws.com%$imageName"
        }
    }


    //Check permission for profile image
    private fun changeProfilePicturePermission() {
        //check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) ==
                PackageManager.PERMISSION_DENIED
            ) {
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(
                    permissions,
                    Constants.PERMISSION_CODE
                )
            } else {
                //permission already granted
                pickImageFromGallery()
            }
        } else {
            //system OS is < Marshmallow
            pickImageFromGallery()
        }
    }

    //Choose image from gallery
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            Constants.IMAGE_PICK_CODE
        )
    }


    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(
                        requireContext(),
                        "Permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //save the data to restore it when someone rotate the screen
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("NAME", donorAddItemFragmentBinding.itemName.text.toString())
        outState.putString("AMOUNT", donorAddItemFragmentBinding.itemAmount.text.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            donorAddItemFragmentBinding.itemName.setText(savedInstanceState.getString("NAME"))
            donorAddItemFragmentBinding.itemAmount.setText(savedInstanceState.getString("AMOUNT"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navController = null
        binding = null
        donor = null
    }
}
