package com.example.morenkov.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Currency;

public class Account {
    // UUID was selected as a account id
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    // currency type may be changed to enum later
    @JsonProperty
    private Currency currency;
    @JsonProperty
    private BigDecimal balance;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", currency=" + currency +
               ", balance=" + balance +
               '}';
    }

    public static AccountBuilder newBuilder() {
        return new Account().new AccountBuilder();
    }

    public class AccountBuilder {
        private AccountBuilder() {
        }

        public AccountBuilder setId(String id) {
            Account.this.id = id;
            return this;
        }

        public AccountBuilder setName(String name) {
            Account.this.name = name;
            return this;
        }

        public AccountBuilder setCurrency(Currency currency) {
            Account.this.currency = currency;
            return this;
        }

        public AccountBuilder setBalance(BigDecimal balance) {
            Account.this.balance = balance;
            return this;
        }

        public Account build() {
            return Account.this;
        }
    }
}
