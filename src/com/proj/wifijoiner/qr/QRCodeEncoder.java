/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.proj.wifijoiner.qr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.result.AddressBookParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.BitMatrix;
import com.proj.wifijoiner.R;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QRCodeEncoder {

  private static final String TAG = QRCodeEncoder.class.getSimpleName();

  private static final int WHITE = 0xFFFFFFFF;
  private static final int BLACK = 0xFF000000;

  private final Activity activity;
  private String contents;
  private String displayContents;
  private String title;
  private BarcodeFormat format;

  QRCodeEncoder(Activity activity, Intent intent) {
    this.activity = activity;
    if (intent == null) {
      throw new IllegalArgumentException("No valid data to encode.");
    }

    String action = intent.getAction();
    if (action.equals(Intents.Encode.ACTION)) {
      if (!encodeContentsFromZXingIntent(intent)) {
        throw new IllegalArgumentException("No valid data to encode.");
      }
    } else if (action.equals(Intent.ACTION_SEND)) {
      if (!encodeContentsFromShareIntent(intent)) {
        throw new IllegalArgumentException("No valid data to encode.");
      }
    }
  }

  public void requestBarcode(Handler handler, int pixelResolution) {
    Thread encodeThread = new EncodeThread(contents, handler, pixelResolution,
        format);
    encodeThread.start();
  }

  public String getContents() {
    return contents;
  }

  public String getDisplayContents() {
    return displayContents;
  }

  public String getTitle() {
    return title;
  }

  public String getFormat() {
    return format.toString();
  }

  // It would be nice if the string encoding lived in the core ZXing library,
  // but we use platform specific code like PhoneNumberUtils, so it can't.
  private boolean encodeContentsFromZXingIntent(Intent intent) {
     // Default to QR_CODE if no format given.
    String formatString = intent.getStringExtra(Intents.Encode.FORMAT);
    try {
      format = BarcodeFormat.valueOf(formatString);
    } catch (IllegalArgumentException iae) {
      // Ignore it then
      format = null;
    }
    if (format == null || BarcodeFormat.QR_CODE.equals(format)) {
      String type = intent.getStringExtra(Intents.Encode.TYPE);
      if (type == null || type.length() == 0) {
        return false;
      }
      this.format = BarcodeFormat.QR_CODE;
      encodeQRCodeContents(intent, type);
    } else {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    }
    return contents != null && contents.length() > 0;
  }

  // Handles send intents from the Contacts app, retrieving a contact as a VCARD.
  private boolean encodeContentsFromShareIntent(Intent intent) {
    format = BarcodeFormat.QR_CODE;
    try {
      Uri uri = (Uri)intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
      InputStream stream = activity.getContentResolver().openInputStream(uri);
      int length = stream.available();
      if (length <= 0) {
        Log.w(TAG, "Content stream is empty");
        return false;
      }
      byte[] vcard = new byte[length];
      int bytesRead = stream.read(vcard, 0, length);
      if (bytesRead < length) {
        Log.w(TAG, "Unable to fully read available bytes from content stream");
        return false;
      }
      String vcardString = new String(vcard, 0, bytesRead, "UTF-8");
      Log.d(TAG, "Encoding share intent content:");
      Log.d(TAG, vcardString);
      Result result = new Result(vcardString, vcard, null, BarcodeFormat.QR_CODE);
      ParsedResult parsedResult = ResultParser.parseResult(result);
      if (!(parsedResult instanceof AddressBookParsedResult)) {
        Log.d(TAG, "Result was not an address");
        return false;
      }      
    } catch (IOException e) {
      Log.w(TAG, e);
      return false;
    } catch (NullPointerException e) {
      Log.w(TAG, e);
      // In case the uri was not found in the Intent.
      return false;
    }
    return contents != null && contents.length() > 0;
  }

  private void encodeQRCodeContents(Intent intent, String type) {
    if (type.equals(Contents.Type.TEXT)) {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    }
  }

  
  static Bitmap encodeAsBitmap(String contents,
                               BarcodeFormat format,
                               int desiredWidth,
                               int desiredHeight) throws WriterException {
    Hashtable<EncodeHintType,Object> hints = null;
    String encoding = guessAppropriateEncoding(contents);
    if (encoding != null) {
      hints = new Hashtable<EncodeHintType,Object>(2);
      hints.put(EncodeHintType.CHARACTER_SET, encoding);
    }
    MultiFormatWriter writer = new MultiFormatWriter();    
    BitMatrix result = writer.encode(contents, format, desiredWidth, desiredHeight, hints);
    int width = result.getWidth();
    int height = result.getHeight();
    int[] pixels = new int[width * height];
    // All are 0, or black, by default
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
      }
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }

  private static String guessAppropriateEncoding(CharSequence contents) {
    // Very crude at the moment
    for (int i = 0; i < contents.length(); i++) {
      if (contents.charAt(i) > 0xFF) {
        return "UTF-8";
      }
    }
    return null;
  }
}
