package uk.ac.ucl.cs.cmic.giftcloud.workers;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudHttpException;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapp.GiftCloudReporterFromApplication;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUploader;

import java.util.List;
import java.util.Vector;

public class GiftCloudUploadWorker implements Runnable {
    List<String> sourceFilePathSelections;
    private GiftCloudReporterFromApplication reporter;
    private GiftCloudUploader giftCloudUploader;

    public GiftCloudUploadWorker(List<String> sourceFilePathSelections, final GiftCloudUploader giftCloudUploader, final GiftCloudReporterFromApplication reporter) {
        this.sourceFilePathSelections = sourceFilePathSelections;
        this.reporter = reporter;
        this.giftCloudUploader = giftCloudUploader;
    }

    public void run() {
        if (giftCloudUploader == null) {
            reporter.showError("An error occurred which prevents the uploader from connecting to the server. Please restart GIFT-Cloud uploader.");
            return;
        }

        reporter.setWaitCursor();

        if (sourceFilePathSelections == null) {
            reporter.updateStatusText("No files selected for upload.");
            reporter.showError("No files were selected for uploading.");
        } else {
            reporter.sendLn("GIFT-Cloud upload started");
            reporter.startProgressBar();
            try {
                if (giftCloudUploader.uploadToGiftCloud(sourceFilePathSelections)) {
                    reporter.updateStatusText("GIFT-Cloud upload complete");
                } else {
                    reporter.updateStatusText("GIFT-Cloud upload failed");
                }
            } catch (GiftCloudHttpException e) {
                reporter.updateStatusText("GIFT-Cloud upload failed with the following error: " + e.getHtmlText());
                e.printStackTrace(System.err);
            } catch (Exception e) {
                reporter.updateStatusText("GIFT-Cloud upload failed with the following error: " + e.toString());
                e.printStackTrace(System.err);
            }
            reporter.endProgressBar();
        }
        reporter.restoreCursor();
    }
}
