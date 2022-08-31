package com.givevision.livecontrolproject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.givevision.livecontrolproject.db.wifiLogs.WifiLogs;
import com.givevision.livecontrolproject.db.wifiLogs.WifiLogsRepository;
import com.givevision.livecontrolproject.log.Pojo;
import com.givevision.livecontrolproject.util.Constants;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * socket dialog with kits
 *
 */

public class TcpServerSocket {
    private static final String TAG = "TcpServerSocket";
    //Server side
    private static final int LOCAL_PORT = 6000;
    private java.net.ServerSocket serverSocket;
    private Handler updateConversationHandler;
    private Thread serverThread;
    private List<Socket> sockets=new ArrayList<Socket>();
    private Socket socket;
    private WifiLogsRepository wifiLogsRepository;
    private Context context;
    private boolean isRoot;
    Timer pingTimer;

    public TcpServerSocket(Context ctx) {
        context=ctx;
        wifiLogsRepository = new WifiLogsRepository(ctx);
        try {
            Process root = Runtime.getRuntime().exec("su");
            Log.v(TAG, "aua permission ok");
            isRoot=true;
        } catch (IOException e) {
            isRoot=false;
            Log.e(TAG, "aua IOException "+e.toString());
        }
    }

    public void start() {
        LogManagement.Log_d(TAG, "ServerThread start");

        updateConversationHandler = new Handler();
        serverThread = new Thread(new ServerThread());
        serverThread.start();
        if(isRoot){
            pingKit();
        }
    }

    public void stop() {
        LogManagement.Log_d(TAG, "serverSocket stop");
        pingTimer.cancel();
        pingTimer.purge();
        serverThread.interrupt();
    }

    public void checkKit(int i) {
        String ipAddress="";
        switch(i){
            case 1:
                ipAddress="192.168.1.11";
                break;
            case 2:
                ipAddress="192.168.1.12";
                break;
            case 3:
                ipAddress="192.168.1.13";
                break;
            case 4:
                ipAddress="192.168.1.14";
                break;
            case 5:
                ipAddress="192.168.1.15";
                break;
            case 6:
                ipAddress="192.168.1.16";
                break;
            case 7:
                ipAddress="192.168.1.17";
                break;
            case 8:
                ipAddress="192.168.1.18";
                break;
            case 9:
                ipAddress="192.168.1.19";
                break;
            default:
                break;
        }
        LogManagement.Log_d(TAG, "checkKit kit= "+ipAddress);
        Pojo pojo=new Pojo();
        java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
        pojo.setPojo(ipAddress,Constants.ACTION_CHECK, Constants.MSG_CHECK_KIT, Constants.ACTION_TYPE_SOCKET,date );
        Intent intent = new Intent();
        intent.setAction("com.givevision.livecontrolproject");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("pojo", pojo.toJSON());
        context.sendBroadcast(intent);
    }

    public void resetKit(String ipAddress) {
        LogManagement.Log_d(TAG, "resetKit kit= "+ipAddress+" sockets.size="+sockets.size());
        for(int i=0; i<sockets.size();i++){
            LogManagement.Log_d(TAG, "resetKit socket InetAddress= "+sockets.get(i).getInetAddress());
            if(sockets.get(i).getInetAddress().toString().contains(ipAddress)){
                Socket s=sockets.get(i);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            DataHandler dataHandler = new DataHandler();
                            dataHandler.resetKit(s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                break;
            }
        }
    }

    class ServerThread implements Runnable {
        public void run() {
            socket = null;
            try {
                LogManagement.Log_d(TAG, "serverSocket start");
                serverSocket = new java.net.ServerSocket(LOCAL_PORT);
            } catch (IOException e) {
                LogManagement.Log_e(TAG, "ServerThread serverSocket error"+e);
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
//                    CommunicationThread commThread = new CommunicationThread(socket);
//                    new Thread(commThread).start();
                    socket = serverSocket.accept();//
                    LogManagement.Log_v(TAG, "ServerThread connection Established= "+socket.getInetAddress()+
                            "  sockets.size="+sockets.size());
                    boolean isActive=false;
                    for(int i=0; i<sockets.size();i++){
                        LogManagement.Log_v(TAG, "ServerThread connection saved i="+i+
                                sockets.get(i).getInetAddress());
                        if(sockets.size()>0 && sockets.get(i).getInetAddress().equals(socket.getInetAddress())){
                            sockets.get(i).close();
//                            sockets.remove(i);
                            sockets.set(i,socket);
                            isActive=true;
                            break;
                        }else{
                            isActive=false;
                        }
                    }
                    Reciever receiver=new Reciever(socket);
                    receiver.start();
                    if(!isActive){
                        sockets.add(socket);
                    }

                    LogManagement.Log_v(TAG, "ServerThread connection added "+socket.getInetAddress()+
                            " sockets.size=" +sockets.size());
                } catch (IOException e) {
                    LogManagement.Log_e(TAG, "ServerThread IOException"+e);
                }
            }
        }
    }

    class Reciever extends Thread{
        String line=null;
        BufferedReader input = null;
        PrintWriter output=null;
        Socket socket;
        String ipAddress;
        public Reciever(Socket s){
            this.socket=s;
            ipAddress=socket.getInetAddress().getHostAddress();
        }
        public void run() {
            ArrayList<String> wifiMsg = new ArrayList<>();
            LogManagement.Log_d(TAG, "Receiver rum : ");
            try {
                input= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output=new PrintWriter(socket.getOutputStream());
                Pojo pojo=new Pojo();
                line=input.readLine();
                while(!socket.isClosed() && !pojo.setPojo(line)&& !Thread.currentThread().isInterrupted()){
                    LogManagement.Log_d(TAG, "Receiver while 1 input.ready()="+input.ready()+
                            line);
                    line=input.readLine();
                }
                while(!socket.isClosed() && !pojo.getAction().contains("QUIT") && !Thread.currentThread().isInterrupted()){
                    output.println(line);
                    output.flush();
                    if(pojo.getAction().contains("syncDb")){
                        DataHandler dataHandler = new DataHandler();
                        dataHandler.syncDb(socket);
                        break;
                    }else{
                        LogManagement.Log_d(TAG, "Receiver from Client : "+pojo.toJSON());
                        SimpleDateFormat sdf= new SimpleDateFormat(Constants.formats[3], Locale.UK);
                        TimeZone timeZone = TimeZone.getDefault();
                        sdf.setTimeZone(timeZone);
                        try {
                            Date date = sdf.parse(pojo.getCreated());
                            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
//                            wifiLogsRepository.insertWifiLogs(pojo.getMessage(),sqlDate);
                        } catch (ParseException e) {
                            LogManagement.Log_e(TAG, "Receiver ParseException : "+e);
                        }
                        Intent intent = new Intent();
                        intent.setAction("com.givevision.livecontrolproject");
                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        intent.putExtra("pojo", pojo.toJSON());
                        context.sendBroadcast(intent);
                        line=input.readLine();
                        if(!pojo.setPojo(line)&& !Thread.currentThread().isInterrupted()){
                            LogManagement.Log_d(TAG, "Receiver while 2 input.ready()="+input.ready());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                line=this.getName(); //reused String line for getting thread name
                LogManagement.Log_e(TAG, "Receiver IO Error/ Client "+line+" terminated abruptly");
            }catch(NullPointerException e){
                line=this.getName(); //reused String line for getting thread name
                LogManagement.Log_e(TAG, "Receiver Client "+line+" Closed");
            }finally{
                try{
                    LogManagement.Log_d(TAG, "Receiver Connection Closing.. socket="+
                            socket.getInetAddress());
                    sockets.remove(socket);
                    if (input!=null){
                        input.close();
                        LogManagement.Log_d(TAG, "Receiver  Input Closed");
                    }
                    if(output!=null){
                        output.close();
                        LogManagement.Log_d(TAG, "Receiver Output Closed");
                    }
                    if (socket!=null){
                        socket.close();
                        LogManagement.Log_d(TAG, "Receiver Socket Closed");
                    }
                }catch(IOException ie){
                    LogManagement.Log_e(TAG, "Receiver Socket Close Error");
                }
                Pojo pojo=new Pojo();
                java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
                pojo.setPojo(ipAddress,Constants.ACTION_ERROR, Constants.MSG_LOST_CONNECTION, Constants.ACTION_TYPE_SOCKET,date );
                Intent intent = new Intent();
                intent.setAction("com.givevision.livecontrolproject");
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent.putExtra("pojo", pojo.toJSON());
                context.sendBroadcast(intent);
            }//end finally
        }
    }

    public class Sender {
        private PrintWriter out;
        public Sender(Socket clientSocket) {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                LogManagement.Log_e(TAG, "Sender IO error in server thread");
            }
        }
        public void sendMessage(String message) {
            LogManagement.Log_d(TAG, "Server: " + message + "\n");
            out.println(message); // Print the message on output stream.
            out.flush();
        }
    }

    public class DataHandler {
        public void syncDb(Socket s) {
            Sender sender = new Sender(s);
            sender.sendMessage("***start***");
            sender.sendMessage("This is a message from the server");
            sender.sendMessage("***stop***");
        }
        public void resetKit(Socket s) {
            Sender sender = new Sender(s);
            sender.sendMessage(Constants.ACTION_RESET);
        }
    }

    private int execute_as_root( String[] commands ) {
        try {
            // Do the magic
            Process p = Runtime.getRuntime().exec( "su" );
            InputStream es = p.getErrorStream();
            DataOutputStream os = new DataOutputStream(p.getOutputStream());

            for( String command : commands ) {
                os.writeBytes(command + "\n");
                Log.i(TAG, "checkCmd :: command "+command);
            }
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            Log.i(TAG, "checkCmd :: os.close() OK ");
            int read;
            byte[] buffer = new byte[4096];
            String output = new String();
            while ((read = es.read(buffer)) > 0) {
                output += new String(buffer, 0, read);
            }
            Log.i(TAG, "checkCmd :: output "+output);
            p.waitFor();

            return p.exitValue();
        } catch (IOException e) {
            Log.e(TAG, "checkCmd :: IOException"+e);
            return -2;
        }
        catch (InterruptedException e) {
            Log.e(TAG, "checkCmd :: InterruptedException"+e);
            return -3;
        }
    }

    public void pingKit(){
        pingTimer=new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runcCMD("192.168.1.10", new String[]{"ping -c 1  " + "192.168.1.10" + " \n",});
                runcCMD("192.168.1.11", new String[]{"ping -c 1  " + "192.168.1.11" + " \n",});
                runcCMD("192.168.1.12", new String[]{"ping -c 1  " + "192.168.1.12" + " \n",});
                runcCMD("192.168.1.13", new String[]{"ping -c 1  " + "192.168.1.13" + " \n",});
                runcCMD("192.168.1.14", new String[]{"ping -c 1  " + "192.168.1.14" + " \n",});
                runcCMD("192.168.1.15", new String[]{"ping -c 1  " + "192.168.1.15" + " \n",});
                runcCMD("192.168.1.16", new String[]{"ping -c 1  " + "192.168.1.16" + " \n",});
                runcCMD("192.168.1.17", new String[]{"ping -c 1  " + "192.168.1.17" + " \n",});
                runcCMD("192.168.1.18", new String[]{"ping -c 1  " + "192.168.1.18" + " \n",});
                runcCMD("192.168.1.19", new String[]{"ping -c 1  " + "192.168.1.19" + " \n",});
                runcCMD("192.168.1.1", new String[]{"ping -c 1  " + "192.168.1.1" + " \n",});
                runcCMD("192.168.1.168", new String[]{"ping -c 1  " + "192.168.1.168" + " \n",});
            }

        }, 0, 2000);
    }

    private void runcCMD(String ipAddress,String[] commands) {
        Pojo pojo=new Pojo();
        java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
        if (execute_as_root(commands) == 0) {
            LogManagement.Log_d(TAG, "executeCmde true for= "+ipAddress);
            pojo.setPojo(ipAddress,Constants.ACTION_OK, Constants.MSG_NETWORK_CONNECTED, Constants.ACTION_TYPE_PING,date );
            Intent intent = new Intent();
            intent.setAction("com.givevision.livecontrolproject");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("pojo", pojo.toJSON());
            context.sendBroadcast(intent);
        }else{
            LogManagement.Log_e(TAG, "executeCmde false for= "+ipAddress);
            pojo.setPojo(ipAddress,Constants.ACTION_ERROR, Constants.MSG_LOST_CONNECTION, Constants.ACTION_TYPE_PING,date );
            Intent intent = new Intent();
            intent.setAction("com.givevision.livecontrolproject");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("pojo", pojo.toJSON());
            context.sendBroadcast(intent);
        }
    }
}
