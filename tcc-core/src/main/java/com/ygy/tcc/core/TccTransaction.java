package com.ygy.tcc.core;

import com.google.common.collect.Lists;
import com.ygy.tcc.core.enums.TccStatus;
import com.ygy.tcc.core.enums.TransactionRole;
import lombok.Data;

import java.util.List;


@Data
public class TccTransaction {

    private String tccId;

    private TccStatus status;

    private TransactionRole role;

    private List<TccParticipant> participants = Lists.newArrayList();

    private int version;

    private long createTime;

    private long updateTime;


    public TccTransaction(TransactionRole role) {
        this.role = role;
    }

    public TccTransaction(String tccId, TccStatus status, TransactionRole role) {
        this.tccId = tccId;
        this.status = status;
        this.role = role;
    }

}
