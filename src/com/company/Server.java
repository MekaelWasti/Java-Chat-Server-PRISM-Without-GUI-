package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private BufferedWriter bw;
    public String message = "";
    public String finalMessage = "";
    public String address = "";
    public String sendingUser = "";
    public Socket s;

    public HashMap<String,Socket> listOfUsers = new HashMap<String,Socket>();

    public void start() throws IOException {
        //Set Up Server GUI Elements

        //Socket
        ServerSocket ss = new ServerSocket(63030);
        Thread thread = new Thread(() -> {
            //System.out.println("New Thread \"thread\"");
            //AtomicInteger count = new AtomicInteger();
            while (true) {


                try {
                    s = ss.accept();
                    BufferedReader br = new BufferedReader((new InputStreamReader(s.getInputStream())));

                    //Get Username
                    sendingUser = br.readLine();
                    listOfUsers.put(sendingUser, s);

                } catch (IOException e) {
                    e.printStackTrace();
                }


                Socket finalS = s;
                System.out.println("Client Connected: " + sendingUser + " - Address: " + s.getRemoteSocketAddress());

                Thread thread1 = new Thread(() -> {
                    //count.getAndIncrement();
                    //System.out.println("New Thread \"thread1\"" + count);
                    while (finalS.isConnected()) {

                        BufferedReader br = null;
                        try {
                            br = new BufferedReader((new InputStreamReader(finalS.getInputStream())));
                            bw = new BufferedWriter(new OutputStreamWriter(finalS.getOutputStream()));

                            String line;
                            int i = 0;
                            while ((i != 3)) {
                                line = br.readLine();
                                if (i == 0) {
                                    address = line;
                                } else if (i == 1) {
                                    sendingUser = line;
                                } else if (i == 2) {
                                    finalMessage = line;
                                }
                                i++;
                            }

                            System.out.println("Sending: " + finalMessage + " - From: " + sendingUser + " - To: " + address);
                            writeMessage(finalS, address, sendingUser);
                            address = "";
                            finalMessage = "";
                            sendingUser = "";

                        } catch (IOException e) {
                            removeClient(sendingUser);
//                            removeClient(finalS);
                            try {
                                finalS.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                thread1.start();
            }
        });
        thread.start();
    }

    public void writeMessage(Socket s, String address, String sendingUser) throws IOException {
        System.out.println("Sending to Address: " + address);
        System.out.println("Client Address: " + s.getRemoteSocketAddress());

        //Slicing out port from user code and sending to desired client
        if (!listOfUsers.containsKey(address)) {
            System.out.println("Could not find user");
            return;
        }

        try {

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(listOfUsers.get(address).getOutputStream()));
            StringBuilder rawUsernameBuilder = new StringBuilder(sendingUser);
            String rawUsername = rawUsernameBuilder.substring(0, sendingUser.length() - 6);
            bw.write(rawUsername + ": " + finalMessage);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void removeClient(String sendingUser) {
        listOfUsers.remove(sendingUser);
    }

}