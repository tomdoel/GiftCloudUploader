/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.MockRestClientFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * GiftCloudIntegrationTestApp is a Java application that mimics GiftCloudUploaderApp but does not connect to the server. A fake server is used instead, allowing this application to be used for testing purposes
 */
public class GiftCloudIntegrationTestApp {

	/**
	 * <p>The method to invoke the application.</p>
	 *
	 * @param	arg	none
	 */
	public static void main(String arg[]) {
		final List<File> fileList = new ArrayList<File>();
		if (arg.length==2) {
			fileList.add(new File(arg[1]));
		}

		new GiftCloudUploaderApp(new MockRestClientFactory(), fileList);
	}
}
