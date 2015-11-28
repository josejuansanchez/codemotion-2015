package org.josejuansanchez.makey_makey_ibm_bluemix;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.josejuansanchez.makey_makey_ibm_bluemix.constants.Bluemix;

public class MainActivity extends AppCompatActivity implements KeyEvent.Callback  {

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextView = (TextView) findViewById(R.id.textview);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                mTextView.setText("UP");
                sendDataToBluemix("up");
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                mTextView.setText("DOWN");
                sendDataToBluemix("down");
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mTextView.setText("LEFT");
                sendDataToBluemix("left");
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mTextView.setText("RIGHT");
                sendDataToBluemix("right");
                return true;
            case KeyEvent.KEYCODE_SPACE:
                mTextView.setText("SPACE");
                sendDataToBluemix("space");
                return true;
            default:
                mTextView.setText("OTHER: " + keyCode);
                sendDataToBluemix("other");
                return super.onKeyUp(keyCode, event);
        }
    }

    public void sendDataToBluemix(String value) {
        String payload = "{\"key\":\"" + value + "\"}";
        doMQTTAction(Bluemix.uri, Bluemix.topic, Bluemix.clientId, Bluemix.username, Bluemix.password, payload);
    }

    private void doMQTTAction(final String uri, final String topic, final String clientId,
                              final String username, final String password, final String payload) {
        new Thread(new Runnable() {
            public void run() {

                MqttClient client = null;
                try {
                    // Note: MqttClient only accept tcp, ssl or local
                    client = new MqttClient(uri, clientId, null);
                    client.setCallback(new ExampleCallBack());
                } catch (MqttException e1) {
                    e1.printStackTrace();
                }

                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(username);
                options.setPassword(password.toCharArray());

                try {
                    client.connect(options);
                } catch (MqttException e) {
                    Log.d(getClass().getCanonicalName(), "Connection attempt failed with reason code = " + e.getReasonCode() + ":" + e.getCause());
                }

                try {
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setPayload(payload.getBytes());
                    client.publish(topic, mqttMessage);
                    client.disconnect();
                }
                catch (MqttException e) {
                    Log.d(getClass().getCanonicalName(), "Publish failed with reason code = " + e.getReasonCode());
                }
            }
        }).start();
    }

    public class ExampleCallBack implements MqttCallback
    {
        public void connectionLost(Throwable cause)
        {
            Log.d(getClass().getCanonicalName(), "MQTT Server connection lost" + cause.toString());
        }
        public void messageArrived(String topic, MqttMessage message)
        {
            Log.d(getClass().getCanonicalName(), "Message arrived:" + topic + ":" + message.toString());
        }
        public void deliveryComplete(IMqttDeliveryToken token)
        {
            Log.d(getClass().getCanonicalName(), "Delivery complete");
        }
    }



}
