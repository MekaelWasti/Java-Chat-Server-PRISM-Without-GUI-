package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private BufferedWriter bw;
    //    public String message = null;
    public String message = "";
    public Socket s;
    public ArrayList<Socket> listOfClients = new ArrayList<Socket>();

    public void start() throws IOException {
        //Socket

        ServerSocket ss = new ServerSocket(63030);

        Thread thread = new Thread(() -> {
//        System.out.println("New Thread \"thread\"");
            AtomicInteger count = new AtomicInteger();
            while (true) {
                try {
                    s = ss.accept();
                    listOfClients.add(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                InetSocketAddress sockaddress = (InetSocketAddress)s.getRemoteSocketAddress();
                InetAddress inaddr = sockaddress.getAddress();
                Inet4Address in4address = (Inet4Address)inaddr;
                byte[] ip4bytes = in4address.getAddress(); // returns byte[4]
                String ip4string = in4address.toString();
                Socket finalS = s;
                System.out.println("Client Connected: " + ip4string);

                Thread thread1 = new Thread(() -> {
                    count.getAndIncrement();
                    System.out.println("New Thread \"thread1\"" + count);
                    while (finalS.isConnected()) {


                        BufferedReader br = null;

                        try {
                            br = new BufferedReader(new InputStreamReader(finalS.getInputStream()));
                            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

                            message = br.readLine();
                            System.out.println("Message is: " + message);
                            writeMessage(finalS);
                            System.out.println(this.message);

                        } catch (IOException e) {
                            removeClient(finalS);
                            try {
                                finalS.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            //e.printStackTrace();
                            //try {
                            //    br.close();
                            //    finalS.close();
                            //} catch (IOException ex) {
                            //    ex.printStackTrace();
                            //}
                        }
                    }
                });

                thread1.start();
            }
        });
        thread.start();
    }

    public void writeMessage(Socket s) throws IOException {
        try {
            //Send to each client
            for (int i = 0; i < listOfClients.size(); i++) {
                System.out.println(listOfClients.get(i).getRemoteSocketAddress());
                if (!Objects.equals(listOfClients.get(i).getRemoteSocketAddress().toString(), s.getRemoteSocketAddress().toString())) {
                    bw = new BufferedWriter(new OutputStreamWriter(listOfClients.get(i).getOutputStream()));
                    bw.write(message);
                    bw.newLine();
                    bw.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void removeClient(Socket s) {
        listOfClients.remove(s);
    }

}