package androidx.media.filterpacks.base;

import android.os.SystemClock;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.InputPort;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;

public final class MergeFilter extends Filter {
    public static final int MODE_LFU = 2;
    public static final int MODE_LRU = 1;
    private int mMode = 1;
    private long[] mPortScores = null;

    public MergeFilter(MffContext context, String name) {
        super(context, name);
    }

    public void setMergeMode(int mode) {
        if (isRunning()) {
            throw new IllegalStateException("Cannot update merge mode while running!");
        }
        this.mMode = mode;
    }

    public Signature getSignature() {
        return new Signature().addOutputPort("output", 2, FrameType.any()).disallowOtherOutputs();
    }

    public void onInputPortAttached(InputPort port) {
        port.setWaitsForFrame(false);
    }

    public void onInputPortOpen(InputPort port) {
        port.attachToOutputPort(getConnectedOutputPort("output"));
    }

    protected void onOpen() {
        initScores();
    }

    protected void onProcess() {
        long maxScore = Long.MIN_VALUE;
        int bestPortIx = -1;
        InputPort[] ports = getConnectedInputPorts();
        for (int i = 0; i < ports.length; i++) {
            if (ports[i].hasFrame()) {
                long score = this.mPortScores[i];
                if (score >= maxScore) {
                    maxScore = score;
                    bestPortIx = i;
                }
            }
        }
        if (bestPortIx >= 0) {
            getConnectedOutputPort("output").pushFrame(ports[bestPortIx].pullFrame());
            updateScore(bestPortIx);
        }
    }

    private void updateScore(int portIx) {
        switch (this.mMode) {
            case 1:
                this.mPortScores[portIx] = -SystemClock.elapsedRealtime();
                return;
            case 2:
                this.mPortScores[portIx] = this.mPortScores[portIx] - 1;
                return;
            default:
                throw new RuntimeException("Unknown merge mode " + this.mMode + "!");
        }
    }

    private void initScores() {
        this.mPortScores = new long[getConnectedInputPorts().length];
        for (int i = 0; i < this.mPortScores.length; i++) {
            this.mPortScores[i] = Long.MIN_VALUE;
        }
    }
}
