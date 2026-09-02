# Setup for remote Tests


## Generate keycloak offline token

* Assert that the user `unittest` exists
* 


``
curl \  
 -d "client_id=elexis-rcp-openid" -d "client_secret=hardcodedclientsecretfttb" \  
 -d "username=unittest" -d "password=<pass>" \
 -d "grant_type=password" \
 -d "scope=openid info offline_access" \   
 https://localhost:8080/auth/realms/demo/protocol/openid-connect/token | jq
 ```