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

public class Application {

    private static final Object CONNECT_LOCK = new Object();
    private static final Object READ_LOCK = new Object();

    private static NotARealDebugger nard;

    public static void main( String[] args ) throws Exception {

        System.loadLibrary( "RIMUsbJni" );

        NardListener nardListener = new MyNardListener();

        Thread readThread = new Thread( new ReadThread() );
        readThread.start();

        ConnectThread connectThread = new ConnectThread();
        connectThread.nardListener = nardListener;
        Thread _connectThread = new Thread( connectThread );
        _connectThread.start();
    }

    private static final class MyNardListener implements NardListener {

        public void out( String msg ) {

            System.out.print( msg );
        }

        public void outln( String msg ) {

            System.out.println( msg );
        }

        public void err( String msg ) {

            System.err.print( msg );
        }

        public void errln( String msg ) {

            System.err.println( msg );
        }
    }

    private static final class ConnectThread implements Runnable {

        public NardListener nardListener;

        private void waitForClosedConnection() {

            synchronized (CONNECT_LOCK) {
                try {
                    CONNECT_LOCK.wait();
                }
                catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

        private void notifyReadThread() {

            synchronized (READ_LOCK) {
                READ_LOCK.notify();
            }
        }

        @Override
        public void run() {

            while (true) {
                if ( nard != null ) {
                    waitForClosedConnection();
                }
                NotARealDebugger nard = new NotARealDebugger( new TransportUSB() );
                nard.nardListener = nardListener;
                try {
                    boolean connected = nard.connect();
                    if ( !connected ) {
                        continue;
                    }
                }
                catch (Exception e) {
                    nard.close();
                    Thread.yield();
                    continue;
                }
                Application.nard = nard;
                notifyReadThread();
            }
        }
    }

    private static final class ReadThread implements Runnable {

        private void waitForOpenConnection() {

            synchronized (READ_LOCK) {
                try {
                    READ_LOCK.wait();
                }
                catch (InterruptedException e) {
                    // do nothing
                }
            }
        }

        private void notifyConnectThread() {

            synchronized (CONNECT_LOCK) {
                CONNECT_LOCK.notify();
            }
        }

        private void close() {

            if ( nard != null ) {
                nard.close();
                nard = null;
            }
        }

        public void run() {

            while (true) {
                if ( nard == null ) {
                    waitForOpenConnection();
                }
                try {
                    while (nard != null) {
                        nard.read();
                    }
                }
                catch (Exception e) {
                    // do nothing
                }
                finally {
                    close();
                }
                notifyConnectThread();
            }
        }
    }
}
