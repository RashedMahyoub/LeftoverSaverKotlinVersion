package com.snipertech.leftoversaver.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.model.Order
import com.snipertech.leftoversaver.util.Constants
import kotlinx.android.synthetic.main.order_list.view.address_info
import kotlinx.android.synthetic.main.order_list.view.city_name
import kotlinx.android.synthetic.main.order_list.view.donor_info
import kotlinx.android.synthetic.main.order_list.view.items_info
import kotlinx.android.synthetic.main.order_list.view.needy_info
import kotlinx.android.synthetic.main.order_list_donor.view.*
import java.util.*
import kotlin.collections.ArrayList


class VolunteerOrdersAdapter(private val items: List<Order>, ctx: Context): RecyclerView.Adapter<VolunteerOrdersAdapter.OrderViewHolder>() {

    companion object{
        private const val USER_TYPE_DONOR = 0
        private const val USER_TYPE_VOL = 1
    }
    private lateinit var listener: OnItemClickListener
    private var orderList = ArrayList<Order>()
    private var context: Context

    init {
        orderList = items as ArrayList<Order>
        context = ctx
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        return if (viewType == USER_TYPE_DONOR) {
            OrderViewHolder(
                LayoutInflater.from(context).inflate(R.layout.order_list_donor, parent, false)
            )
        } else {
            OrderViewHolder(
                LayoutInflater.from(context).inflate(R.layout.order_list, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orderList[position], listener)
    }


    class OrderViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        private val needyPhone: TextView = itemView.needy_info
        private val city: TextView = itemView.city_name
        private val address: TextView = itemView.address_info
        private val donorName: TextView = itemView.donor_info
        private val itemsList: TextView = itemView.items_info
        private val status: TextView = itemView.order_status
        private val volunteer: TextView = itemView.volunteer_info
        private val orderType: TextView = itemView.order_type_info
        val list: StringBuilder = StringBuilder("")

        fun bind(order: Order, listener: OnItemClickListener){
            list.clear()
            order.itemList.forEach{list.append("[${it.name}: ${it.amount}]  ")}

            city.text = order.city
            needyPhone.text = order.needy
            address.text = order.address
            donorName.text = order.itemList[0].donor.name
            itemsList.text = list
            orderType.text = order.type
            status.text = order.stage
            volunteer.text = order.volunteer

            when(order.stage){
                "Waiting" -> {
                    status.setTextColor(
                        ResourcesCompat.getColor(status.resources, R.color.yellow, null)
                    )
                }
                "Reserved" -> {
                    status.setTextColor(Color.GREEN)
                }
            }

            itemView.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    listener.onItemClick(order)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val prefs: SharedPreferences =
            context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        return when (prefs.getString(Constants.TYPE, "")) {
            Constants.DONOR ->{
                USER_TYPE_DONOR
            }else -> {
                USER_TYPE_VOL
            }
        }
    }

    fun getItemAt(position: Int): Order {
        return orderList[position]
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    interface OnItemClickListener {
        fun onItemClick(order: Order)
    }

    fun setOnClickLister(listener: OnItemClickListener){
        this.listener = listener
    }

    fun filterList(s: Editable){
        if (s.isEmpty()) {
            orderList = items as ArrayList<Order>
        } else {
            val resultList = MutableLiveData<List<Order>>()
            val filteredList = ArrayList<Order>()
            for (row in items) {
                if (row.needy.toLowerCase(Locale.ROOT)
                        .contains(s.toString().toLowerCase(Locale.ROOT))) {
                    filteredList.add(row)
                }
            }
            resultList.value = filteredList
            orderList = resultList.value as ArrayList<Order>
        }
        notifyDataSetChanged()
    }
}