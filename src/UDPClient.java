/**
 * Created by priyapns on 2/4/17.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

class UDPClient {
    public static void main(String args[]) throws Exception {
        BufferedReader brFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendWord;
        byte[] receiveMeaning = new byte[10000];
        Boolean ifNotEmpty = false;
        String word = "";
        while (true) {
            do {
                System.out.println("Enter a word:");
                word = brFromUser.readLine();
                if (word.equals("#")) {
                    System.out.println("# is not a word. \nPlease try again.");
                    ifNotEmpty = false;
                }
                else if (word.isEmpty()) {
                    System.out.println("Word cannot be empty. \nPlease try again.");
                    ifNotEmpty = false;
                }
                else{
                    ifNotEmpty = true;
                }
            }while(!ifNotEmpty);

            sendWord = word.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendWord, sendWord.length, IPAddress, 9876);
            clientSocket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receiveMeaning, receiveMeaning.length);
            clientSocket.receive(receivePacket);

            String meaning = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println(meaning);

            System.out.println("Type '#' to stop searching. Press <ENTER> to try another word.");

            Scanner in1 = new Scanner(System.in);
            String ch = in1.nextLine();

            if (ch.equals("#")) {
                sendWord = "#".getBytes();
                sendPacket = new DatagramPacket(sendWord, sendWord.length, IPAddress, 9876);
                clientSocket.send(sendPacket);
                break;
            }
        }
        clientSocket.close();
    }

}