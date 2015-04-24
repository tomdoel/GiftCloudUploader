package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BackgroundServiceTaskWrapperTest {

    @Test
    public void testGetTask() throws Exception {
        final BackgroundServiceErrorRecord errorRecord = new BackgroundServiceErrorRecord();
        final String task = "Task1";
        final String result = "Result1";
        BackgroundServiceTaskWrapper<String, String> taskWrapper = new BackgroundServiceTaskWrapper<String, String>(task, result, errorRecord);
        Assert.assertEquals(taskWrapper.getTask(), task);
    }

    @Test
    public void testGetErrorRecord() throws Exception {
        final BackgroundServiceErrorRecord errorRecord = new BackgroundServiceErrorRecord();
        final String task = "Task1";
        final String result = "Result1";
        BackgroundServiceTaskWrapper<String, String> taskWrapper = new BackgroundServiceTaskWrapper<String, String>(task, result, errorRecord);
        Assert.assertEquals(taskWrapper.getErrorRecord(), errorRecord);
    }

    @Test
    public void testAddError() throws Exception {
        final BackgroundServiceErrorRecord errorRecord = new BackgroundServiceErrorRecord();
        final String task = "Task1";
        final String result = "Result1";
        BackgroundServiceTaskWrapper<String, String> taskWrapper = new BackgroundServiceTaskWrapper<String, String>(task, result, errorRecord);
        Assert.assertEquals(errorRecord.getErrorList().size(), 0);

        final Exception exception1 = new Exception("Exception1");
        taskWrapper.addError(exception1);
        Assert.assertEquals(errorRecord.getErrorList().size(), 1);
        Assert.assertEquals(errorRecord.getErrorList().get(0).getException(), exception1);

        final Exception exception2 = new Exception("Exception2");
        taskWrapper.addError(exception2);
        Assert.assertEquals(errorRecord.getErrorList().size(), 2);
        Assert.assertEquals(errorRecord.getErrorList().get(0).getException(), exception1);
        Assert.assertEquals(errorRecord.getErrorList().get(1).getException(), exception2);
    }

    @Test
    public void testShouldRetry() throws Exception {
        final BackgroundServiceErrorRecord errorRecord = mock(BackgroundServiceErrorRecord.class);
        final String task = "Task1";
        final String result = "Result1";
        BackgroundServiceTaskWrapper<String, String> taskWrapper = new BackgroundServiceTaskWrapper<String, String>(task, result, errorRecord);

        when(errorRecord.shouldRetry()).thenReturn(true);
        Assert.assertTrue(taskWrapper.shouldRetry());

        when(errorRecord.shouldRetry()).thenReturn(false);
        Assert.assertFalse(taskWrapper.shouldRetry());
    }

    @Test
    public void testGetResult() throws Exception {
        final BackgroundServiceErrorRecord errorRecord = new BackgroundServiceErrorRecord();
        final String task = "Task1";
        final String result = "Result1";
        BackgroundServiceTaskWrapper<String, String> taskWrapper = new BackgroundServiceTaskWrapper<String, String>(task, result, errorRecord);
        Assert.assertEquals(taskWrapper.getResult(), result);
    }
}