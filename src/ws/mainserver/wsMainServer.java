/**
 * UWAGA komentarze od pierwszej kolumny tekstu robie na potrzeby RMI
 * rezygnyje obługi bazy danych na chwilę obecną
 */

package ws.mainserver;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.Timer;
import java.util.TimerTask;
import java.text.*;
import java.util.*;
import java.util.Date.*;
import java.text.SimpleDateFormat;

public class wsMainServer extends UnicastRemoteObject implements wsMainServerInt
{
    // MAIN uruchamia serwer RMI
    public static void main(String[] args)
    {
        try
        {
            wsMainServer serObj = new wsMainServer();
            Registry reg = LocateRegistry.createRegistry(1112);
            reg.bind("MainServer", serObj);
        }
        catch(AlreadyBoundException ee)
        {
            System.out.println(ee.getMessage());
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        System.out.println("wsMainServerRMI is ready");
    }
    
    Logger log = null;                                                      // obiekt loggera
    /*
     * mapy przechowują informacje o aktualnie zalogowanych urzytkownikach
     */
    private Map<String, Date> tokenMap = new HashMap<String,Date>();        //{token, date}
    private Map<String, String> userMap = new HashMap<String,String>();     //{user, token}

    /*
     * Obiekt HideTimer wywołuje metodę run() raz na TIMER_DELAY (sekundy * 1000 ms)
     */
    private final long  TIMER_DELAY = 15 * 60 * 1000;                   // opuznienie timera ms [m* s *ms]
    private final int  SESSION_VALID_INTERVAL = 10;                     // czas aktualności tokena minuty
    private HideTimer timerRuntimeUpdate  =  null;                      // Obiekt HideTimer dzięki ktoremu działa timer

    private Connection connection;                                      //  połączenie z bazą danych

    @Override
    public String isRMIWorkCorect() throws RemoteException
    {
        return "RMI betwen game server and main server works corect;)";
    }

    private final class ConnectionPropperties implements Serializable                           // parametry połączenia z bazą
    {
       /*
       public final String url = "jdbc:mysql://localhost:3306/test";
       public final String adminUser = "root";
       public final String adminPass = "pawelek666";
       */
        
       public final String url = "jdbc:mysql://localhost:3306/gamecenter";
       public final String adminUser = "gamecenter";
       public final String adminPass = "centrum#@01start";    
    }
    ConnectionPropperties connectionProp = new ConnectionPropperties();
    

    public wsMainServer() throws RemoteException
    {
        log = new Logger(); // pierwszy musi! powstać logger
        timerRuntimeUpdate  = new HideTimer(TIMER_DELAY);

        // rejestracja sterownika mysql
/*for RMI        try
        {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (Exception e) {
            log.addLog(Logger.ERROR,"[wsMainServer() can not register mysql.jdbc.Driver ]" + e.getMessage());
            //e.printStackTrace();
        }
for RMI END*/      
        //establishConnectionToDB(connectionProp);
        log.addLog(Logger.INFO,"[wsMainServer()] started");
    }
    /*
     * pk
     * token = {user+czas_pierwszego_poprawnego_logowania}
     * ret
     * - 0; jeżeli nie ma takiego użytkownika lub hasło niepoprawne
     * - token; i dodaje urzytkownika do map jeśli dane są poprawne (logowanie PO wygaśnięciu sesji)
     * - token; i aktualizacja czasu jeśli jest już w mapach i dane (login, hasło) są poprawne  (logowanie PRZED wygaśnięcien sesji)
     */
    @Override
    public String login(String user, String pass)
    {
        String token = "0";
//for RMI        boolean isUserPassCorrect = checkUserPassInDB(user,pass);
        boolean isUserPassCorrect = true;   //for RMI

        // czy już sie logował i kombinuje drugi raz;) ?
        // tak? -> tzn że jest już w mapach i jego token sie nie zmienił
        //      -aktualizuje tylko dla niego {token, czas}
        //      (nie sprwadzam czy sesja dla niego wygasła bo tym zajmie sie timer)
        if(isUserPassCorrect && userMap.containsKey(user))
        {
            Date date = new Date();                 //pobierz aktualny czas
            token =  (String)userMap.get(user);     //pobierz jego aktualny token i aktualizuj czas
            tokenMap.put(token, date );
            log.addLog(Logger.INFO,"[login()] user: " + user + " is (re)logging");
        }
        // czy user i pass zgadza się z danymi w bazie?
        // tak? -> dodaj jego dane do map i zwruć wygenerowany token
        else if(isUserPassCorrect)
        {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            String actualTime = dateFormat.format(date);

            // dodaj do map
            token = user + actualTime;           //zbuduj i dodaj {token, data}
            tokenMap.put(token,date);
            userMap.put(user,token);             // dodaj {user, token}
            log.addLog(Logger.INFO,"[login()] user: " + user + " is logging");
        }else
        // nie ma go w bazie (zły login lub hasło) i nie ma go w mapie
        // nie zalogował sie -> token zerowy
        {
            log.addLog(Logger.INFO,"[login()] user: " + user + " invalid password or login");
            token = "0";
        }

        return token;
    }
    /**
     * pk
     * rejestruje użytkownika o danym loginie i haśle (dodaje do bazy)
     * @return
     * true -> zarejestrowano
     * false -> login jest już zajęty
     */    
/* for RMI    public boolean registerUser(String user, String pass)
    {
        try{
            if(establishConnectionToDB(connectionProp) == true )
            {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE login LIKE '" + user + "';");
                if(rs.next()) {
                    log.addLog(Logger.INFO,"[registerUser()] user: " + user + " already exists ");
                    connection.close();
                    return false;
                }
                stmt.execute("INSERT INTO users (login, pass) VALUES( '" + user + "' , '" + pass + "');");

                stmt.close();
                connection.close();
                log.addLog(Logger.INFO,"[registerUser()] user: " + user + /*" pass: " + pass +*/ 
/*for RMI" registered");
                return true;
            }
            else
            {
                log.addLog(Logger.ERROR,"[registerUser()] connection status: fault");
                return false;
            }
        }catch (Exception e) {
            log.addLog(Logger.ERROR,e.getMessage());
            //e.printStackTrace();
        }
        return false;
    }
for RMI END*/

     /*
     * pk
     * sprawdza czy dany login jest NIE jest już zarejestrowany w bazie użytkowników
     * @ret
     * true-> login nie istnieje, można go zarejestrować
     * false-> login zajęty
     */    
/* for RMI    public boolean checkLoginAvailable(String login)
    {
        try{
            if(establishConnectionToDB(connectionProp)==true)
            {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE login LIKE '" + login + "';");
                if(rs.next()) {
                    connection.close();
                    return false;
                }
                connection.close();
            }
            else
            {
                log.addLog(Logger.ERROR,"[checkLoginAvailable()]: connection status: fault");
                return false;
            }
        }catch (Exception e) {
            log.addLog(Logger.ERROR,e.getMessage());
            //e.printStackTrace();
        }
        log.addLog(Logger.INFO,"[checkLoginAvailable()]: login: " + login + " is avaliable" );
        return true;
    }
for RMI END*/

    /**
     *  pk
     *  Metoda sprawdzająca czy przekazany jako argument wywołania token jest jeszcze aktywny,
     *  czas aktywności to SESSION_VALID_INTERVAL minut.
     *  @return
     * true -> token aktywny i aktualizacja czasu tokena
     * false -> token wygasł
    */
    public boolean isValid(String token)
    {
        // czy token jest w mapie?
        if(tokenMap.containsKey(token))
        {
             // czy sesja tokena jest aktywna?
             if(isSessionActive( (Date)tokenMap.get(token)))
             {
                 tokenMap.put(token, new Date() );  // uaktualnij czas
                 return true;
             }
             // sesja wygasła, usówa token i urzytkownika
             else
             {
                 tokenMap.remove(token);
                 log.addLog(Logger.INFO,"[isValid()] session expired" + token);
                 deleteUserByToken(token);
                 return false;
             }

        }
        // nie ma takiego tokens
        log.addLog(Logger.INFO,"[isValid()] invalid token: " + token);
        return false;
    }

    /*
     * //FUNKCJA TESTOWA
     * wypisuje na konsole servera i zwraca srtinga z informacjami o:
     * - tabeli użytkowników w bazie
     * - zawartości map: userMap i tokenMap
     */

/*for RMI    public String debugInfo()
    {
        String str ="##DEBUG_INFO##\n";
        //test log: zawartość bazy użytkowników
        try
        {
            if(establishConnectionToDB(connectionProp)==true)
            {
                str +="\n## USER DATABASE ##\n";
                Statement stmt = connection.createStatement();
                ResultSet rs =  stmt.executeQuery("select * from users;");
                while(rs.next())
                {
                     int id = rs.getInt("id");
                     String login = rs.getString("login");
                     //String pass = rs.getString("pass");
                     str += "{" + id + " " + login +  /*+ pass + */ 
/*for RMI"}\n";
                }
                stmt.close();
                connection.close();
            }
            else
            {
                str +="\n can not connect to data base\n";
            }
        }
        catch (Exception e) {
            str += e.getMessage();
            //e.printStackTrace();
        }
        //test log: zawartość map
        str += "\n## [ userMap ] ##\n" + userMap.toString();
        str += "\n ##[ tokenMap ] ##\n" + tokenMap.toString();

        //log.addLog(Logger.INFO,str);
        return str + log.getAllLog();
    }
for RMI END*/    

    //FUNKCJA TESTOWA
    /*
    public String DeleteLoginedUsers()
    {
        tokenMap.clear();
        userMap.clear();
        return "wywaliłem wszystkich zalogowanych";
    }
    */
    // --------------------------PRIVATE METHODS------------------------------------------------
    // METODY NIE DOSTĘPNE POPRZEZ WEB WERWIS, HERMETYZUJĄ MECHANIKE SERVERA
    //------------------------------------------------------------------------------------------

    /*
     * pk
     * nawiązywanie połączenia z bazą danych
     */
    private boolean establishConnectionToDB( ConnectionPropperties cp )
    {
        try
        {
            //Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(cp.url, cp.adminUser, cp.adminPass );
            if(connection== null || !connection.isValid(1)){
                log.addLog(Logger.ERROR,"[establishConnectionToDB()] unable to connect to data base: " + cp.url);
                return false;
            }
        }
        catch (Exception e) {
            log.addLog(Logger.ERROR,"[establishConnectionToDB()] unable to connect to data base: " + e.getMessage());
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     * Klasa wewnętrza HideTimer, stworzylem ją po to aby ukryć metodę run() przed klientami
     * teraz timer uruchamia się co TIMER_DELAY, i nie można wywołać metody sprzątającej z zewnątrz
     */
    private class HideTimer extends TimerTask
    {
        private Timer timerRuntimeUpdate = new Timer();

        public HideTimer(final long  TIMER_DELAY)
        {
            // planuje wywołanie timera co określony przez TIMER_DELAY czas (w milisekundach)
            timerRuntimeUpdate.schedule(this, 0, TIMER_DELAY );
            log.addLog(Logger.INFO,"[HideTimer()] STARTED");
        }

        @Override
        public void run()
        {
            // log test
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
            String current_time = format.format(date);
            log.addLog( Logger.INFO,"[run() cleanExpiredSessions] " + current_time);

            cleanExpiredSessions();     // usówa wygasłe sesje
        }
    }

    /*
     * pk
     * sprawdza czy dany login i hasło znajdują się w bazie
     * tak? -> ret true
     * nie? -> ret false + debug info
     */
    private boolean checkUserPassInDB(String user, String pass) 
    {
        try{
            if(establishConnectionToDB(connectionProp)==true)
            {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE login LIKE '" + user + "' AND pass LIKE '" + pass +"';");
                if(rs.next()) {
                    connection.close();
                    return true;
                }
                stmt.close();
                connection.close();
            }
            else
            {
                log.addLog(Logger.ERROR,"[checkUserPassInDB()]: connection status: fault");
                return false;
            }
        }catch (Exception e) {
            log.addLog(Logger.ERROR,e.getMessage());
            //e.printStackTrace();
        }
        log.addLog(Logger.INFO,"[checkUserPassInDB()]: " + user /*+ " pass: " + pass*/ + ": fault");
        return false;
    }
    /*
     * pk
     * sprawdza czy sesja o danym czasie jest nadal aktywna
     * długość sesji określa SESSION_VALID_INTERVAL w minutach
     */
    private boolean isSessionActive(Date srcDate)
    {
        Calendar now = Calendar.getInstance();                  // aktualny czas
        Calendar before = Calendar.getInstance();               // sprawdzany czas
        before.setTime(srcDate);
        before.add(Calendar.MINUTE, SESSION_VALID_INTERVAL);    // dodaje czas sesji

        if(now.compareTo(before) < 0)                           // i porównuje czy nadal jest aktywny
            return true;

        return false;
    }
    /*
     * pk
     * usówa parę z userMap o danym tokenie
     * wykorzystywana gdy sesja tokena wygasa i trzeba usunąć też z userMap token i login
     * ps. zakładamy że token zawiera nazwę loginu
     */
    private boolean deleteUserByToken(String token)
    {
        final int TOKEN_SUBSTRACT_FOR_LOGIN = 8;    // ile trzeba odjąć od długości tokena żeby się dostać do końca loginu?
        //
        String login = token.substring(0, token.length() - TOKEN_SUBSTRACT_FOR_LOGIN);
        if(userMap.remove(login).isEmpty() == false)
            return true;
        /*
        Set set = userMap.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext())
        {
            Map.Entry me = (Map.Entry)i.next();
            if( token.compareTo((String)me.getValue()) == 0)
            {
                System.out.println("[deleteUserByToken()info]" + me.getKey() + " : " + me.getValue()+ " removed");
                i.remove();
                return true;
            }
        }
         */
        return false;
    }
    /*
     * usówa wygasłe sesje z map (userMap i tokenMap)
     * iteruje przez wszystkie elementy
     */
    private void cleanExpiredSessions()
    {
        Set set = tokenMap.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext())
        {
            Map.Entry me = (Map.Entry)i.next();
            if( !isSessionActive( (Date)me.getValue()) )
            {
                log.addLog(Logger.INFO, "[cleanExpiredSessions()]" + me.getKey() + " : " + me.getValue()+ " expired");
                deleteUserByToken((String)me.getKey());
                i.remove();
            }
        }
    }

    // ------------- LOGGER -------------------
    /*
     * klasa będąca buforem logów z podziałem na 2(3) typy logów (ERROR lub INFO)
     * można dzięki temu odczytać x (MAX_BUFF_SIZE) ostatnich logów
     *
     * logi są ZAJEBIŚCIE potrzebne do debugowania, jak coś sie posypie to wiele łatwiej sie znajdzie błąd
     * do każdej istotnej funkcji radze robić logi
     */

    private class Logger
    {
        public static final int MAX_BUFF_SIZE = 10;
        private List<String> log_inf = new ArrayList<String>();;
        //private List<String> log_warn = new ArrayList<String>();;
        private List<String> log_err = new ArrayList<String>();;

        public static final int INFO =0;
        //public static final int WARNING=1;
        public static final int ERROR =2;

        public void addLog(int LOG_ID, String s)
        {
            switch(LOG_ID)
            {
                case INFO:
                {
                    if(log_inf.size() == MAX_BUFF_SIZE)
                    {
                        log_inf.remove(0);
                    }
                    log_inf.add("INFO: " + s + "\n");
                    System.out.println("INFO: "+s);
                }break;
                case ERROR:
                {
                    if(log_err.size() == MAX_BUFF_SIZE)
                    {
                        log_err.remove(0);
                    }
                    log_err.add("ERROR: " + s + "\n");
                    System.out.println("ERROR: "+s);
                }break;
            }
        }
        public String getInfoLog()
        {
            return " #INFO LOG# " + log_inf.toString() + "\n";
        }
        public String getErrorLog()
        {
            return " #ERROR LOG# " + log_err.toString() + "\n";
        }
        public String getAllLog()
        {
            return getInfoLog() + getErrorLog();
        }

    }
}

/*
 // task sample
 class Task extends TimerTask {

    private String _objectName;                 // task name
    private long _delay;                        // timer shedule interval

    public Task(String objectName, long delay) {
        _objectName = objectName;
        _delay = delay;
    }

    public void run() {
        // Get current date/time and format it for output
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        String current_time = format.format(date);
        // Output to user the name of the objecet and the current time
        System.out.println(_objectName + " - Current time: " + current_time);
    }
    public long getDelay(){
        return _delay;
    }
}
 *
 */