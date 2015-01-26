/*
 * ====================================================================
 * This file is part of the ebXML Registry by Icar Cnr v3.2 
 * ("eRICv32" in the following disclaimer).
 *
 * "eRICv32" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * "eRICv32" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License Version 3
 * along with "eRICv32".  If not, see <http://www.gnu.org/licenses/>.
 *
 * eRICv32 is a forked, derivative work, based on:
 * 	- freebXML Registry, a royalty-free, open source implementation of the ebXML Registry standard,
 * 	  which was published under the "freebxml License, Version 1.1";
 *	- ebXML OMAR v3.2 Edition, published under the GNU GPL v3 by S. Krushe & P. Arwanitis.
 * 
 * All derivative software changes and additions are made under
 *
 * Copyright (C) 2013 Ing. Antonio Messina <messina@pa.icar.cnr.it>
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the freebxml Software Foundation.  For more
 * information on the freebxml Software Foundation, please see
 * "http://www.freebxml.org/".
 *
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * ====================================================================
 */

package it.cnr.icar.eric.common.security;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class KeyToolStripped {

    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog("it.cnr.icar.eric.common.KeyToolStripped");    
    
    public KeyToolStripped() {
    }
    
    public static void dumpProviderInfo(String msg) {
        Provider[] providers = Security.getProviders();
        System.err.println(msg);
        for (int i = 0; i < providers.length; i++) {

            Provider provider = providers[i];
            System.err.println("Provider name: " + provider.getName());
            //System.err.println("Provider class: " + provider.getClass().getName());
            //System.err.println("Provider information: " + provider.getInfo());
            //System.err.println("Provider version: " + provider.getVersion());
            Set<Entry<Object, Object>> entries = provider.entrySet();
            @SuppressWarnings("unused")
			Iterator<Entry<Object, Object>> iterator = entries.iterator();

            /*
            while (iterator.hasNext()) {
                System.err.println("    Property entry: " + iterator.next());
            }
             */
        }        
    }        

    /** 
      * Generate a public/private key pair.
      *
      * @throws Exception
      */    
    public static void generateKeyPair(KeyStore keyStore, char[] storePass, String alias, char[] keyPass, String dname, String keyAlg, int validity)
        throws Exception 
    {
        int keySize = 1024;
        if (keyStore.containsAlias(alias)) {
            MessageFormat messageformat = new MessageFormat(
                        "Key pair not generated, alias <alias> already exists");
            Object[] aobj = { alias };
            throw new Exception(messageformat.format(((Object) (aobj))));
        }

        String sigAlg = null;
        
        if (keyAlg.equalsIgnoreCase("DSA")) {
            sigAlg = "SHA1WithDSA";
        } else if (keyAlg.equalsIgnoreCase("RSA")) {
            sigAlg = "MD5WithRSA";
        } else {
            throw new Exception("Cannot derive signature algorithm");
        }

        //Must specify provider "SunRsaSign" otherwise it gets some weird NSS specific provider
        //when running in AppServer EE.
        CertAndKeyGen certandkeygen = new CertAndKeyGen(keyAlg, sigAlg);
        X500Name x500name;

        if (dname == null) {
            throw new Exception("Key pair not generated, dname is null.");
        } else {
            x500name = new X500Name(dname);
        }

        certandkeygen.generate(keySize);

        PrivateKey privatekey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509certificate = new X509Certificate[1];
        ax509certificate[0] = certandkeygen.getSelfCertificate(x500name,
                validity * 24 * 60 * 60);


        keyStore.setKeyEntry(alias, privatekey, keyPass, ax509certificate);
    }

}
