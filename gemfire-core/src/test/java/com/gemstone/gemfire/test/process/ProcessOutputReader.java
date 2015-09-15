package com.gemstone.gemfire.test.process;

import java.util.concurrent.TimeUnit;

/**
 * Starts the stdout and stderr reader threads for a running process. Provides
 * a mechanism to waitFor the process to terminate.
 * </p>
 * Extracted from ProcessWrapper.
 * 
 * @author Kirk Lund
 */
public class ProcessOutputReader {

  private boolean started;
  
  private final Process process;
  private final ProcessStreamReader stdout;
  private final ProcessStreamReader stderr;
  
  public ProcessOutputReader(final Process process, final ProcessStreamReader stdout, final ProcessStreamReader stderr) {
    this.process = process;
    this.stdout = stdout;
    this.stderr = stderr;
  }

  public void start() {
    synchronized(this) {
      this.stdout.start();
      this.stderr.start();
      this.started = true;
    }
  }
  
  public boolean waitFor(final long timeout, final TimeUnit unit) throws InterruptedException {
    synchronized(this) {
      if (!this.started) {
        throw new IllegalStateException("Must be started before waitFor");
      }
    }
    
    final long startTime = System.nanoTime();
    
    long millisToJoin = unit.toMillis(timeout);
    this.stderr.join(millisToJoin);

    long nanosRemaining = unit.toNanos(timeout) - (System.nanoTime() - startTime);
    millisToJoin = unit.toMillis(nanosRemaining);
    this.stdout.join(millisToJoin);

    nanosRemaining = unit.toNanos(timeout) - (System.nanoTime() - startTime);
    return waitForProcess(nanosRemaining, unit);
  }

  private boolean waitForProcess(final long timeout, final TimeUnit unit) throws InterruptedException {
    long startTime = System.nanoTime();
    long nanosRemaining = unit.toNanos(timeout);

    while (nanosRemaining > 0) {
      try {
        this.process.exitValue();
        return true;
      } catch(IllegalThreadStateException ex) {
        if (nanosRemaining > 0) {
          long millisToSleep =Math.min(TimeUnit.NANOSECONDS.toMillis(nanosRemaining) + 1, 100);
          Thread.sleep(millisToSleep);
        }
      }
      nanosRemaining = unit.toNanos(timeout) - (System.nanoTime() - startTime);
    }
    return false;
  }
}
