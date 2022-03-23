package cn.byteroute.io;
import cn.byteroute.io.IProxyServiceCallback;
interface IProxyService {
     int getState();
     void registerCallback(in IProxyServiceCallback callback);
     void unregisterCallback(in IProxyServiceCallback callback);
}