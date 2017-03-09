/*******************************************************************************
 * ADOBE CONFIDENTIAL
 *  ___________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe Systems Incorporated and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe Systems Incorporated and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe Systems Incorporated.
 ******************************************************************************/

package com.hotstar.player.adplayer.crypto;

import android.util.Base64;
import com.hotstar.player.adplayer.AdVideoApplication;

public class CryptoHelper
{
    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME
            + CryptoHelper.class.getSimpleName();

    public static String base64Encode(byte[] inData) {
        if (inData == null)
            return null;

        try {
            return new String(Base64.encode(inData, Base64.DEFAULT));
        }
        catch(Exception e) {
            AdVideoApplication.logger.e(LOG_TAG, e.getMessage());
        }

        return null;
    }

    public static byte[] base64Decode(String inData) {
        if (inData == null)
            return null;

        try {
            return Base64.decode(inData.getBytes(), Base64.DEFAULT);
        }
        catch(Exception e) {
            AdVideoApplication.logger.e(LOG_TAG, e.getMessage());
        }

        return null;

    }

    public static String getSignatureAlgorithm() {
        return "SHA256WithRSA";
    }
}
