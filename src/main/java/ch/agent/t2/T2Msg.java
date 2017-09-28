/*
 *   Copyright 2011-2017 Hauser Olsson GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package ch.agent.t2;

import java.util.ResourceBundle;

import ch.agent.core.KeyedMessage;
import ch.agent.core.MessageBundle;

/**
 * T2Msg provides keyed messages to all ch.agent.t2.* packages.
 *
 * @author Jean-Paul Vetterli
 */
public class T2Msg extends KeyedMessage {

	public class K {
		public static final String T0001 = "T0001";
		public static final String T0003 = "T0003";
		public static final String T0004 = "T0004";
		public static final String T0005 = "T0005";
		public static final String T0006 = "T0006";
		public static final String T0007 = "T0007";
		public static final String T0012 = "T0012";
		public static final String T0013 = "T0013";
		public static final String T0014 = "T0014";
		public static final String T0015 = "T0015";
		public static final String T0016 = "T0016";
		
		public static final String T1014 = "T1014";
		public static final String T1015 = "T1015";
		public static final String T1016 = "T1016";
		public static final String T1017 = "T1017";
		public static final String T1018 = "T1018";
		public static final String T1019 = "T1019";
		public static final String T1021 = "T1021";
		public static final String T1022 = "T1022";
		public static final String T1023 = "T1023";
		public static final String T1025 = "T1025";
		public static final String T1026 = "T1026";
		public static final String T1027 = "T1027";
		public static final String T1051 = "T1051";
		public static final String T1052 = "T1052";
		public static final String T1053 = "T1053";
		public static final String T1054 = "T1054";
		public static final String T1055 = "T1055";
		public static final String T1058 = "T1058";
		public static final String T1059 = "T1059";
		public static final String T1060 = "T1060";
		public static final String T1068 = "T1068";
		public static final String T1069 = "T1069";
		public static final String T1070 = "T1070";
		public static final String T1071 = "T1071";
		public static final String T1072 = "T1072";
		public static final String T1073 = "T1073";
		public static final String T1074 = "T1074";
		public static final String T1075 = "T1075";
		public static final String T1076 = "T1076";
		public static final String T1077 = "T1077";
		public static final String T1081 = "T1081";
		public static final String T1082 = "T1082";
		public static final String T1083 = "T1083";
		public static final String T1084 = "T1084";
		public static final String T1085 = "T1085";
		public static final String T1086 = "T1086";
		public static final String T1112 = "T1112";
		public static final String T1113 = "T1113";
		public static final String T1114 = "T1114";
		public static final String T1115 = "T1115";
		public static final String T1116 = "T1116";
		public static final String T1117 = "T1117";
		public static final String T1118 = "T1118";
		
		public static final String T5005 = "T5005";
		public static final String T5008 = "T5008";
		public static final String T5009 = "T5009";
		public static final String T5011 = "T5011";
		public static final String T5012 = "T5012";
		public static final String T5013 = "T5013";
		public static final String T5014 = "T5014";
		public static final String T5015 = "T5015";
		public static final String T5016 = "T5016";
		public static final String T5017 = "T5017";
		public static final String T5018 = "T5018";
		public static final String T5019 = "T5019";
		public static final String T5020 = "T5020";
		public static final String T5031 = "T5031";
		
		public static final String T7015 = "T7015";
		public static final String T7016 = "T7016";
		public static final String T7017 = "T7017";
		public static final String T7018 = "T7018";
		public static final String T7019 = "T7019";
		public static final String T7021 = "T7021";
		public static final String T7023 = "T7023";
		public static final String T7025 = "T7025";
		public static final String T7026 = "T7026";
		public static final String T7027 = "T7027";
	}
	
	private static final String BUNDLE_NAME = "ch.agent.t2.T2Msg";

	private static final MessageBundle BUNDLE = new MessageBundle("T2", ResourceBundle.getBundle(BUNDLE_NAME));

	public static RuntimeException runtimeException(String msgKey, Throwable cause) {
		throw new RuntimeException(new T2Msg(msgKey).toString(), cause);
	}
	
	public static T2Exception exception(String key, Object... arg) {
		return new T2Exception(new T2Msg(key, arg));
	}
	
	public static T2Exception exception(Throwable cause, String key, Object... arg) {
		return new T2Exception(new T2Msg(key, arg), cause);
	}

	public T2Msg(String key, Object... args) {
		super(key, BUNDLE, args);
	}


}