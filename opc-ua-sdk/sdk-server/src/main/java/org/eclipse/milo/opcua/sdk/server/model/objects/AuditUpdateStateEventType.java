package org.eclipse.milo.opcua.sdk.server.model.objects;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyType;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part16/4.4.17">https://reference.opcfoundation.org/v105/Core/docs/Part16/4.4.17</a>
 */
public interface AuditUpdateStateEventType extends AuditUpdateMethodEventType {
    QualifiedProperty<Object> OLD_STATE_ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "OldStateId",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=24"),
        -1,
        Object.class
    );

    QualifiedProperty<Object> NEW_STATE_ID = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "NewStateId",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=24"),
        -1,
        Object.class
    );

    Object getOldStateId();

    void setOldStateId(Object value);

    PropertyType getOldStateIdNode();

    Object getNewStateId();

    void setNewStateId(Object value);

    PropertyType getNewStateIdNode();
}