package com.genisky.clocking;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.genisky.account.AuditItem;
import com.genisky.account.AuditItemInfo;
import com.genisky.account.PrepareResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AuditActivity extends AppCompatActivity {
    private List<Map<String, Object>> data_list  = new ArrayList<Map<String, Object>>();
    private MyAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit);


        ListView listView = (ListView)findViewById(R.id.audit_list);
        assert listView != null;
        listView.setItemsCanFocus(false);

        AuditItemInfo info = MainActivity.Services.getAuditItems();
        getdata(info.audititems);

        _adapter = new MyAdapter();
        listView.setAdapter(_adapter);
    }

    private void submit(ViewParent viewParent, boolean accept) {
        View view = (View)viewParent;
        int id = Integer.valueOf(((TextView) view.findViewById(R.id.audit_item_id)).getText().toString());
        PrepareResponse response = MainActivity.Services.auditing(id, accept);
        Toast.makeText(getApplicationContext(), response.message, Toast.LENGTH_SHORT).show();

        for(int i = 0; i < data_list.size(); ++i){
            if (Objects.equals(data_list.get(i).get("id").toString(), String.valueOf(id))){
                data_list.remove(i);
                break;
            }
        }
        _adapter = new MyAdapter();
        ((ListView)findViewById(R.id.audit_list)).setAdapter(_adapter);
    }

    private List<Map<String, Object>> getdata(AuditItem[] audititems){
        data_list.clear();
        for(int i=0;i<audititems.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", audititems[i].title);
            map.put("request", audititems[i].request);
            map.put("people", Arrays.toString(audititems[i].people));
            map.put("time", audititems[i].datetime);
            map.put("id", audititems[i].id);
            data_list.add(map);
        }
        return data_list;
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return data_list.size();
        }

        @Override
        public Object getItem(int i) {
            return data_list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return (int)data_list.get(i).get("id");
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null){
                view = LayoutInflater.from(AuditActivity.this).inflate(R.layout.audit_item, null);
                ((TextView)view.findViewById(R.id.audit_item_title)).setText(data_list.get(i).get("title").toString());
                ((TextView)view.findViewById(R.id.audit_item_request_id)).setText(data_list.get(i).get("request").toString());
                ((TextView)view.findViewById(R.id.audit_item_people)).setText(data_list.get(i).get("people").toString());
                ((TextView)view.findViewById(R.id.audit_item_time)).setText(data_list.get(i).get("time").toString());
                ((TextView)view.findViewById(R.id.audit_item_id)).setText(data_list.get(i).get("id").toString());
                view.findViewById(R.id.audit_item_accept).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submit(view.getParent().getParent(), true);
                    }
                });
                view.findViewById(R.id.audit_item_reject).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submit(view.getParent().getParent(), false);
                    }
                });
            }
            return view;
        }
    }
}
