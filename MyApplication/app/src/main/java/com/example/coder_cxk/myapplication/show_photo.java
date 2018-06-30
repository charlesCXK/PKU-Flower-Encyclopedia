package com.example.coder_cxk.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;

public class show_photo extends AppCompatActivity {
    private ImageView ivDynamicPicture;
    private TextView ivDynamicString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        ivDynamicPicture= (ImageView) findViewById(R.id.show_board);
        ivDynamicString = (TextView) findViewById(R.id.show_board_text);

        getPicture();
        getString();
    }

    private void getPicture() {
        //获取字符串
        SharedPreferences sPreferences = getSharedPreferences("Picture", Context.MODE_PRIVATE);
        String imageBase64 = sPreferences.getString("cameraImage", "");
        //把字符串解码成Bitmap对象
        byte[] byte64 = Base64.decode(imageBase64, 0);
        ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
        Bitmap bitmap = BitmapFactory.decodeStream(bais);
        //显示图片
        ivDynamicPicture.setImageBitmap(bitmap);
    }
    public void getString(){
        //获取字符串
        SharedPreferences sPreferences = getSharedPreferences("OutString", Context.MODE_PRIVATE);
        String outString = sPreferences.getString("identify_res", "");

        //显示字符串
        ivDynamicString.setText(outString);
    }
}
