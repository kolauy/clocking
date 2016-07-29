package com.genisky.clocking;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import com.genisky.account.UserInfo;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        UserInfo user = MainActivity.Services.getUserInfo();
        ((TextView)findViewById(R.id.setting_name)).setText(user.name);
        if (user.picture != null && user.picture.length() > 0){
            byte[] b = Base64.decode(user.picture, Base64.DEFAULT);
            ((ImageView)findViewById(R.id.setting_image)).setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
        }
        ((TextView)findViewById(R.id.setting_phone)).setText(MainActivity.DatabaseManager.getAuthenticationRequest().phone);
        ((TextView)findViewById(R.id.setting_department)).setText(user.department);
        ((TextView)findViewById(R.id.setting_company)).setText(user.company);
    }
}
