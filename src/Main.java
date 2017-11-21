
import java.io.*;

public class Main {
  public static void main(String[] args) throws IOException {
  	//ImageCreation image = new ImageCreation("resources/USCWeek.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Hussein_Day1_007.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Michael_Day2_018.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Apple.rgb",45000);
  	//ImageCreation image = new ImageCreation("resources/Disney.rgb",17500);
  	int threshold = 45000;
	String name = "resources/Apple.rgb";
	File file = new File(name);
  	String method = "background";
	int width = 352;
	int height = 288;
	int nBytes = width*height*3;
	double len = file.length();
	double nFrames = len/(width*height*3);
	int[] byteIndicies = new int[(int) nFrames+1];
	
	for (int b = 0; b < nFrames; ++b) {
		byteIndicies[b] = b * nBytes;
	}

	ImageCreation image = new ImageCreation(name,threshold,"background");
	image = new ImageCreation(name,threshold,"people");


	// name = "resources/Disney.rgb";
	// threshold = 17500;
	// image = new ImageCreation(name,threshold,"background");
	// image = new ImageCreation(name,threshold,"people");	

	// name = "resources/USCWeek.rgb";
	// threshold = 20000;
	// image = new ImageCreation(name,threshold,"background");
	// image = new ImageCreation(name,threshold,"people");	

	// name = "resources/Hussein_Day1_007.rgb";
	// threshold = 20000;
	// image = new ImageCreation(name,threshold,"background");
	// image = new ImageCreation(name,threshold,"people");	

	// name = "resources/Michael_Day2_018.rgb";
	// threshold = 20000;
	// image = new ImageCreation(name,threshold,"background");
	// image = new ImageCreation(name,threshold,"people");	
	//System.out.println("AVPlayer loading...");
	//new AVPlayer("resources/Disney.rgb","resources/Disney.wav",byteIndicies,nFrames,threshold,method);
  }
}