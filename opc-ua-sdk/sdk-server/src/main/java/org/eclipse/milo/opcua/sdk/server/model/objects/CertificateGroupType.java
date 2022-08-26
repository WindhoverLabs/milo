package org.eclipse.milo.opcua.sdk.server.model.objects;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.core.nodes.MethodNode;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.api.methods.Out;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyType;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.eclipse.milo.opcua.stack.core.util.Lazy;

public interface CertificateGroupType extends BaseObjectType {
    QualifiedProperty<NodeId[]> CERTIFICATE_TYPES = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "CertificateTypes",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17"),
        1,
        NodeId[].class
    );

    NodeId[] getCertificateTypes();

    void setCertificateTypes(NodeId[] value);

    PropertyType getCertificateTypesNode();

    TrustListType getTrustListNode();

    MethodNode getGetRejectedListMethodNode();

    CertificateExpirationAlarmType getCertificateExpiredNode();

    TrustListOutOfDateAlarmType getTrustListOutOfDateNode();

    abstract class GetRejectedListMethod extends AbstractMethodInvocationHandler {
        private final Lazy<Argument[]> outputArguments = new Lazy<>();

        public GetRejectedListMethod(UaMethodNode node) {
            super(node);
        }

        @Override
        public Argument[] getInputArguments() {
            return new Argument[]{};
        }

        @Override
        public Argument[] getOutputArguments() {
            return outputArguments.getOrCompute(() -> {
                NamespaceTable namespaceTable = getNode().getNodeContext().getNamespaceTable();

                return new Argument[]{
                    new Argument("Certificates", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=15").toNodeId(namespaceTable).orElseThrow(), 1, new UInteger[]{UInteger.valueOf(0)}, new LocalizedText("", ""))
                };
            });
        }

        @Override
        protected Variant[] invoke(InvocationContext context,
                                   Variant[] inputValues) throws UaException {
            Out<ByteString[]> certificates = new Out<>();
            invoke(context, certificates);
            return new Variant[]{new Variant(certificates.get())};
        }

        protected abstract void invoke(InvocationContext context,
                                       Out<ByteString[]> certificates) throws UaException;
    }
}