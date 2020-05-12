package com.mp.android.apps.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class GPSUtils {
    private static String TAG = GPSUtils.class.getSimpleName();
    private static GPSUtils mInstance;
    private Context mContext;
    private static LocationListener mLocationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Logger.d(TAG, "onStatusChanged");

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            Logger.d(TAG, "onProviderEnabled");

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            Logger.d(TAG, "onProviderDisabled");

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
        }
    };


    private GPSUtils(Context context) {
        this.mContext = context;
    }

    public static GPSUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GPSUtils(context);
        }
        return mInstance;
    }

    /**
     * 获取地理位置，先根据GPS获取，再根据网络获取
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public Location getLocation() {
        Location location = null;
        try {
            if (mContext == null) {
                return null;
            }
            LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return null;
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  //从gps获取经纬度
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {//当GPS信号弱没获取到位置的时候再从网络获取
                    location = getLocationByNetwork();
                }
            } else {    //从网络获取经纬度
                location = getLocationByNetwork();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return location;
    }

    public Map<String, String> getAddress() {
        Map<String, String> map = new HashMap<>(16);
        //用来接收位置的详细信息
        List<Address> result = null;
        String addressLine = "";
        Location location = getLocation();
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(mContext, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null && result.get(0) != null) {
            //这块获取到的是个数组我们取一个就好 下面是具体的方法查查API就能知道自己要什么
            if (result.get(0).getLocality() != null) {
                map.put("locality", result.get(0).getLocality());
            } else if (result.get(0).getAdminArea() != null) {
                map.put("locality", result.get(0).getAdminArea());
            } else {
                map.put("locality", "未知");
            }
            if (result.get(0).getSubLocality() != null) {
                map.put("sublocality", result.get(0).getSubLocality());
            } else {
                map.put("sublocality", "未知");
            }
        }

        return map;
    }

    /**
     * 判断是否开启了GPS或网络定位开关
     *
     * @return
     */
    public boolean isLocationProviderEnabled() {
        boolean result = false;
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return result;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            result = true;
        }
        return result;
    }

    /**
     * 获取地理位置，先根据GPS获取，再根据网络获取
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    private Location getLocationByNetwork() {
        Location location = null;
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, mLocationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return location;
    }
}
