#!/bin/bash
# ===============LICENSE_START=======================================================
# Acumos Apache-2.0
# ===================================================================================
# Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
# ===================================================================================
# This Acumos software file is distributed by AT&T and Tech Mahindra
# under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# This file is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ===============LICENSE_END=========================================================
#
# What this is: Utility to delete a soltiuon via the Acumos common-dataservice API.
#
# Prerequisites:
# - Acumos deployed via oneclick_deploy.sh, and acumos_env.sh as updated by it
#
#. Usage:
#. $ cd system-integration/tests
#. $ bash delete_solution.sh <solution_id>
#.   solution_id: unique id of the solution
#

function fail() {
  reason="$1"
  fname=$(caller 0 | awk '{print $2}')
  fline=$(caller 0 | awk '{print $1}')
  if [[ "$1" == "" ]]; then reason="unknown failure at $fname $fline"; fi
  log "$reason"
  exit 1
}

function log() {
  setx=${-//[^x]/}
  set +x
  fname=$(caller 0 | awk '{print $2}')
  fline=$(caller 0 | awk '{print $1}')
  echo; echo "$fname:$fline ($(date)) $1"
  if [[ -n "$setx" ]]; then set -x; else set +x; fi
}


function delete_solution() {
  trap 'fail' ERR
  if [[ "$solution_id" != "" ]]; then
    log "delete solution $solution_id"
    curl -u $ACUMOS_CDS_USER:$ACUMOS_CDS_PASSWORD -X DELETE \
      $cds_baseurl/solution/$solution_id
    log "delete solution finished"
  else
    log "nothing todo: solution_id is empty"
  fi
}

set -x
trap 'fail' ERR

if [[ $# -eq 1 ]]; then
  solution_id=$1
  export AIO_ROOT=$HOME/system-integration/AIO
  source $AIO_ROOT/utils.sh
  source $AIO_ROOT/acumos_env.sh
  cds_baseurl="-k https://$ACUMOS_DOMAIN/ccds"
  check_name_resolves cds-service
  if [[ "$NAME_RESOLVES" == "true" ]]; then
    cds_baseurl="http://cds-service:8000/ccds"
  fi
  delete_solution
else
  grep '#. ' $0 | sed 's/#.//g'
fi

