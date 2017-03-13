package com.nielsmasdorp.speculum.presenters;

import android.app.Application;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.nielsmasdorp.speculum.R;
import com.nielsmasdorp.speculum.activity.MainActivity;
import com.nielsmasdorp.speculum.interactor.MainInteractor;
import com.nielsmasdorp.speculum.models.Configuration;
import com.nielsmasdorp.speculum.models.RedditPost;
import com.nielsmasdorp.speculum.models.Weather;
import com.nielsmasdorp.speculum.models.YoMommaJoke;
import com.nielsmasdorp.speculum.util.Constants;
import com.nielsmasdorp.speculum.views.MainView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class MainPresenterImpl implements MainPresenter {

    private MainView view;
    private MainInteractor interactor;
    private Application application;
    private Configuration configuration;
    private SpeechRecognizer recognizer;
    private TextToSpeech textToSpeech;

    public MainPresenterImpl(MainView view, MainInteractor interactor, Application application) {

        this.view = view;
        this.interactor = interactor;
        this.application = application;
    }

    /*
    Begin presenter methods
     */
    @Override
    public void setConfiguration(Configuration configuration) {

        this.configuration = configuration;
    }

    @Override
    public void start(boolean hasAccessToCalendar) {
        if (null != configuration) {
            startWeather();
            if (!configuration.isSimpleLayout()) {
                if (hasAccessToCalendar) {
                    startCalendar();
                }
            }
        }
    }

    private void updateData() {
        interactor.unSubscribe();
        if (null != configuration) {
            startWeather();
            if (!configuration.isSimpleLayout()) {
                startCalendar();
            }
        }
    }

    @Override
    public void showError(String error) {
        view.showError(error);
    }

    @Override
    public void finish() {
        tearDownSpeechService()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        interactor.unSubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showError(e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }

    private Observable<Void> tearDownSpeechService() {
        return Observable.defer(() -> {
            if (recognizer != null) {
                recognizer.cancel();
                recognizer.shutdown();
            }
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
            view.hideMap();
            return Observable.empty();
        });
    }

    /*
    End presenter methods
     */

    /*
    Begin start background data methods
     */
    private void startWeather() {
        interactor.loadWeather(configuration.getLocation(), configuration.isCelsius(), configuration.getPollingDelay(), ((MainActivity) view).getString(R.string.forecast_api_key), new WeatherSubscriber());
    }

    private void startCalendar() {
        interactor.loadLatestCalendarEvent(configuration.getPollingDelay(), new CalendarEventSubscriber());
    }
    /*
    End start background data methods
     */



    private final class WeatherSubscriber extends Subscriber<Weather> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            view.showError(e.getMessage());
        }

        @Override
        public void onNext(Weather weather) {
            view.displayCurrentWeather(weather, configuration.isSimpleLayout());
        }
    }

    private final class CalendarEventSubscriber extends Subscriber<String> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            view.showError(e.getMessage());
        }

        @Override
        public void onNext(String event) {
            view.displayLatestCalendarEvent(event);
        }
    }

    private final class YoMammaJokeSubscriber extends Subscriber<YoMommaJoke> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            view.showError(e.getMessage());
        }

        @Override
        public void onNext(YoMommaJoke joke) {
        }
    }

    private final class AssetSubscriber extends Subscriber<File> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            view.showError(e.getMessage());
        }

        @Override
        public void onNext(File assetDir) {
        }
    }
}
