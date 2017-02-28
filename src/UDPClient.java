/**
 * Created by priyapns on 2/4/17.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.Naming;
import java.util.Scanner;

class UDPClient {
    public static void main(String args[]) throws Exception {
        try {
            String[] words = {"abet", "apple", "orange", "intimidation", "zoology", "wreath", "vaporize", "transport", "abrogative", "circumscribe"};
            int i = 0;
            String serverURL = "rmi://" + args[0] + "/UDPServer";
            UDPServerIntf udpServerIntf = (UDPServerIntf) Naming.lookup(serverURL);
            while (i < 5) {
                for (String word : words) {
                    System.out.println("searching meaning for " + word);
                    System.out.println(udpServerIntf.GetMeaning(word));
                }
                i++;
            }
            System.out.println(udpServerIntf.GetMeaning("#"));
        } catch (Exception e) {
        }
    }
}