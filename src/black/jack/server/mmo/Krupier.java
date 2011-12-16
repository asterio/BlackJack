package black.jack.server.mmo;

class Krupier extends Gracz
{
    private Stos s;    

    public Krupier(Stos s)
    {
        super();
        this.s = s;        
    }
    public void start()
    {
        boolean v = true;
        while(v)
        {
            if(super.sumCards() <= 16)
            {
                addCard(s.getCard());                
            }
            else
            {
                v = false;
            }
        }
        System.out.println("Krupier():" + super.sumCards() + " KONIEC");
    }
}