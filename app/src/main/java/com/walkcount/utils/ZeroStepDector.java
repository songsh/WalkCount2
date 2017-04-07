package com.walkcount.utils;

import java.util.Timer;
import java.util.TimerTask;

import org.greenrobot.eventbus.EventBus;

import com.walkcount.bean.CountBean;
import com.walkcount.utils.WalkUtils.SensorType;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ZeroStepDector implements SensorEventListener,Dector{

	private int i_zaxis = 150;
	private int zaxisIndex;
	private float[] zaxis = new float[150];
	private long lastUpdateTime;
	private Timer t;
	private TimerTask t1;

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		try {

			if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION)
				return;
			long curTime = System.currentTimeMillis();
				if (zaxisIndex > i_zaxis ) {
					zaxisIndex = 0;
				}
				Log.i("axis", zaxisIndex+" "+event.values[SensorManager.DATA_Z]+ " "+(curTime - lastUpdateTime));
				zaxis[zaxisIndex++] = event.values[SensorManager.DATA_Z];
				lastUpdateTime = curTime;
		} catch (Exception e) {
			Log.e("e", e.getMessage());
		}
		
	}
	
	public void stop(){
		if(t !=null){
			t.cancel();
			t.purge();
			t = null;
		}
	}
	class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			int footCount = 0;
			for (int i = 0; i < zaxis.length - 2; i++) {
				if (Math.abs(zaxis[i]) < 0.25)
					continue;
				if ((zaxis[i] >= 0 && zaxis[i + 1] < 0)
						|| (zaxis[i] > 0 && zaxis[i + 1] <= 0)) {
					footCount++;
				}
			}
			EventBus.getDefault().post(new CountBean(footCount));
			resetData();
		}
	};
	
	private void resetData() {
		zaxis = new float[i_zaxis];
		zaxisIndex = 0;
		Log.i("reset", zaxisIndex+"");
	}

	@Override
	public void start() {
		if(t==null){
			t = new Timer(true);
		}
		if(t1!=null){
			t1.cancel();
			t1 = null;
		}
		t1 = new MyTimerTask();
		t.scheduleAtFixedRate(t1, 1, 1000 * 30);
		
	}

}
