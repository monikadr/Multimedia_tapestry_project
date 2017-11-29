import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;


public class AVPlayer implements MouseListener, MouseMotionListener {

	// variables to read rgb file
	int ind = 0;
	int offset = 0;
	int numRead = 0;
	// variables to create UI
	JFrame frame;
	JPanel panel, sliderPanel,buttonPanel,tapestry;
	public static JSlider slider;
	int width = 352;
	int height = 288;
	BufferedImage img;
	// variable that holds the start of each frame index in video
	public int[] byteIndicies;
	// number of frames per second
	public int fps = 20;
	// total number of frames in the video file
	double Frames;
	public static File soundFile, file;
	public static InputStream is, sis;
	// length of audio and video file
	public static long len, slen;
	// if doing fast forward like clicking on time slider
	public static boolean fastForward = false;
	public static byte[] bytes, sbytes;
	// to keep track of video state = 0 -> playing, state=1 -> pause, state=2 -> stop
	public static int state;
	// to keep track of playing frame number 
	public static int startFrame, currFrame = 0;
	// threads for audio and video 
	public Thread soundThread,videoThread;
	// to keep track if audio is already playing
	public static boolean isAlreadyPlaying = false;
	// to keep track if audio 
	public boolean isPause = false;
	public boolean zoom = false;
	public String audioFileName;
	public String videoFileName;
	private ArrayList<Integer> sceneIndex,zoom1SceneIndex,zoom2SceneIndex;
	private BufferedImage indexImage,zoom1IndexImage,zoom2IndexImage;
	private String nameOfTapestry;
	public MyButton playButton, stopButton, pauseButton;
	PlaySound playSound = new PlaySound();
	int threshold_zoom1, threshold_zoom2;
	int zoom_count =0;
	
	public AVPlayer(String video, String audio, int[] byteIndicies,double f,int threshold,String method) throws IOException {
		ImageCreation imageCreation = new ImageCreation(video,threshold,method);
		this.sceneIndex = imageCreation.getSceneIndex();
		
		// change this threshold based on video
		this.threshold_zoom1 = threshold-5000;
		this.threshold_zoom2 = threshold-10000;
		
		//creating zoom images and getting scene index
		ImageCreation imageCreation1 = new ImageCreation(video,this.threshold_zoom1,method);
		this.zoom1SceneIndex = imageCreation1.getSceneIndex();
		
		ImageCreation imageCreation2 = new ImageCreation(video,this.threshold_zoom2,method);
		this.zoom2SceneIndex = imageCreation2.getSceneIndex();
		
		// initialization for variables
		this.byteIndicies = byteIndicies;
		this.Frames = f;
		this.audioFileName = audio;
		this.videoFileName = video;
		this.nameOfTapestry = imageCreation.getName();
		String t = "index_" + threshold + "_out_scaled.png";
		indexImage = ImageIO.read(new File(t));

		// creating zoom index images
		String t1 = "index_" + threshold_zoom1 + "_out_scaled.png";
		zoom1IndexImage = ImageIO.read(new File(t1));
		String t2 = "index_" + threshold_zoom2 + "_out_scaled.png";
		zoom2IndexImage = ImageIO.read(new File(t2));
		
		// setting slider, stop, pause, play buttons - UI build
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		
		//load video file
		try {
			file = new File(video);
			is = new FileInputStream(file);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		len = file.length();
		bytes = new byte[(int) len];
	
		// get start image of the video and display
		img = refreshFrame(0);
		showImage(frame.getContentPane());

		// adding slider after video screen
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
		slider.setMaximumSize(new Dimension(width, 50));
		startFrame = currFrame;

		sliderPanel.add(slider);
		slider.addMouseListener(new MouseListener() {

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
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				JSlider slider = (JSlider) e.getSource();
				fastForward = true;
				currFrame = (int) ((e.getX() * 1.85 * 9) + startFrame);
				slider.setValue(currFrame);
				isAlreadyPlaying = false;
			
				videoThread.interrupt();
				soundThread.interrupt();				
				playSound.stop();
				try {
					videoThread.sleep(10);
					soundThread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				soundThread = null;
				videoThread = null;
				playSound.jump(currFrame);
				
			
				playback();
				
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		
		});
		
		// adding button after slider
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		playButton = new MyButton("Play");
		buttonPanel.add(playButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
		pauseButton = new MyButton("Pause");
		buttonPanel.add(pauseButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacing
		stopButton = new MyButton("Stop");
		buttonPanel.add(stopButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 50)));
		sliderPanel.add(buttonPanel);
		pauseButton.setEnabled(false);
		stopButton.setEnabled(false);

		System.out.println("tapestry panel loading..");
		// need to add tapestry panel 
		ImageIcon tap = new ImageIcon(this.nameOfTapestry+ "_" + method +"_" + threshold + ".png");
		JLabel label = new JLabel("", tap, JLabel.CENTER);
		tapestry = new JPanel(new BorderLayout());
		tapestry.add( label, BorderLayout.CENTER );
		
		// to add scrolling
		tapestry.addMouseWheelListener(new MouseWheelListener(){

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				//System.out.println(e.getWheelRotation());
				//System.out.println(e.getX());
				//System.out.println(e.getY());
				
				ImageIcon tap = null;
				if(e.getWheelRotation()<0){
					//zoom in - increase size
					
					if(zoom_count<0)
					{
						zoom_count=0;
					}
					zoom_count++;
					zoom = true;
					 tapestry.removeAll();
					 if(zoom_count==1){
						 
						tap = new ImageIcon(nameOfTapestry+ "_" + method +"_" + threshold_zoom1 + ".png");
						
					 }
					 else if(zoom_count>=2){
						 tap = new ImageIcon(nameOfTapestry+ "_" + method +"_" +  threshold_zoom2 + ".png");
					 }
					
					 
						JLabel label = new JLabel("", tap, JLabel.CENTER);
		
						tapestry.add (label, BorderLayout.CENTER );
						
						tapestry.revalidate();
						tapestry.repaint();
						
				}
				else
				{
					// zoom out - decrease size
					zoom_count--;
					if(zoom_count>2){
						zoom_count=2;
					}
					 tapestry.removeAll();
					 zoom=false;
					 if(zoom_count>=2){
						tap = new ImageIcon(nameOfTapestry+ "_" + method +"_" +  threshold_zoom1 + ".png");
						
					 }
					 else
						 if(zoom_count<=1){
							 tap = new ImageIcon(nameOfTapestry+ "_" + method +"_" + threshold + ".png");
						 }
						JLabel label = new JLabel("", tap, JLabel.CENTER);
		
						tapestry.add (label, BorderLayout.CENTER );
						
						tapestry.revalidate();
						tapestry.repaint();
				}
				
			}
			
		});
		
		tapestry.addMouseListener(new MouseListener() {
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
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				int pix,frameNum,keyFrameIndex;
				System.out.println("x: " + e.getX() + " y: " + e.getY());
				fastForward = true;
				if(zoom){
					pix = zoom1IndexImage.getRGB(e.getX(),e.getY());
					frameNum = (pix >> 16) & 0x000000FF;
					keyFrameIndex = sceneIndex.get(frameNum);
				}
				else{
				pix = indexImage.getRGB(e.getX(),e.getY());
				frameNum = (pix >> 16) & 0x000000FF;
				keyFrameIndex = sceneIndex.get(frameNum);
				}
				currFrame = keyFrameIndex/(352*288*3);
				if (currFrame > 40) {
					currFrame -= 40;
				}
				isAlreadyPlaying = false;
			
				videoThread.interrupt();
				soundThread.interrupt();				
				playSound.pause();
				try {
					videoThread.sleep(10);
					soundThread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				soundThread = null;
				videoThread = null;
				playSound.jump(currFrame);
						
				playback();
				
				
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}			
		});

		sliderPanel.add(tapestry);

		//adding whole panel to frame
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

	// button class for displaying button and taking click on button
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
	}

	// playing video and audio based on the state or button press
	public void buttonPressed(String name) {
		if (name.equals("Play") && isAlreadyPlaying == false) { // Play		
			playButton.setEnabled(false);
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			isAlreadyPlaying = true;
			state = 0;
			playback();
		} else if (name.equals("Play") && state == 1) { // resume
			playButton.setEnabled(false);
			pauseButton.setEnabled(true);
			stopButton.setEnabled(true);
			isAlreadyPlaying = true;
			state = 0;
			isPause = false;
			playSound.resume();
			videoThread.interrupt();
			soundThread.interrupt();
		} else if (name.equals("Pause")) { // Pause
			playButton.setEnabled(true);
			pauseButton.setEnabled(false);
			stopButton.setEnabled(true);
			state = 1;
			isPause = true;
			videoThread.interrupt();
			soundThread.interrupt();
			playSound.pause();
		} else if (name.equals("Stop")) { // Stop
			playButton.setEnabled(true);
			pauseButton.setEnabled(false);
			stopButton.setEnabled(false);
			state = 2;
			currFrame = 0;
			isAlreadyPlaying = false;
			soundThread.interrupt();
			videoThread.interrupt();
			playSound.stop();
		} 
	}
	
	// start the video and audio when play button is clicked
	public void playback(){
		try {
			playSound.load(audioFileName,currFrame);
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
		}		
		fastForward = false;
		soundThread = null;
		soundThread = new Thread(new sound());
		soundThread.start();
		videoThread = null;
		videoThread = new Thread(new video());
		videoThread.start();
	}
	
	// get new frame of video based on frame number
	public BufferedImage refreshFrame(int frame_no) {
		
			ind = byteIndicies[frame_no];
		
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		try {
			
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					img.setRGB(x,y,pix);
					ind++;
				} 
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}

	// to show the start frame of the video
	public void showImage(Container pane) {
		// show the initial panel and start of video
		currFrame = 0;
		panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		panel.add(label, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		pane.add(panel, BorderLayout.CENTER);

	}

	// repaint the video after every frame
	public void videoOriginal(BufferedImage img) {
		// update image
		currFrame++;
		panel.removeAll();
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(new ImageIcon(img));
		label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		panel.add(label, BorderLayout.CENTER);
		panel.revalidate();
		panel.repaint();
		slider.setValue(currFrame);
	}
	
	// start playing sound thread
	public class sound implements Runnable {
		public void run() {
			try {
				playSound.play();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// start playing video thread
	public class video implements Runnable{
		public void run(){
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			double spf = playSound.getSampleRate()/fps;
			int j=0;
			while((j < Math.round(playSound.getPosition())/spf) && fastForward == false){
				img = refreshFrame(currFrame);
				videoOriginal(img);
				j++;
			}
			while(j > Math.round(playSound.getPosition()/spf) &&  fastForward == false) {
				// Do Nothing : this to slow down the video number of frames per sec
			}

			for(int i = j; i < Frames&&  fastForward == false; i++) {
				// Video ahead of audio, wait for audio to catch up
				
				while(i > Math.round(playSound.getPosition()/spf) &&  fastForward == false) {
					// Do Nothing
				}
				
				while(i < Math.round(playSound.getPosition()/spf)&&  fastForward == false) {
					img = refreshFrame(currFrame);
					videoOriginal(img);
					i++;
				}
				
				img = refreshFrame(currFrame);
				videoOriginal(img);
				System.gc();
			}
		}
	}
}