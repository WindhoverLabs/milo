package org.eclipse.milo.opcua.sdk.server.model.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class AuditHistoryDeleteEventTypeNode extends AuditHistoryUpdateEventTypeNode implements AuditHistoryDeleteEventType {
    public AuditHistoryDeleteEventTypeNode(UaNodeContext context, NodeId nodeId,
                                           QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                           UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                           RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                           UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    public AuditHistoryDeleteEventTypeNode(UaNodeContext context, NodeId nodeId,
                                           QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                           UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                           RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions);
    }

    @Override
    public PropertyTypeNode getUpdatedNodeNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditHistoryDeleteEventType.UPDATED_NODE);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public NodeId getUpdatedNode() {
        return getProperty(AuditHistoryDeleteEventType.UPDATED_NODE).orElse(null);
    }

    @Override
    public void setUpdatedNode(NodeId value) {
        setProperty(AuditHistoryDeleteEventType.UPDATED_NODE, value);
    }
}