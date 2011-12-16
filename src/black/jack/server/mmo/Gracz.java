package black.jack.server.mmo;

import java.util.ArrayList;

class Gracz
{
    protected  int sum;
    protected int counter;
    protected ArrayList<Card> listaC = new ArrayList<Card>();
    private String cards = "";
    private byte status = 127;

    public byte getStatus()
    {
        return status;
    }

    public void setStatus(byte s)
    {
        status = s;
    }

    public Gracz()
    {
        sum = 0;
        counter = 0;
    }

    public void addCard(Card c)
    {
        listaC.add(c);
        if(c.getNr() == 11)
        {
            if(sum < 11)
                sum += 11;
            else
                sum += 1;
        }
        else sum += c.getNr();
        cards += getPrev().info()+":";
        counter++;
    }
    public int sumCards()
    {
        System.out.println("sumCards():"+sum);
        return sum;
    }
    public int countCards()
    {
        return counter;
    }
    public Card getPrev()
    {
        return listaC.get(listaC.size()-1);
    }
    public Card getCard(int i)
    {
        return listaC.get(i-1);
    }
    public String getCards()
    {
        return cards;
    }
}