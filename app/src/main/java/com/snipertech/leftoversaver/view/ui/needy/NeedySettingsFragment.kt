package com.snipertech.leftoversaver.view.ui.needy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.databinding.NeedySettingsFragmentBinding
import com.snipertech.leftoversaver.util.Constants
import com.snipertech.leftoversaver.util.UsefulMethodsUtil

class NeedySettingsFragment : Fragment() {

    companion object {
        fun newInstance() =
            NeedySettingsFragment()
    }

    private var binding: NeedySettingsFragmentBinding? = null
    private val settingsFragmentBinding get() = binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = NeedySettingsFragmentBinding.inflate(inflater, container, false)
        return settingsFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language,
            R.layout.support_simple_spinner_dropdown_item
        )
        settingsFragmentBinding.changeLanguage.adapter = adapter

        spinnerItemClicked()
    }

    private fun spinnerItemClicked() {
        settingsFragmentBinding.changeLanguage.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p0 != null) {
                        changeLanguage(p0.getItemAtPosition(p2) as String)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

    }

    private fun changeLanguage(lang: String) {
        when (lang) {
            resources.getString(R.string.english) -> {
                setLocale(Constants.ENGLISH)
            }
            resources.getString(R.string.arabic) -> {
                setLocale(Constants.ARABIC)
            }
        }
    }


    private fun setLocale(languageToLoad: String) {
        val pref = UsefulMethodsUtil

        pref.saveStringToSharedPreferences(
            requireContext(),
            Constants.PREF_NAME,
            Constants.SELECTED_LANGUAGE,
            languageToLoad
        )
        requireActivity().finish()
        startActivity(Intent(requireContext(), NeedyMainActivity::class.java))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
