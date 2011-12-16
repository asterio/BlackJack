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
public class OpponentsSub 
{
    private int count;
    private List<Player> list = new ArrayList<Player>();
    private List<String> tokens = new ArrayList<String>();
    public OpponentsSub(String tekst)
    {
        StringTokenizer str = new StringTokenizer(tekst,"&");

        count = str.countTokens();
        while(str.hasMoreTokens())
        {
            tokens.add(str.nextToken());             
        }
        for(int i = 0; i < tokens.size(); i++)
        {
            if(tokens.get(i).equals("10") || tokens.get(i).equals("11"))
            {
                System.out.println(tokens.get(i));
                count--;
            }
            else
            {
                list.add(new Player(tokens.get(i)));
            }
        }
    }
    public int getCount()
    {
        return count;
    }
    public List<Player> getList()
    {
        return list;
    }
}

