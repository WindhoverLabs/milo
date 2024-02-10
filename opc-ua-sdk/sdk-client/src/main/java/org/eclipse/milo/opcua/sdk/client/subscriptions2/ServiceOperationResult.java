/*
 * Copyright (c) 2024 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.sdk.client.subscriptions2;

import java.util.Optional;

import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

interface ServiceOperationResult {

    /**
     * The StatusCode associated with the service call this operation was a part of.
     *
     * @return the StatusCode associated with the service call this operation was a part of.
     */
    StatusCode serviceResult();

    /**
     * The StatusCode associated with the operation-level result.
     *
     * @return the StatusCode associated with the operation-level result.
     */
    Optional<StatusCode> operationResult();

}