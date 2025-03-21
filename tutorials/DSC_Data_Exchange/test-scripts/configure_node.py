import sys
import requests
import pprint

from connectorAPI.idsapi import IdsApi

node_rest_server = "http://localhost:8062/api/v1"
consumer_url_searching = "https://localhost:8081"
provider_url_searching = "https://172.17.0.1:8080"
consumer_url_downloading = "https://172.17.0.1:8081"
provider_url_downloading = "https://172.17.0.1:8080/api/ids/data"

if __name__ == "__main__":
    argv = sys.argv[1:]
    if len(argv) > 0:
        node_rest_server = argv[0] + "/api/v1"
        print("Setting flask_server as:", node_rest_server)
    if len(argv) > 1:
        consumer_url_searching = argv[1]
        print("Setting consumer_url as:", consumer_url_searching)
    if len(argv) > 2:
        provider_url_searching = argv[2]
        print("Setting provider_url as:", provider_url_searching)
    if len(argv) > 3:
        consumer_url_downloading = argv[3]
        print("Setting customDSC as:", consumer_url_downloading)
    if len(argv) > 4:
        provider_url_downloading = argv[4]
        print("Setting recipient as:", provider_url_downloading)


# Suppress ssl verification warning
requests.packages.urllib3.disable_warnings()

# Consumer
consumer = IdsApi(consumer_url_searching)

# IDS
# Call description
provider_description = consumer.descriptionRequest(provider_url_searching + "/api/ids/data", "")
# pprint.pprint(provider_description)
catalog = provider_description["ids:resourceCatalog"][0]
catalog_description = consumer.descriptionRequest(provider_url_searching + "/api/ids/data", catalog["@id"])
# pprint.pprint(catalog_description)
resource = catalog_description["ids:offeredResource"][0]
resource_description = consumer.descriptionRequest(provider_url_searching + "/api/ids/data", resource["@id"])
# pprint.pprint(resource_description)
representation = resource_description["ids:representation"][0]
representation_description = consumer.descriptionRequest(provider_url_searching + "/api/ids/data", representation["@id"])
# pprint.pprint(representation_description)
artifact = representation_description["ids:instance"][0]
artifact_description = consumer.descriptionRequest(provider_url_searching + "/api/ids/data", artifact["@id"])
# pprint.pprint(artifact_description)

contract_offers = resource["ids:contractOffer"][0]
contract = contract_offers["ids:permission"][0]
contract["ids:target"] = artifact["@id"]

session = requests.Session()

print("sending configs to server")
print("sending recipient")
response = session.post(node_rest_server + "/recipient", json={
    "recipient": provider_url_downloading
})
pprint.pprint(response)
print("----------")
print("sending artifactId")
response = session.post(node_rest_server + "/artifactId", json={
    "artifactId": artifact["@id"]
})
pprint.pprint(response)
print("----------")
print("sending resourceId")
response = session.post(node_rest_server + "/resourceId", json={
    "resourceId": resource["@id"]
})
pprint.pprint(response)
print("----------")
print("sending contract")
response = session.post(node_rest_server + "/contract", json={
    "contract": contract
})
pprint.pprint(response)
print("----------")
print("sending useCustomDSC")
response = session.post(node_rest_server + "/useCustomDSC", json={
    "useCustomDSC": True
})
pprint.pprint(response)
print("----------")
print("sending customDSC")
response = session.post(node_rest_server + "/customDSC", json={
    "customDSC": consumer_url_downloading
})
pprint.pprint(response)
print("----------")

data_response = session.get(node_rest_server + "/data")

pprint.pprint(data_response.content)
