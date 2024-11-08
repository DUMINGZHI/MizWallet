package com.wallet.model;

import lombok.Data;

import java.math.BigInteger;

@Data
public class GasEstimationRequest {

    public BigInteger value;

    public String fromAddress;

    public String toAddress;

}
