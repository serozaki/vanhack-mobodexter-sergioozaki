package com.serozaki.mobodexter.raspberryagent.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

public class KeystoreUtil {
    private KeyStore keyStore;
    private String keyPassword;

    public KeystoreUtil(String certificateChainPem, String privateKeyPem) throws IOException {
        PrivateKey privateKey = loadPrivate(privateKeyPem, null);
        List<Certificate> certChain = loadCertificates(certificateChainPem);
        keyPassword = new BigInteger(128, new SecureRandom()).toString(32);
        keyStore = createKeyStore(privateKey, certChain, keyPassword);
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    private KeyStore createKeyStore(PrivateKey privateKey, List<Certificate> certificates, String keyPassword2)
            throws IOException {
        try {
            KeyStore newKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            newKeystore.load(null);
            Certificate[] certChain = new Certificate[certificates.size()];
            certChain = certificates.toArray(certChain);
            newKeystore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certChain);
            return newKeystore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new IOException(e);
        }

    }

    private PrivateKey loadPrivate(String privateKeyPem, String algorithm) throws IOException {
        try {
            return PrivateKeyReader.getPrivateKey(
                    new ByteArrayInputStream(privateKeyPem.getBytes(Charset.forName("UTF-8"))), algorithm);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Certificate> loadCertificates(String certificateChainPem) throws IOException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (List<Certificate>) certFactory.generateCertificates(
                    new ByteArrayInputStream(certificateChainPem.getBytes(Charset.forName("UTF-8"))));
        } catch (CertificateException e) {
            throw new IOException(e);
        }
    }

}
