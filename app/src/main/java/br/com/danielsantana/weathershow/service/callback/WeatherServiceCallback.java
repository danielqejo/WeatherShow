package br.com.danielsantana.weathershow.service.callback;

import br.com.danielsantana.weathershow.model.Channel;

public interface WeatherServiceCallback {
    void serviceSucess(Channel channel);
    void serviceFailure(Exception e);
}
