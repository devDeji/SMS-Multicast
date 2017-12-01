package org.yuzhiqiang.smsmulticast

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.yuzhiqiang.smsmulticast.R
import java.io.FileNotFoundException
import java.util.*

class MainActivity : Activity() {
    // Do not change reference of list otherwise notifyDataSetChanged() will not work
    private val list = ArrayList<Message>()
    private val handler = Handler()
    internal var listAdapter: ArrayAdapter<*>
    private var broadcastReceiver: BroadcastReceiver? = null
    private var listView: ListView? = null

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.listView) as ListView
        setupBroadcastReceiver()
        setListView()
    }

    private fun setupBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    if (intent.action == "SENT_SMS_ACTION") {
                        val message = intent.extras!!.get("sent_message") as Message
                        Toast.makeText(this@MainActivity, "SENT: " + message.destination + "(" + list.indexOf(message) + ")", Toast.LENGTH_SHORT).show()
                        if (resultCode == Activity.RESULT_OK)
                            if (list[list.indexOf(message)].status != Message.FAILED)
                                list[list.indexOf(message)].status = Message.SENT
                            else
                                list[list.indexOf(message)].status = Message.FAILED
                        refreshListView()
                    }
                } catch (ignored: Exception) {
                }

            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("SENT_SMS_ACTION"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val intent = Intent()
        when (item.itemId) {
            R.id.action_import_CSV_file -> {
                val intentImport = Intent(Intent.ACTION_GET_CONTENT)
                intentImport.type = "application/octet-stream"
                startActivityForResult(intentImport, REQUEST_IMPORT_DATA)
                return true
            }
            R.id.action_paste_CSV -> {
                try {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    if (!clipboard.hasPrimaryClip() || !clipboard.primaryClipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        throw Exception()
                    }
                    val CSVString = clipboard.primaryClip.getItemAt(0).text.toString()
                    val importing = CSVImporter.fromString(CSVString)
                    this.list.addAll(importing)
                    Toast.makeText(this, importing.size.toString() + " messages imported.", Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) {
                    Toast.makeText(this, "No valid CSV found in clipboard.", Toast.LENGTH_SHORT).show()
                }

                refreshListView()
                return true
            }
            R.id.action_send -> {
                sendSMS()
                return true
            }
            R.id.action_clear_sent -> {
                clearListSent()
                return true
            }
            R.id.action_clear_all -> {
                list.clear()
                refreshListView()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMPORT_DATA) {
                try {
                    val importing = CSVImporter.fromFile(data.data!!.path)
                    this.list.addAll(importing)
                    Toast.makeText(this, importing.size.toString() + " messages imported.", Toast.LENGTH_SHORT).show()
                } catch (ex: FileNotFoundException) {
                    Toast.makeText(this, "File Not Found.", Toast.LENGTH_SHORT).show()
                } catch (ex: Exception) {
                    Toast.makeText(this, "Not a valid CSV file.", Toast.LENGTH_SHORT).show()
                }

                refreshListView()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun setListView() {
        listAdapter = MessageArrayAdapter(this, list)
        listView!!.adapter = listAdapter
    }

    private fun refreshListView() {
        // Do not change reference of this.list otherwise notifyDataSetChanged() will not work
        listAdapter.notifyDataSetChanged()
    }

    private fun sendSMS() {
        val pendingList = ArrayList<Message>()
        for (message in list) {
            if (!message.isSentOut) {
                message.status = Message.PENDING
                pendingList.add(message)
            }
        }
        Toast.makeText(this, "Sending " + list.size + " messages.", Toast.LENGTH_SHORT).show()
        val sms = SmsManager.getDefault()
        object : Thread() {
            override fun run() {
                super.run()
                for (message in pendingList) {
                    handler.post { Toast.makeText(this@MainActivity, "Sending: " + message.destination + "(" + list.indexOf(message) + ")", Toast.LENGTH_SHORT).show() }
                    val sentIntent = Intent("SENT_SMS_ACTION")
                    sentIntent.putExtra("sent_message", message)
                    val sentPendingIntent = PendingIntent.getBroadcast(this@MainActivity, list.indexOf(message), sentIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                    val contents = sms.divideMessage(message.content)
                    val PendingIntents = ArrayList<PendingIntent>()
                    for (i in contents.indices)
                        PendingIntents.add(sentPendingIntent)
                    sms.sendMultipartTextMessage(message.destination, null, contents, PendingIntents, null)
                    message.status = Message.SENDING
                    handler.post { refreshListView() }
                    try {
                        Thread.sleep(4000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
        }.start()

    }

    private fun clearListSent() {
        val restList = ArrayList<Message>()
        for (message in this.list)
            if (message.status != Message.SENT)
                restList.add(message)
        this.list.clear()
        this.list.addAll(restList)
        refreshListView()
    }

    companion object {

        private val REQUEST_IMPORT_DATA = 0
    }
}