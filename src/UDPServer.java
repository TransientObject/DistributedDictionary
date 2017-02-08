import java.io.*;
import java.net.*;
import java.lang.String;
import java.util.Scanner;

class UDPServer extends Dictionary
{
    public static void main(String args[]) throws Exception
    {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        System.out.println ("UDP Server Waiting for client on port 9876");
        byte[] receiveData = new byte[1024];
        byte[] sendMeaning = new byte[10000];

        String response;
        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("Server started. Waiting for input from client");
            serverSocket.receive(receivePacket);
            String word = new String(receivePacket.getData(), 0, receivePacket.getLength());
            Boolean isWord = false;

            // ending condition
            if (word.equals("#")){
                break;
            }
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            isWord = validateWord(word);
            if (isWord){
                response = getMeaning(word);
                if (response.isEmpty())
                    response = "Sorry, meaning was not found in the dictionary!";
            }
            else {
                response = word + " is not a valid word. please try again";
            }

            sendMeaning = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendMeaning, sendMeaning.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
        serverSocket.close();
    }
}