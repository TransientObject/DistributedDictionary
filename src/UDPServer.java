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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class UDPServer extends UnicastRemoteObject implements UDPServerIntf {

    PrintWriter writer;
    public UDPServer() throws RemoteException {
        try
        {
            writer = new PrintWriter("RMIServer_log.txt", "UTF-8");
            writer.print("");
        }
        catch(Exception e)
        {}
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
            if (word.equals("#"))
            {
                System.out.println("client wants to end the connection");
                writer.append("client wants to end the connection\n");
                writer.close();
                System.exit(0);
            }
            writer.append("\n\nreceived word from client " + word + "\n");
            Boolean isWord = false;
            isWord = Dictionary.validateWord(word);
            Boolean getMeaningFromClient = false;
            if (isWord) {
                writer.append(word + " is a valid entry"+ "\n");
                Files.write(Paths.get("RMIServer_log.txt"), (word + " is a valid entry"+ "\n").getBytes("UTF-8"), StandardOpenOption.APPEND);
                response = Dictionary.getMeaning(word);
                if (response.isEmpty()) {
                    getMeaningFromClient = true;
                    response = word + " meaning was not found in the dictionary";
                    System.out.println(response + "\n");
                    writer.append(response+ "\n");
                }
            } else {
                getMeaningFromClient = true;
                response = word + " was not found in the dictionary";
                System.out.println(response + "\n");
                writer.append(response+ "\n");
            }

            if (getMeaningFromClient) {
                System.out.println("meaning not found in dictionary");
            }
            else
            {
                writer.append("meaning of the word is " + response + "\n");
            }
        }
        catch (Exception e) {
            System.out.println("UDPServer GetMeaning error: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }
}