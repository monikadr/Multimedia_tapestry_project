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
	    this.newWidth = 235;
	    this.newHeight = 190;
	    //this.run();
	    System.out.println("Running seam carving");
	    this.runSeamCarving();
	    System.out.println("Stitching images together");
	    this.finalOutputImage();
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

	public void finalOutputImage() throws IOException {
	    int count = 0;
	    int offset = 15*(this.sceneIndex.size()-1);
	    int width = this.newWidth*this.sceneIndex.size() - offset;
	    tapestry = new BufferedImage(width, this.newHeight, BufferedImage.TYPE_INT_RGB);
    	Graphics g = tapestry.getGraphics();
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

	    					// System.out.println("val1: " + val1 + " val2: " + val2);
	    					// System.out.println("r1: " + r1 + " g1: " + g1 + " b1: " + b1 + " r2: " + r2 + " g2: " + g2 + " b2: " + b2);
	    					// System.out.println("avgR: " + avgR + " avgG: " + avgG + " avgB: " + avgB);

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


	public void run() {
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