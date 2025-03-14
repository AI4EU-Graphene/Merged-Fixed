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

===============
License Manager
===============

License Manager Client Library
------------------------------

The license manager client library provides a java jar to perform the following functions:
  - Provide a schema for license profile
  - Api to Validate license profiles against the schema
  - Api to Register ML models with License Usage Manager 
  - Api to Verify entitlement with License Usage Manager
  - Api to Support Portal back end
  - Api to fetch all the available SwidTags with License Usage Manager
  
.. toctree::
       :maxdepth: 2

       api-docs.rst
       developer-guide.rst
       release-notes.rst

License Profile editor
----------------------

The license profile editor provides a micro front end for the creation of the license profile.
  - Can work standalone or with the portal front end
  - Angular 8 app that is served using a docker image with nginx web server.

.. toctree::
       :maxdepth: 2

       user-guide-license-profile-editor.rst
       dev-guide-license-profile-editor.rst
       release-notes-license-profile-editor.rst


License Right To Use (RTU) editor
---------------------------------

The license right to use (RTU) editor provides a micro front end for the management of
  agreements and right.

  - suppliers create an agreement with a subscriber.
  - subscriber can review the agreement from the suppliers and save into
    license usage manager
  - subscriber can add restrictions on top of the agreement from the supplier
    such as specifying specific users who can use the software.
  - Angular 8 app that is served using a docker image with nginx web server.


.. toctree::
       :maxdepth: 2

       user-guide-license-rtu-editor.rst
       dev-guide-license-rtu-editor.rst
       release-notes-license-rtu-editor.rst


Model Usage Tracking
--------------------

When you deploy a model using kubernetes the default profile will use
the ELK stack and report back usage of the model.

.. toctree::
       :maxdepth: 2

       Model Usage Tracking <model-usage-tracking.rst>


License Usage Management (LUM)
------------------------------

The license usage management (LUM) documentation is located here:
:doc:`License Usage Manager<../../license-usage-manager/docs/index>`

License Server Admin guide
--------------------------

.. toctree::
       :maxdepth: 2

       user-guide-admin-license-services.rst