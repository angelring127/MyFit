package com.example.angel.myfit.basichistoryapi.events;


import com.example.angel.myfit.basichistoryapi.data.Sensor;

public class SensorRangeEvent {
    private Sensor sensor;

    public SensorRangeEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
