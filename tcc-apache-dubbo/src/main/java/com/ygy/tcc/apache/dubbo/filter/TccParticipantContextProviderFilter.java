package com.ygy.tcc.apache.dubbo.filter;

import com.ygy.tcc.apache.dubbo.constants.TccDubboConstants;
import com.ygy.tcc.core.enums.TccParticipantStatus;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.participant.TccParticipantContext;
import com.ygy.tcc.core.util.GsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Objects;

@Activate(group = {CommonConstants.PROVIDER}, order = 1)
public class TccParticipantContextProviderFilter implements Filter {


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        TccParticipantContext participantContext = getTccParticipantContextFromDubbo();
        try {
            if (participantContext != null && Objects.equals(participantContext.getStatus(), TccParticipantStatus.TRYING)) {
                TccHolder.bindParticipantContext(participantContext);
            }
            return invoker.invoke(invocation);
        }finally {
            if (participantContext != null) {
                TccParticipantContext currentContext = TccHolder.getParticipantContext();
                if (currentContext != null && Objects.equals(currentContext.getParticipantId(),participantContext.getParticipantId())) {
                    TccHolder.clearParticipantContext();
                }
            }
        }
    }

    private TccParticipantContext getTccParticipantContextFromDubbo() {
        String attachment = RpcContext.getServerContext().getAttachment(TccDubboConstants.TCC_PARTICIPANT_CONTEXT_KEY);
        if (StringUtils.isNotEmpty(attachment)) {
            return GsonUtil.fromJson(attachment, TccParticipantContext.class);
        }
        return null;
    }

}
