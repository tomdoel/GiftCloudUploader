package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.apple.eawt.Application;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudUploaderRestServerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class GiftCloudUploaderApp {

    protected static String resourceBundleName  = "uk.ac.ucl.cs.cmic.giftcloud.GiftCloudUploader";

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		try {

            // Set the dock icon - we need to do this before the main class is created
            URL iconURL = GiftCloudUploaderApp.class.getResource("/uk/ac/ucl/cs/cmic/giftcloud/GiftSurgMiniIcon.png");

            if (iconURL == null) {
                System.out.println("Warning: could not find the icon resource");
            } else {
                if (isOSX()) {
                    try {
                        Image iconImage = ImageIO.read(iconURL);
                        if (iconImage == null) {
                            System.out.println("Could not find icon");
                        } else {
                            Application.getApplication().setDockIconImage(new ImageIcon(iconImage).getImage());
                        }
                    } catch (Exception e) {
                        System.out.println("Warning: could not configure the dock menu");
                        e.printStackTrace(System.err);
                    }
                }
            }

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.UIElement", "true");

            final ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleName);
            final String applicationTitle = resourceBundle.getString("applicationTitle");

            // This is used to set the application title on OSX, but may not work when run from the debugger
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationTitle);

            new GiftCloudUploaderMain(new GiftCloudUploaderRestServerFactory(), resourceBundle);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

    public static boolean isOSX() {
        return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }
}
