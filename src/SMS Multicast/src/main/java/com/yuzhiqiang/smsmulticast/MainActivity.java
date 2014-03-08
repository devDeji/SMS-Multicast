package com.yuzhiqiang.smsmulticast;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Random;
import java.util.UUID;

public class MainActivity extends Activity {

    ArrayAdapter listAdapter;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals("SENT_SMS_ACTION")) {
                    Message message = (Message) intent.getExtras().get("sent_message");
                    Toast.makeText(MainActivity.this, "SENT: " + message.destination + "(" + list.indexOf(message) + ")", Toast.LENGTH_SHORT).show();
                    if (getResultCode() == Activity.RESULT_OK)
                        if(list.get(list.indexOf(message)).status != Message.FAILED)
                            list.get(list.indexOf(message)).status = Message.SENT;
                    else
                        list.get(list.indexOf(message)).status = Message.FAILED;
                    refreshListView();
                }
            } catch (Exception ex) {

            }
        }
    };
    private ListView listView;
    private ArrayList<Message> list;
    private Handler handler;

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
        handler = new Handler();
        registerReceiver(broadcastReceiver, new IntentFilter("SENT_SMS_ACTION"));
        setListView();
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
            case R.id.action_clear_sent:
                clearListSent();
                return true;
            case R.id.action_clear_all:
                list.clear();
                refreshListView();
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

    private void setListView() {
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
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
        listView.setAdapter(listAdapter);
    }

    private void refreshListView() {
        listAdapter.notifyDataSetChanged();
    }

    private void sendSMS() {
        final ArrayList<Message> pendingList = new ArrayList<>();
        for (Message message : list) {
            if (message.isSentOut() == false) {
                message.status = Message.PENDING;
                pendingList.add(message);
            }
        }
        Toast.makeText(this, "Sending " + list.size() + " messages.", Toast.LENGTH_SHORT).show();
        final SmsManager sms = SmsManager.getDefault();
        new Thread() {
            @Override
            public void run() {
                super.run();
                for (final Message message : pendingList) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Sending: " + message.destination + "(" + list.indexOf(message) + ")", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent sentIntent = new Intent("SENT_SMS_ACTION");
                    sentIntent.putExtra("sent_message", message);
                    PendingIntent sentPendingIntent = PendingIntent.getBroadcast(MainActivity.this, list.indexOf(message), sentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    ArrayList<String> contents = sms.divideMessage(message.content);
                    ArrayList<PendingIntent> PendingIntents = new ArrayList<>();
                    for (int i = 0; i < contents.size(); i++)
                        PendingIntents.add(sentPendingIntent);
                    sms.sendMultipartTextMessage(message.destination, null, contents, PendingIntents, null);
                    message.status = Message.SENDING;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            refreshListView();
                        }
                    });
                    try {
                        sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    private void clearListSent() {
        final ArrayList<Message> sentList = new ArrayList<>();
        for (Message message : this.list) {
            if (message.status == Message.SENT) {
                sentList.add(message);
            }
        }
        for (Message message : sentList) {
            this.list.remove(message);
        }
        refreshListView();
    }
}