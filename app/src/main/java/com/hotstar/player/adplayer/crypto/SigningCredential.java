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

import com.hotstar.player.adplayer.AdVideoApplication;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class SigningCredential implements ICertificateInfo, IKeyInfo
{
    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME
            + SigningCredential.class.getSimpleName();

    protected KeyStore.PrivateKeyEntry mKeyEntry = null;

    public SigningCredential (InputStream inPKCSFile, String inPassword) {
        mKeyEntry = extractPrivateKeyEntry(inPKCSFile, inPassword);
    }

    private KeyStore.PrivateKeyEntry extractPrivateKeyEntry(InputStream inPKCSFile, String inPassword) {
        if (inPKCSFile == null)
            return null;

        try {
            KeyStore ks  = KeyStore.getInstance(ICertificateInfo.KEYSTORE_PKCS12);
            AdVideoApplication.logger.d(LOG_TAG, "KS provider : " + ks.getProvider());

            ks.load(inPKCSFile, inPassword.toCharArray());

            String keyAlias = null;
            Enumeration<String> aliases = ks.aliases();
            while(aliases.hasMoreElements()) {
                keyAlias = aliases.nextElement();
                if (ks.isKeyEntry(keyAlias))
                    break;
            }

            if (keyAlias != null) {
                KeyStore.PrivateKeyEntry keyEntry =
                        (KeyStore.PrivateKeyEntry) ks.getEntry
                                (keyAlias, new KeyStore.PasswordProtection(inPassword.toCharArray()));

                return keyEntry;
            }
        }
        catch(Exception e) {
            AdVideoApplication.logger.e(LOG_TAG, e.getMessage());
        }


        return null;
    }

    public PrivateKey getPrivateKey() {
        if (mKeyEntry == null)
            return null;

        return mKeyEntry.getPrivateKey();
    }

    public Certificate getCertificate() {
        if (mKeyEntry == null)
            return null;

        return mKeyEntry.getCertificate();
    }

    public Certificate[] getCertificateChain() {
        if (mKeyEntry == null)
            return null;

        return mKeyEntry.getCertificateChain();

    }

    public boolean isValid() {
        return mKeyEntry != null;
    }

}
