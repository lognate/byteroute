package cn.byteroute.io.annotation;

import androidx.annotation.IntDef;
@IntDef({ProxyState.STATE_IDLE, ProxyState.STATE_CONNECTING, ProxyState.STATE_CONNECTED, ProxyState.STATE_STOPPING, ProxyState.STATE_STOPPED})
public @interface ProxyState {
    int STATE_IDLE = -1;
    int STATE_CONNECTING = 0;
    int STATE_CONNECTED = 1;
    int STATE_STOPPING = 2;
    int STATE_STOPPED = 3;
}