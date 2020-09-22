package com.mindorks.example.ubercaranimation

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.mindorks.example.ubercaranimation.model.BaseResponse
import com.mindorks.example.ubercaranimation.sevices.ApiService
import com.mindorks.example.ubercaranimation.util.AnimationUtils
import com.mindorks.example.ubercaranimation.util.MapUtils
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class LoTrinhActivity : AppCompatActivity(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null
    var latLng = LatLng(28.435350000000003, 77.11368)
    private var grayPolyline: Polyline? = null
    private var blackPolyline: Polyline? = null
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    val locationList = ArrayList<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lo_trinh)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        this.googleMap = p0
        if (googleMap != null) {
            try {
                getListOfLocations()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun getListOfLocations() {
        ApiService.service.getLocation().subscribeOn(Schedulers.io()) //(*)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<BaseResponse> {
                override fun onSuccess(respon: BaseResponse) {
                    // xu ly ve anh dg di o day.
                    if (respon.lstCarPosInfos != null && respon.lstCarPosInfos!!.isNotEmpty()) {
                        latLng = LatLng(respon.lstCarPosInfos!![0].latitude!!, respon.lstCarPosInfos!![0].longtitude!!)
                        googleMap?.animateCamera( CameraUpdateFactory.newLatLngZoom(latLng, 14f ))
                        for (item in respon.lstCarPosInfos!!) {
                            createMarker(item.latitude!!, item.longtitude!!, getColor(item.color))
                            item.longtitude?.let { item.latitude?.let { it1 -> LatLng(it1, it) } }?.let {
                                locationList.add(
                                    it
                                )
                            }
                        }
                        showPath(locationList)
                    }
                }

                override fun onSubscribe(d: Disposable) {}

                override fun onError(e: Throwable) {
                }
            })
    }

    private fun getColor(code : String?): Int{
        var mColor = 0
        when(code){
            "black" ->{
                mColor = R.drawable.bg_black
            }
            "darker_gray" ->{
                mColor = R.drawable.bg_gray
            }
            "holo_blue_dark" ->{
                mColor = R.drawable.bg_bule
            }
            "holo_green_dark" ->{
                mColor = R.drawable.bg_green
            }
            "holo_orange_dark" ->{
                mColor = R.drawable.bg_orange
            }
            "holo_red_dark" ->{
                mColor = R.drawable.bg_red
            }
        }
        return mColor
    }

    private fun createMarker(latitude: Double, longitude: Double, iconResID: Int): Marker? {
        val latLng = LatLng(latitude, longitude)
        return googleMap?.addMarker(
            MarkerOptions().position(latLng).flat(true).
            icon(MapUtils.bitmapDescriptorFromVector(this, iconResID)))
    }

    private fun showPath(latLngList: ArrayList<LatLng>) {
        val builder = LatLngBounds.Builder()
        for (latLng in latLngList) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))

        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.GRAY)
        polylineOptions.width(5f)
        polylineOptions.addAll(latLngList)
        grayPolyline = googleMap?.addPolyline(polylineOptions)

        val blackPolylineOptions = PolylineOptions()
        blackPolylineOptions.color(Color.BLACK)
        blackPolylineOptions.width(5f)
        blackPolyline = googleMap?.addPolyline(blackPolylineOptions)

        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker?.setAnchor(0.5f, 0.5f)
        destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
        destinationMarker?.setAnchor(0.5f, 0.5f)

        val polylineAnimator = AnimationUtils.polylineAnimator()
        polylineAnimator.addUpdateListener { valueAnimator ->
            val percentValue = (valueAnimator.animatedValue as Int)
            val index = (grayPolyline?.points!!.size) * (percentValue / 100.0f).toInt()
            blackPolyline?.points = grayPolyline?.points!!.subList(0, index)
        }
        polylineAnimator.start()
    }
    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker {
        val bitmapDescriptor =
            BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap())
        return googleMap?.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )!!
    }

}