import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.*;

public interface UDPServerIntf  extends Remote {

    public String GetMeaning(String word) throws RemoteException;

}