package com.example.angel.myfit.basichistoryapi.events;


import com.example.angel.myfit.basichistoryapi.data.Sensor;

public class NewSensorEvent {
    private Sensor sensor;

    public NewSensorEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
