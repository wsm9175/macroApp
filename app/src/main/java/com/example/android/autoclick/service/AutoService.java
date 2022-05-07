package com.example.android.autoclick.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.android.autoclick.view.FloatingView;

public class AutoService extends AccessibilityService {
    private final String TAG = AutoService.class.getSimpleName();
    private boolean isOn;
    private Handler mHandler;
    private int mX;
    private int mY;
    private FloatingView floatingView;
    private static int count =0;

    @Override
    public void onCreate() {
//        super.onCreate();
        Log.d("AutoService", "onCreate");
        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    /*버튼 클릭시 FloatingView switch ON/OFF-> service intent -> autoService에서 스위치 ON/OFF 설정/
      service의 onAccessibilityEvent가 view의 이벤트를 계속 감지하고 있는데 스위치가 ON이고
      view touch라면 지정된 좌표 터치 실행*/
    //touch를 감지하는 메서드
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent");
        Log.d(TAG, String.valueOf(event.getEventType()));
        Log.d(TAG, "event.getAction() :" + event.getAction());
        Log.d(TAG, "ccessibilityEvent.TYPE_VIEW_CLICKED : " + AccessibilityEvent.TYPE_VIEW_CLICKED);
        Log.d(TAG, "isOn : " + isOn);
        // view click이 감지 되었다면
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED && isOn) {
//        if (isOn) {
            Log.d(TAG, "실행완료");
            if (mRunnable == null) {
                mRunnable = new IntervalRunnable();
            }
//                playTap(mX,mY);
//                mHandler.postDelayed(mRunnable, 1000);
            //터치 실행
            count++;
            mHandler.post(mRunnable);
        }else if(event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED|| event.getEventType() ==  AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ) {
            Log.d(TAG, event.toString());
        }
    }


    @Override
    public void onInterrupt() {
    }


    @Override
    protected void onServiceConnected() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","SERVICE STARTED");

        Log.d(TAG, "onStartCommand");
        if (intent != null) {
            this.isOn = intent.getBooleanExtra("switch", false);
            if (isOn) {
                Log.d("Service", "onStartCommand On");
                this.mX = intent.getIntExtra("x", 0);
                this.mY = intent.getIntExtra("y", 0);

            } else {
                this.count=intent.getIntExtra("count", 0);
                Log.d("Service", "onStartCommand OFF");
                mHandler.removeCallbacksAndMessages(null);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    private void playTap(int x, int y) {
        //Log.d("TAPPED","STARTED TAPpING");
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        swipePath.lineTo(x, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
//        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 10));
        //dispatchGesture(gestureBuilder.build(), null, null);
        //Log.d("hello","hello?");

        boolean flag = dispatchGesture(gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 10)).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d("Gesture Completed", "Gesture Completed");
                super.onCompleted(gestureDescription);
                //mHandler.postDelayed(mRunnable, 1);
                Log.d(TAG, "gesture complete");
//                mHandler.post(mRunnable);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                //Log.d("Gesture Cancelled","Gesture Cancelled");
                super.onCancelled(gestureDescription);
                Log.d(TAG, "gesture canceled");
            }
        }, null);
        Log.d("FLAG", String.valueOf(flag));
    }


    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            Log.d("clicked", "click");
            try {
                if(count==1){
                    Thread.sleep(350);
                    playTap(mX, mY);
                }
                else{
                    count=0;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}