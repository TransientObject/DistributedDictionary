import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class WordAndPacket{
    String word;
    DatagramPacket  packet;

    WordAndPacket(String word, DatagramPacket packet){
        this.word = word;
        this.packet = packet;
    }

}

class Constants{
    static final int numThreads = 100;
}

class UDPServerWorkPartitioning
{
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        ExecutorService executor = Executors.newFixedThreadPool(Constants.numThreads);
        PrintWriter writer = new PrintWriter("WorkPartitioning_log.txt", "UTF-8");

        while(true){
            WordAndPacket wordAndPacket;
            Callable<WordAndPacket> task = new ListenToClient(serverSocket, executor, writer);
            Future<WordAndPacket> future = executor.submit(task);
            wordAndPacket = future.get();
            if (wordAndPacket.word.equals("#")){
                System.out.println("Client is closing the session\n");
                executor.shutdown();
                while (!executor.isTerminated()) {}
                System.out.println("All threads have finished executing. Bye");
                serverSocket.close();
                System.exit(0);
            }
            else
            {
                executor.execute(new ValidateAndFetchWord(wordAndPacket.word, serverSocket, wordAndPacket.packet, executor, writer));
            }
        }
    }
}

class ListenToClient implements Callable{
    DatagramSocket serverSocket;
    ExecutorService executor;
    PrintWriter writer;

    ListenToClient(DatagramSocket serverSocket, ExecutorService executor, PrintWriter writer){
        this.serverSocket = serverSocket;
        this.executor = executor;
        this.writer = writer;
    }

    public WordAndPacket call(){
        try{
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String word = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Got new data - " + word);
            long threadID = Thread.currentThread().getId() % Constants.numThreads + 1;
            writer.println("Thread " + threadID + " - Received the word " + word + " from client");
            return new WordAndPacket(word,receivePacket);
        }
        catch(Exception e){
            System.out.println("Exception caught : "+ e.getMessage());
        }
        return null;
    }
}

class ValidateAndFetchWord implements Runnable{
    String word;
    DatagramPacket receivePacket;
    DatagramSocket serverSocket;
    ExecutorService executor;
    PrintWriter writer;

    ValidateAndFetchWord(String word, DatagramSocket serverSocket, DatagramPacket receivePacket, ExecutorService executor, PrintWriter writer){
        this.word = word;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.executor = executor;
        this.writer = writer;
    }

    public void run(){
        try{
            long threadID = Thread.currentThread().getId() % Constants.numThreads + 1;
            writer.println("Thread " + threadID + " validating and fetching the word - " + word );
            Boolean isWord = Dictionary.validateWord(word);
            String response;
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
            executor.execute(new SendToClient(response, serverSocket, receivePacket, word, writer));
        }
        catch(Exception e){
            System.out.println("Exception caught : "+ e.getMessage());
        }
    }
}

class SendToClient implements Runnable{
    String response;
    DatagramPacket receivePacket;
    DatagramSocket serverSocket;
    PrintWriter writer;
    String word;

    SendToClient(String response, DatagramSocket serverSocket, DatagramPacket receivePacket, String word, PrintWriter writer){
        this.response = response;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.writer = writer;
        this.word = word;
    }

    public void run(){
        try{
            byte[] sendMeaning = new byte[10000];
            String response;

            long threadID = Thread.currentThread().getId() % Constants.numThreads + 1;
            writer.println("Thread " + threadID + " sending response back to client for word - " + word);

            InetAddress IPAddress = this.receivePacket.getAddress();
            int port = this.receivePacket.getPort();

            sendMeaning = this.response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendMeaning, sendMeaning.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
        catch(Exception e){
            System.out.println("Exception caught : "+ e.getMessage());
        }
    }
}