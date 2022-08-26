package org.eclipse.milo.opcua.sdk.server.model.objects;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyType;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part9/5.10.5">https://reference.opcfoundation.org/v105/Core/docs/Part9/5.10.5</a>
 */
public interface AuditConditionRespondEventType extends AuditConditionEventType {
    QualifiedProperty<UInteger> SELECTED_RESPONSE = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "SelectedResponse",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=7"),
        -1,
        UInteger.class
    );

    UInteger getSelectedResponse();

    void setSelectedResponse(UInteger value);

    PropertyType getSelectedResponseNode();
}