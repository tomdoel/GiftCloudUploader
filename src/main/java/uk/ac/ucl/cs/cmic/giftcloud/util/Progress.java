/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Parts of this software are derived from XNAT
    http://www.xnat.org
    Copyright (c) 2014, Washington University School of Medicine
    All Rights Reserved
    See license/XNAT_license.txt

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.util;

public interface Progress {
    void startProgressBar(int maximum);

    void startProgressBar();

    void updateProgressBar(int value);

    void updateProgressBar(int value, int maximum);

    void endProgressBar();

    void updateStatusText(final String progressText);

    boolean isCancelled();
}
