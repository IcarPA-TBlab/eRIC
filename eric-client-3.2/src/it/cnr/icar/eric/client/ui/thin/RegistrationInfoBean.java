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
package it.cnr.icar.eric.client.ui.thin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.xml.registry.JAXRException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.infomodel.User;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.PostalAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import it.cnr.icar.eric.client.ui.thin.security.SecurityUtil;
import it.cnr.icar.eric.client.xml.registry.BusinessLifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.CertificateUtil;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.client.xml.registry.util.UserRegistrationInfo;
import it.cnr.icar.eric.common.security.X509Parser;

import sun.security.x509.X500Name;

/**
 * This bean acts as the backing bean for the Registration Wizard. It requires
 * RegistryObjectCollectionBean to be prepared for user registration by calling
 * doRegister() in that bean.
 *
 * The Registration Wizzard has 4 steps:
 * - 1. Info
 * - 2. User Details
 * - 3. Auth Details and actual registration
 * - 4. Sucess message, download key (if generated), links to browser key import.
 *
 * Current step is accessible through 'currentStep' field, 0 (zero) being a
 * special step meaning wizard is not 'active'.
 *
 * @author  dhilder
 * @author  Diego Ballve / Digital Artefacts Europe
 */
public class RegistrationInfoBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8827586058880525180L;

	private static final Log log = LogFactory.getLog(RegistrationInfoBean.class);

    // properties initialized by doClear - START
    private transient String alias;
    private transient String password;
    private transient String passwordRepeat;
    private transient X500Bean x500Bean;
    private transient X509Certificate x509Cert;
    private transient String generatePrivateKey;
    private transient FileUploadBean fileUploadBean;
    private transient KeyStore keystore;
    private transient int currentStep = 0;
    // properties initialized by doClear - END

    public RegistrationInfoBean() {
        // reset fields
        doClear();
    }

    @SuppressWarnings("unchecked")
	public static RegistrationInfoBean getInstance() {
        RegistrationInfoBean riBean = 
            (RegistrationInfoBean)FacesContext.getCurrentInstance()
                                                      .getExternalContext()
                                                      .getSessionMap()
                                                      .get("registrationInfo");
        if (riBean == null) {
            riBean = new RegistrationInfoBean();
            FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .put("registrationInfo", riBean);
        }
        return riBean;
    }
        
    public String getRequiredFieldFlag() {
        String requiredField = "";
        if (currentStep == 2) {
            requiredField = "*";
        }
        return requiredField;
    }
    
    public boolean isFieldRequired() {
        boolean fieldRequired = false;
        if (currentStep == 2) {
            fieldRequired = true;
        }
        return fieldRequired;
    }
    
    /**
     * Getter for property alias.
     * @return Value of property alias.
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Setter for property alias.
     * @param alias New value of property alias.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Getter for property password.
     * @return Value of property password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Setter for property password.
     * @param password New value of property password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Getter for property passwordRepeat.
     * @return Value of property passwordRepeat.
     */
    public String getPasswordRepeat() {
        return this.passwordRepeat;
    }

    /**
     * Setter for property passwordRepeat.
     * @param passwordRepeat New value of property passwordRepeat.
     */
    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    /**
     * Getter for property generatePrivateKey.
     * @return Value of property generatePrivateKey.
     */
    public String getGeneratePrivateKey() {
        if (this.generatePrivateKey == null) {
            this.generatePrivateKey = "true";
        }
        return this.generatePrivateKey;
    }

    public boolean generatePrivateKey() {
        return "true".equals(getGeneratePrivateKey());
    }

    /**
     * Setter for property generatePrivateKey.
     * @param generatePrivateKey New value of property generatePrivateKey.
     */
    public void setGeneratePrivateKey(String generatePrivateKey) {
        this.generatePrivateKey = generatePrivateKey;
    }

    /**
     * Getter for property x500Bean.
     * @return Value of property x500Bean.
     */
    public X500Bean getX500Bean() {
        if (x500Bean == null) {
            x500Bean = new X500Bean();
        }
        return this.x500Bean;
    }

    /**
     * Getter for property keystore.
     * @return Value of property keystore.
     */
    public KeyStore getKeystore() {
        return this.keystore;
    }


    /**
     * Getter for property fileUploadBean.
     * @return Value of property fileUploadBean.
     */
    public FileUploadBean getFileUploadBean()   {
        //TODO: Clean this workaround to initialize bean
        //why faces managed property did not set this?!
        if (this.fileUploadBean == null) {
            this.fileUploadBean = FileUploadBean.getInstance();
        }

        return this.fileUploadBean;
    }

    /**
     * Setter for property fileUploadBean.
     * @param fileUploadBean New value of property fileUploadBean.
     */
    public void setFileUploadBean(it.cnr.icar.eric.client.ui.thin.FileUploadBean fileUploadBean)   {

        this.fileUploadBean = fileUploadBean;
    }

   /**
    * Event method for setting flag for generatePrivateKey.
    *
    * @param ValueChangeEvent
    */
    public void changeGeneratePrivateKey(ValueChangeEvent event) {
        if (null != event.getNewValue()){
            String value = (String)event.getNewValue();
            if (value.equalsIgnoreCase("true")){
                setGeneratePrivateKey("true");
            } else {
                setGeneratePrivateKey("false");
            }
        }
    }

    /**
     * Getter for property currentStep.
     * @return Value of property currentStep.
     */
    public int getCurrentStep() {
        return this.currentStep;
    }

    /**
     *
     */
    public void doNext() {
        if (currentStep < 4) {
            currentStep++;
        } else if (currentStep == 4) {
            //reset wizard
            currentStep = 0;
        }
    }

    /**
     *
     */
    public void doPrev() {
        // no going back from step 4
        if (currentStep > 1 && currentStep < 4) {
            currentStep--;
        }
    }

    /**
     * Clear all the properties in this bean.
     */
    public String doClear() {
	log.trace("doClear started");

        doClearUser();
        doClearAuth();

        currentStep = 0;
        return "clear";
    }

    /**
     * Clear all user properties in this bean.
     */
    public String doClearUser() {
	log.trace("doClearUser started");

        RegistryObjectCollectionBean.getInstance().doRegister();

        return "clear";
    }

    /**
     * Clear all auth properties in this bean.
     */
    public String doClearAuth() {
	log.trace("doClearAuth started");

        getFileUploadBean().doClear();
        alias = null;
        password = null;
        passwordRepeat = null;
        keystore = null;
        getX500Bean().clear();
        x509Cert = null;
        generatePrivateKey = "true";
        return "clear";
    }


    public String doCheckUserDetails() {
	log.trace("doCheckUserDetails started");
        boolean valid = true;
        FacesContext context = FacesContext.getCurrentInstance();
        UserPreferencesBean userPreferenceBean = (UserPreferencesBean)context.getExternalContext().getSessionMap().get("userPreferencesBean");

        User user = (User)RegistryObjectCollectionBean.getInstance()
            .getCurrentRegistryObjectBean().getRegistryObject();

        try {
            if (user.getPersonName().getFirstName() == null ||
                "".equals(user.getPersonName().getFirstName().trim())) {
                String msg = WebUIResourceBundle.getInstance().getString("requiredFieldMissing");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                valid = false;
            } else if (user.getPersonName().getLastName() == null ||
                "".equals(user.getPersonName().getLastName().trim())) {
                String msg = WebUIResourceBundle.getInstance().getString("requiredFieldMissing");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                valid = false;
            } else if (user.getPostalAddresses() == null || user.getPostalAddresses().size() == 0){
                String msg = WebUIResourceBundle.getInstance().getString("requiredFieldMissing");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                valid = false;
            } else {
                PostalAddress address = (PostalAddress)user.getPostalAddresses().iterator().next();
                if (address.getCity() == null || "".equals(address.getCity().trim()) ||
                    (ProviderProperties.getInstance().getProperty("noStateOrProvince").indexOf(userPreferenceBean.getContentLocale().getLanguage()) == -1 && 
                    (address.getStateOrProvince() == null || "".equals(address.getStateOrProvince().trim()))) ||
                    address.getCountry() == null || "".equals(address.getCountry().trim())) {
                    String msg = WebUIResourceBundle.getInstance().getString("requiredFieldMissing");
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                    valid = false;
                } else if (address.getCountry().trim().length() !=2) {
                    String msg = WebUIResourceBundle.getInstance().getString("countryDefinedByTwoChars");
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                    valid = false;
                }
            }
        } catch (JAXRException e) {
            OutputExceptions.error(log,
				   WebUIResourceBundle.getInstance().
				   getString("message.ExceptionOccured"),
				   e);
            valid = false;
        }

        if (valid) {
	    try {
		getX500Bean().setName(user.getPersonName().getFullName());
		if (user.getPostalAddresses() != null && user.getPostalAddresses().size() > 0) {
		    PostalAddress address = (PostalAddress)user.getPostalAddresses().iterator().next();
		    getX500Bean().setCity(address.getCity());
		    getX500Bean().setStateOrProvince(address.getStateOrProvince());
		    getX500Bean().setCountry(address.getCountry());
		}
	    } catch (JAXRException e) {
		OutputExceptions.error(log,
				       WebUIResourceBundle.getInstance().
				       getString("message.ExceptionOccured"),
				       e);
		valid = false;
	    }
	}

        if (valid) {
	    doNext();
	    return "ok";
	} else {
            return "error";
        }
    }

    public boolean doCheckAuthDetails() {
	log.trace("doCheckAuthDetails started");
        boolean valid = true;
        FacesContext context = FacesContext.getCurrentInstance();
        UserPreferencesBean userPreferenceBean = (UserPreferencesBean)context.getExternalContext().getSessionMap().get("userPreferencesBean");

        if (generatePrivateKey()) {
	    /*
	    ** ??? Following code enforces neither password length
	    ** ??? restrictions nor alias uniqueness requirements, caught only
	    ** ??? when certificate generation attempted
	    */
            // verify fields, generate keystore
            if (alias == null || "".equals(alias) ||
                password == null || "".equals(password) ||
                passwordRepeat == null || "".equals(passwordRepeat) ||
                getX500Bean().getName() == null || "".equals(getX500Bean().getName()) ||
                getX500Bean().getUnit() == null || "".equals(getX500Bean().getUnit()) ||
                getX500Bean().getOrganization() == null || "".equals(getX500Bean().getOrganization()) ||
                getX500Bean().getCity() == null || "".equals(getX500Bean().getCity()) ||
                (ProviderProperties.getInstance().getProperty("noStateOrProvince").indexOf(userPreferenceBean.getContentLocale().getLanguage()) == -1  && 
                (getX500Bean().getStateOrProvince() == null || "".equals(getX500Bean().getStateOrProvince()))) ||
                getX500Bean().getCountry() == null || "".equals(getX500Bean().getCountry())) {
                String msg = WebUIResourceBundle.getInstance().getString("requiredFieldMissing");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                valid = false;
            } else if (password != null && passwordRepeat != null && !passwordRepeat.equals(password)) {
                String msg = WebUIResourceBundle.getInstance().getString("passwordsDoNotMatch");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                valid = false;
            } else if (password.length() < 6) {
                String msg = WebUIResourceBundle.getInstance().getString("passwordLengthInvalid");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                valid = false;
            }

            if (valid) {
                // Creates a new private key with a self signed certificate, in PKCS12 KeyStore
                @SuppressWarnings("unused")
				X500Name x500Name = getX500Bean().toX500Name();
                keystore = createPKCS12KeyStore(getX500Bean(), alias, password.toCharArray());
                if (keystore == null) {
		    // createPKCS12KeyStore() has already swallowed (and
		    // displayed) any useful error information.
                    valid = false;
                    x509Cert = null;
                } else {
                    try {
                        x509Cert = (X509Certificate)keystore.getCertificate(alias);
                    } catch (KeyStoreException e) {
                        // should never happen
			OutputExceptions.error(log,
					       WebUIResourceBundle.
					       getInstance().
			getString("message.ExceptionOnKeystoregetCertificate"),
					       e);
			valid = false;
                    }
                }
            } else {
                keystore = null;
                x509Cert = null;
            }

        } else {
            // accept uploaded x509 cert
            if (valid && getFileUploadBean().getFile() == null) {
                String msg = WebUIResourceBundle.getInstance().getString("requiredFieldMissing");
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                valid = false;
            }

            if (valid) {
		try {
		    FileInputStream inStream = new FileInputStream(getFileUploadBean().getFile());
		    x509Cert = X509Parser.parseX509Certificate(inStream);
		} catch (Exception e) {
		    String msg = WebUIResourceBundle.getInstance().
			getString("x509DERParseFail");
		    context.addMessage(null, new FacesMessage(FacesMessage.
							      SEVERITY_ERROR,
							      msg, null));
		    log.error(msg);
		    msg = WebUIResourceBundle.getInstance().
			getString("verifyCert");
		    OutputExceptions.error(log, msg, e);
		    valid = false;
		}
	    }

            keystore = null;
        }

        return valid;
    }

    /**
     * Cancel the wizard.
     */
    public String doCancel() {
	log.trace("doCancel started");
        FacesContext context = FacesContext.getCurrentInstance();

        doClear();

        // add success message
        String msg = WebUIResourceBundle.getInstance().getString("registrationCancelled");
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));

        // reset current step
        currentStep = 0;
        return "clear";
    }


    /*
    ** ??? Unsucessful registration which encounters an exception in
    ** ??? saveObjects() corrupts the JSP context.  Numerous errors occur.
    ** ??? To reproduce, make the database connection read only and hit
    ** ??? 'Previous' button after errors that result at step 3.  clear
    ** ??? credentials attempt in finally clause may not be working completely
    */
    public String doRegister() {
	log.trace("doRegister started");
        String result = "unknown";
        FacesContext context = FacesContext.getCurrentInstance();

        if (!doCheckAuthDetails()) {
            return result;
        }

        try {
            User user = (User)RegistryObjectCollectionBean.getInstance()
                                        .getCurrentRegistryObjectBean()
                                        .getRegistryObject();
            Collection<?> emails = user.getEmailAddresses();
            ArrayList<EmailAddress> newList = new ArrayList<EmailAddress>();

            Iterator<?> iters = emails.iterator();
            while (iters.hasNext()) {
                EmailAddress email = (EmailAddress) iters.next();
                if (null != email.getAddress() && email.getAddress().length() > 0) {
                    newList.add(email);
                }
            }
        user.setEmailAddresses(newList);
            
            List<User> users = new ArrayList<User>();
            users.add(user);
            BusinessLifeCycleManagerImpl blcm = RegistryBrowser.getBLCM();
            handleAuth();
	    log.trace("calling saveObjects()");
            BulkResponse resp = blcm.saveObjects(users);
	    log.trace("saveObjects() returned");
            if ((resp != null) &&
                (resp.getStatus() != JAXRResponse.STATUS_SUCCESS))
            {
                Collection<?> exceptions = resp.getExceptions();
                if (exceptions != null && !exceptions.isEmpty()) {
		    String msg = WebUIResourceBundle.getInstance().
			    getString("errorRegistrationNeeded");
		    context.addMessage(null, new FacesMessage(FacesMessage.
							      SEVERITY_ERROR,
							      msg, null));
		    log.error(msg);

                    Iterator<?> iter = exceptions.iterator();
                    while (iter.hasNext()) {
                        Exception e = (Exception)iter.next();
			OutputExceptions.error(log, e);
                    }
		} else {
		    String msg = WebUIResourceBundle.getInstance().
			getString("registrationFailed");
		    context.addMessage(null, new FacesMessage(FacesMessage.
							      SEVERITY_ERROR,
							      msg, null));
		    log.error(WebUIResourceBundle.getInstance().
			      getString("message.RegistrationFailed"));
                }
		result = "error";
	    } else {
		// ??? Is (null == resp) really success?
		result = "registered";
		doNext();
	    }
        } catch (Throwable t) {
	    OutputExceptions.error(log,
				   WebUIResourceBundle.getInstance().
				   getString("message.RegistrationFailed"),
				   WebUIResourceBundle.getInstance().
				   getString("registrationFailed"),
				   t);
            result = "error";
        } finally {
            try {
                RegistryBrowser.getInstance().clearCredentials();
            } catch (Throwable t) {
		// User doesn't need to hear about a cleanup problem
                OutputExceptions.logWarning(log,
					    WebUIResourceBundle.getInstance().
				 getString("message.couldNotClearCredentials"),
					    t);
            }
        }


        return result;
    }

    /**
     * Handle authentication, according to provided authentication details.
     */
    private void handleAuth() throws Exception {
        // if user provided a certificate file, use it, otherwise use old code
        if (x509Cert == null) {
	    // ??? By this point in process, when will this case be true?
            log.trace("Creating user credentials from RegistryBrowser Principal");
            // Should be done somewhere else already?
        } else {
            log.trace("Creating user credentials uploaded file");
            SecurityUtil.getInstance().handleCredentials(
                    x509Cert, RegistryBrowser.getConnection());
        }
    }

    private KeyStore createPKCS12KeyStore(X500Bean x500Bean, String alias, char[] keyPassword) {
        KeyStore ks = null;
        try {
            User user = (User)RegistryObjectCollectionBean.getInstance()
                .getCurrentRegistryObjectBean().getRegistryObject();
            char[] storePassword = ProviderProperties.getInstance().
		getProperty("jaxr-ebxml.security.storepass").toCharArray();
            UserRegistrationInfo userRegInfo = new UserRegistrationInfo(user);

            userRegInfo.setCAIssuedCert(true); // ??? Registry-generated
            userRegInfo.setAlias(alias);
            userRegInfo.setKeyPassword(keyPassword);
            userRegInfo.setStorePassword(storePassword);
            userRegInfo.setP12File(System.getProperty("java.io.tmpdir") + "/." + alias + ".p12");
            userRegInfo.setOrganization(x500Bean.getOrganization());
            userRegInfo.setOrganizationUnit(x500Bean.getUnit());            
            CertificateUtil.generateRegistryIssuedCertificate(userRegInfo);

            //Remove certificate from client keystore (which is on server in this case)
            //since private key should not be held anywhere other than client machine.
            try {
                CertificateUtil.removeCertificate(alias, storePassword);
            } catch (Exception e) {
		// User doesn't need to hear about a cleanup problem
		OutputExceptions.logWarning(log, e);
            }

            File p12File = null;
            try {
                p12File = new File(userRegInfo.getP12File());
                if (p12File.exists()) {
                    InputStream is = new FileInputStream(p12File);
                    ks = KeyStore.getInstance("PKCS12");
                    ks.load(is, keyPassword);
                    is.close();
                } else {
                    throw new JAXRException(WebUIResourceBundle.getInstance().
					    getString("message.FailedToCreatePKCS12Keystore"));
                }
            } finally {
                try {
                    if (p12File != null) {
                        p12File.delete();
                    }
                } catch (Exception e) {
		    // User doesn't need to hear about a cleanup problem
		    OutputExceptions.logWarning(log, e);
                }
            }
            return ks;
        } catch (Exception e) {
            OutputExceptions.error(log,
				   WebUIResourceBundle.getInstance().
			     getString("message.FailedToCreatePKCS12Keystore"),
				   e);
            return null;
        }
    }
}
