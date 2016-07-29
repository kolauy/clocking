package com.genisky.clocking;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.genisky.account.ClockingMessage;
import com.genisky.account.MessageInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    private List<Map<String, Object>> data_list = new ArrayList<Map<String, Object>>();
    private SimpleAdapter _sim_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        ListView listView = (ListView)findViewById(R.id.message_list);
        assert listView != null;
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        MessageInfo info = MainActivity.Services.getUnreadMessages();
        getdata(info.messages, MainActivity.DatabaseManager.getMessages());
        MainActivity.DatabaseManager.saveUnreadMessages(info.messages);

        String [] from ={"time", "title", "text"};
        int [] to = {R.id.message_item_time, R.id.message_item_title, R.id.message_item_text};
        _sim_adapter = new SimpleAdapter(this, data_list, R.layout.message_item, from, to);

        listView.setAdapter(_sim_adapter);

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

        ((Button)findViewById(R.id.message_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List ids = new ArrayList();
                for (int i = 0; i < data_list.size(); ++i){
                    if ((boolean)data_list.get(i).get("checked") == true)
                        ids.add(data_list.get(i).get("id"));
                }
                if (ids.size() <= 0){
                    Toast.makeText(getApplicationContext(), "未选中消息", Toast.LENGTH_SHORT).show();
                    return;
                }
                deleteSelection(ids);
            }
        });
        ((Button)findViewById(R.id.message_delete_all)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List ids = new ArrayList();
                for (int i = 0; i < data_list.size(); ++i){
                    ids.add(data_list.get(i).get("id"));
                }
                deleteSelection(ids);
            }
        });
    }

    private void deleteSelection(List ids) {
        int[] temp = new int[ids.size()];
        for(int i = 0; i < ids.size(); ++i){
            temp[i] = (int)ids.get(i);
            for(int j = 0; j < data_list.size(); ++j){
                int id = (int)data_list.get(j).get("id");
                if (id == temp[i]) {
                    data_list.remove(j);
                    break;
                }
            }
        }
        _sim_adapter.notifyDataSetChanged();
        MainActivity.DatabaseManager.deleteMessages(temp);
    }


    private void getdata(ClockingMessage[] remoteMessages, ClockingMessage[] localMessages){
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<remoteMessages.length;i++){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("time", remoteMessages[i].datetime);
            map.put("title", remoteMessages[i].title);
            map.put("text", remoteMessages[i].content);
            map.put("id", remoteMessages[i].id);
            map.put("checked", false);
            list.add(remoteMessages[i].id);
            data_list.add(map);
        }
        for(int i=0;i<localMessages.length;i++){
            if (list.contains(localMessages[i].id))
                continue;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("time", localMessages[i].datetime);
            map.put("title", localMessages[i].title);
            map.put("text", localMessages[i].content);
            map.put("id", localMessages[i].id);
            map.put("checked", false);
            data_list.add(map);
        }
    }
}
