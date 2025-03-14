
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
Developer Guide - License Profile Editor
=============================================

About License Profile Editor
----------------------------

* License Profile Editor provides a UI editor to create
  license Profile and save / export in json format.

* The editor can be used as
   * Web component inline to your web page
   * iframe document launched from your web page

* For building and using as Web Component, refer
  license-manager/license-profile-editor/README.md.

* This document discusses how to build and load
  license profile editor as iframe document.


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
  ``cd license-profile-editor``

2. Install node modules

  ``npm install``

3. Build via this command:

   ``npm run build``

4. Command to build docker image:
   ``docker build -t acumos/license-profile-editor:<<VERSION>> .``

Portal UI Integration (docker-compose)
--------------------------------------

Following is the sample docker-compose yaml configuration
to include when building/running Portal UI service.

1. docker-compose yaml for license-profile-editor service

.. code-block:: yaml

    services:
    license-profile-editor-service:
        image: ${LICENSE_PROFILE_EDITOR_IMAGE}
        ports:
        - "8089:80"
        restart: on-failure

2. docker-compose yaml for portal-fe service

This is to link / declare dependency for starting containers
locally and ensure that the license-profile-editor container
has started before running the portal.

.. code-block:: yaml

    services:
        portal-fe-service:
            image: ${PORTAL_FE_IMAGE}
            ...
            links:
                - portal-be-service
                - license-profile-editor-service
            depends_on:
                - portal-be-service
                - license-profile-editor-service
        ...

License Profile Editor - iframe communication
---------------------------------------------

1. JavaScript code block to enable the Portal UI parent window
   to communicate with child License Profile Editor iframe window.

.. code-block:: javascript

    // add following code-block to Portal UI
    var iframe;
    // addEventListener and old browser support
    function bindEvent(element, eventName, eventHandler) {
        if (element.addEventListener) {
            element.addEventListener(eventName, eventHandler, false);
        } else if (element.attachEvent) {
            element.attachEvent('on' + eventName, eventHandler);
        }
    }
    function licenseProfileMsgListener(event) {
        // TODO check #3 below
    }
    // TODO - call this function onload of license-profile-editor-iframe
    function initOnLoad() {
        iframe = document.getElementById('license-profile-editor-iframe');
        bindEvent(window, 'message', licenseProfileMsgListener);
    }

2. Protocol to send License Profile Template.

.. code-block:: javascript

    // add following code-block to Portal UI
    function sendMessage(msgObj) {
        var val = // must be VALID JSON value

        // outgoing message object has key and value properties
        //   - "key": "input" - identifies input data
        //   - "value": val - must be VALID JSON value
        //              { } - empty JSON to clear editor selection
        var msgObj = {
            "key": "input",
            "value": val
        };
        iframe.contentWindow.postMessage(msgObj, '*');
    }

3. Protocol to receive License Profile JSON.

.. code-block:: javascript

    // add following code-block to Portal UI
    function licenseProfileMsgListener(event) {
        // message listener
        if (event.data.key === 'output') {

            // incoming event.data object has key and value properties
            //   - "key": "output" - identifies output from license profile editor iframe
            //   - "value": JSON data value

        } else if (event.data.key === 'action') {

            // incoming event.data object has key and value properties
            //   - "key": "action" - identifies action request from license profile editor iframe
            //   - "value":
            //      - possible value(s) 'cancel'

            if (event.data.value === 'cancel') {
                // TODO decide what to do on CANCEL
            }
        }
    }

4. Sample HTML iframe code block.

.. code-block:: html

    <iframe id="license-profile-editor-iframe"
            src="http://<<HOST>>:8089/license-profile-editor/index.html?mode=iframe"
            frameborder="0" style="width: 100%; height: 100%;"
            onload="initOnLoad();"></iframe>


