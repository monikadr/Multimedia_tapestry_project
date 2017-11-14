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
	JPanel panel, imagesPanel,sliderPanel,buttonPanel,tapestry;
	int width = 352;
	int height = 288;
	private final int EXTERNAL_BUFFER_SIZE = 524288;

	BufferedImage img;
	public int[] byteIndicies;
	Timer fps;
	public static File soundFile, file;
	public static InputStream is, sis;
	public static long len, slen;
	public static byte[] bytes, sbytes;
	public static AudioInputStream audioInputStream;
	public static boolean vidFlag;
	public static int state, startFrame;
	public static int currFrame = 0;
	public static Thread soundThread;
	public static SourceDataLine dataLine;
	public static boolean isAlreadyPlaying = false;
	public static JSlider slider;

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

			bytes = new byte[(int) len];
			sbytes = new byte[(int) slen];
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
			while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			vidFlag = false;
			fps = new Timer(46, new refreshFrame());
			fps.setInitialDelay(46);

			int ind = 0;
			for (int y = 0; y < height; y++) {

				for (int x = 0; x < width; x++) {

					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind + height * width];
					byte b = bytes[ind + height * width * 2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x, y, pix);
					ind++;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// setting slider, stop, pause, play buttons
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		showImage(frame.getContentPane());

		// adding slider to buttom
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
		sliderPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Spacing
		slider = new JSlider();
		slider.setValue(0);
		slider.setMinimum(0);
		slider.setMaximum(6000);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setAutoscrolls(true);
		slider.setPreferredSize(new Dimension(width, 50));
		startFrame = currFrame;

		slider.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();

				currFrame = (int) ((e.getX() * 1.85 * 9) + startFrame);
				img = refreshFrame(currFrame);
				videoOriginal(img);
				try {
					audioInputStream.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				dataLine.stop();
				dataLine.flush();
				dataLine.close();
				soundThread = new Thread(new PlaySound());
				fps.start();
				soundThread.start();
				slider.setValue(currFrame);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

		});

		sliderPanel.add(slider);

		// adding button

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		MyButton playButton = new MyButton("Play");
		buttonPanel.add(playButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
		MyButton pauseButton = new MyButton("Pause");
		buttonPanel.add(pauseButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
		MyButton stopButton = new MyButton("Stop");
		buttonPanel.add(stopButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		sliderPanel.add(buttonPanel);

		// adding tapestry image

		tapestry = new JPanel();
		// to do adding image to panel
		
		tapestry.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				int x = e.getX();
				int y = e.getY();
				// get the clickedFrame
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		sliderPanel.add(tapestry);


		//adding sliderPanel to frame
		frame.getContentPane().add(sliderPanel, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);

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
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				byte a = 0;
				byte r = bytes[ind];
				byte g = bytes[ind + height * width];
				byte b = bytes[ind + height * width * 2];

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
				img.setRGB(x, y, pix);
				ind++;
			}
		}
		return img;
	}

	public void showImage(Container pane) {

		panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
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
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		panel.add(label, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		slider.setValue(currFrame);
	}

	public void buttonPressed(String name) {
		if (name.equals("Play") && isAlreadyPlaying == false) { // Play
			state = 0;
			soundThread = null;
			soundThread = new Thread(new PlaySound());
			fps.start();
			soundThread.start();
			isAlreadyPlaying = true;
		} else if (name.equals("Play") && state == 1) { // Play after pause
			// TO DO : to continue playing from where it was stopped
			state = 0;
			soundThread = null;
			soundThread = new Thread(new PlaySound());
			fps.start();
			soundThread.start();
			isAlreadyPlaying = true;
		} else if (name.equals("Pause")) { // Pause
			state = 1;
			fps.stop();
			try {
				audioInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			soundThread.interrupt();
			dataLine.stop();
			dataLine.flush();
			dataLine.close();
		} else if (name.equals("Stop")) { // Stop
			state = 2;
			currFrame = 0;
			BufferedImage f = refreshFrame(currFrame);
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
				if (currFrame >= byteIndicies.length-1) {
					currFrame = 0;
					soundThread.interrupt();
					dataLine.stop();
					dataLine.flush();
					dataLine.close();
					soundThread = null;
					fps.stop();
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
		MyButton(String label) {
			setFont(new Font("Helvetica", Font.BOLD, 10));
			setText(label);
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					buttonPressed(getText());
				}
			});
		}

		MyButton(String label, ImageIcon icon) {
			Image img = icon.getImage();
			Image scaleimg = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
			setIcon(new ImageIcon(scaleimg));
			setName(label);
			addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					buttonPressed(getName());
				}
			});
		}
	}

	public class PlaySound implements Runnable {
		public void run() {
			InputStream sis = null;
			try {
				sis = new FileInputStream(soundFile);
			} catch (FileNotFoundException e) {
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
				int val = currFrame * 4410;
				audioInputStream.skip((long) val);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				while (readBytes != -1) {

					readBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);

					if (readBytes >= 0) {
						dataLine.write(audioBuffer, 0, readBytes);
					}
				}
			} catch (IOException e1) {
				new PlayWaveException(e1);
			}

		}
	}
}