#!/bin/bash

function setup_helm() {
  trap 'fail' ERR
  log "Setup helm"
  # Install Helm_v3
  wget https://get.helm.sh/helm-v3.6.3-linux-amd64.tar.gz #versions: https://github.com/helm/helm/releases
  tar -xvf helm-v3.6.3-linux-amd64.tar.gz
  sudo cp linux-amd64/helm /usr/local/bin/helm
}

set -x
trap 'fail' ERR
WORK_DIR=$(pwd)
cd $(dirname "$0")
export AIO_ROOT="$(cd ../AIO; pwd -P)"
source $AIO_ROOT/utils.sh
cd $WORK_DIR
verify_ubuntu_or_centos
setup_helm
log "Setup is complete."
