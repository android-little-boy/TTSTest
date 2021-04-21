package com.androidlittleboy.ttstest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText et_text;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FileUtils.getInstance().pushFont(this,"");
        init();

    }

    private void init() {
        et_text = (EditText) findViewById(R.id.editText);
        //创建tts对象
        tts = new TextToSpeech(this, this);
    }


    public void play(View view) {
//        String str = et_text.getText().toString().trim();
        String str = "华微软件";
        if (!TextUtils.isEmpty(str)) {
            // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            tts.setPitch(1.0f);
            // 设置语速
            tts.setSpeechRate(1.0f);
            //播放语音
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
        //判断tts回调是否成功
        if (status == TextToSpeech.SUCCESS) {
            int result1 = tts.setLanguage(Locale.US);
            int result2 = tts.setLanguage(Locale.CHINESE);
            if (result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED
                    || result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
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