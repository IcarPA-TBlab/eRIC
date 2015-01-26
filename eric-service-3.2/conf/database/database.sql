--$Header: /cvsroot/ebxmlrr/omar/conf/database/database.sql.template,v 1.11 2006/08/06 17:05:06 farrukh_najmi Exp $
--
-- Normative SQL Schema for ebXML Registry withe the exception of index definitions which are non-normative
--
-- Workaround for the databases:
--
--  Oracle:
--      - Oracle does not support boolean field. So the we have to use VARCHAR(1)
--      - Timestamp field does not allow to have timezone (e.g. +8). We use
--        VARCHAR(30) to store timestamp. The field type 'timestamp with time
--        zone' is not available in other databases.
--  DB2:
--      - Some of the names of tables and indexed have been shortened to below 18
--        characters to accommodate the limit

--TODO: Fix spec to get rid of 1 in names such as AssocType1 and SlotType1 in version 4.

--Must drop views before tables for databases that do not support cascade
----DROP VIEW Identifiable;
----DROP VIEW RegistryObject;


CREATE TABLE Association (
--Identifiable Attributes
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--Association attributes
  associationType	VARCHAR(256) NOT NULL,
  sourceObject		VARCHAR(256) NOT NULL,
  targetObject  	VARCHAR(256) NOT NULL
);
--DROP TABLE Association
--CASCADE
--;

CREATE TABLE AuditableEvent (
--Identifiable Attributes
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AuditableEvent'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--AuditableEvent attributes
  requestId             VARCHAR(256) NOT NULL,
  eventType			VARCHAR(256) NOT NULL,
  timeStamp_        VARCHAR(30) NOT NULL,
  user_				VARCHAR(256) NOT NULL
);
--DROP TABLE AuditableEvent
--CASCADE
--;

CREATE TABLE AffectedObject (

--Each row is a relationship between a RegistryObject and an AuditableEvent
--Enables many-to-many relationship between effected RegistryObjects and AuditableEvents
  id                            VARCHAR(256) NOT NULL,
  home                      VARCHAR(256),
  eventId                   VARCHAR(256) NOT NULL,

  PRIMARY KEY (id, eventId)
);
--DROP TABLE AffectedObject
--CASCADE
--;

CREATE TABLE Classification (
--Identifiable Attributes
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--Classfication attributes.
  classificationNode		VARCHAR(256),
  classificationScheme		VARCHAR(256),
  classifiedObject		VARCHAR(256) NOT NULL,
  nodeRepresentation		VARCHAR(256)
);
--DROP TABLE Classification
--CASCADE
--;

CREATE TABLE ClassificationNode (
--Identifiable Attributes
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ClassificationNode'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--ClassficationNode attributes
  code					VARCHAR(256),
  parent				VARCHAR(256),
  path					VARCHAR(1024)
);
--DROP TABLE ClassificationNode
--CASCADE
--;

CREATE TABLE ClassScheme (
--Identifiable Attributes
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ClassificationScheme'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--ClassificationScheme attributes
  isInternal		VARCHAR(1) NOT NULL,
  nodeType		VARCHAR(256) NOT NULL
);
--DROP TABLE ClassScheme
--CASCADE
--;

CREATE TABLE ExternalIdentifier (
--Identifiable Attributes
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--ExternalIdentifier attributes
  registryObject	VARCHAR(256) NOT NULL,
  identificationScheme		VARCHAR(256) NOT NULL,
  value				VARCHAR(256) NOT NULL
);
--DROP TABLE ExternalIdentifier
--CASCADE
--;

CREATE TABLE ExternalLink (
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--ExternalLink attributes
  externalURI		VARCHAR(256) NOT NULL
);
--DROP TABLE ExternalLink
--CASCADE
--;

CREATE TABLE ExtrinsicObject (
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--ExtrinsicObject attributes
  isOpaque			VARCHAR(1) NOT NULL,
  mimeType			VARCHAR(256),

--contentVersionInfo flattened
  contentVersionName		VARCHAR(16),
  contentVersionComment	VARCHAR(256)

);
--DROP TABLE ExtrinsicObject
--CASCADE
--;

CREATE TABLE Federation (
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Federation'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--Federation attributes: currently none defined
--xsd:duration stored in string form since no corresponding SQL type. Is 32 long enough?
  replicationSyncLatency    VARCHAR(32)
);
--DROP TABLE Federation
--CASCADE
--;

CREATE TABLE Name_ (
--LocalizedString attributes flattened for Name
  charset			VARCHAR(32),
  lang				VARCHAR(32) NOT NULL,
  value				VARCHAR(1024) NOT NULL,
--The RegistryObject id for the parent RegistryObject for which this is a Name
  parent			VARCHAR(256) NOT NULL,
  PRIMARY KEY (parent, lang)
);
--DROP TABLE Name_
--CASCADE
--;

CREATE TABLE Description (
--LocalizedString attributes flattened for Description
  charset			VARCHAR(32),
  lang				VARCHAR(32) NOT NULL,
  value				VARCHAR(1024) NOT NULL,
--The RegistryObject id for the parent RegistryObject for which this is a Name
  parent			VARCHAR(256) NOT NULL,
  PRIMARY KEY (parent, lang)
);
--DROP TABLE Description
--CASCADE
--;

CREATE TABLE UsageDescription (
--LocalizedString attributes flattened for UsageDescription
  charset			VARCHAR(32),
  lang				VARCHAR(32) NOT NULL,
  value				VARCHAR(1024) NOT NULL,
--The RegistryObject id for the parent RegistryObject for which this is a Name
  parent			VARCHAR(256) NOT NULL,
  PRIMARY KEY (parent, lang)
);
--DROP TABLE UsageDescription
--CASCADE
--;

CREATE TABLE ObjectRef (
--Stores remote ObjectRefs only
--Identifiable Attributes
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                          VARCHAR(256)
);
--DROP TABLE ObjectRef
--CASCADE
--;

CREATE TABLE Organization (
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Organization'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--Organization attributes
--Organization.address attribute is in PostalAddress table
  parent			VARCHAR(256),
--primary contact for Organization, points to a User.
  primaryContact	VARCHAR(256)
--Organization.telephoneNumbers attribute is in TelephoneNumber table
);
--DROP TABLE Organization
--CASCADE
--;

CREATE TABLE RegistryPackage (
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256)

--RegistryPackage attributes: currently none defined
);
--DROP TABLE RegistryPackage
--CASCADE
--;

CREATE TABLE PostalAddress (
  city				VARCHAR(64),
  country			VARCHAR(64),
  postalCode		VARCHAR(64),
  state				VARCHAR(64),
  street			VARCHAR(64),
  streetNumber		VARCHAR(32),
--The parent object that this is an address for
  parent			VARCHAR(256) NOT NULL
);
--DROP TABLE PostalAddress
--CASCADE
--;

CREATE TABLE EmailAddress (
  address			VARCHAR(64) NOT NULL,
  type				VARCHAR(256),
--The parent object that this is an email address for
  parent			VARCHAR(256) NOT NULL
);
--DROP TABLE EmailAddress
--CASCADE
--;

CREATE TABLE Registry (
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Registry'),
  status		VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--Registry attributes
--xsd:duration stored in string form since no corresponding SQL type. Is 32 long enough?
  catalogingSyncLatency    VARCHAR(32) DEFAULT 'P1D',
  conformanceProfile      VARCHAR(16),
  operator  VARCHAR(256) NOT NULL,

--xsd:duration stored in string form since no corresponding SQL type. Is 32 long enough?
  replicationSyncLatency    VARCHAR(32) DEFAULT 'P1D',
  specificationVersion    VARCHAR(8) NOT NULL
);
--DROP TABLE Registry
--CASCADE
--;

CREATE TABLE Service (
  id			VARCHAR(256) NOT NULL PRIMARY KEY,
  home                  VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Service'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256)

--Service attributes: currently none defined
);
--DROP TABLE Service
--CASCADE
--;

CREATE TABLE ServiceBinding (
  id			VARCHAR(256) NOT NULL PRIMARY KEY,
  home                  VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ServiceBinding'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--ServiceBinding attributes
  service			VARCHAR(256) NOT NULL,
  accessURI			VARCHAR(256),
  targetBinding		VARCHAR(256)
);
--DROP TABLE ServiceBinding
--CASCADE
--;

CREATE TABLE Slot (
--Multiple rows of Slot make up a single Slot
  sequenceId		INT NOT NULL,
  name_				VARCHAR(256) NOT NULL,
  slotType			VARCHAR(256),
  value				VARCHAR(256),
--The parent RegistryObject that this is a Slot for
  parent			VARCHAR(256) NOT NULL,
  PRIMARY KEY (parent, name_, sequenceId)
);
--DROP TABLE Slot
--CASCADE
--;

CREATE TABLE SpecificationLink (
  id			VARCHAR(256) NOT NULL PRIMARY KEY,
  home                  VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:SpecificationLink'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--SpecificationLink attributes
  serviceBinding	VARCHAR(256) NOT NULL,
  specificationObject VARCHAR(256) NOT NULL
);
--DROP TABLE SpecificationLink
--CASCADE
--;

CREATE TABLE Subscription (
  id			VARCHAR(256) NOT NULL PRIMARY KEY,
  home                  VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Subscription'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--Subscription attributes
  selector              VARCHAR(256) NOT NULL,
  endTime               VARCHAR(30),

--xsd:duration stored in string form since no corresponding SQL type. Is 32 long enough?
  notificationInterval  VARCHAR(32) DEFAULT 'P1D',
  startTime             VARCHAR(30)
);
--DROP TABLE Subscription
--CASCADE
--;

CREATE TABLE NotifyAction (
  notificationOption       VARCHAR(256) NOT NULL,

--Either a ref to a Service, a String representing an email address in form: mailto:user@server,
--or a String representing an http URLin form: http://url
  endPoint                    VARCHAR(256) NOT NULL,

--Parent Subscription reference
  parent			VARCHAR(256) NOT NULL
);
--DROP TABLE NotifyAction
--CASCADE
--;

CREATE TABLE Notification (
  id				VARCHAR(256) NOT NULL PRIMARY KEY,
  home                      VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Notification'),
  status		VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--Notification attributes
  subscription        VARCHAR(256) NOT NULL
);
--DROP TABLE Notification
--CASCADE
--;

CREATE TABLE NotificationObject (

--Each row is a relationship between a RegistryObject and a Notification
--Enables a Notification to have multiple RegistryObjects
  notificationId             VARCHAR(256) NOT NULL,
  registryObjectId           VARCHAR(256) NOT NULL,

  PRIMARY KEY (notificationId, registryObjectId)
);
--DROP TABLE NotificationObject
--CASCADE
--;

CREATE TABLE AdhocQuery (
  id			VARCHAR(256) NOT NULL PRIMARY KEY,
  home                  VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:AdhocQuery'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--AdhocQuery attributes. Flattend QueryExpression attributes
  queryLanguage		VARCHAR(256) NOT NULL,
  query			VARCHAR(4096) NOT NULL
);
--DROP TABLE AdhocQuery
--CASCADE
--;

CREATE TABLE UsageParameter (
  value				VARCHAR(1024) NOT NULL,
--The parent SpecificationLink that this is a usage parameter for
  parent			VARCHAR(256) NOT NULL
);
--DROP TABLE UsageParameter
--CASCADE
--;

CREATE TABLE TelephoneNumber (
  areaCode			VARCHAR(8),
  countryCode		VARCHAR(8),
  extension			VARCHAR(8),
  -- we use "number_" instead of number, which is reserved in Oracle
  number_			VARCHAR(16),
  phoneType			VARCHAR(256),
  parent			VARCHAR(256) NOT NULL
);
--DROP TABLE TelephoneNumber
--CASCADE
--;

CREATE TABLE User_ (
  id			VARCHAR(256) NOT NULL PRIMARY KEY,
  home                  VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person:User'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--User attributes
--address is in PostalAddress table
--email is in EMailAddress table
--personName flattened
  personName_firstName	VARCHAR(64),
  personName_middleName	VARCHAR(64),
  personName_lastName	VARCHAR(64)
--telephoneNumbers is in TelephoneNumber table
);
--DROP TABLE User_
--CASCADE
--;

CREATE TABLE Person (
  id			VARCHAR(256) NOT NULL PRIMARY KEY,
  home                  VARCHAR(256),
--RegistryObject Attributes
  lid				VARCHAR(256) NOT NULL,
  objectType		VARCHAR(256) CHECK (objectType = 'urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Person'),
  status			VARCHAR(256) NOT NULL,

--VersionInfo flattened
  versionName		VARCHAR(16),
  comment_		VARCHAR(256),

--User attributes
--address is in PostalAddress table
--email is in EMailAddress table
--personName flattened
  personName_firstName	VARCHAR(64),
  personName_middleName	VARCHAR(64),
  personName_lastName	VARCHAR(64)
--telephoneNumbers is in TelephoneNumber table
);
--DROP TABLE Person
--CASCADE
--;

--Special case: The create table for RepositoryItem is done via Hibernate
--DROP TABLE RepositoryItem
--CASCADE
--;

CREATE VIEW Identifiable (
--Identifiable Attributes
  id,
  home
) AS
 SELECT
--Identifiable Attributes
  id,
  home
 FROM AdhocQuery
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Association
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM AuditableEvent
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Classification
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM ClassificationNode
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM ClassScheme
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM ExternalIdentifier
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM ExternalLink
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM ExtrinsicObject
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Federation
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Organization
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Registry
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM RegistryPackage
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Service
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM ServiceBinding
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM SpecificationLink
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Subscription
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM User_
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM Person
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home
 FROM ObjectRef
;
----DROP VIEW Identifiable;

CREATE VIEW RegistryObject (
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
) AS
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM AdhocQuery
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Association
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM AuditableEvent
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Classification
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM ClassificationNode
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM ClassScheme
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM ExternalIdentifier
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM ExternalLink
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM ExtrinsicObject
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Federation
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Organization
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Registry
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM RegistryPackage
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Service
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM ServiceBinding
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM SpecificationLink
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Subscription
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM User_
 UNION ALL
 SELECT
--Identifiable Attributes
  id,
  home,
--RegistryObject Attributes
  lid,
  objectType,
  status,
--VersionInfo flattened
  versionName,
  comment_
 FROM Person
;
----DROP VIEW RegistryObject;

--All index definitions are non-normative

---
-- FOR ORACLE, YOU MUST COMMMENT ALL THE FOLLOWING LINES BECAUSE ORACLE DOES
-- NOT ALLOW CREATE INDEX ON PRIMARY KEYS
--

--lid index
CREATE INDEX lid_AdhQuery_idx ON AdhocQuery(lid);
CREATE INDEX lid_Assoc_idx ON Association(lid);
CREATE INDEX lid_AUEVENT_idx ON AuditableEvent(lid);
CREATE INDEX lid_Class_idx ON Classification(lid);
CREATE INDEX lid_Node_idx ON ClassificationNode(lid);
CREATE INDEX lid_SCHEME_idx ON ClassScheme(lid);
CREATE INDEX lid_EID_idx ON ExternalIdentifier(lid);
CREATE INDEX lid_ExLink_idx ON ExternalLink(lid);
CREATE INDEX lid_EXTOBJ_idx ON ExtrinsicObject(lid);
CREATE INDEX lid_FED_idx ON Federation(lid);
CREATE INDEX lid_ORG_idx ON Organization(lid);
CREATE INDEX lid_Registry_idx ON Registry(lid);
CREATE INDEX lid_PKG_idx ON RegistryPackage(lid);
CREATE INDEX lid_Service_idx ON Service(lid);
CREATE INDEX lid_BIND_idx ON ServiceBinding(lid);
CREATE INDEX lid_SLnk_idx ON SpecificationLink(lid);
CREATE INDEX lid_SUBS_idx ON Subscription(lid);
CREATE INDEX lid_User_idx ON User_(lid);
CREATE INDEX lid_Person_idx ON Person(lid);
----DROP INDEX lid_AdhQuery_idx;
----DROP INDEX lid_Assoc_idx;
----DROP INDEX lid_AUEVENT_idx;
----DROP INDEX lid_Class_idx;
----DROP INDEX lid_Node_idx;
----DROP INDEX lid_SCHEME_idx;
----DROP INDEX lid_EID_idx;
----DROP INDEX lid_ExLink_idx;
----DROP INDEX lid_EXTOBJ_idx;
----DROP INDEX lid_FED_idx;
----DROP INDEX lid_ORG_idx;
----DROP INDEX lid_Registry_idx;
----DROP INDEX lid_PKG_idx;
----DROP INDEX lid_Service_idx;
----DROP INDEX lid_BIND_idx;
----DROP INDEX lid_SLnk_idx;
----DROP INDEX lid_SUBS_idx;
----DROP INDEX lid_User_idx;
----DROP INDEX lid_Person_idx;


--id index
CREATE INDEX id_AdhQuery_idx ON AdhocQuery(id);
CREATE INDEX id_Assoc_idx ON Association(id);
CREATE INDEX id_AUEVENT_idx ON AuditableEvent(id);
CREATE INDEX id_Class_idx ON Classification(id);
CREATE INDEX id_Node_idx ON ClassificationNode(id);
CREATE INDEX id_SCHEME_idx ON ClassScheme(id);
CREATE INDEX id_EID_idx ON ExternalIdentifier(id);
CREATE INDEX id_ExLink_idx ON ExternalLink(id);
CREATE INDEX id_EXTOBJ_idx ON ExtrinsicObject(id);
CREATE INDEX id_FED_idx ON Federation(id);
CREATE INDEX id_ObjectRef_idx ON ObjectRef(id);
CREATE INDEX id_ORG_idx ON Organization(id);
CREATE INDEX id_Registry_idx ON Registry(id);
CREATE INDEX id_PKG_idx ON RegistryPackage(id);
CREATE INDEX id_Service_idx ON Service(id);
CREATE INDEX id_BIND_idx ON ServiceBinding(id);
CREATE INDEX id_SLnk_idx ON SpecificationLink(id);
CREATE INDEX id_SUBS_idx ON Subscription(id);
CREATE INDEX id_User_idx ON User_(id);
CREATE INDEX id_Person_idx ON Person(id);
----DROP INDEX id_AdhQuery_idx;
----DROP INDEX id_Assoc_idx;
----DROP INDEX id_AUEVENT_idx;
----DROP INDEX id_Class_idx;
----DROP INDEX id_Node_idx;
----DROP INDEX id_SCHEME_idx;
----DROP INDEX id_EID_idx;
----DROP INDEX id_ExLink_idx;
----DROP INDEX id_EXTOBJ_idx;
----DROP INDEX id_FED_idx;
----DROP INDEX id_ObjectRef_idx;
----DROP INDEX id_ORG_idx;
----DROP INDEX id_Registry_idx;
----DROP INDEX id_PKG_idx;
----DROP INDEX id_Service_idx;
----DROP INDEX id_BIND_idx;
----DROP INDEX id_SLnk_idx;
----DROP INDEX id_SUBS_idx;
----DROP INDEX id_User_idx;
----DROP INDEX id_Person_idx;

--home index
CREATE INDEX home_AdhQuery_idx ON AdhocQuery(home);
CREATE INDEX home_Assoc_idx ON Association(home);
CREATE INDEX home_AUEVENT_idx ON AuditableEvent(home);
CREATE INDEX home_Class_idx ON Classification(home);
CREATE INDEX home_Node_idx ON ClassificationNode(home);
CREATE INDEX home_SCHEME_idx ON ClassScheme(home);
CREATE INDEX home_EID_idx ON ExternalIdentifier(home);
CREATE INDEX home_ExLink_idx ON ExternalLink(home);
CREATE INDEX home_EXTOBJ_idx ON ExtrinsicObject(home);
CREATE INDEX home_FED_idx ON Federation(home);
CREATE INDEX home_ORG_idx ON Organization(home);
CREATE INDEX home_Registry_idx ON Registry(home);
CREATE INDEX home_PKG_idx ON RegistryPackage(home);
CREATE INDEX home_Service_idx ON Service(home);
CREATE INDEX home_BIND_idx ON ServiceBinding(home);
CREATE INDEX home_SLnk_idx ON SpecificationLink(home);
CREATE INDEX home_SUBS_idx ON Subscription(home);
CREATE INDEX home_User_idx ON User_(home);
CREATE INDEX home_Person_idx ON Person(home);
----DROP INDEX home_AdhQuery_idx;
----DROP INDEX home_Assoc_idx;
----DROP INDEX home_AUEVENT_idx;
----DROP INDEX home_Class_idx;
----DROP INDEX home_Node_idx;
----DROP INDEX home_SCHEME_idx;
----DROP INDEX home_EID_idx;
----DROP INDEX home_ExLink_idx;
----DROP INDEX home_EXTOBJ_idx;
----DROP INDEX home_FED_idx;
----DROP INDEX home_ORG_idx;
----DROP INDEX home_Registry_idx;
----DROP INDEX home_PKG_idx;
----DROP INDEX home_Service_idx;
----DROP INDEX home_BIND_idx;
----DROP INDEX home_SLnk_idx;
----DROP INDEX home_SUBS_idx;
----DROP INDEX home_User_idx;
----DROP INDEX home_Person_idx;

CREATE INDEX id_evId_AFOBJ_idx ON AffectedObject(id, eventId);
----DROP INDEX id_evId_AFOBJ_idx;


--
-- Following indexes should be OK to use in all databases
--

--index on AffectedObject
CREATE INDEX id_AFOBJ_idx ON AffectedObject(id);
----DROP INDEX id_AFOBJ_idx;
CREATE INDEX evid_AFOBJ_idx ON AffectedObject(eventid);
----DROP INDEX evid_AFOBJ_idx;

--index on AuditableEvent
CREATE INDEX lid_AUEVENT_evtTyp ON AuditableEvent(eventType);

--name index
CREATE INDEX value_Name_idx ON Name_(value);
CREATE INDEX lngval_Name_idx ON Name_(lang, value);
----DROP INDEX value_Name_idx;
----DROP INDEX lngval_Name_idx;

--description index
CREATE INDEX value_Desc_idx ON Description(value);
CREATE INDEX lngval_Desc_idx ON Description(lang, value);
----DROP INDEX value_Desc_idx;
----DROP INDEX lngval_Desc_idx;

--UsageDesc index
CREATE INDEX value_UsgDes_idx ON UsageDescription(value);
CREATE INDEX lngval_UsgDes_idx ON UsageDescription(lang, value);
----DROP INDEX value_UsgDes_idx;
----DROP INDEX lngval_UsgDes_idx;

--Indexes on Assoc

CREATE INDEX src_Ass_idx ON Association(sourceObject);
CREATE INDEX tgt_Ass_idx ON Association(targetObject);
CREATE INDEX type_Ass_idx ON Association(associationType);
----DROP INDEX src_Ass_idx;
----DROP INDEX tgt_Ass_idx;
----DROP INDEX type_Ass_idx;


--Indexes on Class

CREATE INDEX clsObj_Class_idx ON Classification(classifiedObject);
CREATE INDEX node_Class_idx ON Classification(classificationNode);
----DROP INDEX clsObj_Class_idx;
----DROP INDEX node_Class_idx;

--Indexes on ClassNode

CREATE INDEX parent_Node_idx ON ClassificationNode(parent);
CREATE INDEX code_Node_idx ON ClassificationNode(code);
CREATE INDEX path_Node_idx ON ClassificationNode(path);
----DROP INDEX parent_Node_idx;
----DROP INDEX code_Node_idx;
----DROP INDEX path_Node_idx;

--Indexes on ExIdentifier

CREATE INDEX ro_EID_idx ON ExternalIdentifier(registryObject);
----DROP INDEX ro_EID_idx;

--Indexes on ExLink

CREATE INDEX uri_ExLink_idx ON ExternalLink(externalURI);
----DROP INDEX uri_ExLink_idx;

--Indexes on ExtrinsicObject

--Indexes on Organization
CREATE INDEX parent_ORG_idx ON Organization(parent);
----DROP INDEX parent_ORG_idx;

--Indexes on PostalAddress

CREATE INDEX parent_PstlAdr_idx ON PostalAddress(parent);
CREATE INDEX city_PstlAdr_idx ON PostalAddress(city);
CREATE INDEX cntry_PstlAdr_idx ON PostalAddress(country);
CREATE INDEX pCode_PstlAdr_idx ON PostalAddress(postalCode);
----DROP INDEX parent_PstlAdr_idx;
----DROP INDEX city_PstlAdr_idx;
----DROP INDEX cntry_PstlAdr_idx;
----DROP INDEX pCode_PstlAdr_idx;

--Indexes on EmailAddress

CREATE INDEX parent_EmlAdr_idx ON EmailAddress(parent);
----DROP INDEX parent_EmlAdr_idx;

--Indexes on ServiceBinding

CREATE INDEX service_BIND_idx ON ServiceBinding(service);
----DROP INDEX service_BIND_idx;

--Indexes on Slot

CREATE INDEX parent_Slot_idx ON Slot(parent);
CREATE INDEX name_Slot_idx ON Slot(name_);
----DROP INDEX parent_Slot_idx;
----DROP INDEX name_Slot_idx;

--Indexes on SpecLink

CREATE INDEX binding_SLnk_idx ON SpecificationLink(serviceBinding);
CREATE INDEX spec_SLnk_idx ON SpecificationLink(specificationObject);
----DROP INDEX binding_SLnk_idx;
----DROP INDEX spec_SLnk_idx;

--Indexes on TelephoneNumber

CREATE INDEX parent_Phone_idx ON TelephoneNumber(parent);
----DROP INDEX parent_Phone_idx;

--Indexes on User
CREATE INDEX lastNm_User_idx ON User_(personName_lastName);
----DROP INDEX lastNm_User_idx;

--Indexes on Person
CREATE INDEX lastNm_Person_idx ON Person(personName_lastName);
----DROP INDEX lastNm_Person_idx;
