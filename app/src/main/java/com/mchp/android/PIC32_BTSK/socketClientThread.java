package com.mchp.android.PIC32_BTSK;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Created by pjay on 2015/10/1.
 */
public class socketClientThread extends Thread {
    //Socket
    private static final String host = "140.116.156.227";
    private static final int port = 5508;
    private Socket socket = null;
    private String userIDstr = "N,0,";

    private static final int SEND_ID = 0;
    private static final int PASSING_DATA = 1;
    private int cur_state = SEND_ID;

    private DataInputStream input = null;
    private DataOutputStream output = null;
    private String inMsg;

    private BlockingQueue<String> dataQueue;
    private BlockingQueue<String> sendDataBTQueue;

    private Handler mHandler;

    public socketClientThread(String userID, BlockingQueue<String> dataQueue, Handler mHandler, BlockingQueue<String> sendDataBTQueue) {
        userIDstr += userID;
        this.dataQueue = dataQueue;
        this.mHandler = mHandler;
        this.sendDataBTQueue = sendDataBTQueue;
    }

    private void showMsgByToast(String msgStr)
    {
        // Send a message back to the UI Activity
        Message msg = mHandler.obtainMessage(PIC32_BTSK.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(PIC32_BTSK.TOAST, msgStr);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void sendMessageByBluetooth(String msgStr)
    {
        sendDataBTQueue.offer(msgStr);
    }

    private void parsingReceivedMessage(String msgStr)
    {
        if(PIC32_BTSK.D) Log.d("SOCKET", "Parsing msg: " + msgStr );
        String[] msgArray = msgStr.split(",");
        if (msgArray[0].equals("N") && msgArray[1].equals("2")) {
            //This message is N,2,"DATA STRING" which is a valid data sequence
            if (msgArray[2].equals("777")) {
                //DATA STRING is accelerometer values
                sendMessageByBluetooth(msgArray[2] + "," + msgArray[3] + "," + msgArray[4] + ","
                        + msgArray[5] + "," + msgArray[6]);
            }
        }
    }

    public void run() {
        try
        {
            showMsgByToast("Try to connect to server...");

                    socket = new Socket( host, port );
            input = new DataInputStream( socket.getInputStream() );
            output = new DataOutputStream( socket.getOutputStream() );

            showMsgByToast("Connected to server!");

            //Super Loop
            while ( socket.isConnected() )
            {
                switch (cur_state) {

                    case SEND_ID:
                        if(PIC32_BTSK.D) Log.d("SOCKET", "Send: " + userIDstr);
                        output.writeUTF(userIDstr);
                        output.flush();
                        inMsg = input.readUTF();
                        if(PIC32_BTSK.D) Log.d("SOCKET", "Recevice: " + inMsg );
                        if (inMsg.equals("N,1,ok"))
                            cur_state = PASSING_DATA;
                        break;
                    case PASSING_DATA:
                        if (!dataQueue.isEmpty()) {
                            String msg = dataQueue.poll();
                            if(PIC32_BTSK.D) Log.d("SOCKET", "Send: N,2," + msg );
                            output.writeUTF("N,2," + msg);
                            output.flush();
                        }
                        if (input.available() > 0) {
                            inMsg = input.readUTF();
                            parsingReceivedMessage(inMsg);
                            if(PIC32_BTSK.D) Log.d("SOCKET", "Recevice: " + inMsg );
                        }
                        Thread.sleep(10);
                        break;
                    default:;
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try {
                if ( input != null ) input.close();
                if ( output != null ) output.close();
                if ( socket != null ) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            showMsgByToast("Connection lost");
        }
    }

    public void cancel() {
        try {
            if ( input != null ) input.close();
            if ( output != null ) output.close();
            if ( socket != null ) socket.close();
            if(PIC32_BTSK.D) Log.d("SOCKET", "Connection closed");
            showMsgByToast("Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
