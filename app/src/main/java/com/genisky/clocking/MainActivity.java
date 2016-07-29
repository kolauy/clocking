package com.genisky.clocking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.genisky.account.AuthenticationResponse;
import com.genisky.account.DatabaseManager;
import com.genisky.account.Office;
import com.genisky.account.OfficeInfo;
import com.genisky.account.PrepareResponse;
import com.genisky.account.UserInfo;
import com.genisky.server.GeniskyServices;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static DatabaseManager DatabaseManager;
    public static GeniskyServices Services;
    public static String UserName;
    public static String BarcoeRegExpression;
    public static Office[] Offices;

    private GridView gview;
    private List<Map<String, Object>> data_list;
    private SimpleAdapter sim_adapter;
    private int[] icons = {
            R.drawable.zhaoxiangdaka,
            R.drawable.dakabulu,
            R.drawable.dakaxiuzheng,
            R.drawable.jiabanshenqing,
            R.drawable.xiaoxituisong,
            R.drawable.daishenhexiang,
            R.drawable.yuebaochaxun
    };
    private String[] iconName = { "照相打卡", "打卡补录", "打卡修正", "加班申请", "消息推送", "待审核项", "月报查询" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        DatabaseManager = new DatabaseManager(this);
        if (!DatabaseManager.isAccountExist()) {
            Intent register = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(register);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseManager.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!DatabaseManager.isAccountExist()){
            return;
        }
        AuthenticationResponse result = DatabaseManager.getAuthenticationResult();
        Services = new GeniskyServices(result);
        String state = Services.getUserState();
        if (state == null || !state.equals("active")){
            Toast.makeText(this, "失效用户", Toast.LENGTH_SHORT).show();
            Intent register = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(register);
        }
        UserInfo info = Services.getUserInfo();
        UserName = info.name;
        ((TextView)findViewById(R.id.user_name)).setText(String.format("%s (%s)", info.name, info.department));
        ((TextView)findViewById(R.id.company_name)).setText(info.company);
        if (info.companyPicture != null && info.companyPicture.length() > 0) {
            byte[] data = Base64.decode(info.companyPicture, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            ((LinearLayout)findViewById(R.id.company_background)).setBackground(new BitmapDrawable(bitmap));
        }
        if (info.picture != null && info.picture.length() > 0) {
            byte[] data = Base64.decode(info.picture, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            ((ImageView)findViewById(R.id.user_image)).setImageBitmap(bitmap);
        }
        if (info.company != null && info.company.length() > 0){
            OfficeInfo officeInfo = Services.getOffice(info.company);
            Offices = officeInfo.offices;
        }

        gview = (GridView) findViewById(R.id.gridView);

        data_list = new ArrayList<Map<String, Object>>();

        getData(info);

        String [] from ={"image","text"};
        int [] to = {R.id.task_item_image, R.id.task_item_text};
        sim_adapter = new SimpleAdapter(this, data_list, R.layout.task_item, from, to);

        gview.setAdapter(sim_adapter);
        gview.setOnItemClickListener(new GridViewItemClickListener(this));

        ImageButton setting = (ImageButton)findViewById(R.id.main_setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        setting.setVisibility(View.INVISIBLE);
    }

    class GridViewItemClickListener implements AdapterView.OnItemClickListener {

        private final Activity _parent;

        public GridViewItemClickListener(Activity parent){
            _parent = parent;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = null;
            CharSequence text = ((TextView)view.findViewById(R.id.task_item_text)).getText();
            if (text.equals("照相打卡")){
                if (Offices == null || Offices.length <= 0){
                    Toast.makeText(_parent, "未设置办公室信息", Toast.LENGTH_SHORT).show();
                }else{
                    Calendar now = Calendar.getInstance();
                    String day = "" + now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DATE);
                    long time = now.get(Calendar.HOUR_OF_DAY) * 3600 + now.get(Calendar.MINUTE) * 60 + now.get(Calendar.SECOND);
                    boolean found = false;
                    for (int i = 0; i < Offices.length; ++i){
                        if (time >= Offices[i].clockingStartSeconds && time < Offices[i].clockingStopSeconds){
                            found = true;
                            break;
                        }
                    }
                    if (!found){
                        Toast.makeText(_parent, "未到打卡时间", Toast.LENGTH_SHORT).show();
                    }else{
                        PrepareResponse prepare = Services.prepareClocking(day);
                        if (prepare.result.equalsIgnoreCase("yes")){
                            BarcoeRegExpression = prepare.message;
                            intent=new Intent(_parent, ScanActivity.class);
                        }else{
                            Toast.makeText(_parent, prepare.message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }else if (text.equals("打卡补录")){
                PrepareResponse prepare = Services.prepareAmending();
                if (prepare.result.equalsIgnoreCase("yes")){
                    intent=new Intent(_parent, AmendActivity.class);
                }else{
                    Toast.makeText(_parent, prepare.message, Toast.LENGTH_SHORT).show();
                }
            }else if (text.equals("打卡修正")){
                PrepareResponse prepare = Services.prepareAdjusting();
                if (prepare.result.equalsIgnoreCase("yes")){
                    intent=new Intent(_parent, AdjustActivity.class);
                }else{
                    Toast.makeText(_parent, prepare.message, Toast.LENGTH_SHORT).show();
                }
            }else if (text.equals("加班申请")){
                intent = new Intent(_parent, OverworkActivity.class);
            }else if (text.equals("消息推送")){
                intent = new Intent(_parent, MessageActivity.class);
            }else if (text.equals("待审核项")){
                intent = new Intent(_parent, AuditActivity.class);
            }else if (text.equals("月报查询")){
                intent = new Intent(_parent, ReportActivity.class);
            }else if (text.equals("系统设置")){
                intent = new Intent(_parent, SettingActivity.class);
            }
            if (intent != null){
                startActivity(intent);
            }
        }
    }

    public List<Map<String, Object>> getData(UserInfo info){
        for(int i=0;i<iconName.length;i++){
            boolean enable = true;
            if (info.functions != null && info.functions.length > 0){
                enable = false;
                for(int j = 0; j < info.functions.length; j++){
                    if (info.functions[j].equalsIgnoreCase(iconName[i]))
                        enable = true;
                }
            }
            if (!enable){
                continue;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", icons[i]);
            map.put("text", iconName[i]);
            data_list.add(map);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("image", R.drawable.bigsetting);
        map.put("text", "系统设置");
        data_list.add(map);
        return data_list;
    }
}
