
import java.io.*;

public class Main {
  public static void main(String[] args) throws IOException {
  	//ImageCreation image = new ImageCreation("resources/USCWeek.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Hussein_Day1_007.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Michael_Day2_018.rgb",20000);
  	//ImageCreation image = new ImageCreation("resources/Apple.rgb",45000);
  	//ImageCreation image = new ImageCreation("resources/Disney.rgb",17500);
  	int threshold = 20000;
	String name = "resources/USCWeek.rgb";
	File file = new File(name);

	//method can be background or people depending on the kind of person. People will run foreground analysis
  	String method = "people";
	int width = 352;
	int height = 288;
	int nBytes = width*height*3;
	double len = file.length();
	double nFrames = len/(width*height*3);
	int[] byteIndicies = new int[(int) nFrames+1];
	
	for (int b = 0; b < nFrames; ++b) {
		byteIndicies[b] = b * nBytes;
	}

	new AVPlayer("resources/USCWeek.rgb","resources/USCWeek.wav",byteIndicies,nFrames,threshold,method);
  }
}