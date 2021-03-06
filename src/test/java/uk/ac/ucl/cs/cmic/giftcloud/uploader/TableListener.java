/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;
import static java.lang.Math.max;

class TableListener implements TableModelListener {
    private int expectedFirstRowChanged = -1;
    private int expectedLastRowChanged = -1;
    private int firstRowChanged = -1;
    private int lastRowChanged = -1;
    private int columns = -1;
    private boolean expectationHit = false;
    private CountDownLatch endLatch;

    @Override
    public synchronized void tableChanged(TableModelEvent e) {
        if (firstRowChanged == -1) {
            firstRowChanged = e.getFirstRow();
        } else {
            firstRowChanged = min(firstRowChanged, e.getFirstRow());
        }
        if (lastRowChanged == -1) {
            lastRowChanged = e.getLastRow();
        } else {
            lastRowChanged = max(lastRowChanged, e.getLastRow());
        }
        columns = e.getColumn();
        if (!expectationHit) {
            if (firstRowChanged == expectedFirstRowChanged && lastRowChanged == expectedLastRowChanged) {
                endLatch.countDown();
                expectationHit = true;
            }
        }
    }

    public synchronized void clear() {
        firstRowChanged = -1;
        lastRowChanged = -1;
        columns = -1;
    }

    public synchronized void clearAndSetExpectations(int expectedFirstRowChanged, int expectedLastRowChanged) {
        clear();
        expectationHit = false;
        endLatch = new CountDownLatch(1);
        this.expectedFirstRowChanged = expectedFirstRowChanged;
        this.expectedLastRowChanged = expectedLastRowChanged;
    }

    public void waitForCompletion() {
        try {
            // If it takes longer than 1 second then we assume the thread expectations are not fulfilled, so this means the test has failed
            boolean ok = endLatch.await(1000, TimeUnit.MILLISECONDS);
            Assert.assertTrue("TableListener: The expected event did not fire within the given time", ok);
        } catch (InterruptedException e) {
            Assert.fail();
        }
    }
}
