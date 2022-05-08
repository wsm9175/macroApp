package com.example.android.autoclick.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.example.android.autoclick.R;
import com.example.android.autoclick.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //FrameLayout mLayout;
    private final String TAG = MainActivity.class.getSimpleName();
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;

    private final String DATE = "date";
    private final String LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "이 앱을 이용하기 위해선 다른 앱 위에 표시 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            askPermission();
        }
        if (!checkAccessibilityPermissions()) {
            setAccessibilityPermissions();
            Toast.makeText(this, "이 앱을 이용하기 위해선 접근성 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
        findViewById(R.id.startFloat).setOnClickListener(this);

        getRemainingDate();
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

    // 결제 남은 날짜를 계산
    private void getRemainingDate() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, user.getUid());
        mDatabase.child("UserDate").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "task isSuccessful");
                    Log.d(TAG, String.valueOf(task.getResult().getValue()));
                    User mUser = task.getResult().getValue(User.class);
                }else{
                    Log.d(TAG, "task failed");
                    task.getException().printStackTrace();
                }
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
}
