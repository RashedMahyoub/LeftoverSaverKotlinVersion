package com.snipertech.leftoversaver.view.ui.donor

import android.Manifest.permission
import android.app.Activity.MODE_PRIVATE
import android.app.Activity.RESULT_OK
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
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.DonorProfileFragmentBinding
import com.snipertech.leftoversaver.model.Donor
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.Constants.IMAGE_PICK_CODE
import com.snipertech.leftoversaver.util.Constants.PERMISSION_CODE
import com.snipertech.leftoversaver.util.Constants.bucketName
import com.snipertech.leftoversaver.util.DataState
import com.snipertech.leftoversaver.util.ImageManager.Companion.getPath
import com.snipertech.leftoversaver.util.UploadImageAsync
import com.snipertech.leftoversaver.util.UsefulMethodsUtil
import com.snipertech.leftoversaver.viewModel.DonorViewModel
import com.snipertech.leftoversaver.viewModel.MainStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class DonorProfileFragment : Fragment() {

    companion object {
        fun newInstance() = DonorProfileFragment()
    }

    private var binding: DonorProfileFragmentBinding? = null
    private val donorProfileFragmentBinding get() = binding!!
    private val viewModel: DonorViewModel by viewModels()
    private var navController: NavController? = null
    private var donor: Donor? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DonorProfileFragmentBinding.inflate(
            inflater,
            container,
            false
        )
        return donorProfileFragmentBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        init()
    }


    private fun init() {
        //get user from shared preferences
        val pref = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val obj = pref.getString(Constants.OBJECT, "")
        donor = Gson().fromJson(obj, Donor::class.java)

        displayUser()
        changeProfilePicturePermission()
        subscribeObservers()
    }

    //Display user's information
    private fun displayUser() {
        donorProfileFragmentBinding.apply {
            donorName.text = donor?.name
            donorAddress.text = donor?.address
            donorPhone.text = donor?.phoneNumber

            //Change the url's $ sign to / and ~ to : to get the correct url format
            val img =
                donor?.imageUrl?.replace(Regex("""[%]"""), "/")?.replace(Regex("""[~]"""), ":")

            Glide.with(requireContext())
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                )
                .load(img)
                .centerCrop()
                .into(donorProfileFragmentBinding.profileImageDonor)
        }
    }

    //Check permission for profile image
    private fun changeProfilePicturePermission() {
        donorProfileFragmentBinding.profileImageDonor.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(requireContext(), permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(
                        permissions,
                        PERMISSION_CODE
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
    }

    //Choose image from gallery
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            IMAGE_PICK_CODE
        )
    }


    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
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

    //handle result of picked image
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //Get Image Uri
            val imageUri = data?.data

            //Get the file of the image
            val f = File(imageUri?.let { getPath(requireContext(), it) }!!)

            //Get the file name
            val imageName: String = f.name

            //Upload the file into Amazon S3 bucket
            UploadImageAsync(requireActivity()).execute(
                imageName,
                getPath(requireContext(), imageUri)
            )

            Log.d("RETROFIT", imageName)

            //get the url for that file to access it later
            val imageUrl = "https~%%$bucketName.s3-us-west-1.amazonaws.com%$imageName"

            //change the profile picture
            if(donor != null){
                viewModel.setStateEvent(
                    MainStateEvent.UpdateProfilePicture(donor!!.phoneNumber, imageUrl, donor!!.password)
                )
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

    //Check if image url has been saved in DB or not
    private fun subscribeObservers() {
        viewModel.dataStateDonor.observe(viewLifecycleOwner, Observer { dataState ->
            when (dataState) {
                is DataState.Success<Donor?> -> {
                    val pref = UsefulMethodsUtil
                    pref.apply {
                        dataState.data?.let {
                            saveObjectToSharedPreference(
                                requireContext(),
                                Constants.PREF_NAME,
                                Constants.OBJECT,
                                it
                            )
                        }
                    }
                    displaySuccess(resources.getString(R.string.upload_sccess))

                    val img =
                        dataState.data?.imageUrl?.replace(
                            Regex("""[%]"""), "/")?.
                            replace(Regex("""[~]"""), ":")

                    Log.d("WOW", "$img")
                    Glide.with(this)
                        .applyDefaultRequestOptions(
                            RequestOptions()
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                        )
                        .load(img)
                        .centerCrop()
                        .into(donorProfileFragmentBinding.profileImageDonor)
                }
                is DataState.Error -> {
                    displayError()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        navController = null
        binding = null
        donor = null
    }
}
