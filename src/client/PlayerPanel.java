/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
import java.awt.GridLayout;
import javax.swing.*;
/**
 *
 * @author Asterio
 */
public class PlayerPanel extends JPanel
{
    private JLabel score = new JLabel();
    //private JLabel cards = new JLabel();
    private JPanel cards = new JPanel();
    private String tekst;
    private String user;
    private int tokens;
    public PlayerPanel(String tekst, int w)
    {
        this.tekst = tekst;
        this.setBorder(BorderFactory.createTitledBorder(tekst));
        this.setBounds(0, w, 400, 110);
        this.setLayout(new GridLayout(2,3));
        this.add(new JLabel("Cards:"));
        this.cards.setLayout(new GridLayout(1,5));
        this.cards.setSize(100, 350);
        this.add(cards);
        this.add(new JLabel("Score:"));
        this.add(score);
        this.user = tekst;
    }
    public int getTokens()
    {
        return tokens;
    }
    public void setTokens(int t)
    {
        this.tokens = t;
    }

    public String getUser()
    {
        return user;
    }
    public void setUser(String user)
    {
        this.user = user;
    }
    public JPanel getPanel()
    {
        return this;
    }
    public JLabel getScore()
    {
        return score;
    }
    /*public JLabel getCards()
    {
        return cards;
    }*/
    public JPanel getCards()
    {
        return cards;
    }

}
