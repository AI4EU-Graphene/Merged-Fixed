.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
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

===================================
Acumos Nexus Client Developer Guide
===================================

The Acumos Nexus Client is a Java library that facilitates download and upload of artifacts
from/to a Nexus repository.

Usage Example
-------------

The following code excerpt shows a test case of the get-artifact feature::

    import org.acumos.nexus.client.NexusArtifactClient;
    import org.acumos.nexus.client.RepositoryLocation;

    ..

    public void testGetArtifact() throws Exception {
        NexusArtifactClient artifactClient = new NexusArtifactClient(
                new RepositoryLocation("0", REPO_URL, REPO_USER, REPO_PASS, null));
        ByteArrayOutputStream outputStream = artifactClient.getArtifact(artifactPath);
    }
