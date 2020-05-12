package androidx.media.filterpacks.face;

import android.annotation.TargetApi;
import android.hardware.Camera.Face;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.media.effect.EffectUpdateListener;
import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.FrameValues;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.OutputPort;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.TextureSource;

@TargetApi(14)
public final class FaceTrackerFilter extends Filter {
    private static final String FACE_TRACKER_EFFECT = "com.google.android.media.effect.effects.FaceTrackingEffect";
    private EffectContext mEffectContext = null;
    private EffectUpdateListener mFaceListener = new EffectUpdateListener() {
        public void onEffectUpdated(Effect effect, Object info) {
            synchronized (FaceTrackerFilter.this) {
                FaceTrackerFilter.this.mFaces = (Face[]) info;
            }
        }
    };
    private Effect mFaceTracker = null;
    private Face[] mFaces = null;
    private TextureSource mIgnoreTex = null;

    public FaceTrackerFilter(MffContext context, String name) {
        super(context, name);
    }

    public static boolean isSupported() {
        return EffectFactory.isEffectSupported(FACE_TRACKER_EFFECT);
    }

    public Signature getSignature() {
        FrameType imageType = FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2);
        return new Signature().addInputPort("image", 2, imageType).addOutputPort("faces", 2, FrameType.array(Face.class)).disallowOtherPorts();
    }

    protected void onPrepare() {
        this.mEffectContext = EffectContext.createWithCurrentGlContext();
        EffectFactory factory = this.mEffectContext.getFactory();
        if (EffectFactory.isEffectSupported(FACE_TRACKER_EFFECT)) {
            this.mFaceTracker = factory.createEffect(FACE_TRACKER_EFFECT);
            this.mFaceTracker.setParameter("ignoreOutput", Boolean.valueOf(true));
            this.mFaceTracker.setUpdateListener(this.mFaceListener);
            this.mIgnoreTex = TextureSource.newTexture();
            this.mIgnoreTex.allocate(640, 480);
            return;
        }
        String valueOf = String.valueOf(this);
        throw new RuntimeException(new StringBuilder(String.valueOf(valueOf).length() + 39).append("Cannot find a face-tracker engine for ").append(valueOf).append("!").toString());
    }

    protected void onProcess() {
        OutputPort outPort = getConnectedOutputPort("faces");
        FrameImage2D imageFrame = getConnectedInputPort("image").pullFrame().asFrameImage2D();
        this.mFaceTracker.apply(imageFrame.lockTextureSource().getTextureId(), imageFrame.getWidth(), imageFrame.getHeight(), this.mIgnoreTex.getTextureId());
        imageFrame.unlock();
        synchronized (this) {
            if (this.mFaces == null) {
                this.mFaces = new Face[0];
            }
            FrameValues facesFrame = outPort.fetchAvailableFrame(new int[]{this.mFaces.length}).asFrameValues();
            facesFrame.setValues(this.mFaces);
            outPort.pushFrame(facesFrame);
        }
    }

    protected void onTearDown() {
        if (this.mEffectContext != null) {
            this.mEffectContext.release();
            this.mEffectContext = null;
        }
        if (this.mIgnoreTex != null) {
            this.mIgnoreTex.release();
            this.mIgnoreTex = null;
        }
    }
}
