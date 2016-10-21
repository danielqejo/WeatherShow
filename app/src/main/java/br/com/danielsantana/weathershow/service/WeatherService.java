package br.com.danielsantana.weathershow.service;

import br.com.danielsantana.weathershow.model.Channel;
import br.com.danielsantana.weathershow.service.callback.WeatherServiceCallback;
import br.com.danielsantana.weathershow.task.GoogleLocationTask;
import br.com.danielsantana.weathershow.task.YahooWeatherTask;
import br.com.danielsantana.weathershow.task.callback.TaskCallback;
import br.com.danielsantana.weathershow.utils.SearchHelper;

public class WeatherService implements TaskCallback {

    private WeatherServiceCallback callback;

    public WeatherService(WeatherServiceCallback callback) {
        this.callback = callback;
    }

    /**
     * Gets current Location from Google Services
     * @param latitude
     * @param longitude
     * @param apiKey
     */
    public void getWeatherFromGoogle(String latitude, String longitude, String apiKey){
        new GoogleLocationTask(this).execute(latitude, longitude, apiKey);
    }

    /**
     * Refreshes Weather from Yahoo
     * @param location
     * @param temperatureType
     */
    public void refreshWeather(String location, String temperatureType){
        new YahooWeatherTask(this).execute(location, temperatureType);
    }

    /**
     * Checks taskId and send to callback
     * @param taskId
     * @param object
     */
    @Override
    public void onTaskSucess(Integer taskId, Object object) {
        if(taskId == 1){
            new YahooWeatherTask(this).execute(object.toString(), SearchHelper.getTemperatureType());
        }else{
            callback.serviceSucess((Channel) object);
        }
    }

    /**
     * Throw Exception to the callback
     * @param exception
     */
    @Override
    public void onTaskFailure(Exception exception) {
        callback.serviceFailure(exception);
    }
}