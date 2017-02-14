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

class UDPServerWorkPartitioning
{
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        while(true){
            WordAndPacket wordAndPacket;
            Callable<WordAndPacket> task = new ListenToClient(serverSocket, executor);
            Future<WordAndPacket> future = executor.submit(task);
            wordAndPacket = future.get();
            if (wordAndPacket.word.equals("#")){
                System.out.println("Client is closing the session. Bye\n");
                executor.shutdown();
                while (!executor.isTerminated()) {}
                System.out.println("All threads have finished executing. Server closing session");
                serverSocket.close();
                System.exit(0);
            }
            else
            {
                System.out.println("recvd word"+wordAndPacket.word);
                executor.execute(new ValidateAndFetchWord(wordAndPacket.word, serverSocket, wordAndPacket.packet, executor));
            }
        }
    }
}

class ListenToClient implements Callable{
    DatagramSocket serverSocket;
    ExecutorService executor;

    ListenToClient(DatagramSocket serverSocket, ExecutorService executor){
        this.serverSocket = serverSocket;
        this.executor = executor;
    }

    public WordAndPacket call(){
        try{
            System.out.println("Starting to listen to new data");
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String word = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Got new data - " + word);
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

    ValidateAndFetchWord(String word, DatagramSocket serverSocket, DatagramPacket receivePacket, ExecutorService executor){
        this.word = word;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.executor = executor;
    }

    public void run(){
        try{
            Boolean isWord = Dictionary.validateWord(word);
            String response;
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
            System.out.println("response for the word - " + word + " is \n\n" + response);
            executor.execute(new SendToClient(response, serverSocket, receivePacket));
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

    SendToClient(String response, DatagramSocket serverSocket, DatagramPacket receivePacket){
        this.response = response;
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
    }

    public void run(){
        try{
            byte[] sendMeaning = new byte[10000];
            String response;

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