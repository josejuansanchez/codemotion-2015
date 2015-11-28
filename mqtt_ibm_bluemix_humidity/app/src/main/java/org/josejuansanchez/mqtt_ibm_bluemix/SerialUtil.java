package org.josejuansanchez.mqtt_ibm_bluemix;


import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.felhr.usbserial.UsbSerialDevice;

import java.util.HashMap;

public class SerialUtil {


    public static UsbSerialDevice findSerial(UsbManager manager) {

        HashMap<String, UsbDevice> devices = manager.getDeviceList();


        for (UsbDevice device : devices.values()) {
            UsbDeviceConnection connection = manager.openDevice(device);
            UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serial != null)
                return serial;
        }



        return null;
    }

}
