//
//  JavaQTMovieView.java
//
//  Created by Albert Russel on 12/30/08.
//  Copyright 2008 MPI - Max Planck Institute for Psycholinguistics, Nijmegen
//

import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import player.*;


/*
 terugzetten in eigen frame gaat ook al niet helemaal goed, voorlopig opgeven
 commando's geven gaat asynchroon, daarom misschien delay, 
 synchroon geprobeerd, geeft beachball bij start/stop
 */
/**
 * A simple test class for experimentation. Won't work out of the box, a lot of code is 
 * commented out.
 */
public class JavaQTMovieView  implements ActionListener {
	private JButton loadButton;
	private JButton playButton;
	private JButton ffButton;
	private JButton fbButton;
	private JButton selButton;
	private JButton detachButton;
	private JButton drawButton;
	
	private static boolean playing;
	private static Listner l;
	private JavaQTMoviePlayer view = null;
	private JavaQTMoviePlayer view2 = null;
	private JavaQTMoviePlayer view9;
	private JFrame video1;
	
	private static String filePath1 = "/Users/Shared/MPI/Dev_LAT/resources/testdata/elan/elan-example1.mpg";
	private static String filePath2 = "/Users/Shared/MPI/Demo material/yele/r03_v20_s5.mpg";
	
	private static class Listner implements JavaQTMoviePlayerCreatedListener {
		public void playerCreated(JavaQTMoviePlayer player) {
			//System.out.println("View Created: " + player.getId());
		}
	}
	
	public static void main (String args[]) {
		JavaQTMovieView vv = new JavaQTMovieView();
	}
	
    public JavaQTMovieView () {
        System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
		System.out.println("Java version: " +
				 System.getProperty("java.version"));
    	System.out.println("Runtime version: " +
				 System.getProperty("java.runtime.version"));
        //System.out.println("Started application");
       
		l = new Listner();
		
		try {
			view = new JavaQTMoviePlayer(filePath1, l);//, 5000, 0.5f, 0.5f);
		} catch (NoCocoaPlayerException npe) {
			System.out.println(npe.getMessage());
		}
		//view.createMovie(filePath1);
		
		//System.out.println("new view");
		//System.out.println("frame dur: " + view.getFrameDuration());
		
		video1 = new JFrame("video 1");
		//video1.getContentPane().setLayout(null);
//		video1.getContentPane().setLayout(new GridLayout(2, 3));
		video1.getContentPane().setLayout(new GridLayout(1, 2));
		video1.setSize(500, 500);
		video1.setLocation(600, 200);
		video1.setVisible(true);
		if (view != null) {
			video1.getContentPane().add(view);
		}
	//	view.setLocation(100, 100);
	//	view.setSize(600, 600);
		//System.out.println("added view");
		//video1.setSize(401, 400);
		
		try {
			view2 = new JavaQTMoviePlayer(filePath2, null);//, 5000, 0.5f, 0.5f);
		} catch (NoCocoaPlayerException npe) {
			System.out.println(npe.getMessage());
			view2 = null;
		}
		
//		final JavaQTMoviePlayer view3 = new JavaQTMoviePlayer(filePath1, null);//, 5000, 0.5f, 0.5f);
//		final JavaQTMoviePlayer view4 = new JavaQTMoviePlayer(filePath1, null);//, 5000, 0.5f, 0.5f);
//		final JavaQTMoviePlayer view5 = new JavaQTMoviePlayer(filePath1);//, 5000, 0.5f, 0.5f);
//		final JavaQTMoviePlayer view6 = new JavaQTMoviePlayer(filePath1);//, 5000, 0.5f, 0.5f);
		//view9 = view2;
		if (view2 != null) {
			video1.getContentPane().add(view2);
			view.addListener(view2);
		}
		//view2.setOffset(2000);
		//view2.setLocation(100, 400);view2.setSize(300, 200);
//		video1.getContentPane().add(view3);view.addListener(view3);
		//view3.setLocation(100, 100);view3.setSize(90, 100);
//		video1.getContentPane().add(view4);view.addListener(view4);
		//view4.setLocation(0, 100);view4.setSize(90, 100);
//		video1.getContentPane().add(view5);
		//		video1.getContentPane().add(view6);

		
		video1.pack();
		
		
		
		//DRAWING CODE
	
		 //SAMPLE USAGE OF addXXXToDrawingElementList(.....)
		 //This is a little faster than adding individual elements, do not forget to call sendDrawingElementList()
		/*view.clearDrawingElementList();
		for (int i = 0; i < 200000; i++) {
			view.addLineToDrawingElementList(40, 140, 0.1f, 0.1f, 0.1f, 0.6f, 0, 0, 255, 1f, 3);
		}
		view.sendDrawingElementList();
		 */
		 
//		
		view.addRectangle(1000, 2000, 0.4f, 0.4f, 0.3f, 0.3f, 0, 255, 0, 0.9f, 10, false);
		view.addRectangle(4000, 5000, 0.1f, 0.1f, 0.2f, 0.2f, 255, 0, 0, 1f, 5, false);
		view.addRectangle(1000, 2000, 0.7f, 0.7f, 0.3f, 0.3f, 0, 0, 255, 0.2f, 1, true);
		view.addRectangle(1000, 2000, 0.0f, 0.0f, 0.1f, 0.1f, 0, 0, 255, 0.9f, 1, true);
		view.addLine(1500, 2500, 0.2f, 0.8f, 0.7f, 0.4f, 0, 255, 255, 0.9f, 20);
		view.addEllipse(1500, 2500, 0.5f, 0.5f, 0.3f, 0.3f, 255, 255, 0, 0.5f, 1, true);
		view.addEllipse(500, 1500, 0.5f, 0.5f, 0.5f, 0.3f, 255, 255, 0, 0.5f, 3, false);
		view.addEllipse(2500, 3500, 0.5f, 0.5f, 0.3f, 0.5f, 255, 255, 0, 0.5f, 1, true);
		view.addLine(1000, 2500, 0.0f, 0.999f, 0.7f, 0.999f, 255, 0, 0, 1f, 1);
		view.addString("KNOEP!", 1000, 2000, 0.85f, 0.05f, 0, 0, 255, 0.9f, "Verdana", 12);
		for (int i = 0; i < 100; i++) {
			view.addString("KNOEP!", i * 40, (i + 1) * 40, i * 0.01f, 0.5f, 0, 0, 0, 1f, "Verdana", 24);
			view.addEllipse(i * 40, (i + 1) * 40, 0.5f, 0.5f, i * 0.005f, 0.5f - i * 0.005f, 255, 0, 0, 0.5f, 1, true);
			view.addLine(i * 40, (i + 1) * 40, 0.3f, 0.3f, 0.3f + i * 0.004f, 0.4f - i * 0.002f, 0, 0, 255, 1f, 3);
		}
		//
//		view.removeAllElementsBetween(1000, 2000);
//		view.setDrawingPeriod(15);
					
		
		// END DRAWING CODE
		
		
		
		JPanel buttons = new JPanel();
		loadButton = new JButton("Goto 0");
		playButton = new JButton("Play");
		ffButton = new JButton(">");
		fbButton = new JButton("<");
		selButton = new JButton("Sel");
		detachButton = new JButton("Detach");
		drawButton = new JButton("Draw");
		
		final JPanel nopPanel = new JPanel();
		

		
		loadButton.addActionListener(this);
		playButton.addActionListener(this);
		ffButton.addActionListener(this);
		fbButton.addActionListener(this);
		selButton.addActionListener(this);
		detachButton.addActionListener(this);
		drawButton.addActionListener(this);
		buttons.add(loadButton);
		buttons.add(playButton);
		buttons.add(fbButton);
		buttons.add(ffButton);
		buttons.add(selButton);
		buttons.add(detachButton);
		buttons.add(drawButton);
		
		JFrame frame = new JFrame("QT Test");
		frame.setLocation(700, 800);
		frame.getContentPane().add(buttons, BorderLayout.SOUTH);
		frame.setVisible(true);
		frame.pack();
    }

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loadButton) {
			//view.createQTMovie("/Users/albertr/Data/MPEG2.mpeg");
			//view.createQTMovie(filePath1);
			//video1.setSize(401, 400);
			//System.out.println("duration: " + view.getMediaDuration());
			//System.out.println("frame dur: " + view.getFrameDuration());
			//System.out.println("rate: " + view.getRate());
			if (view != null) {
				view.setMediaTime(0);
			}
			//view.setLocation(300, 600);
			//view.setSize(100, 100);
			
			
			//	final JavaQTMoviePlayer view9 = new JavaQTMoviePlayer(filePath1, null);//, 5000, 0.5f, 0.5f);
			//	view9.createMovie(filePath1);
			
			//			byte[] imageBytes = view.getFrame(1500, 500, 350);
			//			System.out.println("Image size: " + imageBytes.length);
			
			//			System.out.println("1 valid: " + view.isMovieValid());
			//			System.out.println("2 valid: " + view2.isMovieValid());
			//			System.out.println("3 valid: " + view3.isMovieValid());
			//			System.out.println("4 valid: " + view4.isMovieValid());
			/*			try {
			 FileOutputStream fos = new FileOutputStream("/Users/albertr/tmp/pict02.png");
			 fos.write(imageBytes);
			 } catch (Exception ex) {
			 ex.printStackTrace();
			 }*/
			//			view.cleanUpOnClose();
			//			video1.remove(view);
			//view.setRate(0.5f);
			//view.setVolume(0.5f);
		} else if (e.getSource() == playButton) {
			if (view != null) {
				if (playing) {
					view.stop();
					playing = false;
				} else {
					view.start();
					playing = true;
				}
			}
		} else if (e.getSource() == ffButton) {
			if (view != null) {
				view.nextFrame();
				//	view.setDrawingVisible(false);
			}
		} else if (e.getSource() == fbButton) {
			if (view != null) {
				view.previousFrame();
				//	view.setDrawingVisible(true);
			}
		} else if (e.getSource() == selButton) {
			if (view != null){
				view.playInterval(500, 540);
				//view.playInterval(1361836, 1362786);				
			}
		} else if (e.getSource() == detachButton) {
			
			//System.out.println("w: " + view.getNaturalWidth() + "  h: " + view.getNaturalHeight());
			
			
			//video1.getContentPane().remove(view4);
			if (view != null) {
				view.setOffset(5000);
			}
			if (view2 != null) {
				view2.setOffset(10000);
			}
			
			//view.cleanUpOnClose();
			/*			
			 System.out.println("Creating player");
			 final JavaQTMoviePlayer view5 = new JavaQTMoviePlayer(filePath1, 5500, 0.5f, 0.5f, null);
			 final JPanel nop = new JPanel();
			 nop.add(view5);
			 System.out.println("added to Panel");
			 */		
			/*				
			 final JDialog video2 = new JDialog();
			 video2.setSize(400, 400);
			 video2.setLocation(350, 100);
			 //video2.setVisible(true);
			 video2.add(view5);
			 video2.pack();
			 //view.addListener(view5);
			 view5.start();
			 
			 view5.setMediaTime(9500);
			 view5.setVolume(0.2f);
			 view5.setRate(2.1f);	
			 System.out.println("video: " + view5.hasVideo());				
			 
			 System.out.println("frame dur: " + view5.getFrameDuration());
			 */
			
			
			//view.getFrame(15500, "/Users/albertr/tmp/pict01.tiff");
			//byte[] imageBytes = view.getFrame(15500);
			//System.out.println("Image size: " + imageBytes.length);
			
			/*	try {
			 FileOutputStream fos = new FileOutputStream("/Users/albertr/tmp/pict02.tiff");
			 fos.write(imageBytes);
			 } catch (Exception ex) {
			 ex.printStackTrace();
			 }*/
			
			/*
			 Toolkit toolkit = Toolkit.getDefaultToolkit();
			 Image image = toolkit.createImage(imageBytes, 0, imageBytes.length) ;
			 
			 JFrame fr = new JFrame("image");
			 fr.setSize(200, 200);
			 fr.setLocation(200, 200);
			 fr.setVisible(true);
			 fr.getContentPane().add(new JLabel(new ImageIcon(image)));
			 */
			
			
			/*if (video1.getContentPane() != nopPanel) {
			 view.stop();
			 video1.getContentPane().remove(view);
			 //view.removeNotify();
			 video2.getContentPane().add(view);
			 //view.addNotify();
			 //Container con = video1.getContentPane();
			 video1.setContentPane(nopPanel);
			 //video2.setContentPane(con);
			 view.validate();
			 } else {
			 view.stop();
			 video2.getContentPane().remove(view);
			 //view.removeNotify();
			 video1.getContentPane().add(view);
			 //view.addNotify();
			 //video1.setContentPane(video2.getContentPane());
			 //video2.setContentPane(nopPanel);
			 view.validate();
			 }*/
		} else if (e.getSource() == drawButton) {
			if (view != null) {
				view.createDrawingView();
				
				view.addRectangle(1000, 2000, 0.4f, 0.4f, 0.3f, 0.3f, 0, 255, 0, 0.9f, 10, false);
				view.addRectangle(4000, 5000, 0.1f, 0.1f, 0.2f, 0.2f, 255, 0, 0, 1f, 5, false);
				view.addRectangle(1000, 2000, 0.7f, 0.7f, 0.3f, 0.3f, 0, 0, 255, 0.2f, 1, true);
				view.addRectangle(1000, 2000, 0.0f, 0.0f, 0.1f, 0.1f, 0, 0, 255, 0.9f, 1, true);
				view.addLine(1500, 2500, 0.2f, 0.8f, 0.7f, 0.4f, 0, 255, 255, 0.9f, 20);
				view.addEllipse(1500, 2500, 0.5f, 0.5f, 0.3f, 0.3f, 255, 255, 0, 0.5f, 1, true);
				view.addEllipse(500, 1500, 0.5f, 0.5f, 0.5f, 0.3f, 255, 255, 0, 0.5f, 3, false);
				view.addEllipse(2500, 3500, 0.5f, 0.5f, 0.3f, 0.5f, 255, 255, 0, 0.5f, 1, true);
				view.addLine(1000, 2500, 0.0f, 0.999f, 0.7f, 0.999f, 255, 0, 0, 1f, 1);
				view.addString("KNOEP!", 1000, 2000, 0.85f, 0.05f, 0, 0, 255, 0.9f, "Verdana", 12);
			}
			
		}
		
	}
}
