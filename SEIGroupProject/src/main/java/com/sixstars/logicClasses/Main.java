package com.sixstars.logicClasses;

import com.sixstars.ui.WelcomePage;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("This is working!");
        SwingUtilities.invokeLater(()->new WelcomePage());

    }
}
