package com.example.mauricio.myapplicationmlg;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "_openweather_app_";
    private static final int REQUEST_LOCATION = 22;

    //Se utiliza para los servicios de ubicacion
    private GoogleApiClient mGoogleApiClient;

    //Variables para el Bundle savedInstanceState
    private static final String IDENTIFICADOR_KEY = "Identificador del ImageView";
    private static final String TEMPERATURA_KEY = "Temperatura";
    private int identificador_guardado;
    private String temperatura_guardada;
    //-------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Recurso de la ImageView
        final ImageView iv_weather = (ImageView) findViewById(R.id.iv_weather);
        final TextView tv_temp = (TextView) findViewById(R.id.tv_temp);

        //Cargar el estado guardado si existe
        if (savedInstanceState != null){
            identificador_guardado = savedInstanceState.getInt(IDENTIFICADOR_KEY);
            temperatura_guardada = savedInstanceState.getString(TEMPERATURA_KEY);

            //Asignar el identificador a la ImageView
            iv_weather.setImageDrawable(getResources().getDrawable(identificador_guardado, null));
            //Asignar la temperatura al TextView
            tv_temp.setText(temperatura_guardada);
        }

        // Creamos una instancia del Google Api Client
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                            //Aqui ya nos conectamos al Servicio del Api de Google
                            //Podemos solicitar la ubicacion, este metodo esta definido abajo
                            getLocation();

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    /***
     * Solicita la ubicacion mediante GPS. Primero se tiene que verificar que el usuario otorgue los permisos.
     */
    private void getLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //La primera vez que se ejecuta la actividad, se solicitan permisos
            //Si el usuario selecciono ok, o cancel en la ventana de permisos se mandara el resultado a onRequestPermissionsResult. Este metodo
            //se define abajo
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        //Aqui, ya tenemos permisos

        //Iniciamos
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, request, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        LatLng newLocation = new LatLng(location.getLatitude(),location.getLongitude());

                        makeHttpRequest(newLocation);


                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)  &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Tenemos permisos
                getLocation();
            }

        } else {
            // Permission was denied. Display an error message.
            Log.d(TAG, "No se tienen los permisos ACCESS_FINE_LOCATION  y  ACCESS_COARSE_LOCATION");
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        if(mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }


    private void makeHttpRequest(LatLng newLocation){
        //Recurso de la ImageView
        final ImageView iv_weather = (ImageView) findViewById(R.id.iv_weather);
        final TextView tv_temp = (TextView) findViewById(R.id.tv_temp);

        //Cola de peticiones
        RequestQueue queue = Volley.newRequestQueue(this);


        String url = "http://192.168.1.67/ejemplo1/clima/" + newLocation.latitude + "/" + newLocation.longitude;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            if (response.has("weather")) {

                                JSONArray weatherArray = response.getJSONArray("weather");
                                JSONObject weather = weatherArray.getJSONObject(0);


                                String ciudad = response.getString("name");
                                Log.v(TAG, "Ciudad: " + ciudad);

                                if (weather.has("icon")) {

                                    String icon = weather.getString("icon");
                                    // Identificador de la imagen
                                    int identificador = getResources().getIdentifier("img_" + icon, "drawable", getPackageName());

                                    // Guardar el identificador
                                    identificador_guardado = identificador;

                                    //Asignar el identificador a la ImageView
                                    iv_weather.setImageDrawable(getResources().getDrawable(identificador, null));
                                }


                            }

                            if (response.has("main")) {
                                JSONObject main = response.getJSONObject("main");
                                double temp = main.getDouble("temp");
                                String temperatura = "" + temp + "\u00b0";
                                temperatura_guardada = temperatura;

                                tv_temp.setText(temperatura);

                                Log.v(TAG, "Temperatura: " + temperatura);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.getMessage());

                    }
                });

        queue.add(jsonObjectRequest);

    }


    //Guardar el estado actual para poder cargarlo si la applicación pasa del estado
    //onPause y se dedestruye el proceso de la app. Por ejemplo cuando cambia la
    //orientación de la pantalla
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(IDENTIFICADOR_KEY, identificador_guardado);
        savedInstanceState.putString(TEMPERATURA_KEY, temperatura_guardada);

        super.onSaveInstanceState(savedInstanceState);
    }
}
