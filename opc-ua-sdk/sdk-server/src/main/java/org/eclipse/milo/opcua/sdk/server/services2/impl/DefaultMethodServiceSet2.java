package org.eclipse.milo.opcua.sdk.server.services2.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.milo.opcua.sdk.server.DiagnosticsContext;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.Session;
import org.eclipse.milo.opcua.sdk.server.api.services.MethodServices.CallContext;
import org.eclipse.milo.opcua.sdk.server.services2.MethodServiceSet2;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult;
import org.eclipse.milo.opcua.stack.core.types.structured.CallRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.ResponseHeader;
import org.eclipse.milo.opcua.stack.transport.server.ServiceRequestContext;

import static org.eclipse.milo.opcua.sdk.server.services2.AbstractServiceSet.createResponseHeader;
import static org.eclipse.milo.opcua.stack.core.util.FutureUtils.failedUaFuture;

public class DefaultMethodServiceSet2 implements MethodServiceSet2 {

    private final OpcUaServer server;

    public DefaultMethodServiceSet2(OpcUaServer server) {
        this.server = server;
    }

    @Override
    public CompletableFuture<CallResponse> onCall(ServiceRequestContext context, CallRequest request) {
        Session session;
        try {
            session = server.getSessionManager()
                .getSession(context, request.getRequestHeader());
        } catch (UaException e) {
            // TODO Session-less service invocation?
            return CompletableFuture.failedFuture(e);
        }

        List<CallMethodRequest> methodsToCall = List.of(request.getMethodsToCall());

        if (methodsToCall.isEmpty()) {
            return failedUaFuture(StatusCodes.Bad_NothingToDo);
        }

        if (methodsToCall.size() > server.getConfig().getLimits().getMaxNodesPerMethodCall().longValue()) {
            return failedUaFuture(StatusCodes.Bad_TooManyOperations);
        }

        var diagnosticsContext = new DiagnosticsContext<CallMethodRequest>();

        var callContext = new CallContext(
            server,
            session,
            diagnosticsContext
        );

        session.getSessionDiagnostics().getCallCount().record(callContext.getFuture());
        session.getSessionDiagnostics().getTotalRequestCount().record(callContext.getFuture());

        server.getAddressSpaceManager().call(callContext, methodsToCall);

        return callContext.getFuture().thenApply(values -> {
            ResponseHeader header = createResponseHeader(request);

            return new CallResponse(header, values.toArray(CallMethodResult[]::new), new DiagnosticInfo[0]);
        });
    }

}