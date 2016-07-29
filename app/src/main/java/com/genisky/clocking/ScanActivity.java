package com.genisky.clocking;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.genisky.account.Office;
import com.genisky.account.PrepareResponse;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class ScanActivity extends AppCompatActivity {
    public LocationClient mLocationClient = null;
    private double _longitude;
    private double _latitude;
    private Office _office;
    private Pattern _codePattern;

    private CompoundBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        ((TextView) findViewById(R.id.scan_request_id)).setText(MainActivity.UserName);

        Calendar c = Calendar.getInstance();
        ((TextView) findViewById(R.id.scan_request_date)).setText("" + c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE));
        ((TextView) findViewById(R.id.scan_request_time)).setText("" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND));

        _office = null;
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        initLocation();
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location == null){
                    return;
                }
                _latitude = location.getLatitude();
                _longitude = location.getLongitude();
                _office = null;

                Calendar c = Calendar.getInstance();
                long currentSeconds = c.get(Calendar.SECOND) + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.HOUR_OF_DAY) * 3600;

                double distance = Double.MAX_VALUE;
                String office = "";
                for(int i = 0; i < MainActivity.Offices.length; ++i){
                    if (currentSeconds < MainActivity.Offices[i].clockingStartSeconds || currentSeconds > MainActivity.Offices[i].clockingStopSeconds){
                        continue;
                    }
                    double current = getDistance(_latitude, _longitude, MainActivity.Offices[i].latitude, MainActivity.Offices[i].longitude);
                    if(current >= distance){
                        continue;
                    }
                    distance = current;
                    office = MainActivity.Offices[i].name;
                    if (current > MainActivity.Offices[i].clockingDistance){
                        continue;
                    }
                    _office = MainActivity.Offices[i];
                }
                String text = "距离 " + office + " " + distance + " 米";
                if (_office != null){
                    text = "距离 " + _office.name + " " + distance + " 米";
                }else{
                    Toast.makeText(ScanActivity.this, "距离办公室太远", Toast.LENGTH_SHORT).show();
                }
                ((TextView) findViewById(R.id.scan_request_location)).setText(text);
                ((TextView) findViewById(R.id.scan_request_location)).setTextColor(_office != null ? Color.rgb(0, 0, 0) : Color.rgb(200, 0, 0));
            }
        });    //注册监听函数

        _codePattern = Pattern.compile(MainActivity.BarcoeRegExpression);
        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (_office == null){
                    return;
                }
                String code = result.getText();
                if (!_codePattern.matcher(code).matches()){
                    Toast.makeText(ScanActivity.this, "非法二维码", Toast.LENGTH_SHORT).show();
                    return;
                }
                Calendar now = Calendar.getInstance();
                String date = "" + now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DATE);
                String time = "" + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":"  + now.get(Calendar.SECOND);
                PrepareResponse response = MainActivity.Services.clocking(code, date, time, _longitude, _latitude, _office.name);
                Toast.makeText(ScanActivity.this, response.message, Toast.LENGTH_SHORT).show();
                if (response.result.equalsIgnoreCase("success")){
                    finish();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });
    }


    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setProdName("clocking_sdk");
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onStart() {
        if (!mLocationClient.isStarted()){
            mLocationClient.start();
        }
        mLocationClient.requestLocation();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mLocationClient.isStarted()){
            mLocationClient.stop();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        try {
            barcodeView.resume();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            barcodeView.pause();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }

    private double getDistance(double latitude_1, double longitude_1, double latitude_2, double longitude_2){
        double pk = 180.0 / 3.14159265;
        double a1 = latitude_1 / pk;
        double a2 = longitude_1 / pk;
        double b1 = latitude_2 / pk;
        double b2 = longitude_2 / pk;
        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        return 6366000 * tt;
    }
}