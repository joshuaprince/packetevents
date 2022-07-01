/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.wrapper.login.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.crypto.MinecraftEncryptionUtil;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientEncryptionResponse;

import java.security.PublicKey;

/**
 * This packet is sent by the server to the client if the server is in online mode.
 * If the server is in offline mode, this packet won't be sent,
 * encryption won't be enabled and no authentication will be performed.
 *
 * @see WrapperLoginClientEncryptionResponse
 */
public class WrapperLoginServerEncryptionRequest extends PacketWrapper<WrapperLoginServerEncryptionRequest> {
    private String serverId;
    private PublicKey publicKey;
    private byte[] verifyToken;

    public WrapperLoginServerEncryptionRequest(PacketSendEvent event) {
        super(event);
    }

    public WrapperLoginServerEncryptionRequest(String serverId, byte[] publicKeyBytes, byte[] verifyToken) {
        super(PacketType.Login.Server.ENCRYPTION_REQUEST);
        this.serverId = serverId;
        this.publicKey = MinecraftEncryptionUtil.publicKey(publicKeyBytes);
        this.verifyToken = verifyToken;
    }

    public WrapperLoginServerEncryptionRequest(String serverId, PublicKey publicKey, byte[] verifyToken) {
        super(PacketType.Login.Server.ENCRYPTION_REQUEST);
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
    }

    @Override
    public void read() {
        this.serverId = readString(20);
        this.publicKey = readPublicKey();
        this.verifyToken = readByteArray(ByteBufHelper.readableBytes(buffer));
    }

    @Override
    public void write() {
        writeString(serverId, 20);
        writePublicKey(publicKey);
        writeByteArray(verifyToken);
    }

    @Override
    public void copy(WrapperLoginServerEncryptionRequest wrapper) {
        this.serverId = wrapper.serverId;
        this.publicKey = wrapper.publicKey;
        this.verifyToken = wrapper.verifyToken;
    }

    /**
     * This is an empty string on the vanilla client. (1.7.10 and above)
     *
     * @return Server ID
     */
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * The public key is in DER encoding format.
     * More technically, it is in ASN.1 format.
     * The public key will be encrypted by the client and
     * sent to the server via the {@link WrapperLoginClientEncryptionResponse} packet.
     *
     * @return Public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * The verify token will be encrypted by the client and
     * sent to the server via the {@link WrapperLoginClientEncryptionResponse} packet.
     *
     * @return Verify token
     */
    public byte[] getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(byte[] verifyToken) {
        this.verifyToken = verifyToken;
    }
}
