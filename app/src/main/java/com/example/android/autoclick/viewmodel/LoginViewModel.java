package com.example.android.autoclick.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import com.example.android.autoclick.callback.ActionListener;
import com.example.android.autoclick.callback.ErrorListener;
import com.example.android.autoclick.view.MainActivity;

import java.lang.ref.WeakReference;

public class LoginViewModel extends ViewModel {
    private final String TAG = LoginViewModel.class.getSimpleName();
    private WeakReference<Activity> mActivityRef;

    //이메일 로그인 처리를 위한 변수
    private EmailLoginExcutor mEmailLoginExcutor;

    public LoginViewModel(){};


    // activity setting
    public void setParentContext(Activity parentContext){
        mActivityRef = new WeakReference<>(parentContext);
    }

    public ActionListener getActionListener(){
        return () -> {
            Intent intent = new Intent(mActivityRef.get(), MainActivity.class);
            mActivityRef.get().startActivity(intent);
            finishActivity();
        };
    }

    public ErrorListener getErrorListener(){
        return () -> Toast.makeText(mActivityRef.get(), "이메일, 비밀번호를 다시 확인해주세요", Toast.LENGTH_SHORT).show();
    }

    private void finishActivity() {
        if (mActivityRef.get() != null) {
            mActivityRef.get().finish();
        }
    }

     /*
                          이메일 로그인 메서드 모음
                                                                 */
    public void setmEmailLoginExcutor(){
        mEmailLoginExcutor = new EmailLoginExcutor();
    }

    public void setActionListener(){
        mEmailLoginExcutor.setActionListener(getActionListener());
    }

    //activity에서 이메일 로그인 버튼을 클릭한 경우
    public void onRequestSignInWithEmail(String email, String password){
        mEmailLoginExcutor.signInWithEmail(email, password);
    }
}
