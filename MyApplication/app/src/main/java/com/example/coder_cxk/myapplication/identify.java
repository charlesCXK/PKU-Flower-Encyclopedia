//回滚到支持地图能够正常查询的版本
package com.example.coder_cxk.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;



//@RuntimePermissions
public class identify extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int WRITE_PERMISSION = 0x01;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    //private MyLocationListener mLocationListener;
    private boolean firstLocation = true;
    private BitmapDescriptor mCurrentMarker;
    // private MyLocationListener myListener = new MyLocationListener();//继承BDAbstractLocationListener的class

    private MyLocationConfiguration config;

    private static final int BAIDU_READ_PHONE_STATE =100;

    public void showContacts(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ){
            Toast.makeText(getApplicationContext(),"没有权限,请手动开启定位权限",Toast.LENGTH_SHORT).show();
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            ActivityCompat.requestPermissions(identify.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,}, BAIDU_READ_PHONE_STATE);
        }
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
            default:
                break;
        }
    }

    //baidu ditu api
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //判断是否为android6.0系统版本，如果是，需要动态添加权限
        if (Build.VERSION.SDK_INT>=23){
            showContacts();
        }

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_identify);
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
    }

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

}
// 00:37