package org.eclipse.milo.opcua.sdk.server.model.objects;

import org.eclipse.milo.opcua.sdk.core.nodes.MethodNode;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.api.methods.Out;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.eclipse.milo.opcua.stack.core.types.structured.DataSetReaderDataType;
import org.eclipse.milo.opcua.stack.core.util.Lazy;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part14/9.1.6/#9.1.6.9">https://reference.opcfoundation.org/v105/Core/docs/Part14/9.1.6/#9.1.6.9</a>
 */
public interface ReaderGroupType extends PubSubGroupType {
    PubSubDiagnosticsReaderGroupType getDiagnosticsNode();

    ReaderGroupTransportType getTransportSettingsNode();

    ReaderGroupMessageType getMessageSettingsNode();

    MethodNode getAddDataSetReaderMethodNode();

    MethodNode getRemoveDataSetReaderMethodNode();

    abstract class AddDataSetReaderMethod extends AbstractMethodInvocationHandler {
        private final Lazy<Argument[]> inputArguments = new Lazy<>();

        private final Lazy<Argument[]> outputArguments = new Lazy<>();

        public AddDataSetReaderMethod(UaMethodNode node) {
            super(node);
        }

        @Override
        public Argument[] getInputArguments() {
            return inputArguments.getOrCompute(() -> {
                NamespaceTable namespaceTable = getNode().getNodeContext().getNamespaceTable();

                return new Argument[]{
                    new Argument("Configuration", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=15623").toNodeId(namespaceTable).orElseThrow(), -1, null, new LocalizedText("", ""))
                };
            });
        }

        @Override
        public Argument[] getOutputArguments() {
            return outputArguments.getOrCompute(() -> {
                NamespaceTable namespaceTable = getNode().getNodeContext().getNamespaceTable();

                return new Argument[]{
                    new Argument("DataSetReaderNodeId", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17").toNodeId(namespaceTable).orElseThrow(), -1, null, new LocalizedText("", ""))
                };
            });
        }

        @Override
        protected Variant[] invoke(InvocationContext context,
                                   Variant[] inputValues) throws UaException {
            DataSetReaderDataType configuration = (DataSetReaderDataType) inputValues[0].getValue();
            Out<NodeId> dataSetReaderNodeId = new Out<>();
            invoke(context, configuration, dataSetReaderNodeId);
            return new Variant[]{new Variant(dataSetReaderNodeId.get())};
        }

        protected abstract void invoke(InvocationContext context,
                                       DataSetReaderDataType configuration, Out<NodeId> dataSetReaderNodeId) throws UaException;
    }

    abstract class RemoveDataSetReaderMethod extends AbstractMethodInvocationHandler {
        private final Lazy<Argument[]> inputArguments = new Lazy<>();

        public RemoveDataSetReaderMethod(UaMethodNode node) {
            super(node);
        }

        @Override
        public Argument[] getInputArguments() {
            return inputArguments.getOrCompute(() -> {
                NamespaceTable namespaceTable = getNode().getNodeContext().getNamespaceTable();

                return new Argument[]{
                    new Argument("DataSetReaderNodeId", ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17").toNodeId(namespaceTable).orElseThrow(), -1, null, new LocalizedText("", ""))
                };
            });
        }

        @Override
        public Argument[] getOutputArguments() {
            return new Argument[]{};
        }

        @Override
        protected Variant[] invoke(InvocationContext context,
                                   Variant[] inputValues) throws UaException {
            NodeId dataSetReaderNodeId = (NodeId) inputValues[0].getValue();
            invoke(context, dataSetReaderNodeId);
            return new Variant[]{};
        }

        protected abstract void invoke(InvocationContext context,
                                       NodeId dataSetReaderNodeId) throws UaException;
    }
}
