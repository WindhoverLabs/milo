/*
 * Copyright (c) 2022 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.opcua.stack.transport.https;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import org.eclipse.milo.opcua.stack.client.transport.UaTransportRequest;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.ServerTable;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.channel.EncodingLimits;
import org.eclipse.milo.opcua.stack.core.encoding.EncodingContext;
import org.eclipse.milo.opcua.stack.core.encoding.EncodingManager;
import org.eclipse.milo.opcua.stack.core.encoding.OpcUaEncodingManager;
import org.eclipse.milo.opcua.stack.core.encoding.binary.OpcUaBinaryDecoder;
import org.eclipse.milo.opcua.stack.core.encoding.binary.OpcUaBinaryEncoder;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.DataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.OpcUaDataTypeManager;
import org.eclipse.milo.opcua.stack.core.types.UaResponseMessageType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.eclipse.milo.opcua.stack.transport.client.ClientApplication;
import org.eclipse.milo.opcua.stack.transport.client.OpcClientTransportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpcClientHttpCodec extends MessageToMessageCodec<HttpResponse, UaTransportRequest> {

    private static final AttributeKey<UaTransportRequest> KEY_PENDING_REQUEST =
        AttributeKey.newInstance("pendingRequest");

    private static final String UABINARY_CONTENT_TYPE =
        HttpHeaderValues.APPLICATION_OCTET_STREAM.toString();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EndpointDescription endpoint;
    private final TransportProfile transportProfile;

    private final OpcClientTransportConfig config;
    private final ClientApplication application;

    OpcClientHttpCodec(OpcClientTransportConfig config, ClientApplication application) {
        this.config = config;
        this.application = application;

        endpoint = application.getEndpoint();
        transportProfile = TransportProfile.fromUri(endpoint.getTransportProfileUri());
    }

    @Override
    protected void encode(
        ChannelHandlerContext ctx,
        UaTransportRequest transportRequest,
        List<Object> out) throws Exception {

        logger.debug("encoding: " + transportRequest.getRequest());

        ctx.channel().attr(KEY_PENDING_REQUEST).set(transportRequest);

        ByteBuf content = Unpooled.buffer();

        switch (transportProfile) {
            case HTTPS_UABINARY: {
                var encoder = new OpcUaBinaryEncoder(newEncodingContext(config.getEncodingLimits()));
                encoder.setBuffer(content);
                encoder.encodeMessage(null, transportRequest.getRequest());
                break;
            }

            case HTTPS_UAXML: {
                // TODO put document into a SOAP message.
                throw new UaException(StatusCodes.Bad_InternalError,
                    "no encoder for transport: " + transportProfile);
            }

            default:
                throw new UaException(StatusCodes.Bad_InternalError,
                    "no encoder for transport: " + transportProfile);
        }

        String endpointUrl = endpoint.getEndpointUrl();

        FullHttpRequest httpRequest = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1,
            HttpMethod.POST,
            EndpointUtil.getPath(endpointUrl),
            content
        );

        httpRequest.headers().set(HttpHeaderNames.HOST, EndpointUtil.getHost(endpointUrl));
        httpRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        httpRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, UABINARY_CONTENT_TYPE);
        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        httpRequest.headers().set("OPCUA-SecurityPolicy", application.getEndpoint().getSecurityPolicyUri());

        out.add(httpRequest);
    }

    @Override
    protected void decode(
        ChannelHandlerContext ctx,
        HttpResponse httpResponse,
        List<Object> out) throws Exception {

        logger.trace("channelRead0: " + httpResponse);

        UaTransportRequest transportRequest = ctx.channel()
            .attr(KEY_PENDING_REQUEST)
            .getAndSet(null);

        if (httpResponse instanceof FullHttpResponse) {
            String contentType = httpResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);

            FullHttpResponse fullHttpResponse = (FullHttpResponse) httpResponse;
            ByteBuf content = fullHttpResponse.content();

            UaResponseMessageType responseMessage;

            switch (transportProfile) {
                case HTTPS_UABINARY: {
                    if (!UABINARY_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
                        throw new UaException(StatusCodes.Bad_DecodingError,
                            "unexpected content-type: " + contentType);
                    }

                    var decoder = new OpcUaBinaryDecoder(newEncodingContext(config.getEncodingLimits()));
                    decoder.setBuffer(content);
                    responseMessage = (UaResponseMessageType) decoder.decodeMessage(null);
                    break;
                }

                case HTTPS_UAXML: {
                    // TODO extract document from SOAP message body
                    throw new UaException(StatusCodes.Bad_InternalError,
                        "no decoder for transport: " + transportProfile);
                }

                default:
                    throw new UaException(StatusCodes.Bad_InternalError,
                        "no decoder for transport: " + transportProfile);
            }

            transportRequest.getFuture().complete(responseMessage);
        } else {
            HttpResponseStatus status = httpResponse.status();

            if (status.equals(HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE)) {
                transportRequest.getFuture().completeExceptionally(
                    new UaException(StatusCodes.Bad_ResponseTooLarge));
            } else {
                transportRequest.getFuture().completeExceptionally(
                    new UaException(StatusCodes.Bad_UnexpectedError,
                        String.format("%s: %s", status.code(), status.reasonPhrase())));
            }
        }
    }

    private static EncodingContext newEncodingContext(EncodingLimits encodingLimits) {
        return new DefaultEncodingContext(encodingLimits);
    }

    private static class DefaultEncodingContext implements EncodingContext {

        private final NamespaceTable namespaceTable = new NamespaceTable();
        private final ServerTable serverTable = new ServerTable();

        private final EncodingLimits encodingLimits;

        private DefaultEncodingContext(EncodingLimits encodingLimits) {
            this.encodingLimits = encodingLimits;
        }

        @Override
        public DataTypeManager getDataTypeManager() {
            return OpcUaDataTypeManager.getInstance();
        }

        @Override
        public EncodingManager getEncodingManager() {
            return OpcUaEncodingManager.getInstance();
        }

        @Override
        public EncodingLimits getEncodingLimits() {
            return encodingLimits;
        }

        @Override
        public NamespaceTable getNamespaceTable() {
            return namespaceTable;
        }

        @Override
        public ServerTable getServerTable() {
            return serverTable;
        }

    }

}