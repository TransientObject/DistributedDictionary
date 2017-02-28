import java.io.*;
import java.net.*;
import java.lang.String;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.rmi.Remote;
import java.rmi.server.*;

public class UDPServer extends UnicastRemoteObject implements UDPServerIntf {
    public UDPServer() throws RemoteException {
    }

    public static void main(String[] args) {
        try {
            UDPServer server = new UDPServer();
            Naming.rebind("UDPServer", server);
        } catch (Exception e) {
            System.out.println("UDPServer Main error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String GetMeaning(String word) {
        String response = "";
        System.out.println("received word from client " + word);
        try {
            PrintWriter writer = new PrintWriter("RMIServer_log.txt", "UTF-8");
            Boolean isWord = false;
            isWord = Dictionary.validateWord(word);
            Boolean getMeaningFromClient = false;
            if (isWord) {
                response = Dictionary.getMeaning(word);
                if (response.isEmpty()) {
                    getMeaningFromClient = true;
                    response = word + " meaning was not found in the dictionary";
                    System.out.println(response + "\n");
                }
            } else {
                getMeaningFromClient = true;
                response = word + " was not found in the dictionary";
                System.out.println(response + "\n");
            }

            if (getMeaningFromClient) {
                System.out.println("meaning not found in dictionary");
            }

        } catch (Exception e) {
            System.out.println("UDPServer GetMeaning error: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}