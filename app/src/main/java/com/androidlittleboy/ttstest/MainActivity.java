package com.androidlittleboy.ttstest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText et_text;
    private TextToSpeech tts;
    private boolean isInstallingEngine = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.REQUEST_INSTALL_PACKAGES)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 1);
            }
        }
        if (AppUtils.getInstance().checkDeamonApk(this)) {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (AppUtils.getInstance().checkDeamonApk(this)) {
                    init();
                }
            }else {
                Log.d("TAG", "onRequestPermissionsResult: ");
//                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 666) {
            if (resultCode == RESULT_OK) {
                init();
            } else {
                Log.d("TAG", "onActivityResult: ");
                finish();
            }
        }
    }

    private void init() {
        et_text = (EditText) findViewById(R.id.editText);
        //??????tts??????
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!AppUtils.getInstance().isAppExist(MainActivity.this, "com.iflytek.speechcloud")) {
                    isInstallingEngine = true;
                }
                isInstallingEngine = false;
                tts = new TextToSpeech(MainActivity.this, MainActivity.this, "com.iflytek.speechcloud");
            }
        }).start();
    }


    public void play(View view) {
        if (isInstallingEngine) {
            Toast.makeText(this, "????????????????????????????????????", Toast.LENGTH_SHORT).show();
            return;
        }
        String str = et_text.getText().toString().trim();
        if (!TextUtils.isEmpty(str)) {
            // ???????????????????????????????????????????????????????????????????????????,1.0?????????
            tts.setPitch(1.0f);
            // ????????????
            tts.setSpeechRate(1.0f);
            //????????????
            tts.speak(str, TextToSpeech.QUEUE_ADD, null, null);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {

                }

                @Override
                public void onDone(String utteranceId) {

                }

                @Override
                public void onError(String utteranceId) {

                }
            });
        }
    }

    @Override
    public void onInit(int status) {
        //??????tts??????????????????
        if (status == TextToSpeech.SUCCESS) {
            int result1 = tts.setLanguage(Locale.US);
            int result2 = tts.setLanguage(Locale.CHINESE);
            if (result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED
                    || result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}