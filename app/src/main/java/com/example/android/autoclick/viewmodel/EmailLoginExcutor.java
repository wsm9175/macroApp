package com.example.android.autoclick.viewmodel;

import android.util.Log;

import com.example.android.autoclick.callback.ActionListener;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class EmailLoginExcutor {
    private final String TAG = EmailLoginExcutor.class.getSimpleName();
    private FirebaseUser mUser;
    private ActionListener actionListener;

    private FirebaseAuth mAuth;

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public EmailLoginExcutor(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void signInWithEmail(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()){
                       Log.d(TAG, "email login success");
                       try{
                            task.getResult(ApiException.class);
                            mUser = mAuth.getCurrentUser();
                            actionListener.NotifySignInEmailSuccess();
                       }catch (ApiException e){
                           e.printStackTrace();
                           return;
                       }
                   }else{
                       Log.d(TAG, "email login failed");
                   }
                });
    }
}
