/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.core.dtd;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaSerializationException;
import org.eclipse.milo.opcua.stack.core.serialization.OpcUaBinaryStreamDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.OpcUaBinaryStreamEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.SerializationContext;
import org.eclipse.milo.opcua.stack.core.serialization.UaEnumeratedType;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.DataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.DataTypeDictionary2;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.DiagnosticInfo;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.util.Namespaces;
import org.jetbrains.annotations.Nullable;
import org.opcfoundation.opcua.binaryschema.EnumeratedType;
import org.opcfoundation.opcua.binaryschema.FieldType;
import org.opcfoundation.opcua.binaryschema.StructuredType;
import org.opcfoundation.opcua.binaryschema.SwitchOperand;
import org.opcfoundation.opcua.binaryschema.TypeDescription;

public abstract class AbstractBsdCodec<StructureT, MemberT> implements BinaryDataTypeCodec {

    private final Map<String, FieldType> fields = new HashMap<>();
    private final Map<String, FieldType> lengthFields = new HashMap<>();

    private final StructuredType structuredType;

    protected AbstractBsdCodec(StructuredType structuredType) {
        this.structuredType = structuredType;

        structuredType.getField().forEach(field -> {
            fields.put(field.getName(), field);

            String lengthField = field.getLengthField();

            if (lengthField != null) {
                lengthFields.put(lengthField, fields.get(lengthField));
            }
        });
    }

    @Override
    public StructureT decode(
        SerializationContext context,
        OpcUaBinaryStreamDecoder decoder
    ) throws UaSerializationException {

        LinkedHashMap<String, MemberT> members = new LinkedHashMap<>();

        PeekingIterator<FieldType> fieldIterator = Iterators
            .peekingIterator(structuredType.getField().iterator());

        while (fieldIterator.hasNext()) {
            FieldType field = fieldIterator.next();
            String fieldName = field.getName();
            String typeName = field.getTypeName().getLocalPart();
            String typeNamespace = field.getTypeName().getNamespaceURI();

            if (fieldIsAbsent(field, members)) {
                continue;
            }

            boolean typeNamespaceIsUa =
                Namespaces.OPC_UA.equals(typeNamespace) ||
                    Namespaces.OPC_UA_BSD.equals(typeNamespace);

            if (fieldIsScalar(field)) {
                Function<OpcUaBinaryStreamDecoder, Object> reader;
                if (typeNamespaceIsUa && (reader = getReader(typeName)) != null) {
                    Object value = reader.apply(decoder);

                    members.put(fieldName, opcUaToMemberTypeScalar(fieldName, value, typeName));
                } else {
                    Object value = decode(context, typeNamespace, typeName, decoder);

                    members.put(fieldName, opcUaToMemberTypeScalar(fieldName, value, typeName));
                }
            } else {
                if (field.isIsLengthInBytes()) {
                    throw new UaSerializationException(
                        StatusCodes.Bad_DecodingError,
                        "IsLengthInBytes=true not supported");
                }

                int length = fieldLength(field, members);

                if ("Bit".equals(typeName) && typeNamespaceIsUa) {
                    BigInteger bitAccumulation = BigInteger.valueOf(0L);

                    for (int i = 0; i < length; i++) {
                        BigInteger bitValue = BigInteger.valueOf(decoder.readBit());

                        bitAccumulation = bitAccumulation.or(bitValue.shiftLeft(i));
                    }

                    members.put(fieldName, opcUaToMemberTypeArray(fieldName, bitAccumulation.intValue(), typeName));
                } else {
                    Object[] values = null;

                    if (length >= 0) {
                        values = new Object[length];

                        Function<OpcUaBinaryStreamDecoder, Object> reader;
                        if (typeNamespaceIsUa && (reader = getReader(typeName)) != null) {
                            for (int i = 0; i < length; i++) {
                                Object value = reader.apply(decoder);

                                values[i] = value;
                            }
                        } else {
                            for (int i = 0; i < length; i++) {
                                Object value = decode(context, typeNamespace, typeName, decoder);

                                values[i] = value;
                            }
                        }
                    }

                    members.put(fieldName, opcUaToMemberTypeArray(fieldName, values, typeName));
                }
            }
        }

        for (String lengthField : lengthFields.keySet()) {
            members.remove(lengthField);
        }

        return createStructure(structuredType.getName(), members);
    }

    @Override
    public void encode(
        SerializationContext context,
        OpcUaBinaryStreamEncoder encoder,
        Object structure
    ) throws UaSerializationException {

        LinkedHashMap<String, MemberT> members = new LinkedHashMap<>(getMembers((StructureT) structure));

        PeekingIterator<FieldType> fieldIterator = Iterators
            .peekingIterator(structuredType.getField().iterator());

        while (fieldIterator.hasNext()) {
            FieldType field = fieldIterator.next();

            if (fieldIsAbsent(field, members)) {
                continue;
            }

            if (lengthFields.containsKey(field.getName())) {
                // let arrays encode their own length field so the
                // structure representation isn't required to include it
                continue;
            }

            encodeField(context, encoder, members, field);
        }
    }

    private void encodeField(
        SerializationContext context,
        OpcUaBinaryStreamEncoder encoder,
        LinkedHashMap<String, MemberT> members,
        FieldType field
    ) {

        String typeName = field.getTypeName().getLocalPart();
        String typeNamespace = field.getTypeName().getNamespaceURI();

        MemberT member = members.get(field.getName());

        boolean typeNamespaceIsUa =
            Namespaces.OPC_UA.equals(typeNamespace) ||
                Namespaces.OPC_UA_BSD.equals(typeNamespace);

        if (fieldIsScalar(field)) {
            Object scalarValue = memberTypeToOpcUaScalar(member, typeName);

            BiConsumer<OpcUaBinaryStreamEncoder, Object> writer;
            if (typeNamespaceIsUa && (writer = getWriter(typeName)) != null) {
                writer.accept(encoder, scalarValue);
            } else {
                encode(context, typeNamespace, typeName, scalarValue, encoder);
            }
        } else {
            if (field.isIsLengthInBytes()) {
                throw new UaSerializationException(
                    StatusCodes.Bad_EncodingError,
                    "IsLengthInBytes=true not supported");
            }

            if ("Bit".equals(typeName) && typeNamespaceIsUa) {
                int length = fieldLength(field, members);

                Number number = (Number) memberTypeToOpcUaArray(member, typeName);
                BigInteger bi = BigInteger.valueOf(number.longValue());

                for (int i = 0; i < length; i++) {
                    encoder.writeBit(bi.shiftRight(i).and(BigInteger.ONE).intValue());
                }
            } else {
                Object[] valueArray = (Object[]) memberTypeToOpcUaArray(member, typeName);

                FieldType lengthField = fields.get(field.getLengthField());

                if (lengthField != null) {
                    int length = valueArray.length;

                    members.put(lengthField.getName(), opcUaToMemberTypeScalar(
                        lengthField.getName(),
                        length,
                        lengthField.getTypeName().getLocalPart()
                    ));

                    encodeField(context, encoder, members, lengthField);
                }

                if (valueArray != null) {
                    BiConsumer<OpcUaBinaryStreamEncoder, Object> writer;
                    if (typeNamespaceIsUa && (writer = getWriter(typeName)) != null) {
                        for (Object value : valueArray) {
                            writer.accept(encoder, value);
                        }
                    } else {
                        for (Object value : valueArray) {
                            encode(context, typeNamespace, typeName, value, encoder);
                        }
                    }
                }
            }
        }
    }

    private Object decode(
        SerializationContext context,
        String namespaceUri,
        String description,
        OpcUaBinaryStreamDecoder decoder
    ) throws UaSerializationException {

        DataTypeDictionary2 dictionary =
            context.getDataTypeManager().getTypeDictionary(namespaceUri);

        if (dictionary instanceof BinaryDataTypeDictionary) {
            BinaryDataTypeDictionary binaryDictionary = (BinaryDataTypeDictionary) dictionary;
            TypeDescription typeDescription = binaryDictionary.getTypeDescription(description);

            if (typeDescription instanceof StructuredType) {
                DataTypeCodec codec = dictionary.getCodec(description);

                if (codec != null) {
                    return codec.decode(context, decoder);
                } else {
                    throw new UaSerializationException(
                        StatusCodes.Bad_DecodingError,
                        String.format(
                            "no OpcBinaryDataTypeCodec registered for description=%s under namespaceUri=%s",
                            description, namespaceUri)
                    );
                }
            } else if (typeDescription instanceof EnumeratedType) {
                // TODO fieldName?
                return decoder.readEnum(null);
            } else {
                throw new UaSerializationException(
                    StatusCodes.Bad_DecodingError,
                    String.format("type not supported: %s / %s", namespaceUri, description)
                );
            }
        } else {
            throw new UaSerializationException(
                StatusCodes.Bad_DecodingError,
                String.format(
                    "no OpcUaBinaryDataTypeDictionary registered for under namespaceUri=%s", namespaceUri)
            );
        }
    }

    private void encode(
        SerializationContext context,
        String namespaceUri,
        String description,
        Object value,
        OpcUaBinaryStreamEncoder encoder
    ) throws UaSerializationException {

        DataTypeDictionary2 dictionary =
            context.getDataTypeManager().getTypeDictionary(namespaceUri);

        if (dictionary instanceof BinaryDataTypeDictionary) {
            BinaryDataTypeDictionary binaryDictionary = (BinaryDataTypeDictionary) dictionary;
            TypeDescription typeDescription = binaryDictionary.getTypeDescription(description);

            if (typeDescription instanceof StructuredType) {
                DataTypeCodec codec = dictionary.getCodec(description);

                if (codec != null) {
                    codec.encode(context, encoder, value);
                } else {
                    throw new UaSerializationException(
                        StatusCodes.Bad_DecodingError,
                        String.format(
                            "no OpcBinaryDataTypeCodec registered for description=%s under namespaceUri=%s",
                            description, namespaceUri)
                    );
                }
            } else if (typeDescription instanceof EnumeratedType) {
                // TODO fieldName?
                encoder.writeEnum(null, new UaEnumeratedType() {
                    @Override
                    public ExpandedNodeId getTypeId() {
                        return ExpandedNodeId.NULL_VALUE;
                    }

                    @Override
                    public int getValue() {
                        return (Integer) value;
                    }
                });
            } else {
                throw new UaSerializationException(
                    StatusCodes.Bad_DecodingError,
                    String.format("type not supported: %s / %s", namespaceUri, description)
                );
            }
        } else {
            throw new UaSerializationException(
                StatusCodes.Bad_EncodingError,
                String.format(
                    "no OpcUaBinaryDataTypeDictionary registered for under namespaceUri=%s", namespaceUri)
            );
        }
    }

    protected abstract StructureT createStructure(String name, LinkedHashMap<String, MemberT> members);

    protected abstract Map<String, MemberT> getMembers(StructureT value);

    /**
     * Convert an OPC UA scalar value into a member of type {@link MemberT}.
     *
     * @param name     then name of the member.
     * @param value    the value of the member.
     * @param typeName the name of the OPC UA DataType.
     * @return a member of type {@link MemberT}.
     */
    protected abstract MemberT opcUaToMemberTypeScalar(String name, @Nullable Object value, String typeName);

    /**
     * Convert an OPC UA array value into a member of type {@link MemberT}.
     *
     * @param name     the name of the member.
     * @param values   the values of the member array.
     * @param typeName the name of the OPC UA DataType.
     * @return member of type {@link MemberT}.
     */
    protected abstract MemberT opcUaToMemberTypeArray(String name, @Nullable Object values, String typeName);

    protected abstract Object memberTypeToOpcUaScalar(MemberT member, String typeName);

    protected abstract Object memberTypeToOpcUaArray(MemberT member, String typeName);

    private int fieldLength(FieldType field, LinkedHashMap<String, MemberT> members) {
        int length = 1;

        if (field.getLength() != null) {
            length = field.getLength().intValue();
        } else if (field.getLengthField() != null) {
            MemberT lengthMember = members.get(field.getLengthField());

            if (lengthMember != null) {
                String lengthTypeName = structuredType.getField().stream()
                    .filter(f -> f.getName().equals(field.getLengthField()))
                    .findFirst()
                    .map(f -> f.getTypeName().getLocalPart())
                    .orElse("Int32");

                length = ((Number) memberTypeToOpcUaScalar(lengthMember, lengthTypeName)).intValue();
            }
        }

        return length;
    }

    private boolean fieldIsAbsent(FieldType field, Map<String, MemberT> members) {
        if (field.getSwitchField() == null) {
            return false;
        } else {
            MemberT controlField = members.get(field.getSwitchField());

            String controlTypeName = structuredType.getField().stream()
                .filter(f -> f.getName().equals(field.getSwitchField()))
                .findFirst()
                .map(f -> f.getTypeName().getLocalPart())
                .orElse("Int32");

            long controlValue = ((Number) memberTypeToOpcUaScalar(controlField, controlTypeName)).longValue();

            long switchValue = field.getSwitchValue() != null ?
                field.getSwitchValue() : 1L;

            SwitchOperand switchOperand = field.getSwitchOperand() != null ?
                field.getSwitchOperand() : SwitchOperand.EQUALS;

            return !compareToSwitchValue(controlValue, switchOperand, switchValue);
        }
    }

    private static boolean compareToSwitchValue(long controlValue, SwitchOperand switchOperand, long switchValue) {
        switch (switchOperand) {
            case EQUALS:
                return controlValue == switchValue;
            case NOT_EQUAL:
                return controlValue != switchValue;
            case GREATER_THAN:
                return controlValue > switchValue;
            case GREATER_THAN_OR_EQUAL:
                return controlValue >= switchValue;
            case LESS_THAN:
                return controlValue < switchValue;
            case LESS_THAN_OR_EQUAL:
                return controlValue <= switchValue;
            default:
                throw new UaSerializationException(
                    StatusCodes.Bad_InternalError,
                    "unknown SwitchOperand: " + switchOperand);
        }
    }

    private static boolean fieldIsScalar(FieldType field) {
        return field.getLengthField() == null && field.getLength() == null;
    }

    private static Function<OpcUaBinaryStreamDecoder, Object> getReader(String typeName) {
        switch (typeName) {
            //@formatter:off
            case "Boolean":
                return OpcUaBinaryStreamDecoder::readBoolean;
            case "SByte":
                return OpcUaBinaryStreamDecoder::readSByte;
            case "Int16":
                return OpcUaBinaryStreamDecoder::readInt16;
            case "Int32":
                return OpcUaBinaryStreamDecoder::readInt32;
            case "Int64":
                return OpcUaBinaryStreamDecoder::readInt64;
            case "Byte":
                return OpcUaBinaryStreamDecoder::readByte;
            case "UInt16":
                return OpcUaBinaryStreamDecoder::readUInt16;
            case "UInt32":
                return OpcUaBinaryStreamDecoder::readUInt32;
            case "UInt64":
                return OpcUaBinaryStreamDecoder::readUInt64;
            case "Float":
                return OpcUaBinaryStreamDecoder::readFloat;
            case "Double":
                return OpcUaBinaryStreamDecoder::readDouble;
            case "String":
                return OpcUaBinaryStreamDecoder::readString;
            case "DateTime":
                return OpcUaBinaryStreamDecoder::readDateTime;
            case "Guid":
                return OpcUaBinaryStreamDecoder::readGuid;
            case "ByteString":
                return OpcUaBinaryStreamDecoder::readByteString;
            case "XmlElement":
                return OpcUaBinaryStreamDecoder::readXmlElement;
            case "NodeId":
                return OpcUaBinaryStreamDecoder::readNodeId;
            case "ExpandedNodeId":
                return OpcUaBinaryStreamDecoder::readExpandedNodeId;
            case "StatusCode":
                return OpcUaBinaryStreamDecoder::readStatusCode;
            case "QualifiedName":
                return OpcUaBinaryStreamDecoder::readQualifiedName;
            case "LocalizedText":
                return OpcUaBinaryStreamDecoder::readLocalizedText;
            case "ExtensionObject":
                return OpcUaBinaryStreamDecoder::readExtensionObject;
            case "DataValue":
                return OpcUaBinaryStreamDecoder::readDataValue;
            case "Variant":
                return OpcUaBinaryStreamDecoder::readVariant;
            case "DiagnosticInfo":
                return OpcUaBinaryStreamDecoder::readDiagnosticInfo;

            case "Bit":
                return OpcUaBinaryStreamDecoder::readBit;
            case "Char":
                return OpcUaBinaryStreamDecoder::readCharacter;
            case "CharArray":
                return OpcUaBinaryStreamDecoder::readUtf8CharArray;
            case "WideChar":
                return OpcUaBinaryStreamDecoder::readWideChar;
            case "WideCharArray":   // fall through
            case "WideString":
                return OpcUaBinaryStreamDecoder::readUtf16CharArray;
            default:
                return null;
            //@formatter:on
        }
    }

    private static BiConsumer<OpcUaBinaryStreamEncoder, Object> getWriter(String typeName) {
        switch (typeName) {
            //@formatter:off
            case "Boolean":
                return (w, v) -> w.writeBoolean((Boolean) v);
            case "SByte":
                return (w, v) -> w.writeSByte((Byte) v);
            case "Int16":
                return (w, v) -> w.writeInt16((Short) v);
            case "Int32":
                return (w, v) -> w.writeInt32((Integer) v);
            case "Int64":
                return (w, v) -> w.writeInt64((Long) v);
            case "Byte":
                return (w, v) -> w.writeByte((UByte) v);
            case "UInt16":
                return (w, v) -> w.writeUInt16((UShort) v);
            case "UInt32":
                return (w, v) -> w.writeUInt32((UInteger) v);
            case "UInt64":
                return (w, v) -> w.writeUInt64((ULong) v);
            case "Float":
                return (w, v) -> w.writeFloat((Float) v);
            case "Double":
                return (w, v) -> w.writeDouble((Double) v);
            case "String":
                return (w, v) -> w.writeString((String) v);
            case "DateTime":
                return (w, v) -> w.writeDateTime((DateTime) v);
            case "Guid":
                return (w, v) -> w.writeGuid((UUID) v);
            case "ByteString":
                return (w, v) -> w.writeByteString((ByteString) v);
            case "XmlElement":
                return (w, v) -> w.writeXmlElement((XmlElement) v);
            case "NodeId":
                return (w, v) -> w.writeNodeId((NodeId) v);
            case "ExpandedNodeId":
                return (w, v) -> w.writeExpandedNodeId((ExpandedNodeId) v);
            case "StatusCode":
                return (w, v) -> w.writeStatusCode((StatusCode) v);
            case "QualifiedName":
                return (w, v) -> w.writeQualifiedName((QualifiedName) v);
            case "LocalizedText":
                return (w, v) -> w.writeLocalizedText((LocalizedText) v);
            case "ExtensionObject":
                return (w, v) -> w.writeExtensionObject((ExtensionObject) v);
            case "DataValue":
                return (w, v) -> w.writeDataValue((DataValue) v);
            case "Variant":
                return (w, v) -> w.writeVariant((Variant) v);
            case "DiagnosticInfo":
                return (w, v) -> w.writeDiagnosticInfo((DiagnosticInfo) v);

            case "Bit":
                return (w, v) -> w.writeBit((Integer) v);
            case "Char":
                return (w, v) -> w.writeCharacter((Character) v);
            case "CharArray":
                return (w, v) -> w.writeUtf8CharArray((String) v);
            case "WideChar":
                return (w, v) -> w.writeWideChar((Character) v);
            case "WideCharArray":   // fall through
            case "WideString":
                return (w, v) -> w.writeUtf16CharArray((String) v);
            default:
                return null;
            //@formatter:on
        }
    }

}