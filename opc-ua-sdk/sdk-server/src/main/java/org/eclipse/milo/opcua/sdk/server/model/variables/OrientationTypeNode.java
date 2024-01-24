/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.server.model.variables;

import java.util.Optional;

import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessLevelExType;
import org.eclipse.milo.opcua.stack.core.types.structured.AccessRestrictionType;
import org.eclipse.milo.opcua.stack.core.types.structured.EUInformation;
import org.eclipse.milo.opcua.stack.core.types.structured.RolePermissionType;

public class OrientationTypeNode extends BaseDataVariableTypeNode implements OrientationType {
    public OrientationTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                               LocalizedText displayName, LocalizedText description, UInteger writeMask,
                               UInteger userWriteMask, RolePermissionType[] rolePermissions,
                               RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                               DataValue value, NodeId dataType, Integer valueRank, UInteger[] arrayDimensions,
                               UByte accessLevel, UByte userAccessLevel, Double minimumSamplingInterval, boolean historizing,
                               AccessLevelExType accessLevelEx) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, value, dataType, valueRank, arrayDimensions, accessLevel, userAccessLevel, minimumSamplingInterval, historizing, accessLevelEx);
    }

    public OrientationTypeNode(UaNodeContext context, NodeId nodeId, QualifiedName browseName,
                               LocalizedText displayName, LocalizedText description, UInteger writeMask,
                               UInteger userWriteMask, RolePermissionType[] rolePermissions,
                               RolePermissionType[] userRolePermissions, AccessRestrictionType accessRestrictions,
                               DataValue value, NodeId dataType, Integer valueRank, UInteger[] arrayDimensions) {
        super(context, nodeId, browseName, displayName, description, writeMask, userWriteMask, rolePermissions, userRolePermissions, accessRestrictions, value, dataType, valueRank, arrayDimensions);
    }

    @Override
    public PropertyTypeNode getAngleUnitNode() {
        Optional<VariableNode> propertyNode = getPropertyNode(OrientationType.ANGLE_UNIT);
        return (PropertyTypeNode) propertyNode.orElse(null);
    }

    @Override
    public EUInformation getAngleUnit() {
        return getProperty(OrientationType.ANGLE_UNIT).orElse(null);
    }

    @Override
    public void setAngleUnit(EUInformation value) {
        setProperty(OrientationType.ANGLE_UNIT, value);
    }
}