package com.walkcount.count;

import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.orm.Database;
import com.walkcount.utils.CrashHandler;

import android.app.Application;
import android.content.Context;
import android.os.Binder;
import android.support.multidex.MultiDex;
import android.widget.Toast;


public class WalkApplication extends com.orm.SugarApp{
	
	
	public static Database db;
	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        
        db = getDatabase();
		ANRWatchDog watchDog = new ANRWatchDog();
		watchDog.setANRListener(new ANRWatchDog.ANRListener() {
			@Override
			public void onAppNotResponding(ANRError error) {
				Toast.makeText(WalkApplication.this,error.getMessage(),Toast.LENGTH_SHORT).show();
			}
		}).start();
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

}
