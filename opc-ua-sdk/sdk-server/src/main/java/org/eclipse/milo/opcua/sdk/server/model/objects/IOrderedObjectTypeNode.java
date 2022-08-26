package org.eclipse.milo.opcua.sdk.server.model.objects;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class IOrderedObjectTypeNode extends BaseInterfaceTypeNode implements IOrderedObjectType {
    public IOrderedObjectTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                                  LocalizedText displayName, LocalizedText description, UInteger writeMask,
                                  UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                  RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                                  UByte eventNotifier) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, eventNotifier);
    }

    public IOrderedObjectTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                                  LocalizedText displayName, LocalizedText description, UInteger writeMask,
                                  UInteger userWriteMask, RolePermissionType[] rolePermissions,
                                  RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions);
    }

    @Override
    public PropertyTypeNode getNumberInListNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(IOrderedObjectType.NUMBER_IN_LIST);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public Variant getNumberInList() {
        return getProperty(IOrderedObjectType.NUMBER_IN_LIST).orElse(null);
    }

    @Override
    public void setNumberInList(Variant value) {
        setProperty(IOrderedObjectType.NUMBER_IN_LIST, value);
    }
}