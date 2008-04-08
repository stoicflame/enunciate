/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.rest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Multipart request handler that provides for a streaming approach for
 * resolving the parts of the request.<br/><br/>
 *
 * Using the <tt>StreamingMultipartRequestHandler</tt> allows you to have access to the parts
 * before they're completely uploaded, but the parsed parts are subject to the following
 * restrictions:<br/><br/>
 *
 * <ul>
 * <li>The multipart request is parsed ONLY for its file fields.  (The form fields are ignored).</li>
 * <li>When a multipart request is parsed, the resulting collection of parts has an undefined size.
 * Any attempts to access the size of the collection (including toArray()) will result in an
 * UnsupportedOperationException.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class StreamingMultipartRequestHandler implements MultipartRequestHandler {

  private MultipartProgressListenerFactory progressListenerFactory;

  /**
   * Whether the request is a multipart request, according to commons-fileupload.
   *
   * @param request The request.
   * @return Whether the request is a multipart request.
   */
  public boolean isMultipart(HttpServletRequest request) {
    return ServletFileUpload.isMultipartContent(request);
  }

  /**
   * No-op, returns the request unmodified.
   *
   * @param request The request.
   * @return The request (unmodified).
   */
  public HttpServletRequest handleMultipartRequest(HttpServletRequest request) {
    return request;
  }

  /**
   * Parses the parts.  The resulting collection has an undefined size. Any attempts to access the size of the
   * collection (including toArray()) will result in an UnsupportedOperationException.
   *
   * @param request The request.
   * @return The parts.
   */
  public Collection<DataHandler> parseParts(HttpServletRequest request) throws IOException, FileUploadException {
    ServletFileUpload fileUpload = new ServletFileUpload();
    FileItemIterator itemIterator = fileUpload.getItemIterator(request);
    ProgressListener progressListener = null;
    if (getProgressListenerFactory() != null) {
      progressListener = getProgressListenerFactory().newProgressListener(request);
    }
    return new StreamingMultipartCollection(itemIterator, progressListener);
  }

  /**
   * A progress listener for the file upload.
   *
   * @return A progress listener for the file upload.
   */
  public MultipartProgressListenerFactory getProgressListenerFactory() {
    return progressListenerFactory;
  }

  /**
   * A progress listener for the file upload.
   *
   * @param progressListenerFactory A progress listener for the file upload.
   */
  public void setProgressListenerFactory(MultipartProgressListenerFactory progressListenerFactory) {
    this.progressListenerFactory = progressListenerFactory;
  }

  private static class StreamingMultipartCollection extends AbstractCollection<DataHandler> {

    private final FileItemIterator itemIterator;
    private final ProgressListener progressListener;

    public StreamingMultipartCollection(FileItemIterator itemIterator, ProgressListener progressListener) {
      this.itemIterator = itemIterator;
      this.progressListener = progressListener;
    }

    public Iterator<DataHandler> iterator() {
      return new StreamingMultipartIterator(this.itemIterator);
    }

    public int size() {
      throw new UnsupportedOperationException("This is a streaming collection; size is unknown.");
    }

    private class StreamingMultipartIterator implements Iterator<DataHandler> {

      private final FileItemIterator itemIterator;
      private FileItemStream nextStream;
      private long bytesReadSoFar = 0L;
      private int currentItemNumber = 0;

      public StreamingMultipartIterator(FileItemIterator itemIterator) {
        this.itemIterator = itemIterator;
        advanceNextStream();
      }

      public boolean hasNext() {
        return this.nextStream != null;
      }

      public DataHandler next() {
        if (this.nextStream == null) {
          throw new NoSuchElementException();
        }

        DataHandler dataHandler = new DataHandler(new FileItemStreamDataSource(this.nextStream, this.currentItemNumber));
        advanceNextStream();
        return dataHandler;
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      /**
       * Advances to the next stream, skipping any form fields.
       */
      private void advanceNextStream() {
        this.nextStream = null;
        try {
          while (this.itemIterator.hasNext()) {
            FileItemStream nextStream = this.itemIterator.next();
            while (!nextStream.isFormField()) {
              //skip any form fields.
              nextStream = this.itemIterator.next();
            }
            this.nextStream = nextStream;
            this.currentItemNumber++;
          }
        }
        catch (FileUploadException e) {
          throw new StreamingMultipartException("Error parsing multipart request.", e);
        }
        catch (IOException e) {
          throw new StreamingMultipartException("Error parsing multipart request.", e);
        }
      }

      private class FileItemStreamDataSource extends RESTRequestDataSource {

        private final FileItemStream itemStream;
        private final int itemNumber;

        public FileItemStreamDataSource(FileItemStream itemStream, int itemNumber) {
          super(null, itemStream.getName());
          this.itemStream = itemStream;
          this.itemNumber = itemNumber;
        }

        public InputStream getInputStream() throws IOException {
          return new ProgressAwareInputStream(itemStream.openStream());
        }

        public String getContentType() {
          return itemStream.getContentType();
        }

        @Override
        public long getSize() {
          return super.getSize();
        }

        private class ProgressAwareInputStream extends FilterInputStream {

          public ProgressAwareInputStream(InputStream in) {
            super(in);
          }

          @Override
          public int read() throws IOException {
            int value = super.read();
            if (value != -1) {
              StreamingMultipartIterator.this.bytesReadSoFar++;
              updateProgress();
            }
            return value;
          }

          @Override
          public int read(byte b[]) throws IOException {
            int bytesRead = super.read(b);
            if (bytesRead >= 0) {
              StreamingMultipartIterator.this.bytesReadSoFar += bytesRead;
              updateProgress();
            }
            return bytesRead;
          }

          @Override
          public int read(byte b[], int off, int len) throws IOException {
            int bytesRead = super.read(b, off, len);
            if (bytesRead >= 0) {
              StreamingMultipartIterator.this.bytesReadSoFar += bytesRead;
              updateProgress();
            }
            return bytesRead;
          }

          private void updateProgress() {
            if (StreamingMultipartCollection.this.progressListener != null) {
              StreamingMultipartCollection.this.progressListener.update(StreamingMultipartIterator.this.bytesReadSoFar, -1, FileItemStreamDataSource.this.itemNumber);
            }
          }
        }
      }
    }
  }
}
