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
        final int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        PrintWriter writer = new PrintWriter("ProgramSync_log.txt", "UTF-8");
        Semaphore semaphore = new Semaphore(1,true);

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
            Runnable getDataAndSend = new UDPServerThread(word, serverSocket, serverSocketForMeaning, receivePacket, semaphore, numThreads, writer);
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
    int numThreads;
    PrintWriter writer;

    UDPServerThread(String word, DatagramSocket serverSocket, DatagramSocket serverSocketForMeaning, DatagramPacket receivePacket, Semaphore semaphore, int numThreads, PrintWriter writer){
        this.word = word;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.semaphore = semaphore;
        this.serverSocketForMeaning = serverSocketForMeaning;
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
            System.out.println("Thread " + normalizedThreadID + " - search for word " + word);

            Boolean isWord = false;

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            Boolean getMeaningFromClient = false;

            writer.println("Thread " + normalizedThreadID + " - wait for semaphore to fetch/add " + word);
            System.out.println("Thread " + normalizedThreadID + " - wait for semaphore to fetch/add " + word);
            semaphore.acquire();
            writer.println("Thread " + normalizedThreadID + " - get the semaphore to fetch/add " + word);
            System.out.println("Thread " + normalizedThreadID + " - get the semaphore to fetch/add " + word);
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
                writer.println("Thread " + normalizedThreadID + " - waiting for the client to give meaning for the word " + word);
                System.out.println("Thread " + normalizedThreadID + " - waiting for the client to give meaning for the word " + word);
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
                {
                    System.out.println("adding meaning of word " + word);
                    Dictionary.addMeaning(word, meaning);
                }
                else
                {
                    System.out.println("adding the word and the meaning of word " + word);
                    Dictionary.addWord(word, meaning);
                }
            }
            else
            {
                writer.println("Thread " + normalizedThreadID + " - " + word + " is found in the dictionary. fetching it for the client");
                System.out.println("Thread " + normalizedThreadID + " - " + word + " is found in the dictionary. fetching it for the client");
            }

            writer.println("Thread " + normalizedThreadID + " - release the semaphore");
            System.out.println("Thread " + normalizedThreadID + " - release the semaphore");
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