--$Header: /cvsroot/ebxmlrr/omar/conf/database/storedProcedures.sql,v 1.1 2005/01/30 16:52:07 doballve Exp $
--Define Stored procedures that map to RIM class methods

--Procedures on RegistryObject
CREATE PROCEDURE RegistryObject_associatedObjects(registryEntryId) {
--Must return a collection of UUIDs for related RegistryObject instances
}

CREATE PROCEDURE RegistryObject_auditTrail(registryEntryId) {
--Must return an collection of UUIDs for AuditableEvents related to the RegistryObject.
--Collection must be in ascending order by timestamp
}

CREATE PROCEDURE RegistryObject_externalLinks(registryEntryId) {
--Must return a collection of UUIDs for ExternalLinks annotating this RegistryObject.
}

CREATE PROCEDURE RegistryObject_externalIdentifiers(registryEntryId) {
--Must return a collection of UUIDs for ExternalIdentifiers for this RegistryObject.
}

CREATE PROCEDURE RegistryObject_classifications(registryEntryId) {
--Must return a collection of UUIDs for Classifications classifying this RegistryObject.
}

CREATE PROCEDURE RegistryObject_packages(registryEntryId) {
--Must return a collection of UUIDs for Packages that this RegistryObject belongs to.
}


--Procedures on RegistryPackage
CREATE PROCEDURE RegistryPackage_memberObjects(packageId) {
--Must return a collection of UUIDs for RegistryObjects that are memebers of this Package.
}

--Procedures on ExternalLink
CREATE PROCEDURE ExternalLink_linkedObjects(registryEntryId) {
--Must return a collection of UUIDs for objects in this relationship
}

--Procedures on ClassificationNode
CREATE PROCEDURE ClassificationNode_classifiedObjects(classificationNodeId) {
--Must return a collection of UUIDs for RegistryEntries classified by this ClassificationNode
}



