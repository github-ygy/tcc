package com.ygy.tcc.core.recovery;



import com.ygy.tcc.core.TccTransaction;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TccRecoveryLoadResult implements Serializable {

    private List<TccTransaction> needRecoverTransactions;

    private String nextCursor;

    public TccRecoveryLoadResult(List<TccTransaction> needRecoverTransactions, String nextCursor) {
        this.needRecoverTransactions = needRecoverTransactions;
        this.nextCursor = nextCursor;
    }

}
