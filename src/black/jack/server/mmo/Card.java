package black.jack.server.mmo;

class Card
{
    private int val;
    private int nr;

    public Card(int val, int nr)
    {
        this.val = val;
        this.nr = nr;
    }

    public int getVal()
    {
        return val;
    }
    public int getNr()
    {
        int v;
        switch(nr)
        {
            case 11:
                v = 10;
                break;
            case 12:
                v = 10;
                break;
            case 13:
                v = 10;
                break;
            case 14:
                v = 11;
                break;
            default:
                v = nr;
        }
        return v;
    }

    private String getName()
    {
        String name = "";
        switch(nr)
        {
            case 2:
               name = "2";
               break;
            case 3:
                name = "3";
                break;
            case 4:
                name = "4";
                break;
            case 5:
                name = "5";
                break;
            case 6:
                name = "6";
                break;
            case 7:
                name = "7";
                break;
            case 8:
                name = "8";
                break;
            case 9:
                name = "9";
                break;
            case 10:
               name = "10";
                break;
            case 11:
                name = "Walet";
                break;
            case 12:
                name = "Dama";
                break;
            case 13:
               name = "Król";
               break;
            case 14:
               name = "As";
               break;

        }
        return name;
    }
    private String getColor()
    {
        String color="";
        switch(val)
        {
            case 0:
                color = "Kier";
                break;
            case 1:
                color = "Karo";
                break;
            case 2:
                color = "Wino";
                break;
            case 3:
                color = "Trefl";
                break;
        }
        return color;
    }
    public String info()
    {
        return getName()+ "_"+getColor();
    }
}