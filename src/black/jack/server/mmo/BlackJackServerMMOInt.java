package black.jack.server.mmo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BlackJackServerMMOInt extends Remote
{
    String DebugDeleteLoginedUsers() throws RemoteException;
    PlayerEvent[] anyEvents3(String token) throws RemoteException;
    List anyEvents4(String token) throws RemoteException;
    String changeTable(String token, String tableId) throws RemoteException;
    String debugInfo() throws RemoteException;
    String hit(String token) throws RemoteException;
    String loginOnBJS(String token, String tableId) throws RemoteException;
    String pass(String token) throws RemoteException;
    void stop(String token) throws RemoteException;
    String takeListOfTables2() throws RemoteException;
    String takePartInGame(String token) throws RemoteException;
    String takeTwoCards(String token) throws RemoteException;
    void test() throws RemoteException;
    String isRMIWorkCorect() throws RemoteException;
    
    PlayerEvent getPlayerEvent() throws RemoteException;
    PlayerEvent[] getPlayerEvents() throws RemoteException;

}
