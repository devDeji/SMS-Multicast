package com.yuzhiqiang.smsmulticast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
                ArrayList<Message> messages = importDataFile(filePath);
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

    private ArrayList<Message> importDataFile(String filePath) throws Exception {
        ArrayList<Message> list = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(filePath);
        Scanner scanner = new Scanner(fileInputStream);
        try {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                int comma = line.indexOf(',');
                int semicolon = line.indexOf(';');
                int splitLocation;
                if (comma != -1 && semicolon != -1)
                    splitLocation = Math.min(comma, semicolon);
                else if (comma == -1 && semicolon != -1)
                    splitLocation = semicolon;
                else if (comma != -1 && semicolon == -1)
                    splitLocation = comma;
                else
                    throw new Exception();
                String destination = line.substring(0, splitLocation);
                String content = line.substring(splitLocation + 1);
                Message message = new Message(destination, content);
                list.add(message);
            }
            list.add(new Message("10086", "cd1"));
            list.add(new Message("10086", "cd2"));
            list.add(new Message("10086", "cd3"));
        } finally {
            scanner.close();
        }
        return list;
    }
}