package org.eclipse.milo.opcua.sdk.server.model.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class AuditConditionConfirmEventTypeNode extends AuditConditionEventTypeNode implements AuditConditionConfirmEventType {
    public AuditConditionConfirmEventTypeNode(UaNodeContext context, NodeId nodeId,
                                              QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                              UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                              RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                              UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    public AuditConditionConfirmEventTypeNode(UaNodeContext context, NodeId nodeId,
                                              QualifiedName browseName, LocalizedText displayName, LocalizedText description,
                                              UInteger writeMask, UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                              RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions);
    }

    @Override
    public PropertyTypeNode getConditionEventIdNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditConditionConfirmEventType.CONDITION_EVENT_ID);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public ByteString getConditionEventId() {
        return getProperty(AuditConditionConfirmEventType.CONDITION_EVENT_ID).orElse(null);
    }

    @Override
    public void setConditionEventId(ByteString value) {
        setProperty(AuditConditionConfirmEventType.CONDITION_EVENT_ID, value);
    }

    @Override
    public PropertyTypeNode getCommentNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(AuditConditionConfirmEventType.COMMENT);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public LocalizedText getComment() {
        return getProperty(AuditConditionConfirmEventType.COMMENT).orElse(null);
    }

    @Override
    public void setComment(LocalizedText value) {
        setProperty(AuditConditionConfirmEventType.COMMENT, value);
    }
}