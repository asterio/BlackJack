/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Asterio
 */
public class OpponentSubString
{
    private StringTokenizer str;
    private int sum;
    private int status;
    private int count;
    private ArrayList<String> cards = new ArrayList<String>();
    public OpponentSubString(String text)
    {
        str = new StringTokenizer(text,":");
        status = Integer.parseInt(str.nextToken());
        count = Integer.parseInt(str.nextToken());
        for(int i = 0; i < count; i++)
        {
            cards.add(str.nextToken());
        }
        sum = Integer.parseInt(str.nextToken());
    }
    public int getSum()
    {
        return sum;
    }
    public int getStatus()
    {
        return status;
    }
    public int getCount()
    {
        return count;
    }
    public String getCards()
    {
        String card = "";
        for(int i = 0; i < cards.size(); i++)
        {
            card += cards.get(i).toString();
        }
        return card;
    }
    public ArrayList<String> getCard()
    {
        return cards;
    }
}
