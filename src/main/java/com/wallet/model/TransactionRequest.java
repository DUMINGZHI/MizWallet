package com.wallet.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TransactionRequest {

    public String xid;

    public BigInteger value;

    public String fromAddress;

    public String toAddress;
}
