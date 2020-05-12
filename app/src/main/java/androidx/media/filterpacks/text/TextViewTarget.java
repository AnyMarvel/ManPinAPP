package androidx.media.filterpacks.text;

import android.view.View;
import android.widget.TextView;
import androidx.media.filterfw.FrameType;
import androidx.media.filterfw.MffContext;
import androidx.media.filterfw.Signature;
import androidx.media.filterfw.ViewFilter;

public class TextViewTarget extends ViewFilter {
    private TextView mTextView = null;

    public TextViewTarget(MffContext context, String name) {
        super(context, name);
    }

    public void onBindToView(View view) {
        if (view instanceof TextView) {
            this.mTextView = (TextView) view;
            return;
        }
        throw new IllegalArgumentException("View must be a TextView!");
    }

    public Signature getSignature() {
        return new Signature().addInputPort("text", 2, FrameType.single(String.class)).disallowOtherPorts();
    }

    protected void onProcess() {
        final String text = (String) getConnectedInputPort("text").pullFrame().asFrameValue().getValue();
        if (this.mTextView != null) {
            this.mTextView.post(new Runnable() {
                public void run() {
                    TextViewTarget.this.mTextView.setText(text);
                }
            });
        }
    }
}
