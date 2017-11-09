import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import javax.management.modelmbean.XMLParseException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


public class AVPlayer implements MouseListener, MouseMotionListener {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	JPanel panel,currStrip;
	int width = 352;
	int height = 288;
	private final int EXTERNAL_BUFFER_SIZE = 524288;
	
	BufferedImage img;
	public int[] byteIndicies ;
	Timer fps;
	public static File soundFile,file;
	public static InputStream is, sis;
	public static long len, slen;
	public static byte[] bytes, sbytes;
	public static AudioInputStream audioInputStream;
	public static boolean vidFlag;
	public static int state,startFrame;
	public static int currFrame=0;
	public static Thread soundThread;
	public static SourceDataLine dataLine;
	public static boolean isAlreadyPlaying = false;

	public AVPlayer(String video, String audio, int[] byteIndicies) {
		
		this.byteIndicies = byteIndicies;
		
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			file = new File(video);
			is = new FileInputStream(file);
			soundFile = new File(audio);
			sis = new FileInputStream(soundFile);
			len = file.length();
			slen = soundFile.length();

			bytes = new byte[(int)len];
			sbytes = new byte[(int)slen];
			audioInputStream = null;
			try {
			    audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			} catch (UnsupportedAudioFileException e1) {
			    new PlayWaveException(e1);
			} catch (IOException e1) {
			    new PlayWaveException(e1);
			}

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			 vidFlag = false;
			 fps = new Timer(50, new refreshFrame());
			 fps.setInitialDelay(50);

			int ind = 0;
			for(int y = 0; y < height; y++){

				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x,y,pix);
					ind++;
				}
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Use labels to display the images
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		showImage(frame.getContentPane());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setPreferredSize(new Dimension(width, 50));
		frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);


		MyButton playButton = new MyButton("Play");
		playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(playButton);

		MyButton pauseButton = new MyButton("Pause");
		pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(pauseButton);

		MyButton stopButton = new MyButton("Stop");
		stopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(stopButton);

			frame.pack();
		    frame.setVisible(true);

	   }
	
//		GridBagLayout gLayout = new GridBagLayout();
//		frame.getContentPane().setLayout(gLayout);
//
//		JLabel lbText1 = new JLabel("Video: " + video);
//		lbText1.setHorizontalAlignment(SwingConstants.LEFT);
//		JLabel lbText2 = new JLabel("Audio: " + audio);
//		lbText2.setHorizontalAlignment(SwingConstants.LEFT);
//		lbIm1 = new JLabel(new ImageIcon(img));
//
//		GridBagConstraints c = new GridBagConstraints();
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.anchor = GridBagConstraints.CENTER;
//		c.weightx = 0.5;
//		c.gridx = 0;
//		c.gridy = 0;
//		frame.getContentPane().add(lbText1, c);
//
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.anchor = GridBagConstraints.CENTER;
//		c.weightx = 0.5;
//		c.gridx = 0;
//		c.gridy = 1;
//		frame.getContentPane().add(lbText2, c);
//
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.gridx = 0;
//		c.gridy = 2;
//		frame.getContentPane().add(lbIm1, c);
//
//		frame.pack();
//		frame.setVisible(true);


	

	public void playWAV(String filename){
		// opens the inputStream
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		// initializes the playSound Object
		PlaySound playSound = new PlaySound(inputStream);

		// plays the sound
		try {
			playSound.play();
		} catch (PlayWaveException e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		}
	
	public BufferedImage refreshFrame(int currFrame) {
		// get new picture
    	int ind = byteIndicies[currFrame];
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind+height*width];
				byte b = bytes[ind+height*width*2];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x,y,pix);
				ind++;
			}
    	}
		return img;
	}
	public void showImage(Container pane) {

		   panel.removeAll();
		   panel.setLayout(new BorderLayout());
		   JLabel label = new JLabel(new ImageIcon(img));
		   label.setPreferredSize(new Dimension(img.getWidth()+100, img.getHeight()));
		   //original.add(label, BorderLayout.CENTER);
		   panel.add(label, BorderLayout.CENTER);
		   panel.revalidate();
		   panel.repaint();
		   pane.add(panel, BorderLayout.CENTER);

	   }
	
	public void videoOriginal(BufferedImage img) {
		// update image
		panel.removeAll();
	   panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
	   label.setPreferredSize(new Dimension(img.getWidth()+100, img.getHeight()));
	   panel.add(label, BorderLayout.CENTER);
	   panel.revalidate();
	   panel.repaint();
	}
	
	public void buttonPressed(String name)
	{
		if (name.equals("Play")&&isAlreadyPlaying==false) { // Play
			state = 0;
			soundThread = null;
			soundThread = new Thread(new RefreshSound());
			fps.start();
			soundThread.start();
			isAlreadyPlaying = true;
		} else if (name.equals("Pause")) { // Pause
			state = 1;
			fps.stop();
			try {
				audioInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			soundThread.interrupt();
			dataLine.stop();
			dataLine.flush();
			dataLine.close();
		} else if (name.equals("Stop")) { // Stop
			state = 2;
			//fps.stop();
			currFrame=0;
			BufferedImage f = refreshFrame(currFrame);
			//if (view == 0) {
			videoOriginal(f);
			isAlreadyPlaying = false;
			soundThread.interrupt();
			dataLine.stop();
			dataLine.flush();
			dataLine.close();
		} else if (name.equals("Close")) { // close
			System.exit(0);
		} 
	}

	
class refreshFrame implements ActionListener {
	public void actionPerformed(ActionEvent e) {
		if (state == 0) { // play
			++currFrame;
			if (currFrame == 720) {
				currFrame = 0;
				soundThread.interrupt();
				dataLine.stop();
				dataLine.flush();
				dataLine.close();
				soundThread = null;
				soundThread = new Thread(new RefreshSound());
				soundThread.start();
			}
			img = refreshFrame(currFrame);
			videoOriginal(img);
		} else if (state == 1) { // pause
			BufferedImage f = refreshFrame(currFrame);
			videoOriginal(f);
			fps.stop();
		} else if (state == 2) { // stop
			BufferedImage f = refreshFrame(currFrame);
			videoOriginal(f);
			fps.stop();
		}
		}
	}
class MyButton extends JButton {
	MyButton(String label){
		setFont(new Font("Helvetica", Font.BOLD, 10));
		setText(label);
		addMouseListener(
			new MouseAdapter() {
  				public void mousePressed(MouseEvent e)
  				{
					buttonPressed(getText());
				}
			}
		);
	}

	MyButton(String label, ImageIcon icon){
		Image img = icon.getImage();
		Image scaleimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		setIcon(new ImageIcon(scaleimg));
		setName(label);
		addMouseListener(
			new MouseAdapter() {
  				public void mousePressed(MouseEvent e) {
					buttonPressed(getName());
				}
			}
		);
	}
}
public class RefreshSound implements Runnable {
	public void run() {
			try {
				sis = new FileInputStream(soundFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long slen = soundFile.length();
			sbytes = new byte[(int) slen];

			audioInputStream = null;
			try {
		    audioInputStream = AudioSystem.getAudioInputStream(soundFile);
				} catch (UnsupportedAudioFileException e1) {
		    new PlayWaveException(e1);
				} catch (IOException e1) {
		    new PlayWaveException(e1);
		}
					AudioFormat audioFormat = audioInputStream.getFormat();
					Info info = new Info(SourceDataLine.class, audioFormat);

					// opens the audio channel

					dataLine = null;
					try {
					    dataLine = (SourceDataLine) AudioSystem.getLine(info);
					    dataLine.open(audioFormat, EXTERNAL_BUFFER_SIZE);
					} catch (LineUnavailableException e1) {
					    new PlayWaveException(e1);
					}

		dataLine.start();

		int readBytes = 0;
		byte[] audioBuffer = new byte[EXTERNAL_BUFFER_SIZE];
		try {
			audioInputStream.skip((long) (currFrame*7357.0));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
		    while (readBytes != -1) {

			readBytes = audioInputStream.read(audioBuffer, 0,
				audioBuffer.length);

				if (readBytes >= 0){
			    dataLine.write(audioBuffer, 0, readBytes);
				}
		    }
		} catch (IOException e1) {
		    new PlayWaveException(e1);
		}


	}
}
}
