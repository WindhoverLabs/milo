/*
 * Copyright (c) 2024 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.dtd;

import java.util.List;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.core.dtd.generic.StructCodec;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.DataTypeDictionary;
import org.eclipse.milo.opcua.stack.core.types.DataTypeManager;

public class LegacyDataTypeManagerInitializer implements OpcUaClient.DataTypeManagerInitializer {

    private final OpcUaClient client;

    public LegacyDataTypeManagerInitializer(OpcUaClient client) {
        this.client = client;
    }

    @Override
    public void initialize(NamespaceTable namespaceTable, DataTypeManager dataTypeManager) throws UaException {
        List<DataTypeDictionary> dataTypeDictionaries =
            new BinaryDataTypeDictionaryReader(client)
                .readDataTypeDictionaries(StructCodec::new);

        for (DataTypeDictionary dictionary : dataTypeDictionaries) {
            dataTypeManager.registerTypeDictionary(dictionary);

            dictionary.getTypes().forEach(type ->
                dataTypeManager.registerType(
                    type.getDataTypeId(),
                    type.getCodec(),
                    type.getEncodingId(), null, null
                )
            );
        }
    }

}