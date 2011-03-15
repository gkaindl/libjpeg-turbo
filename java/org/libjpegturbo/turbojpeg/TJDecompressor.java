/*
 * Copyright (C)2011 D. R. Commander.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the libjpeg-turbo Project nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS",
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.libjpegturbo.turbojpeg;

import java.awt.image.*;

/**
 * TurboJPEG decompressor
 */
public class TJDecompressor {

  /**
   * Create a TurboJPEG decompresssor instance.
   */
  public TJDecompressor() throws Exception {
    init();
  }

  /**
   * Create a TurboJPEG decompressor instance and associate the JPEG image
   * stored in <code>jpegImage</code> with the newly-created instance.
   *
   * @param jpegImage JPEG image buffer (size of JPEG image is assumed to be
   * the length of the buffer)
   */
  public TJDecompressor(byte[] jpegImage) throws Exception {
    init();
    setJPEGBuffer(jpegImage, jpegImage.length);
  }

  /**
   * Create a TurboJPEG decompressor instance and associate the JPEG image
   * of length <code>imageSize</code> bytes stored in <code>jpegImage</code>
   * with the newly-created instance.
   *
   * @param jpegImage JPEG image buffer
   *
   * @param imageSize size of JPEG image (in bytes)
   */
  public TJDecompressor(byte[] jpegImage, int imageSize) throws Exception {
    init();
    setJPEGBuffer(jpegImage, imageSize);
  }

  /**
   * Associate a JPEG image buffer with this decompressor instance.  This
   * buffer will be used as the source buffer for subsequent decompress
   * operations.
   *
   * @param jpegImage JPEG image buffer
   *
   * @param imageSize size of JPEG image (in bytes)
   */
  public void setJPEGBuffer(byte[] jpegImage, int imageSize) throws Exception {
    if(jpegImage == null || imageSize < 1)
      throw new Exception("Invalid argument in setJPEGBuffer()");
    jpegBuf = jpegImage;
    jpegBufSize = imageSize;
    decompressHeader(jpegBuf, jpegBufSize);
  }

  /**
   * Returns the width of the JPEG image associated with this decompressor
   * instance.
   *
   * @return the width of the JPEG image associated with this decompressor
   * instance
   */
  public int getWidth() throws Exception {
    if(jpegWidth < 1) throw new Exception("JPEG buffer not initialized");
    return jpegWidth;
  }

  /**
   * Returns the height of the JPEG image associated with this decompressor
   * instance.
   *
   * @return the height of the JPEG image associated with this decompressor
   * instance
   */
  public int getHeight() throws Exception {
    if(jpegHeight < 1) throw new Exception("JPEG buffer not initialized");
    return jpegHeight;
  }

  /**
   * Returns the level of chrominance subsampling used in the JPEG image
   * associated with this decompressor instance.
   *
   * @return the level of chrominance subsampling used in the JPEG image
   * associated with this decompressor instance
   */
  public int getSubsamp() throws Exception {
    if(jpegSubsamp < 0) throw new Exception("JPEG buffer not initialized");
    if(jpegSubsamp >= TJ.NUMSAMPOPT)
      throw new Exception("JPEG header information is invalid");
    return jpegSubsamp;
  }

  /**
   * Returns the JPEG image buffer associated with this decompressor instance.
   *
   * @return the JPEG image buffer associated with this decompressor instance
   */
  public byte[] getJPEGBuf() throws Exception {
    if(jpegBuf == null) throw new Exception("JPEG buffer not initialized");
    return jpegBuf;
  }

  /**
   * Returns the size of the JPEG image (in bytes) associated with this
   * decompressor instance.
   *
   * @return the size of the JPEG image (in bytes) associated with this
   * decompressor instance
   */
  public int getJPEGSize() throws Exception {
    if(jpegBufSize < 1) throw new Exception("JPEG buffer not initialized");
    return jpegBufSize;
  }


  /**
   * Returns the width of the largest scaled down image that the TurboJPEG
   * decompressor can generate without exceeding the desired image width and
   * height.
   *
   * @param desiredWidth desired width (in pixels) of the decompressed image.
   * If this is set to 0, then only the height will be considered when
   * determining the scaled image size.
   *
   * @param desiredHeight desired height (in pixels) of the decompressed image.
   * If this is set to 0, then only the width will be considered when
   * determining the scaled image size.
   *
   * @return the width of the largest scaled down image that the TurboJPEG
   * decompressor can generate without exceeding the desired image width and
   * height
   */
  public int getScaledWidth(int desiredWidth, int desiredHeight)
    throws Exception {
    if(jpegWidth < 1 || jpegHeight < 1)
      throw new Exception("JPEG buffer not initialized");
    if(desiredWidth < 0 || desiredHeight < 0)
      throw new Exception("Invalid argument in getScaledWidth()");
    TJ.ScalingFactor sf[] = TJ.getScalingFactors();
    if(desiredWidth == 0) desiredWidth = jpegWidth;
    if(desiredHeight == 0) desiredHeight = jpegHeight;
    int scaledWidth = jpegWidth, scaledHeight = jpegHeight;
    for(int i = 0; i < sf.length; i++) {
      scaledWidth = (jpegWidth * sf[i].num + sf[i].denom - 1) / sf[i].denom;
      scaledHeight = (jpegHeight * sf[i].num + sf[i].denom - 1) / sf[i].denom;
      if(scaledWidth <= desiredWidth && scaledHeight <= desiredHeight)
        break;
    }
    if(scaledWidth > desiredWidth || scaledHeight > desiredHeight)
      throw new Exception("Could not scale down to desired image dimensions");
    return scaledWidth;
  }

  /**
   * Returns the height of the largest scaled down image that the TurboJPEG
   * decompressor can generate without exceeding the desired image width and
   * height.
   *
   * @param desiredWidth desired width (in pixels) of the decompressed image.
   * If this is set to 0, then only the height will be considered when
   * determining the scaled image size.
   *
   * @param desiredHeight desired height (in pixels) of the decompressed image.
   * If this is set to 0, then only the width will be considered when
   * determining the scaled image size.
   *
   * @return the height of the largest scaled down image that the TurboJPEG
   * decompressor can generate without exceeding the desired image width and
   * height
   */
  public int getScaledHeight(int desiredWidth, int desiredHeight)
    throws Exception {
    if(jpegWidth < 1 || jpegHeight < 1)
      throw new Exception("JPEG buffer not initialized");
    if(desiredWidth < 0 || desiredHeight < 0)
      throw new Exception("Invalid argument in getScaledHeight()");
    TJ.ScalingFactor sf[] = TJ.getScalingFactors();
    if(desiredWidth == 0) desiredWidth = jpegWidth;
    if(desiredHeight == 0) desiredHeight = jpegHeight;
    int scaledWidth = jpegWidth, scaledHeight = jpegHeight;
    for(int i = 0; i < sf.length; i++) {
      scaledWidth = (jpegWidth * sf[i].num + sf[i].denom - 1) / sf[i].denom;
      scaledHeight = (jpegHeight * sf[i].num + sf[i].denom - 1) / sf[i].denom;
      if(scaledWidth <= desiredWidth && scaledHeight <= desiredHeight)
        break;
    }
    if(scaledWidth > desiredWidth || scaledHeight > desiredHeight)
      throw new Exception("Could not scale down to desired image dimensions");
    return scaledHeight;
  }

  /**
   * Decompress the JPEG source image associated with this decompressor
   * instance and output a decompressed image to the given destination buffer.
   *
   * @param dstBuf buffer which will receive the decompressed image.  This
   * buffer should normally be <code>pitch * scaledHeight</code> bytes in size,
   * where <code>scaledHeight = ceil(jpegHeight * scalingFactor)</code>, and
   * the supported scaling factors can be determined by calling {@link
   * TJ#getScalingFactors}.
   *
   * @param desiredWidth desired width (in pixels) of the decompressed image.
   * If the desired image dimensions are smaller than the dimensions of the
   * JPEG image being decompressed, then TurboJPEG will use scaling in the JPEG
   * decompressor to generate the largest possible image that will fit within
   * the desired dimensions.  If desiredWidth is set to 0, then only the height
   * will be considered when determining the scaled image size.
   *
   * @param pitch bytes per line of the destination image.  Normally, this
   * should be set to <code>scaledWidth * TJ.pixelSize(pixelFormat)</code> if
   * the decompressed image is unpadded, but you can use this to, for instance,
   * pad each line of the decompressed image to a 4-byte boundary.  NOTE:
   * <code>scaledWidth = ceil(jpegWidth * scalingFactor)</code>.  Setting this
   * parameter to 0 is the equivalent of setting it to
   * <code>scaledWidth * pixelSize</code>.
   *
   * @param desiredHeight desired height (in pixels) of the decompressed image.
   * If the desired image dimensions are smaller than the dimensions of the
   * JPEG image being decompressed, then TurboJPEG will use scaling in the JPEG
   * decompressor to generate the largest possible image that will fit within
   * the desired dimensions.  If desiredHeight is set to 0, then only the
   * width will be considered when determining the scaled image size.
   *
   * @param pixelFormat Pixel format of the decompressed image (see
   * {@link TJ})
   *
   * @param flags the bitwise OR of one or more of the flags described in
   * {@link TJ}
   */
  public void decompress(byte[] dstBuf, int desiredWidth, int pitch,
    int desiredHeight, int pixelFormat, int flags) throws Exception {
    if(jpegBuf == null) throw new Exception("JPEG buffer not initialized");
    if(dstBuf == null || desiredWidth < 0 || pitch < 0 || desiredHeight < 0
      || pixelFormat < 0 || pixelFormat >= TJ.NUMPFOPT || flags < 0)
      throw new Exception("Invalid argument in decompress()");
    decompress(jpegBuf, jpegBufSize, dstBuf, desiredWidth, pitch,
      desiredHeight, pixelFormat, flags);
  }

  /**
   * Decompress the JPEG source image associated with this decompressor
   * instance and return a buffer containing the decompressed image.
   *
   * @param desiredWidth see
   * {@link #decompress(byte[], int, int, int, int, int)} for description
   *
   * @param pitch see
   * {@link #decompress(byte[], int, int, int, int, int)} for description
   *
   * @param desiredHeight see
   * {@link #decompress(byte[], int, int, int, int, int)} for description
   *
   * @param pixelFormat Pixel format of the decompressed image (see
   * {@link TJ})
   *
   * @param flags the bitwise OR of one or more of the flags described in
   * {@link TJ}
   *
   * @return a buffer containing the decompressed image
   */
  public byte[] decompress(int desiredWidth, int pitch, int desiredHeight,
    int pixelFormat, int flags) throws Exception {
    if(desiredWidth < 0 || pitch < 0 || desiredHeight < 0
      || pixelFormat < 0 || pixelFormat >= TJ.NUMPFOPT || flags < 0)
      throw new Exception("Invalid argument in decompress()");
    int pixelSize = TJ.getPixelSize(pixelFormat);
    int scaledWidth = getScaledWidth(desiredWidth, desiredHeight);
    int scaledHeight = getScaledHeight(desiredWidth, desiredHeight);
    if(pitch == 0) pitch = scaledWidth * pixelSize;
    byte[] buf = new byte[pitch * scaledHeight];
    decompress(buf, desiredWidth, pitch, desiredHeight, pixelFormat, flags);
    return buf;
  }

  /**
   * Decompress the JPEG source image associated with this decompressor
   * instance and output a YUV planar image to the given destination buffer.
   * This method performs JPEG decompression but leaves out the color
   * conversion step, so a planar YUV image is generated instead of an RGB
   * image.  The padding of the planes in this image is the same as the images
   * generated by {@link TJCompressor#encodeYUV(byte[], int)}.  Note that, if
   * the width or height of the image is not an even multiple of the MCU block
   * size (see {@link TJ#getMCUWidth} and {@link TJ#getMCUHeight}), then an
   * intermediate buffer copy will be performed within TurboJPEG.
   *
   * @param dstBuf buffer which will receive the YUV planar image.  Use
   * {@link TJ#bufSizeYUV} to determine the appropriate size for this buffer
   * based on the image width, height, and level of chrominance subsampling.
   *
   * @param flags the bitwise OR of one or more of the flags described in
   * {@link TJ}
   */
  public void decompressToYUV(byte[] dstBuf, int flags) throws Exception {
    if(jpegBuf == null) throw new Exception("JPEG buffer not initialized");
    if(dstBuf == null || flags < 0)
      throw new Exception("Invalid argument in decompressToYUV()");
    decompressToYUV(jpegBuf, jpegBufSize, dstBuf, flags);
  }

  
  /**
   * Decompress the JPEG source image associated with this decompressor
   * instance and return a buffer containing a YUV planar image.  See {@link
   * #decompressToYUV(byte[], int)} for more detail.
   *
   * @param flags the bitwise OR of one or more of the flags described in
   * {@link TJ}
   *
   * @return a buffer containing a YUV planar image
   */
  public byte[] decompressToYUV(int flags) throws Exception {
    if(flags < 0)
      throw new Exception("Invalid argument in decompressToYUV()");
    if(jpegWidth < 1 || jpegHeight < 1 || jpegSubsamp < 0)
      throw new Exception("JPEG buffer not initialized");
    if(jpegSubsamp >= TJ.NUMSAMPOPT)
      throw new Exception("JPEG header information is invalid");
    byte[] buf = new byte[TJ.bufSizeYUV(jpegWidth, jpegHeight, jpegSubsamp)];
    decompressToYUV(buf, flags);
    return buf;
  }

  /**
   * Decompress the JPEG source image associated with this decompressor
   * instance and output a decompressed image to the given
   * <code>BufferedImage</code> instance.
   *
   * @param dstImage a <code>BufferedImage</code> instance which will receive
   * the decompressed image
   *
   * @param flags the bitwise OR of one or more of the flags described in
   * {@link TJ}
   */
  public void decompress(BufferedImage dstImage, int flags) throws Exception {
    if(dstImage == null || flags < 0)
      throw new Exception("Invalid argument in decompress()");
    int desiredWidth = dstImage.getWidth();
    int desiredHeight = dstImage.getHeight();
    int scaledWidth = getScaledWidth(desiredWidth, desiredHeight);
    int scaledHeight = getScaledHeight(desiredWidth, desiredHeight);
    if(scaledWidth != desiredWidth || scaledHeight != desiredHeight)
      throw new Exception("BufferedImage dimensions do not match a scaled image size that TurboJPEG is capable of generating.");
    int pixelFormat;  boolean intPixels = false;
    switch(dstImage.getType()) {
      case BufferedImage.TYPE_3BYTE_BGR:
        pixelFormat = TJ.PF_BGR;  break;
      case BufferedImage.TYPE_BYTE_GRAY:
        pixelFormat = TJ.PF_GRAY;  break;
      case BufferedImage.TYPE_INT_BGR:
        pixelFormat = TJ.PF_RGBX;  intPixels = true;  break;
      case BufferedImage.TYPE_INT_RGB:
        pixelFormat = TJ.PF_BGRX;  intPixels = true;  break;
      default:
        throw new Exception("Unsupported BufferedImage format");
    }
    WritableRaster wr = dstImage.getRaster();
    if(intPixels) {
      SinglePixelPackedSampleModel sm =
        (SinglePixelPackedSampleModel)dstImage.getSampleModel();
      int pitch = sm.getScanlineStride();
      DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
      int[] buf = db.getData();
      if(jpegBuf == null) throw new Exception("JPEG buffer not initialized");
      decompress(jpegBuf, jpegBufSize, buf, scaledWidth, pitch, scaledHeight,
        pixelFormat, flags);
    }
    else {
      ComponentSampleModel sm =
        (ComponentSampleModel)dstImage.getSampleModel();
      int pixelSize = sm.getPixelStride();
      if(pixelSize != TJ.getPixelSize(pixelFormat))
        throw new Exception("Inconsistency between pixel format and pixel size in BufferedImage");
      int pitch = sm.getScanlineStride();
      DataBufferByte db = (DataBufferByte)wr.getDataBuffer();
      byte[] buf = db.getData();
      decompress(buf, scaledWidth, pitch, scaledHeight, pixelFormat, flags);
    }
  }

  /**
   * Decompress the JPEG source image associated with this decompressor
   * instance and return a <code>BufferedImage</code> instance containing the
   * decompressed image.
   *
   * @param desiredWidth see
   * {@link #decompress(byte[], int, int, int, int, int)} for description
   *
   * @param desiredHeight see
   * {@link #decompress(byte[], int, int, int, int, int)} for description
   *
   * @param bufferedImageType the image type of the <code>BufferedImage</code>
   * instance to create (for instance, <code>BufferedImage.TYPE_INT_RGB</code>)
   *
   * @param flags the bitwise OR of one or more of the flags described in
   * {@link TJ}
   *
   * @return a <code>BufferedImage</code> instance containing the
   * decompressed image
   */
  public BufferedImage decompress(int desiredWidth, int desiredHeight,
    int bufferedImageType, int flags) throws Exception {
    if(desiredWidth < 0 || desiredHeight < 0 || flags < 0)
      throw new Exception("Invalid argument in decompress()");
    int scaledWidth = getScaledWidth(desiredWidth, desiredHeight);
    int scaledHeight = getScaledHeight(desiredWidth, desiredHeight);
    BufferedImage img = new BufferedImage(scaledWidth, scaledHeight,
      bufferedImageType);
    decompress(img, flags);
    return img;
  }

  /**
   * Free the native structures associated with this decompressor instance.
   */
  public void close() throws Exception {
    destroy();
  }

  protected void finalize() throws Throwable {
    try {
      close();
    }
    catch(Exception e) {}
    finally {
      super.finalize();
    }
  };

  private native void init() throws Exception;

  private native void destroy() throws Exception;

  private native void decompressHeader(byte[] srcBuf, int size)
    throws Exception;

  private native void decompress(byte[] srcBuf, int size, byte[] dstBuf,
    int desiredWidth, int pitch, int desiredHeight, int pixelFormat, int flags)
    throws Exception;

  private native void decompress(byte[] srcBuf, int size, int[] dstBuf,
    int desiredWidth, int pitch, int desiredHeight, int pixelFormat, int flags)
    throws Exception;

  private native void decompressToYUV(byte[] srcBuf, int size, byte[] dstBuf,
    int flags)
    throws Exception;

  static {
    System.loadLibrary("turbojpeg");
  }

  protected long handle = 0;
  protected byte[] jpegBuf = null;
  protected int jpegBufSize = 0;
  protected int jpegWidth = 0;
  protected int jpegHeight = 0;
  protected int jpegSubsamp = -1;
};
