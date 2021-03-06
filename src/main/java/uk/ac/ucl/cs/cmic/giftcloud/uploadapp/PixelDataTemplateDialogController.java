/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Some parts of this software were derived from DicomCleaner,
    Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import uk.ac.ucl.cs.cmic.giftcloud.uploader.PixelDataAnonymiserFilterCache;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudUtils;

/**
 * Factory/controller class for the dialog that allows creation of pixel data redaction templates
 *
 * @author  Tom Doel
 */
class PixelDataTemplateDialogController {
    private PixelDataTemplateDialog pixelDataDialog = null;
    private final GiftCloudUploaderAppConfiguration appConfiguration;
    private final PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache;
    private final MainFrame mainFrame;
    private final GiftCloudDialogs dialogs;
    private final GiftCloudReporterFromApplication reporter;

    /**
     * Creates a new controller class
     *
     * @param appConfiguration
     * @param pixelDataAnonymiserFilterCache
     * @param mainFrame
     * @param dialogs
     * @param reporter
     */
    PixelDataTemplateDialogController(final GiftCloudUploaderAppConfiguration appConfiguration, PixelDataAnonymiserFilterCache pixelDataAnonymiserFilterCache, final MainFrame mainFrame, final GiftCloudDialogs dialogs, final GiftCloudReporterFromApplication reporter) {
        this.appConfiguration = appConfiguration;
        this.pixelDataAnonymiserFilterCache = pixelDataAnonymiserFilterCache;
        this.mainFrame = mainFrame;
        this.dialogs = dialogs;
        this.reporter = reporter;
    }

    /**
     * Lazy creation for showing the pixel data template dialog
     */
    void showPixelDataTemplateDialog() {
        if (pixelDataDialog == null || !pixelDataDialog.isVisible()) {
            GiftCloudUtils.runLaterOnEdt(new Runnable() {
                @Override
                public void run() {
                    pixelDataDialog = new PixelDataTemplateDialog(mainFrame.getContainer(), appConfiguration.getResourceBundle().getString("pixelDataDialogTitle"), pixelDataAnonymiserFilterCache, appConfiguration.getProperties(), dialogs, reporter);
                }
            });
        }
    }
}
