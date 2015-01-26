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
package it.cnr.icar.eric.client.admin.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import it.cnr.icar.eric.client.admin.AbstractAdminFunction;
import it.cnr.icar.eric.client.admin.AdminException;
import it.cnr.icar.eric.client.admin.AdminFunctionContext;
import it.cnr.icar.eric.client.common.userModel.EmailAddressModel;
import it.cnr.icar.eric.client.common.userModel.KeyModel;
import it.cnr.icar.eric.client.common.userModel.PersonNameModel;
import it.cnr.icar.eric.client.common.userModel.PostalAddressModel;
import it.cnr.icar.eric.client.common.userModel.TelephoneNumberModel;
import it.cnr.icar.eric.client.common.userModel.UserModel;
import it.cnr.icar.eric.client.xml.registry.BusinessLifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.ConnectionImpl;
import it.cnr.icar.eric.client.xml.registry.RegistryServiceImpl;
import it.cnr.icar.eric.client.xml.registry.util.CertificateUtil;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.client.xml.registry.util.SecurityUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;


public class AddUser extends AbstractAdminFunction {
    
    private static final Log log = LogFactory.getLog(AddUser.class);
    
    boolean useEditor = false;
    static final String FIRST_NAME = "firstName";
    static final String LAST_NAME = "lastName";
    static final String MIDDLE_NAME = "middleName";
    static final String ALIAS = "alias";
    static final String KEYPASS = "keypass";
    static final String USER_TYPE = "userType";
    static final String ORGANIZATION = "organization";
    static final String ORGANIZATIONUNIT = "organizationunit";
    static final String POST1_CITY = "post1.city";
    static final String POST1_COUNTRY = "post1.country";
    static final String POST1_POSTALCODE = "post1.postalcode";
    static final String POST1_STATE_OR_PROVINCE = "post1.stateOrProvince";
    static final String POST1_STREET = "post1.street";
    static final String POST1_STREETNUM = "post1.streetNumber";
    static final String POST1_TYPE = "post1.type";
    static final String POST2_CITY = "post2.city";
    static final String POST2_COUNTRY = "post2.country";
    static final String POST2_POSTALCODE = "post2.postalcode";
    static final String POST2_STATE_OR_PROVINCE = "post2.stateOrProvince";
    static final String POST2_STREET = "post2.street";
    static final String POST2_STREETNUM = "post2.streetNumber";
    static final String POST2_TYPE = "post2.type";
    static final String POST3_CITY = "post3.city";
    static final String POST3_COUNTRY = "post3.country";
    static final String POST3_POSTALCODE = "post3.postalcode";
    static final String POST3_STATE_OR_PROVINCE = "post3.stateOrProvince";
    static final String POST3_STREET = "post3.street";
    static final String POST3_STREETNUM = "post3.streetNumber";
    static final String POST3_TYPE = "post3.type";
    static final String PHONE1_AREA_CODE = "telephone1.areaCode";
    static final String PHONE1_COUNTRY_CODE = "telephone1.countryCode";
    static final String PHONE1_EXTENSION = "telephone1.extension";
    static final String PHONE1_NUMBER = "telephone1.number";
    static final String PHONE1_TYPE = "telephone1.type";
    static final String PHONE1_URL = "telephone1.URL";
    static final String PHONE2_AREA_CODE = "telephone2.areaCode";
    static final String PHONE2_COUNTRY_CODE = "telephone2.countryCode";
    static final String PHONE2_EXTENSION = "telephone2.extension";
    static final String PHONE2_NUMBER = "telephone2.number";
    static final String PHONE2_TYPE = "telephone2.type";
    static final String PHONE2_URL = "telephone2.URL";
    static final String PHONE3_AREA_CODE = "telephone3.areaCode";
    static final String PHONE3_COUNTRY_CODE = "telephone3.countryCode";
    static final String PHONE3_EXTENSION = "telephone3.extension";
    static final String PHONE3_NUMBER = "telephone3.number";
    static final String PHONE3_TYPE = "telephone3.type";
    static final String PHONE3_URL = "telephone3.URL";
    static final String EMAIL1_ADDRESS = "email1.address";
    static final String EMAIL1_TYPE = "email1.type";
    static final String EMAIL2_ADDRESS = "email2.address";
    static final String EMAIL2_TYPE = "email2.type";
    static final String EMAIL3_ADDRESS = "email3.address";
    static final String EMAIL3_TYPE = "email3.type";
    static Object[][] propsData = {
            { FIRST_NAME, new String[] { "-fn" } },
            { LAST_NAME, new String[] { "-ln" } },
            { MIDDLE_NAME, new String[] { "-mn" } },
            { ALIAS, null },
            { KEYPASS, null },
            { USER_TYPE, new String[] { "-type" } },
            {ORGANIZATION, new String[] {"-organisation", "-org"}},
            {ORGANIZATIONUNIT, new String[] {"-organisationunit", "-orgunit"}},
            {POST1_TYPE, new String[] { "-postalType" } },
            { POST1_CITY, new String[] { "-city" } },
            { POST1_COUNTRY, new String[] { "-country" } },
            {
                POST1_POSTALCODE,
                new String[] { "-postalcode", "-postcode", "-zip" }
            },
            {
                POST1_STATE_OR_PROVINCE,
                new String[] { "-stateOrProvince", "-state", "-province" }
            },
            { POST1_STREET, new String[] { "-street" } },
            { POST1_STREETNUM, new String[] { "-streetNumber", "-number" } },
            { POST2_TYPE, null },
            { POST2_CITY, null },
            { POST2_COUNTRY, null },
            { POST2_POSTALCODE, null },
            { POST2_STATE_OR_PROVINCE, null },
            { POST2_STREET, null },
            { POST2_STREETNUM, null },
            { POST3_TYPE, null },
            { POST3_CITY, null },
            { POST3_COUNTRY, null },
            { POST3_POSTALCODE, null },
            { POST3_STATE_OR_PROVINCE, null },
            { POST3_STREET, null },
            { POST3_STREETNUM, null },
            { PHONE1_TYPE, new String[] { "-phoneType" } },
            { PHONE1_AREA_CODE, new String[] { "-areaCode" } },
            { PHONE1_COUNTRY_CODE, new String[] { "-countryCode" } },
            { PHONE1_EXTENSION, new String[] { "-extension" } },
            { PHONE1_NUMBER, new String[] { "-number" } },
            { PHONE1_URL, new String[] { "-URL" } },
            { PHONE2_TYPE, null },
            { PHONE2_AREA_CODE, null },
            { PHONE2_COUNTRY_CODE, null },
            { PHONE2_EXTENSION, null },
            { PHONE2_NUMBER, null },
            { PHONE2_URL, null },
            { PHONE3_TYPE, null },
            { PHONE3_AREA_CODE, null },
            { PHONE3_COUNTRY_CODE, null },
            { PHONE3_EXTENSION, null },
            { PHONE3_NUMBER, null },
            { PHONE3_URL, null },
            { EMAIL1_TYPE, new String[] { "-emailType" } },
            { EMAIL1_ADDRESS, new String[] { "-emailAddress", "-email" } },
            { EMAIL2_TYPE, null },
            { EMAIL2_ADDRESS, null },
            { EMAIL3_TYPE, null },
            { EMAIL3_ADDRESS, null }
        };
    static HashMap<String, Object> paramNames = new HashMap<String, Object>();
    
    HashMap<String, String> dontVersionSlotsMap = new HashMap<String, String>();

    static {
        for (int i = 0; i < propsData.length; i++) {
            paramNames.put("-" + ((String) propsData[i][0]).toLowerCase(),
                propsData[i][0]);

            if (propsData[i][1] != null) {
                for (int paramIdx = 0;
                        paramIdx < ((String[]) propsData[i][1]).length;
                        paramIdx++) {
                    paramNames.put(((String[]) propsData[i][1])[paramIdx].toLowerCase(),
                        propsData[i][0]);
                }
            }
        }
    }

    @SuppressWarnings("static-access")
	public AddUser() {
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");        
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");        
    }
    
    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        this.context = context;

        if (context.getDebug()) {
            Iterator<String> paramNameIter = paramNames.keySet().iterator();

            while (paramNameIter.hasNext()) {
                String paramName = paramNameIter.next();
                context.printMessage(paramName + "=" +
                    paramNames.get(paramName));
            }
        }

        Properties userProps = new Properties();

        if (args == null) {
            context.printMessage(format(rb,"argumentRequired"));

            return;
        }

        String[] tokens = args.split("\\s+");

        boolean parsedOkay = parseArgs(tokens, userProps);

        if (!parsedOkay) {
            return;
        }

        if (useEditor) {
            userProps = editProperties(userProps);
        }
        
        ConnectionImpl connection = ((RegistryServiceImpl)(context.getService().getLCM().getRegistryService())).getConnection();
        
        // Use Swing-client's model so can use model's validate() method.
        UserModel userModel = new UserModel(context.getService().getLCM()
                                                   .createUser());
        updateUserModel(userModel, userProps);

        userModel.validate();
        KeyModel userRegInfo = userModel.getUserRegistrationInfo();
        String alias = userRegInfo.getAlias();
        char[] keypass = userRegInfo.getKeyPassword();
        
        //Would have liked to have used RegistryFacade.registerUser() here to avoid code duplication but 
        //That would mean messing with existing admintool framework. Defer that for now.
        //Instead copy paste code for now from RegistryFacade.registerUser()
        try {
            //Do equivalent of RegistryFacade.logoff()
            connection.logoff();

                        
            userRegInfo.setAlias(alias);
            userRegInfo.setKeyPassword(keypass);
            char[] storepass = ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.storepass").toCharArray();
            if (!CertificateUtil.certificateExists(alias, storepass)) {
                CertificateUtil.generateRegistryIssuedCertificate(userRegInfo);
            }

            //Do equivalent of RegistryFacaqde.logon() using new cert first
            HashSet<X500PrivateCredential> creds = new HashSet<X500PrivateCredential>();
            @SuppressWarnings("unused")
			String str = keypass.toString();
            creds.add(SecurityUtil.getInstance().aliasToX500PrivateCredential(alias, new String(keypass)));
            connection.setCredentials(creds);            
            
            // Now save the User
            ArrayList<User> objects = new ArrayList<User>();
            objects.add(userRegInfo.getUser());
            ((BusinessLifeCycleManagerImpl)(context.getService().getLCM())).saveObjects(objects, dontVersionSlotsMap);
        } 
        catch (Exception e) {
            // Remove the self-signed certificate from the keystore, if one
            // was created during the self-registration process
            try {
                if (alias != null) {
                    CertificateUtil.removeCertificate(alias,
                        userRegInfo.getStorePassword());
                }
            } catch (Exception removeCertException) {
                log.warn(removeCertException);
            }

            if (e instanceof JAXRException) {
                throw (JAXRException)e;
            } else {
                throw new JAXRException(e);
            }
        }
        finally {
            File tmpFile = new File(userRegInfo.getP12File());
            if (tmpFile.exists()) {
                tmpFile.delete();
            }             
        }
    }

    boolean parseArgs(String[] tokens, Properties userProps)
        throws Exception {
        boolean parsedOkay = true;
        int tIndex = 0;

        for (tIndex = 0;
                ((tIndex < tokens.length) && (tokens[tIndex].charAt(0) == '-'));
                tIndex++) {
            String option = tokens[tIndex];

            if (context.getDebug()) {
                context.printMessage(format(rb, "debug.option",
					    new Object[] {option}));
            }

            if (collator.compare(option, "-edit") == 0) {
                useEditor = true;
            } else if (collator.compare(option, "-load") == 0) {
                if (++tIndex == tokens.length) {
                    context.printMessage(getUsage());
                    parsedOkay = false;

                    break;
                }

                String fileName = tokens[tIndex];
                userProps.load(new FileInputStream(fileName));
            } else if (paramNames.containsKey(option.toLowerCase())) {
                String propertyName = (String) paramNames.get(option.toLowerCase());

                if (++tIndex == tokens.length) {
                    context.printMessage(getUsage());
                    parsedOkay = false;

                    break;
                }

                userProps.setProperty(propertyName, tokens[tIndex]);
            } else {
                context.printMessage(format(rb,"invalidArgument",
					    new Object[] { option }));
                parsedOkay = false;

                break;
            }
        }

        return parsedOkay;
    }

    /**
     * Lets the tool user edit the properties using their favourite
     * editor.
     *
     * @param userProps current user properties
     * @return edited user properties
     * @exception Exception if an error occurs
     */
    Properties editProperties(Properties userProps) throws Exception {
        String editor = context.getEditor();

        // It should be sufficient to use userProps.store(), but
        // want to output only the known properties in a known order, so
        // do it all in the code.
        File tempFile = File.createTempFile("AddUser", "properties");

        // Property files are always ISO-8859-1, so write properties out
        // using ISO-8859-1 charset.
        Writer fileWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(tempFile), "ISO-8859-1"));

        for (int i = 0; i < propsData.length; i++) {
            String propertyName = (String) propsData[i][0];
            fileWriter.write(propertyName + "=" +
                userProps.getProperty(propertyName, "") + "\n");
        }

        fileWriter.close();

        if (context.getDebug()) {
            context.printMessage(format(rb, "debug.editor",
					new Object[] {editor, tempFile.
						      getAbsolutePath()}));
        }

        Runtime r = Runtime.getRuntime();
        String command = editor + " " + tempFile.getAbsolutePath();
        String[] s = command.split("\\s+");

        Process p = r.exec(s);

        int exitStatus = p.waitFor();

        if (exitStatus != 0) {
            throw new AdminException(format(rb, "editFailed"));
        }

        if (context.getDebug()) {
            BufferedReader fileReader = new BufferedReader(new FileReader(
                        tempFile));

            String line;

            while ((line = fileReader.readLine()) != null) {
                context.printMessage(line);
            }

            fileReader.close();
        }

        // Drop old userProps and read new set from tempFile.
        userProps = new Properties();
        userProps.load(new FileInputStream(tempFile));

        boolean tempFileIsDeleted = tempFile.delete();

        if (!tempFileIsDeleted) {
            context.printMessage(format(rb,"couldNotDelete",
					new Object[] { tempFile.getAbsolutePath() }));
        }

        return userProps;
    }

    void updateUserModel(UserModel userModel, Properties userProps)
        throws Exception {
        userModel.getUserRegistrationInfo().setAlias(userProps.getProperty(ALIAS, ""));
        userModel.getUserRegistrationInfo().setKeyPassword(userProps.getProperty(KEYPASS, "").toCharArray());
        userModel.getUserRegistrationInfo().setOrganization(userProps.getProperty(ORGANIZATION, ""));
        userModel.getUserRegistrationInfo().setOrganizationUnit(userProps.getProperty(ORGANIZATIONUNIT, ""));

        PersonNameModel personNameModel = userModel.getPersonNameModel();

        personNameModel.setFirstName(userProps.getProperty(FIRST_NAME, ""));
        personNameModel.setMiddleName(userProps.getProperty(MIDDLE_NAME, ""));
        personNameModel.setLastName(userProps.getProperty(LAST_NAME, ""));

        EmailAddressModel emailAddressModel = userModel.getEmailAddressModel();

        //TODO: Need to harmonize with spec change that now supports multiple email addresses.
        //Code below only sets one address apparently.
        if (userProps.getProperty(EMAIL1_ADDRESS) != null) {
            if (userProps.getProperty(EMAIL1_TYPE) != null) {
                emailAddressModel.setKey(userProps.getProperty(EMAIL1_TYPE));
            }
            emailAddressModel.setAddress(userProps.getProperty(EMAIL1_ADDRESS,
                    ""));
        }

        if (userProps.getProperty(EMAIL2_ADDRESS) != null) {
            if (userProps.getProperty(EMAIL2_TYPE) != null) {
                emailAddressModel.setKey(userProps.getProperty(EMAIL2_TYPE));
            }
            emailAddressModel.setAddress(userProps.getProperty(EMAIL2_ADDRESS,
                    ""));
        }

        if (userProps.getProperty(EMAIL3_ADDRESS) != null) {
            if (userProps.getProperty(EMAIL3_TYPE) != null) {
                emailAddressModel.setKey(userProps.getProperty(EMAIL3_TYPE));
            }
            emailAddressModel.setAddress(userProps.getProperty(EMAIL3_ADDRESS,
                    ""));
        }

        PostalAddressModel postalAddressModel = userModel.getPostalAddressModel();

        if (userProps.getProperty(POST1_TYPE) != null) {
            postalAddressModel.setKey(userProps.getProperty(POST1_TYPE));
            postalAddressModel.setCity(userProps.getProperty(POST1_CITY, ""));
            postalAddressModel.setState(userProps.getProperty(
                    POST1_STATE_OR_PROVINCE, ""));
            postalAddressModel.setPostalCode(userProps.getProperty(
                    POST1_POSTALCODE, ""));
            postalAddressModel.setCountry(userProps.getProperty(POST1_COUNTRY,
                    ""));
            postalAddressModel.setStreet(userProps.getProperty(POST1_COUNTRY, ""));
            postalAddressModel.setStreetNum(userProps.getProperty(
                    POST1_STREETNUM, ""));
        }

        if (userProps.getProperty(POST2_TYPE) != null) {
            postalAddressModel.setKey(userProps.getProperty(POST2_TYPE));
            postalAddressModel.setCity(userProps.getProperty(POST2_CITY, ""));
            postalAddressModel.setState(userProps.getProperty(
                    POST2_STATE_OR_PROVINCE, ""));
            postalAddressModel.setPostalCode(userProps.getProperty(
                    POST2_POSTALCODE, ""));
            postalAddressModel.setCountry(userProps.getProperty(POST2_COUNTRY,
                    ""));
            postalAddressModel.setStreet(userProps.getProperty(POST2_COUNTRY, ""));
            postalAddressModel.setStreetNum(userProps.getProperty(
                    POST2_STREETNUM, ""));
        }

        if (userProps.getProperty(POST3_TYPE) != null) {
            postalAddressModel.setKey(userProps.getProperty(POST3_TYPE));
            postalAddressModel.setCity(userProps.getProperty(POST3_CITY, ""));
            postalAddressModel.setState(userProps.getProperty(
                    POST3_STATE_OR_PROVINCE, ""));
            postalAddressModel.setPostalCode(userProps.getProperty(
                    POST3_POSTALCODE, ""));
            postalAddressModel.setCountry(userProps.getProperty(POST3_COUNTRY,
                    ""));
            postalAddressModel.setStreet(userProps.getProperty(POST3_COUNTRY, ""));
            postalAddressModel.setStreetNum(userProps.getProperty(
                    POST3_STREETNUM, ""));
        }

        TelephoneNumberModel telephoneNumberModel = userModel.getTelephoneNumberModel();

        if (userProps.getProperty(PHONE1_TYPE) != null) {
            telephoneNumberModel.setKey(userProps.getProperty(PHONE1_TYPE));
            telephoneNumberModel.setAreaCode(userProps.getProperty(
                    PHONE1_AREA_CODE, ""));
            telephoneNumberModel.setCountryCode(userProps.getProperty(
                    PHONE1_COUNTRY_CODE, ""));
            telephoneNumberModel.setExtension(userProps.getProperty(
                    PHONE1_EXTENSION, ""));
            telephoneNumberModel.setNumber(userProps.getProperty(
                    PHONE1_NUMBER, ""));
            telephoneNumberModel.setURL(userProps.getProperty(PHONE1_URL, ""));
        }

        if (userProps.getProperty(PHONE2_TYPE) != null) {
            telephoneNumberModel.setKey(userProps.getProperty(PHONE2_TYPE));
            telephoneNumberModel.setAreaCode(userProps.getProperty(
                    PHONE2_AREA_CODE, ""));
            telephoneNumberModel.setCountryCode(userProps.getProperty(
                    PHONE2_COUNTRY_CODE, ""));
            telephoneNumberModel.setExtension(userProps.getProperty(
                    PHONE2_EXTENSION, ""));
            telephoneNumberModel.setNumber(userProps.getProperty(
                    PHONE2_NUMBER, ""));
            telephoneNumberModel.setURL(userProps.getProperty(PHONE2_URL, ""));
        }

        if (userProps.getProperty(PHONE3_TYPE) != null) {
            telephoneNumberModel.setKey(userProps.getProperty(PHONE3_TYPE));
            telephoneNumberModel.setAreaCode(userProps.getProperty(
                    PHONE3_AREA_CODE, ""));
            telephoneNumberModel.setCountryCode(userProps.getProperty(
                    PHONE3_COUNTRY_CODE, ""));
            telephoneNumberModel.setExtension(userProps.getProperty(
                    PHONE3_EXTENSION, ""));
            telephoneNumberModel.setNumber(userProps.getProperty(
                    PHONE3_NUMBER, ""));
            telephoneNumberModel.setURL(userProps.getProperty(PHONE3_URL, ""));
        }
    }

    public String getUsage() {
        String usage = format(rb, "usage.addUser");

        for (int i = 0; i < propsData.length; i++) {
            String params = "";
            usage += "\n         ";

            if (propsData[i][1] == null) {
                params = ("-" + propsData[i][0]);
            } else {
                params = ("(-" + propsData[i][0]);

                for (int paramIdx = 0;
                        paramIdx < ((String[]) propsData[i][1]).length;
                        paramIdx++) {
                    params += (" | " + ((String[]) propsData[i][1])[paramIdx]);
                }

                params += ")";
            }
            usage += format(rb, "usage.addUsage", new Object[] {params});
        }
        usage += "\n";

        return usage;
    }
}
