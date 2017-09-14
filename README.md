# rest-demo
This is demo Spring boot application with JAX-RS rest service.
It runs on port 8080.  
So, respoints are under http://localhost:8080/v1/accounts/account


## The RESTful API -
- /v1/accounts/account     // to create an account, JSON input, POST
- /v1/accounts/account/account_id      // to get details of an account, GET
- /v1/accounts/account/account_id      // to update an account, PATCH
- /v1/accounts/account         // list all the account, GET
