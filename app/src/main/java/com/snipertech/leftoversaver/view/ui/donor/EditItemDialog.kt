package com.snipertech.leftoversaver.view.ui.donor

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.repository.DonorRepository
import kotlinx.android.synthetic.main.dialog_edit_item.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class DialogEditItem @Inject constructor(private val donorRepository: DonorRepository){
    private var counter = 0.0
    fun showDialog(activity: Activity, donorPhone: String, Name: String, Amount: Double) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_edit_item)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val itemName = dialog.text_name
        val itemAmount = dialog.text_amount
        val add = dialog.add
        val remove = dialog.del
        val dialogButton = dialog.btn_dialog

        counter = 0.0
        counter += Amount
        itemName.text = Name
        itemAmount.text = counter.toString()
        add.setOnClickListener {
            counter += 0.5
            itemAmount.text = counter.toString()
        }
        remove.setOnClickListener {
            counter -= 0.5
            itemAmount.text = counter.toString()
        }

        dialogButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                donorRepository.updateItem(donorPhone, Name, counter)
                cancel()
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}
