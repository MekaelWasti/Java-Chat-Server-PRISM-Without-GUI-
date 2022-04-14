package com.company;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private BufferedWriter bw;
    public String finalMessage = "";
    public String address = "";
    public String sendingUser = "";
    public Socket s;

    //Store all client usernames as keys and corresponding sockets as values
    public HashMap<String,Socket> listOfUsers = new HashMap<String,Socket>();

    public void start() throws IOException {
        //Socket
        ServerSocket ss = new ServerSocket(63030);

//        Thread thread = new Thread(() -> {
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

                            System.out.println("Sending: " + finalMessage + " - From: " + sendingUser + " - To: " + address); //For development purposes
                            writeMessage(finalS, address, sendingUser);
                            address = "";
                            finalMessage = "";
                            sendingUser = "";

                        } catch (IOException e) {
                            removeClient(sendingUser);
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
//        });
//        thread.start();
    }

    public void writeMessage(Socket s, String address, String sendingUser) throws IOException {
        System.out.println("Sending to Address: " + address); //For development purposes
        System.out.println("Client Address: " + s.getRemoteSocketAddress()); //For development purposes

        for (String key : listOfUsers.keySet()) {
            System.out.println(key);

        }

        //Error if desired user to send to is not found
        if (!listOfUsers.containsKey(address)) {
            System.out.println("Could not find user"); //For development purposes
            return;
        }

        try {
            //Write and send message to each desired user (write out sending username)
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(listOfUsers.get(address).getOutputStream()));
            StringBuilder rawUsernameBuilder = new StringBuilder(sendingUser);

            //Slicing out port from user code and sending to desired client
            String rawUsername = rawUsernameBuilder.substring(0, sendingUser.length() - 6);
            bw.write(rawUsername + ": " + finalMessage);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Is called when a socket connection is closed and the client is
    //removed from the hash map
    public void removeClient(String sendingUser) {
        listOfUsers.remove(sendingUser);

    }

}