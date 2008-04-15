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
import java.io.OutputStream;
import java.util.*;

/**
 * Multipart request handler that provides for a streaming approach for
 * resolving the parts of the request.<br/><br/>
 *
 * Using the <tt>StreamingMultipartRequestHandler</tt> allows you to have access to the parts
 * before they're completely uploaded, but the parsed parts are subject to the following
 * restrictions:<br/><br/>
 *
 * <ul>
 *   <li>When a multipart request is parsed, the resulting collection of parts has an undefined size.
 *   Any attempts to access the size of the collection (including toArray() and isEmpty()) will result in an
 *   UnsupportedOperationException.</li>
 *   <li>Iterating to the "next" item in the collection (using the iterator) invalidates the "previous" item
 *   in the collection (since the stream cursor advances to the next file).</li>
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
   * collection (including toArray()) will result in an UnsupportedOperationException.<br/><br/>
   *
   * The DataHandler instances each will have a DataSource that is an instance of
   * {@link org.codehaus.enunciate.modules.rest.StreamingFileItemDataSource}
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
    private boolean modified;

    public StreamingMultipartCollection(FileItemIterator itemIterator, ProgressListener progressListener) {
      this.itemIterator = itemIterator;
      this.progressListener = progressListener;
      modified = false;
    }

    public Iterator<DataHandler> iterator() {
      if (!modified) {
        StreamingMultipartIterator iterator = new StreamingMultipartIterator(this.itemIterator);
        modified = true;
        return iterator;
      }
      else {
        throw new ConcurrentModificationException("This streaming collection has already been read.");
      }
    }

    public int size() {
      throw new UnsupportedOperationException("This is a streaming collection; size is unknown.");
    }

    private class StreamingMultipartIterator implements Iterator<DataHandler> {

      private final FileItemIterator itemIterator;
      private long bytesReadSoFar = 0L;
      private int currentItemNumber = 0;

      public StreamingMultipartIterator(FileItemIterator itemIterator) {
        this.itemIterator = itemIterator;
      }

      public boolean hasNext() {
        try {
          return this.itemIterator.hasNext();
        }
        catch (FileUploadException e) {
          throw new StreamingMultipartException("Error parsing multipart request.", e);
        }
        catch (IOException e) {
          throw new StreamingMultipartException("Error parsing multipart request.", e);
        }
      }

      public DataHandler next() {
        try {
          return new DataHandler(new FileItemStreamDataSource(this.itemIterator.next(), this.currentItemNumber));
        }
        catch (FileUploadException e) {
          throw new StreamingMultipartException("Error parsing multipart request.", e);
        }
        catch (IOException e) {
          throw new StreamingMultipartException("Error parsing multipart request.", e);
        }
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      private class FileItemStreamDataSource implements StreamingFileItemDataSource {

        private final FileItemStream itemStream;
        private final int itemNumber;

        public FileItemStreamDataSource(FileItemStream itemStream, int itemNumber) {
          this.itemStream = itemStream;
          this.itemNumber = itemNumber;
        }

        public InputStream getInputStream() throws IOException {
          return new ProgressAwareInputStream(itemStream.openStream());
        }

        public boolean isFormField() {
          return this.itemStream.isFormField();
        }

        public String getFormFieldName() {
          return this.itemStream.getFieldName();
        }

        public OutputStream getOutputStream() throws IOException {
          throw new IOException();
        }

        public String getName() {
          return this.itemStream.getName();
        }

        public String getContentType() {
          return itemStream.getContentType();
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
