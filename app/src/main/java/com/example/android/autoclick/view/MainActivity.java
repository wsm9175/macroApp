package com.example.android.autoclick.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autoclick.DateReceiver;
import com.example.android.autoclick.NetworkUtil;
import com.example.android.autoclick.R;
import com.example.android.autoclick.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //FrameLayout mLayout;
    private final String TAG = MainActivity.class.getSimpleName();
    private DateReceiver dateReceiver;

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    private static int interval = 0;
    private TextView txt_date;
    private TextView txt_id;
    private Button startButton;
    private ProgressBar progressBar;

    private PackageInfo pInfo = null;
    private String myVersionName;
    private int myVersionCode;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private final String REF = "https://quickjob-c9ee7-default-rtdb.asia-southeast1.firebasedatabase.app";
    private final String APPVERSION = "appversion";
    private final String USER = "user";
    private final String DATE = "date";
    private final String LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String idByANDROID_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        dateReceiver = new DateReceiver();
        dateReceiver.setActivity(this);

        txt_date = findViewById(R.id.txt_date);
        txt_id = findViewById(R.id.txt_id);
        startButton = findViewById(R.id.startFloat);

        startButton.setOnClickListener(this);

        this.isLoading.setValue(false);
        this.progressBar = findViewById(R.id.progress1);
        this.progressBar.bringToFront();
        this.startButton.setVisibility(View.INVISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);

        this.mAuth = FirebaseAuth.getInstance();
        this.user = mAuth.getCurrentUser();

        txt_id.setText("접속 계정 : " + this.user.getEmail());

        this.isLoading.observe(this, data -> {
            if (isLoading.getValue()) {
                this.progressBar.setVisibility(View.VISIBLE);
                this.startButton.setVisibility(View.INVISIBLE);
            }else{
                this.progressBar.setVisibility(View.INVISIBLE);
                this.startButton.setVisibility(View.VISIBLE);
            }
        });

        Log.d(TAG, String.valueOf(NetworkUtil.getConnectivityStatus(getApplicationContext())));
        if(NetworkUtil.getConnectivityStatus(getApplicationContext()) == 0){
            Toast.makeText(this, "네트워크 연결 후 앱을 재시작 해주세요.",Toast.LENGTH_LONG).show();
        }

        // 패키지 정보를 받아와 서버의 앱 버전과 비교. 버전 불일치시 업데이트 요청 후 종료
        try {
            pInfo = getApplication().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
              myVersionCode = (int) pInfo.getLongVersionCode();
            }
            myVersionName = pInfo.versionName;

            Log.d(TAG, "myVersionCode : " + myVersionCode);
            Log.d(TAG, "myVersionName : " + myVersionName);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "앱의 버전 정보를 얻어오지 못했습니다.",Toast.LENGTH_SHORT).show();
            shutDown();
        }

        compareVersion(idByANDROID_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "이 앱을 이용하기 위해선 다른 앱 위에 표시 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            askPermission();
        }
        if (!checkAccessibilityPermissions()) {
            setAccessibilityPermissions();
            Toast.makeText(this, "이 앱을 이용하기 위해선 접근성 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(this.dateReceiver, filter);
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    // 접근성 권한이 있는지 없는지 확인하는 부분
    // 있으면 true, 없으면 false
    private boolean checkAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);

        for (int i = 0; i < list.size(); i++) {
            AccessibilityServiceInfo info = list.get(i);

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            if (info.getResolveInfo().serviceInfo.packageName.equals(getApplication().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    // 접근성 설정화면으로 넘겨주는 부분
    private void setAccessibilityPermissions() {
        AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
        gsDialog.setTitle("접근성 권한 설정");
        gsDialog.setMessage("접근성 권한을 필요로 합니다");
        gsDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 설정화면으로 보내는 부분
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return;
            }
        }).create().show();
    }

    private void compareVersion(String android){
        isLoading.setValue(true);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance(REF).getReference(APPVERSION);
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    String latestVersion = (String) task.getResult().getValue();
                    Log.d(TAG, "latestVersion : " + latestVersion);
                    if(!latestVersion.equals(myVersionName)){
                        Toast.makeText(getApplicationContext(), "앱을 최신버전으로 업데이트 해주세요",Toast.LENGTH_SHORT).show();
                        shutDown();
                    }else{
                        getRemainingDate(android);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "server connection error",Toast.LENGTH_SHORT).show();
                    shutDown();
                }
            }
        });
    }

    // 결제 남은 날짜를 계산
    private void getRemainingDate(String android) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance(REF).getReference(USER);
        Log.d(TAG, mDatabase + " " + user.getUid());
        mDatabase.child(user.getUid()).get().addOnCompleteListener(task -> {
            Log.d(TAG, String.valueOf(task.isSuccessful()));
            if (task.isSuccessful()) {
                Log.d(TAG, "task isSuccessful");
                Log.d(TAG, String.valueOf(task.getResult().getValue()));
                User mUser = task.getResult().getValue(User.class);
                String mday = mUser.getDate();
                String mandroid = mUser.getAndroid();
                boolean isLimit = mUser.isLimit();
                if(isLimit){
                    Toast.makeText(getApplicationContext(), "부정 이용으로 이용이 제한된 계정입니다.",Toast.LENGTH_SHORT).show();
                    shutDown();
                }

                if(mandroid.equals("")){
                    mDatabase.child(user.getUid()).child("android").setValue(android);
                }else{
                    Toast.makeText(getApplicationContext(), "중복로그인으로 이용을 제한합니다.",Toast.LENGTH_SHORT).show();
                    shutDown();
                }

                String[] mdaylist = mday.split("-");
                int year = Integer.parseInt(mdaylist[0]);
                int month = Integer.parseInt(mdaylist[1]);
                int day = Integer.parseInt(mdaylist[2]);
                GregorianCalendar cal = new GregorianCalendar();
                long currentTime = cal.getTimeInMillis() / (1000 * 60 * 60 * 24);
                cal.set(year, month - 1, day);
                long birthTime = cal.getTimeInMillis() / (1000 * 60 * 60 * 24);
                interval = (int) (birthTime - currentTime);
                if(interval < 0){
                    startButton.setEnabled(false);
                }
                txt_date.setText("다음 결제일까지 : D-" + String.valueOf(interval));
                this.isLoading.setValue(false);
            } else {
                Log.d(TAG, "task failed");
                task.getException().printStackTrace();
                this.isLoading.setValue(false);
            }
        });
    }

    // 접근성 및 다른 앱 위에 표시 권한을 체크. 모든 권한이 부여돼있다면 flattingView display
    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && checkAccessibilityPermissions()) {
            Log.d(TAG, "condition 1");
            startService(new Intent(MainActivity.this, FloatingView.class));
            finish();
        } else if (Settings.canDrawOverlays(this) && checkAccessibilityPermissions()) {
            Log.d(TAG, "condition 2");
            startService(new Intent(MainActivity.this, FloatingView.class));
            finish();
        } else {
            Log.d(TAG, "no permission");
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "askPermission");
                Toast.makeText(this, "이 앱을 이용하기 위해선 다른 앱 위에 표시 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                askPermission();
            } else {
                Log.d(TAG, "accessibility permission");
                Toast.makeText(this, "이 앱을 이용하기 위해선 접근성 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                setAccessibilityPermissions();
            }
        }
    }

    private void shutDown(){
        moveTaskToBack(true);						// 태스크를 백그라운드로 이동
        finishAndRemoveTask();						// 액티비티 종료 + 태스크 리스트에서 지우기
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
