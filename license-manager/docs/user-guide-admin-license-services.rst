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

===============================
License Server Admin User Guide
===============================


Introduction
------------

This administration guide walks an administrator/developer to be able to run the following 4 services by way of docker images:
  - license-profile-editor
  - license-rtu-editor
  - lum-server
  - lum-db

using either
  - docker compose
  - kubernetes (we use minikube for our example)
  - with the rest of Acumos using All in One tooling

Acumos AIO installation allows to setup above services based on kubernetes and
docker-compose.

This document provides instructions to explicitly install / run
above services using docker-compose.

Docker-compose Prerequisites
----------------------------

1. Be logged into the acumos docker registry:

  .. code-block:: shell

    docker login nexus3.acumos.org:10002

    user name: docker
    password: docker

Using Docker Compose - License Editors
--------------------------------------

1. For the license-profile-editor and license-rtu-editor, we have
   the following example docker compose file.


  .. code-block:: yaml

    version: "3"
    services:
      license-rtu-editor:
        # replace username/repo:tag with your name and image details
        image: nexus3.acumos.org:10002/acumos/license-rtu-editor:0.1.3
        ports:
          - "8092:8080"
      license-profile-editor:
        # replace username/repo:tag with your name and image details
        image: nexus3.acumos.org:10002/acumos/license-profile-editor:0.0.12
        ports:
          - "8093:8080"

Note:
  - You can change the 8092 and 8093 ports to match your available ports
on your host.
  - If you change the ports, then make sure that the portal-be >
    "license_profile" configuration refers to correct port.

2. Then run the following docker-compose command

  .. code-block:: shell

    docker-compose up

3. Each editor can be accessed using following URLs:

  License Profile editor:
  http://localhost:8093/

  License Right to Use (RTU) editor:
  http://localhost:8092/


Using Docker Compose - License Usage Manager
--------------------------------------------

1. We start with a lum_config.json

  .. code-block:: json

    {
      "lumServer": {
        "database": {
          "user": "lum-user",
          "host": "lum-database",
          "database": "lumdb",
          "port": 5432,
          "max": 10,
          "idleTimeoutMillis": 30000
        },
        "serverName": "lum-server",
        "maxTxRetryCount": 10
      }
    }

2. Create a .env file with the password for the database

  .. code-block:: json

    postgresqlPassword=lum-db-password
    postgresqlUser=lum-user


3. For the License Usage Manager services, we have the following
   example docker compose file.

  .. code-block:: yaml

    version: "3.4"
    services:
      lum-database:
        image: nexus3.acumos.org:10002/acumos/lum-db:0.28.1
        environment:
          POSTGRES_USER: ${postgresqlUser}
          POSTGRES_PASSWORD: ${postgresqlPassword}
        expose:
          - 5432
      lum-server:
        # replace username/repo:tag with your name and image details
        image: nexus3.acumos.org:10002/acumos/lum-server:0.28.1
        depends_on:
          - lum-database
        volumes:
          - ./lum_config.json:/opt/app/lum/etc/config.json
        environment:
          DATABASE_PASSWORD: ${postgresqlPassword}
        ports:
          - "2080:2080"


Note:
  - Make sure that the portal-be > "lum" configuration service url refers
    to 2080 port.

4. Then run the following docker-compose command

  .. code-block:: shell

    docker-compose up

5. LUM service provides an OpenApi web interface that shows list of LUM apis
   and provides an opportunity to test APIs using the web interface.
   To access the LUM OpenApi web interface, launch follwing URL:

  http://localhost:2080/ui/openapi/
