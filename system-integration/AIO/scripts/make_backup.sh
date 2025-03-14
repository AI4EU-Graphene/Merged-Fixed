#!/bin/bash

set -x
trap 'fail' ERR

export AIO_ROOT=$HOME/system-integration/AIO
source $AIO_ROOT/utils.sh
source $AIO_ROOT/acumos_env.sh

export TODAY=$(date +"%a")
export TARGET_BDIR=$HOME/backups/$TODAY
export BDIR=$HOME/backups/current
mkdir -p $BDIR
cd $BDIR
cp $AIO_ROOT/nexus_env.sh .


mysqldump -h $ACUMOS_MARIADB_DOMAIN -P $ACUMOS_MARIADB_NODEPORT --user=root --password=$ACUMOS_MARIADB_PASSWORD  $ACUMOS_CDS_DB > acumosdb.sql

export NEXUS_DATA_DIR=/mnt/acumos/$(kubectl -n $ACUMOS_NEXUS_NAMESPACE get pvc | tail -1 | sed 's/ \+/ /g' | cut -d' ' -f3)
cd $NEXUS_DATA_DIR
tar -czf $BDIR/nexus-data.tgz --exclude=cache --exclude=tmp --exclude=log --exclude=javaprefs .

cd $HOME
rm -rf $TARGET_BDIR
mv $BDIR $TARGET_BDIR
