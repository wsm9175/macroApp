package com.example.android.autoclick;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.android.autoclick.model.User;
import com.example.android.autoclick.view.FloatingView;
import com.example.android.autoclick.view.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class DateReceiver extends BroadcastReceiver {
    private final String TAG = DateReceiver.class.getSimpleName();
    private final String REF = "https://quickjob-c9ee7-default-rtdb.asia-southeast1.firebasedatabase.app";
    private final String USER = "user";
    private final String APPVERSION = "appversion";
    private String myVersionName;
    private int myVersionCode;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private ConnectivityManager connectivityManager;
    private static int interval = 0;
    private Context mContext;
    private Activity mActivity;

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "broadcast onReceive");
        mContext = context;
        connectivityManager = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);

        if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)) {
            Log.d(TAG, "Intent.ACTION_DATE_CHANGED");
            //남은 기간 계산
            getRemainingDate();
            //버전 비교
            compareVersion();
        } else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
            //서버에 true 전송
            Toast.makeText(mContext, "사용자 시스템 임의 변경 감지 앱 종료", Toast.LENGTH_LONG).show();
            Log.d(TAG, "시간 변경 감지");
            insertValue();
        } else if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            int status = NetworkUtil.getConnectivityStatus(mContext);
            if(status == 0){
                Toast.makeText(mContext, "네트워크 연결 후 앱을 재시작 해주세요.",Toast.LENGTH_LONG).show();
                shutDown();
            }
        }
    }

    // 결제 남은 날짜를 계산
    private void getRemainingDate() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance(REF).getReference(USER);
        Log.d(TAG, mDatabase + " " + user.getUid());
        mDatabase.child(user.getUid()).get().addOnCompleteListener(task -> {
            Log.d(TAG, String.valueOf(task.isSuccessful()));
            if (task.isSuccessful()) {
                Log.d(TAG, "task isSuccessful");
                Log.d(TAG, String.valueOf(task.getResult().getValue()));
                User mUser = task.getResult().getValue(User.class);
                String mday = mUser.getDate();
                String[] mdaylist = mday.split("-");
                int year = Integer.parseInt(mdaylist[0]);
                int month = Integer.parseInt(mdaylist[1]);
                int day = Integer.parseInt(mdaylist[2]);
                GregorianCalendar cal = new GregorianCalendar();
                long currentTime = cal.getTimeInMillis() / (1000 * 60 * 60 * 24);
                cal.set(year, month - 1, day);
                long birthTime = cal.getTimeInMillis() / (1000 * 60 * 60 * 24);
                interval = (int) (birthTime - currentTime);
                Log.d(TAG, "다음 결제일까지 : D-" + String.valueOf(interval));
                judgment(interval);
            } else {
                Log.d(TAG, "task failed");
                task.getException().printStackTrace();
            }
        });
    }

    private void compareVersion(){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance(REF).getReference(APPVERSION);
        mDatabase.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String latestVersion = (String) task.getResult().getValue();
                Log.d(TAG, "latestVersion : " + latestVersion);
                if(!latestVersion.equals(myVersionName)){
                    Toast.makeText(mContext, "앱을 최신버전으로 업데이트 해주세요",Toast.LENGTH_SHORT).show();
                    shutDown();
                }else{
                    getRemainingDate();
                }
            }else{
                Toast.makeText(mContext, "server connection error",Toast.LENGTH_SHORT).show();
                shutDown();
            }
        });
    }

    private void judgment(int dDay) {
        if (dDay < 0) {
            Log.d(TAG, "다음 결제일까지 : D-" + String.valueOf(interval));
            Toast.makeText(mContext, "이용기간이 만료되어 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
            shutDown();
        } else {
            Toast.makeText(mContext, "다음 결제일까지 : D-" + String.valueOf(interval), Toast.LENGTH_SHORT).show();
        }
    }

    private void insertValue() {
        Log.d(TAG, "시간 변경 감지");
        DatabaseReference mDatabase = FirebaseDatabase.getInstance(REF).getReference(USER);
        Map<String, Object> map = new HashMap<>();
        map.put("limit", true);
        mDatabase.child(user.getUid()).updateChildren(map).addOnCompleteListener(task -> {
            Log.d(TAG, "limit change");
            shutDown();
        }).addOnFailureListener(e -> shutDown());
    }

    private void shutDown() {
        mActivity.moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
        mActivity.finishAndRemoveTask();                        // 액티비티 종료 + 태스크 리스트에서 지우기
        mActivity.stopService(new Intent(mActivity, FloatingView.class));
        android.os.Process.killProcess(android.os.Process.myPid());

    }
}