package mediator;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.MessageHelper;

/**
 * Created by chanaka on 2/28/18.
 */
public class TestMediator extends AbstractMediator {


    private static final Log log = LogFactory.getLog(TestMediator.class);

    private int limit = 1; // default value set to 1

    public TestMediator(){}

    @Override
    public boolean mediate(MessageContext messageContext) {
        String count = (String) messageContext.getProperty("count");
        MessageContext newCtx;
        //int limit = 3;
        for (int i = 0; i < Integer.parseInt(count); ) {
            try {
                newCtx = MessageHelper.cloneMessageContext(messageContext);
                newCtx.setProperty("startValue", i);
                newCtx.setProperty("limitValue", limit);
                messageContext.getSequence("conf:dblookupsequence").mediate(newCtx);
                i = i + limit;
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            }

        }
        return true;
    }

    public String getType() {
        return null;
    }

    public void setTraceState(int traceState) {
        traceState = 0;
    }

    public int getTraceState() {
        return 0;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
