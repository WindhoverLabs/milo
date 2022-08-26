package org.eclipse.milo.opcua.sdk.server.model.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.BaseDataVariableTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.FiniteStateVariableTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.FiniteTransitionVariableTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class FiniteStateMachineTypeNode extends StateMachineTypeNode implements FiniteStateMachineType {
    public FiniteStateMachineTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                                      LocalizedText displayName, LocalizedText description, UInteger writeMask,
                                      UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                      RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                      UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    public FiniteStateMachineTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                                      LocalizedText displayName, LocalizedText description, UInteger writeMask,
                                      UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                      RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions);
    }

    @Override
    public FiniteStateVariableTypeNode getCurrentStateNode() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "CurrentState");
        return (FiniteStateVariableTypeNode) component.orElse(null);
    }

    @Override
    public LocalizedText getCurrentState() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "CurrentState");
        return component.map(node -> (LocalizedText) node.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public void setCurrentState(LocalizedText value) {
        getVariableComponent("http://opcfoundation.org/UA/", "CurrentState").ifPresent(n -> n.setValue(new DataValue(new Variant(value))));
    }

    @Override
    public FiniteTransitionVariableTypeNode getLastTransitionNode() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "LastTransition");
        return (FiniteTransitionVariableTypeNode) component.orElse(null);
    }

    @Override
    public LocalizedText getLastTransition() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "LastTransition");
        return component.map(node -> (LocalizedText) node.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public void setLastTransition(LocalizedText value) {
        getVariableComponent("http://opcfoundation.org/UA/", "LastTransition").ifPresent(n -> n.setValue(new DataValue(new Variant(value))));
    }

    @Override
    public BaseDataVariableTypeNode getAvailableStatesNode() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "AvailableStates");
        return (BaseDataVariableTypeNode) component.orElse(null);
    }

    @Override
    public NodeId[] getAvailableStates() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "AvailableStates");
        return component.map(node -> (NodeId[]) node.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public void setAvailableStates(NodeId[] value) {
        getVariableComponent("http://opcfoundation.org/UA/", "AvailableStates").ifPresent(n -> n.setValue(new DataValue(new Variant(value))));
    }

    @Override
    public BaseDataVariableTypeNode getAvailableTransitionsNode() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "AvailableTransitions");
        return (BaseDataVariableTypeNode) component.orElse(null);
    }

    @Override
    public NodeId[] getAvailableTransitions() {
        Optional<VariableNode> component = getVariableComponent("http://opcfoundation.org/UA/", "AvailableTransitions");
        return component.map(node -> (NodeId[]) node.getValue().getValue().getValue()).orElse(null);
    }

    @Override
    public void setAvailableTransitions(NodeId[] value) {
        getVariableComponent("http://opcfoundation.org/UA/", "AvailableTransitions").ifPresent(n -> n.setValue(new DataValue(new Variant(value))));
    }
}