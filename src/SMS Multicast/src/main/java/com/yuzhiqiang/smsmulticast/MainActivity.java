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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SENT_SMS_ACTION")) {
                Toast.makeText(MainActivity.this, "SENT", Toast.LENGTH_SHORT).show();
                Message message = (Message) intent.getExtras().get("sent_message");
                list.get(list.indexOf(message)).status = Message.SENT;
                refreshListView();
            }
        }
    };
    private ListView listView;
    private ArrayList<Message> list;


    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getViewers();
        setListeners();
        list = new ArrayList<>();
        registerReceiver(broadcastReceiver, new IntentFilter("SENT_SMS_ACTION"));

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
                sendSMS();
                return true;
            case R.id.action_clear:
                clearListSent();
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
                text1.setText(list.get(position).status + ": " + list.get(position).destination);
                text2.setText(list.get(position).content);
                return view;
            }
        };
        listView.setAdapter(adapter);
    }

    private void sendSMS() {
        Toast.makeText(this, "Sending " + list.size() + " messages.", Toast.LENGTH_SHORT).show();
        SmsManager sms = SmsManager.getDefault();
        for (Message message : this.list) {
            Intent sentIntent = new Intent("SENT_SMS_ACTION");
            sentIntent.putExtra("sent_message", message);
            PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            sms.sendTextMessage(message.destination, null, message.content, sentPendingIntent, null);
            message.status = Message.SENDING;
        }
    }

    private void clearListSent() {
        for (Message message : this.list) {
            if (message.status == Message.SENT)
                list.remove(message);
        }
        refreshListView();
    }
}