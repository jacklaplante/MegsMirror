package com.nielsmasdorp.speculum.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.assent.Assent;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nielsmasdorp.speculum.R;
import com.nielsmasdorp.speculum.SpeculumApplication;
import com.nielsmasdorp.speculum.models.Configuration;
import com.nielsmasdorp.speculum.models.RedditPost;
import com.nielsmasdorp.speculum.models.Weather;
import com.nielsmasdorp.speculum.presenters.MainPresenter;
import com.nielsmasdorp.speculum.util.ASFObjectStore;
import com.nielsmasdorp.speculum.util.Constants;
import com.nielsmasdorp.speculum.views.MainView;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainActivity extends AppCompatActivity implements MainView, View.OnSystemUiVisibilityChangeListener {

    // @formatter:off
    @BindView(R.id.iv_current_weather) ImageView ivWeatherCondition;
    @BindView(R.id.tv_current_temp) TextView tvWeatherTemperature;
    @BindView(R.id.weather_layout) LinearLayout llWeatherLayout;
    @BindView(R.id.tv_last_updated) TextView tvWeatherLastUpdated;

    @Nullable @BindView(R.id.tv_summary) TextView tvWeatherSummary;
    @Nullable @BindView(R.id.iv_forecast_weather1) ImageView ivDayOneIcon;
    @Nullable @BindView(R.id.tv_forecast_temp1) TextView tvDayOneTemperature;
    @Nullable @BindView(R.id.tv_forecast_date1) TextView tvDayOneDate;
    @Nullable @BindView(R.id.iv_forecast_weather2) ImageView ivDayTwoIcon;
    @Nullable @BindView(R.id.tv_forecast_temp2) TextView tvDayTwoTemperature;
    @Nullable @BindView(R.id.tv_forecast_date2) TextView tvDayTwoDate;
    @Nullable @BindView(R.id.iv_forecast_weather3) ImageView ivDayThreeIcon;
    @Nullable @BindView(R.id.tv_forecast_temp3) TextView tvDayThreeTemperature;
    @Nullable @BindView(R.id.tv_forecast_date3) TextView tvDayThreeDate;
    @Nullable @BindView(R.id.iv_forecast_weather4) ImageView ivDayFourIcon;
    @Nullable @BindView(R.id.tv_forecast_temp4) TextView tvDayFourTemperature;
    @Nullable @BindView(R.id.tv_forecast_date4) TextView tvDayFourDate;
    @Nullable @BindView(R.id.tv_calendar_event) TextView tvCalendarEvent;

    @BindString(R.string.old_config_found_snackbar) String oldConfigFound;
    @BindString(R.string.old_config_found_snackbar_back) String getOldConfigFoundBack;
    @BindString(R.string.give_command) String giveCommand;
    @BindString(R.string.listening) String listening;
    @BindString(R.string.command_understood) String commandUnderstood;
    @BindString(R.string.executing) String executing;
    @BindString(R.string.last_updated) String lastUpdated;
    @BindString(R.string.static_maps_api_key) String mapsApiKey;

    // @formatter:on

    @Inject
    MainPresenter presenter;

    @Inject
    ASFObjectStore<Configuration> objectStore;

    MaterialDialog mapDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((SpeculumApplication) getApplication()).createMainComponent(this).inject(this);
        Assent.setActivity(this, this);

        Configuration configuration = objectStore.get();
        boolean didLoadOldConfig = getIntent().getBooleanExtra(Constants.SAVED_CONFIGURATION_IDENTIFIER, false);

        ViewStub viewStub = configuration.isSimpleLayout() ?
                (ViewStub) findViewById(R.id.stub_simple) :
                (ViewStub) findViewById(R.id.stub_verbose);
        if (null != viewStub) viewStub.inflate();

        ButterKnife.bind(this);

        //never sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (didLoadOldConfig)
            showConfigurationSnackbar();

        presenter.setConfiguration(configuration);
    }

    private void showConfigurationSnackbar() {
        Snackbar snackbar = Snackbar
                .make(llWeatherLayout, oldConfigFound, Snackbar.LENGTH_LONG)
                .setAction(getOldConfigFoundBack, view -> {
                    onBackPressed();
                });

        snackbar.show();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUI() {
        View mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mDecorView.setOnSystemUiVisibilityChangeListener(this);
    }

    @Override
    public void showMap(String location) {

        String url = Constants.STATIC_MAPS_URL_FIRST +
                location + Constants.STATIC_MAPS_URL_SECOND +
                location + Constants.STATIC_MAPS_URL_THIRD +
                mapsApiKey;

        mapDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.map_image, false)
                .contentGravity(GravityEnum.CENTER)
                .build();

        View imageView = mapDialog.getCustomView();
        Picasso.with(MainActivity.this).load(url).into((ImageView) imageView);
        mapDialog.show();
    }

    @Override
    public void hideMap() {
        if (null != mapDialog && mapDialog.isShowing()) {
            mapDialog.dismiss();
            mapDialog = null;
        }
        hideSystemUI();
    }

    @Override
    @SuppressWarnings("all")
    public void displayCurrentWeather(Weather weather, boolean isSimpleLayout) {

        // Current simple weather
        this.ivWeatherCondition.setImageResource(weather.getIconId());
        this.tvWeatherTemperature.setText(weather.getTemperature());
        this.tvWeatherLastUpdated.setText(lastUpdated + " " + weather.getLastUpdated());

        if (!isSimpleLayout) {
            this.tvDayOneDate.setText(weather.getForecast().get(0).getDate());
            this.tvDayOneTemperature.setText(weather.getForecast().get(0).getTemperature());
            this.ivDayOneIcon.setImageResource(weather.getForecast().get(0).getIconId());
            this.tvDayTwoDate.setText(weather.getForecast().get(1).getDate());
            this.tvDayTwoTemperature.setText(weather.getForecast().get(1).getTemperature());
            this.ivDayTwoIcon.setImageResource(weather.getForecast().get(1).getIconId());
            this.tvDayThreeDate.setText(weather.getForecast().get(2).getDate());
            this.tvDayThreeTemperature.setText(weather.getForecast().get(2).getTemperature());
            this.ivDayThreeIcon.setImageResource(weather.getForecast().get(2).getIconId());
            this.tvDayFourDate.setText(weather.getForecast().get(3).getDate());
            this.tvDayFourTemperature.setText(weather.getForecast().get(3).getTemperature());
            this.ivDayFourIcon.setImageResource(weather.getForecast().get(2).getIconId());
        } else {
            this.tvWeatherSummary.setText(weather.getSummary());
        }

        if (this.llWeatherLayout.getVisibility() != View.VISIBLE) {
            this.llWeatherLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    @SuppressWarnings("all")
    public void displayLatestCalendarEvent(String event) {
        this.tvCalendarEvent.setText(event);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Assent.setActivity(this, this);
        hideSystemUI();
        presenter.start(Assent.isPermissionGranted(Assent.READ_CALENDAR));
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.finish();
        if (isFinishing())
            Assent.setActivity(this, null);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            hideSystemUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((SpeculumApplication) getApplication()).releaseMainComponent();
    }
}
