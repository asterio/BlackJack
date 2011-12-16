/**
 * Interfejs stworzony tylko na potrzeby RMI 
 */

package ws.mainserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface wsMainServerInt  extends Remote
{
    //boolean checkLoginAvailable(String login) throws RemoteException;
    //String debugInfo() throws RemoteException;
    boolean isValid(String token) throws RemoteException;
    String login(String user, String pass) throws RemoteException;
   // boolean registerUser(String user, String pass) throws RemoteException;
    String isRMIWorkCorect() throws RemoteException;
}
