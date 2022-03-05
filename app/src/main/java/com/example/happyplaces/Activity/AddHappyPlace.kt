package com.example.happyplaces.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.lifecycleScope
import com.example.happyplaces.Database.HappyPlaceEntity
import com.example.happyplaces.Database.HappyPlacesDao
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.Database.happyPlacesApp
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlace : AppCompatActivity() {
    private var binding: ActivityAddHappyPlaceBinding?=null
    val c=Calendar.getInstance()
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var mapLauncher: ActivityResultLauncher<Intent>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var customDialog:Dialog?=null
    private var currentImageUri: Uri? = null
    private var mLatitude:Double=0.0
    private var mLongitude:Double=0.0
    var happyPlaceDetailEntity:HappyPlaceEntity?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val happyPlaceDao=(application as happyPlacesApp).db.happyPlacesDao()

        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)

        setSupportActionBar(binding?.toolbarAddPlace)
        if (supportActionBar !=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        if (!Places.isInitialized()){
            Places.initialize(this,resources.getString(R.string.google_maps_api_key))
        }


        //set fetched data to form
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailEntity=intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceEntity?
        }
        if (happyPlaceDetailEntity != null){
                lifecycleScope.launch {
                    supportActionBar?.title = "Edit Happy Place"
                    binding?.etTitle?.setText(happyPlaceDetailEntity!!.title)
                    //because we are checking if currentImageUri is null
                    currentImageUri=Uri.parse(happyPlaceDetailEntity!!.image)
                    binding?.ivImage?.setImageURI(currentImageUri)
                    binding?.etDescription?.setText(happyPlaceDetailEntity!!.description)
                    binding?.etLocation?.setText(happyPlaceDetailEntity!!.location)
                    mLatitude=happyPlaceDetailEntity!!.latitude
                    mLongitude=happyPlaceDetailEntity!!.longitude
                    binding?.btnSave?.text="Update"
                }
        }

        //start activity for result for camera
            cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {

                    if (result.data != null){
                        try {
                            showProgressDialog()
                            lifecycleScope.launch {
                                val bitmap: Bitmap = result!!.data!!.extras!!.get("data") as Bitmap
                                currentImageUri=saveBitmapFile(bitmap)
                                binding?.ivImage?.setImageBitmap(bitmap)
                                }
                        }catch (e:Exception){
                            e.printStackTrace()
                            Toast.makeText(this,"Failed to load image from camera",Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

          //start activity for result for gallery
            galleryLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        if (result.data != null){
                            try {
                                showProgressDialog()
                                    lifecycleScope.launch {
                                        // set image uri in case picking from gallery , in order to use uri on saving picture in database
                                        currentImageUri=result.data?.data
                                        currentImageUri?.let {
                                            if (Build.VERSION.SDK_INT < 28) {
                                                val bitmap = MediaStore.Images.Media.getBitmap(this@AddHappyPlace.contentResolver, currentImageUri)
                                                currentImageUri=saveBitmapFile(bitmap)
                                                binding?.ivImage?.setImageBitmap(bitmap)
                                            } else {
                                                val source = ImageDecoder.createSource(this@AddHappyPlace.contentResolver, currentImageUri!!)
                                                val bitmap = ImageDecoder.decodeBitmap(source)
                                                currentImageUri=saveBitmapFile(bitmap)
                                                binding?.ivImage?.setImageBitmap(bitmap)
                                            }
                                        }
                                    }
                            }catch (e:Exception){
                                e.printStackTrace()
                                Toast.makeText(this,"Failed to load image from gallery",Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

//        mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                if (result.data != null){
//                    try {
//                        val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)
//
//                        binding?.etLocation?.setText(place.address)
//                        mLatitude = place.latLng!!.latitude
//                        mLongitude = place.latLng!!.longitude
//
//                    }catch (e:Exception){
//                        e.printStackTrace()
//                        Toast.makeText(this,"rid",Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }

        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }
        //set binding.etDate as we enter activity
        val sdf=SimpleDateFormat("yyyy.MM.dd",Locale.getDefault())
        binding?.etDate?.setText(sdf.format(c.time).toString())
        binding?.etDate?.setOnClickListener {
            datePicker()
        }
        binding?.tvAddImage?.setOnClickListener {
            setImage()
        }

        binding?.btnSave?.setOnClickListener {
            save(happyPlaceDao)
        }

        binding?.btnCurrent?.setOnClickListener {
            if (!isLocationEnabled()){
                Toast.makeText(this,"your location provider is turned off. please turn it on",Toast.LENGTH_SHORT).show()
                val intent=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }else{
                Dexter.withContext(this)
                    .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,)
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()){
                                requestNewLocationData()
                            }
                        }
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                            showRationalDialog()
                        }
                    }).check()

            }



        }

//        binding?.etLocation?.setOnClickListener {
//            try {
//                val fields= listOf(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)
//                val intent= Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(this)
//                mapLauncher.launch(intent)
//
//
//            }catch (e:Exception){
//                e.printStackTrace()
//            }
//
//        }







    }//FUN

    //hide keyboard when touched outside
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }


    //show loading dialog
    private fun showProgressDialog(){
        customDialog= Dialog(this)
        customDialog?.setContentView(R.layout.dialog_custom_progress)
        customDialog?.show()
    }

    //canceling the loading dialog
    private fun cancelDialog(){
        if (customDialog!=null){
            customDialog?.dismiss()
            customDialog=null
        }
    }

    private fun datePicker(){
        // implement c in AppCompat to be able to use it in onCreate too, so then we can automatically set binding.etDate as we enter to activity
        val year=c.get(Calendar.YEAR)
        val month=c.get(Calendar.MONTH)
        val day=c.get(Calendar.DAY_OF_MONTH)

            val dpd=DatePickerDialog(this,DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                val sdf=SimpleDateFormat("yyyy.MM.dd",Locale.getDefault())
               // binding?.etDate?.setText(""+mDay+"/"+mMonth+"/"+mYear)
                binding?.etDate?.setText(sdf.format(c.time).toString())
            },year,month,day)
            dpd.show()
    }

    private fun setImage(){
        val pictureDialog=AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItem= arrayOf("select photo from gallery","capture photo from camera")
        pictureDialog.setItems(pictureDialogItem){
                dialog,which->
                     when(which){
                         0-> choosePhotoFromGallery()
                         1-> openCamera()
                     }
        }
        pictureDialog.show()
    }


    private fun choosePhotoFromGallery(){
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        galleryLauncher.launch(intent)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                    showRationalDialog()
                }
            }).check()
    }

    private fun openCamera() {
        //ask camera and read storage permission
        val permissionRequests = mutableListOf<String>()
        permissionRequests.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionRequests.add(Manifest.permission.CAMERA)
        //ask write external storage permission when sdk is less than 28
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionRequests.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        Dexter.withContext(this)
            .withPermissions(permissionRequests)
            .withListener(object:MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport){
                    if (report.areAllPermissionsGranted()) {
//                        val values = ContentValues()
//                        values.put(MediaStore.Images.Media.TITLE, "New Picture")
//                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
//                        currentImageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                       // intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri)
                        cameraLauncher.launch(intent)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?){
                    showRationalDialog()
                }
            }).check()
    }

    private fun showRationalDialog(){
        AlertDialog.Builder(this@AddHappyPlace).setMessage(
            "can't work without required permissions,you can change setting under application setting")
            .setPositiveButton("Go to setting"){ _,_ ->
                try {
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri=Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?):Uri{
        var result=""
        withContext(Dispatchers.IO){
            if (mBitmap != null){
                try {
                    val bytes= ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes)
                    val f= File(externalCacheDir?.absoluteFile.toString() +
                            File.separator +"HappyPlaces_" + System.currentTimeMillis()/1000 + ".jpeg")
                    val fo= FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result=f.absolutePath
                    cancelDialog()

//                    runOnUiThread {
//                         cancelDialog()
//                        if (result.isNotEmpty()){
//                            Toast.makeText(this@AddHappyPlace,"file was saved on: $result",Toast.LENGTH_SHORT).show()
//                        }else{
//                            Toast.makeText(this@AddHappyPlace,"something went wrong",Toast.LENGTH_SHORT).show()
//                        }
//                    }
                }
                catch (e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
        }
        return Uri.parse(result)

    }

    //check if the location in off or on
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    //for take the last location of user
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var locationRequest = LocationRequest.create().apply {
            interval =0
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates=1
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallBack, Looper.myLooper()!!)
    }

    //this val is used in top function
    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation:Location=locationResult.lastLocation
            mLatitude=lastLocation.latitude
            Log.e("current latitude","$mLatitude")
            mLongitude=lastLocation.longitude
            Log.e("current Longitude","$mLongitude")
            lifecycleScope.launch(Dispatchers.Main){
                binding?.etLocation?.setText(getAddress(mLatitude,mLongitude))
            }
        }
    }

     fun getAddress(latitude:Double,longitude:Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""
        addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses.isNotEmpty()) {
            address = addresses[0]
            addressText = address.getAddressLine(0)
        } else{
            addressText = "its not appear"
        }
        return addressText
    }

    private fun save(happyPlaceDao: HappyPlacesDao){
        when{
            binding?.etTitle?.text.isNullOrEmpty() -> {
                Toast.makeText(this,"please enter a title",Toast.LENGTH_SHORT).show()
            }
            binding?.etDescription?.text.isNullOrEmpty() -> {
                Toast.makeText(this,"please enter a description",Toast.LENGTH_SHORT).show()
            }
            binding?.etLocation?.text.isNullOrEmpty() -> {
                Toast.makeText(this,"please enter a location",Toast.LENGTH_SHORT).show()
            }
            currentImageUri ==null -> {
                Toast.makeText(this,"please select an image",Toast.LENGTH_SHORT).show()
            }
            else ->{
                val title =binding?.etTitle?.text.toString()
                val image=currentImageUri.toString()
                val description =binding?.etDescription?.text.toString()
                val date =binding?.etDate?.text.toString()
                val location =binding?.etLocation?.text.toString()
                if (happyPlaceDetailEntity==null){
                    lifecycleScope.launch {
                        happyPlaceDao.insert(
                            HappyPlaceEntity (
                                title = title,
                                image = image,
                                description = description,
                                date = date,
                                location = location,
                                latitude = mLatitude,
                                longitude = mLongitude)
                        )
                        Toast.makeText(applicationContext,"record was saved",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    lifecycleScope.launch {
                        happyPlaceDao.update(
                            HappyPlaceEntity(
                            happyPlaceDetailEntity!!.id,
                            title = title,
                            image = image,
                            description = description,
                            date = date,
                            location = location,
                            latitude = mLatitude,
                            longitude = mLongitude)
                        )
                        Toast.makeText(applicationContext,"record updated",Toast.LENGTH_SHORT).show()
                    }
                }

                finish()
            }
        }
    }










//    companion object{
//        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
//    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}