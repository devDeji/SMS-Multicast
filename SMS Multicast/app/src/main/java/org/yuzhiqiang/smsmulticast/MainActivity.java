package org.yuzhiqiang.smsmulticast;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.yuzhiqiang.smsmulticast.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	private static final int REQUEST_IMPORT_DATA = 0;
	// Do not change reference of list otherwise notifyDataSetChanged() will not work
	private final ArrayList<Message> list = new ArrayList<>();
	private final Handler handler = new Handler();
	ArrayAdapter listAdapter;
	private BroadcastReceiver broadcastReceiver;
	private ListView listView;

	@Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listView);
		setupBroadcastReceiver();
		setListView();
	}

	private void setupBroadcastReceiver() {
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					if (intent.getAction().equals("SENT_SMS_ACTION")) {
						Message message = (Message) intent.getExtras().get("sent_message");
						Toast.makeText(MainActivity.this, "SENT: " + message.destination + "(" + list.indexOf(message) + ")", Toast.LENGTH_SHORT).show();
						if (getResultCode() == Activity.RESULT_OK)
							if (list.get(list.indexOf(message)).status != Message.FAILED)
								list.get(list.indexOf(message)).status = Message.SENT;
							else
								list.get(list.indexOf(message)).status = Message.FAILED;
						refreshListView();
					}
				} catch (Exception ignored) {
				}
			}
		};
		registerReceiver(broadcastReceiver, new IntentFilter("SENT_SMS_ACTION"));
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
			case R.id.action_import_CSV_file:
				Intent intentImport = new Intent(Intent.ACTION_GET_CONTENT);
				intentImport.setType("application/octet-stream");
				startActivityForResult(intentImport, REQUEST_IMPORT_DATA);
				return true;
			case R.id.action_paste_CSV:
				try {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					if(!clipboard.hasPrimaryClip() || !clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
						throw new Exception();
					}
					String CSVString = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
					List<Message> importing = CSVImporter.fromString(CSVString);
					this.list.addAll(importing);
					Toast.makeText(this, importing.size() + " messages imported.", Toast.LENGTH_SHORT).show();
				} catch (Exception ex) {
					Toast.makeText(this, "No valid CSV found in clipboard.", Toast.LENGTH_SHORT).show();
				}
				refreshListView();
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
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_IMPORT_DATA) {
				try {
					List<Message> importing = CSVImporter.fromFile(data.getData().getPath());
					this.list.addAll(importing);
					Toast.makeText(this, importing.size() + " messages imported.", Toast.LENGTH_SHORT).show();
				} catch (FileNotFoundException ex) {
					Toast.makeText(this, "File Not Found.", Toast.LENGTH_SHORT).show();
				} catch (Exception ex) {
					Toast.makeText(this, "Not a valid CSV file.", Toast.LENGTH_SHORT).show();
				}
				refreshListView();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}



	private void setListView() {
		listAdapter = new MessageArrayAdapter(this, list);
		listView.setAdapter(listAdapter);
	}

	private void refreshListView() {
		// Do not change reference of this.list otherwise notifyDataSetChanged() will not work
		listAdapter.notifyDataSetChanged();
	}

	private void sendSMS() {
		final ArrayList<Message> pendingList = new ArrayList<>();
		for (Message message : list) {
			if (!message.isSentOut()) {
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
		final ArrayList<Message> restList = new ArrayList<>();
		for (Message message : this.list)
			if (message.status != Message.SENT)
				restList.add(message);
		this.list.clear();
		this.list.addAll(restList);
		refreshListView();
	}
}