package black.jack.server.mmo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Wersja Alfa serwera w trybie MMO, chce nadać mu jakąś funkcjonalność aby dało
 * się grać w kilka osób przy jednym stole i kilku stołach równocześnie, jak to będzie działać
 * pomyśli się o optymalizacji i rozbutowie funkcjonalności.
 */

/*
 * -- usunieto nie potrzebne metody
 * -- krupiera przeniesiono na poczatek tabeli zdarzen
 * -- sporo zmian w kodzie bez konsekwencji zewnetrznych
 */


class BlackJackGame
{
    // zmienne potrzebne w realizacji stołu BlackJack`a

    private Stos stos;                                  // przechowuje przetasowane karty
    private Krupier krupier;                            // krupier wiadomo
    private Map<String, Gracz> activePlayers;           // mapa graczy bioracych udzial w rozdaniu
    private Map<String, Boolean> PlayersMap;            // mapa  graczy znajdujących się przy stole zawiera również informacje czy gracz bierze udzial w rozgrywce
    private Map<String, Boolean> playerTook2Cards;      // mapa z informacja czy gracz pobral 2 pierwsze karty i odkryta karte krupiera
    private Map<String, Boolean> passPlayers;           // mapa z informacją czy gracz spasował
    private Map<String, Integer> sumMap;                // Mapa pomocnicza do analizy wyników
    private Map<String, Date> activityMap;              // Mapa czasów potrzeban do sprawdzenia czy akcja zostałą wykonana w odpowiednimczasie
    private byte finishedCounter = 0;                   // licznik graczy ktorzy zkaonczyli rozgrywke w danym rozdaniu
    private boolean gameStarted = false;                // informacja czy gra zostala wogole rozpoczeta
    private boolean canStart = false;                   // informacja czy można wywołać metode startGame
    private StartGameTimer startGameTimer;              // timer odliczajacy czas do rozpoczecia gry
    private ActionTimer actionTimer;                    // timer sprawdzający czas ostatniej akcji
    private String tableID;                             // identyfikator stołu
    
//************************************************************************************************
    //Metody publiczne dostepne na zewnątrz

    /**
     * Podstawowy konstruktor, stworzenie stolu wiąże się z koniecznością obecności
     * przy nim co najmniej jednego gracza
     * @param token
     */
    public BlackJackGame(String token, String id)
    {
        activePlayers = new HashMap<String, Gracz>();
        PlayersMap = new HashMap<String, Boolean>();
        playerTook2Cards = new HashMap<String, Boolean>();
        passPlayers = new HashMap<String, Boolean>();
        sumMap = new HashMap<String, Integer>();
        activityMap = new HashMap<String, Date>();

        PlayersMap.put(token, false);
        tableID = id;
    }

    /**
     * metoda pozwala na dodanie garacza do stołu, pod warunkiem ze ich liczba
     * jest mniejsza od 7
     *
     * @param token
     * @return 1 - dodanao gracza, i rozpoczyna on gre od zaraz
     * @return 2 - dodanao gracza, ale aktualnie trwa rozgrywka
     * @return -1 - nie dodano gracza stół pelny
     */
    public List getEventsList(String token)
    {
        List tab = new ArrayList();
        int i = 0;        
        Set<String> k = activePlayers.keySet();
        for( String keyX : k)
        {
            tab.add( new PlayerEvent(keyX.substring(0, keyX.length()-8),
                                     activePlayers.get(keyX).getCards(), (byte)activePlayers.get(keyX).sumCards(), activePlayers.get(keyX).getStatus()));
        }
        tab.add( new PlayerEvent("krupier", krupier.getCards(), (byte)krupier.sumCards(), krupier.getStatus()));
        return tab;
    }
    public String addPlayer(String token)
    {
        if(isLessThan7())
        {            
            PlayersMap.put(token, false);
            if(gameStarted)
                return "2";
            return "1";
        }
        return "-1";
    }

    /**
     * Metoda pozwalająca graczowi wyrazić chęć wzięcia udziału w grze, kiedy pierwszy
     * z graczy znajdujacych sie przy stole ja wywoła, pozostali mają 10 sekund na
     * wyrażenie chęci wzięcia udziału w rozdaniu, w przeciwnym razie w tej kolejce zostana
     * pominięci
     * @param token
     * @return  1 - dodano gracza do bierzącego rozdania, gra rozpocznie sie za kilka sekund<BR>
     *          -1 - trwa rozgrywka, poczekaj na jej zakończenie
     */
    public String takeAPartInGame(String token)
    {
        System.out.println("takeAPartInGame: " + token);
        if(gameStarted == false)
        {
            if(canStart)
            {                
                PlayersMap.put(token, true);
                activePlayers.put(token, new Gracz());
                return "1";
            }            
            activePlayers.clear();
            Set<String> key = PlayersMap.keySet();
            for(String k : key)
                PlayersMap.put(k, false);

            PlayersMap.put(token, true);
            activePlayers.put(token, new Gracz());
            canStart = true;
            startGameTimer = new StartGameTimer();
            System.out.println("odliczanie do rozpoczęcia gry trwa: ");
            return "1";
        }
        return "-1";
    }

    /**
     * Metoda służy to pobrania kart z początkowego rozdania
     * @param token
     * @return String:
     *       Format Stringa:  "1||0 : 1_karta : 2_karta : suma" gdzie: <BR>
     *              1 - gracz wygral <BR>
     *              0 - gracz gra dalej <BR>
                    karta - zwraca aktualnie wyciągnietą karte<BR>
     *              suma - zwraca sume pkt gracza<BR>
     *       -2 - gra nie zostałą ropoczęta <BR>
     *       -3 - gracz pobrał już swoje karty <BR>
     *       -4 - gracz nie bierze udzialu w rozgrywce
     */
    public String takeTwoCards(String token)
    {
        if(gameStarted)
        {
            if(activePlayers.containsKey(token))
            {
                if(playerTook2Cards.get(token).equals(false))       // sprawdzamy czy gracz nie pobrał informacji o kartach wcześniej
                {
                    String ret = "";                    
                    int sum = activePlayers.get(token).sumCards();  // obliczenie sumy pkt z dwóch kart gracza

                    if (sum < 21)       // mozna grac dalej
                    {
                        activePlayers.get(token).setStatus((byte)0);
                        ret += 0 +":";
                           playerTook2Cards.put(token, true);
                    }
                    else // Black Jack, gracz wygrywa z krupierem
                    {
                        activePlayers.get(token).setStatus((byte)1);
                        ret += 1 + ":";
                            //  playerTook2Cards.put(token, false);  // gracz pobierze karty, ale zkonczy gre
                                                                     // wiec nie bedzie i tak mógł pobrać kart po raz kolejny
                        passPlayers.put(token, true);
                        ++finishedCounter;
                        if(finishedCounter == activePlayers.size())
                            gamePass();
                    }
                    ret += activePlayers.get(token).getCard(1).info() +":";
                    ret += activePlayers.get(token).getCard(2).info() +":";
                    ret += sum;

                    activityMap.put(token, new Date());                      // zapisywany jest aktualczy czas od którego gracz ma 10 sekund na podjęcie akcji

                    return ret;
                }
                return "-3";
            }
            return "-4";
        }
        return "-2";
    }

    /**
     * metoda pozwalajaca na dobranie karty przez gracza
     * zwraca String z informacją o postepie w grze.
     * string jest parsowany po stronie klienta;
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
     *          -5 - gracz spasowal wczesniej <BR>
     */
    public String hit(String token)
    {
        if(gameStarted)
        {
            if (activePlayers.containsKey(token))
            {
                if(playerTook2Cards.get(token).equals(true))
                {
                    if(passPlayers.get(token).equals(false))
                    {
                        System.out.println("hit(): ");
                        activePlayers.get(token).addCard(stos.getCard());
                        int wynik = activePlayers.get(token).sumCards();

                        activityMap.put(token, new Date());                      // zapisywany jest aktualczy czas od którego gracz ma 10 sekund na podjęcie akcji

                        //LOG
                        System.out.println("hit(): Aktualny wynik gracza:" + wynik);
                        if(wynik == 21)
                        {
                            activePlayers.get(token).setStatus((byte)1);                            
                            passPlayers.put(token, true);
                            ++finishedCounter;
                            if(finishedCounter == activePlayers.size())
                                gamePass();
                            //LOG
                                System.out.println("hit(): " + "1:" + activePlayers.get(token).getPrev().info() + ":" + wynik);
                            return("1:" + activePlayers.get(token).getPrev().info() + ":" + wynik);
                        }
                        else if(wynik > 21)
                        {
                            activePlayers.get(token).setStatus((byte)-1);                            
                            passPlayers.put(token, true);
                            ++finishedCounter;
                            if(finishedCounter == activePlayers.size())
                                gamePass();
                            //LOG
                                System.out.println("hit(): " + "-1:" + activePlayers.get(token).getPrev().info() + ":" + wynik);
                            return("-1:" + activePlayers.get(token).getPrev().info() + ":" + wynik);
                        }
                        activePlayers.get(token).setStatus((byte)0);
                        //LOG
                            System.out.println("hit(): " + "0:" + activePlayers.get(token).getPrev().info() + ":" + wynik);
                        return("0:" + activePlayers.get(token).getPrev().info() + ":" + wynik);
                    }
                    return "-5";
                }
                return "-3";
            }
            return "-4";
        }
        return "-2";
    }


    /**
     * metoda wywoływana gdy gracz decyduje sie nie dobierać wiecej kart,
     * nastepuje obliczenie sumy pkt gracza i krupiera, następnie jest wysyłana
     * informacja zwrotna
     * @param token
     * @return String: <BR>
     *          1 - spasowano<BR>
     *          -2 - gra nie zostala rozpoczeta <BR>
     *          -3 - gracz nie pobral poczatkowych kart <BR>
     *          -4 - gracz nie bierze udzialu w rozgrywce <BR>
     *          -5 - gracz spasowal wczesniej <BR>
     */
    public String pass(String token)
    {
        if(gameStarted)
        {
            if (activePlayers.containsKey(token))
            {
                if(playerTook2Cards.get(token).equals(true))
                {
                    if(passPlayers.get(token).equals(false))
                    {
                        passPlayers.put(token, true);
                        ++finishedCounter;
                        if(finishedCounter == activePlayers.size())
                            gamePass();
                        return "1";
                    }
                    return "-5";
                }
                return "-3";
            }
            return "-4";
        }
        return "-2";
    }

    /**
     * Metoda do usuwania gracza ze stołu
     * @param token
     * @return 1 - usunieta poprawnie
     *         0 - nie usunieto
     */
    public boolean  removePlayer(String token)
    {
        if(PlayersMap.containsKey(token))
        {
            if(activePlayers.containsKey(token))
                activePlayers.remove(token);
            if(playerTook2Cards.containsKey(token))
                playerTook2Cards.remove(token);
            if(passPlayers.containsKey(token))
                passPlayers.remove(token);
            if(sumMap.containsKey(token))
                sumMap.remove(token);

            PlayersMap.remove(token);

            if(finishedCounter == activePlayers.size())
                gamePass();
            return true;
        }
        return false;
    }

    /**
     * Metoda zdarzeń zwraca tablice graczy i krupiera traktowanego jako gracz
     * przechowuje 4 wartośći:
     * login ;
     * cards - lista kart posiadanych przez gracza;
     * pointsSum suma pkt ;
     * status - informacja czy graczy wygrał, przegrał, gra dalej itp. itd. a nawet etc.
     * @param token
     * @return
     */
    public PlayerEvent[] getEvents(String token)
    {
        PlayerEvent tab[] = new PlayerEvent[activePlayers.size()+1];
        int i = 0;        
        Set<String> k = activePlayers.keySet();
        for( String keyX : k)
        {
            tab[i] = new PlayerEvent(keyX.substring(0, keyX.length()-8),
                                     activePlayers.get(keyX).getCards(), (byte)activePlayers.get(keyX).sumCards(), activePlayers.get(keyX).getStatus());
            ++i;
        }
        tab[i] = new PlayerEvent("krupier", krupier.getCards(), (byte)krupier.sumCards(), krupier.getStatus());
        return tab;
    }

    public String DebugInfo()
    {
        String debug = "Lista userow przy stole: ";
        Set<String> k = PlayersMap.keySet();                   // kazdy z graczy dostaje do swojej kolejki zdarzen
        for( String keyX : k)                                       // informacje o kartach krupiera
        {
            debug += keyX + " __ ";
        }
        return debug;
    }

    public boolean isStarted()
    {
        return gameStarted;
    }    

    public int getNumberOfPlayers()
    {
        return PlayersMap.size();
    }

    public boolean isLessThan7()
    {
        if(PlayersMap.size() < 7)
            return true;
        return false;
    }

    
//##############################################################################################
    // mechanizmy wewnętrzne

    /**
     * metoda pozwalająca na rozpoczecie nowej gry
     * wywołanie jej jest konieczne żeby móc zagrać w gre
     *
     * @value String token - przyjmuje token który został zwrócony zalogowaniu na wsMainServer
     * @return  1  gra zostala rozpoczeta
     * @return -1 nie wszyscy gracze zakonczyli poprzednia gre
     */
    private void startGame()
    {
        if(!gameStarted)
        {
            stos = new Stos();              // przygotowanie tali
            krupier = new Krupier(stos);    // i krupiera
            finishedCounter = 0;

            for(byte i = 0; i < 2 ;++i)                                 // rozdanie po 2 karty
            {
                krupier.addCard(stos.getCard());                        //  krupierowi
                Set<String> keys = activePlayers.keySet();
                for( String key : keys)
                {
                    activePlayers.get(key).addCard(stos.getCard());     // i graczom
                }
            }

            activityMap.clear();
            Set<String> k = activePlayers.keySet();                         // kazdy z graczy dostaje do swojej kolejki zdarzen
            for( String keyX : k)                                       // informacje o drugiej karcie krupiera
            {
                activityMap.put(keyX, new Date());                      // zapisywany jest aktualczy czas od którego gracz ma 10 sekund na podjęcie akcji
                passPlayers.put(keyX, false);
                playerTook2Cards.put(keyX, false);
            }
            
            gameStarted = true;                 // rozpoczeto gre przy stole

            startGameTimer.cancel();
            startGameTimer = null;              // w czasie gry nie trzeba odliczać do rozpoczęcia gry

            actionTimer = new ActionTimer();
            canStart = false;                   // zmienna musi być false aby można było później od nowa dodać graczy do rozdania
            //LOG
                System.out.println("startGame(): GRA ZOSTAŁA ROZPOCZETA");
        }
    }

    /**
     * Metoda zawiera mechanizm zakończenia gry w chwili kiedy wszyscy gracze spasuja lub system
     * na podstawie punktów stwierdzi wygraną lub przegraną
     */
    private void gamePass()
    {
        gameStarted = false;
        actionTimer.cancel();
        actionTimer = null;
        sumMap.clear();
        krupier.start();
        
        Set<String> k = activePlayers.keySet();                         // kazdy z graczy dostaje do swojej kolejki zdarzen
        for( String keyX : k)                                           // informacje o kartach krupiera
            sumMap.put(keyX, activePlayers.get(keyX).sumCards());
        
        krupier.setStatus((byte)6);

        boolean isWiner = false;                        // czy któryś gracz ma oczko
        List<String> list = new ArrayList<String>();    // lista graczy którzy mają oczko
        for( String keyX : k)
        {
            if( sumMap.get(keyX) == 21)
            {
                activePlayers.get(keyX).setStatus((byte)1);
                list.add(keyX);
                isWiner = true;
            }
        }

        if(isWiner)     // skoro jest gracz z oczkiem to dodaje informacje
        {               // o przegranej pozostałych graczy i krupiera
            for( String keyX : k)
            {
                if(!list.contains(keyX))
                    activePlayers.get(keyX).setStatus((byte)-1);
                
                krupier.setStatus((byte)-1);
            }
        }

        if(isWiner == false)    // nie ma gracza z oczkiem to analizuje wyniki graczy i krupiera
        {
            List<String> tmp = new ArrayList<String>();     // zmienna z listą zwycięzców
            int wyn = 0;
            boolean krupierIsWinner = false;

            if(krupier.sumCards() == 21)    // krupier ma oczko i inni gracze nie mieli => krupier wygray gre
            {
                krupier.setStatus((byte)1);
                krupierIsWinner = true;
            }
            else if(krupier.sumCards() < 21) // jezeli krupier nie przekroszył 21 pkt to jego wynik jest baza
                wyn = krupier.sumCards();    // do analizy wyników pozostałych graczy

            if(krupierIsWinner == false)
            {
                for( String keyX : k)
                {
                    if( sumMap.get(keyX) < 21)
                    {
                        if(sumMap.get(keyX) > wyn)
                        {
                            tmp.clear();
                            wyn = sumMap.get(keyX);     // nowy najwyższy wynik bazowy
                            tmp.add(keyX);
                        }
                        else if(sumMap.get(keyX) == wyn)
                               tmp.add(keyX);
                    }
                }
                for(String keyX : k)
                {
                    if(tmp.size() > 0)
                    {                        
                        krupier.setStatus((byte)-1);
                        if(tmp.contains(keyX))
                            activePlayers.get(keyX).setStatus((byte)1);
                        else
                            activePlayers.get(keyX).setStatus((byte)-1);
                    }
                    else
                    {
                        krupier.setStatus((byte)1);
                        for( String key : k)
                            activePlayers.get(key).setStatus((byte)-1);                        
                    }
                }
            }
        }
    }


    /**
     * Timer liczy czas do rozpoczęcia gry
     */

    class StartGameTimer extends TimerTask
    {
        public StartGameTimer()
        {
            // planuje wywołanie timera co określony przez TIMER_DELAY czas (w milisekundach)
            timerUpdate.schedule(this, 0, TIMER_DELAY);

            System.out.println("StartGameTimer() STARTED");
        }
        @Override
        public void run()
        {
            try
            {
                Thread.sleep(10000);
                startGame();
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(BlackJackServerMMO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        private Timer timerUpdate = new Timer();
        private final long TIMER_DELAY = 10*1000;  // minuty * sekundy * milisekundy
    }

    class ActionTimer extends TimerTask
    {
        public ActionTimer()
        {
            // planuje wywołanie timera co określony przez TIMER_DELAY czas (w milisekundach)
            timerUpdate.schedule(this, 0, TIMER_DELAY);

            System.out.println("ActionTimer() STARTED");
        }
        @Override
        public void run()
        {
            System.out.println("ActionTimer: run()");
            Set<String> k = activityMap.keySet();
            for(String key : k)
            {
                Calendar now = Calendar.getInstance();                  // aktualny czas
                Calendar before = Calendar.getInstance();               // sprawdzany czas
                before.setTime(activityMap.get(key));
                before.add(Calendar.SECOND, 15);    // dodaje czas sesji

                /**
                 * the value 0 if the time represented by the argument is equal to the time
                 * represented by this Calendar; a value less than 0 if the time of this Calendar
                 * is before the time represented by the argument; and a value greater than 0 if
                 * the time of this Calendar is after the time represented by the argument.
                 */
                if(now.compareTo(before) > 0)                           // i porównuje czy nadal jest aktywny
                    pass(key);                
            }
        }
        private Timer timerUpdate = new Timer();
        private final long TIMER_DELAY = 15*1000;  // minuty * sekundy * milisekundy
    }
}
