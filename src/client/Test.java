/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import black.jack.server.mmo.BlackJackServerMMO;
import black.jack.server.mmo.BlackJackServerMMOInt;
import black.jack.server.mmo.PlayerEvent;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Asterio
 */
public class Test 
{
    public static void main(String[] args)
    {
        try 
        {
            System.out.println("Black Test Client is starting...");

            Registry reg = LocateRegistry.getRegistry(1112);
            ws.mainserver.wsMainServerInt connectToMainServer = (ws.mainserver.wsMainServerInt) reg.lookup("MainServer");
            System.out.println(connectToMainServer.isRMIWorkCorect());

            
            Registry reg1 = LocateRegistry.getRegistry(1113);
            BlackJackServerMMOInt tc= (BlackJackServerMMOInt) reg1.lookup("GameServer");
            
            //PlayerEvent p1 = tc.getPlayerEvent();
            PlayerEvent[] p = tc.getPlayerEvents();
            for(int i = 0; i < p.length; i++)
            {
                System.out.println(i + " " + p[i].getCards() + " " + p[i].getLogin() + " " + p[i].getStatus() + " " + p[i].getPointsSum());
            }
            //System.out.println(p1.getCards() + " " + p1.getLogin() + " " + p1.getStatus() + " " + p1.getPointsSum());

            System.out.println("Black Test Clienn started...");
        } catch (NotBoundException ex) 
        {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (AccessException ex) 
        {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (RemoteException ex) 
        {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
