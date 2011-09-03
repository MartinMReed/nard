/**
 * Copyright (c) 2011 Martin M Reed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
