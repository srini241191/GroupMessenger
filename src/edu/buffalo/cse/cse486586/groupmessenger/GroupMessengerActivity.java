package edu.buffalo.cse.cse486586.groupmessenger;

import android.net.Uri;


import android.os.AsyncTask;


import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Hashtable;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.net.UnknownHostException;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

     static final String TAG = GroupMessengerActivity.class.getSimpleName();
     static final String PORTS[] = new String[]{"11124","11120","11116","11112","11108"};
     static final int SERVER_PORT = 10000;
     static final int  length = PORTS.length;
     
     
     //Referred from OnPTestClickListener.java//
     private ContentResolver mContentResolver;
     private Uri mUri =  buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");
     public String key,message1[],str1,message_part0, message_part1,msg;
     public int sequence;
     @Override
     
     
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        
        mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");
        
        mContentResolver=getContentResolver();
        
        //Log.d("SEQ",""+ seq);
        //Log.d("RG" ,""+r_g);
        
        
        
        
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        try{
            ServerSocket serverSocket=new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,serverSocket);
            }catch(IOException E){
            Log.e(TAG, "Can't create a ServerSocket");
            return;
            }
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
        
        
        
        final EditText edittext= (EditText)findViewById(R.id.editText1);
        final Button button=(Button) findViewById(R.id.button4);
        button.setOnClickListener(new OnClickListener() {
        @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
            try
            {
            String msg = edittext.getText().toString();
            edittext.setText("");
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msg,myPort);    
            }catch(NullPointerException E){
                Log.e(TAG,"NullPointerException");
            }
            }
        });
        
    }
    
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    
    
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
        ServerSocket serverSocket = sockets[0];

    	Hashtable<String, String> key_value = new Hashtable<String, String>();
    	int seq =0, r_g =0;
//The server accepts the incoming request from the client and sends them to the publishProgress() function//
            Socket client = null;
            while(true) //keep accepting connections for a while since Chat continues indefinitely//
            {
            try {

                    client= serverSocket.accept();
                    BufferedReader in = new BufferedReader(
                              new  InputStreamReader(client.getInputStream()));
                    str1=in.readLine();
                    message1 = str1.split("@@");
                    if ((message1.length)== 2)
                    {
                    message_part0 = message1[0];
                    Log.d("MESSAGE",message1[0]);
                    message_part1 = message1[1];
                    Log.d("ID",message1[1]);
                    //The message_part0 and message_part1 denote the key-value pair to be stored into the Hashtable//
                    //Insert every message into the Hashtable as the key, value pair//
                    key_value.put(message1[1], message1[0]);
                    
                    
                  
   //Since avd0 is the sequencer, then multicast each message to every avd//
                  if(hack().equals("11108"))
                  {
                  for(int i=0;i<length;i++)
                  {
                   
                   Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(PORTS[i]));
                   DataOutputStream os=new DataOutputStream(socket.getOutputStream());
                   OutputStreamWriter out1=new OutputStreamWriter(os);
                   out1.write("Ordered_Sequence"+"@@"+message_part1+"@@"+seq);
                   out1.close();
                   os.close();
                   socket.close();
                   }   
                    	seq++;
                   
                  }
              } 
                   
          else
           { 

           String key = message1[1];
           Log.d("KEY",key);
           int sequence = Integer.parseInt(message1[2]);
           Log.d("ORDER",""+sequence);
           while(true)
           {
        	   if(key_value.get(key) != null && sequence == r_g)
        	   {
        		   
        		   msg = key_value.get(key);
        		   Log.d("MESS_RETR",msg);
        		   key_value.remove(key);
        		   publishProgress(msg);
        		   ContentValues cv = new ContentValues();
        		   cv.put("key", String.valueOf(sequence));
        		   cv.put("value",msg );
        		   mContentResolver.insert(mUri,cv);
        		   r_g = sequence +1 ;
        		   break;
        	   }
        	 }
           
            
   }
                    } catch (Exception e) {
                    e.printStackTrace();
                }
             
            }
        }

 protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived+"\t\n");
            return;
        }
    }
    
private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String msg_to_multicast = msgs[0];
                UUID idOne = UUID.randomUUID();
                msg_to_multicast = msgs[0] + "@@" + idOne;
                Log.d("MESSAGE_TO_MULTICAST",msg_to_multicast);
                for(int i=0;i<length;i++)
                {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(PORTS[i]));
                DataOutputStream os=new DataOutputStream(socket.getOutputStream());
                OutputStreamWriter out1=new OutputStreamWriter(os);
                out1.write(msg_to_multicast);
                out1.flush();
                out1.close();
                os.close();
                socket.close();
                
            } 
            }catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
                e.printStackTrace();
            }

            return null; 
    }     
    }

public String hack()
{
	TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
    final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
    return myPort;
}
}