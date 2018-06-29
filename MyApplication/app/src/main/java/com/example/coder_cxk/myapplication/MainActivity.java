package com.example.coder_cxk.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.baidu.mapapi.SDKInitializer;


public class MainActivity extends Activity {

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SDKInitializer.initialize(getApplicationContext());
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext

        setContentView(R.layout.activity_main);
        // 设置首页背景透明度，0-255是透明度的值
        View v = findViewById(R.id.main);
		v.getBackground().setAlpha(100);
    }

    /*
    首页按钮的跳转。一个跳转到花卉识别界面，另一个跳转到花卉百科界面
    我认为此函数可以重复使用
    */
    public void jump2identify(View view){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, identify.class);
         MainActivity.this.startActivity(intent);
    }
    
    public void jump2baike(View view){
        Intent intent = new Intent();

        intent.setClass(MainActivity.this, baike.class);
        MainActivity.this.startActivity(intent);
    }

    // 跳转到每日猜花界面
    public void jump2game(View view){
        Intent intent = new Intent();

        intent.setClass(MainActivity.this, guessflower.class);
        MainActivity.this.startActivity(intent);
    }
}
