package com.sy599.game.qipai.dddz.command;

import com.sy599.game.gcommand.BaseCommand;
import com.sy599.game.qipai.AbstractCommandProcessor;
import com.sy599.game.qipai.dddz.bean.DddzPlayer;
import com.sy599.game.qipai.dddz.command.play.PlayCommand;
import com.sy599.game.qipai.dddz.command.play.DddzComCommand;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.ObjectUtil;
import com.sy599.game.websocket.constant.WebSocketMsgType;

import java.util.HashMap;
import java.util.Map;

public class DddzCommandProcessor extends AbstractCommandProcessor<DddzPlayer> {
	private static DddzCommandProcessor processor = new DddzCommandProcessor();
	private static Map<Short, Class<? extends BaseCommand<DddzPlayer>>> commandMap = new HashMap<Short, Class<? extends BaseCommand<DddzPlayer>>>();
	private static Map<Class<?>, Short> msgClassToMsgTypeMap = new HashMap<Class<?>, Short>();

	static {
		// commandMap.put(WebSocketMsgType.cs_com, DddzComCommand.class);
		commandMap.put(WebSocketMsgType.cs_play, PlayCommand.class);
		commandMap.put(WebSocketMsgType.cs_com, DddzComCommand.class);
//		commandMap.put(WebSocketMsgType.cs_com, PdkComCommand.class);

		try {
			for (Short type : commandMap.keySet()) {
				Class<? extends BaseCommand> cl = commandMap.get(type);
				BaseCommand action = ObjectUtil.newInstance(cl);
				Map<Class<?>, Short> msgTypeMap = action.getMsgTypeMap();
				if (msgTypeMap != null && !msgTypeMap.isEmpty()) {
					for (Class<?> msgClass : msgTypeMap.keySet()) {
						if (msgClassToMsgTypeMap.containsKey(msgClass)) {
							throw new Exception("msgClassToMsgTypeMap err!!!!");

						} else {
							msgClassToMsgTypeMap.put(msgClass, msgTypeMap.get(msgClass));

						}
					}
				}
			}

		} catch (Exception e) {
			LogUtil.e("SocketAcitonProcessor err:", e);

		}

	}

	public static DddzCommandProcessor getInstance() {
		return processor;
	}


	public short getMsgType(Class<?> clazz) {
		if (msgClassToMsgTypeMap.containsKey(clazz)) {
			return msgClassToMsgTypeMap.get(clazz);
		}
		return 0;
	}

	@Override
	public Map<Short, Class<? extends BaseCommand<DddzPlayer>>> loadCommands() {
		return commandMap;
	}
}
