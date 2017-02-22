import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

class UDPServer
{
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        DatagramSocket serverSocketForMeaning = new DatagramSocket(9880);
        byte[] receiveData = new byte[1024];
        ExecutorService executor = Executors.newFixedThreadPool(5);
        Semaphore semaphore = new Semaphore(1,true);


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
            Runnable getDataAndSend = new UDPServerThread(word, serverSocket, serverSocketForMeaning, receivePacket, semaphore);
            executor.execute(getDataAndSend);
        }
    }
}


class UDPServerThread implements Runnable{
    long threadID = -1;
    String word;
    DatagramPacket receivePacket;
    DatagramSocket serverSocket;
    DatagramSocket serverSocketForMeaning;
    Semaphore semaphore;


    UDPServerThread(String word, DatagramSocket serverSocket, DatagramSocket serverSocketForMeaning, DatagramPacket receivePacket, Semaphore semaphore){
        this.word = word;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.semaphore = semaphore;
        this.serverSocketForMeaning = serverSocketForMeaning;
    }

    public void run(){
        try{
            this.threadID = Thread.currentThread().getId();
            byte[] sendMeaning = new byte[10000];

            String response;

            Boolean isWord = false;
            System.out.println("\n\nReceived input from client. Word is " + word + " thread ID is " + this.threadID);

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            Boolean getMeaningFromClient = false;

            semaphore.acquire();
            isWord = Dictionary.validateWord(word);
            if (isWord){
                response = Dictionary.getMeaning(word);
                if (response.isEmpty()){
                    getMeaningFromClient = true;
                    response = word + " meaning was not found in the dictionary";
                    System.out.println(response + "\n");
                }
            }
            else {
                getMeaningFromClient = true;
                response = word + " was not found in the dictionary";
                System.out.println(response + "\n");
            }

            if(getMeaningFromClient)
            {
                // indicate that the word is not in the dictionary
                sendMeaning = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendMeaning, sendMeaning.length, IPAddress, port);
                serverSocket.send(sendPacket);

                //receive the meaning
                byte[] receiveMeaning = new byte[10240];
                DatagramPacket receivePacket = new DatagramPacket(receiveMeaning, receiveMeaning.length);
                serverSocketForMeaning.receive(receivePacket);
                String meaning = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("received meaning from client " + meaning);

                if(isWord)
                    Dictionary.addMeaning(word, meaning);
                else
                    Dictionary.addWord(word, meaning);
            }
            semaphore.release();

            if (getMeaningFromClient)
                return;

            sendMeaning = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendMeaning, sendMeaning.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
        catch(Exception e){
            System.out.println("Exception caught : "+ e.getMessage());
        }
    }
}