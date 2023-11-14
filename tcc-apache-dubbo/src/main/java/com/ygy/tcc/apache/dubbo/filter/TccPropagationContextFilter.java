package com.ygy.tcc.apache.dubbo.filter;

import com.ygy.tcc.apache.dubbo.constants.TccDubboConstants;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.participant.TccParticipantHookManager;
import com.ygy.tcc.core.participant.TccPropagationContext;
import com.ygy.tcc.core.util.GsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Objects;

@Activate(group = {CommonConstants.PROVIDER}, order = 1)
public class TccPropagationContextFilter implements Filter {


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        TccPropagationContext propagationContext = getTccPropagationContextFromDubbo(invocation);
        try {
            if (propagationContext != null) {
                TccHolder.bindPropagationContext(propagationContext);
                TccParticipantHookManager.doParticipantHook(propagationContext);
            }
            return invoker.invoke(invocation);
        } finally {
            if (propagationContext != null) {
                TccPropagationContext currentContext = TccHolder.getPropagationContext();
                if (currentContext != null && Objects.equals(currentContext.getTccId(), propagationContext.getTccId())) {
                    TccHolder.clearPropagationContext();
                }
            }
        }
    }

    private TccPropagationContext getTccPropagationContextFromDubbo(Invocation invocation) {
        String attachment = invocation.getAttachment(TccDubboConstants.TCC_PROPAGATION_CONTEXT_DUBBO_KEY);
        if (StringUtils.isNotEmpty(attachment)) {
            return GsonUtil.fromJson(attachment, TccPropagationContext.class);
        }
        return null;
    }

}
