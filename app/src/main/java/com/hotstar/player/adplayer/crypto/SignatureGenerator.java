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
import com.adobe.adobepass.accessenabler.api.AccessEnablerException;
import com.hotstar.player.adplayer.AdVideoApplication;

import javax.crypto.Cipher;
import java.security.Signature;

public class SignatureGenerator
{
    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME
            + SignatureGenerator.class.getSimpleName();

    protected IKeyInfo mSignatureKey = null;

    public SignatureGenerator (SigningCredential inCreds) {
        mSignatureKey = inCreds;
    }

    public String generateSignature(String inData) throws AccessEnablerException {
        try {
            Signature rsaSigner = Signature.getInstance(CryptoHelper.getSignatureAlgorithm());

            rsaSigner.initSign(mSignatureKey.getPrivateKey());

            rsaSigner.update(inData.getBytes());
            byte[] signature = rsaSigner.sign();

            return CryptoHelper.base64Encode(signature);
        }
        catch(Exception e) {
            AdVideoApplication.logger.e(LOG_TAG, e.toString());
            throw new AccessEnablerException();
        }
    }

    public String decryptCiphertext(String inData) throws AccessEnablerException {
        try {
            byte[] encryptedBytes = Base64.decode(inData, Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, mSignatureKey.getPrivateKey());

            int blockSize = 256; // 2048 key
            byte[] decryptedText = new byte[0];

            //if the input fits in one buffer the process it in one step
            if(encryptedBytes.length <= blockSize) {
                decryptedText = cipher.doFinal(encryptedBytes);
            } else {
                byte[] scrambled;
                byte[] buffer = new byte[blockSize];

                for(int i=0; i< encryptedBytes.length; i++) {
                    if( (i > 0) && (i%blockSize == 0)){
                        scrambled = cipher.doFinal(buffer);
                        decryptedText = append(decryptedText, scrambled);

                        //calculate the next block size
                        int newBlockSize = blockSize;
                        if(i + blockSize > encryptedBytes.length) {
                            newBlockSize = encryptedBytes.length - i;
                        }
                        //clean buffer
                        buffer = new byte[newBlockSize];
                    }
                    buffer[i%blockSize] = encryptedBytes[i];
                }
                scrambled = cipher.doFinal(buffer);
                decryptedText = append(decryptedText, scrambled);
            }

            return new String(decryptedText, "UTF-8");
        } catch (Exception e) {
            AdVideoApplication.logger.e(LOG_TAG, e.toString());
            throw new AccessEnablerException();
        }
    }

    private byte[] append(byte[] someBytes, byte[] moreBytes) {
        byte[] bytes = new byte[someBytes.length+moreBytes.length];
        System.arraycopy(someBytes, 0, bytes, 0, someBytes.length);
        System.arraycopy(moreBytes, 0, bytes, someBytes.length, moreBytes.length);
        return bytes;
    }

}
