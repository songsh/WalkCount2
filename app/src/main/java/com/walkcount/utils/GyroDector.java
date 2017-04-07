package com.walkcount.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class GyroDector implements SensorEventListener,Dector{

	String fileName = System.currentTimeMillis()+".log";
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_GYROSCOPE)
			return;
		LogUtils.file(event.values[SensorManager.DATA_Y]+"", fileName);
		
		
	}


	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}
}
