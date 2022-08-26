package org.eclipse.milo.opcua.sdk.server.model.variables;

import org.eclipse.milo.opcua.sdk.core.QualifiedProperty;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;

/**
 * @see <a href="https://reference.opcfoundation.org/v105/Core/docs/Part8/5.3.1">https://reference.opcfoundation.org/v105/Core/docs/Part8/5.3.1</a>
 */
public interface DataItemType extends BaseDataVariableType {
    QualifiedProperty<String> DEFINITION = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "Definition",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=12"),
        -1,
        String.class
    );

    QualifiedProperty<Double> VALUE_PRECISION = new QualifiedProperty<>(
        "http://opcfoundation.org/UA/",
        "ValuePrecision",
        ExpandedNodeId.parse("nsu=http://opcfoundation.org/UA/;i=11"),
        -1,
        Double.class
    );

    String getDefinition();

    void setDefinition(String value);

    PropertyType getDefinitionNode();

    Double getValuePrecision();

    void setValuePrecision(Double value);

    PropertyType getValuePrecisionNode();
}