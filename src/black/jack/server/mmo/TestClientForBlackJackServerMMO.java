package black.jack.server.mmo;

import ws.mainserver.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestClientForBlackJackServerMMO
{
    public static void main(String [] args)
    {
        try
        {
            System.out.println("BlackJackServerMMO is starting...");

            Registry reg = LocateRegistry.getRegistry(1111);
            BlackJackServerMMOInt tc= (BlackJackServerMMOInt) reg.lookup("GameServer");
            System.out.println(tc.isRMIWorkCorect());
            
            System.out.println("BlackJackServerMMO started...");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}