package com.ygy.tcc.alibaba.dubbo.filter;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.ygy.tcc.alibaba.dubbo.constants.TccDubboConstants;
import com.ygy.tcc.core.holder.TccHolder;
import com.ygy.tcc.core.participant.TccPropagationContext;
import com.ygy.tcc.core.util.GsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Activate(group = {"provider"}, order = 1)
public class TccPropagationContextFilter implements Filter {


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        TccPropagationContext propagationContext = getTccPropagationContextFromDubbo();
        try {
            if (propagationContext != null) {
                TccHolder.bindPropagationContext(propagationContext);
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

    private TccPropagationContext getTccPropagationContextFromDubbo() {
        String attachment = RpcContext.getContext().getAttachment(TccDubboConstants.TCC_PROPAGATION_CONTEXT_DUBBO_KEY);
        if (StringUtils.isNotEmpty(attachment)) {
            return GsonUtil.fromJson(attachment, TccPropagationContext.class);
        }
        return null;
    }

}
