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

				for(int y = 0; y < originalHeight; y++){

					for(int x = 0; x < originalWidth; x++){
						
						int r1 = bytes[ind1] & 0xff;
						int g1 = bytes[ind1+originalHeight*originalWidth] & 0xff;
						int b1 = bytes[ind1+originalHeight*originalWidth*2] & 0xff; 

				        int grayLevel1 = (r1 + g1 + b1) / 3;
				        int gray1 = (grayLevel1 << 16) + (grayLevel1 << 8) + grayLevel1;
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
						
						int r2 = bytes[ind2] & 0xff;
						int g2 = bytes[ind2+originalHeight*originalWidth] & 0xff;
						int b2 = bytes[ind2+originalHeight*originalWidth*2] & 0xff; 
						int pix2 = 0xff000000 | ((r2 & 0xff) << 16) | ((g2 & 0xff) << 8) | (b2 & 0xff);

				        int grayLevel2 = (r2 + g2 + b2) / 3;
				        int gray2 = (grayLevel2 << 16) + (grayLevel2 << 8) + grayLevel2;
				        sad += pix1 - pix2;
				        //img.setRGB(x, y, pix1);
						ind1++;
						ind2++;
					}
				}
				if (Math.abs(sad) > 2139999999 || count == 0) {
					//System.out.println(count);
					this.sceneIndex.add(count*originalWidth*originalHeight*3);
					//System.out.println(sad);
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
				// Use labels to display the images



				// lbIm1 = new JLabel(new ImageIcon(img));

				// GridBagConstraints c = new GridBagConstraints();
				// c.fill = GridBagConstraints.HORIZONTAL;
				// c.anchor = GridBagConstraints.CENTER;
				// c.weightx = 0.5;
				// c.gridx = 0;
				// c.gridy = 0;
				// frame.getContentPane().add(lbText1, c);

				// c.fill = GridBagConstraints.HORIZONTAL;
				// c.anchor = GridBagConstraints.CENTER;
				// c.weightx = 0.5;
				// c.gridx = 0;
				// c.gridy = 1;
				// frame.getContentPane().add(lbText2, c);

				// c.fill = GridBagConstraints.HORIZONTAL;
				// c.gridx = 0;
				// c.gridy = 2;
				// frame.getContentPane().add(lbIm1, c);

				// frame.pack();
				// frame.setVisible(true);
				count++;
				//TimeUnit.MILLISECONDS.sleep(50);
				//System.out.println();
			}
			this.printIndexes();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void printIndexes() {
		System.out.println("final number of scenes " + this.sceneIndex.size());
		System.out.println(this.sceneIndex);
	}
}