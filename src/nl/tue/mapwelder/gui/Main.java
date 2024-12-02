/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.mapwelder.gui;

import java.awt.BorderLayout;
import javax.swing.JFrame;

/**
 *
 * @author Wouter
 */
public class Main {
    
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("MapWelder");
        
        Data data = new Data();
        data.draw = new DrawPanel(data);
        data.side = new SidePanel(data);
        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        frame.add(data.draw, BorderLayout.CENTER);        
        frame.add(data.side, BorderLayout.WEST);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
}
