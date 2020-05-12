package com.google.android.apps.photolab.storyboard.activity;

public interface IComicMoveable {

    public enum ComicMoveableKind {
        NONE,
        EDGE,
        PANEL,
        BITMAP,
        SPEECH_BUBBLE,
        CAPTION
    }

    ComicMoveableKind getMoveableKind();
}
