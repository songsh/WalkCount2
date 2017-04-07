package com.walkcount.utils;

import java.util.Timer;

import org.greenrobot.eventbus.EventBus;

import com.walkcount.bean.CountBean;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.CountDownTimer;
import android.util.Log;

public class StepDector implements SensorEventListener,Dector {
    private final String TAG = "StepDcretor";
    // 存放三轴数据
    final int valueNum = 3;
    // 用于存放计算阈值的波峰波谷差值
    float[] tempValue = new float[valueNum];
    int tempCount = 0;
    // 是否上升的标志位
    boolean isDirectionUp = false;
    // 持续上升次数
    int continueUpCount = 0;
    // 上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;
    // 上一点的状态，上升还是下降
    boolean lastStatus = false;
    // 波峰值
    float peakOfWave = 0;
    // 波谷值
    float valleyOfWave = 0;
    // 此次波峰的时间
    long timeOfThisPeak = 0;
    // 上次波峰的时间
    long timeOfLastPeak = 0;
    // 当前的时间
    long timeOfNow = 0;
    // 上次传感器的值
    float gravityOld = 0;
    // 动态阈值需要动态的数据,这个值用于这些动态数据的阈值
    final float initialValue = (float) 2.0;
    // 初始阈值
    float ThreadValue = (float) 2.0;
    // 初始范围
    float minValue = 8f;
    float maxValue = 20.6f;
    // 0-准备计时,1-计时中,2-正常计步中
    private int CountTimeState = 0;
    public static int CURRENT_STEP = 0;
    public static int TEMP_STEP = 0;
    private int lastStep = -1;
    // 用x、y、z轴三个维度算出的平均值
    public static float average = 0;
    // 计时器
    private Timer timer;
    // 倒计时2秒，2秒内不会显示计步，用于屏蔽细微波动
    private long duration = 3000;
    // 倒计时器
    private TimeCount time;
    // 传感器变化监听
    private String filename = System.currentTimeMillis()+".log";

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        synchronized (this) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calc_step(event);
            }
        }
    }

    synchronized private void calc_step(SensorEvent event) {
        average = (float) Math.sqrt(Math.pow(event.values[0], 2)
                + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        detectorNewStep(average);
    }

    /**
     * 检测步子，并开始计步
     * 传入sersor中的数据
     * 如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
     * 符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
     * @param values
     * @return
     */
    public void detectorNewStep(float values) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
//            LogUtils.file(values+" ",filename);
            if (DetectorPeak(values, gravityOld)) {
                //主要记录波峰及发生时间
                LogUtils.file(peakOfWave +" " +valleyOfWave + " "+(System.currentTimeMillis() - timeOfNow),filename);
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();

                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= ThreadValue) && (timeOfNow - timeOfLastPeak) <= 2000) {
                    timeOfThisPeak = timeOfNow;
                    // 更新UI
                    preStep();
                }
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
    }

    /**
     * 更新UI
     * @param
     * @return
     */
    private void preStep() {
        if (CountTimeState == 0) {
            // 开启计时器
            time = new TimeCount(duration, 800);
            time.start();
            CountTimeState = 1;
            Log.v(TAG, "开启计时器");
        } else if (CountTimeState == 1) {
            TEMP_STEP++;
            Log.v(TAG, "计步中 TEMP_STEP:" + TEMP_STEP);
        } else if (CountTimeState == 2) {
            CURRENT_STEP++;
            EventBus.getDefault().post(new CountBean(1));
        }
    }

    /**
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 目前点为下降的趋势：isDirectionUp为false
     * 之前的点为上升的趋势：lastStatus为true
     * 到波峰为止，持续上升大于等于2次
     * 波峰值大于1.2g,小于2g
     * 记录波谷值
     * 观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 所以要记录每次的波谷值，为了和下次的波峰做对比
     * @param newValue
     * @param oldValue
     * @return
     */
    public boolean DetectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        Log.v(TAG, "oldValue:" + oldValue);
        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue))) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

    /**
     * 阈值的计算
     * 通过波峰波谷的差值计算阈值
     * 记录4个值，存入tempValue[]数组中
     * 在将数组传入函数averageValue中计算阈值
     * @param value
     * @return
     *
     * 目前记录4个值
     */
    public float Peak_Valley_Thread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;
    }

    /**
     * 梯度化阈值
     * 计算数组的均值
     * 通过均值将阈值梯度化在一个范围里
     * @author leibing
     * @createTime 2016/08/31
     * @lastModify 2016/08/31
     * @param value
     * @param n
     * @return
     */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;
        if (ave >= 8) {
            Log.v(TAG, "超过8");
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
            Log.v(TAG, "7-8");
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
            Log.v(TAG, "4-7");
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
            Log.v(TAG, "3-4");
            ave = (float) 2.0;
        } else {
            Log.v(TAG, "else");
            ave =  initialValue;
        }
        return 2;
    }

    /**
     * @className: TimeCount
     * @classDescription:倒计时器（用于计步）
     * @author: leibing
     * @createTime: 2016/09/01
     */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            time.cancel();
            CURRENT_STEP += TEMP_STEP;
            lastStep = -1;
            Log.v(TAG, "计时正常结束");
            EventBus.getDefault().post(new CountBean(TEMP_STEP));

//            timer = new Timer(true);
//            TimerTask task = new TimerTask() {
//                public void run() {
//                    if (lastStep == CURRENT_STEP) {
//                        timer.cancel();
//                        CountTimeState = 0;
//                        lastStep = -1;
//                        TEMP_STEP = 0;
//                        Log.v(TAG, "停止计步：" + CURRENT_STEP);
//                    } else {
//                        lastStep = CURRENT_STEP;
//                    }
//                }
//            };
//            timer.schedule(task, 0, 1000);
            CountTimeState = 2;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (lastStep == TEMP_STEP) {
                Log.v(TAG, "onTick 计时停止");
                time.cancel();
                CountTimeState = 0;
                lastStep = -1;
                TEMP_STEP = 0;
            } else {
                lastStep = TEMP_STEP;
            }
        }
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
