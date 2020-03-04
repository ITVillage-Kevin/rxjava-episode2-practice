package com.itvillage.sensor;

import com.itvillage.domain.Temperature;
import com.itvillage.utils.NumberUtil;
import com.itvillage.utils.TimeUtil;
import io.reactivex.Observable;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 온도 센서
 * - 실제 온도 센서에서 온도 데이터를 랜덤한 시간에 가져오도록 시뮬레이션 한다.
 */
@Component
public class TemperatureSensor {

    // 온도 데이터를 통지하는 스트림을 생성한다.
    public Observable<Temperature> getTemperatureStream(){
        return Observable.interval(500L, TimeUnit.MILLISECONDS)
                .delay(item -> {
                    TimeUtil.sleep(NumberUtil.randomRange(1000, 3000));
                    return Observable.just(item);
                })
                .map(notUse -> this.getTemperature())
                .publish() // 구독자들에게 통지되는 데이터를 브로드 캐스팅한다.
                .refCount(); // 구독자가 있는 경우에만 데이터를 통지한다.
    }

    private Temperature getTemperature() {
        return new Temperature(NumberUtil.randomRange(-10, 30));
    }
}
