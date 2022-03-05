package com.example.happyplaces.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.Database.HappyPlaceEntity
import com.example.happyplaces.Database.HappyPlacesDao
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.Adapter.happyPlacesAdapter
import com.example.happyplaces.Database.happyPlacesApp
import com.example.happyplaces.Utils.SwipeToDeleteCallback
import com.example.happyplaces.Utils.SwipeToEditCallback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(){
    private var binding: ActivityMainBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent=Intent(this, AddHappyPlace::class.java)
            startActivity(intent)
        }

        handleRecyclerView()











    } //FUN
    fun handleRecyclerView(){
        val happyPlaceDao=(application as happyPlacesApp).db.happyPlacesDao()
        lifecycleScope.launch {
            happyPlaceDao.fetchAllHappyPlaces().collect {
                val list=ArrayList(it)
                setUpDataToRecyclerView(list,happyPlaceDao)
            }
        }
    }



   fun setUpDataToRecyclerView(happyPlaceList:ArrayList<HappyPlaceEntity>, happyPlaceDao: HappyPlacesDao){
        if(happyPlaceList.isNotEmpty()){
            val itemAdapter = happyPlacesAdapter(this, happyPlaceList)
            binding?.recycleView?.layoutManager= LinearLayoutManager(this)
            binding?.recycleView?.setHasFixedSize(true)
            binding?.recycleView?.adapter=itemAdapter
            binding?.recycleView?.visibility= View.VISIBLE
            binding?.noRecordFound?.visibility= View.GONE

            itemAdapter.setOnClickListener(object:happyPlacesAdapter.OnClickListener{
                override fun onClick(position: Int, entity: HappyPlaceEntity) {
                    val intent=Intent(this@MainActivity,HappyPlaceDetailActivity::class.java)
                    intent.putExtra(EXTRA_PLACE_DETAILS,entity)
                    startActivity(intent)
                }
            })
            //swipeEdit
            val editSwipeHandler = object: SwipeToEditCallback(this){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = binding?.recycleView?.adapter as happyPlacesAdapter
                    adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition)
                }
            }
            val editItemTouchHelper= ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(binding?.recycleView)

            //swipeDelete
            val deleteSwipeHandler = object: SwipeToDeleteCallback(this){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = binding?.recycleView?.adapter as happyPlacesAdapter
                    val item =adapter.notifyDeleteItem(viewHolder.adapterPosition)
                    lifecycleScope.launch {
                        happyPlaceDao.delete(item)
                        itemAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    }
                    // set recycler view again
                    handleRecyclerView()
                }
            }
            val deleteItemTouchHelper= ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(binding?.recycleView)
        }else{
            binding?.recycleView?.visibility= View.GONE
            binding?.noRecordFound?.visibility= View.VISIBLE
        }
    }






    











companion object{
    internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1


}
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}