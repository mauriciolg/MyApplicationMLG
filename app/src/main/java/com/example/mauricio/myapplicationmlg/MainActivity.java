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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Recurso de la ImageView
        final ImageView iv_weather = (ImageView) findViewById(R.id.iv_weather);
        final TextView tv_temp = (TextView) findViewById(R.id.tv_temp);


        //Cola de peticiones
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://192.168.1.69/ejemplo1/clima";

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

                                    //Asignar el identificador a la ImageView
                                    iv_weather.setImageDrawable(getResources().getDrawable(identificador, null));
                                }
                                if (response.has("main")) {
                                    JSONObject main = response.getJSONObject("main");

                                    double temp = main.getDouble("temp");

                                    tv_temp.setText("" + temp + "\u00b0");
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
}
