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
package it.cnr.icar.eric.common;

import java.security.SecureRandom;

import java.util.regex.Pattern;

/**
 * A <code>UUIDFactory</code> generates a UUID
 */
public class UUIDFactory {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private UUIDFactory _uuidFactory; */
    private static UUIDFactory instance = null;

	/** Regex pattern for a UUID */
	private static final Pattern uuidPattern =
		Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    /**
     * random number generator for UUID generation
     */
    private final SecureRandom secRand = new SecureRandom();

    /**
     *         128-bit buffer for use with secRand
     */
    private final byte[] secRandBuf16 = new byte[16];

    /**
     * 64-bit buffer for use with secRand
     */
    @SuppressWarnings("unused")
	private final byte[] secRandBuf8 = new byte[8];

    protected UUIDFactory() {
    }

    public UUID newUUID() {
        secRand.nextBytes(secRandBuf16);
        secRandBuf16[6] &= 0x0f;
        secRandBuf16[6] |= 0x40; /* version 4 */
        secRandBuf16[8] &= 0x3f;
        secRandBuf16[8] |= 0x80; /* IETF variant */
        secRandBuf16[10] |= 0x80; /* multicast bit */

        long mostSig = 0;

        for (int i = 0; i < 8; i++) {
            mostSig = (mostSig << 8) | (secRandBuf16[i] & 0xff);
        }

        long leastSig = 0;

        for (int i = 8; i < 16; i++) {
            leastSig = (leastSig << 8) | (secRandBuf16[i] & 0xff);
        }

        return new UUID(mostSig, leastSig);
    }

    /**
	 * Returns true if and only if uuid matches the pattern for valid UUIDs.
	 *
	 * @param uuid the <code>String</code> to check.
	 * @return <code>true</code> if and only if the specified.
	 * <code>String</code> matches the pattern; <code>false</code> otherwise.
	 */
	public boolean isValidUUID(String uuid) {
		return uuidPattern.matcher(uuid).matches();
    }

    /**
	 * Returns true if and only if uuidURN matches the pattern for valid UUID URNs.
	 *
	 * @param uuidURN the <code>String</code> to check.
	 * @return <code>true</code> if and only if the specified.
	 * <code>String</code> matches the pattern; <code>false</code> otherwise.
	 */
	public boolean isValidUUIDURN(String uuidURN) {
		return uuidURN.startsWith("urn:uuid:") && isValidUUID(uuidURN.substring(9));
    }

    private static void printUsage() {
        System.err.println(
            "...UUIDFactory [help] cnt=<number of uuids required>");
        System.exit(-1);
    }

    public static void main(String[] args) {
        int cnt = 1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("help")) {
                printUsage();
            } else if (args[i].startsWith("cnt=")) {
                cnt = Integer.parseInt(args[i].substring(4, args[i].length()));
            } else {
                System.err.println("Unknown parameter: '" + args[i] +
                    "' at position " + i);

                if (i > 0) {
                    System.err.println("Last valid parameter was '" +
                        args[i - 1] + "'");
                }

                printUsage();
            }
        }

        UUIDFactory uf = UUIDFactory.getInstance();

        for (int i = 0; i < cnt; i++) {
            UUID id = uf.newUUID();
            System.out.println("new UUID : " + id.toString());
        }
    }

    public synchronized static UUIDFactory getInstance() {
        if (instance == null) {
            instance = new UUIDFactory();
        }

        return instance;
    }
}
