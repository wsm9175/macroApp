package com.example.android.autoclick.view;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.android.autoclick.DateReceiver;
import com.example.android.autoclick.R;
import com.example.android.autoclick.databinding.ActivityLoginBinding;
import com.example.android.autoclick.util.Util;
import com.example.android.autoclick.viewmodel.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = LoginActivity.class.getSimpleName();
    private LoginViewModel mLoginViewModel;
    private ActivityLoginBinding activityLoginBinding;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // view model 사용을 위한 초기화 작업
        mLoginViewModel = new ViewModelProvider(this, new ViewModelProvider
                .AndroidViewModelFactory(getApplication())).get(LoginViewModel.class);
        mLoginViewModel.setParentContext(this);

        // 자동 로그인 검사
        if(checkLogin()){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        mLoginViewModel.setmEmailLoginExcutor();
        mLoginViewModel.setActionListener();

        //이메일 로그인 버튼 클릭 시
        activityLoginBinding.btnSignInEmail.setOnClickListener(view -> {
            Log.d(TAG, "Email Login Request");

            // email과 password를 받아올때 앞뒤 공백을 제거하고 변수에 값을 삽입한다.
            String email = activityLoginBinding.edtId.getText().toString().trim();
            String password = activityLoginBinding.edtPassword.getText().toString().trim();

            try {
                //keyBoard가 올라와 있는 경우 키보드를 내림
                Util.keyboardOff(LoginActivity.this);
            }catch (Exception e){
                Log.d(TAG, "keyBoard state : off");
            }

            //조건 검사 후 로그인 실행
            checkEmailPassword(email, password);
        });
    }

    private boolean checkLogin(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        boolean check = false;
        this.user = mAuth.getCurrentUser();
        if (user != null) {
            check = true;
        }
        Log.d(TAG, String.valueOf(check));
        return check;
    }

    private void checkEmailPassword(String email, String password) {
        if (email.equals("")) {
            Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
        } else if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
        } else {
            //로그인 실행
            mLoginViewModel.onRequestSignInWithEmail(email, password);
        }
    }

}