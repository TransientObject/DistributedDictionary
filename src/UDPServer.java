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
        final int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        PrintWriter writer = new PrintWriter("BagofTasks_log.txt", "UTF-8");

        while(true){
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String word = new String(receivePacket.getData(), 0, receivePacket.getLength());

            // ending condition
            if (word.equals("#")){
                System.out.println("Client is closing the session\n");
                executor.shutdown();
                while (!executor.isTerminated()) {}
                System.out.println("All threads have finished executing. Bye");
                writer.close();
                serverSocket.close();
                System.exit(0);
            }
            Runnable getDataAndSend = new UDPServerThread(word, serverSocket, receivePacket, numThreads, writer);
            executor.execute(getDataAndSend);
        }
    }
}


class UDPServerThread implements Runnable{
    long threadID = -1;
    String word;
    DatagramPacket receivePacket;
    DatagramSocket serverSocket;
    int numThreads;
    PrintWriter writer;

    UDPServerThread(String word, DatagramSocket serverSocket, DatagramPacket receivePacket, int numThreads, PrintWriter writer){
        this.word = word;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.numThreads = numThreads;
        this.writer = writer;
    }

    public void run(){
        try{
            this.threadID = Thread.currentThread().getId();
            byte[] sendMeaning = new byte[10000];
            long normalizedThreadID = (this.threadID % this.numThreads)+1;
            String response;
            writer.println("Thread " + normalizedThreadID + " - search for word " + word);

            System.out.println("search for word " + word);
            Boolean isWord = false;

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();


            isWord = Dictionary.validateWord(word);
            if (isWord){
                response = Dictionary.getMeaning(word);
                if (response.isEmpty()){
                    response = "Sorry. For the given word, " + word + " meaning was not found in the dictionary!";
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