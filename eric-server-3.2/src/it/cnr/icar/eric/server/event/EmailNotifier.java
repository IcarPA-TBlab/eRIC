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
package it.cnr.icar.eric.server.event;

import it.cnr.icar.eric.client.xml.registry.infomodel.PersonNameImpl;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.RegistryException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.NotifyActionType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Notifier used to send notifications to a email end-point when its
 * Subscription matches a registry event.
 *
 * @author <a href="mailto:Geert.Hofbauer@cronos.be">Geert Hofbauer</a>
 */
public class EmailNotifier extends AbstractNotifier {
    
    private static final Log log = LogFactory.getLog(EmailNotifier.class);

    protected void sendNotification(ServerRequestContext context, NotifyActionType notifyAction, 
        NotificationType notification, AuditableEventType ae) throws RegistryException
    {
        log.trace("Sending email notification");

        String endpoint = notifyAction.getEndPoint();

        if ((endpoint == null) ||
                !endpoint.startsWith("mailto:")) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.emailNotificationWOmailto",
                        new Object[]{endpoint}));
        }

        try {
            // get the body of the message , not yet defined !
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().rimFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            //marshaller.marshal(notification, sw);
            ObjectFactory of = new ObjectFactory();
            marshaller.marshal(of.createNotification(notification), sw);

            //Now get the RegistryResponse as a String
            
            //Produce verbose response
            String notif = sw.toString();
            String action = ae.getEventType();
            String userId = ae.getUser();
            
            String userInfo;
            if (userId != null) {
                UserType user =
		    (UserType)context.getRegistryObject(userId, "User_");
                userInfo = ServerResourceBundle.getInstance().
		    getString("message.user", new String[] { getUserInfo(user) });
            } else {
                userInfo = ServerResourceBundle.getInstance().
		    getString("message.userUnknown");
            }
            
            SubscriptionType subscription = (SubscriptionType) context.getRegistryObject(notification.getSubscription(), "Subscription");
            String xsltId = getStyleSheetId(subscription);
            RepositoryItem repositoryItem = null;

	    // empty string for notification property == use old format
	    if (! "".equals(xsltId)) {
		//Dont use transform if there are any problems
		try {
		    repositoryItem = RepositoryManagerFactory.getInstance().
			getRepositoryManager().getRepositoryItem(xsltId);
		} catch (Exception e) {
		    log.warn(ServerResourceBundle.getInstance().
			     getString("message.rawEmailNotification"), e);
		}
	    }

            String contentType;
            String message;
            if (repositoryItem == null) {
                //No style sheet so skip the tranformation
                contentType = "text/xml";
                message = sw.toString();
            } else {
                contentType = "text/html";
                try {
                    message = transformContent(context, xsltId, notif, action, userInfo);
                } catch (RegistryException e) {
                    contentType = "text/xml";
                    message = sw.toString();                    
                }
            }

            // set parameters and send the mail
            String subject = ServerResourceBundle.getInstance().getString("message.registryNotification",
                                    new Object[]{notification.getId()});
            postMail( endpoint, message, subject, contentType);
        } catch (MessagingException e) {
            throw new RegistryException(e);
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
    }
    
    //Get teh style sheet configured for this subscription
    @SuppressWarnings("static-access")
	private String getStyleSheetId(SubscriptionType subscription) {
        String xsltId = bu.FREEBXML_REGISTRY_DEFAULT_NOTIFICATION_FORMATTER;
        
        try {
            //See if Subscription has an XSLT style sheet specified. If so, use that.
            HashMap<String, Serializable> slots = bu.getSlotsFromRegistryObject(subscription);        
            xsltId = (String)slots.get(bu.CANONICAL_SLOT_SUBSCRIPTION_NOTIFICATION_FORMATTER);

            if (xsltId == null) {
                //Subscription did not specify a style sheet. Use the system wide default.
                xsltId = RegistryProperties.getInstance().getProperty(
                    "eric.server.event.defaultNotificationFormatter", 
                    bu.FREEBXML_REGISTRY_DEFAULT_NOTIFICATION_FORMATTER);
            }
        } catch (JAXBException e) {
            //Cannot happen
            log.error(e, e);
        }                
        
        return xsltId;
    }
    
    private String transformContent(ServerRequestContext context, String xsltId, String xmlNotif, String action, String user) throws RegistryException {
        try {
            RepositoryItem repositoryItem = RepositoryManagerFactory.getInstance()
                .getRepositoryManager().getRepositoryItem(xsltId);
            StreamSource xsltIn = new StreamSource(repositoryItem.getDataHandler().getInputStream());

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(xsltIn);

            //transformer.setURIResolver(rm.getURIResolver());
            transformer.setErrorListener(new ErrorListener() {
                    public void error(TransformerException exception)
                        throws TransformerException {
                        log.info(ServerResourceBundle.getInstance().
				 getString("xsltError"), exception);
                    }

                    public void fatalError(TransformerException exception)
                        throws TransformerException {
                        log.error(ServerResourceBundle.getInstance().
				 getString("xsltFatalError"), exception);
                        throw exception;
                    }

                    public void warning(TransformerException exception)
                        throws TransformerException {
                        log.info(ServerResourceBundle.getInstance().
				 getString("xsltWarning"), exception);
                    }
                });

            //Set parameters
            transformer.setParameter("action", action);
            transformer.setParameter("user", user);
            transformer.setParameter("registryBaseURL", 
                RegistryProperties.getInstance().getProperty("eric.registry.baseurl"));

            ByteArrayInputStream bais = new ByteArrayInputStream(xmlNotif.getBytes("utf-8"));
            StreamSource inputSrc = new StreamSource(bais);

            //TODO: use file in case we have a large amount of data to transform?
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult(baos);

            transformer.transform(inputSrc, streamResult);

            return baos.toString("utf-8");
        } catch (Exception e) {
            log.error(ServerResourceBundle.getInstance().
		      getString("message.prettyPrintNotificationFailure"), e);
            throw new RegistryException(e);
        }
    }    

    private void postMail( String endpoint, String message, String subject, String contentType)
        throws MessagingException
    {
        

        // get the SMTP address
        String smtpHost = RegistryProperties.getInstance().
            getProperty("eric.server.event.EmailNotifier.smtp.host");

        // get the FROM address
        String fromAddress = RegistryProperties.getInstance().
            getProperty("eric.server.event.EmailNotifier.smtp.from", "eric@localhost");
        
        // get the TO address that follows 'mailto:'
        String recipient = endpoint.substring(7);
        
        String smtpPort = RegistryProperties.getInstance()
            .getProperty("eric.server.event.EmailNotifier.smtp.port", null);
                        
        String smtpAuth = RegistryProperties.getInstance()
            .getProperty("eric.server.event.EmailNotifier.smtp.auth", null);
        
        String smtpDebug = RegistryProperties.getInstance()
            .getProperty("eric.server.event.EmailNotifier.smtp.debug", "false");
        
        //Set the host smtp address
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.debug", smtpDebug);
        props.put("mail.smtp.host", smtpHost);
        if ((smtpPort != null) && (smtpPort.length() > 0)) {
            props.put("mail.smtp.port", smtpPort);
        }
        Session session;
        if ("tls".equals(smtpAuth)) {
            // get the username
            String userName = RegistryProperties.getInstance().
                getProperty("eric.server.event.EmailNotifier.smtp.user", null);

            String password = RegistryProperties.getInstance().
                getProperty("eric.server.event.EmailNotifier.smtp.password", null);
        
            Authenticator authenticator = new MyAuthenticator(userName, password);
            props.put("mail.user",      userName);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            session = Session.getInstance(props, authenticator);
        } else {
            session = Session.getInstance(props);
        }
        
        session.setDebug(Boolean.valueOf(smtpDebug).booleanValue());

        // create a message
        Message msg = new MimeMessage(session);

        // set the from and to address
        InternetAddress addressFrom = new InternetAddress(fromAddress);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[1];
        addressTo[0] = new InternetAddress(recipient);
        msg.setRecipients(Message.RecipientType.TO, addressTo);

        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setContent(message, contentType);
        Transport.send(msg);
    }    

    //TODO: Move to a utility class as there is also another copy in AzImpl
    private String getUserInfo(UserType user) {
	String idStr = user.getId();
        String userInfo;
        
        try {
            PersonNameImpl personName =
		new PersonNameImpl(null, null, user.getPersonName());
            userInfo = ServerResourceBundle.getInstance().
		getString("message.idName",
			  new String[] { idStr, personName.getFormattedName() });
        } catch (Exception e) {
            log.warn(e.toString(), e);

	    // problem with the formatted name, fall back to id alone
	    userInfo = ServerResourceBundle.getInstance().
		getString("message.id", new String[] { idStr });
        }
        
        return userInfo;
    }
    
    private class MyAuthenticator extends Authenticator {
        private String    username;
        private String    password;

        public MyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }
    
}
