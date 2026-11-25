package kr.blackr.split.sdk;

/** Versioned Messenger IPC contract. Add-on services are discovered, then bound explicitly. */
public final class WidgetProtocol {
    private WidgetProtocol() {}
    public static final int VERSION=1;
    public static final String SERVICE_ACTION="kr.blackr.customsplit.action.WIDGET_PROVIDER";
    public static final String META_ID="kr.blackr.customsplit.widget.ID";
    public static final String META_TITLE="kr.blackr.customsplit.widget.TITLE";
    public static final String META_VERSION="kr.blackr.customsplit.widget.VERSION";
    public static final String KEY_VERSION="version", KEY_INSTANCE="instance", KEY_REMOTE_VIEWS="remote_views", KEY_ERROR="error";
    /** Host -> add-on: subscribe to one visible widget; replyTo receives frames. */
    public static final int SUBSCRIBE=1;
    /** Host -> add-on: immediately stop producing updates for this instance. */
    public static final int UNSUBSCRIBE=2;
    /** Add-on -> host: one RemoteViews frame with the same instance token. */
    public static final int FRAME=3;
    /** Add-on -> host: short user-visible failure, with the same token. */
    public static final int FAILURE=4;
}
