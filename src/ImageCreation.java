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
import java.awt.Graphics;


public class ImageCreation {
	KeyFrameIdentification keyFrames;
	private ArrayList<Integer> sceneIndex;
	private static int threshold;
	private boolean[] face;
	private static int originalWidth;
	private static int originalHeight;
	private static int newWidth;
	private static int newHeight;
	BufferedImage tapestry;
	
	public ImageCreation(String fileName, int threshold) throws IOException {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    keyFrames = new KeyFrameIdentification(fileName,threshold);
	    sceneIndex = new ArrayList<Integer>();
	    sceneIndex = keyFrames.getSceneIndex();
	    this.threshold = threshold;
	    face = new boolean[sceneIndex.size()];
	    for (int i = 0; i < face.length; i++) {
	    	face[i] = false;
	    }
	    keyFrames.printIndexes();
	    this.originalWidth = 352;
	    this.originalHeight = 288;
	    this.newWidth = 200;
	    this.newHeight = 155;
	    //this.imageTapestryViaSeamCarving();
	    //this.runFaceDetection();
	    System.out.println("Running seam carving...");
	    this.runSeamCarving();
	    System.out.println("Stitching images together...");
	    this.finalOutputImageAfterSeam();
	}

	public void runSeamCarving() throws IOException {
	    int count = 0;
	    while (count < this.sceneIndex.size()) {
	    	String fileName = "keyframes/" + threshold + "_" + count + ".png";
	    	String outputFileName = "out" + count + ".png";
	    	SeamCarver seamCarver = new SeamCarver(fileName,outputFileName,this.originalWidth - this.newWidth, this.originalHeight - this.newHeight);
	    	count++;
	    }
	}

	public void finalOutputImageAfterSeam() throws IOException {
		int count = 0;
		int tapX = 0;
		int tapY = 0;
		int width = 0;
		int maxSize = this.sceneIndex.size();
		System.out.println("maxSize "+maxSize);
		if ((maxSize-1)%2 == 0)
			width = ((maxSize-1)/2+1)*this.newWidth;
		else
			width = ((maxSize-1)/2+1)*this.newWidth + this.newWidth/4;

		System.out.println("size "+ width +" x "+(this.newHeight*2-30));
	    tapestry = new BufferedImage(width, this.newHeight*2-30, BufferedImage.TYPE_INT_RGB);

		while (count < maxSize) {
			System.out.println("on count number: " + count);
			String fileName = "out" + count + ".png";
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

	    ImageIO.write(tapestry,"png",new File("tapestry-seam.png"));
	    //SeamCarver seamCarver = new SeamCarver("tapestry-seam.png","tapestry-seam-post.png",50,50);
	}


	public void imageTapestryViaSeamCarving() throws IOException {
		int count = 0;
		int tapX = 0;
		int tapY = 0;
		int width = 0;
		int maxSize = this.sceneIndex.size();

		if (maxSize%2 == 0) {
			width = ((maxSize/2)+1)*this.originalWidth;
		}
		else {
			width = ((maxSize+1)*this.originalWidth)/2;
		}

	    tapestry = new BufferedImage(width, this.originalHeight*2, BufferedImage.TYPE_INT_RGB);

		while (count < maxSize) {
			System.out.println("on keyframe number: " + count);
			String fileName = "keyframes/" + threshold + "_" + count + ".png";
			BufferedImage img = ImageIO.read(new File(fileName));
			if (count%2 == 0) {
				tapY = 0;
				if (count == 0)
					tapX = 0;
				else 
					tapX = (count/2)*this.originalWidth;
				System.out.println("tapx: " + tapX + " tapy: " + tapY);
	    		for (int y = 0; y < this.originalHeight; y++) {
	    			for (int x = 0; x < this.originalWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.originalWidth;
	    			tapY++;
	    		}

			}
			else {
				tapY = this.originalHeight;
				if (count == 1) {
					tapX = this.originalWidth/2;
				}
				else {
					tapX = (count/2)*this.originalWidth + (this.originalWidth/2);
				}
				System.out.println("tapx: " + tapX + " tapy: " + tapY);
	    		for (int y = 0; y < this.originalHeight; y++) {
	    			for (int x = 0; x < this.originalWidth; x++) {
	    				//System.out.println("x " + x + " y " + y + " tapx " + tapX + " tapY " + tapY);
	    				tapestry.setRGB(tapX,tapY,img.getRGB(x,y));
	    				tapX++;
	    			}
	    			tapX = (count/2)*this.originalWidth + (this.originalWidth/2);
	    			tapY++;
	    		}
			}
			count++;
		}

	    ImageIO.write(tapestry,"png",new File("tapestry.png"));
	    System.out.println("Running seam carving..");
	    //trial1
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",width/2,this.originalHeight/2);
	    //trial2
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(width/2),(int)this.originalHeight*5/3);
	    //trial4 - accurate
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(width/2),this.originalHeight);
	    //trial5 - accurate - columns done first then rows
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(int)(width*29/50),this.originalHeight*6/5);
	    //trial6 - accurate - rows done first then columns
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(int)(width*29/50),this.originalHeight*6/5);
	    //trial7 - accurate - rows first then columns
	    //SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",(int)(width*17/24),this.originalHeight*7/5);
	    //trial8 - accurate - rows first then col
	    int trial = (int)((int)(width*29/50)+(int)(width*17/24))/2;
	    SeamCarver seamCarver = new SeamCarver("tapestry.png","tapestry-seam.png",trial,this.originalHeight*6/5);

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
		    	String outputFileName = "out" + count + ".png";
		    	BufferedImage bi = ImageIO.read(new File(outputFileName));
	    		for (int y = 0; y < this.newHeight; y++) {
	    			for (int x = 0; x < this.newWidth; x++) {
	    				tapestry.setRGB(x,y,bi.getRGB(x,y));
	    			}
	    		}	    		    		
	    	}
	    	else {
		    	String outputFileName = "out" + count + ".png";
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
	}


	public void runFaceDetection() {
	   System.out.println("\nRunning DetectFaceDemo");

	    CascadeClassifier haarUpperBody = new CascadeClassifier("resources/haarcascades/haarcascade_upperbody.xml");
	    CascadeClassifier haarLowerBody = new CascadeClassifier("resources/haarcascades/haarcascade_lowerbody.xml");
	    CascadeClassifier haarFullBody = new CascadeClassifier("resources/haarcascades/haarcascade_fullbody.xml");
	    CascadeClassifier haarProfileFace = new CascadeClassifier("resources/haarcascades/haarcascade_profileface.xml");
	    CascadeClassifier haarFrontalFaceAlt1 = new CascadeClassifier("resources/haarcascades/haarcascade_frontalface_alt_tree.xml");
	    CascadeClassifier haarFrontalFaceAlt2 = new CascadeClassifier("resources/haarcascades/haarcascade_frontalface_alt.xml");
	    CascadeClassifier haarFrontalFaceAlt3 = new CascadeClassifier("resources/haarcascades/haarcascade_frontalface_alt2.xml");

	    // CascadeClassifier lbpFrontalFace = new CascadeClassifier("resources/lbpcascades/lbpcascade_frontalface.xml");
	    // CascadeClassifier lbpProfileFace = new CascadeClassifier("resources/lbpcascades/lbpcascade_profileface.xml");
	    int count = 0;

	    while (count < this.sceneIndex.size()) {
	    	String fileName = "keyframes/" + threshold + "_" + count + ".png";
	    	Mat image = Imgcodecs.imread(fileName);
	    	String outputFileName = "out" + count + ".png";
	    	System.out.println(count);

		    MatOfRect fullBodyHaar = new MatOfRect();
		    haarFullBody.detectMultiScale(image, fullBodyHaar);


		    if (fullBodyHaar.toArray().length > 0) {
		    	System.out.println(String.format("Detected %s fullBodyHaar", fullBodyHaar.toArray().length));
				for (Rect rect : fullBodyHaar.toArray()) {
				    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
				}
				face[count] = true;
				System.out.println(String.format("Writing %s", outputFileName));
				Imgcodecs.imwrite(outputFileName, image);
		    }

		    else {
			    MatOfRect upperBodyHaar = new MatOfRect();
			    haarUpperBody.detectMultiScale(image, upperBodyHaar);
			    if (upperBodyHaar.toArray().length > 0) {
					System.out.println(String.format("Detected %s upper body haar", upperBodyHaar.toArray().length));
					for (Rect rect : upperBodyHaar.toArray()) {
					    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
					}	
					face[count] = true;
					System.out.println(String.format("Writing %s", outputFileName));
					Imgcodecs.imwrite(outputFileName, image);					    	
			    }

			    else {
				    MatOfRect lowerBodyHaar = new MatOfRect();
				    haarLowerBody.detectMultiScale(image, lowerBodyHaar);
				    if (lowerBodyHaar.toArray().length > 0) {
		    			System.out.println(String.format("Detected %s lower body haar", lowerBodyHaar.toArray().length));
		    			for (Rect rect : lowerBodyHaar.toArray()) {
						    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
						}
						face[count] = true;
						System.out.println(String.format("Writing %s", outputFileName));
						Imgcodecs.imwrite(outputFileName, image);	
			    	}

			    	else {

		    			MatOfRect frontalFaceHaar1 = new MatOfRect();
		    			haarFrontalFaceAlt1.detectMultiScale(image, frontalFaceHaar1);

		    			if (frontalFaceHaar1.toArray().length > 0) {
		    				System.out.println(String.format("Detected %s frontalFaceHaar1", frontalFaceHaar1.toArray().length));
			    			for (Rect rect : frontalFaceHaar1.toArray()) {
							    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
							}
							face[count] = true;
							System.out.println(String.format("Writing %s", outputFileName));
							Imgcodecs.imwrite(outputFileName, image);			
		    			}

		    			else {
			    			MatOfRect frontalFaceHaar2 = new MatOfRect();
			    			haarFrontalFaceAlt2.detectMultiScale(image, frontalFaceHaar2);

			    			if (frontalFaceHaar2.toArray().length > 0) {
			    				System.out.println(String.format("Detected %s frontalFaceHaar2", frontalFaceHaar2.toArray().length));
							    for (Rect rect : frontalFaceHaar2.toArray()) {
								    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
								}
								face[count] = true;
								System.out.println(String.format("Writing %s", outputFileName));
								Imgcodecs.imwrite(outputFileName, image);	
			    			}

			    			else {

					    		MatOfRect frontalFaceHaar3 = new MatOfRect();
				    			haarFrontalFaceAlt3.detectMultiScale(image, frontalFaceHaar3);

				    			if (frontalFaceHaar3.toArray().length > 0) {
				    				System.out.println(String.format("Detected %s frontalFaceHaar3", frontalFaceHaar3.toArray().length));
					    			for (Rect rect : frontalFaceHaar3.toArray()) {
									    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
									}
									face[count] = true;
									System.out.println(String.format("Writing %s", outputFileName));
									Imgcodecs.imwrite(outputFileName, image);			
				    			}

				    			else {
									MatOfRect profileFaceHaar = new MatOfRect();
								    haarProfileFace.detectMultiScale(image, profileFaceHaar);

								    if (profileFaceHaar.toArray().length > 0) {
								    	System.out.println(String.format("Detected %s profileFaceHaar", profileFaceHaar.toArray().length));
						    			for (Rect rect : profileFaceHaar.toArray()) {
										    Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
										}
										face[count] = true;
										System.out.println(String.format("Writing %s", outputFileName));
										Imgcodecs.imwrite(outputFileName, image);
								    }
				    			}
			    			}
		    			}
			    	}		    	
			    }
		    }
	    	count++;
	    }


	    // MatOfRect frontalFaceLbp = new MatOfRect();
	    // lbpFrontalFace.detectMultiScale(image, frontalFaceLbp);
	    // System.out.println(String.format("Detected %s frontalFaceLbp", frontalFaceLbp.toArray().length));

	    // MatOfRect profileFaceLbp = new MatOfRect();
	    // lbpProfileFace.detectMultiScale(image, profileFaceLbp);
	    // System.out.println(String.format("Detected %s profileFaceLbp", profileFaceLbp.toArray().length));

	}
}