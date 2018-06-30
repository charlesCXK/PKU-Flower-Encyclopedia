package com.example.coder_cxk.myapplication;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.util.Log;


public class history extends AppCompatActivity {

    String logfile;     // log 文件的文件路径
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        String strPath = Environment.getExternalStorageDirectory() + File.separator + "pkuflower";
        logfile = strPath + File.separator + "log.txt";       // 获取log文件信息
        ArrayList<String> log_info = read_log(logfile);
        modify_block(log_info);
    }

    // 根据log信息动态修改历史记录
    public void modify_block(ArrayList<String> arr){
        int len = arr.size();
        Resources res = getResources();

        // 根据记录个数动态显示layout block
        for (int i=0;i<18;i++){
            int layoutid = res.getIdentifier("history_l"+i,"id",getPackageName());
            LinearLayout ll = (LinearLayout) findViewById(layoutid);
            ll.setVisibility(View.GONE);
        }
        for (int i=len-1;i>=0;i--){      // 倒顺输出
            String[] split_res = arr.get(i).split("\\$");     // 字符串分隔，获取路径，时间，花的种类

            int layoutid = res.getIdentifier("history_l"+(len-i-1),"id",getPackageName());
            LinearLayout ll = (LinearLayout) findViewById(layoutid);
            ll.setVisibility(View.VISIBLE);

            int id=res.getIdentifier("history_text"+(len-i-1),"id",getPackageName());
            TextView txt = (TextView) findViewById(id);
            txt.setText(split_res[1]+" "+split_res[2]);

            int imgid=res.getIdentifier("history_img"+(len-i-1),"id",getPackageName());
            ImageView img = (ImageView) findViewById(imgid);

            Bitmap bmp = getImageThumbnail(split_res[0], 200, 200);
            img.setImageBitmap(bmp);
        }

        // 历史记录太多，清除一半
        if(len == 18){
            try {
                FileWriter writer = new FileWriter(logfile, false);     // 以追加方式写文件
                String content = "";
                for(int i=17;i>8;i--){
                    content += arr.get(i);
                }
                for(int i=0;i<=8;i++){
                    String[] split_res = arr.get(i).split("\\$");
                    String small_pic = split_res[0];
                    String big_pic = small_pic.substring(0,small_pic.length() - 10) + small_pic.substring(small_pic.length()-4,small_pic.length());
                    File file = new File(small_pic);
                    boolean result = file.delete();
                    file = new File(big_pic);
                    result = file.delete();
                }
                writer.write(content);
                writer.close();
            }catch(IOException e){}
        }
    }

    // 读取log文件。每行是文件路径 + 时间 + 识别结果 + 换行符
    public ArrayList<String> read_log(String logfile){
        ArrayList<String> res = new ArrayList<String>();
        //BufferedReader是可以按行读取文件
        try {
            FileInputStream inputStream = new FileInputStream(logfile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                System.out.println(str);
                res.add(str);
            }
            //close
            inputStream.close();
            bufferedReader.close();
        } catch(IOException e){}
    /*
        Integer i = res.size();
        TextView txt = (TextView) findViewById(R.id.history_text0);
        txt.setText(i.toString());*/

        return res;
    }
    /*读入bitmap图片*/
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //关于inJustDecodeBounds的作用将在下文叙述
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        int h = options.outHeight;//获取图片高度
        int w = options.outWidth;//获取图片宽度
        int scaleWidth = w / width; //计算宽度缩放比
        int scaleHeight = h / height; //计算高度缩放比
        int scale = 1;//初始缩放比
        if (scaleWidth < scaleHeight) {//选择合适的缩放比
            scale = scaleWidth;
        } else {
            scale = scaleHeight;
        }
        options.inSampleSize = scale;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把inJustDecodeBounds 设为 false
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
    /*清除历史记录*/
    public void clear_history(View view){
        try {
            FileWriter writer = new FileWriter(logfile, false);     // 以追加方式写文件
            String content = "";
            writer.write(content);
            writer.close();
        }catch(IOException e){}
        // 修改log信息
        ArrayList<String> log_info = read_log(logfile);
        modify_block(log_info);
        // 重新载入log
        setContentView(R.layout.activity_history);
    }
}
