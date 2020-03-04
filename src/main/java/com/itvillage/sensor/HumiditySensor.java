package com.itvillage.sensor;

import com.itvillage.domain.Humidity;
import com.itvillage.utils.NumberUtil;
import com.itvillage.utils.TimeUtil;
import io.reactivex.Observable;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 습도 센서 클래스
 * - 실제 습도 센서에서 습도 데이터를 랜덤한 시간에 가져오는것으로 시뮬레이션 한다.
 * TODO TemperatureSensor와 공통 되는 부분 리팩토링 필요
 */
@Component
public class HumiditySensor {
    private final Random random = new Random();

    // 습도 데이터를 통지하는 스트림을 생성한다.
    public Observable<Humidity> getHumidityStream(){
        return Observable.interval(0L, TimeUnit.MILLISECONDS)
                    .delay(item -> {
                        TimeUtil.sleep(NumberUtil.randomRange(1000, 3000));
                        return Observable.just(item);
                    })
                    .map(notUse -> this.getHumidity())
                    .publish() // 구독자들에게 통지되는 데이터를 브로드 캐스팅한다.
                    .refCount(); // 구독자가 있는 경우에만 데이터를 통지한다.
    }

    private Humidity getHumidity() {

        return new Humidity(NumberUtil.randomRange(30, 70));
    }
}
