
.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2019 Nordix Foundation
.. ===================================================================================
.. This Acumos documentation file is distributed by Nordix Foundation
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
.. http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================

=============================================
Developer Guide - License RTU Editor
=============================================

About License RTU Editor
----------------------------

* License RTU Editor provides a UI editor to create
  right to use agreement and save / export in json format.

* The editor can be used as
   * Web component inline to your web page
   * iframe document launched from your web page

* For building and using as Web Component, refer
  license-manager/license-rtu-editor/README.md.


Build Prerequisites
-------------------

* Git Shell (https://git-for-windows.github.io/) or
  SourceTree (https://www.sourcetreeapp.com/) for Cloning
  & pushing the code changes.
* Proxy setup to download dependencies from open source repositories
* Open Source or GitShell Command Line Interface
* Docker

Build Instructions
-------------------

1. Browse to your preferred directory and run below commands:

  ``git clone "https://gerrit.acumos.org/r/license-manager"``
  ``cd license-rtu-editor``

2. Install node modules

  ``npm install``

3. Build via this command:

   ``npm run build``

4. Command to build docker image:
   ``docker build -t acumos/license-rtu-editor:<<VERSION>> .``
