package black.jack.server.mmo;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
//import ws.mainserver.WsMainServerConection;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws.mainserver.wsMainServerInt;

public class BlackJackServerMMO extends UnicastRemoteObject implements BlackJackServerMMOInt
{

  
    public static void main(String [] argv) throws RemoteException
    {
        try
        {
            BlackJackServerMMO serMMO = new BlackJackServerMMO();
            //serMMO.test();            
            //Registry reg = LocateRegistry.createRegistry(2222);
            Registry reg = LocateRegistry.createRegistry(1113);
            reg.rebind("GameServer", serMMO);
        }
        
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.println("BlackJackServerMMO is ready");
    }




///************************************************************************************************************************    
    // zmienne potrzebne serwerowi do działania
//RMI    private WsMainServerConection connectToMainServer;
    private ws.mainserver.wsMainServerInt connectToMainServer;
    private Map<String, BlackJackGame> tableMap;    // Lista stołów, w Stringu jest przechowywane id stołu
    private Map<String, String> userMap;            // Lista zalogowanych uzytkowników, mapa wiąże uzytkownika ze stołem
    private CleanerTimer timer;                     // usuwa z userMap, i ze stołów użytkowników któych sesja wygasła
    private int maxValueOfTable = 15;



    public BlackJackServerMMO() throws RemoteException
    {
        tableMap = new HashMap<String, BlackJackGame>();
        userMap = new HashMap<String, String>();
        timer = new CleanerTimer();
        try
        {
            System.out.println("BlackJackServerMMO is starting...");
//RMI            connectToMainServer = new WsMainServerConection();
            Registry reg = LocateRegistry.getRegistry(1112);
            connectToMainServer = (ws.mainserver.wsMainServerInt) reg.lookup("MainServer");
            System.out.println(connectToMainServer.isRMIWorkCorect());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
//RMI            e.printStackTrace();
        }
    }
    
    @Override
    public String isRMIWorkCorect() throws RemoteException
    {
        return "RMI betwen game server and client works corect;)";
    }

///****************************************************************************************************************************
    //metody publiczne dostępne na zwenątrz

    /**
     * Metoda pozwala klientowi pobrac liste stołow dostepnych na serwerze
     * oraz liczbe graczy przy kazdym z nich. Jeżeli libcza graczy przy stole wynosi 7
     * do tego stołu nie można już się dosiąść. Maxymalna liczba stołów na chwile obecną to 15.
     * @return null - brak stołów, można nadać nowe id w celu jego stworzenia <BR>
     *         String - numberOfTables_tableID:numberOfPlayers_tableID:numberOfPlayers_... etc.
     */
    @Override
    public String takeListOfTables2()
    {
        if(tableMap.isEmpty())
            return null;
        String retInfo = "" + tableMap.size();
        Set<String> keys = tableMap.keySet();
        int i = 0;
        for( String key : keys)
        {
            if(tableMap.get(key).getNumberOfPlayers() <= 0)
                tableMap.remove(key);
            else
            retInfo += "_" +key + ":" + tableMap.get(key).getNumberOfPlayers();
        }
        return retInfo;
    }

    /**     
     * Metoda logowania na serwerze gry na podstawie tokenu,
     * pozwala na dodanie gracza do stołu którego id jest przekazane
     * jako drugi argument funkcji pod warunkiem że liczba graczy przy nim nie
     * jest równa 7. Jeżeli liczba stołów jest mniejsza od 15 to jaki drugi argument
     * można porzekazać nowe ID w celu utworzenia nowego stołu dla gracza.
     *
     * @param String token, String tableId
     * @return String: <BR>
     *          1 - zalogowan poprawnie<BR>
     *          0 - gracz był zalogowany wcześnie, oznacza to, że zostaje przy tym samym stole <BR>
     *          null - user jest nie zalogowany na main serwerze<BR>
     *          -1  - nie ma miejsca przy stole, liczba graczy równa 7.<BR>
     *          -2  - na serwerze aktualnie nie ma miejsca dla kolejnego stołu
     */
//RMI    public String loginOnBJS(String token, String tableId)
    @Override
    public String loginOnBJS(String token, String tableId) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            if(userMap.containsKey(token))
                return "0";

            if(tableMap.isEmpty())
            {
                tableMap.put(tableId, new BlackJackGame(token, tableId));
                userMap.put(token, tableId);
                return "1";
            }

            if(tableMap.containsKey(tableId))
            {
                if(tableMap.get(tableId).isLessThan7())
                {
                    tableMap.get(tableId).addPlayer(token);
                    userMap.put(token, tableId);
                    return "1";
                }
                return "-1";    // too many user at the table
            }

            if(tableMap.size() < maxValueOfTable)
            {
                tableMap.put(tableId, new BlackJackGame(token, tableId));
                userMap.put(token, tableId);
                return "1";
            }
            return "-2";
        }
        return null;
    }

    /**
     * Metoda po przez wywołanie której gracz wyraża chęć wzięcia udziału
     * w kolejnym rozdaniu. Po wywołaniu metody przez pierwszego gracza pozostali
     * mają 10 sekund na jej wywołanie, jest to niezbędne aby mogli wziąć udział
     * w rozdaniu. Po upływie 10 sekund od pierwszego wywołania metody krupier rozdaje karty.
     * @param token
     * @return  1 - dodano gracza do bierzącego rozdania, gra rozpocznie sie za kilka sekund<BR>
     *          -1 - trwa rozgrywka, poczekaj na jej zakończenie<BR>
     *          NULL - uzytkownik nie zalogowany lub sesja wygasła<BR>
     */
//RMI    public String takePartInGame(String token)
    @Override
    public String takePartInGame(String token) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            return tableMap.get(userMap.get(token)).takeAPartInGame(token);
        }
        return null;
    }

    /**
     * Metode gracz musi wywolac po rozdaniu kart przez krupiera tylko raz, aby odebrac karty
     * z pierwszego rozdania kolejne wywolanie metody w jednym rozdaniu spowoduje zwrocenie
     * informacji o błędzie. Karta krupiera, i karty pozostałych graczy można użyskać poprzez
     * wywołanie metody anyEvents(String token)     *
     * @param token
     * @return String:
     *       Format Stringa:  "1||0 : 1_karta : 2_karta : suma" gdzie: <BR>
     *              1 - gracz wygral <BR>
     *              0 - gracz gra dalej <BR>
     *       -2 - gra nie zostałą ropoczęta <BR>
     *       -3 - gracz pobrał już swoje karty <BR>
     *       -4 - gracz nie bierze udzialu w rozgrywce<BR>
     *       NULL - uzytkownik nie zalogowany lub sesja wygasła<BR>
     */
//RMI    public String takeTwoCards(String token)
    @Override
    public String takeTwoCards(String token) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            return tableMap.get(userMap.get(token)).takeTwoCards(token);
        }
        return null;
    }
    
    /**
     * Metoda wywoływana w celu dobrania karty przez gracza, zwraca drazu informacje
     * o dobranej karcie, pozostali gracze uzyskują ją po wywołaniu metody enyEvents()
     * @parm token
     * @return String:
     *          Format Stringa:  "1||0||-1 : karta : suma" gdzie: <BR>
     *              1 - gracz wygral <BR>
     *              0 - gracz gra dalej <BR>
     *              -1 - przegral <BR>
     *              karta - zwraca aktualnie wyciągnietą karte <BR>
     *              suma - zwraca sume pkt gracza <BR>
     *          -2 - gra nie zostala rozpoczeta <BR>
     *          -3 - gracz nie pobral poczatkowych kart <BR>
     *          -4 - gracz nie bierze udzialu w rozgrywce <BR>
     *          -5 - gracz spasowal wczesniej lub wygrał albo przegrał już wcześniej<BR>
     *          NULL - uzytkownik nie zalogowany lub sesja wygasła<BR>
     */
//RMI    public String hit(String token)
    @Override
    public String hit(String token) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            return tableMap.get(userMap.get(token)).hit(token);
        }
        return null;
    }

    /**
     * metoda wywoływana kiedy gracz nie chce już dobirać kart
     * @param token
     * @return String: <BR>
     *          1 - spasowano<BR>
     *          -2 - gra nie zostala rozpoczeta <BR>
     *          -3 - gracz nie pobral poczatkowych kart <BR>
     *          -4 - gracz nie bierze udzialu w rozgrywce <BR>
     *          -5 - gracz spasowal wczesniej lub wygrał albo przegrał już wcześniej<BR>
     *          NULL - uzytkownik nie zalogowany lub sesja wygasła<BR>
     */
//RMI    public String pass(String token) throws
    @Override
    public String pass(String token) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            return tableMap.get(userMap.get(token)).pass(token);
        }
        return null;
    }

    /**
     * Metoda którą należy wywołać w przypadku nagłego zamknięcia gry przez użytkownika,
     * albo poprosu żeby go wylogować
     * @param token
     */
    @Override
    public void stop(String token)
    {
        tableMap.get(userMap.get(token)).removePlayer(token);
            userMap.remove(token);
    }

    /**
     * Metoda pozwala na zmiane stołu przez gracza
     * @param token
     * @param tableId
     * @return String: <BR>
     *          1 - zmieniono stół poprawnie<BR>
     *          -1 - za dużo użytkowników przy stole<BR>
     *          -2 - liczba stołów jest za duża aby dodać kolejny<BR>
     *          -3 - nie udało się usunąć użytkownika z poprzedniego stołu<BR>
     *          -10 - użytkonik nie jest zalogowany na BJSMMO, ten komunikat powinien być możliwy w każdej funkcji<BR>
     *                ale nie pomyślałem wcześniej a mieszać po stronie klienta w tym co już jest i
     *                chwilowo nie poprawiam wcześniejszych funkcjionalności
     */
//RMI    public String changeTable(String token, String tableId)
    @Override
    public String changeTable(String token, String tableId) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            if(tableMap.get(userMap.get(token)).removePlayer(token))
            {
                if(!userMap.containsKey(token))
                    return "-10";    // użytkownik jest nie zalogowany na BJSMMO
                if(tableMap.containsKey(tableId))
                {
                    if(tableMap.get(tableId).isLessThan7())
                    {
                        tableMap.get(tableId).addPlayer(token);
                        return "1";     // doda usera do stołu
                    }
                    return "-1";    // too many user at the table
                }

                if(tableMap.size() < maxValueOfTable)
                {
                    tableMap.put(tableId, new BlackJackGame(token, tableId));
                    return "1";     // dodano usera do stolu
                }
                return "-2";
            }
            return "-3"; // użytkownik nie został poprawnie usunięty ze stołu
        }
        return null;
    }

    
    /**
     * metoda powinna być wywoływana przez klienta w petli(timerze) co 2-3 sekundy aby
     * odpytac serwer o ewentualne zmiany zwiazane z innymi graczami.     *
     * @param token
     * @return Metoda zwraca tablice graczy w której przechowywane są informacje o graczach, 
     * ostatni w tablic jest krupier (planowane przeniesienie na początek, jak dasz znać że masz na to czas)<BR>
     * zawartość obirktu gracz:<BR>
     *          - nick <BR>
     *          - karty w formie string`a <BR>
     *          - suma pkt <BR>
     *          - status: <BR>
     *                    -1 - przegrana <BR>
     *                     0 - gracz gra dalej <BR>
     *                     1 - wygrana  <BR>
     */
//RMI    public PlayerEvent[] anyEvents3(String token)
    @Override
    public PlayerEvent getPlayerEvent() throws RemoteException
    {
        PlayerEvent p1 = new PlayerEvent("asterio","costam",Byte.parseByte("12"),Byte.parseByte("1"));
        /*p1.setLogin("Asterio");
        p1.setCards("CosTam");
        p1.setPointsSum(Byte.parseByte("12"));
        p1.setStatus(Byte.parseByte("1"));*/
        return p1;
    }
    @Override
    public PlayerEvent[] getPlayerEvents() throws RemoteException
    {
        PlayerEvent[] p1 = { new PlayerEvent("asterio","costam",Byte.parseByte("12"),Byte.parseByte("1")),new PlayerEvent("asterio","costam",Byte.parseByte("12"),Byte.parseByte("1")),new PlayerEvent("asterio","costam",Byte.parseByte("12"),Byte.parseByte("1"))};
        /*p1.setLogin("Asterio");
        p1.setCards("CosTam");
        p1.setPointsSum(Byte.parseByte("12"));
        p1.setStatus(Byte.parseByte("1"));*/
        return p1;
    }
    @Override
    public PlayerEvent[] anyEvents3(String token) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            return tableMap.get(userMap.get(token)).getEvents(token);
        }
        return null;
    }
    @Override
    public List anyEvents4(String token) throws RemoteException
    {
        if(connectToMainServer.isValid(token))
        {
            return (tableMap.get(userMap.get(token)).getEventsList(token));
        }
        return null;
    }


    /**
     *  metoda zwraca sting z listą stołów i uzytkownikami przy tym stole
     */
    @Override
    public String debugInfo()
    {
        String debug = "Liczba stołów: " + tableMap.size() +"\n";
        //LOG
            System.out.println("debugInfo(): ");

        Set<String> keys = tableMap.keySet();
        for( String key : keys)
        {
            debug += "table ID: " + key +" :__: " + tableMap.get(key).DebugInfo() + "\n\n";
        }//*/
        return debug;
    }

    /**
     * Metoda wykozystywana podczas testów, pozwala na szybkie wyczyszcenie map
     * @return
     */
    @Override
    public String DebugDeleteLoginedUsers()
    {
        tableMap.clear();
        userMap.clear();
        return "wywaliłem wszystkich zalogowanych i wszystkie stolu";
    }


///###########################################################################################################################3
    // MECHANIZMY WEWNETRZNE SERWERA
    class CleanerTimer extends TimerTask implements Serializable
    {
        public CleanerTimer()
        {
            // planuje wywołanie timera co określony przez TIMER_DELAY czas (w milisekundach)
            timerUpdate.schedule(this, 0, TIMER_DELAY);

            System.out.println("CleanerTimer() STARTED");
        }
        @Override
        public void run()
        {
            try
            {
                //LOG
                    System.out.println("run(): ");
                cleanExpiredUsers();
            }
            catch (RemoteException ex)
            {
                Logger.getLogger(BlackJackServerMMO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private Timer timerUpdate = new Timer();
        private final long TIMER_DELAY = 10*60*1000;  // minuty * sekundy * milisekundy
    }

//RMI    private void cleanExpiredUsers()
    private void cleanExpiredUsers() throws RemoteException
    {
        //LOG
            System.out.println("cleanExpiredUsers(): ");

        Set<String> keys = userMap.keySet();
        for( String key : keys)
        {
            if(!connectToMainServer.isValid(key))
            {
                //LOG
                    System.out.println("cleanExpiredUsers(): remove user: " + key);
                tableMap.get(userMap.get(key)).removePlayer(key);
                userMap.remove(key);
            }
        }
        Set<String> keyss = tableMap.keySet();
        for( String key : keyss)
        {
            if(tableMap.get(key).getNumberOfPlayers() <= 0)
            {
                //LOG
                    System.out.println("cleanExpiredUsers(): remove ampty table: " + key);
                tableMap.remove(key);
            }
        }
    }
    
    @Override
    public void test() throws RemoteException
    {
        //#######################################################################
        //TEST CODE!!        
        wsMainServerInt con = connectToMainServer;
        String spinacz = con.login("spinacz", "spinacz");
//        String spinacz1 = con.login("spinacz1", "spinacz1");
//        String test = con.login("test", "test");
        String ktos = con.login("ktos", "ktos");
//        String xyz = con.login("XYZ", "XYZ");

        

        System.out.println(takeListOfTables2());

        // Logowanie, na koncu cod zwrotny
        System.out.println("logowanie: " + ktos + " " + " __: " + loginOnBJS(ktos, "1") );
        System.out.println("logowanie: " + spinacz + " " + " __: " + loginOnBJS(spinacz, "1") );
//        System.out.println("logowanie: " + spinacz1 + " " + " __: " + loginOnBJS(spinacz1, "2") );
//        System.out.println("logowanie: " + test + " " + " __: " + loginOnBJS(test, "2") );
//        System.out.println("logowanie: " + xyz + " " + " __: " + loginOnBJS(xyz, "1") );

        // Stoły
        System.out.println(takeListOfTables2());
        System.out.println(debugInfo());

        System.out.println("takeAPartInGame: " + ktos + " __: " + takePartInGame(ktos));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlackJackServerMMO.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("anyEvents: " + spinacz + " __: " + anyEvents3(spinacz));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlackJackServerMMO.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("takeAPartInGame: " + spinacz + " __: " + takePartInGame(spinacz));
//        System.out.println("takeAPartInGame: " + spinacz1 + " __: " + takePartInGame(spinacz1));
//        System.out.println("takeAPartInGame: " + ktos + " __: " + takePartInGame(ktos));
//        System.out.println("takeAPartInGame: " + xyz + " __: " + takePartInGame(xyz));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlackJackServerMMO.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("takeAFirstTwoCards: " + ktos + " __: " + takeTwoCards(ktos));
        System.out.println("takeAFirstTwoCards: " + spinacz + " __: " + takeTwoCards(spinacz));
//        System.out.println("takeAFirstTwoCards: " + spinacz1 + " __: " + takeTwoCards(spinacz1));
//        System.out.println("takeAFirstTwoCards: " + xyz + " __: " + takeTwoCards(xyz));
//        System.out.println("takeAFirstTwoCards: " + test + " __: " + takeTwoCards(test));

        System.out.println("anyEvents: " + ktos + " __: " + anyEvents3(ktos));
        System.out.println("anyEvents: " + spinacz + " __: " + anyEvents3(spinacz));
//        System.out.println("anyEvents: " + xyz + " __: " + anyEvents(xyz));
//        System.out.println("anyEvents: " + test + " __: " + anyEvents(test));
//        System.out.println("anyEvents: " + spinacz1 + " __: " + anyEvents(spinacz1));

//        System.out.println("anyEvents: " + spinacz1 + " __: " + anyEvents(spinacz1));
//        System.out.println("anyEvents: " + xyz + " __: " + anyEvents(xyz));

        System.out.println("hit: " + ktos + " __: " + hit(ktos));
        System.out.println("hit: " + ktos + " __: " + hit(ktos));
        System.out.println("hit: " + ktos + " __: " + hit(ktos));
        System.out.println("hit: " + spinacz + " __: " + hit(spinacz));
//        System.out.println("hit: " + xyz + " __: " + hit(xyz));

        System.out.println("anyEvents: " + ktos + " __: " + anyEvents3(ktos));
        System.out.println("anyEvents: " + spinacz + " __: " + anyEvents3(spinacz));
//        System.out.println("anyEvents: " + xyz + " __: " + anyEvents(xyz));
//        System.out.println("anyEvents: " + test + " __: " + anyEvents(test));
//        System.out.println("anyEvents: " + spinacz1 + " __: " + anyEvents(spinacz1));

        System.out.println("pass: " + ktos + " __: " + pass(ktos));
        System.out.println("pass: " + spinacz + " __: " + pass(spinacz));
//        System.out.println("pass: " + xyz + " __: " + pass(xyz));
//        System.out.println("pass: " + test + " __: " + pass(test));
//        System.out.println("pass: " + spinacz1 + " __: " + pass(spinacz1));

        System.out.println("anyEvents: " + ktos + " __: " + anyEvents3(ktos));
        System.out.println("anyEvents: " + spinacz + " __: " + anyEvents3(spinacz));
//        System.out.println("anyEvents: " + xyz + " __: " + anyEvents(xyz));
//        System.out.println("anyEvents: " + test + " __: " + anyEvents(test));
//        System.out.println("anyEvents: " + spinacz1 + " __: " + anyEvents(spinacz1));

        System.exit(0);
        //END OF TEST CODE
        //#####################################################################//
         //*/
    }
}