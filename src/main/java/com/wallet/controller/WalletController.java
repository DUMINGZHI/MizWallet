package com.wallet.controller;

import com.wallet.model.GasEstimationRequest;
import com.wallet.model.MnemonicRequest;
import com.wallet.model.TransactionRequest;
import com.wallet.model.Wallet;
import com.wallet.service.Web3jService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.WalletUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@RestController
public class WalletController {

    @Autowired
    Web3jService ethereumService;

    // 获取指定地址的余额（单位：ETH）
    @GetMapping("/walletBalance/{wid}")
    public Wallet getWalletBalance(@PathVariable String wid) {
        try {
            return ethereumService.getWalletBalance(wid);  // 调用服务层方法
        } catch (Exception e) {
            throw new RuntimeException("Error fetching balance: " + e.getMessage(), e);
        }
    }

    @GetMapping("/checkLoginStatus/{wid}")
    public Boolean checkLoginStatus(@PathVariable String wid) {
        try {
            return ethereumService.checkLoginStatus(wid);  // 调用服务层方法
        } catch (Exception e) {
            throw new RuntimeException("Error check login status: " + e.getMessage(), e);
        }
    }

    @GetMapping("/heartbeat/{wid}")
    public Boolean heartbeat(@PathVariable String wid) {
        try {
            return ethereumService.heartbeat(wid);  // 调用服务层方法
        } catch (Exception e) {
            throw new RuntimeException("Error check login status: " + e.getMessage(), e);
        }
    }


//    @GetMapping("/isValidAddress/{address}")
//    public boolean getWalletBalance(@PathVariable String address) {
//        try {
//            return WalletUtils.isValidAddress(address);
//        } catch (Exception e) {
//            throw new RuntimeException("Error check valid address: " + e.getMessage(), e);
//        }
//    }

//    @GetMapping("/checkGas")
//    public boolean checkGas() {
//        try {
//            return ethereumService.checkGas();
//        } catch (Exception e) {
//            throw new RuntimeException("Error check valid address: " + e.getMessage(), e);
//        }
//    }

    // 获取指定地址的余额（单位：ETH）
    @GetMapping("/balance/{address}")
    public BigDecimal getBalance(@PathVariable String address) {
        try {
            return ethereumService.getBalance(address);  // 调用服务层方法
        } catch (Exception e) {
            throw new RuntimeException("Error fetching balance: " + e.getMessage(), e);
        }
    }

    // 生成助记词
    @GetMapping("/generateMnemonic")
    public String generateMnemonic() {
        try {
            return ethereumService.generateMnemonic();  // 调用服务层方法生成助记词
        } catch (Exception e) {
            throw new RuntimeException("Error generating mnemonic: " + e.getMessage(), e);
        }
    }

    // 基于助记词生成或恢复钱包
    @PostMapping("/generateOrRecoverWallet")
    public Wallet generateOrRecoverWalletFromMnemonic(@RequestBody String mnemonic) {
        try {
            log.debug("Wallet generated successfully!");
            return ethereumService.generateOrRecoverWalletFromMnemonic(mnemonic);  // 调用服务层方法
        } catch (Exception e) {
            throw new RuntimeException("Error generating or recovering wallet: " + e.getMessage(), e);
        }
    }

    // 从助记词获取私钥
    @PostMapping("/getPrivateKeyFromMnemonic")
    public String getPrivateKeyFromMnemonic(@RequestBody String mnemonic) {
        try {
            return ethereumService.getPrivateKeyFromMnemonic(mnemonic);  // 调用服务层方法获取私钥
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving private key: " + e.getMessage(), e);
        }
    }

    // 使用私钥导入钱包
    @PostMapping("/importWalletByPrivateKey")
    public String importWalletByPrivateKey(@RequestBody String privateKey) {
        try {
            return ethereumService.importWalletByPrivateKey(privateKey.replaceAll("^\"|\"$", ""));  // 调用服务层方法导入钱包
        } catch (Exception e) {
            throw new RuntimeException("Error importing wallet by private key: " + e.getMessage(), e);
        }
    }

    // 使用助记词导入钱包
    @PostMapping("/importWalletByMnemonic")
    public String importWalletByMnemonic(@RequestBody MnemonicRequest mnemonicRequest) {
        try {
            // 通过 JSON 传递助记词和密码
            return ethereumService.importWalletByMnemonic(mnemonicRequest.getMnemonic(), mnemonicRequest.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Error importing wallet by mnemonic: " + e.getMessage(), e);
        }
    }

    // 获取当前的 Gas Price
    @GetMapping("/gasPrice")
    public BigDecimal getGasPrice() {
        try {
            return ethereumService.getGasPrice();  // 调用服务层获取 Gas 价格
        } catch (Exception e) {
            throw new RuntimeException("Error fetching gas price: " + e.getMessage(), e);
        }
    }

    // 估算 Gas Limit
    @PostMapping("/estimateGas")
    public BigInteger estimateGasLimit(@RequestBody GasEstimationRequest request) {
        try {
            return ethereumService.estimateGasLimit(request.getFromAddress(), request.getToAddress(), request.getValue());
        } catch (Exception e) {
            throw new RuntimeException("Error estimating gas limit: " + e.getMessage(), e);
        }
    }

    // 执行转账
    @PostMapping("/sendTransaction")
    public String sendTransaction(@RequestBody TransactionRequest request) {
        try {
            BigInteger gasLimit = ethereumService.estimateGasLimit(request.getFromAddress(), request.getToAddress(), request.getValue());
            ethereumService.sendTransaction(request.getXid(), request.getToAddress(), request.getValue(), gasLimit);
            return "Transaction sent successfully!";
        } catch (Exception e) {
            throw new RuntimeException("Error sending transaction: " + e.getMessage(), e);
        }
    }
}
