package org.eclipse.milo.opcua.sdk.server.model.objects;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.sdk.server.model.variables.PropertyType;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part5/6.3.9">https://reference.opcfoundation.org/v105/Core/docs/Part5/6.3.9</a>
 */
public interface NonTransparentRedundancyType extends ServerRedundancyType {
    QualifiedProperty<String[]> SERVER_URI_ARRAY = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ServerUriArray",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12"),
        1,
        String[].class
    );

    String[] getServerUriArray();

    void setServerUriArray(String[] value);

    PropertyType getServerUriArrayNode();
}