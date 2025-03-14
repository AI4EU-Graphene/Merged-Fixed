#source ../acumos_env.sh
sudo cp /etc/letsencrypt/live/$ACUMOS_DOMAIN/privkey.pem acumos.key
sudo chown $USER:$USER acumos.key
sudo cp /etc/letsencrypt/live/$ACUMOS_DOMAIN/fullchain.pem acumos.crt
sudo chown $USER:$USER acumos.crt
rm -f *store*
sed -i '/STORE/d' cert_env.sh

KEYSTORE_PASSWORD=$(uuidgen)
echo "export KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD" >>cert_env.sh
openssl pkcs12 -export \
  -in $ACUMOS_CERT \
  -inkey $ACUMOS_CERT_KEY \
  -passin pass:$CERT_KEY_PASSWORD \
  -certfile $ACUMOS_CERT \
  -out $ACUMOS_CERT_PREFIX-keystore.p12 \
  -passout pass:$KEYSTORE_PASSWORD

TRUSTSTORE_PASSWORD=$(uuidgen)
echo "export TRUSTSTORE_PASSWORD=$TRUSTSTORE_PASSWORD" >>cert_env.sh
keytool -import \
  -file $ACUMOS_CERT \
  -alias $ACUMOS_CERT_PREFIX-ca \
  -keystore $ACUMOS_CERT_PREFIX-truststore.jks \
  -storepass $TRUSTSTORE_PASSWORD -noprompt

