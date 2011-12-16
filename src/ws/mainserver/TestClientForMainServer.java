package ws.mainserver;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestClientForMainServer
{
    public static void main(String [] args)
    {
        try
        {
            System.out.println("BlackJackServerMMO is starting...");

            Registry reg = LocateRegistry.getRegistry(1111);
            wsMainServerInt tc= (ws.mainserver.wsMainServerInt) reg.lookup("MainServer");
            System.out.println(tc.isRMIWorkCorect());
            
            System.out.println("BlackJackServerMMO started...");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}