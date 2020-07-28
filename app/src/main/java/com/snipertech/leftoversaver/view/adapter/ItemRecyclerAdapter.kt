package com.snipertech.leftoversaver.view.adapter

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.model.Item
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_list.view.*
import java.util.*
import kotlin.collections.ArrayList

class ItemRecyclerAdapter(private val items: List<Item>): RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder>() {

    private lateinit var listener: OnItemClickListener
    private var itemList = ArrayList<Item>()

    init {
        itemList = items as ArrayList<Item>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position], listener)
    }


    class ItemViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        private val itemName: TextView = itemView.item_names
        private val itemAmount: TextView = itemView.item_amounts
        private val itemImage: CircleImageView = itemView.imageView

        fun bind(item: Item, listener: OnItemClickListener){
            itemName.text = item.name
            itemAmount.text = item.amount.toString()

            //Change the url's $ sign to / and ~ to : to get the correct url format
            val img =
                item.imageUrl.replace(Regex("""[%]"""), "/").replace(Regex("""[~]"""), ":")

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
                    listener.onItemClick(item)
                }
            }
        }
    }

    fun getItemAt(position: Int): Item{
        return itemList[position]
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnItemClickListener {
        fun onItemClick(item: Item)
    }

    fun setOnClickLister(listener: OnItemClickListener){
        this.listener = listener
    }

    fun filterList(s: Editable){
        if (s.isEmpty()) {
            itemList = items as ArrayList<Item>
        } else {
            val resultList = MutableLiveData<List<Item>>()
            val filteredList = ArrayList<Item>()
            for (row in items) {
                if (row.name.toLowerCase(Locale.ROOT)
                        .contains(s.toString().toLowerCase(Locale.ROOT))) {
                    filteredList.add(row)
                }
            }
            resultList.value = filteredList
            itemList = resultList.value as ArrayList<Item>
        }
        notifyDataSetChanged()
    }
}