package com.itvillage.domain;

import lombok.Data;

import javax.persistence.*;

/**
 * 온도와 습도 데이터를 구성하고 있는 기상 데이터 클래스
 */
@Data
@Entity
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weather_id")
    private long weatherId;
    private int temperature;
    private int humidity;

    public Weather(){}
    public Weather(int temperature, int humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
    }
}
