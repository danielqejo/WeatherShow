package br.com.danielsantana.weathershow.task;

import android.net.Uri;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import br.com.danielsantana.weathershow.task.callback.TaskCallback;
import br.com.danielsantana.weathershow.model.Channel;
import br.com.danielsantana.weathershow.task.exception.YahooWeatherTaskException;



public class YahooWeatherTask extends AsyncTask<String, Void, String> {

    /** Callback */
    private TaskCallback callback;
    /** Task ID */
    private final Integer TASK_ID = 2;

    /**
     * Constructor with callback
     * @param callback
     */
    public YahooWeatherTask(TaskCallback callback) {
        this.callback = callback;
    }

    /**
     * Connects on json provider and retrieves it fully
     * @param params values for search on json
     * @return JSON
     */
    @Override
    protected String doInBackground(String... params) {
        //creates query for database
        String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\") and u='%s'", params[0], params[1]);

        //transforms into URL Encoding
        String uriFormat = Uri.encode(YQL);

        //merge all to collect json
        String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", uriFormat);

        try {
            //creates connection
            URL url = new URL(endpoint);

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
            callback.onTaskFailure(new YahooWeatherTaskException("Serviço do Yahoo não obteve nenhum resultado."));
            return;
        }

        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        jsonObject = jsonObject.getAsJsonObject("query");


        Integer count = jsonObject.get("count").getAsInt();

        //Verifica se o json retornou um resultado
        if(count == 0){
            callback.onTaskFailure(new YahooWeatherTaskException("Nenhum dado foi encontrado para o local indicado."));
            return;
        }

        jsonObject = jsonObject.getAsJsonObject("results").getAsJsonObject("channel");


        //builds Object with the JsonElement found
        Gson gson = new GsonBuilder().create();
        Channel channel = gson.fromJson(jsonObject, Channel.class);

        callback.onTaskSucess(TASK_ID, channel);
    }
}
