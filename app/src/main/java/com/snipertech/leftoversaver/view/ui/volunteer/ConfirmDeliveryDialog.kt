package com.snipertech.leftoversaver.view.ui.volunteer

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.model.Volunteer
import com.snipertech.leftoversaver.repository.VolunteerRepository
import kotlinx.android.synthetic.main.dialog_confirm_delivery.*
import kotlinx.coroutines.*
import javax.inject.Inject

class ConfirmDeliveryDialog @Inject constructor(private val volunteerRepository: VolunteerRepository) {
    fun showDialog(activity: Activity, vol: Volunteer, order: Order) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_confirm_delivery)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val confirm = dialog.btn_confirm

        confirm.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                volunteerRepository.updateVol(order, vol)
                cancel()
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}