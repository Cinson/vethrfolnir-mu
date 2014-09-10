/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vethrfolnir.game.network.mu.send;

import java.nio.ByteOrder;

import io.netty.buffer.ByteBuf;

import com.vethrfolnir.game.entitys.ComponentIndex;
import com.vethrfolnir.game.entitys.GameObject;
import com.vethrfolnir.game.entitys.annotation.FetchIndex;
import com.vethrfolnir.game.entitys.components.Positioning;
import com.vethrfolnir.game.entitys.components.player.PlayerState;
import com.vethrfolnir.game.network.mu.MuClient;
import com.vethrfolnir.game.templates.AccountCharacterInfo;
import com.vethrfolnir.network.NetworkClient;
import com.vethrfolnir.network.WritePacket;

/**
 * @author Vlad
 *
 */
public class PlayerInfo extends WritePacket {

	@FetchIndex
	private ComponentIndex<Positioning> pos;
	
	@FetchIndex
	private ComponentIndex<PlayerState> state;
	
	@Override
	public void write(NetworkClient context, ByteBuf buff, Object... params) {
		MuClient client = as(context);
		boolean toSelf = as(params[0]);

		if(params.length > 1 && params[1] != null) {
			GameObject target = as(params[1]);
			client = target.getClient();
		}
		
		PlayerState state = client.getEntity().get(this.state);
		Positioning positioning = client.getEntity().get(this.pos);
		
		writeArray(buff, 0xC2, 0x00, 0x29, 0x12, 0x01); // size?
		
		if(toSelf)
		{
			writeSh(buff, client.getClientId(), ByteOrder.BIG_ENDIAN);
			shiftC(buff, 5); // shifts the 6th byte for client operations
		}
		else
			writeSh(buff, client.getClientId(), ByteOrder.BIG_ENDIAN);

		writeArray(buff, positioning.getX(), positioning.getY());// 0x42, 0xD3);
		writeC(buff, state.getClassId().classId << 1); // class id
		writeArray(buff, AccountCharacterInfo.getWearBytes());
		writeS(buff, client.getEntity().getName(), 10); // name
		writeArray(buff, positioning.getX(), positioning.getY(),// 0x42, 0xD3,
		0x00, 0x00); // Cool effect
		
		if(state.getAccessLevel() > 0)
			writeC(buff, 0x1C);
		else
			writeC(buff, 0x00);
	}

}