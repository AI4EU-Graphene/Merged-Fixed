<!---
.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
..      http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================
-->

# AI4EU Experiments Installation

This repository has been moved to https://gitlab.eclipse.org/eclipse/graphene/eclipse-graphene

This repository holds installation and deployment scripts for the AI4EU Acumos system.

Software versions:
* Kubernetes 1.19.16
* Kube CNI 0.8.7
* Kubernetes dashboard 2.3.1
* Calico 3.16
* Helm 3.6.3

Preconditions:
* Ubuntu 20.04 server, one big partition
* Docker (version string: 5:19.03.15-ubuntu-focal) installed and docker service enabled and started 
* At least 20GB of disk space available recommended (/var/lib alone will require more than 10GB)
* Installation user created that belongs to groups docker and sudo, in this example the user is ai4eu
* /etc/hosts has exactly one entry for the FQHN pointing to the externel ipv4 interface
* optionally letsencrypt certificates installed

Become user ai4eu (installation user)

Clone this repo and then:

    # Use the installation user without sudo (you will be asked for sudo if neccesary).
    cd system-integration/tools
    bash setup_k8s.sh 
    bash setup_helm.sh
    cd $HOME
    # replace FQHN appropriately
    bash system-integration/AIO/setup_prereqs.sh k8s FQHN $USER generic | tee log_prereqs.txt
    cd system-integration/AIO/
    bash oneclick_deploy.sh | tee log_oneclick.txt

Some of those scripts might take several minutes to complete execution.
The last script should end with output showing the URLs to use, e.g. among others:

    Portal: https://(your FQHN):443
