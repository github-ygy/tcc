package com.ygy.tcc.core.recovery;



import com.ygy.tcc.core.repository.TccTransactionDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TccTransactionDoQueryResult implements Serializable {

    private List<TccTransactionDO> needRecoverTransactionDos;

    private String nextCursor;

    public TccTransactionDoQueryResult(List<TccTransactionDO> needRecoverTransactionDos, String nextCursor) {
        this.needRecoverTransactionDos = needRecoverTransactionDos;
        this.nextCursor = nextCursor;
    }


}
