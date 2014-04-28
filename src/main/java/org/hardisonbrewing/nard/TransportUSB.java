/**
 * Copyright (c) 2011 Martin M Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hardisonbrewing.nard;

import java.io.IOException;
import java.util.Vector;

import net.rim.usb.USBConnection;

public class TransportUSB implements Transport {

    private final byte[] CACHE = new byte[Short.MAX_VALUE];
    private Vector<Byte> byteBuffer = new Vector<Byte>();

    private USBConnection usbConnection;

    public boolean connect() throws IOException {

        int[] attachedDevices = USBConnection.getAttachedDevices();
        if ( attachedDevices.length == 0 ) {
            return false;
        }

        for (int i = 0; i < attachedDevices.length; i++) {
            String pin = Integer.toHexString( attachedDevices[0] ).toUpperCase();
            System.out.println( "Device[" + pin + "] Simulator[" + USBConnection.isSimulator( attachedDevices[i] ) + "]" );
        }

        USBConnection usbConnection = new USBConnection();
        boolean opened = usbConnection.openChannel( "RIM_JVMDebug", attachedDevices[0], null );

        if ( !opened ) {
            return false;
        }

        this.usbConnection = usbConnection;
        return true;
    }

    public void close() {

        if ( usbConnection != null ) {
            try {
                usbConnection.closeChannel();
            }
            catch (Exception e) {
                // do nothing
            }
            usbConnection = null;
        }
    }

    public void writeByte( int i ) throws IOException {

        byte[] b = new byte[] { (byte) ( i & 0xff ) };
        usbConnection.sendPacket( b, 0, b.length );
    }

    public void flush() throws IOException {

    }

    public int readByte() throws IOException {

        if ( byteBuffer.isEmpty() ) {
            int size = usbConnection.receivePacket( CACHE );
            if ( size <= 0 ) {
                return -1;
            }
            for (int i = 0; i < size; i++) {
                byteBuffer.add( CACHE[i] );
            }
        }
        return byteBuffer.remove( 0 );
    }
}
