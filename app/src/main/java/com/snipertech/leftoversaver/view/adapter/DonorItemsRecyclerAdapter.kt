package com.snipertech.leftoversaver.view.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.snipertech.leftoversaver.R
import com.snipertech.leftoversaver.model.Item
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.food_list.view.*

class DonorItemsRecyclerAdapter(items: List<Item>): RecyclerView.Adapter<DonorItemsRecyclerAdapter.ItemViewHolder>() {

    private lateinit var listener: OnItemClickListener
    private var itemList = ArrayList<Item>()

    init {
        itemList = items as ArrayList<Item>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.food_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itemList[position], listener)
    }


    class ItemViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        private val itemName: TextView = itemView.food_name
        private val itemAmount: TextView = itemView.food_amount
        private val add: View = itemView.add
        private val minus: View = itemView.del
        private val counter: TextView = itemView.counter
        private val itemImage: CircleImageView = itemView.item_image
        private var count: Double = 0.0

        @SuppressLint("SetTextI18n")
        fun bind(item: Item, listener: OnItemClickListener){
            itemName.text = item.name
            counter.text = 0.0.toString()

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
            //Check if item is not empty
            if(item.amount <= 0){
                itemAmount.text = "None"
            }else{
                itemAmount.text = item.amount.toString()
            }
            val percentage = (item.amount * (50.0f / 100.0f)).toInt()
            var maximum = false
            add.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    if((count + 0.5) <= percentage){
                        count += 0.5
                    }else{
                        maximum = true
                    }
                    counter.text = count.toString()
                    listener.onAddItemClick(position, count, maximum)
                }
            }

            minus.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    if(count > 0){
                        count -= 0.5
                    }
                    counter.text = count.toString()
                    listener.onDelItemClick(position, count)
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
        fun onAddItemClick(position: Int, counter: Double, isMaximum:Boolean)
        fun onDelItemClick(position: Int, counter: Double)
    }

    fun setOnClickLister(listener: OnItemClickListener){
        this.listener = listener
    }
}