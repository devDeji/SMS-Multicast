package com.yuzhiqiang.smsmulticast;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ListView listView;
    private ArrayList<Message> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViewers();
        setListeners();
    }

    private void getViewers() {
        listView = (ListView) findViewById(R.id.listView);
    }

    private void setListeners() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.action_add:
                return true;
            case R.id.action_import:
                intent.setClass(this, ImportActivity.class);
                startActivityForResult(intent, 1);
                return true;
            case R.id.action_send:
                intent.setClass(this, QueueActivity.class);
                intent.putExtra("Messages", this.list);
                startActivity(intent);
                this.list.clear();
                refreshListView();
                return true;
            case R.id.action_queue:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == 1) {
                this.list.addAll((ArrayList<Message>) data.getExtras().get("imported_data"));
                refreshListView();
            }
        } catch (NullPointerException ex) {
            System.err.println("oops");
        } finally {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void refreshListView() {
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(list.get(position).destination);
                text2.setText(list.get(position).content);
                return view;
            }
        };
        listView.setAdapter(adapter);
    }
}