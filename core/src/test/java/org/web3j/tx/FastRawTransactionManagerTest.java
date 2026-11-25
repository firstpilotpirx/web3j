/*
 * Copyright 2024 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.tx;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.web3j.crypto.SampleKeys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.service.TxSignService;
import org.web3j.service.TxSignServiceImpl;
import org.web3j.tx.response.TransactionReceiptProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FastRawTransactionManagerTest {
    private static final String TRANSACTION_HASH = "0xHASH";

    private Web3j web3j;
    private FastRawTransactionManager fastRawTransactionManager;

    @BeforeEach
    public void setUp() throws Exception {
        web3j = mock(Web3j.class);
        fastRawTransactionManager = new FastRawTransactionManager(web3j, SampleKeys.CREDENTIALS);
    }

    @Test
    void clearNonce() {
        fastRawTransactionManager.setNonce(BigInteger.valueOf(42));

        fastRawTransactionManager.clearNonce();

        BigInteger currentNonce = fastRawTransactionManager.getCurrentNonce();
        assertEquals(currentNonce, BigInteger.valueOf(-1));
    }

    @Test
    void txSignServiceWithTransactionReceiptProcessor() throws Exception {
        TransactionReceipt transactionReceipt = new TransactionReceipt();
        TransactionReceiptProcessor transactionReceiptProcessor =
                mock(TransactionReceiptProcessor.class);
        when(transactionReceiptProcessor.waitForTransactionReceipt(TRANSACTION_HASH))
                .thenReturn(transactionReceipt);
        TxSignService txSignService = new TxSignServiceImpl(SampleKeys.CREDENTIALS);
        EthSendTransaction ethSendTransaction = new EthSendTransaction();
        ethSendTransaction.setResult(TRANSACTION_HASH);

        FastRawTransactionManager transactionManager =
                new FastRawTransactionManager(
                        web3j, txSignService, ChainIdLong.NONE, transactionReceiptProcessor);

        assertEquals(transactionReceipt, transactionManager.processResponse(ethSendTransaction));
        assertEquals(SampleKeys.ADDRESS, transactionManager.getFromAddress());
        assertEquals(BigInteger.valueOf(-1), transactionManager.getCurrentNonce());
    }
}
