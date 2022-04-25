/*
 * Copyright (c) 2021 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.core.types.structured;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.milo.opcua.stack.core.serialization.SerializationContext;
import org.eclipse.milo.opcua.stack.core.serialization.UaDecoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaEncoder;
import org.eclipse.milo.opcua.stack.core.serialization.UaStructure;
import org.eclipse.milo.opcua.stack.core.serialization.codecs.GenericDataTypeCodec;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;

@EqualsAndHashCode(
    callSuper = false
)
@SuperBuilder(
    toBuilder = true
)
@ToString
public class AdditionalParametersType extends Structure implements UaStructure {
    public static final ExpandedNodeId TYPE_ID = ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=16313");

    public static final ExpandedNodeId BINARY_ENCODING_ID = ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17537");

    public static final ExpandedNodeId XML_ENCODING_ID = ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17541");

    public static final ExpandedNodeId JSON_ENCODING_ID = ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=17547");

    private final KeyValuePair[] parameters;

    public AdditionalParametersType(KeyValuePair[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public ExpandedNodeId getTypeId() {
        return TYPE_ID;
    }

    @Override
    public ExpandedNodeId getBinaryEncodingId() {
        return BINARY_ENCODING_ID;
    }

    @Override
    public ExpandedNodeId getXmlEncodingId() {
        return XML_ENCODING_ID;
    }

    @Override
    public ExpandedNodeId getJsonEncodingId() {
        return JSON_ENCODING_ID;
    }

    public KeyValuePair[] getParameters() {
        return parameters;
    }

    public static final class Codec extends GenericDataTypeCodec<AdditionalParametersType> {
        @Override
        public Class<AdditionalParametersType> getType() {
            return AdditionalParametersType.class;
        }

        @Override
        public AdditionalParametersType decode(SerializationContext context, UaDecoder decoder) {
            KeyValuePair[] parameters = (KeyValuePair[]) decoder.readStructArray("Parameters", KeyValuePair.TYPE_ID);
            return new AdditionalParametersType(parameters);
        }

        @Override
        public void encode(SerializationContext context, UaEncoder encoder,
                           AdditionalParametersType value) {
            encoder.writeStructArray("Parameters", value.getParameters(), KeyValuePair.TYPE_ID);
        }
    }
}