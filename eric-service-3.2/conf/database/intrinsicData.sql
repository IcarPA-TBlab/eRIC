

--$Header: /cvsroot/ebxmlrr/omar/conf/database/intrinsicData.sql,v 1.3 2006/04/25 01:27:44 tonygraham Exp $
--SQL Load file for creating loading factory defined data for ebXML Registry databse
--This is a bootstrapping step that cannot be done via normal registry interface

--Insert factory defined Users

--RegistryOperator
INSERT INTO User_ VALUES('urn:freebxml:registry:predefinedusers:registryoperator', null, 'urn:freebxml:registry:predefinedUser:RegistryOperator', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted', '1.0', null,  'Registry', null, 'Operator');
INSERT INTO PostalAddress VALUES('Burlington', 'USA', '01803', 'MA', 'Network Dr.', '1', 'urn:freebxml:registry:predefinedusers:registryoperator');
INSERT INTO TelephoneNumber VALUES('781', '01', '', '442-0703', 'OfficePhone', 'urn:freebxml:registry:predefinedusers:registryoperator');
INSERT INTO EmailAddress VALUES('registryOperator@ebxmlrr.com', 'OfficeEmail', 'urn:freebxml:registry:predefinedusers:registryoperator');
-- We make the owner of the User object to be the user itself.
INSERT INTO AuditableEvent VALUES('urn:freebxml:registry:predefinedEvent:createRegistryOperator', null, 'urn:freebxml:registry:predefinedEvent:createRegistryOperator', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted', '1.0', null,  '//TODO', 'urn:oasis:names:tc:ebxml-regrep:EventType:Created', '2003-12-10 10:53:24', 'urn:freebxml:registry:predefinedusers:registryoperator');
INSERT INTO AffectedObject VALUES('urn:freebxml:registry:predefinedusers:registryoperator', null, 'urn:freebxml:registry:predefinedEvent:createRegistryOperator');

--RegistryGuest
INSERT INTO User_ VALUES('urn:freebxml:registry:predefinedusers:registryguest', null, 'urn:freebxml:registry:predefinedusers:registryguest', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted',   '1.0', null,  'Registry', null, 'Guest');
INSERT INTO PostalAddress VALUES('Burlington', 'USA', '01803', 'MA', 'Network Dr.', '1', 'urn:freebxml:registry:predefinedusers:registryguest');
INSERT INTO TelephoneNumber VALUES('781', '01', '', '442-0703', 'OfficePhone', 'urn:freebxml:registry:predefinedusers:registryguest');
INSERT INTO EmailAddress VALUES('registryGuest@ebxmlrr.com', 'OfficeEmail', 'urn:freebxml:registry:predefinedusers:registryguest');
-- We make the owner of the User object to be the user itself.
INSERT INTO AuditableEvent VALUES('urn:freebxml:registry:predefinedEvent:createRegistryGuest', null, 'urn:freebxml:registry:predefinedEvent:createRegistryGuest', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted', '1.0', null,  '//TODO', 'urn:oasis:names:tc:ebxml-regrep:EventType:Created', '2003-12-10 10:53:24', 'urn:freebxml:registry:predefinedusers:registryguest');
INSERT INTO AffectedObject VALUES('urn:freebxml:registry:predefinedusers:registryguest', null, 'urn:freebxml:registry:predefinedEvent:createRegistryGuest');

--Test user Farrukh
INSERT INTO User_ VALUES('urn:freebxml:registry:predefinedusers:farrukh', null, 'urn:freebxml:registry:predefinedusers:farrukh', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted',  '1.0', null,  'Farrukh', 'Salahudin', 'Najmi');
INSERT INTO PostalAddress VALUES('Burlington', 'USA', '01803', 'MA', 'Network Dr.', '1', 'urn:freebxml:registry:predefinedusers:farrukh');
INSERT INTO TelephoneNumber VALUES('781', '01', '', '442-0703', 'OfficePhone', 'urn:freebxml:registry:predefinedusers:farrukh');
INSERT INTO EmailAddress VALUES('farrukh.najmi@sun.com', 'OfficeEmail', 'urn:freebxml:registry:predefinedusers:farrukh');
-- We make the owner of the User object to be the user itself.
INSERT INTO AuditableEvent VALUES('urn:freebxml:registry:predefinedEvent:createFarrukh', null, 'urn:freebxml:registry:predefinedEvent:createFarrukh', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted', '1.0', null,  '//TODO', 'urn:oasis:names:tc:ebxml-regrep:EventType:Created', '2003-12-10 10:53:24', 'urn:freebxml:registry:predefinedusers:farrukh');
INSERT INTO AffectedObject VALUES('urn:freebxml:registry:predefinedusers:farrukh', null, 'urn:freebxml:registry:predefinedEvent:createFarrukh');

--Test user Nikola
INSERT INTO User_ VALUES('urn:freebxml:registry:predefinedusers:nikola', null, 'urn:freebxml:registry:predefinedusers:nikola', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted',  '1.0', null,  'Nikola', null, 'Stojanovic');
INSERT INTO PostalAddress VALUES('Ithaca', 'France', null, 'NY', 'Terazije', '19', 'urn:freebxml:registry:predefinedusers:nikola');
INSERT INTO TelephoneNumber VALUES('11', '381', '', '222-2222', 'OfficePhone', 'urn:freebxml:registry:predefinedusers:nikola');
INSERT INTO TelephoneNumber VALUES('11', '381', '', '444-4444', 'HomePhone', 'urn:freebxml:registry:predefinedusers:nikola');
INSERT INTO EmailAddress VALUES('nhomest1@twcny.rr.com', 'OfficeEmail', 'urn:freebxml:registry:predefinedusers:nikola');
-- We make the owner of the User object to be the user itself.
INSERT INTO AuditableEvent VALUES('urn:freebxml:registry:predefinedEvent:createNikola', null, 'urn:freebxml:registry:predefinedEvent:createNikola', 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent', 'urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted', '1.0', null,  '//TODO', 'urn:oasis:names:tc:ebxml-regrep:EventType:Created', '2003-12-10 10:53:24', 'urn:freebxml:registry:predefinedusers:nikola');
INSERT INTO AffectedObject VALUES('urn:freebxml:registry:predefinedusers:nikola', null, 'urn:freebxml:registry:predefinedEvent:createNikola');

