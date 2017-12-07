import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * This class describes and contains the entry point to an application that
 * demonstrates the blending transition.
 */

public class Blender1 extends JFrame
{
  /**
  * Construct Blender1 GUI.
  */

  public Blender1 ()
  {
   super ("Blender #1");
   setDefaultCloseOperation (EXIT_ON_CLOSE);

   // Load first image from JAR file and draw image into a buffered image.

   ImageIcon ii1 = new ImageIcon (getClass ().getResource ("./try/out0.png"));
   final BufferedImage bi1;
   bi1 = new BufferedImage (ii1.getIconWidth (), ii1.getIconHeight (),
                BufferedImage.TYPE_INT_RGB);
   Graphics2D g2d = bi1.createGraphics ();
   g2d.drawImage (ii1.getImage (), 0, 0, null);
   g2d.dispose ();

   // Load second image from JAR file and draw image into a buffered image.

   ImageIcon ii2 = new ImageIcon (getClass ().getResource ("./try/out1.png"));
   final BufferedImage bi2;
   bi2 = new BufferedImage (ii2.getIconWidth (), ii2.getIconHeight (),
                BufferedImage.TYPE_INT_RGB);
   g2d = bi2.createGraphics ();
   g2d.drawImage (ii2.getImage (), 0, 0, null);
   g2d.dispose ();

   // Create an image panel capable of displaying entire image. The widths
   // of both images and the heights of both images must be identical.

   final ImagePanel ip = new ImagePanel ();
   ip.setPreferredSize (new Dimension (ii1.getIconWidth (),
                     ii1.getIconHeight ()));
   getContentPane ().add (ip, BorderLayout.NORTH);

   // Create a slider for selecting the blending percentage: 100% means
   // show all of first image; 0% means show all of second image.

   final JSlider slider = new JSlider (JSlider.HORIZONTAL, 0, 100, 100);
   slider.setMinorTickSpacing (5);
   slider.setMajorTickSpacing (10);
   slider.setPaintTicks (true);
   slider.setPaintLabels (true);
   slider.setLabelTable (slider.createStandardLabels (10));
   slider.setInverted (true);
   ChangeListener cl;
   cl = new ChangeListener ()
      {
        public void stateChanged (ChangeEvent e)
        {
         // Each time the user adjusts the slider, obtain the new
         // blend percentage value and use it to blend the images.

         int value = slider.getValue ();
         ip.setImage (blend (bi1, bi2, value/100.0));
        }
      };
   slider.addChangeListener (cl);
   getContentPane ().add (slider, BorderLayout.SOUTH);

   // Display the first image, which corresponds to a 100% blend percentage.

   ip.setImage (bi1);

   pack ();
   setVisible (true);
  }

  /**
  * Blend the contents of two BufferedImages according to a specified
  * weight.
  *
  * @param bi1 first BufferedImage
  * @param bi2 second BufferedImage
  * @param weight the fractional percentage of the first image to keep
  *
  * @return new BufferedImage containing blended contents of BufferedImage
  * arguments
  */

  public BufferedImage blend (BufferedImage bi1, BufferedImage bi2,
          double weight)
{
if (bi1 == null)
throw new NullPointerException ("bi1 is null");

if (bi2 == null)
throw new NullPointerException ("bi2 is null");

int width = bi1.getWidth ();
if (width != bi2.getWidth ())
throw new IllegalArgumentException ("widths not equal");

int height = bi1.getHeight ();
if (height != bi2.getHeight ())
throw new IllegalArgumentException ("heights not equal");

BufferedImage bi3 = new BufferedImage (width, height,
                 BufferedImage.TYPE_INT_RGB);
Graphics2D g2d = bi3.createGraphics ();
g2d.drawImage (bi1, null, 0, 0);
g2d.setComposite (AlphaComposite.getInstance (AlphaComposite.SRC_OVER,
                     (float) (1.0-weight)));
g2d.drawImage (bi2, null, 0, 0);
g2d.dispose ();

return bi3;
}
  /**
  * Application entry point.
  *
  * @param args array of command-line arguments
  */

  public static void main (String [] args)
  {
   Runnable r = new Runnable ()
            {
             public void run ()
             {
               // Create Blender1's GUI on the event-dispatching
               // thread.

               new Blender1 ();
             }
            };
   EventQueue.invokeLater (r);
  }
}

/**
 * This class describes a panel that displays a BufferedImage's contents.
 */

class ImagePanel extends JPanel
{
  private BufferedImage bi;

  /**
  * Specify and paint a new BufferedImage.
  *
  * @param bi BufferedImage whose contents are to be painted
  */

  void setImage (BufferedImage bi)
  {
   this.bi = bi;
   repaint ();
  }

  /**
  * Paint the image panel.
  *
  * @param g graphics context used to paint the contents of the current
  * BufferedImage
  */

  public void paintComponent (Graphics g)
  {
   if (bi != null)
   {
     Graphics2D g2d = (Graphics2D) g;
     g2d.drawImage (bi, null, 0, 0);
   }
  }
}
