package com.quickblox.sample.groupchatwebrtc.activities;

/**
 * Created by Riccardo on 22/11/16.
 */

public interface PathRedoUndoCountChangeListener {
    void onUndoCountChanged(int undoCount);

    void onRedoCountChanged(int redoCount);
}
