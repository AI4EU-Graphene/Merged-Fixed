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

=================================
License Profile Editor User Guide
=================================


Authoring a License Profile
---------------------------

When you onboard a model or create a composite model on Acumos platform,
you can add a license profile to your model. You can also update the license
profile from the my models license profile tab before publishing in case things
change during the development process:

- Upload a license profile json file
- Select from default list of license profiles templates and modify
- Create a new license profile

When you modify or create a new license profile the portal will present
you with the license profile editor.

When creating your license profile, we require the following fields
to be specified:

1. License Key - Such as a SPDX License Identifier
2. License Name - Friendly license name not the license Identifier
3. Copyright year
4. Copyright Company
5. Copyright Suffix: for example, "All Rights Reserved."
6. Contact Name
7. Contact URL or place to find out how to purchase the model.
8. Contact email
9. Right to Use Required - yes or no -- if yes then
   a Right To Use agreement is required.

Examples:

New license profile:

.. image:: images/license-profile-editor-new.png

Modify an existing license profile:

.. image:: images/license-profile-editor-modify.png


Viewing the License Profile in portal
-------------------------------------

.. image:: images/view-license-in-portal.png


License Profile Examples
------------------------

We have 3 different examples of license profiles here:

- Company B Proprietary License Profile

.. literalinclude:: ../license-profile-editor/src/assets/examples/company-b-proprietary.json
   :language: json

- Vendor A OSS License Profile

.. literalinclude:: ../license-profile-editor/src/assets/examples/vendor-a-oss.json
   :language: json

- Apache 2.0 License Profile

.. literalinclude:: ../license-profile-editor/src/assets/examples/apache-2.0-company-a.json
   :language: json

Using the Portal UI, You can select and modify these examples as per your
licensing requirements.

License Profile Json Schema 1.0.0
---------------------------------

The schema for the license profile:

.. literalinclude:: ../license-manager-client-library/src/main/resources/schema/1.0.0/license-profile.json
   :language: json

Admin: Add new or update license profile templates
--------------------------------------------------
As a Platform Operator, you can configure different license profile templates
- making it easy for the onboarding of a model with a license profile.

Prerequisite: Must have access to Common data service swagger.

Steps:

1. get admin CDS user Id (not user name)
2. POST /lic/templ

From the CDS swagger find the license controller

.. image:: images/license-controller-cds.png

Example payload creating a new license profile template for Company Z

.. code-block:: json

  {
    "priority": 10,
    "template": "{\"$schema\":\"https://raw.githubusercontent.com/acumos/license-manager/master/license-manager-client-library/src/main/resources/schema/1.0.0/license-profile.json\",\"keyword\":\"Company-Z-Commercial\",\"licenseName\":\"Company Z Commmercial License\",\"copyright\":{\"year\":2019,\"company\":\"Company Z\",\"suffix\":\"All Rights Reserved\"},\"softwareType\":\"Machine Learning Model\",\"companyName\":\"Company Z\",\"contact\":{\"name\":\"Company Z Team Member\",\"URL\":\"http://Company-Z.com\",\"email\":\"support@Company-Z.com\"},\"additionalInfo\":\"http://Company-Z.com/licenses/Company-Z-Commercial\",\"rtuRequired\":true}",
    "templateId": 4,
    "templateName": "Company Z License",
    "userId": "12345678-abcd-90ab-cdef-1234567890ab"
  }

The "priority" attribute would allow you to define order for
license profile templates entries.

3. Go back to Onboarding and notice new license profile template

.. image:: images/company-z-new-license-profile-template.png

4. If you have enabled security verification license checking then make sure
that you have the correct configuration for any new license profile keyword
that you introduce.
This is important if you have the security verification feature enabled in your
Acumos instance. For example if you added "Company Z Commercial" license
profile template, you also need to add to the SV scan code rules

:doc:`License Scanning setup<../../security-verification/security-verification-service/docs/user-guide>`

