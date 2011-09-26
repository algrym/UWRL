package org.toadking.games.underwaterroguelike;

import javax.swing.JFrame;

public class UnderWaterRogueLike {
    public UnderWaterRogueLike() {
	// Do ridiculous things so the Mac recognizes our name
	System.setProperty("apple.laf.useScreenMenuBar", "true");
	System.setProperty("com.apple.mrj.application.apple.menu.about.name",
		"UWRL");
	
	// 1. Create a frame.
	JFrame frame = new JFrame("Underwater Rogue-Like");

	// 2. Describe what happens when the frame closes
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	// 3. Create the GamePanel component and add it to the frame.
	frame.getContentPane().add(new GamePanel());
	
	// 4. Size the frame.
	frame.pack();

	// 5. Show it.
	frame.setVisible(true);
    }

    public static void main(String[] args) {
	new UnderWaterRogueLike();
    }
}
