package com.itvillage.domain;

/**
 * 온도와 습도 데이터를 구성하고 있는 기상 데이터 클래스
 */
public final class WeatherData {
    private final Temperature temperature;
    private final Humidity humidity;

    public WeatherData(Temperature temperature, Humidity humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public Humidity getHumidity() {
        return humidity;
    }
}
