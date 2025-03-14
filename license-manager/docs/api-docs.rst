.. ===============LICENSE_START================================================
.. Acumos CC-BY-4.0
.. ============================================================================
.. Copyright (C) 2019 Nordix Foundation
.. ============================================================================
.. This Acumos documentation file is distributed by Nordix Foundation.
.. under the Creative Commons Attribution 4.0 International License
.. (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
..      http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END==================================================
..

==================================
Application Programming Interfaces
==================================

This document defines the APIs that are being produced
or consumed by the license management sub-component in Acumos.


LicenseProfile.validate
=======================

Validate given License Profile JSON text based on $schema property.

Example api call:

.. code-block:: java

    // where client is instance of ICommonDataServiceRestClient
    LicenseProfile licProfile = new LicenseProfile(client);
    LicenseProfileValidationResults results = licProfile.validate(licProfileJson);
    boolean isValid = results.getJsonSchemaErrors().isEmpty();

Learn more in `LicenseJsonValidationResults java docs <https://javadocs.acumos.org/org.acumos.license-manager/master/org/acumos/licensemanager/jsonvalidator/model/LicenseJsonValidationResults.html>`_


LicenseProfile.getTemplates
===========================

Fetch list of default License Profile Templates.

Example api call:

.. code-block:: java

    // where client is instance of ICommonDataServiceRestClient
    LicenseProfile licProfile = new LicenseProfile(client);
    List<MLPLicenseProfileTemplate> templates = licProfile.getTemplates();


Learn more in `LicenseProfile java docs <https://javadocs.acumos.org/org.acumos.license-manager/master/org/acumos/licensemanager/client/LicenseProfile.html>`_


LicenseProfile.getTemplate(templateID)
======================================

Fetch License Profile Template for given templateID.

Example api call:

.. code-block:: java

    // where client is instance of ICommonDataServiceRestClient
    LicenseProfile licProfile = new LicenseProfile(client);
    // where licProTplId is templateID of specific License Profile Template
    // to fetch
    MLPLicenseProfileTemplate licProTpl = licProfile.getTemplate(licProTplId);

Learn more in `LicenseProfile java docs <https://javadocs.acumos.org/org.acumos.license-manager/master/org/acumos/licensemanager/client/LicenseProfile.html>`_


LicenseAsset.register
=====================

Method fetches information about the ML model (nexus and CDS) and
registers the software with License Usage Manager.

.. code-block:: java

    // where cdsClient is instance of ICommonDataServiceRestClient
    // LUM_SERVER is url of the LUM service
    // NEXUS_SERVER is the url of the nexus service
    LicenseAsset asset = new LicenseAsset(cdsClient, LUM_SERVER, NEXUS_SERVER);
    RegisterAssetRequest request = new RegisterAssetRequest();
    request.setSolutionId(solutionId);
    request.setRevisionId(revisionId);
    request.setLoggedIdUser(loggedInUser);
    CompletableFuture<RegisterAssetResponse> responseFuture = asset.register(request);
    RegisterAssetResponse response = responseFuture.get();


Learn more in `LicenseAsset java docs <https://javadocs.acumos.org/org.acumos.license-manager/master/org/acumos/licensemanager/client/rtu/LicenseAsset.html>`_



LicenseRtuVerifier.verifyRtu
============================

Method fetches information about the ML model (nexus and CDS) and
registers the software with License Usage Manager.

.. code-block:: java

    VerifyLicenseRequest licenseDownloadRequest =
        new VerifyLicenseRequest(
            licenseAction, solutionId, revisionId, loggedInUserId, assetUsageId);
    licenseDownloadRequest.setAction(licenseAction);
    LicenseRtuVerifier licenseVerifier = new LicenseRtuVerifier(LUM_SERVER);
    CompletableFuture<LicenseRtuVerification> verifyUserRTU =
        licenseVerifier.verifyRtu(licenseDownloadRequest);


Learn more in `LicenseRtuVerifier java docs <https://javadocs.acumos.org/org.acumos.license-manager/master/org/acumos/licensemanager/client/rtu/LicenseRtuVerifier.html>`_


LicenseAsset.getEntitledSwidTagsByUser
=====================

Method fetches all the available SwidTags from LUM based on particular user.

.. code-block:: java

    // where cdsClient is instance of ICommonDataServiceRestClient
    // LUM_SERVER is url of the LUM service
    // NEXUS_SERVER is the url of the nexus service
    LicenseAsset licenseAsset = new LicenseAsset(cdsClient, LUM_SERVER, NEXUS_SERVER);
    String userId;
	String action;
	CompletableFuture<GetEntitledSwidTagsResponse> getEntitledSwidTagsFuture =
        licenseAsset.getEntitledSwidTagsByUser(userId, action);
    GetEntitledSwidTagsResponse response = getEntitledSwidTagsFuture.get();

	
	
LicenseAsset.getAssetUsageAgreement
=====================

Method is used for import the data from LUM.

.. code-block:: java

    // where cdsClient is instance of ICommonDataServiceRestClient
    // LUM_SERVER is url of the LUM service
    // NEXUS_SERVER is the url of the nexus service
    LicenseAsset licenseAsset = new LicenseAsset(cdsClient, LUM_SERVER, NEXUS_SERVER);
    String softwareLicensorId;
	String assetUsageAgreementId;
	CompletableFuture<GetAssetUsageAgreementResponse> responseFuture =
        licenseAsset.getAssetUsageAgreement(softwareLicensorId, assetUsageAgreementId);
    GetAssetUsageAgreementResponse response = responseFuture.get();	

	

LicenseAsset.putAssetUsageAgreement
=====================

Method is used for export the data into LUM.

.. code-block:: java

    // where cdsClient is instance of ICommonDataServiceRestClient
    // LUM_SERVER is url of the LUM service
    // NEXUS_SERVER is the url of the nexus service
    LicenseAsset licenseAsset = new LicenseAsset(cdsClient, LUM_SERVER, NEXUS_SERVER);
    String softwareLicensorId;
	String assetUsageAgreementId;
	PutAssetUsageAgreementRequest request = new PutAssetUsageAgreementRequest();
	CompletableFuture<PutAssetUsageAgreementResponse> responseFuture =
        licenseAsset.putAssetUsageAgreement(softwareLicensorId, assetUsageAgreementId,request);
    PutAssetUsageAgreementResponse response = responseFuture.get();		

	
	
LicenseAsset.putAssetUsageAgreementRestriction
=====================

Method is used for export the data into LUM with Restriction.

.. code-block:: java

    // where cdsClient is instance of ICommonDataServiceRestClient
    // LUM_SERVER is url of the LUM service
    // NEXUS_SERVER is the url of the nexus service
    LicenseAsset licenseAsset = new LicenseAsset(cdsClient, LUM_SERVER, NEXUS_SERVER);
    String softwareLicensorId;
	String assetUsageAgreementId;
	PutAssetUsageAgreementRestrictionRequest request = new PutAssetUsageAgreementRestrictionRequest();
	CompletableFuture<PutAssetUsageAgreementResponse> responseFuture =
        licenseAsset.putAssetUsageAgreementRestriction(softwareLicensorId, assetUsageAgreementId,request);
    PutAssetUsageAgreementResponse response = responseFuture.get();	
