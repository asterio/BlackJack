/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import black.jack.server.mmo.BlackJackServerMMOInt;
import black.jack.server.mmo.PlayerEvent;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import ws.mainserver.wsMainServerInt;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import java.util.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.*;

class ConnectionToMainServer extends JFrame
{
    private JPanel panel;
    
    private JTextField login;
    private JPasswordField pass;
    private JButton connect;
    
    private JLabel status;
    
    private wsMainServerInt con;  
    private BlackJackServerMMOInt conServer;
    
    private String token = "";
    
    private String username = "";
    private Registry reg = null;
    private Registry reg1 = null;

    
    private List<String> list = new ArrayList<String>();
    public ConnectionToMainServer() throws NotBoundException, MalformedURLException, RemoteException 
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run() 
            {
                setSize(400,400);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setVisible(true);
            }   
        });
    }
    public void startGame()
    {
        try 
        {
            reg = LocateRegistry.getRegistry(1112);
            con = (wsMainServerInt) reg.lookup("MainServer");
        }
        catch (NotBoundException ex) 
        {
            Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (AccessException ex) 
        {
            Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (RemoteException ex) 
        {
            Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        

        System.out.println("BlackJackServerMMO started...");

        connect.addActionListener(new ActionListener()
        {
            
            public void actionPerformed(ActionEvent e)
            {
                
                try
                {
                    
                    token = con.login(login.getText(), new String(pass.getPassword()));
                    status.setText("Status: " + token);
                    if(!token.equals("0"))
                    {
                        reg1 = LocateRegistry.getRegistry(1113);
                        conServer = (BlackJackServerMMOInt) reg1.lookup("GameServer");
                        if(conServer.takeListOfTables2() == null)
                        {
                            conServer.loginOnBJS(token, "1");
                            username = login.getText();
                            new GameFrame(1);
                        }
                        else
                        {
                            TablesFrame f = new TablesFrame(conServer.takeListOfTables2());
                            f.showTables();
                        }

                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null,"Failed!");
                    }
                } 
                catch (NotBoundException ex) 
                {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
            }  
            catch (RemoteException ex) 
            {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });            

        addWindowListener(new ClosingListener());
    }
    public void init()
    {
        panel = new JPanel();
        panel.setLayout(new GridLayout(4,2));

        login = new JTextField();
        pass = new JPasswordField();
        connect = new JButton("Connect");
        status = new JLabel("Status:");

        panel.add(new JLabel("Login:"));
        panel.add(login);
        panel.add(new JLabel("Pass:"));
        panel.add(pass);
        panel.add(new JLabel(""));
        panel.add(connect);
        panel.add(status);

        login.addKeyListener(new Listener());

        pass.addKeyListener(new Listener());

        connect.addKeyListener(new Listener());

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());
        content.add(panel);
    }
        
    
    
    
    class Listener implements KeyListener
    {
        @Override
        public void keyTyped(KeyEvent e) 
        {
            int num = e.getKeyCode();
            if(num == KeyEvent.VK_ENTER)
            {
                connect.doClick();
            }    
        }

        @Override
        public void keyPressed(KeyEvent e) 
        {
            int num = e.getKeyCode();
            if(num == KeyEvent.VK_ENTER)
            {
                connect.doClick();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) 
        {
        }
    }
    
    class ClosingListener implements WindowListener
    {

        @Override
        public void windowOpened(WindowEvent e) 
        {}

        @Override
        public void windowClosing(WindowEvent e) 
        {
            if(!token.equals("0") && !token.isEmpty())
            {
                try 
                {
                    conServer.stop(token);
                    System.out.println("windowClosing()");
                }
                catch (RemoteException ex) 
                {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void windowClosed(WindowEvent e) 
        {
            if(!token.equals("0") && !token.isEmpty())
            {
                try 
                {
                    conServer.stop(token);
                    System.out.println("windowClosing()");
                }
                catch (RemoteException ex) 
                {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        @Override
        public void windowIconified(WindowEvent e) 
        {}

        @Override
        public void windowDeiconified(WindowEvent e) 
        {}

        @Override
        public void windowActivated(WindowEvent e) 
        {}

        @Override
        public void windowDeactivated(WindowEvent e) 
        {}
        
    }
    class Table extends JPanel
    {
        private String id;
        private String numberOfPlayers;
        private JButton enter;
        public Table(String id, String number)
        {
            this.id = id;
            this.numberOfPlayers = number;
            enter = new JButton("Enter");
            enter.addActionListener(new ButtonListener(id));
            
            this.setLayout(new GridLayout(1,3));
            this.add(new JLabel(id));
            this.add(new JLabel(numberOfPlayers));
            this.add(enter);
        }
        public String toString()
        {
            return "Id: " + id + " - Number of players: " + numberOfPlayers;
        }
        class ButtonListener implements ActionListener
        {
            private String number;
            public ButtonListener(String number)
            {
                this.number = number;
            }
            public void actionPerformed(ActionEvent e)
            {
                try 
                {
                    new GameFrame(Integer.parseInt(number));
                    String result = conServer.loginOnBJS(token, number);
                    System.out.println("Status zalogowania: " + result + " na stół: " + number );
                    System.out.println("TableId: " + number);
                }
                catch (RemoteException ex) 
                {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    class NewTableListener implements ActionListener
    {
        
        private int id;
        private String tableId;
        public NewTableListener() throws RemoteException
        {
            if(!conServer.takeListOfTables2().isEmpty())
            {
                StringTokenizer str = new StringTokenizer(conServer.takeListOfTables2(),"_");
                tableId = str.nextToken();
                id = Integer.parseInt(tableId);
                id+=1;
                tableId = ""+id;
            }
            else
            {
                JOptionPane.showConfirmDialog(null, "Error in initializing tables.");
            }
        }
        public void actionPerformed(ActionEvent e)
        {
            try 
            {
                System.out.println("Tworze nowy stół:" + tableId);
                conServer.loginOnBJS(token,tableId );
                new GameFrame(id);
            }
            catch (RemoteException ex) 
            {
                Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
  
    
    class TablesFrame extends JFrame 
    {
        private Thread events;
        private Container content;
        private JPanel panel;
        private List<Table> listP = new ArrayList<Table>();
        private int number;
        private JButton newTable;
        public TablesFrame(String tekst) throws RemoteException
        {
            SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    setVisible(true);
                    setTitle("Choose table");
                    setSize(300,300);
                }
            });
            
            
            
            newTable = new JButton("New table");
            newTable.addActionListener(new NewTableListener());
            panel = new JPanel();
            panel.setLayout(new GridLayout(15,1));
            addTables(tekst);
            panel.add(new Table("Table Id","Number of Players"));

            for(int i = 0; i < number; i++)
            {
                panel.add(listP.get(i));
                System.out.println("Dodaje stół " + i);
            }

            content = getContentPane();
            content.setLayout(new GridBagLayout());
            panel.add(newTable);
            content.add(panel);  
        }
        public void addTables(String tekst)
        {
            StringTokenizer str = new StringTokenizer(tekst,"_");
            StringTokenizer str2;
            number = Integer.parseInt(str.nextToken());
            for(int i = 0; i < number; i++)
            {
                str2 = new StringTokenizer(str.nextToken(),":");
                listP.add(new Table(str2.nextToken(),str2.nextToken()));
            }
        }
        public void update(String tekst)
        {
            listP.clear();
            panel.removeAll();
            panel.repaint();
            if(tekst.isEmpty())
            {
                JOptionPane.showMessageDialog(null,"Error!");
            }
            else
            {
                StringTokenizer str = new StringTokenizer(tekst,"_");
                int v = Integer.parseInt(str.nextToken());
                if(v != number)
                {
                    addTables(tekst);
                    
                    panel.add(new Table("Table Id","Number of Players"));

                    for(int i = 0; i < number; i++)
                    {
                        panel.add(listP.get(i));
                        System.out.println("Dodaje stół " + i+1);
                    }
                    panel.add(newTable);
                    panel.repaint();
                }
            }  
            content.repaint();
        }
        public void showTables()
        {
            for(int i = 0; i < number; i++)
            {
                System.out.println(listP.get(i).toString());
            }
        }
        class TableThread implements Runnable
        {
            public void run()
            {
                while(true)
                {
                    try 
                    {
                        update(conServer.takeListOfTables2());
                        showTables();
                        
                        Thread.sleep(2000);
                        
                        
                        System.out.println("TableThread:run()");
                    }
                    catch (InterruptedException ex) 
                    {
                        Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    catch (RemoteException ex) 
                    {
                        Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }   
    } 
    
    class GameFrame extends JFrame
    {
        private Thread events;
        private int tableId;
        private JButton startGame;
        private JButton hit;
        private JButton pass;
   
        private JPanel buttonPanel;
        
        private JPanel info;
        private JTextArea text;
        
        private List<PlayerPanel> listPanel = new ArrayList<PlayerPanel>();
        
        private boolean gameStarted = false;
        public GameFrame(int id)
        {
            this.addWindowListener(new WindowListener()
                    {

                @Override
                public void windowOpened(WindowEvent e) {}

                @Override
                public void windowClosing(WindowEvent e) 
                {
                    if(events != null)
                        if(events.isAlive())
                            events.stop();
                }

                @Override
                public void windowClosed(WindowEvent e) 
                {
                    if(events != null)
                        if(events.isAlive())
                            events.stop();
                }

                @Override
                public void windowIconified(WindowEvent e) {}

                @Override
                public void windowDeiconified(WindowEvent e) {}

                @Override
                public void windowActivated(WindowEvent e) {}

                @Override
                public void windowDeactivated(WindowEvent e) {}
                
            });
            startGame = new JButton("Start Game");
            hit = new JButton("Hit");
            pass = new JButton("Pass");
            buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(2,2));
            buttonPanel.add(startGame);
            buttonPanel.add(hit);
            buttonPanel.add(pass);
            
            pass.setEnabled(gameStarted);
            hit.setEnabled(gameStarted);
            startGame.setEnabled(!gameStarted); 
            hit.addActionListener(new HitListener());
            pass.addActionListener(new PassListener());
            startGame.addActionListener(new StartGameListener());
                      
            info = new JPanel();
            info.setBounds(420, 0, 250, 400);
            info.setBorder(BorderFactory.createTitledBorder("Events"));
            text = new JTextArea(20,20);
            text.setEnabled(false);
            text.append("Info()");
            info.add(new JScrollPane(text));        
            this.tableId = id;
          
            listPanel.add(new PlayerPanel("Krupier",0));
            listPanel.add(new PlayerPanel("Gracz1",110));
            listPanel.add(new PlayerPanel("Gracz2",220));
            listPanel.add(new PlayerPanel("Gracz3",330));
            listPanel.add(new PlayerPanel("Gracz4",440));
            listPanel.add(new PlayerPanel("Gracz5",550));
            listPanel.add(new PlayerPanel("Gracz6",660));
            //listPanel.add(new PlayerPanel("Gracz7",700));
 
            for(int i = 0; i < listPanel.size(); i++)
            {
                add(listPanel.get(i));
            }
            
            add(info);
            add(new JPanel());
            add(buttonPanel,BorderLayout.SOUTH);
            
            
            SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    setSize(750,850);
                    setVisible(true);
                    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    setTitle("Game | Table "+tableId);
                }
            });
        }
        
        
        
        class StartGameListener implements ActionListener
        {
            private String result;
            public void actionPerformed(ActionEvent e)
            {
                if(gameStarted == false)
                {
                    for(int i = 0; i < listPanel.size(); i++)
                    {
                        //listPanel.get(i).getCards().setText("");
                        listPanel.get(i).getCards().removeAll();
                        listPanel.get(i).getCards().repaint();
                        listPanel.get(i).getScore().setText("");
                        listPanel.get(i).setTokens(0);
                    }
                }
                try 
                {
                    result = conServer.takePartInGame(token);
                }
                catch (RemoteException ex) 
                {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Result:" + result);
                
                if(result.equals("1"))
                {
                    try 
                    {
                        startGame.setEnabled(false);
                        JOptionPane.showMessageDialog(null,"You should wait 10seconds, until game start.");
                        Thread.sleep(12000);
                    }
                    catch (InterruptedException ex) 
                    {
                        Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try 
                    {
                        result = conServer.takeTwoCards(token);
                    }
                    catch (RemoteException ex) 
                    {
                        Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(result.equals("-2"))
                    {
                        JOptionPane.showMessageDialog(null, "Error. You cannot take to cards. Start Game.");
                        System.out.println(result);
                    }
                    else if(result.equals("-3"))
                    {
                        JOptionPane.showMessageDialog(null, "Error. You've already taken your cards.");
                        System.out.println(result);
                    }
                    else if(result.equals("-4"))
                    {
                        JOptionPane.showMessageDialog(null, "Error. You don't participate in this game.");
                        System.out.println(result);
                    }
                    else
                    {
                        text.setText("");
                        gameStarted = true;
                        pass.setEnabled(gameStarted);
                        hit.setEnabled(gameStarted);
                        startGame.setEnabled(!gameStarted);  
                        events = new Thread(new Opponent());
                        events.start();
                        TwoCardsSub str = new TwoCardsSub(result);

                        System.out.println(result);
                    }
                }         
                else
                {
                    System.out.println("Result:" + result);
                    JOptionPane.showMessageDialog(null,"Error! " + result);
                }
            }
        }
        
        
        
        class PassListener implements ActionListener
        {
            String result;
            public void actionPerformed(ActionEvent e)
            {
                try 
                {
                    result = conServer.pass(token);
                }
                catch (RemoteException ex) 
                {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println(result);
                
                if(gameStarted == true)
                {
                    if(result.equals("-3"))
                    {
                        JOptionPane.showMessageDialog(null, "Error.First you must take two cards.");
                        gameStarted = false;
                        dispose();
                        throw new UnsupportedOperationException("Error.First you must take two cards." + result);
                    }
                    else if(result.equals("-2"))
                    {
                        JOptionPane.showMessageDialog(null, "Error. Game wasn't started properly.");
                        gameStarted = false;
                        dispose();
                        throw new UnsupportedOperationException("Error. Game wasn't started properly." + result);
                    }
                    else if(result.equals("-4"))
                    {
                        JOptionPane.showMessageDialog(null, "Error. You don't participate in this game.");
                        gameStarted = false;
                        dispose();
                        throw new UnsupportedOperationException("Error. You don't participate in this game." + result);
                    }
                    else if(result.equals("-5"))
                    {
                        JOptionPane.showMessageDialog(null, "Error. You have already finished this game. ");
                        gameStarted = false;
                        dispose();
                        throw new UnsupportedOperationException("Error. You have already finished this game. " + result);
                    }

                }
            }
        }
        
        class HitListener implements ActionListener
        {
            private String result;
            private void addCard(PlayerPanel panel, String card)
            {
                JLabel label = new JLabel();
                label.setIcon(new ImageIcon(card+".gif"));
                panel.getCards().add(label);
                panel.setTokens(panel.getTokens()+1);
            }
            public void actionPerformed(ActionEvent e)
            {
                try 
                {
                    result = conServer.hit(token);
                }
                catch (RemoteException ex) 
                {
                    Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                text.append(result);
                System.out.println(result);
                
                if(gameStarted == true)
                {
     
                    if(result.equals("-3") || result.equals("-2"))
                    {
                        JOptionPane.showMessageDialog(null, "Kod błędu: " + result);
                        gameStarted = false;
                        dispose();
                        throw new UnsupportedOperationException("Kod błędu:" + result);
                    }
                    System.out.println("Gracz: " + result);
                    SubString str = new SubString(result);
                    String username = token.substring(0, token.length()-8);
                    
                    for(int i = 0; i < listPanel.size(); i++)
                    {
                        if(listPanel.get(i).getUser().equals(username))
                        {
                            listPanel.get(i).getScore().setText(""+str.getSum());
/*dodać dodawanie jednej karty*/
                            addCard(listPanel.get(i),str.getCard());
                            //listPanel.get(i).getCards().setText( listPanel.get(i).getCards().getText()+str.getCard());
                        }
                    }
                    if(str.getStatus() != 0)
                    {
                        gameStarted = false;
                        if(str.getStatus() == 1)
                        {
                            JOptionPane.showMessageDialog(null, "You win!");
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null, "You lose!");
                        }
                        pass.setEnabled(gameStarted);
                        hit.setEnabled(gameStarted);
                        
                    }
                    System.out.println("Status: " + str.getStatus());
                    System.out.println("Card: " + str.getCard());
                    System.out.println("Sum: " + str.getSum());
                }
                startGame.setEnabled(!gameStarted);
            }
        }
        
        class Opponent implements Runnable
        {
            private void setCards(PlayerPanel panel, String text)
            {
                StringTokenizer str = new StringTokenizer(text,":");
                String card = null;
                JLabel label = null;
                if(panel.getTokens() != str.countTokens())
                {
                    panel.setTokens(str.countTokens());
                    panel.getCards().removeAll();
                    while(str.hasMoreTokens())
                    {
                        System.out.println("TOKENS:" + str.countTokens());
                        card = str.nextToken();
                        System.out.println("\t" + "card: " + card);
                        label = new JLabel();
                        label.setIcon(new ImageIcon(card+".gif"));
                        panel.getCards().add(label);
                    }
                    panel.getCards().repaint();
                }
            }
            public void run()
            {
                while(gameStarted)
                {
                    PlayerEvent[] players = null;
                    try 
                    {
                        players = conServer.anyEvents3(token);
                        
                        //result = conServer.anyEvents4(token);
                    }
                    catch (RemoteException ex) 
                    {
                        Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    /*if(!result.isEmpty())
                    {
                        Iterator iter = result.iterator();
                        for(int i = 0; i < result.size(); i++)
                        {
                            PlayerEvent player = (PlayerEvent)iter.next();
                            if(player != null)
                            {
                                listPanel.get(i).setUser(player.getLogin());
                                listPanel.get(i).getPanel().setBorder(BorderFactory.createTitledBorder(player.getLogin()));
                                setCards(listPanel.get(i),player.getCards());
                                listPanel.get(i).getScore().setText(""+player.getPointsSum());
                                text.append(player.getLogin() + ": " + player.getCards() + " " + player.getPointsSum() + " Status:" + player.getStatus() + "\n");
                                System.out.println(player.getLogin() + ": " + player.getCards() + player.getPointsSum() + " Status:" + player.getStatus());
                                System.err.println("NextUser");
                            }
                            if(username.equals(player.getLogin()))
                            {
                                if(player.getStatus() == 1)
                                {
                                    JOptionPane.showMessageDialog(null,"You win!");
                                    gameStarted = false;
                                }
                                else if(player.getStatus() == -1)
                                {
                                    JOptionPane.showMessageDialog(null,"You lose!");
                                    gameStarted = false;
                                }
                            }
                            else
                            {
                                if(player.getStatus() == 1)
                                {
                                    JOptionPane.showMessageDialog(null,player.getLogin()+ " win!");
                                    gameStarted = false;
                                }
                            }
                            pass.setEnabled(gameStarted);
                            hit.setEnabled(gameStarted);
                        
                            startGame.setEnabled(!gameStarted);
                        }
                    }*/
                        
                    if(players != null)
                    {
                        for(int i = 0; i < players.length; i++)
                        {
                            if(players[i] != null)
                            {
                                listPanel.get(i).setUser(players[i].getLogin());
                                listPanel.get(i).getPanel().setBorder(BorderFactory.createTitledBorder(players[i].getLogin()));
                                setCards(listPanel.get(i),players[i].getCards());
                                listPanel.get(i).getScore().setText(""+players[i].getPointsSum());
                                text.append(players[i].getLogin() + ": " + players[i].getCards() + " " + players[i].getPointsSum() + " Status:" + players[i].getStatus() + "\n");
                                System.out.println(players[i].getLogin() + ": " + players[i].getCards() + players[i].getPointsSum() + " Status:" + players[i].getStatus());
                                //System.err.println("NextUser");
                            }
                            if(username.equals(players[i].getLogin()))
                            {
                                if(players[i].getStatus() == 1)
                                {
                                    JOptionPane.showMessageDialog(null,"You win!");
                                    gameStarted = false;
                                }
                                else if(players[i].getStatus() == -1)
                                {
                                    JOptionPane.showMessageDialog(null,"You lose!");
                                    gameStarted = false;
                                }
                            }
                            else
                            {
                                if(players[i].getStatus() == 1)
                                {
                                    JOptionPane.showMessageDialog(null,players[i].getLogin()+ " win!");
                                    gameStarted = false;
                                }
                            }
                            pass.setEnabled(gameStarted);
                            hit.setEnabled(gameStarted);
                        
                            startGame.setEnabled(!gameStarted);
                            
                        }
                        try 
                        {
                            Thread.sleep(2000);
                        }
                        catch (InterruptedException ex) 
                        {
                            Logger.getLogger(ConnectionToMainServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }       
    }
    
}


public class BlackJack 
{
    private ConnectionToMainServer connection = null;
    public BlackJack()
    {
        try 
        {
            connection = new ConnectionToMainServer();
        }
        catch (NotBoundException ex) 
        {
            Logger.getLogger(BlackJack.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (MalformedURLException ex) 
        {
            Logger.getLogger(BlackJack.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (RemoteException ex) 
        {
            Logger.getLogger(BlackJack.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void startGame()
    {
        connection.init();
        connection.startGame();
    }
    public static void main(String[] args) 
    {
        try 
        {
            ConnectionToMainServer connectionToMainServer = new ConnectionToMainServer();
            connectionToMainServer.init();
            connectionToMainServer.startGame();
        }
        catch (NotBoundException ex) 
        {
            Logger.getLogger(BlackJack.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (MalformedURLException ex) 
        {
            Logger.getLogger(BlackJack.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (RemoteException ex) 
        {
            Logger.getLogger(BlackJack.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
