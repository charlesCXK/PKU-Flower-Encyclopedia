//回滚到支持地图能够正常查询的版本
package com.example.coder_cxk.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.Manifest.permission.*;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//@RuntimePermissions
public class identify extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /******cxk*****/
    // 下面弹出来的弹出框，选相机/相册
    SelectPicPopupWindow menuWindow;

    private ImageView image;
    private ImageButton btn_picture;
    private Uri fileUri;
    String file, now_time;
    String raw_file, small_file;        // 一个是原图，一个是缩小后的缩率图
    String logfile;     // log 文件的文件名
    String strPath;
    /*******end*******/

    private static final int WRITE_PERMISSION = 0x01;
    private static final int BAIDU_READ_PHONE_STATE =100;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    //private MyLocationListener mLocationListener;
    private boolean firstLocation = true;
    private BitmapDescriptor mCurrentMarker;
    // private MyLocationListener myListener = new MyLocationListener();//继承BDAbstractLocationListener的class

    private MyLocationConfiguration config;

    /*右上角历史记录*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e("ZHANGBIN","create option menu");
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_history:
                Intent intent = new Intent();
                intent.setClass(identify.this, history.class);
                identify.this.startActivity(intent);
                identify.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*手动获取权限*/

    public void showContacts(){
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)||(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED ) ){
            Toast.makeText(getApplicationContext(),"没有权限,请手动开启定位权限",Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(identify.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.INTERNET}, BAIDU_READ_PHONE_STATE);
        }

    }

    /*
     * 将时间戳转换为时间
     */
    public String stampToDate(long timeMillis){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }


    //Android6.0申请权限的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                    //permission was granted, yay! Do the contacts-related task you need to do.
                    //这里进行授权被允许的处理
                } else {
                    Toast.makeText(getApplicationContext(), "获取位置权限失败，请手动开启", Toast.LENGTH_SHORT).show();
                }
                break;
            case WRITE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else
            {
                Log.d("permission......", "Write Permission Failed");
                Toast.makeText(this, "You must allow permission write external storage to your mobile device.", Toast.LENGTH_SHORT).show();
                finish();
            }
            default:
                break;
        }
    }

    //baidu ditu api
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);



        //判断是否为android6.0系统版本，如果是，需要动态添加权限
        if (Build.VERSION.SDK_INT>=23){
            showContacts();
        }
        // 获取权限

        SDKInitializer.initialize(getApplicationContext());
        //setContentView(R.layout.activity_main);
        // 加载toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //获取地图控件引用;

        mMapView = (MapView)findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        //mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15f);
        mBaiduMap.setMapStatus(msu);


        // 那个邮件标志的点击响应函数

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // 侧滑动栏
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mLocationClient = new LocationClient(this);
        firstLocation = true;

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setOpenGps(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);

        //定义自定义图标
        BitmapDescriptor myMarker = BitmapDescriptorFactory.fromResource(R.drawable.flow2);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, myMarker);

        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                // map view 销毁后不在处理新接收的位置
                if(location==null||mMapView==null)
                    return;

                //构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);


                // 第一次定位时，将地图位置移动到当前位置
                if(firstLocation){
                    firstLocation = false;
                    LatLng xy = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    int loc_type = location.getLocType();
                    MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(xy);
                    mBaiduMap.animateMapStatus(status);
                }
            }
        });

        // 获得系统时间戳
        long timeStamp = System.currentTimeMillis();
        now_time = stampToDate(timeStamp);

        strPath = Environment.getExternalStorageDirectory() + File.separator + "pkuflower";
        File dirfile = new File(strPath);
        if(!dirfile.exists()){
            dirfile.mkdirs();
        }

        btn_picture = (ImageButton) findViewById(R.id.btn_camera);
        raw_file = strPath + File.separator + now_time;
        small_file = raw_file + "_small";
        logfile = strPath + File.separator + "log.txt";
        file = raw_file + ".png";
        fileUri = Uri.fromFile(new File(file));//Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.jpg")); //图片存放路径

        /*加了这个，intent.putExtra就不报错*/
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        builder.detectFileUriExposure();

        //把文字控件添加监听，点击弹出自定义窗口
        btn_picture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //实例化SelectPicPopupWindow
                menuWindow = new SelectPicPopupWindow(identify.this, itemsOnClick);
                //显示窗口
                //menuWindow.showAtLocation(MainActivity.this.findViewById(R.id.main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                // 设置layout在PopupWindow中显示的位置
                menuWindow.showAtLocation(v, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
            }
        });
    }


    //


    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息

            Log.e("描述：", sb.toString());
        }
    }

    //

    // 开启定位
    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){//如果定位client没有开启，开启定位
            mLocationClient.start();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭定位
        mBaiduMap.setMyLocationEnabled(false);

        // if(mLocationClient.isStarted()){
        mLocationClient.stop();
        //  }

    }

    @Override
    public  void onDestroy()
    {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()
        mMapView.onDestroy();
        mMapView = null;
    }
    // 打开/关闭侧边栏
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // 初始化菜单信息
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.identify, menu);
        return true;
    }
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/


    // 滑动侧边栏按钮的跳转功能
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_identify) {
            // Handle the camera action
            Intent intent = new Intent();
            intent.setClass(identify.this, identify.class);
            identify.this.startActivity(intent);
        } else if (id == R.id.nav_baike) {
            Intent intent = new Intent();
            intent.setClass(identify.this, baike.class);
            identify.this.startActivity(intent);

        } else if (id == R.id.nav_main) {
            Intent intent = new Intent();
            intent.setClass(identify.this, MainActivity.class);
            identify.this.startActivity(intent);
        }  else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            Uri uri = Uri.parse("https://mail.pku.edu.cn/coremail/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if(id == R.id.guess_flower){
            Intent intent = new Intent();
            intent.setClass(identify.this, guessflower.class);
            identify.this.startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    //自定义的定位监听


    /*5:53 added*/
    //为弹出窗口实现监听类，选择相机还是相册
    private View.OnClickListener itemsOnClick = new View.OnClickListener(){
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_take_photo:
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    /*再次拍照片时，记录新的时间戳*/
                    long timeStamp = System.currentTimeMillis();
                    now_time = stampToDate(timeStamp);
                    raw_file = strPath + File.separator + now_time;
                    small_file = raw_file + "_small";
                    file = raw_file + ".png";
                    fileUri = Uri.fromFile(new File(file));

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(intent, 100);
                    break;
                case R.id.btn_pick_photo:
                    Intent intent1 = new Intent(Intent.ACTION_PICK);
                    intent1.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent1, 200);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){
            /*
            int width = image.getWidth();
            int height = image.getHeight();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileUri.getPath(), opts);
            int w = opts.outWidth;
            int h = opts.outHeight;
            int factor;
            if(w>width && h>height){
                factor = Math.min(w/width, h/height);  //根据ImageView的大小按一定比例缩小图片
            }else {
                factor = 1;
            }*/
            BitmapFactory.Options opts = new BitmapFactory.Options();
            BitmapFactory.decodeFile(fileUri.getPath(), opts);
            opts.inJustDecodeBounds = true;
            opts.inSampleSize = 1;
            opts.inJustDecodeBounds = false;
            // 重新设置文件名
            //raw_file = Environment.getExternalStorageDirectory() + File.separator + now_time;
            fileUri = Uri.fromFile(new File(raw_file+".png"));

            Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath(), opts);

            Bitmap small_one = getImageThumbnail(fileUri.getPath(), 200, 200);
            String small_savepath = saveBitmap(small_one, raw_file+"_small");       // 存储小的那个

            String img_path = raw_file + ".png";

            String result = classify.get_result(img_path);

            if (!result.startsWith("#"))
                write_log(logfile, small_savepath, now_time, result);        // 写入log文件
            //image.setImageBitmap(bm);
            // ********将图片和结果字符串显示到下一个activity********
            intentBitmap(small_one);

            if (result.startsWith("~"))
                intentString("(Not flower) " + result.substring(2));
            else
                intentString(result);

            Intent intent=new Intent(identify.this,show_photo.class);
            startActivity(intent);
        } else if(requestCode == 200 && resultCode == RESULT_OK){
            // 获得系统时间戳
            long timeStamp = System.currentTimeMillis();
            now_time = stampToDate(timeStamp);
            raw_file = strPath + File.separator + now_time;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            Uri uri = data.getData();
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(in, null, opts);
                int w = opts.outWidth;
                int h = opts.outHeight;
                int factor = 1;
                opts.inSampleSize = factor;
                opts.inJustDecodeBounds = false;
                in = getContentResolver().openInputStream(uri);   //需要再次获取，因为前面流已经改变了
                Bitmap bm = BitmapFactory.decodeStream(in, null, opts);

                // ********存储图片、压缩、再存储********
                String savepath = saveBitmap(bm, raw_file);
                Bitmap small_one = getImageThumbnail(savepath, 200, 200);
                String small_savepath = saveBitmap(small_one, raw_file+"_small");       // 存储小的那个

                String img_path = raw_file + ".png";

                String result = classify.get_result(img_path);

                if (!result.startsWith("#"))
                    write_log(logfile, small_savepath, now_time, result);        // 写入log文件
                //image.setImageBitmap(bm);
                // ********将图片和结果字符串显示到下一个activity********
                intentBitmap(small_one);

                if (result.startsWith("~"))
                    intentString("(Not flower) " + result.substring(2));
                else
                    intentString(result);

                Intent intent=new Intent(identify.this,show_photo.class);
                startActivity(intent);

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }
    /*保存bitmap文件到SDK卡目录上*/
    public String saveBitmap(Bitmap bitmap, String filename){
        String savepath = filename+".png";
        File file = new File(savepath);
        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)){
                out.flush();
                out.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return savepath;
    }
    /*生成缩略图*/
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
    // 将图片放到共享区域
    public void intentBitmap(Bitmap bitmap) {
        //把Bitmap转码成字符串
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50,baos);
        String imageBase64 = new String (Base64.encode(baos.toByteArray(), 0));
        //把字符串存到SharedPreferences里面
        SharedPreferences prePicture = getSharedPreferences("Picture", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prePicture.edit();
        editor.putString("cameraImage", imageBase64);
        editor.commit();
    }
    // 将字符串放到共享区域
    public void intentString(String str) {
        //把字符串存到SharedPreferences里面
        SharedPreferences prePicture = getSharedPreferences("OutString", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prePicture.edit();
        editor.putString("identify_res", str);
        editor.commit();
    }
    // 将新的图片信息写入文件（resize之后的）
    public void write_log(String desname, String filename, String thattime, String flower){
        try {
            FileWriter writer = new FileWriter(desname, true);     // 以追加方式写文件
            String content = filename+"$"+thattime+"$"+flower+"\n";
            writer.write(content);
            writer.close();
        }catch(IOException e){}
    }
}
// 00:37