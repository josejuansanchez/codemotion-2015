package org.josejuansanchez.mqtt_ibm_bluemix;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import constants.Bluemix;

public class MainActivity extends AppCompatActivity {

    private UsbSerialDevice serial; ;
    private TextView fromArduino;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fromArduino = (TextView) findViewById(R.id.fromArduino);
        fromArduino.setMovementMethod(new ScrollingMovementMethod());

        initSerial();
    }

    private void initSerial() {

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        serial = SerialUtil.findSerial(manager);

        if (serial == null) {
            Toast.makeText(MainActivity.this, "No he encontrado ningún cacharro serial :(", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!serial.open()) {
            Toast.makeText(MainActivity.this, "Fallo al abrir el puerto serial", Toast.LENGTH_SHORT).show();
            return;
        }

        serial.read(onRead);
        serial.setBaudRate(9600);
        serial.setParity(UsbSerialInterface.PARITY_NONE);
        serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
        serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
        serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

    }

    private UsbSerialInterface.UsbReadCallback onRead = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            final String json = new String(bytes);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fromArduino.setText(fromArduino.getText() + json);
                }
            });

            // Send data to Bluemix
            sendDataToBluemix(json);
        }
    };

    public void sendDataToBluemix(String payload) {
        //String payload = "{\"humidity\":\"" + value + "\"}";
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
