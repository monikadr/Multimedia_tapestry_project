import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.util.*;

public class ImageTapestry {
	
	private ArrayList<Integer> sceneIndex;
	private static int originalHeight;
	private static int originalWidth;
	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;

	public ImageTapestry(String fileName) throws InterruptedException{
		sceneIndex = new ArrayList<Integer>();
		originalWidth = 352;
		originalHeight = 288;

		this.sceneIdentification(fileName);
	}

	public void sceneIdentification(String fileName) throws InterruptedException {
		img = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);

		try {
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);

			long totalFileSize = file.length();
			long totalFrames = totalFileSize/(3*originalWidth*originalHeight);
			long len = originalWidth*originalHeight*3;
			int offset = 0;
			int count = 0;
			byte[] bytes = new byte[(int)totalFileSize];
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			frame = new JFrame();
			GridBagLayout gLayout = new GridBagLayout();
			frame.getContentPane().setLayout(gLayout);

			JLabel lbText1 = new JLabel("Video: ");
			lbText1.setHorizontalAlignment(SwingConstants.LEFT);
			JLabel lbText2 = new JLabel("Audio: ");
			lbText2.setHorizontalAlignment(SwingConstants.LEFT);		

			while (count < totalFrames-1) {
				int ind1 = count*originalHeight*originalWidth*3;
				int ind2 = (count+1)*originalHeight*originalWidth*3;
				int sad = 0;
				int numOfBlack = 0;

				for(int y = 0; y < originalHeight; y++){

					for(int x = 0; x < originalWidth; x++){
						
						int r1 = bytes[ind1] & 0xff;
						int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
						int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 

				        //int grayLevel1 = (r1 + g1 + b1) / 3;
				        //int gray1 = (grayLevel1 << 16) + (grayLevel1 << 8) + grayLevel1;
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
						if (r1 == 0 && g1 == 0 && b1 == 0) {
							numOfBlack++;
						}

						int r2 = bytes[ind2] & 0xff;
						int g2 = bytes[ind2+originalHeight*originalWidth] & 0xff;
						int b2 = bytes[ind2+originalHeight*originalWidth*2] & 0xff; 
						int pix2 = 0xff000000 | ((r2 & 0xff) << 16) | ((g2 & 0xff) << 8) | (b2 & 0xff);

				        //int grayLevel2 = (r2 + g2 + b2) / 3;
				        //int gray2 = (grayLevel2 << 16) + (grayLevel2 << 8) + grayLevel2;
				        sad += pix1 - pix2;
				        //img.setRGB(x, y, pix1);
						ind1++;
						ind2++;
					}
				}

				//To decide threshold use the following values: 2139999999, 2119999999, 2123899999 disney
				//if there are scenes with black scene cuts use || numOfBlack > 32000

				if (Math.abs(sad) > 2123899999) {
					this.sceneIndex.add(count*originalWidth*originalHeight*3);
					//this.displayFrame(count, bytes);					
				}

				count++;
			}
			this.printIndexes();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void printIndexes() throws InterruptedException{
		System.out.println("final number of scenes " + this.sceneIndex.size());
		System.out.println(this.sceneIndex);
	}

	public void displayFrame(int count, byte[] bytes) {
		ind1 = count*originalHeight*originalWidth*3;

		for(int y = 0; y < originalHeight; y++){

			for(int x = 0; x < originalWidth; x++){
				
				int r1 = bytes[ind1] & 0xff;
				int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
				int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 

		        int grayLevel1 = (r1 + g1 + b1) / 3;
		        int gray1 = (grayLevel1 << 16) + (grayLevel1 << 8) + grayLevel1;
				int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
				
		        img.setRGB(x, y, pix1);
				ind1++;
			}
		}

		JPanel  panel = new JPanel ();
	    panel.add (new JLabel (new ImageIcon (img)));
	    
	    JFrame frame = new JFrame("Display images");
	    
	    frame.getContentPane().add (panel);
	    frame.pack();
	    frame.setVisible(true);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}

	public void showScenes(bytep[ bytes]) throws InterruptedException {
		int count = 0;
		int count2 = 0;
		int ind1 = 0, ind2 = 0;
		while (count < this.sceneIndex.size()-1) {
			if (count == 0) {
				ind1 = 0;
				ind2 = this.sceneIndex.get(count);
			}
			else {
				ind1 = this.sceneIndex.get(count);
				ind2 = this.sceneIndex.get(count+1);
			}

			while (ind1 < ind2) {
				ind1 = count2*originalHeight*originalWidth*3;
				count2++;
				for(int y = 0; y < originalHeight; y++){

					for(int x = 0; x < originalWidth; x++){
						
						int r1 = bytes[ind1] & 0xff;
						int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
						int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
						
				        img.setRGB(x, y, pix1);
						ind1++;
					}
				}
				JPanel  panel = new JPanel ();

			    panel.add (new JLabel (new ImageIcon (img)));
			    
			    JFrame frame = new JFrame("Display images");
			    
			    frame.getContentPane().add (panel);
			    frame.pack();
			    frame.setVisible(true);
			    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);				
			}
		
			count++;
			TimeUnit.SECONDS.sleep(5);
		}
	}
}