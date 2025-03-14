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

============================================
License RTU Editor Release Notes
============================================
Version 0.1.7 18 Jun 2020
-------------------------
- Unable to click to retrieve RTU Agreement from LUM- `ACUMOS-4158 <https://jira.acumos.org/browse/ACUMOS-4158>`_

Version 0.1.6 15 May 2020
-------------------------
- Option to click a reset button- `ACUMOS-3905 <https://jira.acumos.org/browse/ACUMOS-3905>`_
- RTU Editor: Mandatory Fields in RTU- `ACUMOS-3904 <https://jira.acumos.org/browse/ACUMOS-3904>`_
- RTU Editor: Quit Button(X) is required on the i frame- `ACUMOS-4131 <https://jira.acumos.org/browse/ACUMOS-4131>`_
- RTU Editor:-UI for RTU editor is not consistent- `ACUMOS-3908 <https://jira.acumos.org/browse/ACUMOS-3908>`_
- As a License Admin I want to be able to launch the RTU editor and have the ability to import/export the agreement with LUM to fill out RTU Agreement- `ACUMOS-4084 <https://jira.acumos.org/browse/ACUMOS-4084>`_

Version 0.1.5 06 Jan 2020
-------------------------
- Adding local file backup due to corporate proxy blocking github url- `ACUMOS-3834 <https://jira.acumos.org/browse/ACUMOS-3834>`_
- Updating angular to latest runtime

Version 0.1.4 05 Dec 2019
-------------------------
- Adding Required indicator to Target Identifier - `ACUMOS-3799 <https://jira.acumos.org/browse/ACUMOS-3799>`_

Version 0.1.3 04 Nov 2019
-------------------------
- Fix to handle non-privileged mode - `ACUMOS-3648 <https://jira.acumos.org/browse/ACUMOS-3648>`_

Version 0.1.1 29 Oct 2019
-------------------------
- Fix to handle nested array types (like refinements, Constraints etc.)  - `ACUMOS-3610 <https://jira.acumos.org/browse/ACUMOS-3610>`_

Version 0.1.0 23 Oct 2019
-------------------------
- RTU editor changes for Supplier/Subscriber - `ACUMOS-3115 <https://jira.acumos.org/browse/ACUMOS-3115>`_

  - As a Supplier, I can

    - select / apply pre-defined Open digital rights language (ODRL)
      RTU agreement examples
    - import / apply sample Open digital rights language (ODRL) RTU
      agreement from a file
    - Use RTU editor to create RTU LUM Asset Usage Agreement and
      download it to local file system

  - As a Subscriber, I can

    - import / apply RTU LUM Asset Usage agreement from a file

      - review Open digital rights language (ODRL) RTU agreement in
        READ-ONLY mode and save to LUM server

    - fetch RTU LUM Asset Usage agreement from LUM server

      - add / edit restrictions
      - save restrictions (of RTU LUM agreement) to LUM server

  - schema changes - v1.0.1

    - added new attribute "schemaType" to distinguish between the
      RTU agreement and restrictions schema
    - used the value of "schemaType" to identify respective layout
    - added new schema for agreement "restrictions"
    - replaced / removed 'dateTime' as only 'date' would be supported

  - layout changes - v1.0.1

    - added layout for RTU agreement restrictions fields

- Added field to submit 'userId' while saving to LUM server
- Subscriber > initialize restriction uid, permission and
  prohibition from respective agreement fields

Version 0.0.7 10 Oct 2019
-------------------------
- Remove redundant property under refinements - lum:swLicensor `ACUMOS-3553 <https://jira.acumos.org/browse/ACUMOS-3553>`_

Version 0.0.6 09 Oct 2019
-------------------------
-  Integration changes with clio `ACUMOS-3538 <https://jira.acumos.org/browse/ACUMOS-3538>`_

  - Show UI field to enter LUM server URL
  - Ability to save the RTU document to LUM server
  - Changes to RTU document as per LUM API expectations
  - Put the base version of app.version.ts in source repo so that
    editor can run in non-build environment like stackblitz

Version 0.0.5 03 Oct 2019
--------------------------
* License RTU editor - fix layout path - `ACUMOS-3494 <https://jira.acumos.org/browse/ACUMOS-3494>`_

Version 0.0.4 01 Oct 2019
--------------------------
* Support to process & render diff versions of license
  RTU documents - `ACUMOS-3494 <https://jira.acumos.org/browse/ACUMOS-3494>`_

Version 0.0.3 20 Sep 2019
--------------------------
* Provide a web based editor to create RTU Agreement `ACUMOS-3310 <https://jira.acumos.org/browse/ACUMOS-3310>`_
* Removed RTU prohibitions > constraints node `ACUMOS-3311 <https://jira.acumos.org/browse/ACUMOS-3311>`_
* Added RTU target > refinement node `ACUMOS-3312 <https://jira.acumos.org/browse/ACUMOS-3312>`_
* Added RTU assignee > refinement node `ACUMOS-3313 <https://jira.acumos.org/browse/ACUMOS-3313>`_
* Added RTU permission > constraints node `ACUMOS-3343 <https://jira.acumos.org/browse/ACUMOS-3343>`_
* RTU editor style improvements `ACUMOS-3468 <https://jira.acumos.org/browse/ACUMOS-3468>`_

Version 0.0.2 30 Aug 2019
--------------------------
* No change - but release along with other sub-project -
  License Manager Client Library

Version 0.0.1, 12 August 2019
-----------------------------

* Support loading as iframe doc `ACUMOS-3280 <https://jira.acumos.org/browse/ACUMOS-3280>`_
* Support using as web component `ACUMOS-3280 <https://jira.acumos.org/browse/ACUMOS-3280>`_
* License RTU Editor for creating RTU `ACUMOS-3079 <https://jira.acumos.org/browse/ACUMOS-3079>`_
