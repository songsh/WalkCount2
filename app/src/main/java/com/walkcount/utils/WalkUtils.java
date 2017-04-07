package com.walkcount.utils;


import java.util.Timer;
import java.util.TimerTask;

import com.walkcount.count.CountService;
import com.walkcount.count.MainActivity;
import com.walkcount.utils.WalkUtils.Callback;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.LinearGradient;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class WalkUtils {
	
	private static WalkUtils walkUtils;
	private Context context;
	private int fromNumber;
	private SensorManager mSensorManager;
	private SensorEventListener mSensorListener;
	private Sensor mSensor;
	protected long lastUpdateTime;
	protected int i_zaxis = 300;
	protected int zaxisIndex;
	protected float[] zaxis;
	private SensorType walkType;
	private int mRate = 200 * 1000;

	private Dector stepDector;
	public WalkUtils(Context context){
		this.context = context;
		initSensor();
	}
	
	public static WalkUtils getInstance(Context context){
		if(walkUtils == null){
			walkUtils = new WalkUtils(context);
		}
		return walkUtils;
	}
	
	
	private void initSensor(){
		if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
			walkType = SensorType.Linear;
			initLinearSensor2();
		}
		else if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)){
			walkType = SensorType.step;
			initStepSensor();
		}else
		{
			walkType = SensorType.jiao;
			initGyroSensor();
		}
	}
	private void initStepSensor(){
		
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
		stepDector = new SStepDector();
		mSensorManager.registerListener((SensorEventListener)stepDector, mSensor, mRate);
		stepDector.start();
		
	}
	private void initGyroSensor(){
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		GyroDector stepDector = new GyroDector();
		mSensorManager.registerListener((SensorEventListener)stepDector, mSensor, 2);
		stepDector.start();
	}
	private void initLinearSensor() {
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		stepDector = new ZeroStepDector();
		mSensorManager.registerListener((SensorEventListener)stepDector, mSensor, mRate);
		stepDector.start();

	}
	private void initLinearSensor2() {
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		stepDector = new StepDector();
		mSensorManager.registerListener((SensorEventListener)stepDector, mSensor, SensorManager.SENSOR_DELAY_UI);
//		stepDector.start();
		
	}
	
	public boolean isReady() {
		return (mSensor == null) ? false : true;
	}

	public void start() {
		if (isReady()) {
			
		} else {
			Toast.makeText(context, "Orientation sensor is not found.",10).show();
		}
	}



	public void stop() {
		if (isReady()) {
			mSensorManager.unregisterListener(mSensorListener);
		}
		if(walkType == SensorType.Linear){
			stepDector.stop();
		}
			
		Intent intent = new Intent(context,CountService.class);
		intent.putExtra("flag", 1);
		context.startService(intent);
	}
	
	public interface Callback{
		public void walkCount(int count);
		public void fail(String message);
	}
	

	
	enum SensorType{
		Linear(0),jiao(1),step(2);
		int type;
		private  SensorType(int type){
			this.type = type;
		}
	}
	


}


