package com.example.happyplaces.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.Activity.AddHappyPlace
import com.example.happyplaces.Activity.MainActivity
import com.example.happyplaces.Database.HappyPlaceEntity
import com.example.happyplaces.Database.HappyPlacesDao
import com.example.happyplaces.databinding.ItemHappyPlaceBinding


class happyPlacesAdapter(
    private val context: Context,
    val entity:ArrayList<HappyPlaceEntity>)
    : RecyclerView.Adapter<happyPlacesAdapter.viewHolder>() {

     class viewHolder(binding: ItemHappyPlaceBinding): RecyclerView.ViewHolder(binding.root) {
        val constraint=binding.constraint
        val cardView=binding.cardView
        val image=binding.itemImage
        val title=binding.itemTitle
        val description=binding.itemDescription
     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        return viewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val entities=entity[position]
        holder.image.setImageURI(Uri.parse(entities.image))
        holder.title.text=entities.title
        holder.description.text=entities.description

        if (position % 2==0){
            holder.cardView.setBackgroundColor(Color.parseColor("#EBEBEB"))
        }else{
            holder.cardView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }

        holder.constraint.setOnClickListener {
            if(onClickListener != null){
                onClickListener!!.onClick(position, entities)
            }
        }


    }

    override fun getItemCount(): Int {
        return entity.size
    }

    //onClick
    private var onClickListener:OnClickListener?=null

    interface OnClickListener{
        fun onClick(position: Int,entity: HappyPlaceEntity)
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onClickListener=onClickListener
    }



    //on swipe edit
    fun notifyEditItem(activity:Activity,position: Int){
        val intent=Intent(context,AddHappyPlace::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,entity[position])
        activity.startActivity(intent)
        notifyItemChanged(position)
    }



    fun notifyDeleteItem(position: Int):HappyPlaceEntity{
        return entity[position]
    }


}