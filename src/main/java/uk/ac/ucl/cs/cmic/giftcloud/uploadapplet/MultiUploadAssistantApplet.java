/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    Released under the Simplified BSD.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;

import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerXnat;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class MultiUploadAssistantApplet extends JApplet {

    private Optional<MultiUploadAppletReporter> reporter = Optional.empty();
    private Optional<MultiUploadAppletParameters> multiUploadParameters = Optional.empty();
    private Optional<RestServerHelper> restServerHelper = Optional.empty();

    /**
     * Default constructor.
     */
    public MultiUploadAssistantApplet() {
        setLayout(new BorderLayout());
        reporter = Optional.of(new MultiUploadAppletReporter(this));
    }

    /**
     * Initializes the applet.
     *
     * @see java.applet.Applet#init()
     */
    @Override
    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            multiUploadParameters = Optional.of(new MultiUploadAppletParameters(this, reporter.get()));

            GiftCloudPropertiesFromWizard giftCloudPropertiesFromWizard = new GiftCloudPropertiesFromWizard(multiUploadParameters.get());

            final RestServerXnat restServerXnat = new RestServerXnat(giftCloudPropertiesFromWizard, multiUploadParameters.get().getStrippedXnatUrl().get(), reporter.get());
            restServerHelper = Optional.of(new RestServerHelper(restServerXnat, reporter.get()));
            restServerHelper.get().tryAuthentication();
        } catch (Throwable t) {
            reporter.get().errorBox("Applet initialisation failed", t);
            throw new RuntimeException(t);
        }
    }

    /**
     * Implementation of the {@link java.applet.Applet#start()} method.
     *
     * @see java.applet.Applet#start()
     */
    @Override
    public void start() {
        try {
            final Dimension windowSize = new Dimension(300, 300);
            new MultiUploadWizard(restServerHelper.get(), windowSize, multiUploadParameters.get(), reporter.get());
        } catch (Throwable t) {
            reporter.get().errorBox("Applet startup failed", t);
            throw new RuntimeException(t);
        }
    }

    /**
     * Implementation of the {@link java.applet.Applet#stop()} method.
     *
     * @see java.applet.Applet#stop()
     */
    @Override
    public void stop()
    {
    }

    /**
     * Implementation of the {@link java.applet.Applet#getAppletInfo()} method.
     *
     * @see java.applet.Applet#getAppletInfo()
     */
    @Override
    public String getAppletInfo() {
        return MultiUploadAppletParameterInfo.getAppletInfo();
    }

    /**
     * Implementation of the {@link java.applet.Applet#getParameterInfo()} method.
     *
     * @see java.applet.Applet#getParameterInfo()
     */
    @Override
    public String[][] getParameterInfo() {
        return MultiUploadAppletParameterInfo.getParameterInfo();
    }
}
