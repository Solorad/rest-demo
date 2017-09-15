package com.example.morenkov.rest;


import com.example.morenkov.exception.AccountStoreException;
import com.example.morenkov.pojo.Account;
import com.example.morenkov.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Optional;

import static javax.ws.rs.core.Response.ok;

/**
 * I would prefer concrete Spring Boot way with @RequestMapping annotation with method
 * instead of @Path, but it was written clear in task: use javax.ws.rs annotations.
 */
@Path("/v1/accounts/account")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestService {
    private static final Logger log = LoggerFactory.getLogger(AccountRestService.class);

    private final AccountService accountService;

    @Autowired
    public AccountRestService(AccountService accountService) {
        this.accountService = accountService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        log.info("getAll method started");
        return ok(accountService.getAll()).build();
    }


    @GET
    @Path("/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("accountId") String accountId) {
        log.info("getAccount '{}' method started", accountId);
        Optional<Account> accountOptional = accountService.getAccountById(accountId);
        return accountOptional.map(account -> ok(account).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).entity("account was not found").build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAccount(Account account) {
        try {
            log.info("createAccount '{}' method started", account);
            account = accountService.createAccount(account);
            return ok(account).build();
        } catch (AccountStoreException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Request body").build();
        }
    }

    @PATCH
    @Path("/{accountId}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public Account updateAccount(@PathParam("accountId") String accountId, Account account)
            throws AccountStoreException {
        log.info("updateAccount for id = '{}' with data = '{}' method started", accountId, account);
        return accountService.updateAccount(accountId, account);
    }
}
