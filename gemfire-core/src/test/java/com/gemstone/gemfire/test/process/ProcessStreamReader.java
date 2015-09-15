package com.gemstone.gemfire.test.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Queue;

/**
 * Reads the output from a process stream and stores it for test validation. 
 * </p>
 * Extracted from ProcessWrapper.
 * 
 * @author Kirk Lund
 */
public class ProcessStreamReader extends Thread {
  
  private volatile RuntimeException startStack;
  
  private final String command;
  private final BufferedReader reader;
  private final Queue<String> lineBuffer;
  private final List<String> allLines;

  public int linecount = 0;

  public ProcessStreamReader(final String command, final InputStream stream, final Queue<String> lineBuffer, final List<String> allLines) {
    this.command = command;
    this.reader = new BufferedReader(new InputStreamReader(stream));
    this.lineBuffer = lineBuffer;
    this.allLines = allLines;
  }

  @Override
  public void start() {
    this.startStack = new RuntimeException(this.command);
    super.start();
  }
  
  @Override
  public void run() {
    try {
      String line;
      while ((line = this.reader.readLine()) != null) {
        this.linecount++;
        this.lineBuffer.offer(line);
        this.allLines.add(line);
      }

      // EOF
      this.reader.close();
    } catch (IOException streamClosed) {
      this.startStack.initCause(streamClosed);
      throw this.startStack;
    }
  }
}
