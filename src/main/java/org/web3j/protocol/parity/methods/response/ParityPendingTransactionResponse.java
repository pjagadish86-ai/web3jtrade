package org.web3j.protocol.parity.methods.response;

import java.util.List;

import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.Transaction;

public class ParityPendingTransactionResponse extends Response<List<Transaction>> {
    public List<Transaction> getParitryPendingResult() {
        return getResult();
    }
}