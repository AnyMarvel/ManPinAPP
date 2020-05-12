package com.google.android.apps.photolab.storyboard.pipeline;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.media.filterfw.Filter;
import androidx.media.filterfw.FilterGraph;
import androidx.media.filterfw.FilterGraph.Builder;
import androidx.media.filterfw.Frame;
import androidx.media.filterfw.FrameImage2D;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.GraphRunner;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;
import androidx.media.filterpacks.base.NullFilter;
import androidx.media.filterpacks.decoder.MediaDecoderSource;

public class VideoLoader {
    private static final float DOWNSCALE_FACTOR = 1.0f;
    private static final String TAG = "VideoLoader";
    private FilterGraph filterGraph;
    private GraphRunner filterGraphRunner;
    private Listener listener;
    private MffContext mffContext;
    private int videoHeight;
    private int videoWidth;

    public interface Listener {
        void onFrame(FrameImage2D frameImage2D, long j);

        void onVideoCompleted(int i);

        void onVideoError(Exception exception, boolean z);
    }

    private class Pull2DFrameFilter extends Filter {
        public Pull2DFrameFilter(MffContext context, String name) {
            super(context, name);
        }

        public Signature getSignature() {
            return new Signature().addInputPort("image", 2, FrameType.image2D(FrameType.ELEMENT_RGBA8888, 2)).disallowOtherPorts();
        }

        protected void onProcess() {
            Frame frame = getConnectedInputPort("image").pullFrame();
            VideoLoader.this.listener.onFrame(frame.asFrameImage2D(), frame.getTimestampMillis());
        }

        protected void onClose() {
            VideoLoader.this.filterGraphRunner.stop();
        }
    }

    private class CustomMediaDecoderSource extends MediaDecoderSource {
        public CustomMediaDecoderSource(MffContext context, String name) {
            super(context, name);
        }

        protected void onClose() {
            Log.i(VideoLoader.TAG, "CustomMediaDecoder was closed");
            VideoLoader.this.finishVideo();
        }
    }

    /**
     * 初始化视频加载内容
     *
     * @param context         ComicActivity
     * @param uri             视频uri地址
     * @param videoWidth      视频宽度
     * @param videoHeight     视频高度
     * @param videoDurationMS 视频长度 ms 毫秒
     * @param listener        监听器
     */
    public VideoLoader(Context context, Uri uri, int videoWidth, int videoHeight, long videoDurationMS, Listener listener) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.listener = listener;
        this.mffContext = new MffContext(context);
        setupGraph(uri);
    }

    private synchronized void finishVideo() {
        Log.d(TAG, "Done running filterGraph on thread:" + Thread.currentThread().getId());
        this.listener.onVideoCompleted(0);
    }

    public void start() {
        this.filterGraphRunner.setIsVerbose(false);
        this.filterGraphRunner.start(this.filterGraph);
    }

    public void tearDown() {
        finishVideo();
        if (this.filterGraphRunner.isRunning()) {
            this.filterGraphRunner.stop();
            this.filterGraphRunner.waitUntilStop();
            Log.i(TAG, "MFF filterGraph stopped");
        }
        Log.d(TAG, "Tearing down filterGraph");
        this.filterGraph.tearDown();
        this.filterGraphRunner.tearDown();
    }

    private void setupGraph(Uri mediaUri) {
        Builder graphBuilder = new Builder(this.mffContext);
        graphBuilder.addFilter(new CustomMediaDecoderSource(this.mffContext, "mediaSource"));
        graphBuilder.addVariable("mediaUriVar", mediaUri);
        graphBuilder.connect("mediaUriVar", "value", "mediaSource", "uri");
        graphBuilder.addVariable("outputWidth", Integer.valueOf((int) (((float) this.videoWidth) / DOWNSCALE_FACTOR)));
        graphBuilder.addVariable("outputHeight", Integer.valueOf((int) (((float) this.videoHeight) / DOWNSCALE_FACTOR)));
        graphBuilder.addFilter(new GlDownscaleFilter(this.mffContext, "glDownscaleFilter"));
        graphBuilder.connect("mediaSource", "video", "glDownscaleFilter", "image");
        graphBuilder.connect("outputWidth", "value", "glDownscaleFilter", "outputWidth");
        graphBuilder.connect("outputHeight", "value", "glDownscaleFilter", "outputHeight");
        graphBuilder.addFilter(new Pull2DFrameFilter(this.mffContext, "pull2DFrameFilter"));
        graphBuilder.connect("glDownscaleFilter", "image", "pull2DFrameFilter", "image");
        graphBuilder.addFilter(new NullFilter(this.mffContext, "nullAudioFilter"));
        graphBuilder.connect("mediaSource", "audio", "nullAudioFilter", "input");
        this.filterGraph = graphBuilder.build();
        if (this.filterGraph == null) {
            throw new RuntimeException("Unable to set up MFF filterGraph");
        }
        this.filterGraphRunner = this.filterGraph.getRunner();
    }
}
