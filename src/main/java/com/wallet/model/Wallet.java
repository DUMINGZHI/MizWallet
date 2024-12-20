package com.wallet.model;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class Wallet {

    public String address;

    public String privateKey;

    public String publicKey;

    public BigDecimal balance;

    public BigDecimal usdtBalance;
}
