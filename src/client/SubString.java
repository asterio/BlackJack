/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.StringTokenizer;

/**
 *
 * @author Asterio
 */
public class SubString
{
    private StringTokenizer str;
    private int sum;
    private String card;
    private int status;
    public SubString(String text)
    {
        str = new StringTokenizer(text,":");
        status = Integer.parseInt(str.nextToken());
        card = str.nextToken();
        sum = Integer.parseInt(str.nextToken());
    }
    public int getSum()
    {
        return sum;
    }
    public String getCard()
    {
        return card;
    }
    public int getStatus()
    {
        return status;
    }
}