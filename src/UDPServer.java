import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class UDPServer
{
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        ExecutorService executor = Executors.newFixedThreadPool(5);


        while(true){
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String word = new String(receivePacket.getData(), 0, receivePacket.getLength());

            // ending condition
            if (word.equals("#")){
                System.out.println("Client is closing the session. Bye\n");
                executor.shutdown();
                while (!executor.isTerminated()) {}
                System.out.println("All threads have finished executing. Server closing session");
                serverSocket.close();
                System.exit(0);
            }
            Runnable getDataAndSend = new UDPServerThread(word, serverSocket, receivePacket);
            executor.execute(getDataAndSend);
        }
    }
}


class UDPServerThread implements Runnable{
    long threadID = -1;
    String word;
    DatagramPacket receivePacket;
    DatagramSocket serverSocket;

    UDPServerThread(String word, DatagramSocket serverSocket, DatagramPacket receivePacket){
        this.word = word;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
    }

    public void run(){
        try{
            this.threadID = Thread.currentThread().getId();
            byte[] sendMeaning = new byte[10000];

            String response;
            System.out.println("ServerThread started. Waiting for input from client on port 9876\n");
            System.out.println("Thread ID - " + this.threadID);

            Boolean isWord = false;
            System.out.println("\n\nReceived input from client. Word is " + word + " thread ID is " + this.threadID);

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();


            isWord = Dictionary.validateWord(word);
            if (isWord){
                response = Dictionary.getMeaning(word);
                if (response.isEmpty()){
                    response = "Sorry, meaning was not found in the dictionary!";
                    System.out.println(response + "\n");
                }
            }
            else {
                response = word + " is not a valid word. please try again";
                System.out.println(response + "\n");
            }

            sendMeaning = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendMeaning, sendMeaning.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
        catch(Exception e){
            System.out.println("Exception caught : "+ e.getMessage());
        }
    }
}