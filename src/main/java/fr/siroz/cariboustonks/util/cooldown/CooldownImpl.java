package fr.siroz.cariboustonks.util.cooldown;

import org.jetbrains.annotations.NotNull;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

final class CooldownImpl implements Cooldown {

    private long lastTested;
    private final long timeout;

    CooldownImpl(long amount, @NotNull TimeUnit unit) {
        this.timeout = unit.toMillis(amount);
        this.lastTested = 0;
    }

    @Override
    public @NotNull OptionalLong getLastTested() {
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
    public @NotNull CooldownImpl copy() {
        return new CooldownImpl(timeout, TimeUnit.MILLISECONDS);
    }
}
