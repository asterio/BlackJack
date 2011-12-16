/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author Asterio
 */
public class Player
{
    private String sum;
    private String status;
    private List<String> cards = new ArrayList<String>();
    private String name;
    public Player(String tekst)
    {
       StringTokenizer str = new StringTokenizer(tekst,":");
       name = str.nextToken();
       status = str.nextToken();
       cards.add(str.nextToken());
       if(str.hasMoreTokens())
            cards.add(str.nextToken());
    }
    public String getSum()
    {
        return sum;
    }
    public String getStatus()
    {
        return status;
    }
    public String getName()
    {
        return name;
    }
    public String getCards()
    {
        String tekst = "";
        for(int i = 0; i < cards.size(); i++)
        {
            tekst +=cards.get(i).toString();
        }
        return tekst;
    }
}
