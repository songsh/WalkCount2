package com.walkcount.count;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.walkcount.R;
import com.walkcount.bean.CountBean;
import com.walkcount.dao.WalkCountDao;
import com.walkcount.utils.DateUtils;
import com.walkcount.utils.WalkUtils;
import com.walkcount.view.TimelyTextView;

import java.lang.reflect.Proxy;

public class MainActivity extends Activity{

	private static final int SPEED_SHRESHOLD = 3000;
	public static final String EVENT_ONACCELERATE = "onAccelerometerChanged";

	private int footCount = 0;
	private final int i_zaxis = 1600;
	protected int fromNumber =0 ;
	private ToggleButton tb_start;
	private boolean bCheck = true;
	private TimelyTextView tv_status;
	public long lastUpdateTime;
	private WalkUtils walkUtils;
	WalkCountDao countDao = new WalkCountDao();
	public String nowDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music);
		if(savedInstanceState!=null){
			bCheck  = savedInstanceState.getBoolean("isCheck");
			fromNumber = savedInstanceState.getInt("count", 0);
		}
		walkUtils = new WalkUtils(MainActivity.this);
		
		initViews();
		initData();
		EventBus.getDefault().register(this);
	}


	private void initData() {
		nowDate = DateUtils.getNowDate();
		fromNumber = countDao.getCount(nowDate);
		tv_status.setText(fromNumber + "");
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(CountBean bean) {
		tv_status.start(fromNumber, fromNumber + bean.count);
		fromNumber = fromNumber + bean.count;
		countDao.save(nowDate, fromNumber);
		if(!CountService.isRunning){
			Intent intent = new Intent(MainActivity.this,CountService.class);
			intent.putExtra("flag", 0);
			intent.putExtra("count", fromNumber);
			startService(intent);
		}else{
			Intent intent = new Intent("android.intent.count");
			intent.putExtra("count", fromNumber);
			sendBroadcast(intent);
		}

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   super.onCreateOptionsMenu(menu);
	   menu.add(0, 0, 0,getString(R.string.menu1));
	   menu.add(0, 1, 0,getString(R.string.menu2));
	   return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:

			Intent intent = new Intent(MainActivity.this,DataActivity.class);
			startActivity(intent);
			break;
		case 1:
			intent = new Intent(MainActivity.this,HistoryActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private void initViews() {
		tv_status = (TimelyTextView) findViewById(R.id.tv_statue);

		tb_start = (ToggleButton) findViewById(R.id.tb_start);
		tb_start.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					walkUtils.start();
					tv_status.setVisibility(View.VISIBLE);
				} else {
					walkUtils.stop();
				}
			}
		});
		if(bCheck){
			tb_start.setChecked(true);
			tv_status.setText(fromNumber+"");
		}
	}

	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("isCheck", tb_start.isChecked());
		outState.putInt("count", fromNumber);
		super.onSaveInstanceState(outState);
	}
	
	protected void onDestory() {
		walkUtils.stop();
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
