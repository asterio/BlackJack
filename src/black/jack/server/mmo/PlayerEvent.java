package black.jack.server.mmo;

import java.io.Serializable;

/**
 *
 * @author spinacz
 * obiekt z aktualną informacją na temat graczy wysyłany w formie tablicy
 * do klienta w odpowiedzi na metode anyEvents
 */
public class PlayerEvent implements Serializable
{
    // private
    private String login;
    private String cards;
    private byte pointsSum;
    private byte status;
    public PlayerEvent()
    {
        
    }

    public PlayerEvent(String l, String c, byte p, byte s)
    {
        login = l;
        cards = c;
        pointsSum = p;
        status = s;
    }
    
    //public method`s
    //get
    public String getLogin()
    {
        return login;
    }

    public String getCards()
    {
        return cards;
    }

    public byte getPointsSum()
    {
        return pointsSum;
    }

    public byte getStatus()
    {
        return status;
    }

    //set
    public void setLogin(String l)
    {
        login = l;
    }

    public void setCards(String c)
    {
        cards = c;
    }

    public void setPointsSum(byte p)
    {
        pointsSum = p;
    }

    public void setStatus(byte s)
    {
        status = s;
    }
}
