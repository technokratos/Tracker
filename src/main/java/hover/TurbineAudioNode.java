package hover;

import com.jme3.audio.AudioNode;

/**
 * Created by denis on 14.03.17.
 */
public class TurbineAudioNode {
    final int normalSampleTrate;
    final AudioNode audioNode;
    private final Float max;
    private final Float min;

    public TurbineAudioNode(AudioNode audioNode) {
        this(audioNode, null, null);
    }

    public TurbineAudioNode(AudioNode audioNode, Float min, Float max) {
        this.audioNode = audioNode;
        normalSampleTrate = audioNode.getAudioData().getSampleRate();
        this.min = min;
        this.max = max;

    }

    public void setRatio(float ratio) {
        if (ratio > 10 || ratio < 0.25) {
            System.out.println("Strange ratio " + ratio);
            return;
        }
        final int sampleRate = (int) (normalSampleTrate * ratio);
        if (audioNode.getAudioData().getSampleRate() != sampleRate) {
            System.out.println("New sample rate "  + sampleRate);
            audioNode.stop();
            audioNode.getAudioData().setupFormat(audioNode.getAudioData().getChannels(), audioNode.getAudioData().getBitsPerSample(), sampleRate);
            audioNode.play();
        }
    }

    public void setLevel(float level) {
        System.out.println("MIN " + min + " max " + max + "  level " + level);
        setRatio((0.25f + (1f - 0.25f) * (level - min) / (max - min)));
    }
}
