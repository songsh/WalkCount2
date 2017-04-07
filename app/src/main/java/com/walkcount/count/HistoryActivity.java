package com.walkcount.count;

import java.util.List;

import com.walkcount.R;
import com.walkcount.db.WalkCount;
import com.walkcount.view.BarChart03View;

import android.app.Activity;
import android.os.Bundle;

public class HistoryActivity extends Activity{
	
	
	BarChart03View lineData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		initView();
		initData();
	}

	private void initView() {
		lineData = (BarChart03View)findViewById(R.id.line_data);
		
	}

	private void initData() {
		List<WalkCount> datas = WalkCount.findWithQuery(WalkCount.class, "select * from walk_count order by date desc limit ?", "7");
		lineData.setData(datas);
	}

}
