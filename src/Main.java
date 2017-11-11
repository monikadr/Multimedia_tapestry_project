// import org.opencv.core.Core;
// import org.opencv.core.Mat;
// import org.opencv.core.MatOfRect;
// import org.opencv.core.Point;
// import org.opencv.core.Rect;
// import org.opencv.core.Scalar;
// import org.opencv.imgcodecs.Imgcodecs;
// import org.opencv.objdetect.CascadeClassifier;
// import org.opencv.imgproc.Imgproc;

// class DetectFaceDemo {
//   public void run() {
//     System.out.println("\nRunning DetectFaceDemo");


//     CascadeClassifier faceDetector = new CascadeClassifier("resources/haarcascade_profileface.xml");
//     Mat image = Imgcodecs.imread("resources/lowres.rgb",4);

//     MatOfRect faceDetections = new MatOfRect();
//     faceDetector.detectMultiScale(image, faceDetections);

//     System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

//     for (Rect rect : faceDetections.toArray()) {
//         Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
//     }

//     String filename = "faceDetection.png";
//     System.out.println(String.format("Writing %s", filename));
//     Imgcodecs.imwrite(filename, image);
//   }
// }

public class Main {
  public static void main(String[] args) throws InterruptedException {
  	ImageTapestry i = new ImageTapestry("resources/Apple.rgb");
    // System.out.println("Hello, OpenCV");

    // // Load the native library.
    // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    // new DetectFaceDemo().run();
  }
}