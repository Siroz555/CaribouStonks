package fr.siroz.cariboustonks.core.module.cooldown;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.NonNull;

final class CooldownImpl implements Cooldown {

    private long lastTested;
    private final long timeout;

    CooldownImpl(long amount, @NonNull TimeUnit unit) {
        this.timeout = unit.toMillis(amount);
        this.lastTested = 0;
    }

    @Override
    public @NonNull OptionalLong getLastTested() {
        return lastTested == 0 ? OptionalLong.empty() : OptionalLong.of(lastTested);
    }

    @Override
    public void setLastTested(long time) {
        if (time <= 0) {
            lastTested = 0;
        } else {
            lastTested = time;
        }
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public @NonNull CooldownImpl copy() {
        return new CooldownImpl(timeout, TimeUnit.MILLISECONDS);
    }
}
