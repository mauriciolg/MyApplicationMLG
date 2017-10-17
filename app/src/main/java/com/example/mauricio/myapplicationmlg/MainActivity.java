package com.example.mauricio.myapplicationmlg;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "openweatherimagen";

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

        //Cola de peticiones
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://192.168.1.66/ejemplo1/clima";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if (response.has("weather")) {

                                JSONArray weatherArray = response.getJSONArray("weather");
                                JSONObject weather = weatherArray.getJSONObject(0);

                                if (weather.has("icon")) {
                                    String icon = weather.getString("icon");

                                    Log.d(TAG, icon);
                                    // Identificador de la imagen
                                    int identificador = getResources().getIdentifier("img_" + icon, "drawable", getPackageName());

                                    identificador_guardado = identificador;

                                    //Asignar el identificador a la ImageView
                                    iv_weather.setImageDrawable(getResources().getDrawable(identificador, null));
                                }
                                if (response.has("main")) {
                                    JSONObject main = response.getJSONObject("main");

                                    double temp = main.getDouble("temp");
                                    String temperatura = "" + temp + "\u00b0";
                                    temperatura_guardada = temperatura;

                                    tv_temp.setText(temperatura);
                                }
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
        queue.add(jsObjRequest);
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


