package com.example.projectmanager.apitests.support;

/** Seconds for {@code @Timeout}: S for a simple GET, M for a single write, L for multi-step tests. */
public final class TestTimeouts {

    public static final long S = 5;
    public static final long M = 15;
    public static final long L = 30;

    private TestTimeouts() {}
}
