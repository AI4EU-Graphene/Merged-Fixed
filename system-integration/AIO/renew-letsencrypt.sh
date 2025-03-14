
function clean_ingress() {

  if [[ $(helm uninstall -n ${ACUMOS_NAMESPACE} ${ACUMOS_NAMESPACE}-nginx-ingress) ]]; then
    echo "Helm release ${ACUMOS_NAMESPACE}-nginx-ingress uninstalled"
  fi
  if [[ $(kubectl get namespace -n $ACUMOS_NAMESPACE) ]]; then
    echo "Cleanup any ingress-related resources"
    if [[ $(kubectl delete secret -n $ACUMOS_NAMESPACE ingress-cert) ]]; then
      echo "Secret ingress-cert deleted"
    fi
    ings=$(kubectl get ingress -n $ACUMOS_NAMESPACE | awk '/-ingress/{print $1}')
    for ing in $ings; do
      kubectl delete ingress -n $ACUMOS_NAMESPACE $ing
    done
  fi
}

source acumos_env.sh
clean_ingress

# wait for cleaning of iptables rules 
sleep 15
echo '***************************************************'
sudo certbot renew -n --force-renewal
echo '***************************************************'

bash $AIO_ROOT/setup_keystore.sh
bash $AIO_ROOT/../charts/ingress/setup_ingress_controller.sh $ACUMOS_NAMESPACE $AIO_ROOT/certs/acumos.crt $AIO_ROOT/certs/acumos.key $ACUMOS_DOMAIN_IP
bash $AIO_ROOT/ingress/setup_ingress.sh
# make sure federation has the lastest certificates
bash $AIO_ROOT/redeploy_component.sh federation

