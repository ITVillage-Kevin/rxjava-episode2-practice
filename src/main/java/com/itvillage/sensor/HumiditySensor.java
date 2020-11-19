package com.itvillage.sensor;

import com.itvillage.utils.LogType;
import com.itvillage.utils.Logger;
import com.itvillage.utils.NumberUtil;
import com.itvillage.utils.TimeUtil;
import io.reactivex.Observable;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * 습도 센서 클래스
 * - 실제 습도 센서에서 습도 데이터를 랜덤한 시간에 가져오는것으로 시뮬레이션 한다.
 * TODO TemperatureSensor와 공통 되는 부분 리팩토링 필요
 */
@Component
public class HumiditySensor {
    // 습도 데이터를 통지하는 스트림을 생성한다.
    public Observable<Integer> getHumidityStream(){
        return Observable.interval(0L, TimeUnit.MILLISECONDS)
                    .delay(item -> {
                        TimeUtil.sleep(NumberUtil.randomRange(1000, 3000));
                        return Observable.just(item);
                    })
                    .map(notUse -> this.getHumidity());
    }

    private int getHumidity() {
        return NumberUtil.randomRange(30, 70);
    }
}
