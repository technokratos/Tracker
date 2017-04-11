package hover;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by denis on 14.03.17.
 */
public class AudioContainer {

    List<AudioWithTimes> audioNodes = new ArrayList<>();

    private final Node rootNode;
    protected final AssetManager assetManager;
    private final String source;
    private final long playBackTime;
    private final int limit;

    public AudioContainer(Node rootNode, AssetManager assetManager, String source) {
        this(rootNode, assetManager, source, 10);
    }

    public AudioContainer(Node rootNode, AssetManager assetManager, String source, int limit) {
        this.limit = limit;
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.source = source;

        final AudioNode child = addAudioNode(rootNode, assetManager, source);


        this.playBackTime = (long) (child.getAudioData().getDuration() * Math.pow(10, 9));
    }

    private AudioNode addAudioNode(Node rootNode, AssetManager assetManager, String source) {
        final AudioNode child = new AudioNode(assetManager, source, AudioData.DataType.Buffer);
        audioNodes.add(new AudioWithTimes(child));
        rootNode.attachChild(child);
        child.setVolume(8);
        return child;
    }

    public void play() {
        play(null);
    }
    public void play(Vector3f position) {
//        System.out.println("SIZE of audio container " + audioNodes.size());
        final long current = System.nanoTime() - playBackTime;
//        System.out.println("AGO time " + current + " play back " + playBackTime);
        for (AudioWithTimes audioWithTimes: audioNodes) {
             if (audioWithTimes.time < current) {
                 audioWithTimes.time = System.nanoTime();
                 if (position != null) {
                     audioWithTimes.audioNode.setLocalTranslation(position);
                 }
                 audioWithTimes.audioNode.play();
//                 System.out.println("SET time  " + audioWithTimes.time);
                 return;
             }
        }
        if (audioNodes.size() >= limit) {
            return;
        }
        final AudioNode child = addAudioNode(rootNode, assetManager, source);
        if (position != null) {
            child.setLocalTranslation(position);
        }
        final AudioWithTimes audioWithTimes = new AudioWithTimes(child);
        audioWithTimes.time = System.nanoTime();
        audioWithTimes.audioNode.play();
    }



    private class AudioWithTimes {
        private final AudioNode audioNode;
        volatile long time = 0;

        private AudioWithTimes(AudioNode audioNode) {
            this.audioNode = audioNode;
        }
    }





}
