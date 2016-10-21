package br.com.danielsantana.weathershow.utils;

public final class SearchHelper {

    /**
     * Google Data Help Variables
     */
    private static final String GOOGLE_API_KEY = "AIzaSyBor2QzPRJ6XBQK9l8jOQoZ61EyTO3f2DA";
    public static final String GOOGLE_FAILURE = "Failure on Google Search";

    /**
     * Yahoo Data Help Variables
     */
    private static String temperatureType= "c";



    public static String getTemperatureType() {
        return temperatureType;
    }

    public static void setTemperatureType(String temperatureType) {
        SearchHelper.temperatureType = temperatureType;
    }

    public static String getGoogleApiKey() {
        return GOOGLE_API_KEY;
    }
}
