import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Scanner;

class UDPServer
{
    public static void main(String args[]) throws Exception
    {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendMeaning = new byte[10000];

        String response;
        System.out.println("Server started. Waiting for input from client on port 9876\n");
        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String word = new String(receivePacket.getData(), 0, receivePacket.getLength());
            Boolean isWord = false;
            System.out.println("\n\nReceived input from client. Word is " + word);

            // ending condition
            if (word.equals("#")){
            	System.out.println("Client is closing the session. Bye\n");
                break;
            }
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
        serverSocket.close();
    }
}