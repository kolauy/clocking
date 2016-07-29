package com.genisky.clocking;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.genisky.account.OrganizationInfo;
import com.genisky.account.People;
import com.genisky.account.PrepareResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmendActivity extends AppCompatActivity {

    private List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
    private SimpleAdapter sim_adapter;
    private boolean _submitting = false;
    private OrganizationInfo _organization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amend);

        ((TextView)findViewById(R.id.amend_request_id)).setText(MainActivity.UserName);


        ListView listView = (ListView)findViewById(R.id.amend_people);
        assert listView != null;
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d");
        Date curDate = new Date(System.currentTimeMillis());
        ((TextView)findViewById(R.id.amend_day)).setText(formatter.format(curDate));

        _organization = MainActivity.Services.getOrganization();
        getdata(_organization.members);


        String [] from ={"image","text"};
        int [] to = {R.id.message_item_image, R.id.message_item_text};
        sim_adapter = new SimpleAdapter(this, data_list, R.layout.people_item, from, to);

        int listViewHeight = 0;
        for(int i=0;i<_organization.members.length;i++){
            View temp = sim_adapter.getView(i,null,listView);
            temp.measure(0,0);
            listViewHeight += temp.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = listViewHeight + 50  + (listView.getDividerHeight() * (_organization.members.length - 1));
        ((ViewGroup.MarginLayoutParams)params).setMargins(10, 10, 10, 10);
        listView.setLayoutParams(params);



        listView.setAdapter(sim_adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checktv = (CheckedTextView) parent.getChildAt(position).findViewById(R.id.message_item_text);
                if(checktv.isChecked()){
                    checktv.setChecked(false);
                }else{
                    checktv.setChecked(true);
                }
                data_list.get(position).put("checked", checktv.isChecked());
            }
        });

        findViewById(R.id.amend_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    private void submit() {
        if (_submitting)
            return;
        TextView view = ((TextView)findViewById(R.id.amend_day));
        try {
            List ids = new ArrayList();
            for (int i = 0; i < data_list.size(); ++i){
                if ((boolean)data_list.get(i).get("checked") == true)
                    ids.add(data_list.get(i).get("id"));
            }
            if (ids.size() == 0){
                Toast.makeText(getApplicationContext(), "未选择补录人员", Toast.LENGTH_SHORT).show();
                return;
            }
            int[] temp = new int[ids.size()];
            for(int i = 0; i < ids.size(); ++i)
                temp[i] = (int)ids.get(i);
            PrepareResponse response = MainActivity.Services.amending(temp, view.getText().toString());
            Toast.makeText(getApplicationContext(), response.message, Toast.LENGTH_SHORT).show();
            if (response.result.equalsIgnoreCase("success"))
                finish();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }finally {
            _submitting = false;
        }
    }

    private void getdata(People[] peoples){
        for(int i=0;i<peoples.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("image", R.drawable.people);
            map.put("text", peoples[i].name);
            map.put("id", peoples[i].id);
            map.put("checked", false);
            data_list.add(map);
        }
    }
}
