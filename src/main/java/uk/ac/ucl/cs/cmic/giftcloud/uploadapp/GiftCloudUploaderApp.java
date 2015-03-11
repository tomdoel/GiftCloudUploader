package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.apple.eawt.Application;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class GiftCloudUploaderApp {

    protected static String resourceBundleName  = "com.pixelmed.display.GiftCloudUploader";

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		try {

            // Set the dock icon - we need to do this before the main class is created
            Image iconImage = ImageIO.read(GiftCloudUploaderApp.class.getResource("/GiftSurgIconOnly.png"));
            if (iconImage == null) {
                System.out.println("Could not find icon");
            } else {
                Application.getApplication().setDockIconImage(new ImageIcon(iconImage).getImage());
            }

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.UIElement", "true");

            final ResourceBundle resourceBundle = ResourceBundle.getBundle(resourceBundleName);
            final String applicationTitle = resourceBundle.getString("applicationTitle");

            // This is used to set the application title on OSX, but may not work when run from the debugger
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationTitle);

            new GiftCloudUploaderMain(resourceBundle);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}