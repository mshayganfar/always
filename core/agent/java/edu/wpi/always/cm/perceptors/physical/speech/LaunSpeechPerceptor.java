package edu.wpi.always.cm.perceptors.physical.speech;

import java.util.regex.*;

import org.apache.activemq.*;
import org.joda.time.*;

import edu.wpi.always.cm.perceptors.*;
import edu.wpi.always.cm.perceptors.SpeechPerception.SpeechState;

public class LaunSpeechPerceptor extends JMSMessagePerceptor<SpeechPerception> implements SpeechPerceptor{

	public static enum LaunState{
		SpeechSilent,
		SpeechNormal,
		PunctualPositiveSpeech,
		PunctualNegativeSpeech,
		PunctualBackchannel,
		PunctualAskQuestion,
		SpeechLoud,
		SpeechLoud2,
		SpeechLoud3
	}
	private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
	
	public LaunSpeechPerceptor(){
		super(url, "DEFAULT_SCOPE");
	}

	private static final Pattern MESSAGE_PATTERN = Pattern.compile("Audio_speak\\suser\\+(\\w+)\\+(\\d+)");
	@Override
	public SpeechPerception handleMessage(String content) {
		Matcher matcher = MESSAGE_PATTERN.matcher(content);
		if(matcher.find()){
			LaunState state;
			try{
				state = LaunState.valueOf(matcher.group(1));
			}catch(IllegalArgumentException e){
				state = LaunState.SpeechNormal;
			}
			switch(state){
			case SpeechSilent:
				return new SpeechPerceptionImpl(DateTime.now(), SpeechState.Silent);
			case SpeechNormal:
			case PunctualPositiveSpeech:
			case PunctualNegativeSpeech:
			case PunctualBackchannel:
			case PunctualAskQuestion:
				return new SpeechPerceptionImpl(DateTime.now(), SpeechState.Normal);
			case SpeechLoud:
			case SpeechLoud2:
			case SpeechLoud3:
				return new SpeechPerceptionImpl(DateTime.now(), SpeechState.Loud);
			}
		}
		return getLatest();
	}
}