/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.core.typetree;

import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.NodeIds;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UNumber;
import org.eclipse.milo.opcua.stack.core.types.structured.DataTypeDefinition;
import org.eclipse.milo.opcua.stack.core.util.Tree;
import org.jetbrains.annotations.Nullable;

/**
 * A tree-based representation of a DataType hierarchy.
 * <p>
 * Allows for convenient operations such as:
 * <ul>
 *     <li>resolving the backing Class for a value of a given DataType</li>
 *     <li>reducing a DataType to its inherited builtin type</li>
 *     <li>checking if a Class would be assignable to a value of some DataType</li>
 *     <li>retrieving the encoding ids for structured DataTypes</li>
 *     <li>retrieving the DataTypeDefinition for structured and enumerated DataTypes</li>
 * </ul>
 */
public class DataTypeTree extends TypeTree<DataType> {

    public DataTypeTree(Tree<DataType> tree) {
        super(tree);
    }

    /**
     * Get the backing Class a value of DataType {@code dataTypeId} would have.
     * <p>
     * Builtin DataTypes are backed by their intrinsic backing class.
     * <p>
     * Abstract types {@link NodeIds#Number}, {@link NodeIds#Integer}, and {@link NodeIds#UInteger}
     * are backed by {@link Number}, {@link Integer}, and {@link UInteger} respectively.
     * <p>
     * Enumerations are backed by {@link Integer}.
     * <p>
     * Structures are backed by {@link ExtensionObject}.
     *
     * @param dataTypeId the {@link NodeId} of a DataType Node.
     * @return the backing Class a value of DataType {@code dataTypeId} would have.
     * @see BuiltinDataType
     * @see BuiltinDataType#getBackingClass()
     */
    public Class<?> getBackingClass(NodeId dataTypeId) {
        if (BuiltinDataType.isBuiltin(dataTypeId)) {
            return BuiltinDataType.getBackingClass(dataTypeId);
        } else {
            if (NodeIds.Enumeration.equals(dataTypeId)) {
                return Integer.class;
            } else if (NodeIds.Number.equals(dataTypeId)) {
                return Number.class;
            } else if (NodeIds.Integer.equals(dataTypeId)) {
                return Number.class;
            } else if (NodeIds.UInteger.equals(dataTypeId)) {
                return UNumber.class;
            } else {
                Tree<DataType> node = types.get(dataTypeId);
                Tree<DataType> parent = node != null ? node.getParent() : null;

                if (parent != null) {
                    return getBackingClass(parent.getValue().getNodeId());
                } else {
                    return Object.class;
                }
            }
        }
    }

    /**
     * Get the {@link BuiltinDataType} {@code dataTypeId} inherits from, following references to the parent
     * as necessary until a {@link BuiltinDataType} is found.
     *
     * @param dataTypeId the {@link NodeId} of a DataType Node.
     * @return the {@link BuiltinDataType} this DataType inherits from.
     */
    public BuiltinDataType getBuiltinType(NodeId dataTypeId) {
        if (BuiltinDataType.isBuiltin(dataTypeId)) {
            return BuiltinDataType.fromNodeId(dataTypeId);
        } else {
            Tree<DataType> node = types.get(dataTypeId);
            Tree<DataType> parent = node != null ? node.getParent() : null;

            if (parent != null) {
                return getBuiltinType(parent.getValue().getNodeId());
            } else {
                return BuiltinDataType.Variant;
            }
        }
    }

    /**
     * Get the {@link DataType} info for the DataType identified by {@code dataTypeId}, if it exists.
     *
     * @param dataTypeId the {@link NodeId} of a DataType Node.
     * @return the {@link DataType} info for the DataType identified by {@code dataTypeId}, if it exists.
     */
    public @Nullable DataType getDataType(NodeId dataTypeId) {
        return getType(dataTypeId);
    }

    /**
     * Get the {@link NodeId} of the Binary Encoding Node for the DataType identified by {@code dataTypeId}.
     *
     * @param dataTypeId the {@link NodeId} of a DataType Node.
     * @return the {@link NodeId} of the Binary Encoding Node, or {@code null} if none exists.
     */
    public @Nullable NodeId getBinaryEncodingId(NodeId dataTypeId) {
        DataType dataType = getDataType(dataTypeId);

        return dataType != null ? dataType.getBinaryEncodingId() : null;
    }

    /**
     * Get the {@link NodeId} of the XML Encoding Node for the DataType identified by {@code dataTypeId}.
     *
     * @param dataTypeId the {@link NodeId} of a DataType Node.
     * @return the {@link NodeId} of the XML Encoding Node, or {@code null} if none exists.
     */
    public @Nullable NodeId getXmlEncodingId(NodeId dataTypeId) {
        DataType dataType = getDataType(dataTypeId);

        return dataType != null ? dataType.getXmlEncodingId() : null;
    }

    /**
     * Get the {@link NodeId} of the JSON Encoding Node for the DataType identified by {@code dataTypeId}.
     *
     * @param dataTypeId the {@link NodeId} of the DataType Node.
     * @return the {@link NodeId} of the JSON Encoding Node for the DataType identified by {@code dataTypeId}.
     */
    public @Nullable NodeId getJsonEncodingId(NodeId dataTypeId) {
        DataType dataType = getDataType(dataTypeId);

        return dataType != null ? dataType.getJsonEncodingId() : null;
    }

    /**
     * Get the {@link DataTypeDefinition} of the DataType identified by {@code dataTypeId}.
     * <p>
     * Only Structured and Enumerated DataTypes have a {@link DataTypeDefinition}.
     *
     * @param dataTypeId the {@link NodeId} of the DataType Node.
     * @return the {@link DataTypeDefinition} of the DataType identified by {@code dataTypeId}.
     */
    public @Nullable DataTypeDefinition getDataTypeDefinition(NodeId dataTypeId) {
        DataType dataType = getDataType(dataTypeId);

        return dataType != null ? dataType.getDataTypeDefinition() : null;
    }

    /**
     * Check if a value of type {@code clazz} is assignable to a value of DataType {@code dataTypeId}, i.e. it is
     * equal to or a subtype of the backing class for {@code dataTypeId}.
     *
     * @param dataTypeId the {@link NodeId} of a DataType Node.
     * @param clazz      the backing Class to check.
     * @return {@code true} if {@code clazz} is equal to or a subtype of the backing class for {@code dataTypeId}.
     */
    public boolean isAssignable(NodeId dataTypeId, Class<?> clazz) {
        Class<?> backingClass = getBackingClass(dataTypeId);

        if (NodeIds.Integer.equals(dataTypeId)) {
            // Can't just check that it's assignable from Number.class because
            // UNumber extends Number rather than the two sharing a common
            // superclass.
            return clazz == byte.class || clazz == Byte.class ||
                clazz == short.class || clazz == Short.class ||
                clazz == int.class || clazz == Integer.class ||
                clazz == long.class || clazz == Long.class;
        } else if (NodeIds.UInteger.equals(dataTypeId)) {
            return UNumber.class.isAssignableFrom(clazz);
        } else {
            return backingClass.isAssignableFrom(clazz);
        }
    }

    public boolean isEnumType(NodeId dataTypeId) {
        return isSubtypeOf(dataTypeId, NodeIds.Enumeration);
    }

    public boolean isStructType(NodeId dataTypeId) {
        return isSubtypeOf(dataTypeId, NodeIds.Structure);
    }

    /**
     * Get the underlying {@link Tree} node for the DataType identified by {@code dataTypeId}.
     *
     * @param dataTypeId the {@link NodeId} of a DataType Node.
     * @return the underlying {@link Tree} node for the DataType identified by {@code dataTypeId}.
     */
    public @Nullable Tree<DataType> getTreeNode(NodeId dataTypeId) {
        return types.get(dataTypeId);
    }

}