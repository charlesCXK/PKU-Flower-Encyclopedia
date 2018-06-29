package com.example.coder_cxk.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class guessflower extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guessflower);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    // 判断猜测是否正确
    public void judge_guess_flower(View view){
        String yourguess="";
        EditText editText1 = (EditText)findViewById(R.id.guesstext);
        yourguess = editText1.getText().toString();

        String ans1 = "hehua";
        String ans2 = "荷花";

        TextView text = (TextView)findViewById(R.id.guess_showtext);
        if(yourguess.equals(ans1)==true || yourguess.equals(ans2)==true){
            text.setText("猜测正确！恭喜~");
        }
        else{
            text.setText("呜呜猜错了......");
        }
    }

}
