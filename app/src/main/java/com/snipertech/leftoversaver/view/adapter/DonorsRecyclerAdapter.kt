package com.snipertech.leftoversaver.view.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.model.Donor
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.donor_list.view.*
import java.util.*
import kotlin.collections.ArrayList

class DonorsRecyclerAdapter(private val donors: List<Donor>): RecyclerView.Adapter<DonorsRecyclerAdapter.ItemViewHolder>() {

    private lateinit var listener: OnItemClickListener
    private var donorList: List<Donor>

    init {
        donorList = donors
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.donor_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(donorList[position], listener)
    }


    class ItemViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        private val donorName: TextView = itemView.donor_names
        private val itemImage: CircleImageView = itemView.donorImage
        private val outOfStock: TextView = itemView.out_of_stock

        fun bind(donor: Donor, listener: OnItemClickListener){
            donorName.text = donor.name

            if(donor.empty){
                outOfStock.text = outOfStock.context.resources.getString(R.string.out_of_stock)
            }
            //Change the url's $ sign to / and ~ to : to get the correct url format
            val img =
                donor.imageUrl.replace(Regex("""[%]"""), "/").replace(Regex("""[~]"""), ":")

            Glide.with(itemImage.context)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round)
                )
                .load(img)
                .centerCrop()
                .into(itemImage)

            itemView.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    listener.onItemClick(donor)
                }
            }
        }
    }

    fun getItemAt(position: Int): Donor{
        return donorList[position]
    }

    override fun getItemCount(): Int {
        return donorList.size
    }

    interface OnItemClickListener {
        fun onItemClick(donor: Donor)
    }

    fun setOnClickLister(listener: OnItemClickListener){
        this.listener = listener
    }

    fun filterList(s: Editable){
        if (s.isEmpty()) {
            donorList = donors as ArrayList<Donor>
        } else {
            val resultList = MutableLiveData<List<Donor>>()
            val filteredList = ArrayList<Donor>()
            for (row in donors) {
                if (row.name.toLowerCase(Locale.ROOT)
                        .contains(s.toString().toLowerCase(Locale.ROOT))) {
                    filteredList.add(row)
                }
            }
            resultList.value = filteredList
            donorList = resultList.value as ArrayList<Donor>
        }
        notifyDataSetChanged()
    }
}