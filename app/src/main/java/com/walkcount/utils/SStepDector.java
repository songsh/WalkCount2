package com.walkcount.utils;

import org.greenrobot.eventbus.EventBus;

import com.walkcount.bean.CountBean;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class SStepDector implements SensorEventListener,Dector{

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_STEP_DETECTOR)
			return;
		
		Log.i("tag", event.values[0]+"");
		int fromNumber = (int)event.values[0];
		EventBus.getDefault().post(new CountBean(fromNumber));
//		callback.walkCount(fromNumber);
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
