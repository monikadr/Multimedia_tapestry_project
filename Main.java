public class Main {

	public static void main (String[] args) throws InterruptedException {
		
		int[] byteIndicies = new int[720];
		for (int b = 0; b < 720; ++b) {
			byteIndicies[b] = b * 304128;
		}
		String videoFileName = args[0];
		String audioFileName = args[1];
		new AVPlayer(videoFileName,audioFileName,byteIndicies);
		
//		ImageTapestry im = new ImageTapestry(args[0]);
	
	}
}