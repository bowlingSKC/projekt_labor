package pl.animations;

import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.util.Duration;

public class Config extends Transition {

    protected static final Interpolator WEB_EASE = Interpolator.SPLINE(0.25, 0.1, 0.25, 1);
    protected final Node node;
    protected Timeline timeline;
    private boolean oldCache = false;
    private CacheHint oldCacheHint = CacheHint.DEFAULT;
    private final boolean useCache;

    public Config(final Node node, final Timeline timeline) {
        this(node, timeline, true);
    }

    public Config(final Node node, final Timeline timeline, boolean useCache) {
        this.node = node;
        this.timeline = timeline;
        this.useCache = useCache;

        statusProperty().addListener(new ChangeListener<Status>() {
            @Override
            public void changed(ObservableValue<? extends Status> observable, Status oldValue, Status newValue) {
                switch (newValue) {
                    case RUNNING:
                        starting();
                        break;
                    default:
                        stopping();
                        break;
                }
            }
        });
    }

    // Animáció elindítása után
    protected void starting() {
        if( useCache ) {
            oldCache = node.isCache();
            oldCacheHint = node.getCacheHint();
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
        }
    }

    // Anumáció leállása után
    protected void stopping() {
        if( useCache ) {
            node.setCache(oldCache);
            node.setCacheHint(oldCacheHint);
        }
    }

    @Override
    protected void interpolate(double frac) {
        timeline.playFrom(Duration.seconds(frac));
        timeline.stop();
    }
}
