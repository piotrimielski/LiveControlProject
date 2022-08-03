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
    private List<Socket> sockets;
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
        sockets=new List<Socket>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<Socket> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] ts) {
                return null;
            }

            @Override
            public boolean add(Socket socket) {
                return false;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends Socket> collection) {
                return false;
            }

            @Override
            public boolean addAll(int i, @NonNull Collection<? extends Socket> collection) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public Socket get(int i) {
                return null;
            }

            @Override
            public Socket set(int i, Socket socket) {
                return null;
            }

            @Override
            public void add(int i, Socket socket) {

            }

            @Override
            public Socket remove(int i) {
                return null;
            }

            @Override
            public int indexOf(@Nullable Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(@Nullable Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<Socket> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<Socket> listIterator(int i) {
                return null;
            }

            @NonNull
            @Override
            public List<Socket> subList(int i, int i1) {
                return null;
            }
        };
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
                    LogManagement.Log_v(TAG, "ServerThread connection Established= "+socket.getInetAddress());
                    boolean isActive=false;
                    for(int i=0; i<sockets.size();i++){
                        if(sockets.get(i).getInetAddress().equals(socket.getInetAddress())){
                            isActive=true;
                        }
                        LogManagement.Log_v(TAG, "ServerThread connection saved= "+sockets.get(i));
                    }
                    if(!isActive){
                        Reciever receiver=new Reciever(socket);
                        receiver.start();
                        sockets.add(socket);
                    }
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
            try {
                input= new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output=new PrintWriter(socket.getOutputStream());
                Pojo pojo=new Pojo();
                line=input.readLine();
                while(!pojo.setPojo(line)&& !Thread.currentThread().isInterrupted()){
                    LogManagement.Log_d(TAG, "while 1 input.ready()="+input.ready());
                    line=input.readLine();
                }
                while(!pojo.getAction().contains("QUIT") && !Thread.currentThread().isInterrupted()){
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
                            LogManagement.Log_e(TAG, "ParseException : "+e);
                        }
                        Intent intent = new Intent();
                        intent.setAction("com.givevision.livecontrolproject");
                        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        intent.putExtra("pojo", pojo.toJSON());
                        context.sendBroadcast(intent);
                        line=input.readLine();
                        if(!pojo.setPojo(line)&& !Thread.currentThread().isInterrupted()){
                            LogManagement.Log_d(TAG, "while 2 input.ready()="+input.ready());
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                line=this.getName(); //reused String line for getting thread name
                LogManagement.Log_e(TAG, "Reciever IO Error/ Client "+line+" terminated abruptly");
            }catch(NullPointerException e){
                line=this.getName(); //reused String line for getting thread name
                LogManagement.Log_e(TAG, "Reciever Client "+line+" Closed");
            }finally{
                try{
                    LogManagement.Log_d(TAG, "Connection Closing..");
                    sockets.remove(socket);
                    if (input!=null){
                        input.close();
                        LogManagement.Log_d(TAG, "Reciever Input Closed");
                    }
                    if(output!=null){
                        output.close();
                        LogManagement.Log_d(TAG, "Reciever Output Closed");
                    }
                    if (socket!=null){
                        socket.close();
                        LogManagement.Log_d(TAG, "Reciever Socket Closed");
                    }
                }catch(IOException ie){
                    LogManagement.Log_e(TAG, "Reciever Socket Close Error");
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
            LogManagement.Log_d(TAG, "Server: " + message + "\n");// Print the message on chat window.
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
            LogManagement.Log_d(TAG, "executeCmde true");
            pojo.setPojo(ipAddress,Constants.ACTION_OK, Constants.MSG_LOST_CONNECTION, Constants.ACTION_TYPE_PING,date );
            Intent intent = new Intent();
            intent.setAction("com.givevision.livecontrolproject");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("pojo", pojo.toJSON());
            context.sendBroadcast(intent);
            LogManagement.Log_e(TAG, "executeCmde false for= "+ipAddress);
        }else{
            pojo.setPojo(ipAddress,Constants.ACTION_ERROR, Constants.MSG_LOST_CONNECTION, Constants.ACTION_TYPE_PING,date );
            Intent intent = new Intent();
            intent.setAction("com.givevision.livecontrolproject");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("pojo", pojo.toJSON());
            context.sendBroadcast(intent);
            LogManagement.Log_e(TAG, "executeCmde false for= "+ipAddress);
        }
    }
}
