public class Main {

	public static void main (String[] args) throws InterruptedException {
		ImageTapestry im = new ImageTapestry(args[0]);

		int nFrames=6000;
		int nBytes = 304128;
		
		int[] byteIndicies = new int[nFrames];
		for (int b = 0; b < nFrames; ++b) {
			byteIndicies[b] = b * nBytes;
		}
		String videoFileName = args[0];
		String audioFileName = args[1];
		new AVPlayer(videoFileName,audioFileName,byteIndicies);
		
	
	}
}