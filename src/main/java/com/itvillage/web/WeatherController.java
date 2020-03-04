package com.itvillage.web;

import com.itvillage.domain.WeatherData;
import com.itvillage.sensor.HumiditySensor;
import com.itvillage.sensor.TemperatureSensor;
import com.itvillage.utils.LogType;
import com.itvillage.utils.Logger;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Consumer;

/**
 * 클라이언트의 기상 데이터 요청을 처리하는 Rest API 컨트롤러
 */
@RestController
public class WeatherController {
    final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;


    private final TemperatureSensor temperatureSensor;

    private final HumiditySensor humiditySensor;

    @Autowired
    public WeatherController(TemperatureSensor temperatureSensor, HumiditySensor humiditySensor) {
        this.temperatureSensor = temperatureSensor;
        this.humiditySensor = humiditySensor;
    }

    // Server Sent Event를 이용한 HTTP Streaming 연결
    @RequestMapping(
            value = "/stream/weather",
            method = RequestMethod.GET
    )
    public SseEmitter connectWeatherEvents(HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(SSE_SESSION_TIMEOUT);

        Disposable disposable = Observable.zip(
                temperatureSensor.getTemperatureStream(),
                humiditySensor.getHumidityStream(),
                (temperature, humidity) -> new WeatherData(temperature, humidity)
        ).subscribe(
                weatherData -> {
                    emitter.send(weatherData);
                    Logger.log(LogType.ON_NEXT,
                            weatherData.getTemperature().getValue() + ", " + weatherData.getHumidity().getValue());
                },
                error -> Logger.log(LogType.ON_ERROR, error.getMessage())
        );

        this.dispose(emitter, () -> {
            if(!disposable.isDisposed())
                disposable.dispose();
        });
        return emitter;
    }

    // TODO emitter 처리는 CustomEmitter를 만들어서 거기서 처리 하게 리팩토링 필요
    private void dispose(SseEmitter emitter, Runnable runnable){
        emitter.onCompletion(runnable);
        emitter.onTimeout(runnable);
    }
}
