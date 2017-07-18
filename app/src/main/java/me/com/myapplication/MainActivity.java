package me.com.myapplication;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements AddItemDialog.ItemDialogListener, MqttCallbackExtended {

    final static String LOG_TAG = MainActivity.class.getSimpleName();
    MqttAndroidClient client;
    ListView mListView;
    ArrayAdapter adapter;
    List<String> listItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("MQTT - Demo");
        setSupportActionBar(toolbar);


        mListView = (ListView) findViewById(R.id.stuff);
        listItems.add("Item 1");
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems);
        mListView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddItemDialog d = new AddItemDialog();
                d.show(getFragmentManager(), "Add Item");
            }
        });

        try {
            String brokerUri = "tcp://10.192.220.226:1883";

            client = new MqttAndroidClient(getApplicationContext(), brokerUri, getDeviceIdentifier());
            client.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            client.connect(options);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String itemName) {
        try {

            MqttMessage mqttMsg = new MqttMessage(itemName.getBytes());
            mqttMsg.setQos(2);
            client.publish("items", mqttMsg);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.d(LOG_TAG, "connectComplete");
        try {
            client.subscribe("items", 2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(LOG_TAG, "messageArrived");
        String item = new String(message.getPayload());
        listItems.add(item);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private String getDeviceIdentifier() {
        return UUID.randomUUID().toString();
    }
}
