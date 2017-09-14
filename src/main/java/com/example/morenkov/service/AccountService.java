package com.example.morenkov.service;

import com.example.morenkov.exception.AccountStoreException;
import com.example.morenkov.pojo.Account;
import com.example.morenkov.rest.AccountRestService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountRestService.class);

    private ObjectMapper mapper;
    private final Lock readLock;
    private final Lock writeLock;


    public AccountService() throws IOException {
        writeSchemaInFile();

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    public List<Account> getAll() {
        readLock.lock();
        try {
            return mapper.readValue(new File("data/accounts.json"), new TypeReference<List<Account>>() {
            });
        } catch (IOException e) {
            log.error("Exception occured during getAll read", e);
            return Collections.emptyList();
        } finally {
            readLock.unlock();
        }
    }

    public Optional<Account> getAccountById(String accountId) {
        readLock.lock();
        try {
            // well at first there was ConcurrentHashMap as a cache for (accountId,account) pairs.
            // But, getAccountById - is the only method where I can use Optional class.
            List<Account> savedAccounts = getAll();
            return getAccountInList(accountId, savedAccounts);
        } finally {
            readLock.unlock();
        }
    }

    public Account createAccount(Account account) throws AccountStoreException {
        if (account == null || StringUtils.isEmpty(account.getName())) {
            log.error("Error on account creation.");
            throw new AccountStoreException("Error on account creation.");
        }
        writeLock.lock();
        try {
            List<Account> savedAccounts = getAll();
            account.setId(UUID.randomUUID().toString());
            savedAccounts.add(account);
            rewriteFile(savedAccounts);
            return account;
        } finally {
            writeLock.unlock();
        }
    }

    public void updateAccount(String accountId, Account account) throws AccountStoreException {
        if (account == null || StringUtils.isEmpty(accountId)) {
            log.error("Error on account creation.");
            throw new AccountStoreException("Error on account update.");
        }
        writeLock.lock();
        try {
            List<Account> accounts = getAll();
            Optional<Account> storedAccountOptional = getAccountInList(accountId, accounts);
            if (!storedAccountOptional.isPresent()) {
                throw new AccountStoreException("Account with id " + accountId + " was not found");
            }
            Account storedAccount = storedAccountOptional.get();
            // I could easily update storedAccount and save list, but here is the only place where I can use
            // Account builder.
            accounts.remove(storedAccount);
            Account updatedAccount = Account.newBuilder()
                    .setId(storedAccount.getId())
                    .setName(isEmpty(account.getName()) ? storedAccount.getName() : account.getName())
                    .setCurrency(isEmpty(account.getCurrency()) ? storedAccount.getCurrency() : account.getCurrency())
                    .setBalance(isEmpty(account.getBalance()) ? storedAccount.getBalance() : account.getBalance())
                    .build();
            accounts.add(updatedAccount);
            rewriteFile(accounts);
        } finally {
            writeLock.unlock();
        }
    }


    private void writeSchemaInFile() throws IOException {
        mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        JsonSchema schema = schemaGen.generateSchema(Account[].class);
        File file = new File("data/metadata.json");

        mapper.writeValue(file, schema);
    }

    /**
     * Fully rewrite .json file with new content.
     *
     * @param savedAccounts
     * @throws IOException - log it, but do nothing.
     */
    private void rewriteFile(List<Account> savedAccounts) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("data/accounts.json"), savedAccounts);
        } catch (IOException e) {
            log.error("Exception occurred on file write", e);
        }
    }

    private Optional<Account> getAccountInList(String accountId, List<Account> savedAccounts) {
        return savedAccounts.stream().filter(a -> Objects.equals(a.getId(), accountId)).findAny();
    }
}
