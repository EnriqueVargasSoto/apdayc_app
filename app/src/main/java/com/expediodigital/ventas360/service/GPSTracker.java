package com.expediodigital.ventas360.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kevin Robinson Meza Hinostroza on septiembre 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class GPSTracker {
    // Get Class Name
    private static String TAG = GPSTracker.class.getName();
    public static final long DEFAULT_MIN_TIME_BW_UPDATES_GPS = 1000 * 5; // 5 seconds
    public static final long DEFAULT_MIN_TIME_BW_UPDATES_NETWORK = 1000 * 6; // 6 seconds
    public static final long DEFAULT_TIME_TO_FORCE_UPDATE = 1000*60*2; // 6 seconds
    private final Context mContext;

    // flag for GPS Status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    Location lastLocation = null;
    Location locationGeneral;

    double latitud = 0.0;
    double longitud = 0.0;

    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; //Not use meters

    // The minimum time between updates in milliseconds
    private long MIN_TIME_BW_UPDATES_GPS;
    private long MIN_TIME_BW_UPDATES_NETWORK;
    private long TIME_TO_FORCE_UPDATE = DEFAULT_TIME_TO_FORCE_UPDATE;

    // Declaring a Location Manager
    protected LocationManager locationManager;

    //Location listeners
    MyLocationListener providerListener_NETWORK;
    MyLocationListener providerListener_GPS;


    public GPSTracker(Context context) {
        this.mContext = context;
        this.MIN_TIME_BW_UPDATES_GPS    = DEFAULT_MIN_TIME_BW_UPDATES_GPS;
        this.MIN_TIME_BW_UPDATES_NETWORK = DEFAULT_MIN_TIME_BW_UPDATES_NETWORK;

        try {
            locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
            //getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            providerListener_GPS = new MyLocationListener();
            providerListener_NETWORK = new MyLocationListener();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @param MIN_TIME_BW_UPDATES_GPS Tiempo entre las actualizaciones de la ubicacion para GPS, el tiempo debe estar indicado en milisegundos.
     * @param MIN_TIME_BW_UPDATES_NETWORK Tiempo entre las actualizaciones de la ubicacion para NETWORK, el tiempo debe estar indicado en milisegundos.
     */
    public GPSTracker(Context context, long MIN_TIME_BW_UPDATES_GPS, long MIN_TIME_BW_UPDATES_NETWORK) {
        this.mContext = context;
        this.MIN_TIME_BW_UPDATES_GPS = MIN_TIME_BW_UPDATES_GPS;
        this.MIN_TIME_BW_UPDATES_NETWORK = MIN_TIME_BW_UPDATES_NETWORK;
        try {
            locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
            //getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            providerListener_GPS = new MyLocationListener();
            providerListener_NETWORK = new MyLocationListener();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public long getMIN_TIME_BW_UPDATES_GPS() {
        return MIN_TIME_BW_UPDATES_GPS;
    }

    public void setMIN_TIME_BW_UPDATES_GPS(long MIN_TIME_BW_UPDATES_GPS) {
        this.MIN_TIME_BW_UPDATES_GPS = MIN_TIME_BW_UPDATES_GPS;
    }

    public long getMIN_TIME_BW_UPDATES_NETWORK() {
        return MIN_TIME_BW_UPDATES_NETWORK;
    }

    public void setMIN_TIME_BW_UPDATES_NETWORK(long MIN_TIME_BW_UPDATES_NETWORK) {
        this.MIN_TIME_BW_UPDATES_NETWORK = MIN_TIME_BW_UPDATES_NETWORK;
    }

    public long getTIME_TO_FORCE_UPDATE() {
        return TIME_TO_FORCE_UPDATE;
    }

    public void setTIME_TO_FORCE_UPDATE(long TIME_TO_FORCE_UPDATE) {
        this.TIME_TO_FORCE_UPDATE = TIME_TO_FORCE_UPDATE;
    }

    /**
     * Try to get my current location by GPS or Network Provider
     */
    public void getLocations() {
        Log.d(TAG,"getLocations from GPSTracker");
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                //Comprobar si es tiene permisos
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"There's no permission for ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION");
                    return;
                }
                setRequestUpdates();
            } else {
                //En versiones anteriores al api 23 no es necesario comprobar permisos
                setRequestUpdates();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Impossible to connect to LocationManager", e);
        }
    }

    public boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isNetworkEnabled() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @SuppressLint("MissingPermission")
    void setRequestUpdates() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES_GPS,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                providerListener_GPS
        );
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES_NETWORK,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                providerListener_NETWORK
        );

        if (locationManager != null) {
            if (isGPSEnabled){
                Log.d(TAG, "Application use GPS Service");
                locationGeneral = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (locationGeneral!=null)
                    getPosition(locationGeneral);
            }
            if (isNetworkEnabled){
                Log.d(TAG, "Application use Network State to get GPS coordinates");
                locationGeneral = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (locationGeneral!=null)
                    getPosition(locationGeneral);
            }
        }
    }

    /**
     * GPSTracker latitude getter and setter
     * @return latitude
     */
    public double getLatitude() {
        return latitud;
    }

    /**
     * GPSTracker longitude getter and setter
     * @return
     */
    public double getLongitude() {
        return longitud;
    }

    /**
     * Stop using GPS listener
     * Calling this method will stop using GPS in your app
     */
    public void stopUsingGPS() {
        Log.d(TAG,"stoping GPSTracker...");
        if (locationManager != null) {
            locationManager.removeUpdates(providerListener_GPS);
            locationManager.removeUpdates(providerListener_NETWORK);
        }
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        //Setting Dialog Title
        alertDialog.setTitle("titulo");

        //Setting Dialog Message
        alertDialog.setMessage("Settings");

        //On Pressing Setting button
        alertDialog.setPositiveButton("positiveButton", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton("cancelButton", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    /**
     * Get list of address by latitude and longitude
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            /**
             * Geocoder.getFromLocation - Returns an array of Addresses
             * that are known to describe the area immediately surrounding the given latitude and longitude.
             */
            List<Address> addresses = geocoder.getFromLocation(latitud, longitud, this.geocoderMaxResults);

            return addresses;
        } catch (IOException e) {
            //e.printStackTrace();
            Log.e(TAG, "Impossible to connect to Geocoder", e);
        }

        return null;
    }

    /**
     * Try to get AddressLine
     * @return null or addressLine
     */
    public String getAddressLine(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);

            return addressLine;
        } else {
            return null;
        }
    }

    /**
     * Try to get Locality
     * @return null or locality
     */
    public String getLocality(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String locality = address.getLocality();

            return locality;
        }
        else {
            return null;
        }
    }

    /**
     * Try to get Postal Code
     * @return null or postalCode
     */
    public String getPostalCode(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();

            return postalCode;
        } else {
            return null;
        }
    }

    /**
     * Try to get CountryName
     * @return null or postalCode
     */
    public String getCountryName(Context context) {
        List<Address> addresses = getGeocoderAddress(context);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();

            return countryName;
        } else {
            return null;
        }
    }

    public void getPosition(Location location) {
        if (location != null) {
            if (lastLocation!=null) {
                if ((location.getTime()-lastLocation.getTime())>(TIME_TO_FORCE_UPDATE)) {//El lastLocation se actualiza cada 2 minutos a lo mucho
                    //Si la nueva ubicacion es mas reciente por 2minutos o mas, entonces se toma esa nueva ubicacion. Si no es mas reciente que al menos 2 minutos, se asume que el vendedor no se ha movido mucho en ese tiempo.
                    //De esta forma se descarta los NETWORK con mala precision y solo se toman en cuenta cuando haya pasado un tiempo prudente donde el usuario se haya movido o si es que la precision del NETWORK es mejor al de GPS
                    Log.e(TAG, "La diferencia de tiempos es de mas de 2 minutos, tomando la posicion mas reciente");
                    lastLocation = location;
                }else{
                    //Si la ubicacion no difiere de 2 minutos, se toma la ubicacion si es que es mas precisa, asumiendo que el vendedor no se ha movido mucho en ese corto tiempo.
                    if (lastLocation.getAccuracy()>=location.getAccuracy()) {
                        lastLocation = location;
                    }
                }
            }else{
                lastLocation = location;
            }
        }
        latitud = lastLocation.getLatitude();
        longitud = lastLocation.getLongitude();
        Log.w(TAG,"getPosition: Latitud:"+ latitud + ", Longitud:" + longitud + ", Precision:"+lastLocation.getAccuracy());
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    /*
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    */
    private class MyLocationListener implements LocationListener{
        String TAG = "MyLocationListener";
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG+":onLocationChanged:", "get posicion from :"+location.getProvider()+" provider");
            /*
            Log.e(TAG+":onLocationChanged:", "---------------------------------------------------");
            Log.v(TAG+":onLocationChanged:", "Obteniendo posicion..."
                    +"\nProveedor:"+location.getProvider()
                    +"\nExactitud:"+location.getAccuracy()
                    +"\nVelocidad:"+location.getSpeed()
                    +"\nHoraProveedor: "+location.getTime());
            Log.e("MyLocationListener :onLocationChanged:", "---------------------------------------------------");
            */
            getPosition(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    }

}
