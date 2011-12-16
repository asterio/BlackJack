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
public class TwoCardsSub
{
    private String status;
    private List<String> cards = new ArrayList<String>();
    private String suma;
    public TwoCardsSub(String tekst)
    {
        System.out.println("TwoCards:" + tekst);
        StringTokenizer str = new StringTokenizer(tekst,":");
        status = str.nextToken();
        cards.add(str.nextToken());
        cards.add(str.nextToken());
        suma = str.nextToken();
    }
    public String getSuma()
    {
        return suma;
    }
    public String status()
    {
        return status;
    }
    public List<String> getCards()
    {
        return cards;
    }
}
