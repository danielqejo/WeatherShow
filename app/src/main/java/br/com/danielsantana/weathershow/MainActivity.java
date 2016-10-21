package br.com.danielsantana.weathershow;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import br.com.danielsantana.weathershow.model.Channel;
import br.com.danielsantana.weathershow.service.callback.WeatherServiceCallback;
import br.com.danielsantana.weathershow.service.WeatherService;
import br.com.danielsantana.weathershow.utils.SearchHelper;

public class MainActivity extends AppCompatActivity implements WeatherServiceCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, AdapterView.OnItemSelectedListener {

    //view Components
    private ImageView weatherImageView;
    private TextView temperaturaTextView;
    private TextView condicaoTextView;
    private TextView localTextView;
    private Button weatherChangeButtonView;
    private Button refreshButton;
    private Spinner listSpinnerView;
    private ProgressDialog dialog;

    //service components
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private WeatherService mWeatherService;
    private Location mLastLocation;

    /**
     * onCreate method from Android
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set up all services
        buildGoogleApi();
        mWeatherService = new WeatherService(this);
        createLocationRequest();

        //set up all view content
        setContentView(R.layout.activity_main);
        weatherImageView = (ImageView) findViewById(R.id.weatherImageView);
        temperaturaTextView = (TextView) findViewById(R.id.temperaturaTextView);
        condicaoTextView = (TextView) findViewById(R.id.condicaoTextView);
        localTextView = (TextView) findViewById(R.id.localTextView);
        weatherChangeButtonView = (Button) findViewById(R.id.weatherChangeButtonView);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        listSpinnerView = (Spinner) findViewById(R.id.listCapitais);

        //set on select for spinner
        listSpinnerView.setOnItemSelectedListener(this);


        //starts Dialog for first attempt
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();
    }

    /**
     * Creates values for automatic location requests.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        //sets interval of checks
        mLocationRequest.setInterval(60000);

        //sets minimum value of interval checks
        mLocationRequest.setFastestInterval(45000);

        //sets location to "very precise"
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * onStart method from Android
     */
    @Override
    protected void onStart() {
        //connects on Google Services
        mGoogleApiClient.connect();
        super.onStart();
    }

    /**
     * onStop method from Android
     */
    @Override
    protected void onStop() {
        //disconnects from Google Services
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * Builds Google Services Api
     */
    private void buildGoogleApi() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }

    /**
     * Updates the components of the view according to the new Channel object
     * @param channel
     */
    @Override
    public void serviceSucess(Channel channel) {
        dialog.hide();

        int resourceId = getResources().getIdentifier("drawable/icon_" + channel.getItem().getCondition().getCode(), null, getPackageName());

        Drawable weatherImageDrawable = ContextCompat.getDrawable(this, resourceId);

        weatherImageView.setImageDrawable(weatherImageDrawable);
        temperaturaTextView.setText(channel.getItem().getCondition().getTemp() + "\u00B0" + channel.getUnits().getTemperature());
        condicaoTextView.setText(channel.getItem().getCondition().getText());
        localTextView.setText(channel.getLocation().getCity() + ", " + channel.getLocation().getRegion());


    }

    /**
     * Runs a toast for not running successfully the service.
     * @param e
     */
    @Override
    public void serviceFailure(Exception e) {
        dialog.hide();
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Runs Weather Service after successfully connecting to Google Services
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            mWeatherService.getWeatherFromGoogle(String.valueOf(mLastLocation.getLatitude()),
                    String.valueOf(mLastLocation.getLongitude()),
                    SearchHelper.getGoogleApiKey());
        } else {
            dialog.hide();
            Toast.makeText(this, "Não foi possível detectar a sua localização.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Start updates for current location
     */
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Runs a toast for ended connection with Google Services
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "A conexão foi encerrada, tente novamente.", Toast.LENGTH_LONG).show();
    }

    /**
     * Runs a toast for not connecting successfully on Google Services
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Não foi possível conectar, tente novamente mais tarde.", Toast.LENGTH_LONG).show();
    }

    /**
     * Updates current location with new location
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    /**
     * Changes the current type of temperature (°C / °F)
     * @param view
     */
    public void changeDisplayTypeTemperature(View view){

        String lastLocation = localTextView.getText().toString();

        if(SearchHelper.getTemperatureType().equalsIgnoreCase("c")){
            weatherChangeButtonView.setText("°C");
            dialog.show();
            mWeatherService.refreshWeather(lastLocation, "f");
            SearchHelper.setTemperatureType("f");

        }else{
            weatherChangeButtonView.setText("°F");
            dialog.show();
            mWeatherService.refreshWeather(lastLocation, "c");
            SearchHelper.setTemperatureType("c");
        }
    }

    /**
     * Runs Weather Service with the selected state
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String capital = listSpinnerView.getSelectedItem().toString();
        dialog.show();
        mWeatherService.refreshWeather(capital, SearchHelper.getTemperatureType());
    }

    /**
     * Overrided method // no use
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //LITERALLY DOES NOTHING
    }

    /**
     * Refreshes the weather
     * @param view
     */
    public void refresh(View view){
        dialog.show();
        if(mLastLocation != null){
            mWeatherService.getWeatherFromGoogle(String.valueOf(mLastLocation.getLatitude()),
                    String.valueOf(mLastLocation.getLongitude()),
                    SearchHelper.getGoogleApiKey());
        } else {
            dialog.hide();
            Toast.makeText(this, "Não foi possível detectar a sua localização.", Toast.LENGTH_LONG).show();
        }
    }

}
