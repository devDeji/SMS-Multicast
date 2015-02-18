package com.yuzhiqiang.smsmulticast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class ImportActivity extends Activity {

	private EditText editTextFilePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);
		getViewers();
		setListeners();
	}

	private void getViewers() {
		editTextFilePath = (EditText) findViewById(R.id.editTextFilePath);

	}

	private void setListeners() {

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_import, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_import) {
			try {
				String filePath = editTextFilePath.getText().toString();
				ArrayList<Message> messages = importCSV(filePath);
				Intent intent = new Intent();
				intent.putExtra("imported_data", messages);
				this.setResult(1, intent);
				this.finish();
			} catch (FileNotFoundException ex) {
				Toast.makeText(this, "File Not Found.", Toast.LENGTH_SHORT).show();
			} catch (Exception ex) {
				Toast.makeText(this, "Not a valid CSV file.", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private ArrayList<Message> importCSV(String filePath) throws Exception {
		final FileReader fileReader = new FileReader(filePath);
		final CSVReader csvReader = new CSVReader(fileReader);
		final String[] header = csvReader.readNext();
		if (header.length != 2
				|| !header[0].equals("Phone number")
				|| !header[1].equals("Message")) {
			throw new Exception();
		}
		ArrayList<Message> list = new ArrayList<>();
		while (true) {
			String[] line = csvReader.readNext();
			if (line == null)
				break;
			if (line.length != 2)
				throw new Exception();
			final String phoneNumber = line[0];
			final String message = line[1];
			list.add(new Message(phoneNumber, message));
		}
		return list;
	}
}