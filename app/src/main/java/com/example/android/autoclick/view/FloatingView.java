package com.example.android.autoclick.view;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.android.autoclick.DateReceiver;
import com.example.android.autoclick.R;
import com.example.android.autoclick.service.AutoService;
//import com.example.android.autoclick.service.AutoService;

public class FloatingView extends Service implements View.OnClickListener {
    private final String TAG = FloatingView.class.getSimpleName();
    private WindowManager mWindowManager;
    private View myFloatingView;
    private Button exitButton;
    private Switch aSwitch;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        //        super.onCreate();
        //
        //
        //        //getting the widget layout from xml using layout inflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

        int layout_parms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_parms,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);


        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);


        //adding an touchlistener to make drag movement of the floating widget
        myFloatingView.findViewById(R.id.thisIsAnID).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TOUCH", "THIS IS TOUCHED");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
//                        Log.d("coordinates", "X : " + initialTouchX + " Y : " + initialTouchY);
//                        getCoordinates();
                        return true;

                    case MotionEvent.ACTION_UP:

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(myFloatingView, params);
                        return true;
                }
                return false;
            }
        });

//        startButton = (Button) myFloatingView.findViewById(R.id.start);
//        startButton.setOnClickListener(this);
//        stopButton = (Button) myFloatingView.findViewById(R.id.stop);
//        stopButton.setOnClickListener(this);

        exitButton = myFloatingView.findViewById(R.id.btn_exit);
        exitButton.setOnClickListener(this);
        aSwitch = myFloatingView.findViewById(R.id.onoff);

          /*버튼 클릭시 FloatingView switch ON/OFF-> service intent -> autoService에서 스위치 ON/OFF 설정/
      service의 onAccessibilityEvent가 view의 이벤트를 계속 감지하고 있는데 스위치가 ON이고
      view touch라면 지정된 좌표 터치 실행*/
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Intent intent = new Intent(getApplicationContext(), AutoService.class);
                if (isChecked) {
                    Log.d("START", "THIS IS STARTED");
                    Point size = getCoordinates();
                    intent.putExtra("switch", true);
                    intent.putExtra("count",1);
                    //터치 좌표

                    intent.putExtra("x", size.x / 2);
                    intent.putExtra("y", size.y - 30);

                } else {
                    Log.d("START", "THIS IS STOP");
                    intent.putExtra("count",0);
                    intent.putExtra("switch", false);
                }
                getApplication().startService(intent);
            }
        });

//        aSwitch.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//
//            }
//        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myFloatingView != null) mWindowManager.removeView(myFloatingView);
    }

    @Override
    public void onClick(View v) {
        stopSelf();
        System.exit(0);
    }

    //화면 해상도를 통해 좌표값을 구해옴
    public Point getCoordinates() {
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Log.d("FloatingView", ">>> size.x : " + size.x + ", size.y : " + size.y);
        return size;
    }

}