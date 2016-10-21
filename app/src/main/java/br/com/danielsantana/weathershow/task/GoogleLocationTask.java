package br.com.danielsantana.weathershow.task;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


import br.com.danielsantana.weathershow.model.AddressComponents;
import br.com.danielsantana.weathershow.task.callback.TaskCallback;
import br.com.danielsantana.weathershow.task.exception.GoogleLocationTaskException;
import br.com.danielsantana.weathershow.utils.SearchHelper;

public class GoogleLocationTask extends AsyncTask<String, Void, String> {

    /** Callback */
    private TaskCallback callback;
    /** Task ID */
    private final Integer  TASK_ID = 1;

    /**
     * Constructor with callback
     * @param callback
     */
    public GoogleLocationTask(TaskCallback callback) {
        this.callback = callback;
    }

    /**
     * Connects on json provider and retrieves it fully
     * @param params values for search on json
     * @return JSON
     */
    @Override
    protected String doInBackground(String... params) {

        //sets url connection
        String latlng = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + params[0] +","+ params[1]
                + "&key=" + SearchHelper.getGoogleApiKey();

        try {
            //creates connection
            URL url = new URL(latlng);

            URLConnection urlConnection = url.openConnection();

            InputStream inputStream = urlConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


            StringBuilder result = new StringBuilder();
            String line;

            //append every line on the buffer
            while((line = bufferedReader.readLine()) != null){
                result.append(line);
            }

            return result.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Filter JSON and sends to the callback
     * @param s JSON
     */
    @Override
    protected void onPostExecute(String s) {
        //check if the json was filled
        if (s == null){
            callback.onTaskFailure(new GoogleLocationTaskException("Serviço do Google não obteve nenhum resultado."));
            return;
        }

        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("results");
        jsonObject = jsonArray.get(0).getAsJsonObject();

        //gets first result on the array
        jsonArray = jsonObject.getAsJsonArray("address_components");

        JsonElement jsonElement = null;

        //checks for current state
        for(int contaArray = 0; contaArray < jsonArray.size() ; contaArray++){
            JsonObject jsonObjectHelper = jsonArray.get(contaArray).getAsJsonObject();
            JsonArray jsonArrayHelper = jsonObjectHelper.getAsJsonArray("types");

            if(jsonArrayHelper.get(0).toString().equals("\"administrative_area_level_1\"")){
                jsonElement = jsonObjectHelper.getAsJsonObject();
            }
        }

        //checks if found the current state
        if(jsonElement == null){
            callback.onTaskFailure(new GoogleLocationTaskException("Elemento não encontrado."));
            return;
        }

        //builds Object with the JsonElement found
        Gson gson = new GsonBuilder().create();
        AddressComponents addressComponents = gson.fromJson(jsonElement, AddressComponents.class);

        callback.onTaskSucess(TASK_ID, addressComponents.getLongShort());
    }
}
