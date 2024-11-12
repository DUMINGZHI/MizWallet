package com.wallet.service;

import com.wallet.model.Wallet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;


@Service
public class Web3jService {

    private final Web3j web3j;

    private Credentials user = null;

    private static final String ETH_TO_USDT_PRICE_FEED_CONTRACT = "0xEe9F2375b4bdF6387aa8265dD4FB8F16512A1d46";
    private static final String INFURA_URL = "http://141.147.153.178:8545";
    private static final BigDecimal WEI_IN_ETHER = new BigDecimal("1000000000000000000");

    // 从 application.properties 中读取以太坊节点的 URL
    public Web3jService(@Value("${ethereum.node.url}") String nodeUrl) {
        this.web3j = Web3j.build(new HttpService(nodeUrl));  // 连接到以太坊节点
    }

    // 获取指定地址的余额（单位：ETH）
    public BigDecimal getBalance(String address) throws Exception {
        EthGetBalance balance = web3j.ethGetBalance(address, org.web3j.protocol.core.DefaultBlockParameterName.LATEST).send();
        return Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER);  // 转换为 ETH 单位
    }

    // 生成助记词
    public String generateMnemonic() {
        try {
            // 使用安全随机数生成12个词的助记词
            SecureRandom secureRandom = new SecureRandom();
            byte[] seed = new byte[16];  // 16字节即128位
            secureRandom.nextBytes(seed);
            return MnemonicUtils.generateMnemonic(seed);
        } catch (Exception e) {
            throw new RuntimeException("生成助记词失败", e);
        }
    }

    // 基于助记词生成/恢复钱包
    public static Wallet generateOrRecoverWalletFromMnemonic(String mnemonic) {
        try {
            // 生成种子
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, "");

            // 使用种子生成 ECKeyPair（密钥对）
            ECKeyPair keyPair = ECKeyPair.create(seed);

            // 获取私钥
            String privateKey = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());

            // 获取公钥
            String publicKey = Numeric.toHexStringNoPrefix(keyPair.getPublicKey());

            // 从公钥生成钱包地址
            String walletAddress = "0x" + Keys.getAddress(keyPair);

            Wallet wallet = new Wallet();
            wallet.setPrivateKey(privateKey);
            wallet.setPublicKey(publicKey);
            wallet.setAddress(walletAddress);

            return wallet;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error generating wallet: " + e.getMessage());
        }
        return null;
    }

    // 从助记词生成私钥
    public String getPrivateKeyFromMnemonic(String mnemonic) {
        try {
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            ECKeyPair ecKeyPair = ECKeyPair.create(seed);
            return Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
        } catch (Exception e) {
            throw new RuntimeException("获取私钥失败", e);
        }
    }

    // 使用私钥导入钱包
    public String importWalletByPrivateKey(String privateKey) {
        user = Credentials.create(privateKey);
        return user.getAddress();
    }

    // 使用助记词导入钱包
    public String importWalletByMnemonic(String mnemonic, String password) throws Exception {
        // 使用助记词恢复钱包
        ECKeyPair keyPair = WalletUtils.loadBip39Credentials(password, mnemonic).getEcKeyPair();
        user = Credentials.create(keyPair);
        return user.getAddress();
    }

    // 获取当前的 Gas Price
    public BigDecimal getGasPrice() throws Exception {
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        BigInteger gasPriceWei = ethGasPrice.getGasPrice();  // 返回 Gas Price，单位为 Wei
        return Convert.fromWei(new BigDecimal(gasPriceWei), Convert.Unit.GWEI);  // 转换为 Gwei
    }

    // 估算 Gas Limit
    public BigInteger estimateGasLimit(String fromAddress, String toAddress, BigInteger value) throws Exception {
        // 这里创建一个交易对象，用来估算 Gas Limit
        org.web3j.protocol.core.methods.request.Transaction transaction =
                new org.web3j.protocol.core.methods.request.Transaction(
                        fromAddress,
                        null,  // nonce 会自动处理
                        null,  // Gas Price
                        null,  // Gas Limit
                        toAddress,
                        value,
                        null
                );

        EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
        return ethEstimateGas.getAmountUsed();  // 返回估算的 Gas Limit
    }

    // 执行转账
    public EthSendTransaction sendTransaction(String toAddress, BigInteger value, BigInteger gasLimit) throws Exception {
        Credentials credentials = user;

        // 获取当前 Gas Price
        BigDecimal gasPrice = getGasPrice();

        // 设置交易
        org.web3j.protocol.core.methods.request.Transaction transaction = new org.web3j.protocol.core.methods.request.Transaction(
                credentials.getAddress(),
                null,
                gasPrice.toBigInteger(),  // Gas Price
                gasLimit,                 // Gas Limit
                toAddress,
                value,
                null
        );

        // 发送交易
        EthSendTransaction response = web3j.ethSendTransaction(transaction).send();
        return response;
    }

    // 关闭连接
    public void shutdown() throws Exception {
        web3j.shutdown();
    }

    // 检查是否够支付 Gas
    public Boolean checkGas() throws Exception {
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        BigInteger gasPriceWei = ethGasPrice.getGasPrice();  // 返回 Gas Price，单位为 Wei
        BigDecimal gasBalance = Convert.fromWei(new BigDecimal(gasPriceWei), Convert.Unit.ETHER);
        BigDecimal walletBalance = Convert.fromWei(getWalletBalance().getBalance(), Convert.Unit.ETHER);

        return walletBalance.compareTo(gasBalance) > 0;
    }

    public Wallet getWalletBalance() throws Exception {
        Wallet wallet = new Wallet();
        String address = user.getAddress();
        wallet.setAddress(address);
        EthGetBalance balance = web3j.ethGetBalance(address, org.web3j.protocol.core.DefaultBlockParameterName.LATEST).send();
        wallet.setBalance(Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER)); // 转换为 ETH 单位
        wallet.setUsdtBalance(wallet.getBalance().multiply(getEthToUsdtPrice()));
        return wallet;
    }

    public BigDecimal getEthToUsdtPrice() throws Exception {
        // 构造用于调用价格预言机合约的函数。
        Function function = new Function(
                "latestAnswer",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );

        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                        null, ETH_TO_USDT_PRICE_FEED_CONTRACT, encodedFunction
                ),
                DefaultBlockParameterName.LATEST
        ).send();

        if (response.hasError()) {
            throw new RuntimeException("Error fetching ETH to USDT price: " + response.getError().getMessage());
        }

        // 使用 FunctionReturnDecoder 解析返回值
        List<Type> decoded = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        BigInteger price = (BigInteger) decoded.get(0).getValue();

        // 假设价格精度为 8 位小数
        return new BigDecimal(price).divide(new BigDecimal("100000000"), 8, BigDecimal.ROUND_HALF_UP);
    }
}
