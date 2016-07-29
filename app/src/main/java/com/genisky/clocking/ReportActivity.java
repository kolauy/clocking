package com.genisky.clocking;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.genisky.account.ReportDetail;
import com.genisky.account.ReportInfo;

import java.util.Calendar;

public class ReportActivity extends AppCompatActivity {
    private int _currentYear;
    private Calendar _start;
    private Calendar _stop;
    private TableRow _last_detail_row;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        _last_detail_row = ((TableRow)findViewById(R.id.report_detail_6));

        Calendar c = Calendar.getInstance();
        _currentYear = c.get(Calendar.YEAR);
        _start = Calendar.getInstance();
        _start.set( c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
        _stop = Calendar.getInstance();
        _stop.set( c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.getActualMaximum(Calendar.DATE));
        ((TextView)findViewById(R.id.report_month)).setText("" + _start.get(Calendar.YEAR) + "-" + (_start.get(Calendar.MONTH) + 1));
        ((Button)findViewById(R.id.report_previous_month)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeMonth(-1);
            }
        });
        ((Button)findViewById(R.id.report_next_month)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeMonth(1);
            }
        });
        UpdateReport();
    }

    private void ChangeMonth(int step) {
        int selectedYear = _start.get(Calendar.YEAR);
        int selectedMonth = _start.get(Calendar.MONTH) + 1;
        if (selectedYear == _currentYear - 1 && selectedMonth == 1 && step < 0){
            return;
        }
        if (selectedYear == _currentYear && selectedMonth == 12 && step > 0){
            return;
        }
        selectedMonth += step;
        if (selectedMonth == 0){
            selectedYear -= 1;
            selectedMonth = 12;
        }else if (selectedMonth == 13){
            selectedYear += 1;
            selectedMonth = 1;
        }
        _start = Calendar.getInstance();
        _start.set(selectedYear, selectedMonth - 1, 1);
        _stop = Calendar.getInstance();
        _stop.set(selectedYear,selectedMonth - 1, _start.getActualMaximum(Calendar.DATE));
        ((TextView)findViewById(R.id.report_month)).setText("" + _start.get(Calendar.YEAR) + "-" + (_start.get(Calendar.MONTH) + 1));
        UpdateReport();
    }

    private void UpdateReport() {
        String start = "" + _start.get(Calendar.YEAR) + "-" + (_start.get(Calendar.MONTH) + 1) + "-" + _start.get(Calendar.DAY_OF_MONTH);
        String stop = "" + _stop.get(Calendar.YEAR) + "-" + (_stop.get(Calendar.MONTH) + 1) + "-" + _stop.get(Calendar.DAY_OF_MONTH);
        ReportInfo report = MainActivity.Services.getReportInfo(start, stop);
        ((TextView)findViewById(R.id.report_summary_clocked)).setText("当月共打卡：" + report.summary.clocked + " 次");
        ((TextView)findViewById(R.id.report_summary_overwork)).setText("当月共加班：" + report.summary.overwork + " 小时");

        int index = _start.get(Calendar.DAY_OF_WEEK);

        TableLayout layout = ((TableLayout)findViewById(R.id.report_detail));


        int weeks = _stop.get(Calendar.WEEK_OF_MONTH);
        if (weeks == 6 && layout.getChildCount() == 6)
            layout.addView(_last_detail_row);
        if (weeks == 5 && layout.getChildCount() == 7)
            layout.removeView(_last_detail_row);

        Calendar current = Calendar.getInstance();
        current.setTime(_start.getTime());
        for(int pos = index - 1; pos > 0; pos--){
            current.add(Calendar.DATE, -1);
            TextView day = (TextView)findViewById(getResources().getIdentifier("report_detail_1_" + pos, "id", getPackageName()));
            day.setText(String.valueOf(current.get(Calendar.DATE)));
            day.setTextColor(Color.rgb(0xdd, 0xdd, 0xdd));
            day.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
        }

        current.setTime(_start.getTime());
        for (int row = 2; row <= layout.getChildCount(); row++){
            for(int pos = row == 2 ? index : 1; pos <= 7; pos++ ){
                TextView day = (TextView)findViewById(getResources().getIdentifier("report_detail_" + (row -1) + "_" + pos, "id", getPackageName()));
                day.setText(String.valueOf(current.get(Calendar.DATE)));

                if (current.getTimeInMillis() > _stop.getTimeInMillis()){
                    day.setTextColor(Color.rgb(0xdd, 0xdd, 0xdd));
                    day.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff));
                }else{
                    String dayString = "" + current.get(Calendar.YEAR) + "-" + (current.get(Calendar.MONTH) + 1) + "-" + current.get(Calendar.DATE);
                    ReportDetail dayReport = GetDayReport(dayString, report.detail);
                    day.setTextColor(Color.rgb(0xf, 0xf, 0xf));
                    int background = Color.rgb(0, 0, 0);
                    if (dayReport == null){
                        background = Color.rgb(0xee, 0xee, 0xee);
                    }else if (dayReport.state.contains("clocked")){
                        background =Color.rgb(0x00, 0xee, 0xee);
                    }else if(dayReport.state.contains("amended")){
                        background =Color.rgb(0xee, 0x00, 0xee);
                    }else if(dayReport.state.contains("adjusted")){
                        background =Color.rgb(0xee, 0xee, 0x00);
                    } else{
                        background =Color.rgb(0xee, 0xee, 0xee);
                    }

                    if(dayReport != null && dayReport.state.contains("overwork")){
                        background &= Color.rgb(0x00, 0xee, 0x00);
                    }
                    day.setBackgroundColor(background);
                }
                current.add(Calendar.DATE, 1);
            }
        }
    }

    private ReportDetail GetDayReport(String dayString, ReportDetail[] detail) {
        for (int i = 0; i < detail.length; ++i){
            if (detail[i].date.equalsIgnoreCase(dayString))
                return detail[i];
        }
        return null;
    }
}
