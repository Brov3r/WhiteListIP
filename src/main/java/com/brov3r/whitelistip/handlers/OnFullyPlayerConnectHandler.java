package com.brov3r.whitelistip.handlers;

import com.avrix.events.OnPlayerFullyConnectedEvent;
import com.brov3r.whitelistip.IpUtils;
import zombie.core.raknet.UdpConnection;

import java.nio.ByteBuffer;

/**
 * Triggered when the player is fully connected to the server.
 */
public class OnFullyPlayerConnectHandler extends OnPlayerFullyConnectedEvent {
    /**
     * Called Event Handling Method
     *
     * @param data             event-related data represented as a ByteBuffer
     * @param playerConnection active player connection
     * @param username         user name
     */
    @Override
    public void handleEvent(ByteBuffer data, UdpConnection playerConnection, String username) {
        IpUtils.checkIP(playerConnection.ip, playerConnection);
    }
}