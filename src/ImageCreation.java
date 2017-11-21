import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.*;
import java.awt.Graphics2D;
import org.opencv.core.CvType;

public class ImageCreation {
	KeyFrameIdentification keyFrames;
	private ArrayList<Integer> sceneIndex;
	private static int threshold;
	private boolean[] face;
	private static int originalWidth;
	private static int originalHeight;
	private static int newWidth;
	private static int newHeight;
	private static String name;
	private static String method;
	BufferedImage tapestry;
	
	public ImageCreation(String fileName, int threshold, String method) throws IOException {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    keyFrames = new KeyFrameIdentification(fileName,threshold);
	    sceneIndex = new ArrayList<Integer>();
	    sceneIndex = keyFrames.getSceneIndex();
	    this.name = fileName.split("\\/")[1].split("\\.")[0];
	    System.out.println(this.name);
	    this.threshold = threshold;
	    this.method = method;
	    face = new boolean[sceneIndex.size()];
	    for (int i = 0; i < face.length; i++) {
	    	face[i] = false;
	    }
	    keyFrames.printIndexes();
	    this.originalWidth = 352;
	    this.originalHeight = 288;



	    if (method.equals("people")) {
	    	this.newWidth = 240;
	    	this.newHeight = 200;
	    	System.out.println("Running foreground analysis..");
		    this.foreground();
	    	System.out.println("Running seam carving on individual images..");
	    	this.runSeamCarving(1);
	    	System.out.println("Combining images..");
		    this.imageTapestryViaSeamCarving();
	    }

	    if (method.equals("background")) {
	    	this.newWidth = 200;
	    	this.newHeight = 166;
	    	System.out.println("Running seam carving on individual images..");
	    	this.runSeamCarving(0);
	    	System.out.println("Combining images..");
	    	this.imageTapestryViaSeamCarving();
	    }
	}

	public int getNewHeight() {
		return this.newHeight;
	}

	public int getNewWidth() {
		return this.newWidth;
	}

	public ArrayList<Integer> getSceneIndex() {
		return this.sceneIndex;
	}

	public void runSeamCarving(int flag) throws IOException {
	    int count = 0;
	    if (flag == 1) {
		    while (count < this.sceneIndex.size()) {
		    	String fileName = "foreground/fg" + count + ".png";
		    	String outputFileName = "seamcarve/out" + count + ".png";
		    	SeamCarver seamCarver = new SeamCarver(fileName,outputFileName,this.originalWidth - this.newWidth, this.originalHeight - this.newHeight);
		    	count++;
		    }   	
	    }
	    else {
		    while (count < this.sceneIndex.size()) {
		    	String fileName = "keyframes/" + threshold + "_" + count + ".png";
		    	String outputFileName = "seamcarve/out" + count + ".png";
		    	SeamCarver seamCarver = new SeamCarver(fileName,outputFileName,this.originalWidth - this.newWidth, this.originalHeight - this.newHeight);
		    	count++;
		    }	    	
	    }

	}

	public void finalOutputImageAfterSeam() throws IOException {
		int count = 0;
		int tapX = 0;
		int tapY = 0;
		int width = 0;
		int maxSize = this.sceneIndex.size();
		if ((maxSize-1)%2 == 0)
			width = ((maxSize-1)/2+1)*this.newWidth;
		else
			width = ((maxSize-1)/2+1)*this.newWidth + this.newWidth/4;

		System.out.println("size "+ width +" x "+(this.newHeight*2-30));
	    tapestry = new BufferedImage(width, this.newHeight*2-30, BufferedImage.TYPE_INT_RGB);

		while (count < maxSize) {
			System.out.println("on count number: " + count);
			String fileName = "seamcarve/out" + count + ".png";
			BufferedImage img = ImageIO.read(new File(fileName));
			if (count%2 == 0) {
				tapY = 0;
				if (count == 0)
					tapX = 0;
				else 
					tapX = (count/2)*this.newWidth;
				//System.out.println("tapX: " + tapX + " tapY: " + tapY);
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.newWidth;
	    			tapY++;
	    		}

			}
			else {
				tapY = this.newHeight - 30;
				if (count == 1) {
					tapX = this.newWidth/2;
				}
				else {
					tapX = (count/2)*this.newWidth + (this.newWidth/4);
				}
				//System.out.println("tapX: " + tapX + " tapY: " + tapY);
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.newWidth + (this.newWidth/4);
	    			tapY++;
	    		}
			}
			count++;
		}

		BufferedImage scaledImg = new BufferedImage(width/2,(this.newHeight*2-30)/2,BufferedImage.TYPE_INT_RGB);
		Graphics2D gImg = scaledImg.createGraphics();

		gImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.drawImage(tapestry, 0, 0, width/2,(this.newHeight*2-30)/2, null);
  		gImg.dispose();

	    ImageIO.write(scaledImg,"png",new File("tapestry-seam.png"));
	    //SeamCarver seamCarver = new SeamCarver("tapestry-seam.png","tapestry-seam-post.png",50,50);
	}


	public void imageTapestryViaSeamCarving() throws IOException {
		int count = 0;
		int tapX = 0;
		int tapY = 0;
		int width = 0;
		int maxSize = this.sceneIndex.size();

		if (maxSize%2 == 0) {
			width = ((maxSize/2)+1)*this.newWidth;
		}
		else {
			width = ((maxSize+1)*this.newWidth)/2;
		}

	    tapestry = new BufferedImage(width, this.newHeight*2, BufferedImage.TYPE_INT_RGB);

		while (count < maxSize) {
			System.out.println("on keyframe number: " + count);
			String fileName = "seamcarve/out" + count + ".png";
			BufferedImage img = ImageIO.read(new File(fileName));
			if (count%2 == 0) {
				tapY = 0;
				if (count == 0)
					tapX = 0;
				else 
					tapX = (count/2)*this.newWidth;
				//System.out.println("tapx: " + tapX + " tapy: " + tapY);
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.newWidth;
	    			tapY++;
	    		}

			}
			else {
				tapY = this.newHeight;
				if (count == 1) {
					tapX = this.newWidth/2;
				}
				else {
					tapX = (count/2)*this.newWidth + (this.newWidth/2);
				}
				//System.out.println("tapx: " + tapX + " tapy: " + tapY);
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.newWidth + (this.newWidth/2);
	    			tapY++;
	    		}
			}
			count++;
		}

	    ImageIO.write(tapestry,"png",new File("tapestry.png"));
	    System.out.println("Running seam carving..");
	    int trial = (int) width/4 + 30;
	    SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",trial,this.newHeight/4 + 10);
	    BufferedImage tapseam = ImageIO.read(new File("tapestry-seam.png"));
	    width = tapseam.getWidth();
	    int height = tapseam.getHeight();

		BufferedImage scaledImg = new BufferedImage(width/2,height/2,BufferedImage.TYPE_INT_RGB);
		Graphics2D gImg = scaledImg.createGraphics();

		gImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.drawImage(tapseam, 0, 0, width/2,height/2, null);
  		gImg.dispose();

  		String outputFileNamee = this.name + "_" + this.method + ".png";
	    ImageIO.write(scaledImg,"png",new File(outputFileNamee));
	}

	public void finalOutputImageSideBySide() throws IOException {
	    int count = 0;
	    int offset = 15*(this.sceneIndex.size()-1);
	    int width = this.newWidth*this.sceneIndex.size() - offset;
	    tapestry = new BufferedImage(width, this.newHeight, BufferedImage.TYPE_INT_RGB);
    	int tapX = 0;

	    while (count < this.sceneIndex.size()) {
	    	if (count == 0) {
	    		//System.out.println("count 0");
		    	String outputFileName = "seamcarve/out" + count + ".png";
		    	BufferedImage bi = ImageIO.read(new File(outputFileName));
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				tapestry.setRGB(x,y,bi.getRGB(x,y));
	    			}
	    		}	    		    		
	    	}
	    	else {
		    	String outputFileName = "seamcarve/out" + count + ".png";
	    		BufferedImage im = ImageIO.read(new File(outputFileName));
	    		int imX = 0;
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = tapX; x < tapX + this.newWidth; x++) {
	    				if (x >= tapX && x < tapX + 15) {
	    					int val1 = tapestry.getRGB(x,y);
	    					int r1 = (val1 >> 16) & 0x000000FF;
	    					int g1 = (val1 >> 8 ) & 0x000000FF;
	    					int b1 = val1 & 0x000000FF;

	    					int val2 = im.getRGB(imX,y);
	    					int r2 = (val2 >> 16 ) & 0x000000FF;
	    					int g2 = (val2 >> 8 ) & 0x000000FF;
	    					int b2 = val2 & 0x000000FF;

	    					int avgR = (r1+r2)/2;
	    					int avgG = (g1+g2)/2;
	    					int avgB = (b1+b2)/2;
	    					int pix = 0xff000000 | ((avgR & 0xff) << 16) | ((avgG & 0xff) << 8) | (avgB & 0xff);
	    					tapestry.setRGB(x,y,pix);
	    				}
	    				else {
	    					tapestry.setRGB(x,y,im.getRGB(imX,y));
	    				}
	    				imX++;
	    			}
	    			imX = 0;
	    		}
	    	}

	        tapX += this.newWidth - 15;
	    	count++;
	    }
	    ImageIO.write(tapestry,"png",new File("tapestry.png"));
	    System.out.println("Running seam carving..");
	    int trial = (int) width/4 + 30;
	    SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",trial,this.newHeight/4 + 10);
	    BufferedImage tapseam = ImageIO.read(new File("tapestry-seam.png"));
	    width = tapseam.getWidth();
	    int height = tapseam.getHeight();

		BufferedImage scaledImg = new BufferedImage(width/2,height/2,BufferedImage.TYPE_INT_RGB);
		Graphics2D gImg = scaledImg.createGraphics();

		gImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.drawImage(tapseam, 0, 0, width/2,height/2, null);
  		gImg.dispose();

	    ImageIO.write(scaledImg,"png",new File("tapestry-seam-scaled.png"));
	}

	public void foreground() {
		System.out.println("running foreground");
		int count = 0;
		while (count < this.sceneIndex.size()) {
			Mat im = Imgcodecs.imread("keyframes/" + threshold + "_" + count + ".png");
	        Mat mask = new Mat();
	        Mat bgModel = new Mat();
	        Mat fgModel = new Mat();
	        Rect rect = new Rect(10, 10,250,290);
	        Mat source = new Mat(1, 1, CvType.CV_8U, new Scalar(3));		
	        Imgproc.grabCut(im, mask, rect, bgModel, fgModel, 1, 0);
	        Core.compare(mask, source, mask, Core.CMP_EQ);
	        Mat fg = new Mat(im.size(), CvType.CV_8UC1, new Scalar(0, 0, 0));
	        im.copyTo(fg, mask);
	        String outputFileName = "foreground/fg" + count + ".png";
			Imgcodecs.imwrite(outputFileName, fg);	
			count++;		
		}

	}
}