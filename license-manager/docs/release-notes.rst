.. ===============LICENSE_START================================================
.. Acumos CC-BY-4.0
.. ============================================================================
.. Copyright (C) 2019 Nordix Foundation
.. Modifications copyright (C)2020 Tech Mahindra
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

============================================
License Manager Client Library Release Notes
============================================
Version 1.5.1 08-May-2020
--------------------------
* lum-java-rest-client 1.2.0 upgrade
* New method is written in LMCL to call new LUM API to retrieve models available for Accucompose composition - `ACUMOS-3971 <https://jira.acumos.org/browse/ACUMOS-3971>`_
* New method is written in LMCL to call existing LUM API for new RTU flow - `ACUMOS-4112 <https://jira.acumos.org/browse/ACUMOS-4112>`_

Version 1.4.4 20 Feb 2020
--------------------------
* LicenseAsset support NexusArtifactClient - `ACUMOS-3960 <https://jira.acumos.org/browse/ACUMOS-3960>`_

Version 1.4.3 2 Dec 2019
--------------------------
* CDS 3.1.0 upgrade

Version 1.4.2 2 Dec 2019
--------------------------
* Fix Logging support reduce amount of logging and use logback - `ACUMOS-3600 <https://jira.acumos.org/browse/ACUMOS-3600>`_

Version 1.4.1 31 Oct 2019
--------------------------
* Prepublish exclusion from LUM since software is not registered - `ACUMOS-3651 <https://jira.acumos.org/browse/ACUMOS-3651>`_

Version 1.4.0 23 Oct 2019
--------------------------
* Integration changes with LUM 0.28.0 - actionable rtu denial messages - `ACUMOS-3601 <https://jira.acumos.org/browse/ACUMOS-3601>`_

Version 1.3.0 17 Oct 2019
--------------------------
* Integration changes with LUM 0.27.1 - `ACUMOS-3082 <https://jira.acumos.org/browse/ACUMOS-3082>`_

Version 1.2.0 10 Oct 2019
--------------------------
- LMCL - `ACUMOS-3537 <https://jira.acumos.org/browse/ACUMOS-3537>`_ ,  `ACUMOS-3553 <https://jira.acumos.org/browse/ACUMOS-3553>`_

  - Class not found issues with LUM java client
  - Handle 402 response
  - Support single action rtu verifications - multiple lookups causing issues
  - LicenseRtuVerifier api updates - handle isAllowed without passing
    action again
  - Support rtu denials from lum

Version 1.1.0 01 Oct 2019
--------------------------
* Support to parse & validate diff versions (latest and boreas releases)
  of license profile documents - `ACUMOS-3494 <https://jira.acumos.org/browse/ACUMOS-3494>`_

Version 1.0.0 23 Sept 2019
---------------------------

* Adding rtuRequired flag to profile to activate lum entitlement requirement during
  software registration `ACUMOS-3458 <https://jira.acumos.org/browse/ACUMOS-3458>`_
  New api
  org.acumos.licensemanager.client.rtu.LicenseAsset

    - register software with LUM `ACUMOS-3339 <https://jira.acumos.org/browse/ACUMOS-3339>`_
      org.acumos.licensemanager.client.rtu.LicenseRtuVerifier
    - verfiyRtu software with LUM  `ACUMOS-3228 <https://jira.acumos.org/browse/ACUMOS-3228>`_

* New dependency org.acumos.license-usage-manager:lum:java-rest-client
* Updatd CDS dependency to 3.0.0
* Java 11 support
* Removed RTU creation APIs -- LUM agreement api will be used to create rtu
    org.acumos.licensemanager.client.LicenseCreator
    org.acumos.licensemanager.client.model.CreatedRtu
    org.acumos.licensemanager.client.model.CreateRtuRequest
    org.acumos.licensemanager.client.model.ICreatedRtuResponse
    org.acumos.licensemanager.client.model.ILicenseCreator
* Renamed APIs
    org.acumos.licensemanager.client.LicenseVerifier ->
    org.acumos.licensemanager.client.rtu.LicenseRtuVerifier
* Updated classes
  org.acumos.licensemanager.client.model.BaseLicenseRequest
  org.acumos.licensemanager.client.model.ICommonLicenseRequest
  org.acumos.licensemanager.exceptions.RightToUseException
  org.acumos.licensemanager.client.model.VerifyLicenseRequest
* LicenseProfile validator bump json schema validator library to 1.0.20 - see fixes in change log https://github.com/networknt/json-schema-validator/blob/master/CHANGELOG.md
* LicenseProfile validator update docs to answer questions in ACUMOS-3338 `ACUMOS-3338 <https://jira.acumos.org/browse/ACUMOS-3338>`_
* Sequence diagrams

Version 0.0.14 4 Sept 2019
--------------------------
* LicenseProfile APIs - getTemplate(s) `ACUMOS-3387 <https://jira.acumos.org/browse/ACUMOS-3387>`_
* LicenseProfile APIs - validate `ACUMOS-3336 <https://jira.acumos.org/browse/ACUMOS-3336>`_

Version 0.0.13 11 Jul 2019
--------------------------
* No change - but release along with other sub-projects - License
  Profile and RTU Editors

Version 0.0.12 11 Jul 2019
--------------------------
* License JSON schema with required fields `ACUMOS-3197 <https://jira.acumos.org/browse/ACUMOS-3197>`_

Version 0.0.11 08 Jul 2019
--------------------------
* Moved LMCL from security-verification to license-manager repo `ACUMOS-3189 <https://jira.acumos.org/browse/ACUMOS-3189>`_

Version 0.0.10 04 Jun 2019
--------------------------
* Support RTU removing users from RTU `ACUMOS-3003 <https://jira.acumos.org/browse/ACUMOS-3003>`_

Version 0.0.9, 29 May 2019
--------------------------
* Update license-manager-client-library version as security-verification
  version changes
* Update RTU refs  `ACUMOS-2896 <https://jira.acumos.org/browse/ACUMOS-2896>`_

Version 0.0.9, 30 May 2019
--------------------------
* Update license-manager-client-library version as security-verification
  version changes

Version 0.0.8, 14 May 2019
--------------------------
* Update license-manager-client-library version as security-verification
  version changes

Version 0.0.7, 17 May 2019
--------------------------
* Update license-manager to support associating Rtu to userId. (`ACUMOS-2896 <https://jira.acumos.org/browse/ACUMOS-2896>`_)

Version 0.0.6, 14 May 2019
--------------------------
* Update license-manager-client-library version as security-verification version changes (`ACUMOS-2887 <https://jira.acumos.org/browse/ACUMOS-2887>`_)

Version 0.0.5, 10 May 2019
--------------------------
* Update license-manager-client-library version as security-verification version changes (`ACUMOS-2887 <https://jira.acumos.org/browse/ACUMOS-2887>`_)

Version 0.0.4, 01 May 2019
--------------------------
* Update license-manager-client-library, security-verification-client and security-verification-service For LF release  (`ACUMOS-2830 <https://jira.acumos.org/browse/ACUMOS-2830>`_)
* Updated license headers `ACUMOS-2794 <https://jira.acumos.org/browse/ACUMOS-2794>`_
* Documentation cleanup `ACUMOS-2795 <https://jira.acumos.org/browse/ACUMOS-2795>`_
* Updated to CDS 2.2.2 to be compatible with portal `ACUMOS-2793 <https://jira.acumos.org/browse/ACUMOS-2793>`_
* Api Docs move from wiki - `ACUMOS-2792 <https://jira.acumos.org/browse/ACUMOS-2792>`_

Version 0.0.3, 09 April 2019
----------------------------

* Adding support for validating license.json
* `ACUMOS-2731 <https://jira.acumos.org/browse/ACUMOS-2731>`_
* Checkstyle - based on google checks -- a couple compatibity issue
* ILicenseVerifier.verifyRTU -> ILicenseVerifier.verfiyRtu (case change)
* ILicenseCreator.createRTU -> ILicenseCreator.createRtu (case change)

Version 0.0.2, 20 March 2019
----------------------------

adding CDS support, simplify api
* `ACUMOS-2631 <https://jira.acumos.org/browse/ACUMOS-2631>`_
* `ACUMOS-2614 <https://jira.acumos.org/browse/ACUMOS-2614>`_

Version 0.0.1, 8 March 2019
---------------------------

initial dev version
* `ACUMOS-2546 <https://jira.acumos.org/browse/ACUMOS-2546>`_
* `ACUMOS-2606 <https://jira.acumos.org/browse/ACUMOS-2606>`_
