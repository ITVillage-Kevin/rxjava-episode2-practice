package com.itvillage.web;

import com.itvillage.domain.Weather;
import com.itvillage.repository.WeatherRepository;
import com.itvillage.sensor.HumiditySensor;
import com.itvillage.sensor.TemperatureSensor;
import com.itvillage.utils.LogType;
import com.itvillage.utils.Logger;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 클라이언트의 기상 데이터 요청을 처리하는 Rest API 컨트롤러
 */
@RestController
public class WeatherController {
    final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;

    private final TemperatureSensor temperatureSensor;
    private final HumiditySensor humiditySensor;
    private final WeatherRepository weatherRepository;
    private SseEmitter emitter;

    private List<Disposable> disposables = new ArrayList<>();

    @Autowired
    public WeatherController(TemperatureSensor temperatureSensor,
                             HumiditySensor humiditySensor,
                             WeatherRepository weatherRepository) {
        this.temperatureSensor = temperatureSensor;
        this.humiditySensor = humiditySensor;
        this.weatherRepository = weatherRepository;
    }

    // Server Sent Event를 이용한 HTTP Streaming 연결
    @CrossOrigin("*")
    @GetMapping("/stream/weather")
    public SseEmitter connectWeatherEvents() {
        emitter = new SseEmitter(SSE_SESSION_TIMEOUT);

        ConnectableObservable<Weather> observable =
                Observable.zip(
                    temperatureSensor.getTemperatureStream(),
                    humiditySensor.getHumidityStream(),
                    (temperature, humidity) -> new Weather(temperature, humidity)
                ).publish(); // 구독자들에게 통지되는 데이터를 브로드 캐스팅한다.

        Disposable disposableSend = sendWeatherData(observable);
        Disposable disposableSave = saveWeatherData(observable);
        disposables.addAll(Arrays.asList(disposableSend, disposableSave));

        observable.connect();

        this.dispose(emitter, () ->
                disposables.stream()
                .filter(disposable -> !disposable.isDisposed())
                .forEach(Disposable::dispose));
        return emitter;
    }

    private Disposable sendWeatherData(ConnectableObservable<Weather> observable) {
        return observable.subscribe(
                weather -> {
                    emitter.send(weather);
                    Logger.log(LogType.ON_NEXT,
                            weather.getTemperature() + ", " + weather.getHumidity());
                },
                error -> Logger.log(LogType.ON_ERROR, error.getMessage()));
    }

    private Disposable saveWeatherData(ConnectableObservable<Weather> observable) {
        return observable.subscribe(
                weather -> {
                    weatherRepository.save(weather);
                },
                error -> Logger.log(LogType.ON_ERROR, error.getMessage()));
    }

    private void dispose(SseEmitter emitter, Runnable runnable){
        emitter.onCompletion(runnable);
        emitter.onTimeout(runnable);
    }
}
