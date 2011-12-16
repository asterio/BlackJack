package black.jack.server.mmo;

import java.util.ArrayList;

class Stos
{
    private int actual;
    private ArrayList<Card> lista = new ArrayList<Card>();

    public Stos()
    {
        actual = 0;
        for(int i = 0; i < 4; i++)
        {
            for ( int suit = 0; suit <= 3; suit++ )
            {
                for ( int value = 2; value <= 14; value++ )
                {
                    lista.add(new Card(suit,value));
                }
            }
        }
        System.out.println("Stos()");
        shuffle();
    }

    public void showCards()
    {
        for(int i = 0; i < lista.size(); i++)
        {
//            System.out.println(lista.get(i).info());
        }
    }

    private void shuffle()
    {
        java.util.Collections.shuffle(lista);
//        System.out.println("Stos():shuffle()");
    }
    public Card getCard()
    {
        Card c = lista.get(actual);
        actual +=1;
//        System.out.println("getCard(): " +c.getNr());
        return c;
    }
}
